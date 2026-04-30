###################################################################################
## MODULE     : spell.lib.adapter.interface
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Adapter interface base
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
from spell.lib.adapter.constants.core import KEY_SEPARATOR

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['Interface']

###############################################################################
class Interface(object):

    """
    Base class of all driver interfaces
    """

    __ifcName = None
    __ctxConfig = None
    __drvConfig = None

    #===========================================================================
    def __init__(self, name):
        self.__ifcName = name
        self.__ctxConfig = None
        self.__drvConfig = None

    #==========================================================================
    def storeConfig(self, ctxConfig, drvConfig):
        self.__ctxConfig = ctxConfig
        self.__drvConfig = drvConfig

    #===========================================================================
    def getInterfaceName(self):
        return self.__ifcName

    #===========================================================================
    def refreshConfig(self):
        pass

    #===========================================================================
    def getContextConfig(self):
        return self.__ctxConfig

    #===========================================================================
    def getDriverConfig(self):
        return self.__drvConfig

    #===========================================================================
    def _createAction(self, key, text):
        return key + KEY_SEPARATOR + text

    #===========================================================================
    def driverUpdateActionList(self, theOptions, exception = None):
        return theOptions

    #===========================================================================
    def driverPerformAction(self, code):
        return [False,False]

    #===========================================================================
    def driverBeforeAction(self, code):
        pass

    #===========================================================================
    def driverAfterAction(self, code):
        pass

    #==========================================================================
    def onCommand(self, commandId):
        if commandId == "CMD_ABORT":
            self._onDriverAbort()
        elif commandId == "CMD_INTERRUPT":
            self._onDriverInterrupt()
        self._onInterfaceCommand(commandId)
        return

    #==========================================================================
    def _onInterfaceCommand(self, commandId):
        return

    #==========================================================================
    def _onDriverAbort(self):
        return

    #==========================================================================
    def _onDriverInterrupt(self):
        return
