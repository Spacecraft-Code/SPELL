###################################################################################
## MODULE     : tm
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telemetry interface of the driver connection layer
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
import sys,os

###############################################################################
# Module import definition

__all__ = ['TM']

###############################################################################
# Superclass
import spell.lib.adapter.tm.TmInterface as superClass

###############################################################################
###############################################################################
class TmInterface( superClass ):

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
        Start the internal driver TM mechanisms

        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Your startup stuff here

    #==========================================================================
    def cleanup(self):
        """
        Perform the cleanup of the internal driver TM resources.
        """
        superClass.cleanup(self)
        # TODO Release resources and cleanup stuff here

    #===========================================================================
    def _createTmItem(self, mnemonic, description = ""):
        """
        Instantiate a telemetry parameter (usually taking the information from GCS)

        The item shall be derived from TmItemClass class.

        If not overriden, the adapter class provides a generic TM item structure.
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the item creation.
        # return theItem

    #==========================================================================
    def _injectItem(self, tmItem, value, config = {} ):
        """
        Inject a ground value for a TM parameter.

        tmItem : instance of TmItemClass with the parameter information
        value  : contains the required value
        config : configuration dictionary with modifiers.
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the item injection.
        # return True/False

    #==========================================================================
    def _refreshItem(self, tmItem, config = {} ):
        """
        Acquire the value for a TM parameter.

        tmItem : instance of TmItemClass with the parameter information
        config : configuration dictionary with modifiers.

        Relevant modifiers are

        - Wait=True/False: when True, the system shall block the caller until
        a new sample of the required parameter arrives to the GCS. If False,
        the last recorded value of the parameter shall be returned immediately.

        - ValueFormat=RAW/ENG: if RAW is given, the raw or uncalibrated value
        of the parameter shall be given. If ENG is used, the engineering or
        calibrated value shall be returned.

        - Timeout=(TIME): maximum amount of time to wait for new samples, 
        before the parameter is declared as impossible to acquire. It only
        makes sense when Wait=True.

        Returned value: a list with the value and True/False indicating the validity
        of the parameter 
        """
        raise DriverException("Service not available in this driver")
        # return [ theValue, theValidity ]

    #===========================================================================
    def _setLimit(self, param, limit, value, config ):
        """
        Modify an Out Of Limits definition for a given telemetry parameter

        param: the telemetry parameter name
        limit: the limit to be modified
        value: the limit value
        config: configuration dictionary with modifiers

        Relevant modifiers are:

        - Select=ACTIVE/ALL/<STR>: indicates which definitions should be affected 
                             (all, only the active ones, or the one indicated by
                             the string)

        The limit to be modified may be one of the following:

        LoRed,LoYel,HiRed,HiYel: indicate the hard and soft limit values

        Nominal,Earning,Error,Ignore: the values are lists containing the values
                            assigned to each category, e.g. Nominal:['ValueA','ValueB']

        Delta: set a step or spike limit
        """
        raise DriverException("Service not available in this driver")
        # return True/False

    #===========================================================================
    def _getLimit(self, param, limit, config ):
        """
        Obtain an Out Of Limits definition for a given telemetry parameter

        param: the telemetry parameter name
        limit: the limit to be modified
        config: configuration dictionary with modifiers

        Relevant modifiers are:

        - Select=ACTIVE/ALL/<STR>: indicates which definitions should be considered 
                             (all, only the active ones, or the one indicated by
                             the string)

        The limit to be obtained may be one of the following:

        LoRed,LoYel,HiRed,HiYel: indicate the hard and soft limit values

        Nominal,Earning,Error,Ignore: the values are lists containing the values
                            assigned to each category, e.g. Nominal:['ValueA','ValueB']

        Delta: step or spike limit
        """
        raise DriverException("Service not available in this driver")
        # return the limit value or raise exception

    #===========================================================================
    def _getLimits(self, param, config ):
        """
        Obtain an Out Of Limits definition for a given telemetry parameter

        param: the telemetry parameter name
        config: configuration dictionary with modifiers

        Relevant modifiers are:

        - Select=ACTIVE/ALL/<STR>: indicates which definitions should be considered 
                             (all, only the active ones, or the one indicated by
                             the string)

        The limit definition to be returned may be one of the following:

        {LoRed,LoYel,HiRed,HiYel}: indicate the hard and soft limit values

        {Nominal,Earning,Error,Ignore}: the values are lists containing the values
                            assigned to each category, e.g. Nominal:['ValueA','ValueB']

        {Delta}: step or spike limit
        """
        raise DriverException("Service not available in this driver")
        # return the limit values or raise exception

    #===========================================================================
    def _setLimits(self, param, limits, config ):
        """
        Modify an Out Of Limits definition for a given telemetry parameter

        param: the telemetry parameter name
        limits: dictionary containing the limit parameters
        config: configuration dictionary with modifiers

        Relevant modifiers are:

        - Select=ACTIVE/ALL/<STR>: indicates which definitions should be affected 
                             (all, only the active ones, or the one indicated by
                             the string)

        Limit parameters dictionary may contain the following:

        LoRed,LoYel,HiRed,HiYel: indicate the hard and soft limit values

        Midpoint,Tolerance: indicate a middle limit point and a tolerance that is
                            used to calculate the upper and lower limits (M+T, M-T)
                            Hard and soft limits are considered equal.

        Nominal,Earning,Error,Ignore: the values are lists containing the values
                            assigned to each category, e.g. Nominal:['ValueA','ValueB']

        Delta: set a step or spike limit
        """
        raise DriverException("Service not available in this driver")
        # return True/False

    #===========================================================================
    def _loadLimits(self, param, limits, config ):
        """
        Load a set of limits definitions for a given parameter, from a given file.

        param: telemetry parameter name
        limits: file with limit definitions
        config: configuration parameters

        TBD
        """
        raise DriverException("Service not available in this driver")
        # return True/False

################################################################################
# Interface handle
TM = TmInterface()
