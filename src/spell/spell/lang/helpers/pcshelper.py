################################################################################
"""
DESCRIPTION: Helpers for PCS wrapper functions. 
    
PACKAGE: spell.lang.helpers.pcshelper

PROJECT: SPELL

 Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 This library is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, either
 version 3 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License and GNU General Public License (to which the GNU Lesser
 General Public License refers) along with this library.
 If not, see <http://www.gnu.org/licenses/>.
 
"""
###############################################################################

#===============================================================================
# SPELL imports
#===============================================================================
from spell.utils.log import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.exception import *
from spell.lib.adapter.utctime import *
from spell.lang.functions import *
from spell.lib.adapter.constants.core import KEY_SEPARATOR
from spell.lib.adapter.pcs_item import PtcItemClass
from spell.lib.adapter.constants.notification import *
from spell.lib.registry import *
from spell.lib.adapter.result import PtcResult
#===============================================================================
# Local imports
#===============================================================================
from .basehelper import *

#===============================================================================
# System imports
#===============================================================================
import time,sys


class BuildPTC_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Build PCS wrapper.
    """
    _pcsName = None
    _pcsArguments = []
    _pcsItem = None
    _isSequence = False

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self._pcsName = None
        self._pcsArguments = []
        self._opName = "PTC build"
        self._pcsItem = None
        self._isSequence = False

    #===========================================================================
    def _obtainCommandName(self, *args, **kargs ):
        if len(args)==1:
            if type(args[0])!=str:
                raise SyntaxException("Expected a pseudo command name")
            self._pcsName = args[0]
        elif len(args)==0:
            if 'command' in kargs:
                self._pcsName = kargs.get('command')
            else:
                raise SyntaxException("Expected a pseudo command")
        else:
            raise SyntaxException("Expected a pseudo command name")

    #===========================================================================
    def _obtainCommandArguments(self, *args, **kargs ):
        if len(args)<=1:
            if 'args' in kargs:
                self._pcsArguments = kargs.get('args')
        else:
            if type(args[1])!=list:
                raise SyntaxException("Expected a list of arguments")
            self._pcsArguments = args[1]

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        self._obtainCommandName(*args,**kargs)
        self._obtainCommandArguments(*args,**kargs)
        # Store information for possible failures
        self.setFailureInfo("PCS", self._pcsName)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the pseudo command construction and return None")
        self._setActionString( ACTION_REPEAT   ,  "Repeat the pseudo command construction")

        if self._isSequence:
            self._write("Building pseudo sequence " + repr(self._pcsName))
        else:
            self._write("Building pseudo command " + repr(self._pcsName))

        # Create the item
        LOG("Obtaining PCS entity: " + repr(self._pcsName), level = LOG_LANG)
        self._pcsItem = REGISTRY['PCS'][self._pcsName]
        self._pcsItem.clear()
        self._pcsItem.configure(self.getConfig())
        if self._isSequence:
            self._pcsItem.addConfig(Sequence,True)

        # Assign the arguments
        for pcsArg in self._pcsArguments:
            LOG("Parsed PCS argument: " + repr(pcsArg[0]), level = LOG_LANG)
            LOG("Argument config   : " + repr(pcsArg[1:]), level = LOG_LANG)
            self._pcsItem[ pcsArg[0] ] = pcsArg[1:]
            self._write("    - Argument " + repr(pcsArg[0]) + " value " + repr(pcsArg[1:]))

        return [False,self._pcsItem,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doSkip(self):
        self._write("Skipping pseudo command construction", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        self._pcsItem = None
        return [False,None]

    #===========================================================================
    def _doRepeat(self):
        self._write("Repeat pseudo command construction", {Severity:WARNING} )
        return [True,False]


################################################################################
class PCS_IsArqEnabled_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the PCS_isArqEnabled wrapper function.
    """
    __parameter = None
    __property = None
    __retry = False

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self._opName = "PCS_IsArqEnabled"
        self.__parameter = None
        self.__property = None

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)>0:
            raise SyntaxException("PCS_IsArqEnabled function does not accept arguments")

        self.__useConfig = {}
        self.__useConfig.update(self.getConfig())
        self.__useConfig[Retry] = self.__retry

