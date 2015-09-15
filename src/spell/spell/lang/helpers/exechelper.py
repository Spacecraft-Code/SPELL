###################################################################################
## MODULE     : spell.lang.helpers.exechelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for execution functions
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
from spell.lib.adapter.utctime import *
from spell.lib.registry import *
from spell.lib.adapter.interface import Interface

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import *

#*******************************************************************************
# System Imports
#*******************************************************************************
import time,sys,threading
import inspect



################################################################################
class Pause_Helper(WrapperHelper):
    
    __condition = None
    __timeout = None
    __configDict = {}
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__condition = None
        self.__timeout = None
        self.__configDict = {}
        self._opName = "Pause" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)==0:
            return
        
        useConfig = {}
        useConfig.update(self.getConfig())

        # Since args is a tuple we have to convert it to a list for TM.verify        
        if len(args)!=1:
            # The case of giving a simple step for verification
            self.__condition = [ item for item in args ]
        else:
            # Givin a step or a step list
            self.__condition = args[0]

        self.__timeout = self.getConfig(Timeout)
        if self.__timeout is None:
            raise SyntaxException("Timeout is required")
            
        self.__configDict = {}
        self.__configDict.update(self.getConfig())
        self.__configDict[Wait] = False
        self.__configDict[HandleError] = False
        self.__configDict[AllowInterrupt] = True
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        if len(args)==0:
            if REGISTRY.exists('EXEC'):
                REGISTRY['EXEC'].pause()
            return [False,True,NOTIF_STATUS_OK,"Execution paused"]
        
        startTime = NOW.abs() #time.time()

        from spell.lang.functions import Verify
        while(True):
            result = Verify( self.__condition, self.__configDict )
            if result and REGISTRY.exists('EXEC'):
                REGISTRY['EXEC'].pause() 
                return [False,True,NOTIF_STATUS_OK,"Execution paused on condition"]

            time.sleep(0.5)

            currTime = NOW.abs() #time.time()
            if (currTime-startTime)>self.__timeout:
                self._write("Not pausing, condition timed-out", {Severity:WARNING})
                return [False,False,NOTIF_STATUS_FL,"Condition timed out"]

################################################################################
class SetExecDelay_Helper(WrapperHelper):

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self._opName = "Exec Delay" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs):
        
        if not REGISTRY.exists('EXEC'): raise DriverException("Cannot execute") 
        delay = None
        if len(args)==0:
            if not kargs.has_key('delay'): 
                raise SyntaxException("No arguments given")
            if kargs.has_key('delay'):
                delay = kargs.get('delay')
        else:
            delay = args[0]
        
        if delay is not None:
            self._write("Setting execution delay to " + str(delay), {Severity:WARNING})
            REGISTRY['EXEC'].setExecutionDelay(delay)
            
        return [False,None,NOTIF_STATUS_OK,"Delay set to " + str(delay)]
        
################################################################################
class Script_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Script wrapper.
    """    
    
    __code = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__code = None
        self._opName = "Script" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        
        if len(args)==0:
            raise SyntaxError("No arguments given")
        
        if type(args[0])!=str:
            raise SyntaxError("Expected a source code string")
        
        self.__code = args[0]
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        result = REGISTRY['EXEC'].script(self.__code)
        return [False,result,NOTIF_STATUS_OK,"Executed"]

################################################################################
class SetUserAction_Helper(WrapperHelper):
    
    __function = None
    __label = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "")
        self.__function = None
        self.__label = None
        self._opName = "User action" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)<2:
            raise SyntaxException("Expected function and label")

        self.__function = args[0]
        if not inspect.isfunction(self.__function):
            raise SyntaxException("Expected function as first argument")

        self.__label = args[1]
        if type(self.__label) != str:
            raise SyntaxException("Expected label string as second argument")
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip setting the user action button and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip setting the user action button and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to set the user action button again")

        if REGISTRY.exists('EXEC'):
            REGISTRY['EXEC'].setUserAction(self.__function,self.__label,self.getConfig())
            
        return [False,True,NOTIF_STATUS_OK,""]
        
    #===========================================================================
    def _doSkip(self):
        self._write("Set user action skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Set user action skipped", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry set user action", {Severity:WARNING} )
        return [True,False]

################################################################################
class EnableUserAction_Helper(WrapperHelper):
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "")
        self._opName = "Enable user action" 

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip enabling the user action button and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip enabling the user action button and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to enable the user action button again")

        if REGISTRY.exists('EXEC'):
            REGISTRY['EXEC'].enableUserAction()
            
        return [False,True,NOTIF_STATUS_OK,"Action enabled"]
        
    #===========================================================================
    def _doSkip(self):
        self._write("Enable user action skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Enable user action skipped", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry enable user action", {Severity:WARNING} )
        return [True,False]

################################################################################
class DisableUserAction_Helper(WrapperHelper):
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "")
        self._opName = "Disable user action" 

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip disabling the user action button and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip disabling the user action button and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to disable the user action button again")

        if REGISTRY.exists('EXEC'):
            REGISTRY['EXEC'].disableUserAction()
            
        return [False,True,NOTIF_STATUS_OK,"Action disabled"]
        
    #===========================================================================
    def _doSkip(self):
        self._write("Disable user action skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Disable user action skipped", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry disable user action", {Severity:WARNING} )
        return [True,False]

################################################################################
class DismissUserAction_Helper(WrapperHelper):
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "")
        self._opName = "Dismiss user action" 

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        
        self._setActionString( ACTION_SKIP   ,  "Skip removing the user action button and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip removing the user action button and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to remove the user action button again")

        if REGISTRY.exists('EXEC'):
            REGISTRY['EXEC'].dismissUserAction()
            
        return [False,True,NOTIF_STATUS_OK,"Action dismissed"]
        
    #===========================================================================
    def _doSkip(self):
        self._write("Dismiss user action skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Dismiss user action skipped", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry dismiss user action", {Severity:WARNING} )
        return [True,False]

