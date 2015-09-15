###################################################################################
## MODULE     : spell.lang.helpers.tskhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for task management
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
from spell.lib.adapter.utctime import TIME
from spell.lib.registry import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import *

#*******************************************************************************
# System Imports
#*******************************************************************************


################################################################################
class StartTask_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the StartTask wrapper.
    """    
    
    # Name of the process to be started
    _process = None
    _args = ""
    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TASK")
        self._process = None
        self._args = ""
        self._opName = "Start task" 

    #===========================================================================
    def _doStartTask(self):
        # Set in progress
        self._notifyValue( self._process, "", NOTIF_STATUS_PR, "Starting" )

        #TODO: merge with defaults

        result = REGISTRY['TASK'].startTask( self._process, self._args, self.getConfig())

        if not result:        
            self._notifyValue( self._process, "", NOTIF_STATUS_FL, "Unable to start" )
        else:
            self._notifyValue( self._process, "", NOTIF_STATUS_OK, "Started" )
            
        return result
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
            Start the given task with the given config
        
        ARGUMENTS:
            Expected arguments are:
                - Process name or command
                - Argument list
                
        RETURNS:
            True if success
            
        RAISES:
            DriverException if there is a problem in the driver layer
            SyntaxException if the process name is not given.
        """
        if len(args)==0:
            raise SyntaxException("No arguments given")
        
        # Get the process name
        self._process = args[0]
        if type(self._process)!=str:
            raise SyntaxException("Expected a string")
        
        if len(args)==2:
            # Get the process arguments
            self._args = args[1]
        else:
            self._args = ""
        
        result = self._doStartTask()
            
        return [False, result,None,None]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( self._process, "", NOTIF_STATUS_FL, "Retrying")
        self._write("Retry start " + self._process, {Severity:WARNING})
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( self.__process, "", NOTIF_STATUS_SP, "Skipped")
        self._write("Skip start " + self._process, {Severity:WARNING})
        return [False, None]
    
################################################################################
class StopTask_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the StopTask wrapper.
    """    
    
    # Name of the process to be started
    _process = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TASK")
        self._process = None
        self._opName = "Stop task" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
            Start the given task with the given config
        
        ARGUMENTS:
            Expected arguments are:
                - Process name or command
                
        RETURNS:
            True if success
            
        RAISES:
            DriverException if there is a problem in the driver layer
            SyntaxException if the process name is not given.
        """
        
        if len(args)==0:
            raise SyntaxException("No arguments given")
        
        # Get the process name
        self._process = args[0]
        if type(self._process)!=str:
            raise SyntaxException("Expected a display name string")
        
        # Set in progress
        self._notifyValue( self._process, "", NOTIF_STATUS_PR, "Stopping" )

        result = REGISTRY['TASK'].stopTask( self._process, self.getConfig())

        if not result:        
            self._notifyValue( self._process, "", NOTIF_STATUS_FL, "Cannot stop" )
        else:
            self._notifyValue( self._process, "", NOTIF_STATUS_OK, "Stopped" )
        
        return [False, True,None,None]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( self._process, "", NOTIF_STATUS_FL, "Repeating")
        self._write("Retry stop " + self._process, {Severity:WARNING})
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( self._process, "", NOTIF_STATUS_SP, "Skipped")
        self._write("Skip stop " + self._process, {Severity:WARNING})
        return [False, None]

################################################################################
class OpenDisplay_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the OpenDisplay wrapper.
    """    
    __display = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TASK")
        self.__display = None
        self._opName = "Open display" 
    
    #===========================================================================
    def _doOpenDisplay(self):
        # Set in progress
        self._notifyValue( self.__display, "", NOTIF_STATUS_PR, "Opening" )

        #TODO: merge with defaults

        result = REGISTRY['TASK'].openDisplay( self.__display, self.getConfig())

        if not result:        
            self._notifyValue( self.__display, "", NOTIF_STATUS_FL, "Unable to open" )
        else:
            self._notifyValue( self.__display, "", NOTIF_STATUS_OK, "Open" )
            
        return result
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No arguments given")
        elif len(args)==1:
            self.__display = args[0]
        else:
            raise SyntaxException("Bad number of arguments")

        if type(self.__display)!=str:
            raise SyntaxException("Expected a display name string")

        #TODO: more processing, this is demo only !!!
        
        result = self._doOpenDisplay()
        
        return [False,result,NOTIF_STATUS_OK,""]

################################################################################
class CloseDisplay_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the CloseDisplay wrapper.
    """    
    __display = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TASK")
        self.__display = None
        self._opName = "Close display" 
    
    #===========================================================================
    def _doCloseDisplay(self):
        # Set in progress
        self._notifyValue( self.__display, "", NOTIF_STATUS_PR, "Closing" )

        #TODO: merge with defaults

        result = REGISTRY['TASK'].closeDisplay( self.__display, self.getConfig())

        if not result:
            self._notifyValue( self.__display, "", NOTIF_STATUS_FL, "Unable to start the task to close the display" )
            raise DriverException("Could not start the task to close the display")
        else:
            self._notifyValue( self.__display, "", NOTIF_STATUS_OK, "Close" )
            
        return result
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No arguments given")
        elif len(args)==1:
            self.__display = args[0]
        else:
            raise SyntaxException("Bad number of arguments")

        if type(self.__display)!=str:
            raise SyntaxException("Expected a display name string")

        #TODO: more processing, this is demo only !!!
        
        result = self._doCloseDisplay()
        
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry CloseDisplay", {Severity:WARNING} )
        return [True, True]

    #===========================================================================
    def _doSkip(self):
        self._write("CloseDisplay SKIPPED", {Severity:WARNING} )
        return [False, True]