#        REGISTRY['CIF'].write("pcshelper ReadyToCommand doOperation", {Severity:WARNING})

        # Store information for possible failures
        self.setFailureInfo("PCS", "PCS_IsArqEnabled")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the PCS_IsArqEnabled and return (True)")
        self._setActionString( ACTION_CANCEL ,  "Cancel the PCS_IsArqEnabled and return (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the PCS_IsArqEnabled")

        result = REGISTRY['PCS'].PCS_IsArqEnabled( self.__parameter, config = self.getConfig() )

        return [False,result,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _getBehaviorOptions(self, exception = None):

        # If the OnFailure parameter is not set, get the default behavior.
        # This default behavior depends on the particular primitive being
        # used, so it is implemented in child wrappers.
        if self.getConfig(OnFailure) is None:
            LOG("Using defaults")
            self.setConfig({OnFailure:ABORT})

        # Special case for verify, OnFalse
        if exception and (exception.reason.find("evaluated to False")>0):
            optionRef = self.getConfig(OnFalse)
        else:
            optionRef = self.getConfig(OnFailure)

        # Get the desired behavior
        theOptions = self._getActionList( optionRef )

        return theOptions

    #===========================================================================
    def _processActionOnResult(self, result):
        return [False, result]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry PCS_IsArqEnabled", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip PCS_IsArqEnabled", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel PCS_IsArqEnabled", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, False]


################################################################################
class PCS_GetStatus_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the PCS_GetStatus wrapper.
    """

    __useConfig = {}
    # Name of the parameter to be checked
    __extended = False
    __retry = False

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self.__parameter = None
        self.__extended = False
        self._opName = "PCS_GetStatus"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)>0:
            raise SyntaxException("PCS_GetStatus function does not accept arguments")

        self.__useConfig = {}
        self.__useConfig.update(self.getConfig())
        self.__useConfig[Retry] = self.__retry

#        REGISTRY['CIF'].write("pcshelper ReadyToCommand doOperation", {Severity:WARNING})

        # Store information for possible failures
        self.setFailureInfo("PCS", "PCS_GetStatus")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_REPEAT , "Repeat PCS_GetStatus")
        self._setActionString( ACTION_SKIP   , "Skip PCS_GetStatus and return True")
        self._setActionString( ACTION_CANCEL , "Cancel PCS_GetStatus and return False")

#        REGISTRY['CIF'].write("pcshelper GetStatus doOperation", {Severity:WARNING})
#        self._write("test write")

        result = REGISTRY['PCS'].PCS_GetStatus( self.__useConfig )

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry PCS_GetStatus", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip PCS_GetStatus", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel PCS_GetStatus", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, False]


################################################################################
class PCS_IsVerifyModeEnabled_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the IsVerifyModeEnabled wrapper function.
    """
    __retryAll = False
    __retry = False
    __useConfig = {}
    __vrfDefinition = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self.__retryAll = False
        self.__retry = False
        self.__useConfig = {}
        self.__vrfDefinition = None
        self._opName = "PCS_VerifyMode"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)>0:
            raise SyntaxException("PCS_IsVerifyModeEnabled function does not accept arguments")

        self.__useConfig = {}
        self.__useConfig.update(self.getConfig())
        self.__useConfig[Retry] = self.__retry


