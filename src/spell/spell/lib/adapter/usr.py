###################################################################################
## MODULE     : spell.lib.adapter.usr
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: User management interface
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

__all__ = ['UserInterface']

INTERFACE_DEFAULTS = { OnFailure:ABORT | SKIP | REPEAT }

###############################################################################
class UserInterface(Configurable, Interface):
    
    """
    DESCRIPTION:
        User management library interface. This class is in charge of
        managing the underlying system users, if any.
    """
    
    #==========================================================================
    def __init__(self):
        Interface.__init__(self, "USR")
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
        LOG("Setup USER adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #==========================================================================
    def cleanup(self):
        LOG("Cleanup USER adapter interface")
        
    #==========================================================================
    def login(self, *args, **kargs):
        if len(args)<2:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        user     = args[0]
        password = args[1]
         
        return self._login( user, password, useConfig )
        
    #==========================================================================
    def logout(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        user = args[0]
         
        return self._logout( user, useConfig )

    #==========================================================================
    def isLoggedIn(self, *args, **kargs):
        if len(args)<1:
            raise SyntaxException("Wrong arguments")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        user = args[0]
         
        return self._isLoggedIn( user, useConfig )
                    
    #==========================================================================
    def _login(self, username, password, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False
        
    #==========================================================================
    def _logout(self, username, config = {}):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _isLoggedIn(self, username, config = {}):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False
