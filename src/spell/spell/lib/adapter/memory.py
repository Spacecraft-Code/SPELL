###################################################################################
## MODULE     : spell.lib.adapter.ranging
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Memory interface
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

__all__ = ['MemInterface']

INTERFACE_DEFAULTS = {  OnFailure:ABORT | SKIP | CANCEL } 

###############################################################################
class MemInterface(Configurable, Interface):

    """
    This class provides the Memory management interface. Feature methods shall
    be overriden by driver concrete interfaces.
    """

    __ctxName = None
    __useConfig = {}

    #===========================================================================
    def __init__(self):
        Interface.__init__(self, "MEM")
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
        LOG("Setup MEM adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #===========================================================================
    def cleanup(self):
        LOG("Cleanup MEM adapter interface")

    #===========================================================================
    def generateReport(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        image  = args[0]
        rtype  = args[1]
        begin  = args[2]
        end    = args[3]
        source = args[4]
        return self._generateReport( image, rtype, begin, end, source, useConfig )

    #===========================================================================
    def compareImages(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        image1  = args[0]
        image2  = args[1]
        rtype   = args[2]
        begin   = args[3]
        end     = args[4]
        source  = args[5]
        return self._compareImages( image1, image2, rtype, begin, end, source, useConfig )

    #===========================================================================
    def memoryLookup(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        name   = args[0]
        address= args[1]
        image  = args[2]
        rtype  = args[3]
        source = args[4]
        return self._memoryLookup( name, address, image, rtype, source, useConfig )

    #===========================================================================
    def _generateReport(self, image, report_type, begin, end, source, config):
        REGISTRY['CIF'].write("Memory management service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _compareImages(self, image1, image2, report_type, begin, end, source, config):
        REGISTRY['CIF'].write("Memory management service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _memoryLookup(self, name, address, image, report_type, source, config):
        REGISTRY['CIF'].write("Memory management service not implemented on this driver", {Severity:WARNING})
        return None