#        REGISTRY['CIF'].write("pcshelper ReadyToCommand doOperation", {Severity:WARNING})

        # Store information for possible failures
        #self.setFailureInfo("PCS", "PCS_IsVerifyModeEnabled")
        #self.setConfig(OnFalse,None)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        #self._notifyOpStatus( NOTIF_STATUS_PR, "Checking PCS_IsVerifyModeEnabled..." )

        self._setActionString( ACTION_SKIP   ,  "Skip the PCS_IsVerifyModeEnabled and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Cancel the PCS_IsVerifyModeEnabled and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the PCS_IsVerifyModeEnabled")
        #self._setActionString( ACTION_RECHECK,  "Repeat the telemetry verification")

        # Wait some time before verifying if requested
        if Delay in self.__useConfig:
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before PCS_IsVerifyModeEnabled", {Severity:INFORMATION})
                WaitFor(delay)

        result = REGISTRY['PCS'].PCS_IsVerifyModeEnabled( self.__vrfDefinition, self.__useConfig )

        # If we reach here, result can be true or false, but no exception was raised
        # this means that a false verification is considered ok.

        return [False,result,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _getBehaviorOptions(self, exception = None):

        # If the OnFailure parameter is not set, get the default behavior.
        # This default behavior depends on the particular primitive being
        # used, so it is implemented in child wrappers.

        if self.getConfig(OnFailure) is None:
            LOG("Using defaults")
            self.setConfig({OnFailure:ABORT})

        # Special case for verify, OnFalse
        if exception and (exception.reason.find("evaluated to False")>0):
            optionRef = self.getConfig(OnFalse)
        else:
            optionRef = self.getConfig(OnFailure)

        # Get the desired behavior
        theOptions = self._getActionList( optionRef )

        return theOptions

    #===========================================================================
    def _processActionOnResult(self, result):
        return [False, result]

    #===========================================================================
    def _getExceptionFlag(self, exception ):
        # Special case for verify, OnFalse
        if exception.reason.find("evaluated to False")>0:
            return self.getConfig(PromptUser)
        else:
            return self.getConfig(PromptFailure)

#    #===========================================================================
#    def _doSkip(self):
#        if self.getConfig(PromptUser)==True:
#            self._write("Verification skipped", {Severity:WARNING} )
#        return [False,True]
#
#    #===========================================================================
#    def _doCancel(self):
#        if self.getConfig(PromptUser)==True:
#            self._write("Verification skipped", {Severity:WARNING} )
#        return [False,False]
#
#    #===========================================================================
#    def _doRecheck(self):
#        self._write("Retry verification", {Severity:WARNING} )
#        return [True,False]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry PCS_IsVerifyModeEnabled", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip PCS_IsVerifyModeEnabled", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel PCS_IsVerifyModeEnabled", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, False]


###############################################################################
class PCS_AbortPtc_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the PCS_AbortPtc wrapper function.
    """
    __retryAll = False
    __retry = False
    __useConfig = {}
    __vrfDefinition = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self.__retryAll = False
        self.__retry = False
        self.__useConfig = {}
        self.__vrfDefinition = None
        self._opName = "PCS_AbortPtc"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)>0:
            raise SyntaxException("PCS_AbortPtc function does not accept arguments")

        self.__useConfig = {}
        self.__useConfig.update(self.getConfig())
        self.__useConfig[Retry] = self.__retry

#        REGISTRY['CIF'].write("pcshelper ReadyToCommand doOperation", {Severity:WARNING})

        # Store information for possible failures
        self.setFailureInfo("PCS", "PCS_AbortPtc")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._notifyOpStatus( NOTIF_STATUS_PR, "Verifying..." )

        self._setActionString( ACTION_SKIP   ,  "Skip the PCS_AbortPtc and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Cancel the PCS_AbortPtc and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the PCS_AbortPtc")
#        self._setActionString( ACTION_RECHECK,  "Repeat the telemetry verification")

        # Wait some time before verifying if requested
        if Delay in self.__useConfig:
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before PCS_AbortPtc", {Severity:INFORMATION})
                WaitFor(delay)

        result = REGISTRY['PCS'].PCS_AbortPtc( self.__vrfDefinition, self.__useConfig )

        # If we reach here, result can be true or false, but no exception was raised
        # this means that a false verification is considered ok.

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _getBehaviorOptions(self, exception = None):

        # If the OnFailure parameter is not set, get the default behavior.
        # This default behavior depends on the particular primitive being
        # used, so it is implemented in child wrappers.
        if self.getConfig(OnFailure) is None:
            LOG("Using defaults")
            self.setConfig({OnFailure:ABORT})

        # Special case for verify, OnFalse
        if exception and (exception.reason.find("evaluated to False")>0):
            optionRef = self.getConfig(OnFalse)
        else:
            optionRef = self.getConfig(OnFailure)

        # Get the desired behavior
        theOptions = self._getActionList( optionRef )

        return theOptions

    #===========================================================================
    def _getExceptionFlag(self, exception ):
        # Special case for verify, OnFalse
        if exception.reason.find("evaluated to False")>0:
            return self.getConfig(PromptUser)
        else:
            return self.getConfig(PromptFailure)

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry PCS_AbortPtc", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip PCS_AbortPtc", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel PCS_AbortPtc", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, False]


#    #===========================================================================
#    def _doSkip(self):
#        if self.getConfig(PromptUser)==True:
#            self._write("Verification skipped", {Severity:WARNING} )
#        return [False,True]
#
#    #===========================================================================
#    def _doCancel(self):
#        if self.getConfig(PromptUser)==True:
#            self._write("Verification skipped", {Severity:WARNING} )
#        return [False,False]
#
#    #===========================================================================
#    def _doRecheck(self):
#        self._write("Retry verification", {Severity:WARNING} )
#        return [True,False]


###############################################################################
class PCS_ReadyToCommand_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the ReadyToCommand wrapper function.
    """
    __retry = False # In case of not ready enable retry

    __useConfig = {}
    __verifyCondition = None
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self.__retry = False
        self.__useConfig = {}
        self._opName = "PCS_ReadyToCommand"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)>0:
            raise SyntaxException("PCS_ReadyToCommand function does not accept arguments")

        self.__useConfig = {}
        self.__useConfig.update(self.getConfig())
        self.__useConfig[Retry] = self.__retry

#        REGISTRY['CIF'].write("pcshelper ReadyToCommand doOperation", {Severity:WARNING})

        # Store information for possible failures
        self.setFailureInfo("PCS", "PCS_ReadyToCommand")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._notifyOpStatus( NOTIF_STATUS_PR, "PCS Readiness check..." )

        self._setActionString( ACTION_SKIP   ,  "Skip the PCS readiness check and return ready (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the PCS readiness check and return not ready (False)")
        self._setActionString( ACTION_RECHECK,  "Recheck the PCS readiness")

        # Wait some time before verifying if requested
        if Delay in self.__useConfig:
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before PCS readiness check", {Severity:INFORMATION})
                WaitFor(delay)