################################################################################
class StartProc_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the StartProc wrapper.
    """    
    __procId = None
    __arguments = {}
    __result = False
    __status = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TASK")
        self.__procId = None
        self.__arguments = {}
        self.__result = False
        self.__status = None
        self._opName = ""

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)!=1:
            raise SyntaxException("Bad arguments, should provide procedure identifier")

        self.__procId = args[0]

        if type(self.__procId)!=str:
            raise SyntaxException("Expected a procedure identifier")

        # Parse arguments for the procedure
        self.__arguments = {}
        if 'args' in kargs:
            defs = kargs['args']
            if type(defs) != list:
                raise SyntaxException("Expected a list of arguments")
            for argument in defs:
                if type(argument)!=list or len(argument)!=2:
                    raise SyntaxException("Wrong argument format, expected a list of 2 elements")
                argName = argument[0]
                argValue = argument[1]
                self.__arguments[argName] = argValue 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        
        self._setActionString( ACTION_SKIP   ,  "Skip starting procedure " + repr(self.__procId) + " and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip starting procedure " + repr(self.__procId) + " and return failure  (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to start the procedure again")

        automatic = self.getConfig(Automatic)
        blocking = self.getConfig(Blocking)
        visible = self.getConfig(Visible)
        
        msg = "Starting procedure " + self.__procId + " (Automatic:"\
              + str(automatic) + ", Blocking:" + str(blocking) \
              + ", Visible: " + str(visible) + ")"
        self._write(msg)
       
        LOG("Starting subprocedure") 
        self._notifyValue("Procedure", self.__procId, NOTIF_STATUS_PR, "Loading procedure")
        
        # Open the procedure (will raise exception on failure)    
        self.__result = REGISTRY['EXEC'].openSubProcedure(self.__procId,
                                          self.__arguments, 
                                          config = self.getConfig() )

        self.__status = REGISTRY['EXEC'].getChildStatus()

        LOG("Subprocedure launch result is " + repr([self.__result,self.__status]))

        if REGISTRY['EXEC'].isChildError():
            error,reason = REGISTRY['EXEC'].getChildError()
            LOG("Initial child status is error: " + repr([error,reason]), LOG_ERROR)
            raise DriverException(error,reason)
        
        loadedStatus = NOTIF_STATUS_OK
        if blocking: 
            loadedStatus = NOTIF_STATUS_PR
        
        self._notifyValue("Procedure", self.__procId, loadedStatus, "Procedure loaded")

        # If the child procedure is started in blocking mode we shall monitor
        # it until it reaches the status ERROR/ABORTED/FINISHED. Otherwise,
        # we finish right away.
        if blocking == True:
            # Start the time condition wait
            REGISTRY['EXEC'].startWait( (), {Procedure:self.__procId} )
            # Wait the procedure to finish
            REGISTRY['EXEC'].wait()

        if self.__result == True:
            if blocking:
                self._notifyValue("Procedure", self.__procId, NOTIF_STATUS_OK, "Execution finished")
            else:
                self._notifyValue("Procedure", self.__procId, NOTIF_STATUS_OK, "Procedure started")
        else:
            self._notifyValue("Procedure", self.__procId, NOTIF_STATUS_FL, "Execution failed")
            error,reason = REGISTRY['EXEC'].getChildError()
            LOG("Subprocedure failed: " + repr([error,reason]), LOG_ERROR)
            raise DriverException(error,reason)
                  
        return [False,self.__result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( "Procedure", self.__procId, NOTIF_STATUS_PR, "Reloading")
        REGISTRY['EXEC'].killSubProcedure()
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "Procedure", self.__procId, NOTIF_STATUS_SP, "Skipped")
        REGISTRY['EXEC'].killSubProcedure()
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._notifyValue( "Procedure", self.__procId, NOTIF_STATUS_SP, "Cancelled")
        REGISTRY['EXEC'].killSubProcedure()
        return [False, False]
        
################################################################################
class PrintDisplay_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the PrintDisplay wrapper.
    """    
    __display = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__display = None
        self._opName = "Print display" 
    
    #===========================================================================
    def _doPrintDisplay(self):
        # Set in progress
        self._notifyValue( self.__display, "", NOTIF_STATUS_PR, "Printing" )

        #TODO: merge with defaults

        result = REGISTRY['TASK'].printDisplay( self.__display, self.getConfig())

        if not result:        
            self._notifyValue( self.__display, "", NOTIF_STATUS_FL, "Unable to print" )
        else:
            self._notifyValue( self.__display, "", NOTIF_STATUS_OK, "Printed" )
            
        return result
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No arguments given")
        elif len(args)==1:
            self.__display = args[0]
        else:
            raise SyntaxException("Bad number of arguments")

        if type(self.__display)!=str:
            raise SyntaxException("Expected a display name string")

        #TODO: more processing, this is demo only !!!
        
        result = self._doPrintDisplay()
        
        return [False,result,NOTIF_STATUS_OK,""]
