###################################################################################
## MODULE     : spell.lib.adapter.resources
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Resource management interface
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
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.registry import REGISTRY

#*******************************************************************************
# Local Imports
#*******************************************************************************
from config import Configurable
from interface import Interface

#*******************************************************************************
# System Imports
#*******************************************************************************
import threading,thread,time

###############################################################################
# Module import definition

__all__ = ['ResourceInterface']

INTERFACE_DEFAULTS = { OnFailure:ABORT | SKIP | REPEAT }

###############################################################################
class ResourceInterface(Configurable,Interface):
    
    """
    DESCRIPTION:
        Resource management library interface. This class is in charge of
        managing the underlying system resources.
    """
    
    # List of resources to check
    toCheck = []
    # Status of each resource (OK/NOK)
    rscStatus = {}
    # Callbacks for resource monitoring
    rscCallbacks = []
    
    #==========================================================================
    def __init__(self):
        Interface.__init__(self, "RSC")
        Configurable.__init__(self)
        LOG("Created")
        self.toCheck = [ 'TM', 'TC' ]
        self.rscStatus = {}
        self.rscCallbacks = []
    
    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )
    
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        self.storeConfig(ctxConfig, drvConfig)
        LOG("Setup RSC adapter interface")

    #==========================================================================
    def cleanup(self):
        LOG("Cleanup RSC adapter interface")
        
    #==========================================================================
    def setLink(self, *args, **kargs):
        if len(args)<2:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        linkName = args[0]
        enable = args[1]
         
        return self._setLink( linkName, enable, useConfig )

    #==========================================================================
    def _setLink(self, linkName, enable, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False
        
    #==========================================================================
    def checkLink(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        linkName = args[0]
         
        return self._checkLink( linkName, useConfig )

    #==========================================================================
    def setResource(self, *args, **kargs):
        if len(args)<2:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        resourceName = args[0]
        resourceValue = args[1]
         
        return self._setResource( resourceName, resourceValue, useConfig )

    #==========================================================================
    def _setResource(self, resourceName, resourceValue, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False
    
    #==========================================================================
    def getResource(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        resourceName = args[0]
         
        return self._getResource( resourceName, useConfig )

    #==========================================================================
    def _getResource(self, resourceName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return None

    #==========================================================================
    def getResourceStatus(self, *args, **kargs ):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        resource = args[0]
        return self._getResourceStatus( resource, useConfig )
        
    #==========================================================================
    def isResourceOK(self, *args, **kargs ):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        resource = args[0]
        return self._isResourceOK( resource, useConfig )

    #==========================================================================
    def addResourceStatusCallback(self, callbackClass ):
        LOG("Add resource monitor: " + repr(callbackClass))
        self.rscCallbacks.append(callbackClass)        

    #==========================================================================
    def _getResourceStatus(self, resourceName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return None

    #==========================================================================
    def _isResourceOK(self, resourceName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def updateStatus(self, resource, status):
        LOG("Resource status change: " + repr(resource) + ":" + repr(status))
        self.rscStatus[resource] = status
        if len(self.rscCallbacks)>0:
            for cbk in self.rscCallbacks:
                cbk.notifyUpdate(resource,status)
    
    
