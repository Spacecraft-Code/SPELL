###################################################################################
## MODULE     : task
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Task management interface of the driver connection layer
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

__all__ = ['TASK']

###############################################################################
# Superclass
import spell.lib.adapter.task.TaskInterface as superClass

###############################################################################
# Task management library interface. This class is in charge of
# managing the underlying system applications.
###############################################################################
class TaskInterface(superClass):

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
        Start the internal driver TASK mechanisms

        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Your startup stuff here

    #==========================================================================
    def cleanup(self):
        """
        Perform the cleanup of the internal driver TASK resources.
        """
        superClass.cleanup(self)
        # TODO Release resources and cleanup stuff here

    #==========================================================================
    def _startTask(self, name, config = {}  ):
        """
        Invoked by the system in order to start a GCS task.

        name: string containing the task name
        config: configuration dictionary with modifiers. 
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the task start. Return True/False.

    #==========================================================================
    def _stopTask(self, name, config = {} ):
        """
        Invoked by the system in order to stop a GCS task.

        name: string containing the task name
        config: configuration dictionary with modifiers. 
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the task stop. Return True/False.

    #==========================================================================
    def _checkTask(self, name, config = {} ):
        """
        Invoked by the system in order to check the status of a GCS task.

        name: string containing the task name
        config: configuration dictionary with modifiers. 
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the task start. Return the status code.

    #==========================================================================
    def _openDisplay(self, displayName, config = {} ):
        """
        Invoked by the system in order to open a telemetry display on the GCS

        displayName: name or identifier of the display
        config: configuration dictionary with modifiers.

        Relevant modifiers are:

        Host: the machine name where the display should be open.
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the operation. Return True on success.

    #==========================================================================
    def _printDisplay(self, displayName, config = {} ):
        """
        Invoked by the system in order to print a telemetry display from the GCS

        displayName: name or identifier of the display
        config: configuration dictionary with modifiers.

        Relevant modifiers are:

        Printer: the printer name
        Format: PS (postcript) or VECTOR (ASCII text, for alphanumeric displays only)
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the operation. Return True on success.

    #==========================================================================
    def _closeDisplay(self, displayName, config = {} ):
        """
        Invoked by the system in order to close a telemetry display on the GCS

        displayName: name or identifier of the display
        config: configuration dictionary with modifiers.

        Relevant modifiers are:

        Host: the machine name where the display should be closed.
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the operation. Return True on success.

################################################################################
# Interface handle
TASK = TaskInterface()
