###################################################################################
## MODULE     : spell.lib.adapter.ranging
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Memory interface
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
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['MemInterface']

INTERFACE_DEFAULTS = {  OnFailure:ABORT | SKIP | CANCEL }

###############################################################################
# Superclass
import spell.lib.adapter.memory
superClass = spell.lib.adapter.memory.MemInterface

###############################################################################
class MemInterface( superClass ):

    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")

    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig,drvConfig)
        LOG("Setup empty MEM interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup empty MEM interface")

    #===========================================================================
    def _generateReport(self, image, report_type, begin, end, source, config):
        raise DriverException("Cannot perform operation", "Not supported by driver")

    #===========================================================================
    def _compareImages(self, image1, image2, report_type, begin, end, source, config):
        raise DriverException("Cannot perform operation", "Not supported by driver")

    #===========================================================================
    def _memoryLookup(self, name, report_type, source, config):
        raise DriverException("Cannot perform operation", "Not supported by driver")

    #===========================================================================
    def _dataObjectLookup(self, name, type, config):
        raise DriverException("Cannot perform DataObjectLookup operation", "Not supported by driver")

    #===========================================================================
    def _dataItemLookup(self, name, type, source, valueFormat, config):
        raise DriverException("Cannot perform DataItemLookup operation", "Not supported by driver")

    #===========================================================================
    def _getObjectIDs(self, type, source, config):
        raise DriverException("Cannot perform GetObjectIDs operation", "Not supported by driver")
