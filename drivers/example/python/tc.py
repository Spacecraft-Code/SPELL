###################################################################################
## MODULE     : tc
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telecommand interface of the driver connection layer
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
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['TC']

###############################################################################
# Superclass
import spell.lib.adapter.tc.TcInterface as superClass

###############################################################################
# Telecommand injection interface.
###############################################################################
class TcInterface(superClass):

    #==========================================================================
    def __init__(self):
        """
        Perform the initialization of data here, after the 
        superclass initialization
        """
        superClass.__init__(self)
        # TODO Your initialization stuff here

    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        """
        Start the internal driver TC mechanisms

        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Your startup stuff here

    #==========================================================================
    def cleanup(self):
        """
        Perform the cleanup of the internal driver TC resources.
        """
        superClass.cleanup(self)
        # TODO Release resources and cleanup stuff here

    #==========================================================================
    def _createTcItem(self, mnenonic, description = "" ):
        """
        Instantiate a telecommand (usually taking the information from GCS)

        The item shall be derived from TcItemClass class.

        If not overriden, the adapter class provides a generic TC item structure.
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the item creation.
        # return theItem

    #==========================================================================
    def _sendCommand(self, tcItem, config = {} ):
        """
        Inject a TC item into the GCS.

        tcItem : TcItemClass instance with the command information.
        config : configuration dictionary
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the command injection. Return True/False

    #==========================================================================
    def _sendList(self, tcItemList, config = {} ):
        """
        Inject a group of TC items into the GCS.

        tcItemList : list of TcItemClass instance with the command information.
        config     : configuration dictionary
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the command list injection. Return True/False

    #==========================================================================
    def _sendBlock(self, tcItemList, config = {} ):
        """
        Inject a block of TC items into the GCS. Blocked items are injected
        and transmitted as a single TC frame.

        tcItemList : list of TcItemClass instance with the command information.
        config     : configuration dictionary
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the block injection. Return True/False

################################################################################
# Interface handle
TC = TcInterface()
