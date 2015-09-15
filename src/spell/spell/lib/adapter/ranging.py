###################################################################################
## MODULE     : spell.lib.adapter.ranging
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Ranging  interface
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lib.registry import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from config import Configurable
from interface import Interface

#*******************************************************************************
# System imports
#*******************************************************************************
import time,string,sys

###############################################################################
# Module import definition

__all__ = ['RngInterface']

INTERFACE_DEFAULTS = {  OnFailure:ABORT | SKIP | CANCEL } 

###############################################################################
class RngInterface(Configurable, Interface):

    """
    This class provides the Ranging management interface. Feature methods shall
    be overriden by driver concrete interfaces.
    """

    __ctxName = None
    __useConfig = {}

    #===========================================================================
    def __init__(self):
        Interface.__init__(self, "RNG")
        Configurable.__init__(self)
        self.__ctxName = None
        LOG("Created")
        
    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )

    #===========================================================================
    def setup(self, ctxConfig, drvConfig):
        LOG("Setup RNG adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #===========================================================================
    def cleanup(self):
        LOG("Cleanup RNG adapter interface")

    #===========================================================================
    def enableRanging(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._enableRanging( useConfig )

    #===========================================================================
    def disableRanging(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._disableRanging( useConfig )

    #===========================================================================
    def startRanging(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._startRanging( args[0], args[1], useConfig )

    #===========================================================================
    def abortRanging(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._abortRanging( useConfig )

    #===========================================================================
    def startCalibration(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._startCalibration( args[0], args[1], useConfig )

    #===========================================================================
    def setBasebandConfig(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._setBasebandConfig( args[0], args[1], args[2], useConfig )

    #===========================================================================
    def getBasebandConfig(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._getBasebandConfig( args[0], args[1], useConfig )

    #===========================================================================
    def getRangingStatus(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._getRangingStatus( useConfig )

    #===========================================================================
    def getBasebandNames(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._getBasebandNames( useConfig )

    #===========================================================================
    def getAntennaNames(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._getAntennaNames( useConfig )

    #===========================================================================
    def getRangingPaths(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._getRangingPaths( useConfig )

    #===========================================================================
    def _enableRanging(self, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _disableRanging(self, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _abortRanging(self, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _startRanging(self, bbe, antenna, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _startCalibration(self, bbe, antenna, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _getBasebandConfig(self, bbe, param, config ):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return None

    #===========================================================================
    def _setBasebandConfig(self, bbe, param, value, config ):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _getAntennaNames(self, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return []

    #===========================================================================
    def _getRangingPaths(self, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return []

    #===========================================================================
    def _getBasebandNames(self, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return []

    #===========================================================================
    def _getRangingStatus(self, config):
        REGISTRY['CIF'].write("Ranging service not implemented on this driver", {Severity:WARNING})
        return None
