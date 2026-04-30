###################################################################################
## MODULE     : spell.lang.helpers.limhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for limit management
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

from .basehelper import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *
from spell.lib.exception import SyntaxException
from spell.lang.functions import *
from spell.lib.registry import *
from spell.lib.adapter.tm_item import TmItemClass

################################################################################
class AdjustLimits_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the AdjustLimits wrapper.
    """
    __verifyList = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Limit adjustment"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)==0:
            raise SyntaxException("Expected a TM verification list")
        self.__verifyList = args[0]
        if type(self.__verifyList)!=list:
            raise SyntaxException("Expected a TM verification list")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        from spell.lang.functions import SetLimits

        result = False
        config = self.getConfig()

        for condition in self.__verifyList:
            paramName = condition[0]
            paramValue = condition[2]
            operator = condition[1]
            if operator != eq: continue

            if type(paramValue)==str: # Status parameters
                # TODO: need to temporarily remove Expected here for BC - it does not work if both modifiers are used
                limits = {Nominal:paramValue}
                config.update(limits)
                result = SetLimits( paramName, config )
            else:
                tolerance = self.getConfig(Tolerance)
                limits = {}
                limits[LoRed] = paramValue - tolerance
                limits[LoYel] = paramValue - tolerance
                limits[HiYel] = paramValue + tolerance
                limits[HiRed] = paramValue + tolerance

                config.update(limits)

                if Select not in config:
                    config[Select] = ACTIVE

                result = SetLimits( paramName, config )

        return [False,result,None,None]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry adjust limits", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip limit adjustment", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel limit adjustment", {Severity:WARNING} )
        return [False, False]

################################################################################
class GetDefaultLimitFilesLoaded_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the GetDefaultLimitFilesLoaded wrapper.
    """

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Get Default Limit Files Loaded"
        self.setConfig({OnFailure:SKIP|REPEAT|CANCEL|ABORT})

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=0:
            raise SyntaxException("No positional argument expected")

        # Store information for possible failures
        self.setFailureInfo("TM")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_REPEAT , "Acquire Default Limit Files Loaded again")
        self._setActionString( ACTION_SKIP   , "Skip the acquisition of Default Limit Files Loaded and return None")
        self._setActionString( ACTION_CANCEL , "Cancel the acquisition of Default Limit Files Loaded and return None")

        timeout = self.getConfig(Timeout)
        if timeout is None:
            timeout = 3.0

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        filelist = REGISTRY['TM'].getDefaultLimitFilesLoaded( timeout )

        return [False,filelist,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( "GetDefaultLimitFilesLoaded", "???", NOTIF_STATUS_PR, " ")
        self._write("Retry get default limit files loaded ", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "GetDefaultLimitFilesLoaded ", "???", NOTIF_STATUS_SP, " ")
        self._write("Skip get default limit files loaded ", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._notifyValue( "GetDefaultLimitFilesLoaded", "???", NOTIF_STATUS_CL, " ")
        self._write("Cancel get Default Limit Files Loaded", {Severity:WARNING} )
        return [False, None]

################################################################################
class GetCurrentOverlay_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the GetCurrentOverlay wrapper.
    """

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Get Current Overlay"
        self.setConfig({OnFailure:SKIP|REPEAT|CANCEL|ABORT})

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=0:
            raise SyntaxException("No positional argument expected")

        # Store information for possible failures
        self.setFailureInfo("TM")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_REPEAT , "Acquire current overlay again")
        self._setActionString( ACTION_SKIP   , "Skip the acquisition of current overlay and return None")
        self._setActionString( ACTION_CANCEL , "Cancel the acquisition of current overlay and return None")

        timeout = self.getConfig(Timeout)
        if timeout is None:
            timeout = 3.0

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        overlay = REGISTRY['TM'].getCurrentOverlay( timeout )

        return [False,overlay,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( "GetCurrentOverlay", "???", NOTIF_STATUS_PR, " ")
        self._write("Retry get current overlay", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "GetCurrentOverlay", "???", NOTIF_STATUS_SP, " ")
        self._write("Skip get current overlay ", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._notifyValue( "GetCurrentOverlay", "???", NOTIF_STATUS_CL, " ")
        self._write("Cancel get current overlay", {Severity:WARNING} )
        return [False, None]

################################################################################
class GetSingleChanneled_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the GetSingleChanneled wrapper.
    """

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Get Single Channeled Limits"
        self.setConfig({OnFailure:SKIP|REPEAT|CANCEL|ABORT})

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=0:
            raise SyntaxException("No positional argument expected")

        # Store information for possible failures
        self.setFailureInfo("TM")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_REPEAT , "Acquire single channeled limits again")
        self._setActionString( ACTION_SKIP   , "Skip the acquisition of single channeled limits and return None")
        self._setActionString( ACTION_CANCEL , "Cancel the acquisition of single channeled limits and return None")

        timeout = self.getConfig(Timeout)
        if timeout is None:
            timeout = 3.0

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        result = REGISTRY['TM'].getSingleChanneled( timeout )

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( "GetSingleChanneled", "???", NOTIF_STATUS_PR, " ")
        self._write("Retry get single channeled limits", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "GetSingleChanneled", "???", NOTIF_STATUS_SP, " ")
        self._write("Skip get single channeled limits ", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._notifyValue( "GetSingleChanneled", "???", NOTIF_STATUS_CL, " ")
        self._write("Cancel get single channeled limits overlay", {Severity:WARNING} )
        return [False, None]

################################################################################
class GetLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the GetLimits wrapper function.
    """
    __parameter = None
    __property = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Get Limits Definition"
        self.__parameter = None
        self.__property = None
        self.setConfig({OnFailure:SKIP|REPEAT|CANCEL|ABORT})

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        from spell.lib.adapter.tm_item import TmItemClass
        if len(args)==0:
            raise SyntaxException("No parameter name given")

        # Check correctness
        param = args[0]
        if type(param) != str and not isinstance(param,TmItemClass):
            raise SyntaxException("Expected a TM item or name")

        self.__parameter = param

        # Store information for possible failures
        self.setFailureInfo("TM", self.__parameter)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the limits acquisition and return None")
        self._setActionString( ACTION_CANCEL ,  "Cancel the limits acquisition and return None")
        self._setActionString( ACTION_REPEAT ,  "Repeat the limits acquisition")

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        result = None
        limits = REGISTRY['TM'].getLimits( self.__parameter, config = self.getConfig() )
        result = limits

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry limits acquisition", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip limits acquisition", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel limits acquisition", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, None]


################################################################################
class IsAlarmed_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the IsAlarmed wrapper function.
    """
    __parameter = None
    __property = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Get if TM parameter is alarmed"
        self.__parameter = None
        self.__property = None

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        from spell.lib.adapter.tm_item import TmItemClass
        if len(args)==0:
            raise SyntaxException("No parameter name given")

        # Check correctness
        param = args[0]
        if type(param) != str and not isinstance(param,TmItemClass):
            raise SyntaxException("Expected a TM item or name")

        self.__parameter = param

        # Store information for possible failures
        self.setFailureInfo("TM", self.__parameter)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the limit alarm acquisition and return (False)")
        self._setActionString( ACTION_CANCEL ,  "Cancel the limit alarm acquisition and return (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the limit alarm acquisition")

        result = False
        alarmed = REGISTRY['TM'].isAlarmed( self.__parameter, config = self.getConfig() )
        result = alarmed

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry limit alarm acquisition", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip limit alarm acquisition", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, False]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel limit alarm acquisition", {Severity:WARNING} )
        self._write("CAUTION: the procedure logic may become invalid!", {Severity:WARNING} )
        return [False, False]

################################################################################
class ActivateOverlay_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the ActivateOverlay wrapper.
    """
    __parameter = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Activate overlay limit"
        self.setConfig({OnFailure:SKIP|REPEAT|CANCEL|ABORT})

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=1:
            raise SyntaxException("Expecting one positional argument: overlay limit file name")
        self.__parameter = args[0]

        # Store information for possible failures
        self.setFailureInfo("TM")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   , "Skip the activation of overlay limit and return None")
        self._setActionString( ACTION_CANCEL , "Cancel the activation of overlay limit and return None")
        self._setActionString( ACTION_REPEAT , "Activate overlay limit again")

        timeout = self.getConfig(Timeout)
        if timeout is None:
            timeout = 5.0

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        result = REGISTRY['TM'].activateOverlay( self.__parameter, timeout )

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( "ActivateOverlay", "???", NOTIF_STATUS_PR, " ")
        self._write("Retry activating overlay limit", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "ActivateOverlay", "???", NOTIF_STATUS_SP, " ")
        self._write("Skip activating overlay limit", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._notifyValue( "ActivateOverlay", "???", NOTIF_STATUS_CL, " ")
        self._write("Cancel activating overlay limit", {Severity:WARNING} )
        return [False, None]

################################################################################
class ApplySingleChanneled_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the ApplySingleChanneled wrapper.
    """
    __parameter = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Apply Single Channeled Limits"
        self.setConfig({OnFailure:SKIP|REPEAT|CANCEL|ABORT})

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=1:
            raise SyntaxException("Expecting one positional argument: dict of singls channeled limits")
        self.__parameter = args[0]

        # Store information for possible failures
        self.setFailureInfo("TM")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_REPEAT , "Applying single channeled limits again")
        self._setActionString( ACTION_SKIP   , "Skip the application of single channeled limits and return None")
        self._setActionString( ACTION_CANCEL , "Cancel the application of single channeled limits and return None")

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        result = REGISTRY['TM'].applySingleChanneled( self.__parameter )

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( "ApplySingleChanneled", "???", NOTIF_STATUS_PR, " ")
        self._write("Retry applying single channeled limits", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "ApplySingleChanneled", "???", NOTIF_STATUS_SP, " ")
        self._write("Skip applying single channeled limits ", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._notifyValue( "ApplySingleChanneled", "???", NOTIF_STATUS_CL, " ")
        self._write("Cancel applying single channeled limits overlay", {Severity:WARNING} )
        return [False, None]

################################################################################
class SetLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the SetTMparam wrapper function.
    """
    __parameter = None
    __result = {}

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__result = {}
        self._opName = ""
        self.setConfig({OnFailure:SKIP|REPEAT|CANCEL|ABORT})

    def convertStatus(self, definition):
        if Nominal in definition:
            definition[Nominal] = repr(definition[Nominal])[1:-1].replace("'","")
        if Warning in definition:
            definition[Warning] = repr(definition[Warning])[1:-1].replace("'","")
        if Error in definition:
            definition[Error] = repr(definition[Error])[1:-1].replace("'","")
        if Ignore in definition:
            definition[Ignore] = repr(definition[Ignore])[1:-1].replace("'","")

    # Transform Midpoint/Tolerance values, in case they are present, to LoHiYelRed.
    def translateMidpoint(self, definition):
        if ( Midpoint in definition and Tolerance in definition ):
            midpoint = definition.get(Midpoint)
            tolerance = definition.get(Tolerance)

            self._write("Midpoint: " + repr(midpoint) + " Tolerance: " + repr(tolerance), {Severity:INFORMATION} )
            self._write("Midpoint: " + repr(type(midpoint)) + " Tolerance: " + repr(type(tolerance)), {Severity:INFORMATION} )

            if not ( type(midpoint) in [int, int, float] and type(tolerance) in [int, int, float] ):
                raise SyntaxException("Midpoint and Tolerance values should be numeric. Midpoint: " + repr(midpoint) + "Tolerance: " + repr(tolerance) )

            definition[LoRed] = midpoint - tolerance
            definition[LoYel] = midpoint - tolerance
            definition[HiYel] = midpoint + tolerance
            definition[HiRed] = midpoint + tolerance

            del definition[Midpoint]
            del definition[Tolerance]

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        ## TODO David
        ## Review controls
        ## Adjust SetLimits when multiple limits provided
        ## Adjust midpoint tolerance transformation (for both limits and definition)
        ## Code refactor. Add funcitons, move to tmInterface from tmModule.

        bParameters = False
        bDefinition = False
        bLimits = False
        bBackwardDef = False
        bBackwardExp = False

        bDelta = False
        bStatus = False
        bAnalog = False

        #limitType = "UNKNOWN";

        definition = {}
        limits = {}

        self.__result = {}

        #Check positional arguments
        if len(args)==0:
            raise SyntaxException("No parameters given")
        # 2 because of backward compatibility
        elif len(args)>1:
            raise SyntaxException("This function only accepts the parameter name as a positional argument")

        # Check correctness
        param = args[0]
        if type(param) != str and not isinstance(param,TmItemClass):
            raise SyntaxException("Expected a TM item or name")

        self.__parameter = param

        #Get param limit definitions
        if self.hasConfig(Limits):
            limitValues = self.getConfig(Limits)

            #Current specification.
            #All the limit definitions for a parameter
            if type(limitValues)==dict:
                bLimits = True
                limits = limitValues

                if self.hasConfig(Select):
                    raise SyntaxException("No Select modifier could be provided when a dictionary of limit definitions has been given. Select='"+self.getConfig(Select)+"' Provided")

            #Backward compatibility
            elif type(limitValues)==list:
                bBackwardDef = True
                if len(limitValues)==2:
                    definition[LoRed] = limitValues[0]
                    definition[LoYel] = limitValues[0]
                    definition[HiRed] = limitValues[1]
                    definition[HiYel] = limitValues[1]
                elif len(limitValues)==4:
                    definition[LoRed] = limitValues[0]
                    definition[LoYel] = limitValues[1]
                    definition[HiRed] = limitValues[2]
                    definition[HiYel] = limitValues[3]
                else:
                    raise SyntaxException("Malformed limit list definition. Two or Four list elements should be provided. Deprecated. Use a dictionary or parameters instead.")
            else:
                raise SyntaxException("Dictionary with the limit definitions expected.")

        # get unique limit definition
        if self.hasConfig(Definition):
            defCheck = self.getConfig(Definition)
            if type(defCheck)!=dict:
                raise SyntaxException("Malformed limit definition. A dictionary is expected.")
            bDefinition = True
            definition = self.getConfig(Definition)

            self._write("(LIMHELPER_preOp) Provided DEFINITION: " + repr(definition), {Severity:INFORMATION} )

        #Get isolated limit parameters
        if self.hasConfig(LoRed):
            definition[LoRed] = self.getConfig(LoRed)
            bParameters = True

        if self.hasConfig(LoYel):
            definition[LoYel] = self.getConfig(LoYel)
            bParameters = True

        if self.hasConfig(HiRed):
            definition[HiRed] = self.getConfig(HiRed)
            bParameters = True
        if self.hasConfig(HiYel):
            definition[HiYel] = self.getConfig(HiYel)
            bParameters = True

        #Status parameter limit
        if self.hasConfig(Nominal):
            definition[Nominal] = self.getConfig(Nominal)
            bParameters = True
        if self.hasConfig(Warning):
            definition[Warning] = self.getConfig(Warning)
            bParameters = True
        if self.hasConfig(Error):
            definition[Error] = self.getConfig(Error)
            bParameters = True
        if self.hasConfig(Ignore):
            definition[Ignore] = self.getConfig(Ignore)
            bParameters = True

        if self.hasConfig(Delta):
            definition[Delta] = self.getConfig(Delta)
            bParameters = True

        if self.hasConfig(Midpoint) and self.hasConfig(Tolerance):
            definition[Midpoint] = self.getConfig(Midpoint)
            definition[Tolerance] = self.getConfig(Tolerance)
            bParameters = True

        #Check if are there status and convert to strings
        if bParameters or bDefinition:
            self.convertStatus(definition)
            self.translateMidpoint(definition)

        if bParameters:
            self._write("(LIMHELPER_preOp) Provided PARAMETERS: " + repr(definition), {Severity:INFORMATION} )

        #TODO Maintain backward compatibility, keep Expected argument.
        #BACKWARD COMPATIBILITY
        if self.hasConfig(Expected):
            definition[Expected] = self.getConfig(Expected)
            bBackwardExp = True

        self._write("(LIMHELPER_preOp) Param: " + repr(self.__parameter), {Severity:INFORMATION} )
        #self._write("(LIMHELPER_preOp) Limits: " + repr(limits), {Severity:INFORMATION} )
        #self._write("(LIMHELPER_preOp) Definition: " + repr(definition), {Severity:INFORMATION} )

        #SYNTAX CONTROL

        # When no limit definitions has been provided.
        if not (bParameters or bDefinition or bLimits or bBackwardDef or bBackwardExp):
            raise SyntaxException("No limit definition has been provided.")

        # Evaluate provided values. Only one limit definition should be provided at a time.
        if not (bParameters ^ bDefinition ^ bLimits ^ bBackwardDef ^ bBackwardExp):
            raise SyntaxException("Multiple limit definition provided. Please provide only one.")

        #Identify Parameter type
        if bLimits:
            bAtLeastOneParam = False

            for defKey in list(limits.keys()):

                limitDef =  limits.get(defKey) ;
                self.translateMidpoint(limitDef)

                self._write("(LIMHELPER_preOp) Limit def: " + repr(limitDef), {Severity:INFORMATION} )

                bAnalog = (LoYel in limitDef
                        or LoRed in limitDef
                        or HiYel in limitDef
                        or HiRed in limitDef)
                bStatus =  (Nominal in limitDef
                           or Warning in limitDef
                           or Error in limitDef
                           or Ignore in limitDef)
                bDelta = (Delta in limitDef)

                if bStatus:
                    self.convertStatus(limitDef)

                ## Syntax error controls ##

                if not ( bAnalog or bStatus or bDelta ):
                    raise SyntaxException("No limit values provided.")

                if not ( bAnalog ^ bStatus ^ bDelta ):
                    raise SyntaxException("Incompatible limit values provided in the limit Definitions.")

                if ( bDelta
                    and not ( type(limitDef[Delta]) in [int, int, float]
                              or ( type(limitDef[Delta])==list
                                 and len(limitDef[Delta])==2
                                 and limitDef[Delta][0] in [int, int, float]
                                 and limitDef[Delta][1] in [int, int, float])
                              )
                    ):
                    raise SyntaxException("Wrong Delta modifier provided. Please introduce a number or a list with two numbers.")
                #if
            #for

            #Set limit definitions to result.
            self.__result[Limits]=limits

        #if limits

        elif (bDefinition or bParameters or bBackwardDef or bBackwardExp): # Will check limit definitions parameters
            if bDefinition and len(definition)==0:
                raise SyntaxException("Provided definition is empty.")

            if not ((LoYel in definition
                        or LoRed in definition
                        or HiYel in definition
                        or HiRed in definition)
                    or (Nominal in definition
                        or Warning in definition
                        or Error in definition
                        or Ignore in definition)
                    or (Delta in definition)
                    or (Midpoint in definition
                        and Tolerance in definition)
                    or (Expected in definition)
                    ):
                raise SyntaxException("No limit values provided.")

            if not ((LoYel in definition
                     or LoRed in definition
                     or HiYel in definition
                     or HiRed in definition)
                  ^ (Nominal in definition
                     or Warning in definition
                     or Error in definition
                     or Ignore in definition)
                  ^ (Delta in definition)
                  ^ (Midpoint in definition
                     and Tolerance in definition)
                  ^ (Expected in definition)
                ):
                raise SyntaxException("Wrong limit values provided.")

            if (Delta in definition
                and not ( type(definition[Delta]) in [int, int, float]
                          or ( type(definition[Delta])==list
                             and len(definition[Delta])==2
                             and limitDef[Delta][0] in [int, int, float]
                             and limitDef[Delta][1] in [int, int, float] )
                          )
                ):
                raise SyntaxException("Wrong Delta modifier provided. Please introduce a number or a list with two numbers.")

            self.delConfig(Limits) #Remove limits definition of backward compatible entries.

            self._write("(LIMHELPER_preOp) Definition2: " + repr(definition), {Severity:INFORMATION} )

            self.__result[Definition]= definition

            self._write("(LIMHELPER_preOp) Result: " + repr(self.__result), {Severity:INFORMATION} )

        # Store information for possible failures
        self.setFailureInfo("TM", self.__result)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip the limits modification and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the limits modification and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat the limits modification")

        self._write("(LIMHELPER_Op) Result: " + repr(self.__result), {Severity:INFORMATION} )

        config = self.getConfig()
        config.update(self.__result)

        # We don't allow resend or recheck, only repeat, abort, skip, cancel
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RESEND))
        self.addConfig(OnFailure,self.getConfig(OnFailure) & (~RECHECK))

        result = REGISTRY['TM'].setLimits( self.__parameter, config )

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
