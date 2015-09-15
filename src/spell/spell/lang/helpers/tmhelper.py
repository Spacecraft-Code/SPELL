###################################################################################
## MODULE     : spell.lang.helpers.tmhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for telemetry functions
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

from basehelper import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *
from spell.lib.exception import SyntaxException
from spell.lang.functions import *
from spell.lib.registry import *

################################################################################
class GetTM_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the GetTM wrapper.
    """    
    
    # Name of the parameter to be checked
    __parameter = None
    __extended = False
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__extended = False
        self._opName = None

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        from spell.lib.adapter.tm_item import TmItemClass
        if len(args)==0:
            raise SyntaxException("No parameter name given")

        # Check correctness
        param = args[0]
        if type(param) != str and not isinstance(param,TmItemClass):
            raise SyntaxException("Expected a TM item or name")

        self.__parameter = param

        # Store the extended flag if any
        self.__extended = (self.getConfig(Extended) == True)
        
        # Store information for possible failures
        self.setFailureInfo("TM", self.__parameter)
        
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_REPEAT , "Acquire telemetry parameter " + repr(self.__pname()) + " again")
        self._setActionString( ACTION_SKIP   , "Skip the acquisition of telemetry parameter " + repr(self.__pname()) + " and return None")
        self._setActionString( ACTION_CANCEL , "Skip the acquisition of telemetry parameter " + repr(self.__pname()) + " and return None")

        if type(self.__parameter)==str:
            # Create the parameter item and store it
            self.__parameter = REGISTRY['TM'][self.__parameter]
        # otherwise it is already a TM item model    

        if self.getConfig(Wait)==True:
            if self.getConfig(ValueFormat)==ENG:
                self._write("Retrieving engineering value of " + repr(self.__pname()))
            else:
                self._write("Retrieving raw value of " + repr(self.__pname()))

        value = None
        # Refresh the object and return it
        REGISTRY['TM'].refresh(self.__parameter, self.getConfig() )
        
        if self.__extended == True:
            value = self.__parameter
            self._notifyValue( self.__pname(), "<OBJ>", NOTIF_STATUS_OK, "TM item obtained")
        
        else: # Normal behavior
            # Get the value in the desired format from the TM interface
            value = self.__parameter.value(self.getConfig())
            
            if self.getConfig(Wait)==True:
                self._write("Last updated value of " + repr(self.__pname()) + ": " + str(value))
            else:
                self._write("Last recorded value of " + repr(self.__pname()) + ": " + str(value))

        return [False, value,None,None]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( self.__pname(), "???", NOTIF_STATUS_PR, " ")
        self._write("Retry get parameter " + self.__pname(), {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( self.__pname(), "???", NOTIF_STATUS_SP, " ")
        self._write("Skip get parameter " + self.__pname(), {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._notifyValue( self.__pname(), "???", NOTIF_STATUS_CL, " ")
        self._write("Cancel get parameter " + self.__pname(), {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def __pname(self):
        if type(self.__parameter)==str:
            return self.__parameter
        return self.__parameter.fullName() 
                    
################################################################################
class SetGroundParameter_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the SetGroundParameter wrapper function.
    """    
    __toInject = None
    __value = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Telemetry injection"
        self.__toInject = None
        self.__value = None
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)==0:
            raise SyntaxException("No parameters given")

        # Since args is a tuple we have to convert it to a list         
        if len(args)!=1:
            # The case of giving a simple inject definition
            self.__toInject = args[0]
            self.__value = args[1]
            # Modifiers will go in useConfig 
        else:
            # Givin an inject list
            self.__toInject = args[0]
        
        # Store information for possible failures
        self.setFailureInfo("TM", self.__toInject)
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the injection of ground parameter " + repr(self.__toInject) + " and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the injection of ground parameter " + repr(self.__toInject) + " and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the injection of the ground parameter " + repr(self.__toInject))

        if type(self.__toInject)==list:
            result = REGISTRY['TM'].inject( self.__toInject, self.getConfig() )
            if result == True:
                self._write("Injected values: ")
                for item in self.__toInject:
                    self._write("  - " + str(item[0]) + " = " + str(item[1]))
            else:
                self._write("Failed to inject values", {Severity:ERROR})
        else:
            result = REGISTRY['TM'].inject( self.__toInject, self.__value, self.getConfig() )
            if result == True:
                self._write("Injected value: " + str(self.__toInject) + " = " + str(self.__value))
            else:
                self._write("Failed to inject value", {Severity:ERROR})

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry inject ground parameter " + repr(self.__toInject), {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip inject ground parameter " + repr(self.__toInject), {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Skip inject ground parameter " + repr(self.__toInject), {Severity:WARNING} )
        return [False, False]

################################################################################
class Verify_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the Verify wrapper function.
    """    
    __retryAll = False
    __retry = False
    __useConfig = {}
    __vrfDefinition = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__retryAll = False
        self.__retry = False
        self.__useConfig = {}
        self.__vrfDefinition = None
        self._opName = "Verification" 
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)==0:
            raise SyntaxException("No arguments given")
        self.__useConfig = {}
        self.__useConfig.update(self.getConfig())
        self.__useConfig[Retry] = self.__retry

        # Since args is a tuple we have to convert it to a list for TM.verify        
        if len(args)!=1:
            # The case of giving a simple step for verification
            self.__vrfDefinition = [ item for item in args ]
        else:
            # Givin a step or a step list
            self.__vrfDefinition = args[0]

        # Store information for possible failures
        self.setFailureInfo("TM", self.__vrfDefinition)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
      
        self._notifyOpStatus( NOTIF_STATUS_PR, "Verifying..." )
  
        self._setActionString( ACTION_SKIP   ,  "Skip the telemetry verification and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the telemetry verification and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the telemetry verification")
        self._setActionString( ACTION_RECHECK,  "Repeat the telemetry verification")

  
        # Wait some time before verifying if requested
        if self.__useConfig.has_key(Delay):
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before TM verification", {Severity:INFORMATION})
                WaitFor(delay)
        
        result = REGISTRY['TM'].verify( self.__vrfDefinition, self.__useConfig )

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
    def _doSkip(self):
        if self.getConfig(PromptUser)==True:
            self._write("Verification skipped", {Severity:WARNING} )
        return [False,True]

    #===========================================================================
    def _doCancel(self):
        if self.getConfig(PromptUser)==True:
            self._write("Verification skipped", {Severity:WARNING} )
        return [False,False]
                
    #===========================================================================
    def _doRecheck(self):
        self._write("Retry verification", {Severity:WARNING} )
        return [True,False]

################################################################################
class SetLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the SetTMparam wrapper function.
    """    
    __parameter = None
    __limits = {}
     
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__limits = {}
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No parameters given")

        self.__parameter = args[0]
        
        self.__limits = {}
        if self.hasConfig(Limits):
            llist = self.getConfig(Limits)
            if type(llist)==list:
                if len(llist)==2:
                    self.__limits[LoRed] = llist[0]
                    self.__limits[LoYel] = llist[0]
                    self.__limits[HiRed] = llist[1]
                    self.__limits[HiYel] = llist[1]
                elif len(llist)==4:
                    self.__limits[LoRed] = llist[0]
                    self.__limits[LoYel] = llist[1]
                    self.__limits[HiRed] = llist[2]
                    self.__limits[HiYel] = llist[3]
                else:
                    raise SyntaxException("Malformed limit definition")
            elif type(llist)==dict:
                self.__limits = llist       
            else:
                raise SyntaxException("Expected list or dictionary")
        else:
            if self.hasConfig(LoRed): self.__limits[LoRed] = self.getConfig(LoRed)
            if self.hasConfig(LoYel): self.__limits[LoYel] = self.getConfig(LoYel)
            if self.hasConfig(HiRed): self.__limits[HiRed] = self.getConfig(HiRed)
            if self.hasConfig(HiYel): self.__limits[HiYel] = self.getConfig(HiYel)

            if self.hasConfig(LoBoth):
                self.__limits[LoYel] = self.getConfig(LoBoth)
                self.__limits[LoRed] = self.getConfig(LoBoth)
            if self.hasConfig(HiBoth):
                self.__limits[HiYel] = self.getConfig(HiBoth)
                self.__limits[HiRed] = self.getConfig(HiBoth)
            if self.hasConfig(Expected):
                self.__limits[Expected] = self.getConfig(Expected)
            if self.hasConfig(Nominal):
                self.__limits[Nominal] = self.getConfig(Nominal)
            if self.hasConfig(Warning):
                self.__limits[Warning] = self.getConfig(Warning)
            if self.hasConfig(Error):
                self.__limits[Error] = self.getConfig(Error)
            if self.hasConfig(Ignore):
                self.__limits[Ignore] = self.getConfig(Ignore)
        
        if len(self.__limits)==0:
            raise SyntaxException("No limits given")
    
            # Store information for possible failures
        self.setFailureInfo("TM", self.__limits)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the limits modification and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the limits modification and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the limits modification")

        result = REGISTRY['TM'].setLimits( self.__parameter, self.__limits, config = self.getConfig() )
        
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry limits modification", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip limits modification", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Skip limits modification", {Severity:WARNING} )
        return [False, False]

################################################################################
class RestoreNormalLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the RestoreNormalLimits wrapper function.
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Reset limits" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
    
        # Store information for possible failures
        self.setFailureInfo("TM", None)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the limits reset and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the limits reset and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the limits reset")

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        result = REGISTRY['TM'].restoreNormalLimits()
        
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry limits reset", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip limits reset", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Skip limits reset", {Severity:WARNING} )
        return [False, False]

################################################################################
class GetLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the GetTMparam wrapper function.
    """    
    __parameter = None
    __property = None
     
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__property = None
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No parameters given")

        self.__parameter = args[0]
        if (len(args) == 2):
            self.__property = args[1]
        else:
            self.__property = "ALL"
    
            # Store information for possible failures
        self.setFailureInfo("TM", self.__parameter)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the limits acquisition and return None")
        self._setActionString( ACTION_CANCEL ,  "Skip the limits acquisition and return None")
        self._setActionString( ACTION_REPEAT ,  "Repeat the limits acquisition")

        result = None
        limits = REGISTRY['TM'].getLimits( self.__parameter, config = self.getConfig() )
        
        if self.__property == "ALL":
            result = limits
        elif self.__property == LoRed:
            result = limits[0]
        elif self.__property == LoYel:
            result = limits[1]
        elif self.__property == HiYel:
            result = limits[2]
        elif self.__property == HiRed:
            result = limits[3]
        else:
            raise DriverException("Cannot get property", "Unknown property name: " + repr(self.__property))
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get property", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip limits acquisition", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._write("Skip limits acquisition", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, None]

################################################################################
class LoadLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the GetTMparam wrapper function.
    """    
    __limitsFile = None
    __retry = False
    __prefix = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__limitsFile = None
        self.__retry = False
        self.__prefix = None
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No limits file URL given")
        
        self.__limitsFile = args[0]
        
                # Store information for possible failures
        self.setFailureInfo("TM", self.__limitsFile)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the limits load operation and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the limits load operation and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the limits load operation")

        result = None
        
        if not self.__retry:
            # Get the database name
            self.__limitsFile = args[0]
            
            if type(self.__limitsFile)!=str:
                raise SyntaxException("Expected a limits file URL")
            if not "://" in self.__limitsFile:
                raise SyntaxException("Limits file name must have URI format")
            idx = self.__limitsFile.find("://")
            self.__prefix = self.__limitsFile[0:idx]
        else:
            self.__retry = False        

        idx = self.__limitsFile.find("//")
        toShow = self.__limitsFile[idx+2:]
        self._notifyValue( "Limits File", repr(toShow), NOTIF_STATUS_PR, "Loading")
        self._write("Loading limits file " + repr(toShow))
        
        result = REGISTRY['TM'].loadLimits( self.__limitsFile, config = self.getConfig() )
        
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Load limits file failed, getting new name", {Severity:WARNING} )
        idx = self.__limitsFile.find("//")
        toShow = self.__limitsFile[idx+2:]
        newName = str(self._prompt("Enter new limits file name (previously " + repr(toShow) + "): ", [], {} ))
        if not newName.startswith(self.__prefix):
            newName =  self.__prefix + "://" + newName
        self.__limitsFile = newName
        self.__retry = True
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip load limits file", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Skip load limits file", {Severity:WARNING} )
        return [False, False]


