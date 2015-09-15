###################################################################################
## MODULE     : spell.lang.helpers.usrhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for user management
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
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.constants.notification import *
from spell.lib.registry import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import WrapperHelper

#*******************************************************************************
# System Imports
#*******************************************************************************

################################################################################
class UserLogin_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the UserLogin wrapper.
    """    
    
    # User name
    __usr = None
    # Password
    __pwd = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "USR")
        self.__usr = ""
        self.__pwd = ""
        self._opName = "User login" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
            Login into the system
        
        ARGUMENTS:
            Expected arguments are:
                - User name
                - Password
                
        RETURNS:
            True if success
            
        RAISES:
            DriverException if there is a problem in the driver layer
            SyntaxException if the user name/password are not given.
        """
        # Get the user name
        self.__usr = args[0]
        # Get the password
        self.__pwd = args[1]
        
        # Set in progress
        self._notifyValue( self.__usr, "", NOTIF_STATUS_PR, "Loggin in" )

        #TODO: merge with defaults

        result = REGISTRY['USER'].login( self.__usr, self.__pwd, self.getConfig())

        if not result:        
            self._notifyValue( self.__usr, "", NOTIF_STATUS_FL, "Login failed" )
        else:
            self._notifyValue( self.__usr, "LOGGED IN", NOTIF_STATUS_OK, "Logged in" )
        
        return [False, True,None,None]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( self.__usr, "", NOTIF_STATUS_FL, "Repeating")
        Display("Retry login" + self.__usr, level = WARNING )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( self.__usr, "", NOTIF_STATUS_SP, "Skipped")
        Display("Skip user login" + self.__usr, level = WARNING )
        return [False, None]

################################################################################
class UserLogout_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the UserLogout wrapper.
    """    
    
    # Name of the process to be started
    __usr = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "USR")
        self.__usr = ""
        self._opName = "User logout" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
            Logout the user from the system
        
        ARGUMENTS:
            Expected arguments are:
                - User name
                
        RETURNS:
            True if success
            
        RAISES:
            DriverException if there is a problem in the driver layer
            SyntaxException if the process name is not given.
        """
        # Get the user name
        self.__usr = args[1]
        
        # Set in progress
        self._notifyValue( self.__usr, "", NOTIF_STATUS_PR, "Loggin out" )

        result = REGISTRY['USER'].logout( self.__usr, self.getConfig())

        if not result:        
            self._notifyValue( self.__usr, "", NOTIF_STATUS_FL, "Failed" )
        else:
            self._notifyValue( self.__usr, "LOGGED OUT", NOTIF_STATUS_OK, "Logged out" )
        
        return [False, True, None, None]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( self.__usr, "", NOTIF_STATUS_FL, "Repeating")
        Display("Retry logout" + self.__usr, level = WARNING )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( self.__usr, "", NOTIF_STATUS_SP, "Skipped")
        Display("Skip logout" + self.__usr, level = WARNING )
        return [False, None]