#        REGISTRY['CIF'].write("pcshelper doOperation", {Severity:WARNING})
        result = REGISTRY['PCS'].PCS_ReadyToCommand( self.__useConfig )

        # If we reach here, result can be true or false, but no exception was raised
        # this means that a false readiness check is considered ok.

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _getBehaviorOptions(self, exception = None):

        # If the OnFailure parameter is not set, get the default behavior.
        # This default behavior depends on the particular primitive being
        # used, so it is implemented in child wrappers.
        if self.getConfig(OnFailure) is None:
            LOG("Using defaults")
            self.setConfig({OnFailure:ABORT})

        # Special case for PCS readiness check, OnFalse
        if exception and (exception.reason.find("evaluated to False")>0):
            optionRef = self.getConfig(OnFalse)
        else:
            optionRef = self.getConfig(OnFailure)

        # Get the desired behavior
        theOptions = self._getActionList( optionRef )

        return theOptions

    #===========================================================================
    def _getExceptionFlag(self, exception ):
        # Special case for PCS readiness check, OnFalse
        if exception.reason.find("evaluated to False")>0:
            return self.getConfig(PromptUser)
        else:
            return self.getConfig(PromptFailure)

    #===========================================================================
    def _doSkip(self):
        if self.getConfig(PromptUser)==True:
            self._write("PCS readiness check skipped", {Severity:WARNING} )
        return [False,True]

    #===========================================================================
    def _doCancel(self):
        if self.getConfig(PromptUser)==True:
            self._write("PCS readiness check canceled", {Severity:WARNING} )
        return [False,False]

    #===========================================================================
    def _doRecheck(self):
        self._write("Retry PCS readiness check", {Severity:WARNING} )
        return [True,False]
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry PCS readiness check", {Severity:WARNING} )
        return [True,False]