################################################################################
class GetTMparam_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the GetTMparam wrapper function.
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__property = None
        self._opName = "" 

        #TODO Add all props and clarify which are allowed
        self.__properties = [None, LoRed, LoYel, HiRed, HiYel,
                             LoBoth, HiBoth, "Applicable", "All",
                             "Description", "Units", "Calibration",
                             "LIMIT_EN", "LIMIT_EVENT"]

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args) == 0:
            raise SyntaxException("No parameters given")

        self.__parameter = args[0]
        self.__property = args[1] if (len(args) == 2) else None

        if self.__property not in self.__properties:
            raise SyntaxException("No valid property")
    
            # Store information for possible failures
        self.setFailureInfo("TM", self.__parameter)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        self._setActionString(ACTION_SKIP, "Skip the parameter property acquisition and return None")
        self._setActionString(ACTION_CANCEL, "Skip the parameter property acquisition and return None")
        self._setActionString(ACTION_REPEAT, "Repeat the parameter property acquisition")

        result = REGISTRY['TM'].getTMparam(self.__parameter,
                                           self.__property,
                                           config=self.getConfig())
        return [False, result, NOTIF_STATUS_OK, ""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get property", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip get property", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel get property", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, None]

################################################################################
class SetTMparam_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the SetTMparam wrapper function.
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__limits = None
        self.__properties = {}
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No parameters given")

        self.__parameter = args[0]
        
        self.__limits = {}
        if self.hasConfig(Limits):
            llist = self.getConfig(Limits)
            if type(llist)==list:
                if len(llist)==2:
                    self.__limits[LoRed] = llist[0]
                    self.__limits[LoYel] = llist[0]
                    self.__limits[HiRed] = llist[1]
                    self.__limits[HiYel] = llist[1]
                elif len(llist)==4:
                    self.__limits[LoRed] = llist[0]
                    self.__limits[LoYel] = llist[1]
                    self.__limits[HiRed] = llist[2]
                    self.__limits[HiYel] = llist[3]
                else:
                    raise SyntaxException("Malformed limit definition")
            elif type(llist)==dict:
                self.__limits = llist       
            else:
                raise SyntaxException("Expected list or dictionary")
        else:
            if self.hasConfig(LoRed): self.__limits[LoRed] = self.getConfig(LoRed)
            if self.hasConfig(LoYel): self.__limits[LoYel] = self.getConfig(LoYel)
            if self.hasConfig(HiRed): self.__limits[HiRed] = self.getConfig(HiRed)
            if self.hasConfig(HiYel): self.__limits[HiYel] = self.getConfig(HiYel)

            if self.hasConfig(LoBoth):
                self.__limits[LoYel] = self.getConfig(LoBoth)
                self.__limits[LoRed] = self.getConfig(LoBoth)
            if self.hasConfig(HiBoth):
                self.__limits[HiYel] = self.getConfig(HiBoth)
                self.__limits[HiRed] = self.getConfig(HiBoth)

        for prop in ["LIMIT_EN", "LIMIT_EVENT"]:
            if self.hasConfig(prop):
                self.__properties[prop] = self.getConfig(prop)

        if len(self.__limits) > 0:
            self.__properties[Limits] = self.__limits
    
            # Store information for possible failures
        self.setFailureInfo("TM", self.__parameter)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the parameter property setting and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the parameter property setting and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the parameter property setting")

        result = REGISTRY['TM'].setTMparam(self.__parameter,
                                           self.__properties,
                                           config=self.getConfig())
        
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry set property", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip set property", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel set property", {Severity:WARNING} )
        return [False, False]

