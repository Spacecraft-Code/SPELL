###################################################################################
## MODULE     : spell.lib.adapter.gcstime
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Time interface for drivers
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
from config import Configurable
from interface import Interface

#*******************************************************************************
# System Imports
#*******************************************************************************
import datetime, time, sys

###############################################################################
# Module import definition

__all__ = ['TimeInterface']

INTERFACE_DEFAULTS = { OnFailure:ABORT }

###############################################################################
class TimeInterface(Configurable,Interface):
    
    """
    DESCRIPTION:
        Time management library interface. This class is in charge of
        managing the underlying system time.
    """
    
    #==========================================================================
    def __init__(self):
        Interface.__init__(self, "TIME")
        Configurable.__init__(self)
        LOG("Created")
    
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        self.storeConfig(ctxConfig, drvConfig)
        LOG("Setup TIME adapter interface")

    #==========================================================================
    def cleanup(self):
        LOG("Cleanup TIME adapter interface")
        
    #==========================================================================
    def getUTC(self, *args, **kargs):
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._getUTC( useConfig )

    #==========================================================================
    def _getUTC(self, config = {} ):
        # Default implementation: get system local time
        return datetime.datetime.utcnow()