###############################################################################
class PCS_Send_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the PCS_Send wrapper function.
    """
    __retry = False # In case of not ready enable retry
    _cmdName = None
    _cmdDef = None
    _cmdArgs = []
    __useConfig = {}
    __verifyCondition = None
    _isGroup = False
    __originalOnFailure = None
    __doSendCommand = True
    __section = 'PCS'
    __actionTaken = None
    __verifyCondition = None
    __doCheckTelemetry = False

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self._reset()
        self.__retry = False
        self.__useConfig = {}
        self._opName = "PCS_Send"
    #===========================================================================
    def _reset(self):
        self._isGroup = False
        self.__originalOnFailure = None
        self._isSequence = False
        self._cmdName = None
        self._cmdDef = None
        self._cmdArgs = None
        self.__doSendCommand = True
        self.__doAdjustLimits = False
        self.__doAdjustLimitsP = False
        self.__canAdjustLimits = False
        self.__section = 'PCS'
        self.__actionTaken = None
        self.__verifyCondition = None
        self.__doCheckTelemetry = False
        self.__confirmed = False
        self._tcResult = PtcResult()
    #===========================================================================
    def _initializeActionStrings(self):
        WrapperHelper._initializeActionStrings(self)
        self._setActionString( ACTION_REPEAT ,  "Repeat the whole PCS_Send() operation")
        self._setActionString( ACTION_RECHECK,  "Repeat the telemetry verification")
        self._setActionString( ACTION_RESEND ,  "Send the pseudo command(s) again")
        self._setActionString( ACTION_SKIP   ,  "Skip the pseudo command injection and proceed with telemetry verification")
        self._setActionString( ACTION_CANCEL ,  "Skip the whole operation and proceed with next SPELL instruction")
        #===========================================================================

    def _obtainVerificationDefinition(self,*args,**kargs):
        # Obtain verification steps
        if self._cmdArgs is not None and len(args)>=3:
            self.__verifyCondition = args[3]
            if type(self.__verifyCondition) != list:
                raise SyntaxException("Expected a list of verification steps")
        elif self._cmdArgs is None and len(args)>=2:
            self.__verifyCondition = args[2]
            if type(self.__verifyCondition) != list:
                raise SyntaxException("Expected a list of verification steps")
        elif 'verify' in kargs:
            self.__verifyCondition = kargs.pop('verify')
        else:
            self.__verifyCondition = None
        if self.__verifyCondition:
            self.__doCheckTelemetry = True

    #===========================================================================
    def _obtainCommandDefinition(self, *args, **kargs):
        LOG("Obtaining pseudo command definition", level = LOG_LANG)
        if len(args) == 0:
            LOG("No positional arguments", level = LOG_LANG)
            # If no positional arguments are given, the command shall be
            # given with these keywords
            if 'command' not in kargs and\
               'group' not in kargs:
                raise SyntaxException("Expected a pseudo command item or name")
            else:
                if 'command' in kargs:
                    LOG("Using keyword argument command", level = LOG_LANG)
                    self._isGroup = False
                    self._cmdDef = kargs.pop('command')
                    if type(self._cmdDef)==list:
                        raise SyntaxException("Cannot accept list as single command")
                elif 'group' in kargs:
                    LOG("Using keyword argument group", level = LOG_LANG)
                    self._isGroup = True
                    self._cmdDef = kargs.pop('group')
                    if type(self._cmdDef)!=list:
                        raise SyntaxException("Shall provide a command list")
                else:
                    raise SyntaxException("Expected keyword: command, group")
        else:
            raise SyntaxException("Expected keyword: command, group")

        # Create the command item if necessary
        if type(self._cmdDef)==str:
            self._cmdDef = REGISTRY['PCS'][self._cmdDef]
        # Do it for each item in the list, if it is the case
        elif type(self._cmdDef)==list:
            cpy = []
            for item in self._cmdDef:
                if type(item)==str:
                    cpy += [REGISTRY['PCS'][item]]
                elif isinstance(item,PtcItemClass):
                    cpy += [item]
                else:
                    raise SyntaxException("Unexpected item in group: " + repr(item))

        # Obtain the string representation of the entity being sent
        if type(self._cmdDef)==list:
            self._cmdName = []
            for item in self._cmdDef:
                if type(item)==str:
                    self._cmdName += [item]
                # Must be tc item, the check was done already
                else:
                    desc = item.desc()
                    if desc != "": desc = ": " + desc
                    self._cmdName += [item.name() + desc]
                # The else case is already controlled
        else:
            desc = self._cmdDef.desc()
            if desc != "": desc = ": " + desc
            self._cmdName = self._cmdDef.name() + desc

        LOG("Got pseudo command definition: " + str(self._cmdName), level = LOG_LANG)
        LOG("Group flag   : " + str(self._isGroup), level = LOG_LANG)

        # Copy the flags to config
        self.addConfig(Sequence,self._isSequence)

    #===========================================================================
    def _checkCommandDefinition(self):
        if not isinstance(self._cmdDef,PtcItemClass) and\
           not type(self._cmdDef) == str and\
           not type(self._cmdDef) == list:
            raise SyntaxException("Expected a PTC name, PTC item or PTC list")

    #===========================================================================
    def _obtainCommandArguments(self, *args, **kargs):
        # 3. Obtain the arguments
        self._cmdArgs = None
        if not self._isGroup:
            LOG("Getting arguments for single pseudo command", level = LOG_LANG)
            if 'args' in kargs:
                LOG("Using keyword args", level = LOG_LANG)
                self._cmdArgs = kargs.pop('args')
            else:
                LOG("No arguments found", level = LOG_LANG)
                self._cmdArgs = None
        # Using a group and args kword is not accepted (??)
        else:
            if 'args' in kargs:
                raise SyntaxException("Cannot use args with PTC lists")

    #===========================================================================
    def _parseCommandArguments(self):
        # 6. Parse arguments if any
        if self._cmdArgs is not None:
            if len(self._cmdArgs)==0:
                raise SyntaxException("Cannot accept empty argument list")
            # Clear any previously existing argument
            self._cmdDef.clear()
            for argument in self._cmdArgs:
                if type(argument)!=list:
                    raise SyntaxException("Malformed argument")
                if len(argument)<1 or type(argument[0])!=str:
                    raise SyntaxException("Malformed argument")
                argName = argument[0]
                argument = argument[1:]
                LOG("Set argument: " + str(argName) + "=" + repr(argument), level = LOG_LANG)
                self._cmdDef[argName] = argument

    #===========================================================================
    def _checkCommandArguments(self):
        if not self._cmdArgs is None and type(self._cmdArgs)!=list:
            raise SyntaxException("Expected an argument list")

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        #-----------------------------------------------------------------------
        # Parse the command information
        #-----------------------------------------------------------------------
        # 1. Obtain the command/sequence
        self._obtainCommandDefinition(*args,**kargs)
        # 2. Check the command correctness
        self._checkCommandDefinition()
        # 3. Obtain tc arguments
        self._obtainCommandArguments(*args,**kargs)
        # 4. Check arguments correctness
        self._checkCommandArguments()
        # 5. Parse command arguments
        self._parseCommandArguments()

        #-----------------------------------------------------------------------
        # Parse the telemetry information
        #-----------------------------------------------------------------------
        self._obtainVerificationDefinition(*args,**kargs)
        if type(self.__verifyCondition)==list:
            if type(self.__verifyCondition[0])!=list:
                self.__verifyCondition = [self.__verifyCondition]

        # Store information for possible failures
        self.setFailureInfo("PCS", self._cmdDef)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        self.__originalOnFailure = self.getConfig(OnFailure)
        repeat = False

        self._notifyOpStatus( NOTIF_STATUS_PR, "PCS Send ..." )

        if self.__verifyCondition:
            self._setActionString( ACTION_SKIP   ,  "Skip the PCS Send. Proceed with telemetry verification")
        else:
            self._setActionString( ACTION_SKIP   ,  "Skip the PCS Send and return ready (True))")

        self._setActionString( ACTION_CANCEL ,  "Cancel the PCS send and return not ready (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the PCS send")

        if self._isGroup:
            if self.__verifyCondition:
                self._setActionString( ACTION_RESEND ,  "Resend all pseudo commands that failed from group with telemetry verification")
            else:
                self._setActionString( ACTION_RESEND ,  "Resend all pseudo commands that failed from group")

        else:
            if self.__verifyCondition:
                self._setActionString( ACTION_RESEND ,  "Send the whole pseudo command again with telemetry verification")
            else:
                self._setActionString( ACTION_RESEND ,  "Send the whole pseudo command again")

        #self._setActionString( ACTION_RECHECK,  "Recheck the PCS send")

        # Wait some time before verifying if requested
        if Delay in self.__useConfig:
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before PCS readiness check", {Severity:INFORMATION})
                WaitFor(delay)

#        REGISTRY['CIF'].write("pcshelper doOperation", {Severity:WARNING})
        self.addConfig(OnFailure, self.__originalOnFailure)
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))
        self.addConfig(OnFalse,self.getConfig(OnFalse) & (~RECHECK))
        tcIsSuccess = self._tcResult
        try:
            if self.__doSendCommand:
                self.__section = 'PTC'
                LOG("Sending pseudo command " + repr(self._cmdDef))
                self._write("Sending pseudo command " + repr(self._cmdDef))
                self._tcResult = REGISTRY['PCS'].PCS_Send( self._cmdDef, self.__useConfig )
                tcIsSuccess = self._tcResult
                if not tcIsSuccess:
                    raise DriverException("Could not execute PCS_Send:" + "\n".join(self._tcResult['error']))
                self._write("PCS Send Execution success")
                LOG("PCS Send succeeded with result: " + str(tcIsSuccess))
            else:
                tcIsSuccess = True
                if self._tcResult:
                    self._tcResult._test = True
        except DriverException as ex:
            LOG("PCS Send failed with exception " + repr(ex))
            self._write("PCS Send Execution failed", {Severity:ERROR} )
            if not self._handleError: raise ex
            action = self._handleException(ex)
            LOG("Action is " + repr(action))
            repeat, result = self._doAction(action)
            LOG("Result of action is " + repr([repeat,result]))
            if repeat:
                LOG("Will repeat cycle")
                if action == 'Q':
                    LOG("Q==> AdjustP=False, Command=False, Telemetry=False, Adjust=False")
                    self.__doSendCommand = False
                    self.__doCheckTelemetry = False
                    opStatus = NOTIF_STATUS_CL
                elif action == 'K':
                    LOG("K==> AdjustP=False, Command=False, Telemetry=True, Adjust=True")
                    self.__doSendCommand = False
                    self.__doCheckTelemetry = True
                    opStatus = NOTIF_STATUS_SP
                elif action == 'S':
                    LOG("S==> AdjustP=False, Command=True, Telemetry=True, Adjust=True")
                    self.__doSendCommand = True
                    self.__doCheckTelemetry = True
                    opStatus = NOTIF_STATUS_PR
                elif action == 'R':
                    LOG("S==> AdjustP=True, Command=True, Telemetry=True, Adjust=True")
                    self.__doSendCommand = True
                    self.__doCheckTelemetry = True
                    opStatus = NOTIF_STATUS_PR
                else:
                    LOG("Custom action with result " + repr(result))
                    LOG("CA==> AdjustP=False, Command=False, Telemetry=" + str(result) + ", Adjust=" + str(result))
                    self.__doSendCommand = result
                    self.__doCheckTelemetry = result
                    if result:
                        opStatus = NOTIF_STATUS_SP
                    else:
                        opStatus = NOTIF_STATUS_CL
                return [repeat,result,opStatus,""]
            else:
                LOG("Will NOT repeat cycle, TC success = " + repr(result))
                result = tcIsSuccess
                if action not in ['Q','K','S','R']:
                    LOG("Custom action with result " + repr(result))
                    LOG("CA==> AdjustP=False, Command=False, Telemetry=" + str(result) + ", Adjust=" + str(result))
                    self.__doCheckTelemetry = result

        # If we reach here, result can be true or false, but no exception was raised
        # this means that a false readiness check is considered ok.

        self.addConfig(OnFailure, self.__originalOnFailure)

        #-----------------------------------------------------------------------
        # TELEMETRY SECTION
        #-----------------------------------------------------------------------
        LOG("Should perform telemetry part: " + repr(self.__doCheckTelemetry) + "," + repr(self.__verifyCondition) + "," + repr(tcIsSuccess))
        if self.__doCheckTelemetry and self.__verifyCondition and tcIsSuccess:
            LOG("Entering in TM section")
            self.__section = 'TM'
            # Store information for possible failures
            self.setFailureInfo("TM", self.__verifyCondition)

            # Adapt the action messages
            self._setActionString( ACTION_RECHECK,  "Repeat the telemetry verification")
            self._setActionString( ACTION_SKIP   ,  "Skip the telemetry verification and return success (True)")
            self._setActionString( ACTION_CANCEL ,  "Skip the telemetry verification and return failure (False)")

            if self.hasConfig(Delay):
                delay = self.getConfig(Delay)
                if delay:
                    from spell.lang.functions import WaitFor
                    self._write("Waiting "+ str(delay) + " seconds before TM verification", {Severity:INFORMATION})
                    WaitFor(delay, Notify=False, Verbosity=999)

            # We dont allow repeat here but allow recheck at least
            self.addConfig(OnFailure,self.getConfig(OnFailure) & (~REPEAT))
            self.addConfig(OnFailure,self.getConfig(OnFailure) | (RECHECK))
            self.addConfig(OnFalse,self.getConfig(OnFalse) | (RECHECK))

            # Adapt the action messages
            self._setActionString( ACTION_RECHECK,  "Repeat the telemetry verification")
            self._setActionString( ACTION_SKIP   ,  "Skip the telemetry verification and return success (True)")
            self._setActionString( ACTION_CANCEL ,  "Skip the telemetry verification and return failure (False)")

            # Perform verification
            tmIsSuccess = REGISTRY['TM'].verify(self.__verifyCondition, config=self.getConfig())

        else:
            tmIsSuccess = True

        if self.__verifyCondition is None:
            result = self._tcResult
        else:

            self._tcResult._test = self._tcResult._test and tmIsSuccess
            result = self._tcResult

        if self.__actionTaken in ["SKIP","CANCEL"]:
            opStatus = NOTIF_STATUS_SP
        elif result:
            opStatus = NOTIF_STATUS_OK
        else:
            opStatus = NOTIF_STATUS_FL

        LOG("Overall operation result: " + repr([ repeat, result, opStatus, "" ]))

        return [ repeat, result, opStatus, "" ]

    #===========================================================================
    def _getBehaviorOptions(self, exception = None):

        # If the OnFailure parameter is not set, get the default behavior.
        # This default behavior depends on the particular primitive being
        # used, so it is implemented in child wrappers.
        if self.getConfig(OnFailure) is None:
            LOG("Using defaults")
            self.setConfig({OnFailure:ABORT})

        # Special case for PCS readiness check, OnFalse
        if exception and (exception.reason.find("evaluated to False")>0):
            optionRef = self.getConfig(OnFalse)
        else:
            optionRef = self.getConfig(OnFailure)

        # Get the desired behavior
        theOptions = self._getActionList( optionRef )

        return theOptions

    #===========================================================================
    def _getExceptionFlag(self, exception ):
        # Special case for PCS readiness check, OnFalse
        if exception.reason.find("evaluated to False")>0:
            return self.getConfig(PromptUser)
        else:
            return self.getConfig(PromptFailure)

    def _doSkip(self):
        self.__actionTaken = "SKIP"
        if self.getConfig(PromptUser)==True:
            self._write("Operation skipped", {Severity:WARNING} )
        # By skipping the operation, if we are in LIM1 or TC stages we still
        # want to verify TM
        if self.__section in ['PCS']:
            self.__doSendCommand = False
            self.__doCheckTelemetry = True
            LOG("Skip but check telemetry")
            self._tcResult._test = True
            return [True,self._tcResult]
        elif self.__section == 'TM':
            self.__doSendCommand = False
            self.__doCheckTelemetry = False
            LOG("Skip and not check telemetry")
            self._tcResult._test = True
            return [True, self._tcResult]
        else:
            LOG("Skip all returning True")
            self._tcResult._test = True
            return [False,self._tcResult]

    #===========================================================================
    def _doCancel(self):
        self._write("Operation cancelled", {Severity:WARNING} )
        self.__actionTaken = "CANCEL"
        LOG("Cancel and dont check telemetry")
        self._tcResult._test = False
        return [False,self._tcResult]

    #===========================================================================
    def _doResend(self):
        self.__actionTaken = "RESEND"
        if self._isSequence:
            self._write("Retrying sequence execution", {Severity:WARNING} )
        elif self._isGroup:
            self._write("Retrying group execution", {Severity:WARNING} )
        else:
            self._write("Retrying command execution", {Severity:WARNING} )
        self.__doSendCommand = True
        self.__doCheckTelemetry = True
        self._tcResult._test = False
        try:
            errorCodes = self._tcResult.code()
            successEntries = self._tcResult.success()
            if type(self._cmdDef) == list:
                skippedCmds = []
                for idx in range(0,len(errorCodes)):
                    if errorCodes[idx] == 0 and successEntries[idx]:
                        cur = self._cmdDef[idx]
                        skippedCmds += [cur]
                        self._write("Skipping command " + str(cur.name()) + ". It was executed successfully before.", {Severity:WARNING} )
                    idx += 1
                for cmd in skippedCmds:
                    self._cmdDef.remove(cmd)
        except:
            pass
        return [True,self._tcResult]

    #===========================================================================
    def _doRepeat(self):
        self.__actionTaken = "CANCEL"
        self._write("Retry whole operation", {Severity:WARNING} )
        self.__doAdjustLimits = True
        self.__doAdjustLimitsP = True
        self.__doSendCommand = True
        self.__doCheckTelemetry = True
        self._tcResult._test = False
        return [True,self._tcResult]

    #===========================================================================
    def _doRecheck(self):
        self.__actionTaken = "RECHECK"
        self._write("Retry verification block", {Severity:WARNING} )
        self.__doSendCommand = False
        self.__doAdjustLimitsP = False
        self.__doAdjustLimits = True
        self.__doCheckTelemetry = True
        self._tcResult._test = False
        return [True,self._tcResult]


###############################################################################
class PCS_Control_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the PCS_Control wrapper function.
    """
    __retry = False # In case of not ready enable retry
    _cmdName = None
    _cmdDef = None
    _cmdArgs = []
    __useConfig = {}

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "PCS")
        self.__retry = False
        self.__useConfig = {}
        self._opName = "PCS_Control"
        self._result = PtcResult()
    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        #-----------------------------------------------------------------------
        # Parse the command information
        #-----------------------------------------------------------------------
        self.__useConfig.update(self.getConfig())

        if 'command' in kargs:
            self._cmdDef = kargs.pop('command')
            self._cmdName = self._cmdDef

        LOG("Got command definition: " + str(self._cmdName), level = LOG_LANG)

        #-----------------------------------------------------------------------
        # Parse the arguments information
        #-----------------------------------------------------------------------
        if 'args' in kargs:
            LOG("Using keyword args", level = LOG_LANG)
            self._cmdArgs = kargs.pop('args')

        LOG("Got arguments: " + str(self._cmdArgs), level = LOG_LANG)
        #self.__useConfig['args']=self._cmdArgs
        self.__useConfig.setdefault('args',self._cmdArgs)
        LOG("Config: " + str(self.__useConfig), level = LOG_LANG)
        #self.__useConfig['args'] = list(self._cmdArgs)

        #self.__useConfig['args'].a
