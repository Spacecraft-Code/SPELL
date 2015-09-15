###################################################################################
## MODULE     : spell.lib.adapter.task
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Task management interface
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

###############################################################################
# Module import definition

__all__ = ['TaskInterface']

INTERFACE_DEFAULTS = { OnFailure:ABORT | SKIP | REPEAT }

###############################################################################
class TaskInterface(Configurable,Interface):
    
    """
    DESCRIPTION:
        Task management library interface. This class is in charge of
        managing the underlying system processes, if any.
    """
    
    #==========================================================================
    def __init__(self):
        Interface.__init__(self, "TASK")
        Configurable.__init__(self)
        LOG("Created")
    
    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )

    #==========================================================================
    def setup(self, ctxConfig, drvConfig ):
        LOG("Setup TASK adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()
    
    #==========================================================================
    def cleanup(self):
        LOG("Cleanup TASK adapter interface")

    #==========================================================================
    def openDisplay(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        displayName  = args[0]
         
        return self._openDisplay( displayName, useConfig )

    #==========================================================================
    def printDisplay(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        displayName  = args[0]
         
        return self._printDisplay( displayName, useConfig )

    #==========================================================================
    def closeDisplay(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        displayName  = args[0]
         
        return self._closeDisplay( displayName, useConfig )
        
    #==========================================================================
    def startTask(self, *args, **kargs):
        if len(args)<2:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        # Reconfigure the host if needed
        if not useConfig.has_key(Host):
            useConfig[Host]=REGISTRY['EXEC'].getControllingHost()
        
        taskName  = args[0]
        if len(args)==1:
            arguments = ""
        else:
            arguments = args[1]
         
        return self._startTask( taskName, arguments, useConfig )
        
    #==========================================================================
    def stopTask(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        taskName = args[0]
         
        return self._stopTask( taskName, useConfig )

    #==========================================================================
    def checkTask(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        taskName = args[0]
         
        return self._checkTask( taskName, useConfig )

    #==========================================================================
    def _startTask(self, taskName, arguments, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False
        
    #==========================================================================
    def _stopTask(self, taskName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _checkTask(self, taskName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _openDisplay(self, displayName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _printDisplay(self, displayName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False
        
    #==========================================================================
    def _closeDisplay(self, displayName, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

