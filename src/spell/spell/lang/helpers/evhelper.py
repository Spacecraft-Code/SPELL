###################################################################################
## MODULE     : spell.lang.helpers.evhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for event functions
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
from spell.lang.functions import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import *

#*******************************************************************************
# System Imports
#*******************************************************************************


###############################################################################
# Module import definition

__all__ = ['Event_Helper']

################################################################################
class Event_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Event wrapper.
    """
    __msg = ""    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "EV")
        self.__msg = ""
        self._opName = "Event Injection" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        # Parse arguments
        if len(args)==0:
            raise SyntaxException("No message given")
        self.__msg = args[0]
        if type(self.__msg)!=str:
            raise SyntaxException("Expected a message string")
        if len(args)>=2:
            self.addConfig(Severity, args[1])
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        
        self._setActionString( ACTION_SKIP   ,  "Skip injecting event and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip injecting event and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to inject the event again")

        # Store information for possible failures
        self.setFailureInfo("EV", self.__msg)

        self._write( self.__msg, self.getConfig() )
        import sys
        sys.stderr.write("SEVERITY " + repr(self.getConfig(Severity)))
        REGISTRY['EV'].raiseEvent(self.__msg, self.getConfig())            
        return [False,True,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doSkip(self):
        self._write("Inject event skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Inject event skipped", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry inject event", {Severity:WARNING} )
        return [True,False]