#        for value in self._cmdArgs:
#            self.__useConfig['args'].append(value)

#        if self._cmdArgs is not None:
#            if len(self._cmdArgs)==0:
#                raise SyntaxException("Cannot accept empty argument list")
#            # Clear any previously existing argument
#            #self._cmdDef.clear()
#            for argument in self._cmdArgs:
#                if type(argument)!=list:
#                    raise SyntaxException("Malformed argument")
#                if len(argument)<1 or type(argument[0])!=str:
#                    raise SyntaxException("Malformed argument")
#                argName = argument[0]
#                argument = argument[1:]
#                LOG("Set argument: " + str(argName) + "=" + repr(argument), level = LOG_LANG)
#                self._cmdDef[argName] = argument

        # Store information for possible failures
        self.setFailureInfo("PCS", "PCS_ReadyToCommand")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._notifyOpStatus( NOTIF_STATUS_PR, "PCS Send ..." )

        self._setActionString( ACTION_SKIP   ,  "Skip the PCS control message and return ready (True)")
        self._setActionString( ACTION_CANCEL ,  "Cancel the PCS control message and return not ready (False)")
        self._setActionString( ACTION_RESEND ,  "Resend the PCS control message")

        # Wait some time before verifying if requested
        if Delay in self.__useConfig:
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before PCS readiness check", {Severity:INFORMATION})
                WaitFor(delay)

