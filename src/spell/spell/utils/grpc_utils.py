###################################################################################
## MODULE     : spell.utils.grpc_utils
## DATE       : Mar 14, 2018
## PROJECT    : SPELL
## DESCRIPTION: GRPC helper class
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

#*******************************************************************************
# SPELL Imports
#*******************************************************************************
from .log import LOG
from spell.lib.adapter.utctime import TIME, NOW
from spell.stubs.SPELLDriver_pb2 import Dict, List, Time

#*******************************************************************************
# Local Imports
#*******************************************************************************

#*******************************************************************************
# System Imports
#*******************************************************************************
import threading, weakref
from grpc import RpcError
from spell.config.reader import Config
from concurrent.futures.thread import ThreadPoolExecutor, _worker,\
    _threads_queues

import google.protobuf.wrappers_pb2 as grpc_wrappers
from google.protobuf.any_pb2 import Any
from google.protobuf.empty_pb2 import Empty
import sys
#*******************************************************************************
# Exceptions
#*******************************************************************************

#*******************************************************************************
# Module globals
#*******************************************************************************

###############################################################################

PACK_MSG_MAP = {
        bool      : lambda value : grpc_wrappers.BoolValue(value=value),
        float     : lambda value : grpc_wrappers.DoubleValue(value=value),
        int       : lambda value : grpc_wrappers.Int64Value(value=value),
        str       : lambda value : grpc_wrappers.StringValue(value=value),
        dict      : lambda value : CreateDictMessage(value),
        list      : lambda value : CreateListMessage(value),
        tuple     : lambda value : CreateListMessage(value),
        TIME      : lambda value : Time(timeMsg = str(value)),
        type(None): lambda value : Any()
    }

UNPACK_MSG_MAP = {
        grpc_wrappers.BoolValue   : lambda value : value.value,
        grpc_wrappers.BytesValue  : lambda value : value.value,
        grpc_wrappers.DoubleValue : lambda value : value.value,
        grpc_wrappers.FloatValue  : lambda value : value.value,
        grpc_wrappers.Int32Value  : lambda value : value.value,
        grpc_wrappers.Int64Value  : lambda value : value.value,
        grpc_wrappers.StringValue : lambda value : str(value.value),
        grpc_wrappers.UInt32Value : lambda value : value.value,
        grpc_wrappers.Int64Value  : lambda value : value.value,
        Dict                      : lambda value : UnpackDictMessage(value),
        List                      : lambda value : UnpackListMessage(value),
        Time                      : lambda value : TIME(str(value.timeMsg)),
        Any                       : lambda value : None
    }

class GRPCThreadPool(ThreadPoolExecutor):
    def __init__(self,desc,*args,**kargs):
        super(GRPCThreadPool, self).__init__(*args,**kargs)
        self._desc = desc
    def _adjust_thread_count(self):
        # When the executor gets lost, the weakref callback will wake up
        # the worker threads.
        def weakref_cb(_, q=self._work_queue):
            q.put(None)
        # TODO(bquinlan): Should avoid creating new threads if there are more
        # idle threads than items in the work queue.
        num_threads = len(self._threads)

        pendingTasks = self._work_queue.qsize()
        #print '[ThreadPool-%s] Pending Tasks: %s'%(self._desc,pendingTasks)
        #print '[ThreadPool-%s] Number of available Workers: %s'%(self._desc,num_threads)

        if num_threads < self._max_workers and pendingTasks > num_threads:
            thread_name = '%s_%d' % (self._thread_name_prefix or self,
                                     num_threads)
            if sys.version_info.major == 3:
                t = threading.Thread(name=thread_name, target=_worker,
                                 args=(weakref.ref(self, weakref_cb),
                                       self._work_queue,
                                       self._initializer,
                                       self._initargs))
            else:
                t = threading.Thread(name=thread_name, target=_worker,
                                     args=(weakref.ref(self, weakref_cb),
                                           self._work_queue))
            t.daemon = True
            t.start()
            self._threads.add(t)
            _threads_queues[t] = self._work_queue

#==============================================================================
def unicode2string(s):
    if isinstance(s, dict):
        return dict([(unicode2string(key), unicode2string(value)) \
                    for key, value in s.items()])
    elif isinstance(s, list):
        return [unicode2string(element) for element in s]
    elif isinstance(s, str):
        return s.encode('utf-8')
    else:
        return s

#==============================================================================
def ConvertFromUnicode(method):
    def convertit(*args, **kw):
        result = method(*args, **kw)
        return unicode2string(result)

    return convertit

#==============================================================================
def LogFunctionInput(method):
    def logit(*args, **kw):
        if 'request' in list(kw.keys()):
            request = kw['request']
        else:
            request = args[1]
        #REGISTRY['CIF'].remoteLog('[DRIVER] Driver function %s called with arguments %s' \
        #                          %(method.__name__, request))
        LOG('[DRIVER] Driver function %s called with arguments %s' \
            % (method.__name__, request))
        result = method(*args, **kw)

        return result

    return logit

#==============================================================================
def UnpackDictMessage(value):
    newDict = {}
    for entry in value.entries:
        newDict[UnpackValue(entry.key)] = UnpackValue(entry.value)
    #ENDFOR

    return newDict

#==============================================================================
def UnpackListMessage(value):
    newList = []
    for entry in value.entries:
        newList.append(UnpackValue(entry))
    #ENDFOR

    return newList

#==============================================================================
def CreateDictMessage(value):
    newDict = Dict()
    for key in list(value.keys()):
        entry = newDict.entries.add()
        entry.key.CopyFrom(PackValue(key))
        entry.value.CopyFrom(PackValue(value[key]))
    #ENDFOR

    return newDict

#==============================================================================
def CreateListMessage(value):
    newList = List()
    for key in value:
        newList.entries.add().CopyFrom(PackValue(key))
    #ENDFOR

    return newList

#==============================================================================
def PackValue(value):
    if type(value) not in list(PACK_MSG_MAP.keys()):
        raise SyntaxError('Unknown data type %s received!' % type(value))
    result = Any()
    result.Pack(PACK_MSG_MAP[type(value)](value=value))

    return result

#==============================================================================
def UnpackValue(msg):
    for msgType in UNPACK_MSG_MAP:
        if msgType.DESCRIPTOR.full_name == msg.TypeName():
            newMsg = msgType()
            success = msg.Unpack(newMsg)
            if success:
                return UNPACK_MSG_MAP[msgType](newMsg)
    #ENDFOR
    raise SyntaxError('Unknown package type %s:%s received!' \
                      % (msg.TypeName(), repr(msg)))

#==============================================================================
def AppendSpellConfig(field, config):
    field.CopyFrom(PackValue(config))

    return
