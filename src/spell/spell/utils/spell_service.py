###################################################################################
## MODULE     : spell.utils.spell_service
## DATE       : Mar 14, 2018
## PROJECT    : SPELL
## DESCRIPTION: Spell Service helper class
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
from spell.utils.log import LOG
from spell.utils.timing import RepeatingThread
import os,time
from spell.utils.grpc_utils import GRPCThreadPool

try:
    global RegisterSpellService
    if not callable(RegisterSpellService):
        def SupportServices():
            return False
        def RegisterSpellService(*args,**kargs):
            return False
    else:
        def SupportServices():
            return True
except: 
    def RegisterSpellService(*args,**kargs):
        return False
    def SupportServices():
        return False

STARTUP_NORMAL = 'STARTUP_NORMAL'
class AbstractSpellService(object):
    def _init_worker_pool(self, name):
        self._pool = GRPCThreadPool(name,20)
        self._threads = []
    def execute(self,targetFunction,*args,**kargs):
        return self._pool.submit(lambda :targetFunction(*args,**kargs))
    
    def repeatFunction(self,targetFunction,period,*args,**kargs):
        repeatingFunction = RepeatingThread(period * 1.0, lambda : targetFunction(*args,**kargs))
        repeatingFunction.start()
        self._threads += [repeatingFunction]
        return repeatingFunction

class SpellService(AbstractSpellService):
    __attributes = None
    __impl = None
    __ping = None
    
    def __init__(self,*args,**kargs):
        self.__attributes = (args,{k.lower():v for k,v in kargs.items()})

    def __call__(self,serviceImpl):
        self.__impl = serviceImpl
        RegisterSpellService(self)
        
        if not hasattr( serviceImpl, '_init_worker_pool') or not callable(serviceImpl._init_worker_pool):
            serviceImpl._init_worker_pool = self._init_worker_pool
            serviceImpl._init_worker_pool(self.getName())
       
        
        if not hasattr( serviceImpl, 'execute') or not callable(serviceImpl.execute):
            serviceImpl.execute = self.execute
        if not hasattr( serviceImpl, 'repeatFunction') or not callable(serviceImpl.repeatFunction):
            serviceImpl.repeatFunction = self.repeatFunction
        
        if not hasattr( serviceImpl, 'isAlive') or not callable(serviceImpl.isAlive):
            serviceImpl.isAlive = self.isAlive

        self.__impl = serviceImpl(*self.__attributes[0],**self.__attributes[1])
        
        return self.__impl
    
    def _startWatchDog(self,callback):
        if self.__ping != None:
            self.__ping.cancel()
        
        self.__ping = RepeatingThread(1.0, lambda : callback(self.getName()))
        self.__ping.start()
#         
    def _start(self):
        if hasattr( self.__impl, 'startService') and callable(self.__impl.startService):
            self.__impl.startService()
            
        while self._isAlive():
            time.sleep(1);
            
        print('exiting service call')
    
    def _getStartCode(self):
        return self._start
    
    def shouldRequestRestart(self):
        if hasattr( self.__impl, 'shouldRequestRestart') and callable(self.__impl.shouldRequestRestart):
            return self.__impl.shouldRequestRestart()
        return False
    
    def _isAlive(self):
        if hasattr( self.__impl, 'isAlive') and callable(self.__impl.isAlive):
            return self.__impl.isAlive()
        
        return False
    
    def isAlive(self):
        return True
    
    def useCommandOverride(self):
        if hasattr( self.__impl, 'useCommandOverride') and callable(self.__impl.useCommandOverride):
            return self.__impl.useCommandOverride()
        return False
    
    def getCommandOverride(self,startTimeout,loginTimeout,configFile,contextName,ipcPort,instanceId,warmstartFile):
        if hasattr( self.__impl, 'getCommandOverride') and callable(self.__impl.getCommandOverride):
            return self.__impl.getCommandOverride(startTimeout,loginTimeout,configFile,contextName,ipcPort,instanceId,warmstartFile)
        return None
    
    def getName(self):
        return self.__attributes[1]['name']
        
    def getBasePath(self):
        f = self.getMainFile()
        return f[:f.rfind(os.sep)]
        
    def getMainFile(self):
        return self.__impl.__init__.__func__.__code__.co_filename.replace('.pyc','.py')     
# @SpellService(name='TestService', port=9999)
# class ServiceImpl(object):
#     def __init__(self,name,port):
#         print 'SPELLService created with name: ' + name + ' on port ' + str(port)
#      
#     def _startupService(self):
#         return 
#      
#     def _isAlive(self):
#         return True
#      
#     def test(self):
#         print 'test called!'
#          