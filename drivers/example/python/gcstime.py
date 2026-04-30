###################################################################################
## MODULE     : gcstime
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Time service interface of the driver connection layer
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

#*******************************************************************************
# SPELL imports
#*******************************************************************************
from spell.lib.exception import DriverException

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
import spell.lib.adapter.gcstime.TimeInterface as superClass

###############################################################################
# The time interface is in charge of managing GCS time.
###############################################################################
class TimeInterface(superClass):

    #==========================================================================
    def __init__(self):
        """
        Perform the initialization of time handling data here, after the 
        superclass initialization
        """
        superClass.__init__(self)
        # TODO Your initialization stuff here

    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        """
        Start the internal driver TIME mechanisms

        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Your startup stuff here

    #==========================================================================
    def cleanup(self):
        """
        Perform the cleanup of the internal driver TIME resources.
        """
        superClass.cleanup(self)
        # TODO Release resources and cleanup stuff here

    #==========================================================================
    def _getUTC(self, config = {} ):
        """
        Obtain the current UTC time according to the GCS time. If the present
        example is not change, the interface provices the current system local
        time.

        config: SPELL configuration dictionary.
        """
        # Default implementation: get system local time
        return datetime.datetime.utcnow()

################################################################################
# Interface handle
TIME = TimeInterface()
