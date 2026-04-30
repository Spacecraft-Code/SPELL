###################################################################################
## MODULE     : spell.lib.adapter.databases.datasource
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Database based on local text files
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or
## modify it under the terms of the GNU Lesser General Public
## License as published by the Free Software Foundation, either
## version 3 of the License, or (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
## Lesser General Public License for more details.
##
## You should have received a copy of the GNU Lesser General Public
## License and GNU General Public License (to which the GNU Lesser
## General Public License refers) along with this library.
## If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

from spell.utils.log import LOG
import sys,os  # @UnusedImport
from spell.lib.exception import DriverException
import json
import urllib
from spell.utils.log import LOG
import operator
from tempfile import TemporaryFile, NamedTemporaryFile
urlencode = None
try:
    urlencode = urllib.urlencode
except:
    urlencode = urllib.parse.urlencode

try:
    import httplib # @UnresolvedImport @UnusedImport
except:
    import http.client as httplib  # @UnresolvedImport @Reimport

class ObjectProxy(object):

    _value = None

    #===========================================================================
    # Proxy methods for lists and dicts below 
    #===========================================================================
    def has_key(self, key):
        return (key in self._value)

    def __lt__(self, other):
        return self._value < other
   
    def insert(self,index, object):  # @ReservedAssignment
        return self._value.insert(index, object)

    def __hash__(self, *args, **kwargs):
        return hash(self._value)

    def __delattr__(self, *args, **kwargs):
        return self._value.__delattr__( *args, **kwargs)

    def __int__(self):
        return int(self._value)

    def __long__(self):
        return long(self._value)

    def __float__(self):
        return float(self._value)

    def __oct__(self):
        return oct(self._value)

    def __hex__(self):
        return hex(self._value)

    def remove(self,value):
        return self._value.remove(value)

    def __delitem__(self, *args, **kwargs):
        return self._value.__delitem__( *args, **kwargs)

    def pop(self, *args, **kwargs):
        return self._value.pop( *args, **kwargs)
    def __mul__(self, other):
        return self._value * other

    def __imul__(self, other):
        self._value *= other
        return self

    def __rmul__(self, other):
        return other * self._value

    def __ge__(self, other):
        return self._value >= other

    def __iter__(self, *args, **kwargs):
        return iter(self._value)

    def count(self,value):
        return self._value.count(value)

    def __reversed__(self, *args, **kwargs):
        return reversed(self._value)

    def index(self,value):
        return self._value.index(value)

    def __eq__(self, other):
        return self._value == other

    def __gt__(self, other):
        return self._value > other

    def sort(self,cmp=None, key=None, reverse=False):  # @ReservedAssignment
        return self._value.sort(cmp, key, reverse)

    def __contains__(self, value):
        return value in self._value

    def reverse(self, *args, **kwargs):
        return self._value.reverse( *args, **kwargs)

    def __le__(self, other):
        return self._value <= other

    def __setitem__(self, index,value):
        self._value[index] = value

    def extend(self,iterable):
        return self._value.extend(iterable)

    def __add__(self, other):
        return self._value + other
    
    def __radd__(self, other):
        return other + self._value
    
    def __iadd__(self, other):
        self._value += other
        return self

    def __sizeof__(self, *args, **kwargs):
        return len(self._value)

    def __len__(self, *args, **kwargs):
        return len(self._value)

    def __getitem__(self, index):
        return self._value[index]

    def __ne__(self,other):
        return self._value != other

    def append(self,object):  # @ReservedAssignment
        return self._value.append(object)

    def fromkeys(self,S):
        return self._value.fromkeys(S)

    def keys(self, *args, **kwargs):
        return self._value.keys( *args, **kwargs)

    def setdefault(self,k):
        return self._value.setdefault(k)

    def values(self, *args, **kwargs):
        return self._value.values( *args, **kwargs)

    def viewkeys(self, *args, **kwargs):
        return self._value.viewkeys( *args, **kwargs)

    def update(self, *args, **kwargs):
        return self._value.update( *args, **kwargs)

    def iterkeys(self, *args, **kwargs):
        return self._value.iterkeys( *args, **kwargs)

    def get(self, k):
        return self._value.get(k)

    def popitem(self, *args, **kwargs):
        return self._value.popitem( *args, **kwargs)

    def iteritems(self, *args, **kwargs):
        return self._value.iteritems( *args, **kwargs)

    def itervalues(self, *args, **kwargs):
        return self._value.itervalues( *args, **kwargs)

    def clear(self, *args, **kwargs):
        return self._value.clear( *args, **kwargs)

    def viewvalues(self, *args, **kwargs):
        return self._value.viewvalues( *args, **kwargs)

    def viewitems(self, *args, **kwargs):
        return self._value.viewitems( *args, **kwargs)

    def items(self, *args, **kwargs):
        return self._value.items( *args, **kwargs)

    def __sub__(self, other):
        return self._value - other

    def __div__(self, other):
        return operator.div(self._value, other)

    def __truediv__(self, other):
        return operator.truediv(self._value, other)

    def __floordiv__(self, other):
        return self._value // other

    def __mod__(self, other):
        return self._value ^ other

    def __divmod__(self, other):
        return divmod(self._value, other)

    def __pow__(self, other, *args):
        return pow(self._value, other, *args)

    def __lshift__(self, other):
        return self._value << other

    def __rshift__(self, other):
        return self._value >> other

    def __and__(self, other):
        return self._value & other

    def __xor__(self, other):
        return self._value ^ other

    def __or__(self, other):
        return self._value | other

    def __rsub__(self, other):
        return other - self._value

    def __rdiv__(self, other):
        return operator.div(other, self._value)

    def __rtruediv__(self, other):
        return operator.truediv(other, self._value)

    def __rfloordiv__(self, other):
        return other // self._value

    def __rmod__(self, other):
        return other % self._value

    def __rdivmod__(self, other):
        return divmod(other, self._value)

    def __rpow__(self, other, *args):
        return pow(other, self._value, *args)

    def __rlshift__(self, other):
        return other << self._value

    def __rrshift__(self, other):
        return other >> self._value

    def __rand__(self, other):
        return other & self._value

    def __rxor__(self, other):
        return other ^ self._value

    def __ror__(self, other):
        return other | self._value

    def __isub__(self, other):
        self._value -= other
        return self

    def __idiv__(self, other):
        self._value = operator.idiv(self._value, other)
        return self

    def __itruediv__(self, other):
        self._value = operator.itruediv(self._value, other)
        return self

    def __ifloordiv__(self, other):
        self._value //= other
        return self

    def __imod__(self, other):
        self._value %= other
        return self

    def __ipow__(self, other):
        self._value **= other
        return self

    def __ilshift__(self, other):
        self._value <<= other
        return self

    def __irshift__(self, other):
        self._value >>= other
        return self

    def __iand__(self, other):
        self._value &= other
        return self

    def __ixor__(self, other):
        self._value ^= other
        return self

    def __ior__(self, other):
        self._value |= other
        return self

    def __neg__(self):
        return -self._value

    def __pos__(self):
        return +self._value

    def __abs__(self):
        return abs(self._value)

    def __invert__(self):
        return ~self._value
    
    def __getslice__(self, i, j):
        return self._value[i:j]

    def __setslice__(self, i, j, value):
        self._value[i:j] = value

    def __delslice__(self, i, j):
        del self._value[i:j]
