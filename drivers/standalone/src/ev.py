###################################################################################
## MODULE     : ev
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Event interface of the driver connection layer
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
import sys

###############################################################################
# Module import definition

__all__ = ['EV']

###############################################################################
# Superclass
import spell.lib.adapter.ev
superClass = spell.lib.adapter.ev.EvInterface

###############################################################################
class EvInterface(superClass):
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
    
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone EV interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone EV interface")
        
    #==========================================================================
    def _raiseEvent(self, message, config):

        severity = config.get(Severity)
        scope = config.get(Scope)

        if severity == INFORMATION:
            severity = "INFO"
        elif severity == WARNING:
            severity = "WARN"
        elif severity == ERROR:
            severity = "ERROR"
        elif severity == FATAL:
            severity = "FATAL"
            
        if scope == SCOPE_PROC:
            scope = "PROC"
        elif scope == SCOPE_SYS:
            scope = "SYSTEM"
        elif scope == SCOPE_CFG:
            scope = "CONFIG"
        else:
            scope = "UNKNOWN"
        sys.stderr.write("[EVENT] " + repr(message) + " (" + repr(severity) + ":" + repr(scope) + ")\n")
            
################################################################################
# Interface instance
EV = EvInterface()            