###################################################################################
## MODULE     : spell.lib.empty.usr
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: User interface
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

###############################################################################
# Module import definition

__all__ = ['USER']

###############################################################################
# Superclass
import spell.lib.adapter.usr
superClass = spell.lib.adapter.usr.UserInterface

###############################################################################
class UserInterface(superClass):

    """
    DESCRIPTION:
        User management interface. 
    """

    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")

    #==========================================================================
    def setup(self, ctxConfig,drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup empty USER interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup empty USER interface")

    #==========================================================================
    def _login(self, username, password, config = {} ):
        raise DriverException("Cannot perform operation", "Not supported by driver")

    #==========================================================================
    def _logout(self, username, config = {}):
        raise DriverException("Cannot perform operation", "Not supported by driver")

    #==========================================================================
    def _isLoggedIn(self, username, config = {}):
        raise DriverException("Cannot perform operation", "Not supported by driver")

###############################################################################
# Interface instance
USER = UserInterface()