#        REGISTRY['CIF'].write("pcshelper doOperation", {Severity:WARNING})

        result = REGISTRY['PCS'].PCS_Control( self._cmdName, self.__useConfig )
        if not result:
            raise DriverException("Could not execute PCS_Control: " + "\n".join(result['error']))
        self._write("PCS Control Execution success")
        LOG("PCS Control succeeded with result: " + str(result))

        self._result = result
        # If we reach here, result can be true or false, but no exception was raised
        # this means that a false readiness check is considered ok.

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _getBehaviorOptions(self, exception = None):

        # If the OnFailure parameter is not set, get the default behavior.
        # This default behavior depends on the particular primitive being
        # used, so it is implemented in child wrappers.
        if self.getConfig(OnFailure) is None:
            LOG("Using defaults")
            self.setConfig({OnFailure:ABORT})

        # Special case for PCS readiness check, OnFalse
        if exception and (exception.reason.find("evaluated to False")>0):
            optionRef = self.getConfig(OnFalse)
        else:
            optionRef = self.getConfig(OnFailure)

        # Get the desired behavior
        theOptions = self._getActionList( optionRef )

        return theOptions

    #===========================================================================
    def _getExceptionFlag(self, exception ):
        # Special case for PCS readiness check, OnFalse
        if exception.reason.find("evaluated to False")>0:
            return self.getConfig(PromptUser)
        else:
            return self.getConfig(PromptFailure)

    #===========================================================================
    def _doSkip(self):
        if self.getConfig(PromptUser)==True:
            self._write("PCS send skipped", {Severity:WARNING} )
        self._result._test = True
        return [False,self._result]

    #===========================================================================
    def _doCancel(self):
        if self.getConfig(PromptUser)==True:
            self._write("PCS send canceled", {Severity:WARNING} )
        self._result._test = False
        return [False,self._result]

    #===========================================================================
    def _doResend(self):
        self._write("Retry PCS send", {Severity:WARNING} )
        self._result._test = False
        return [True,self._result]
