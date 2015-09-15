###################################################################################
## MODULE     : spell.lib.adapter.config
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Base configurable entity and configuration interface for drivers
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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

#*******************************************************************************
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import SyntaxException

#*******************************************************************************
# Local Imports
#*******************************************************************************
from interface import Interface

#*******************************************************************************
# System Imports
#*******************************************************************************


###############################################################################
# Module import definition

__all__ = ['ConfigInterface,Configurable']

INTERFACE_DEFAULTS = {}

NO_CONFIG = [ 'command', 'commands', 'sequence', 'args', 'verify', 'config' ]

###############################################################################
class Configurable(object):
    
    __config = {}
    
    #==========================================================================
    def __init__(self):
        self.__config = {}
    
    #==========================================================================
    def setConfig(self, source ):
        if isinstance(source,Configurable):
            self.__config = source.getConfig()
        elif type(source)==dict:
            self.__config = source.copy()
        else:
            raise BaseException("Cannot set configuration from " + repr(source))
            
    #==========================================================================
    def getConfig(self, key = None):
        if key is not None:
            if self.__config.has_key(key):
                return self.__config.get(key)
            return None
        return self.__config.copy()

    #==========================================================================
    def addConfig(self, key, value):
        self.__config[key] = value

    #==========================================================================
    def updateConfig(self, key, value):
        self.__config.update({key:value})

    #==========================================================================
    def hasConfig(self, key):
        return self.__config.has_key(key)

    #==========================================================================
    def delConfig(self, key):
        if self.__config.has_key(key):
            del self.__config[key]
    
    #==========================================================================
    def buildConfig(self, args, kargs, secondary = {}, defaults = {} ):
        useConfig = {}
        # Parameters coming from defaults
        useConfig.update(defaults)
        # Parameters coming from a secondary source (interfaces)
        useConfig.update(secondary)
        # Parameters coming from this same entity
        useConfig.update(self.__config)
        # Parameters coming from user arguments
        
        # Then update the dict with the dictionary type arguments only
        if len(args)>0:
            for arg in args:
                if type(arg)==dict:
                    useConfig.update(arg)
        
        # Parse named arguments, if any
        if len(kargs)>0 and kargs.has_key('config'):
            # Then update the dict with the contents of 'config'
            useConfig.update(kargs.get('config'))
            kargs.pop('config')
        
        # Update the dict with remaining kargs
        for key in kargs.keys():
            if not key in NO_CONFIG:
                useConfig[key] = kargs.get(key)
                
        return useConfig

    #==========================================================================
    def checkConfig(self, globals, locals):
        for key in self.__config:
            try:
                object = eval(key, globals, locals)
            except:
                raise SyntaxException("Unknown modifier: " + repr(key))
            if type(object)!=str:
                raise SyntaxException("Not a modifier: " + repr(key))
        
###############################################################################
class ConfigInterface(Configurable,Interface):
    """
    DESCRIPTION:
        Base class for driver configuration classes. Child classes shall
        implement the setup() and cleanup() methods. The former is used
        for preparing all objects needed by the driver to work, and the 
        latter is used for cleaning up these objects.
    """
    #==========================================================================
    def __init__(self):
        Interface.__init__(self, "CONFIG")
        LOG("Created")
    
    #==========================================================================
    def setup(self, contextConfig, driverConfig ):
        LOG("Setup CONFIG adapter interface")
        self.storeConfig( contextConfig, driverConfig )
        self.setConfig( INTERFACE_DEFAULTS )

    #==========================================================================
    def cleanup(self, shutdown = False):
        LOG("Cleanup CONFIG adapter interface")
        
    
