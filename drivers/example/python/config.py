###################################################################################
## MODULE     : config
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION:
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
# SPELL Imports
#*******************************************************************************
from spell.lib.exception import DriverException

#*******************************************************************************
# Local Imports
#*******************************************************************************

#*******************************************************************************
# System Imports
#*******************************************************************************
import os,sys

###############################################################################
# Module import definition

__all__ = ['CONFIG']

INTERFACE_DEFAULTS = {}

###############################################################################
# Superclass
import spell.lib.adapter.config.ConfigInterface as superClass

###############################################################################
# The configuration interface is typically in charge of managing resources and
# mechanisms that have nothing to do with specific services like TM, TC and EV.
# This interface normally prepares the ground so that the service interfaces
# like TM can initialize properly. The configuration interface is the first
# on being initialized.
###############################################################################
class ConfigInterface(superClass):

    #==========================================================================
    def __init__(self):
        """
        Perform the initialization of your data here, after the superClass
        initialization.
        """
        superClass.__init__(self)
        # TODO Your initialization stuff here

    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        """
        Start the internal driver mechanisms (typically, start a centralized
        internal service that manages all driver capabilities)

        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Initialize here the driver internal mechanisms
        # Throw DriverException in case of fatal errors

    #==========================================================================
    def cleanup(self, shutdown = False):
        """
        Perform the cleanup of the internal driver resources.

        shutdown: when true, the SPELL executor is shutting down so every resource
        should be released. When false, it means that the driver is being unloaded
        but the executor keeps running. That is, it is possible that the driver
        will be loaded again later on (e.g. on a RELOAD command). In this case
        it may be interesting to keep some resources ready when 'shutdown' is false.
        """
        superClass.cleanup(self, shutdown)
        # TODO Release resources and cleanup stuff here

###############################################################################
# Interface instance
CONFIG = ConfigInterface()