################################################################################
class HttpDataSource(ObjectProxy):

    #===========================================================================
    def __init__(self, dataSource,serviceUrl, options = None,parent=None,extension='json'):
        if options is None:
            options = {}
        self.__baseDataSource = dataSource
        self.__dataSource = dataSource
        self.__dataSourceUrl = None
        if parent:
            self.__dataSourceUrl = parent.__dataSourceUrl
        self.__serviceUrl = serviceUrl
        self.__extension = extension
        self.__options = options
        self.__parent = parent
        self.__connection = None
        self._value = None
        self.__data = None
        self.__requestUrl = None
        self.load()
        LOG("Instanciated HTTP db for " + self.__dataSourceUrl + self.__serviceUrl)
        
    #===========================================================================
    def id(self):
        return self.__requestUrl
    
    def _createHeader(self):
        try:
            from __main__ import PROC
        except:
            PROC = {}
        name = 'not_found'
        step = 'not_found'
        if 'NAME' in PROC.keys():
            name = PROC['NAME']
        if 'STEP' in PROC.keys():
            step = PROC['STEP']
            
        return {'X-Consumer-Application' : 'SPELL'     ,
                'X-Consumer-Id'          : name,
                'X-Consumer-Step'        : step } 
        
    def _checkConnection(self,conn):
        try:
            conn.request('GET', '/')
            conn.getresponse()
            conn.close()
        except:
            conn.close()
            return False
        return True
    
    def _createDataSourceConnection(self):
        conn = None
        try:
            if self.__dataSourceUrl.startswith('http://'):
                conn = httplib.HTTPConnection(self.__dataSourceUrl[len('http://'):],timeout=5)
            elif self.__dataSourceUrl.startswith('https://'):
                import ssl
                context = ssl._create_unverified_context()

                conn = httplib.HTTPSConnection(self.__dataSourceUrl[len('https://'):], timeout=5, context=context)
            else:
                raise DriverException('Could not recognize url schema for ' + str(self.__dataSourceUrl) + '!')
        except Exception as ex:
            raise DriverException('Could not establish connection to url ' + str(self.__dataSourceUrl) + '!',str(ex))
        
        if not self._checkConnection(conn):
            raise DriverException('Could not establish connection to url ' + str(self.__dataSourceUrl) + '!')
    
        return conn
    def _selectNextDataServer(self):
        if not self.__dataSource:
            return False
        self.__dataSource = self.__dataSource.nextDataServer()
        return True

    def _openConnection(self):
        if self.__connection:
            self.__connection.close()
        if not self.__dataSource:
            return None
            
        self.__dataSourceUrl = self.__dataSource.url
        self.__connection = self._createDataSourceConnection()

        return self.__connection
    
    def downloadFile(self, options=None):
        conn = None
        lastExceptionMessage = ''
        self.__dataSource = self.__baseDataSource
        while True:
            try:
                LOG('Loading from ' + str(self.__dataSource))
                conn = self._openConnection()
                # Read the data from the file
                if conn:
                    return self._readDataFile(conn, options)
                else:
                    break
            except DriverException as ex:
                LOG('Failed loading from ' + str(self.__dataSource) + '\n' + str(ex))
                lastExceptionMessage += 'Failed to Load from ' + self.__dataSource.name + ': ' + str(ex) + '\n\n'
                if not self._selectNextDataServer():
                    break    
            finally:
                if conn:
                    conn.close()
        raise DriverException(lastExceptionMessage)

    def openService(self, serviceUrl,options=None,extension='json'):
        
        return HttpDataSource(self.__dataSource,serviceUrl,options,self,extension)
    
    def getParent(self):
        return self.__parent
    
    def create(self,value = None):
        self._value = value
        return self

    def reset(self):
        if self.__extension == 'json':
            try:
                self._value = json.loads(self.__data)
            except Exception as ex:
                raise DriverException('Could not parse ' + str(self.__requestUrl) + ' as json!',str(ex))
        else:
            self._value = self.__data
    #===========================================================================
    def load(self):
        # If the file exists go on directly. Otherwise try to find it
        # no matter the case of the name
        conn = None
        lastExceptionMessage = ''
        self.__dataSource = self.__baseDataSource
        while True:
            try:
                LOG('Loading from ' + str(self.__dataSource))
                conn = self._openConnection()
                # Read the data from the file
                if conn:
                    self._readData(conn)
                    return
                else:
                    break
            except DriverException as ex:
                LOG('Failed loading from ' + str(self.__dataSource) + '\n' + str(ex))
                lastExceptionMessage += 'Failed to Load from ' + self.__dataSource.name + ': ' + str(ex) + '\n\n'
                if not self._selectNextDataServer():
                    break             
            finally:
                if conn:
                    conn.close()
        raise DriverException(lastExceptionMessage)


    #===========================================================================
    def _readDataFile(self,conn,options = None):
        # Load the file contents
        try: 
            params = ''
            if options:
                self.__options.update(options)
            if self.__options:
                params = "?" + urlencode({ k.lower():v for k,v in self.__options.items()})
            conn.request("GET", self.__serviceUrl + params,headers=self._createHeader())
            r = conn.getresponse()
            if r.status == 200:
                self.__data = r.read()
                try:
                    dataHome = os.getenv("SPELL_DATA", (os.getenv("SPELL_HOME") + os.sep + "data") ).replace('/', os.sep)
                    if dataHome is None:
                        raise DriverException("SPELL_DATA environment variable not defined")

                    # Append the data home to the path
                    thePath = dataHome + os.sep 
                    with NamedTemporaryFile(suffix='.imp',delete=False,dir=thePath) as msg:
                        msg.write(self.__data)
                        msg.close()
                        return msg.name[len(thePath):]
                except Exception as ex:
                    raise DriverException('Could not parse ' + str(self.__dataSourceUrl +  self.__serviceUrl + params) + ' as '+txt+'!',str(ex))
            else:
                raise DriverException('Received ' + str(r.status) + ' ' + str(r.reason) + ' for url ' + str(self.__dataSourceUrl +  self.__serviceUrl + params))
        except Exception as ex:
            raise DriverException('Could not request ' + str(self.__dataSourceUrl +  self.__serviceUrl + params) + '!',str(ex))
        finally:
            conn.close()
    
    #===========================================================================
    def _readData(self, conn):
        # Load the file contents
        try: 
            params = ''
            if self.__options:
                params = "?" + urlencode({ k.lower():v for k,v in self.__options.items()})
            conn.request("GET", self.__serviceUrl + params,headers=self._createHeader())
            r = conn.getresponse()
            if r.status == 200:
                self.__data = r.read()
                if self.__extension == 'json':            
                    try:
                        self._value = json.loads(self.__data)
                    except Exception as ex:
                        raise DriverException('Could not parse ' + str(self.__dataSourceUrl +  self.__serviceUrl + params) + ' as json!',str(ex))
                else:
                    self._value = self.__data
            else:
                raise DriverException('Received ' + str(r.status) + ' ' + str(r.reason) + ' for url ' + str(self.__dataSourceUrl +  self.__serviceUrl + params))
        except Exception as ex:
            raise DriverException('Could not request ' + str(self.__dataSourceUrl +  self.__serviceUrl + params) + '!',str(ex))
        finally:
            conn.close()
        
    #===========================================================================
    def _writeData(self):
        conn = None
        try: 
            conn = self._openConnection()
            try:
                if self.__extension == 'json':
                    if type(self._value) == HttpDataSource:
                        data = json.dumps(self._value.get())
                    else:
                        data = json.dumps(self._value)
                else:
                    data = self._value
            except Exception as ex:
                raise DriverException('Could not parse ' + repr(self._value) + ' as json!',str(ex))
            
            conn.request("PUT", self.__requestUrl,body=data,headers=self._createHeader())
            r = conn.getresponse()
            if r.status != 200:
                raise DriverException('Received ' + str(r.status) + ' ' + str(r.reason) + ' while updating url ' + str(self.__requestUrl))
        except Exception as ex:
            raise DriverException('Could not request ' + str(self.__requestUrl) + '!',str(ex))
        finally:
            if conn:
                conn.close()
    
    def uploadObject(self,obj,config):
        conn = None
        try: 
            conn = self._openConnection()
            try:
                if self.__extension == 'json':
                    data = json.dumps(obj)
                else:
                    data = str(obj)
            except Exception as ex:
                raise DriverException('Could not parse ' + repr(self._value) + ' as json!',str(ex))
            
            conn.request("PUT", self.__requestUrl,body=data,headers=self._createHeader())
            r = conn.getresponse()
            if r.status != 200:
                raise DriverException('Received ' + str(r.status) + ' ' + str(r.reason) + ' while updating url ' + str(self.__requestUrl))
            try:
                self.__data = r.read()
                self._value = json.loads(self.__data)
            except Exception as ex:
                pass
            
        except Exception as ex:
            raise DriverException('Could not request ' + str(self.__requestUrl) + '!',str(ex))
        finally:
            if conn:
                conn.close()
          
    def getJsonObject(self):
        return self._value
    
    def getRawJson(self):
        return self.__data
    
    #===========================================================================
    def reload(self):
        self.load()
        
    #===========================================================================
    def __repr__(self):
        return self._value.__repr__()
    
    #===========================================================================
    def __str__(self):
        return self.__data 

