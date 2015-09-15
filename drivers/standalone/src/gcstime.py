###################################################################################
## MODULE     : gcstime
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: GCS time support
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import datetime

###############################################################################
# Module import definition

__all__ = ['TIME']

###############################################################################
# Superclass
import spell.lib.adapter.gcstime
superClass = spell.lib.adapter.gcstime.TimeInterface

###############################################################################
class TimeInterface(superClass):
    
    """
    DESCRIPTION:
        Time management library interface. This class is in charge of
        managing the underlying system time.
    """
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
    
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone TIME interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone TIME interface")
        
    #==========================================================================
    def _getUTC(self, config = {} ):
        # Default implementation: get system local time
        return datetime.datetime.utcnow()
               
################################################################################
# Interface handle
TIME = TimeInterface()
