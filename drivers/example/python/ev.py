###################################################################################
## MODULE     : ev
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Event interface of the driver connection layer
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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

################################################################################

#*******************************************************************************
# SPELL imports
#*******************************************************************************
from spell.lib.exception import DriverException
from spell.lang.modifiers import *
from spell.lang.constants import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import sys,os

###############################################################################
# Module import definition

__all__ = ['EV']

###############################################################################
# Superclass
import spell.lib.adapter.ev.EvInterface as superClass

###############################################################################
# The events interface is in charge of managing GCS events.
###############################################################################
class EvInterface(superClass):

    #==========================================================================
    def __init__(self):
        """
        Perform the initialization of Event Handling data here, after the 
        superclass initialization
        """
        superClass.__init__(self)
        # TODO Your initialization stuff here

    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        """
        Start the internal driver EV mechanisms

        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Your startup stuff here

    #==========================================================================
    def cleanup(self):
        """
        Perform the cleanup of the internal driver EV resources.
        """
        superClass.cleanup(self)
        # TODO Release resources and cleanup stuff here

    #==========================================================================
    def _raiseEvent(self, message, config):
        """
        Invoked by the system in order to inject an event into the GCS.

        message: string containing the event message
        config: dictionary containing the event parameters:
                    - Severity modifier (INFORMATION/WARNING/ERROR/FATAL)
                    - Scope (SCOPE_PROC,SCOPE_SYS,SCOPE_CFG) 
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the event injection

################################################################################
# Interface instance
EV = EvInterface()
