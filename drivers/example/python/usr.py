###################################################################################
## MODULE     : usr
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: User management interface of the driver connection layer
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

__all__ = ['USER']

###############################################################################
# Superclass
import spell.lib.adapter.usr.UserInterface as superClass

###############################################################################
# User management interface.
###############################################################################
class UserInterface(superClass):

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
        Start the internal driver user mechanisms

        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Your startup stuff here

    #==========================================================================
    def cleanup(self):
        """
        Perform the cleanup of the internal driver user resources.
        """
        superClass.cleanup(self)
        # TODO Release resources and cleanup stuff here

    #==========================================================================
    def _login(self, username, password, config = {} ):
        """
        Login the given user in the GCS system.

        username: user name
        password: the user password
        config: configuration dictionary with modifiers.

        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the user login
        # return True/False

    #==========================================================================
    def _logout(self, username, config = {}):
        """
        Logout the given user from the GCS system.

        username: user name
        config: configuration dictionary with modifiers.

        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the user logout
        # return True/False

    #==========================================================================
    def _isLoggedIn(self, username, config = {}):
        """
        Check if the given user is logged in the GCS system.

        username: user name
        config: configuration dictionary with modifiers.

        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the user login check
        # return True/False

###############################################################################
# Interface instance
USER = UserInterface()
