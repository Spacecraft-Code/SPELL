###################################################################################
## MODULE     : spell.lang.helpers.basehelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Base class for language helpers
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
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.result import Result
from spell.lib.adapter.config import Configurable
from spell.lib.registry import *
from spell.lib.adapter.constants.notification import *
from spell.lib.registry import REGISTRY
from spell.config.reader import *

#*******************************************************************************
# Local Imports
#*******************************************************************************

#*******************************************************************************
# System Imports
#*******************************************************************************
import sys

###############################################################################
# Module import definition

__all__ = ['WrapperHelper']

################################################################################
class WrapperHelper(Configurable):

    """
    DESCRIPTION: 
        Provides support for carrying out operations in a closed-loop way, 
        handling failures and repeating/aborting the operation when required. 
        Operation and on-failure behaviors must be defined by children.
    """

    # Action texts
    _ACTIONS = {  ACTION_ABORT      :'Abort this procedure'  ,
                  ACTION_REPEAT     :'Repeat operation' ,
                  ACTION_RESEND     :'Resend command(s)' ,
                  ACTION_RECHECK    :'Recheck telemetry',
                  ACTION_SKIP       :'Skip operation and return success (True)'   ,
                  ACTION_NOACTION   :'No action' ,
                  ACTION_HANDLE     :'Let the procedure handle this error' ,
                  ACTION_CANCEL     :'Cancel operation and return failure (False)' }

    _CT_ACTIONS = {  ABORT    :ACTION_ABORT   ,
                     REPEAT   :ACTION_REPEAT  ,
                     RESEND   :ACTION_RESEND  ,
                     RECHECK  :ACTION_RECHECK ,
                     SKIP     :ACTION_SKIP    ,
                     NOACTION :ACTION_NOACTION,
                     HANDLE   :ACTION_HANDLE,
                     CANCEL   :ACTION_CANCEL    }

    # Action selected by user in case of failure
    _selectedAction = None
    # True if should handle exceptions, False if should let them go up
    _handleError = True
    # True if the user shall be prompted in case of failure
    _promptUser = True
    # True if should return user choice
    _giveChoice = False
    # Notification flag. If true, notifications to clients are enabled.
    _doNotify = True
    # Aborted operation flag
    _aborted = False 
    # Operation name
    _opName = "Operation" 
    # Related interface name
    _interface = None
    # Function name
    _functionName = None
    # Operation last result
    _opResult = None
    # Interruptible statement flag
    _interruptible = False
    # Executor handle
    _exec = None
    # Custom exceptions
    _failureCode = 0
    _failureType = None
    _failureItem = None
    _customException = False
    
    #===========================================================================
    def __init__(self, interface = None ):
        self._interface = interface
        idx = self.__class__.__name__.index("_")
        self._functionName = self.__class__.__name__[:idx]
        self._handleError = True
        self._promptUser = True
        self._giveChoice = False
        self._doNotify = True
        self._selectedAction = None
        self._aborted = False
        self.setConfig(self._getDefaults())
        self._opName = "Operation" 
        self._opResult = None
        self._interruptible = False
        self._exec = REGISTRY['EXEC']
        self._initializeActionStrings()
        self._customException = False
        self._failureCode = 0
        self._failureType = None
        self._failureItem = None
    
    #===========================================================================
    def configure(self, *args, **kargs ):
        self.setConfig(self.buildConfig(args, kargs, self._getDefaults(), 
          {HandleError:True,GiveChoice:False,Notify:True,PromptUser:True,AllowInterrupt:False}))
        
        # Standard exception handling
        self._handleError = self.getConfig(HandleError)

        # Custom exception handling
        self._customException = False
        self._failureCode = 0
        if self.hasConfig(OnFailure):
            onFailure = self.getConfig(OnFailure)
            if (onFailure & HANDLE)>0:
                self._customException = True
                if self.hasConfig(FailureCode):
                    self._failureCode = self.getConfig(FailureCode)
                    
        self._customException = self.getConfig(HandleError)
        self._promptUser = self.getConfig(PromptUser)
        self._doNotify = self.getConfig(Notify)
        self._giveChoice = self.getConfig(GiveChoice)
        self._interruptible = self.getConfig(AllowInterrupt)
        REGISTRY['CIF'].setVerbosity(self.getConfig(Verbosity))

    #===========================================================================
    def cleanup(self, *args, **kargs ):
        REGISTRY['CIF'].resetVerbosity()

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        pass  
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):  
        """
        DESCRIPTION:
            The wrapper operation code. To be implemented by children.
            
        RETURNS:
            Tuple containing [ Repeat, Object ] where Repeat must be boolean.
            If Repeat is true, the operation is repeated. Object is used
            when returning data after the operation is required. 
            
        RAISES:
            SyntaxException     If the argument list is not ok
            DriverException     If there is an error during the operation
        """
        raise NotImplemented

    #===========================================================================
    def _doPostOperation(self, *args, **kargs ):
        pass

    #===========================================================================
    def setFailureInfo(self,ftype, fitem = None):
        self._failureType = ftype
        self._failureItem = fitem

    #===========================================================================
    def __lock(self):
        # Raise the execution block flag on executor
        self._exec.processLock()
        
    #===========================================================================
    def __unlock(self):
        # Raise the execution block flag on executor
        self._exec.processUnlock()

    #===========================================================================
    def execute(self, *args, **kargs ):
        """
        DESCRIPTION:
            Start the closed-loop operation execution.
            
        ARGUMENTS:
            args    List of arguments for the operation
            
        RETURNS:
            Operation result 
            
        RAISES:
            Nothing
        """
        
        self.__lock()
        
        try:
            self._notifyOpStatus( NOTIF_STATUS_PR, "In progress..." )
            
            REGISTRY['CIF'].setVerbosity(self.getConfig(Verbosity))
            repeat = True
            result = None
            
            self._selectedAction = None
    
            # Execute operation
            if len(args)>0 and type(args[-1])==dict:
                args = args[0:-1]
            
            # Carry out the pre-operation 
            self._doPreOperation(*args,**kargs)
            
            #LOG("Start operation loop", level = LOG_LANG )
            # Repeat operation loop
            while(repeat):
                try:
                    # Execute the operation itself
                    #LOG("Executing", level = LOG_LANG )
                    repeat, result, opStatus, opResult = self._doOperation(*args,**kargs)
                    #LOG("Operation returned " + repr(result) + ", repeat = " + repr(repeat), level = LOG_LANG )
                    # In case of driver exception, handle the error
                    if opStatus: self._notifyOpStatus( opStatus, opResult )
                except SpellException,ex:
                    LOG("Caught exception in operation: " + ex.message)
                    self._notifyOpStatus( NOTIF_STATUS_FL, OPERATION_FAILED )
                    if self._handleError:
                        repeat, result = self._handleException(ex)
                        continue
                    else:
                        raise ex
                
                # If we are here the operation did not fail, but the user
                # may want to be prompted depending on the result
                if not repeat and ( (type(result)==bool) or (isinstance(result,Result)) ):
                    if (opStatus != NOTIF_STATUS_SP) and (opStatus != NOTIF_STATUS_CL):
                        repeat, result = self._processActionOnResult(result)
    
            self._opResult = [result,self._selectedAction]
    
            # Post operations
            #LOG("Post-operation", level = LOG_LANG )
            try:
                # Preliminary operations
                self._doPostOperation(*args,**kargs)
            except SyntaxException,ex:
                LOG("Caught syntax exception in post operation")
                self._notifyOpStatus( NOTIF_STATUS_FL, ex.message )
                raise DriverException("Syntax error in procedure: " + ex.message)
    
            #LOG("Final result: " + repr(result), level = LOG_LANG )
            #LOG("Users choice: " + repr(self._selectedAction), level = LOG_LANG )
            
            if self.getConfig(GiveChoice):
                if not self._selectedAction:
                    self._selectedAction = ACTION_NOACTION
                result = [result, self._selectedAction]

        finally:
            REGISTRY['CIF'].resetVerbosity()
            self.__unlock()

        return result

    #===========================================================================
    def _processActionOnResult(self, result):
        LOG("Processing boolean result", level = LOG_LANG )
        if (result==True):
            cfg = self.getConfig(OnTrue)
        else:
            cfg = self.getConfig(OnFalse)

        # If OnTrue/OnFalse configuration is no action, just continue
        if ((cfg is None) or (cfg == NOACTION)): return [False,result]
        
        # If there is an action, will process
        
        # This flag determines if automatic action is taken, or user is prompted
        doPrompt = self.getConfig(PromptUser)

        LOG("Automatic option is " + repr(cfg) + ",doPrompt=" + repr(doPrompt), level = LOG_LANG )
        if doPrompt:
            ex = DriverException("Operation result: " + repr(result) + ", Please select action", "")
            options = self._getActionList( cfg )
            repeat, result = self._handleException(ex,options)
        elif cfg in [ABORT,SKIP,CANCEL,REPEAT,RESEND,RECHECK,NOACTION]:
            action = self._CT_ACTIONS.get(cfg)
            repeat,actionResult = self._doAction(action)
            if (actionResult != result): result = actionResult 
        else:
            repeat = False
            
        return [repeat,result]

    #===========================================================================
    def _getExceptionFlag(self, exception ):
        return self.getConfig(PromptFailure)
        
    #===========================================================================
    def _handleException(self, exception, options = None ):
        """
        DESCRIPTION:
            Prompt the user for action on operation failure. Contents of the
            prompt are the exception reason text, and the list of options 
            configured for the wrapper operation. If config is None, default
            behavior is used.
            
        ARGUMENTS:
            exception   Driver exception raisen by doOperation.
            
        RETURNS:
            Tuple containing [ Repeat, Object ] where Repeat must be boolean.
            If Repeat is true, the operation is repeated. Object is used
            when returning data after the operation is required. 
            
        RAISES:
            Nothing
        """

        LOG("EXCEPTION: " + repr(exception.message + ",reason: " + exception.reason))

        # Get the prompt message from the exception raisen by the driver
        message = exception.message 
        if (exception.reason and exception.reason.strip()!="" and exception.reason != "unknown"): 
            message += ",reason: " + exception.reason

        # Reset abort flag
        self._aborted = False 

        if options is None:
            theOptions = self._getBehaviorOptions(exception)
        else:
            theOptions = options
        
        # If there are no options defined
        if not theOptions or len(theOptions)==0:
            return [False,False]
        
        shouldPrompt = self._getExceptionFlag(exception)
        
        # Unless explicitly said, prompt the user for action
        if not (shouldPrompt == False):
            LOG("Prompting user for action")
            # Prompt the user
            action = self._prompt( message, theOptions, {Type:LIST,Notify:False})
            # In case there is no answer, abort.
            if action is None:
                self._write("Error on prompt", {Severity:ERROR})
                REGISTRY['EXEC'].abort()
                return [False,None]
            elif action == "<CANCEL>":
                REGISTRY['EXEC'].abort()
                return [False,None]
            action = action.upper()
            
        # In this case, take automatically the action specified in OnFailure
        else:
            self._write("Error in operation: " + message, {Severity:ERROR})
            cfgAction = self.getConfig(OnFailure)
            if cfgAction in [ABORT,SKIP,CANCEL,REPEAT,RESEND,RECHECK,NOACTION]:
                action = self._CT_ACTIONS.get(cfgAction)
            else:
                # If it was unable to process the action, prompt the user anyway
                self._write("Unable to process OnFailure action, prompting user", {Severity:ERROR})
                # Prompt the user
                action = self._prompt( message, theOptions, {Type:LIST,Notify:False})
                # In case there is no answer, abort.
                if action is None:
                    self._write("Error on prompt", {Severity:ERROR})
                    REGISTRY['EXEC'].abort()
                    return [False,None]
                elif action == "<CANCEL>":
                    REGISTRY['EXEC'].abort()
                    return [False,None]
                action = action.upper()

        # Perform an action depending on the prompt answer or configured action
        return self._doAction(action)
             

    #===========================================================================
    def _getBehaviorOptions(self, exception):
        # If the OnFailure parameter is not set, get the default behavior.
        # This default behavior depends on the particular primitive being
        # used, so it is implemented in child wrappers.
        if self.getConfig(OnFailure) is None:
            LOG("Using defaults")
            self.setConfig({OnFailure:ABORT})
            
        onFailure = self.getConfig(OnFailure)
        
        # Get the desired behavior as per SPELL standards
        theOptions = self._getActionList( onFailure, exception )
        
        return theOptions

    #===========================================================================
    def _driverUpdateActionList(self, theOptions, exception):
        
        # By default the functions do not let the driver choose. By overriding
        # this method each SPELL function can ask the desired driver interfaces
        # to manipulate the options.
        return theOptions

    #===========================================================================
    def _driverPerformAction(self, code):
        
        # By default the functions do not let the driver perform any action.
        # If this is overriden by a function, the function will invoke the
        # method driverPerformAction of the appropriate interface.
        # Should always return [repeat,result].
        return None #[False,False]

    #===========================================================================
    def _driverBeforeAction(self, code):
        # Let drivers carry out operations before any failure action
        pass

    #===========================================================================
    def _driverAfterAction(self, code):
        # Let drivers carry out operations after any failure action
        pass

    #===========================================================================
    def _initializeActionStrings(self ):
        self.__actionStrings = self._ACTIONS

    #===========================================================================
    def _setActionString(self, key, message ):
        self.__actionStrings[key] = message

    #===========================================================================
    def _getActionStrings(self ):
        return self.__actionStrings
            
    #===========================================================================
    def _getActionList(self, actionCodes, exception = None ):
        theOptions = []
        # Get the desired behavior
        if (actionCodes & ABORT):
            LOG("ACTION ABORT")
            theOptions.append( ACTION_ABORT + KEY_SEPARATOR + self._getActionStrings().get(ACTION_ABORT))
        if (actionCodes & REPEAT):
            LOG("ACTION REPEAT")
            theOptions.append( ACTION_REPEAT + KEY_SEPARATOR + self._getActionStrings().get(ACTION_REPEAT))
        if (actionCodes & RESEND):
            LOG("ACTION RESEND")
            theOptions.append( ACTION_RESEND + KEY_SEPARATOR + self._getActionStrings().get(ACTION_RESEND))
        if (actionCodes & RECHECK):
            LOG("ACTION RECHECK")
            theOptions.append( ACTION_RECHECK + KEY_SEPARATOR + self._getActionStrings().get(ACTION_RECHECK))
        if (actionCodes & SKIP):
            LOG("ACTION SKIP")
            theOptions.append( ACTION_SKIP + KEY_SEPARATOR + self._getActionStrings().get(ACTION_SKIP))
        if (actionCodes & CANCEL):
            LOG("ACTION CANCEL")
            theOptions.append( ACTION_CANCEL + KEY_SEPARATOR + self._getActionStrings().get(ACTION_CANCEL))
        if (actionCodes & HANDLE):
            LOG("ACTION HANDLE")
            theOptions.append( ACTION_HANDLE + KEY_SEPARATOR + self._getActionStrings().get(ACTION_HANDLE))
            
        # Now let the drivers manipulate these options or add custom ones
        theOptions = self._driverUpdateActionList( theOptions, exception )
            
        return theOptions
            
    #===========================================================================
    def _doAction(self, action):
        """
        DESCRIPTION:
            Execute the desired action implementation.
            WARNING: no exception handling is done here. If a new action is
            implemented and it is possible that this action raise an exception,
            it should be handled here.
            
        ARGUMENTS:
            action      The action code identifying the method to be executed.
            
        RETURNS:
            Tuple containing [ Repeat, Object ] where Repeat must be boolean.
            If Repeat is true, the operation is repeated. Object is used
            when returning data after the operation is required.
            
        RAISES:
            Nothing
        """
        
        self._selectedAction = action
        LOG("Performing action: " + repr(action), level = LOG_LANG )
        
        # Give the driver the opportunity to do something before the actions
        self._driverBeforeAction(action)

        if action == ACTION_ABORT:
            self._write("Execution aborted by user", {Severity:WARNING} )
            if REGISTRY.exists('EXEC'):
                REGISTRY['EXEC'].abort()
            return [False,False]
        elif action == ACTION_RESEND:
            self._notifyOpStatus( NOTIF_STATUS_PR, OPERATION_REPEAT )
            result = self._doResend()
        elif action == ACTION_REPEAT:
            self._notifyOpStatus( NOTIF_STATUS_PR, OPERATION_REPEAT )
            result = self._doRepeat()
        elif action == ACTION_RECHECK:
            self._notifyOpStatus( NOTIF_STATUS_PR, OPERATION_REPEAT )
            result = self._doRecheck()
        elif action == ACTION_SKIP:
            self._notifyOpStatus( NOTIF_STATUS_SP, OPERATION_SKIPPED )
            result = self._doSkip()       
        elif action == ACTION_NOACTION:
            result = [False,True]       
        elif action == ACTION_CANCEL:
            self._notifyOpStatus( NOTIF_STATUS_CL, OPERATION_CANCELLED )
            result = self._doCancel()       
        elif action == ACTION_HANDLE:
            self._notifyOpStatus( NOTIF_STATUS_SP, OPERATION_HANDLED )
            handle = Handle( code = self._failureCode, type = self._failureType, item = self._failureItem)
            raise handle       
        else:
            # Give the driver the opportunity to perform a custom action
            result = self._driverPerformAction(action)
            if (result is None):
                self._write("Unknown action: " + repr(action), {Severity:ERROR})
                if REGISTRY.exists('EXEC'):
                    REGISTRY['EXEC'].abort()
                return [False,False]

        # Give the driver the opportunity to do something before the actions
        self._driverAfterAction(action)

        return result

    #===========================================================================
    def _doResend(self):
        raise DriverException("Cannot resend", "This action is not defined for this SPELL function")

    #===========================================================================
    def _doRepeat(self):
        raise DriverException("Cannot repeat", "This action is not defined for this SPELL function")

    #===========================================================================
    def _doRecheck(self):
        raise DriverException("Cannot recheck", "This action is not defined for this SPELL function")

    #===========================================================================
    def _doSkip(self):
        raise DriverException("Cannot skip", "This action is not defined for this SPELL function")

    #===========================================================================
    def _doCancel(self):
        raise DriverException("Cannot cancel", "This action is not defined for this SPELL function")
                                
    #===========================================================================
    def _notifyValue(self, name, value, status, reason):
        if self.getConfig(Notify) == False: return
        REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, name, value, status, reason )

    #===========================================================================
    def _notifyTime(self, name, value, status, reason):
        if self.getConfig(Notify) == False: return
        REGISTRY['CIF'].notify( NOTIF_TYPE_TIME, name, value, status, reason )

    #===========================================================================
    def _notifyOpStatus(self, status, comment):
        if self.getConfig(Notify) == False: return
        if self._opName == "" or self._opName == None: return
        REGISTRY['CIF'].notify( NOTIF_TYPE_SYS, self._opName, " ", status, comment )

    #===========================================================================
    def _write(self, message, config = {Severity:INFORMATION} ):
        useConfig = self.getConfig()
        useConfig.update(config)
        REGISTRY['CIF'].write(message, useConfig)

    #===========================================================================
    def _prompt(self, message, options, config):
        return REGISTRY['CIF'].prompt(message, options, config)

    #===========================================================================
    def _getDefaults(self):
        # Obtain the current context
        defaults = None
        result = {}
        if self._interface:
            defaults = REGISTRY['CTX'].getInterfaceConfig(self._interface)
        if defaults is not None:
            result = defaults.copy() 
        fdefaults = REGISTRY['CTX'].getFunctionConfig(self._functionName)
        if fdefaults:
            result.update(fdefaults)
        return result
