###################################################################################
## MODULE     : spell.lib.adapter.verifier
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telemetry verifier thread
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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
from spell.lib.adapter.tm_item import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.core import COMP_SYMBOLS
from spell.lib.registry import REGISTRY

#*******************************************************************************
# System imports
#*******************************************************************************
import threading,datetime,time,sys

################################################################################
class TmVerifierClass(threading.Thread):

    __tmClass = None
    __tmItem = None
    __definition = []
    
    step = None
    name = None
    value = None
    status = None
    failed = False
    stopped = False
    reason = " "
    updtime = " "
    error = None
    
    __inExpression = False
    __paramName  = None
    __comparison = None
    __fromValue  = None
    __toValue    = None
    __stepConfig = {}
    
    #===========================================================================
    def __init__(self, stepNum, parameters, globalConfig = {}, inExpression = False ):
        
        threading.Thread.__init__(self)
        
        self.__tmClass = REGISTRY['TM']
        
        # Initialize handles
        self.setName("VRF STEP " + str(stepNum))
        self.step = stepNum
        self.__definition = [ stepNum, parameters, globalConfig ]
        self.__inExpression = inExpression
        self.stopped = False
        
        # Status information accessed from tm interface
        self.name = None
        self.value = None
        self.status = None
        self.failed = False
        self.error = None
        self.updtime = " "

        if len(parameters)<3:
            raise SyntaxException("Malformed verification step")

        # Comparison operator
        self.__comparison = parameters[1]
        # Check it
        if type(self.__comparison)!=str:
            raise SyntaxException("Bad parameter, expected comparison operator")

        # Check wether first element is a param name or a tm item 
        if isinstance(parameters[0],TmItemClass):
            self.__tmItem = parameters[0]
            self.__paramName = parameters[0].name() 
        elif type(parameters[0]) == str:
            self.__paramName = parameters[0]  
            self.__tmItem = self.__tmClass[self.__paramName]
        else:
            raise SyntaxException("Bad arguments") 
    
        # Guess the structure of the verification step
        hasConfig = (type(parameters[-1]) == dict)
        
        if hasConfig:
            twoValues = (len(parameters)==5)
        else:
            twoValues = (len(parameters)==4)

        self.__fromValue = parameters[2]
        if twoValues:
            self.__toValue = parameters[3]
        else:
            self.__toValue = None        
    
        # Build the configuration dictionary
        self.__stepConfig = {}
        self.__stepConfig.update(globalConfig)
        self.__stepConfig["STEP_ID"] = self.step

        # Get step specific configuration
        if hasConfig:
            self.__stepConfig.update(parameters[-1])
            
        self.__updateInfo("UNINIT", False, "", False)

    #===========================================================================
    def __updateInfo(self, status, failed, reason = " ", notify = True):

        # Configuration to be used in this step        
        useConfig = self.__stepConfig.copy()
        # We do not want to wait for notifications
        useConfig[Wait] = False
        useConfig[Notify] = False
        
        # The name
        self.name = repr(self.step) + "@" + self.__paramName
        self.updtime = str(datetime.datetime.now())[:-3]
        
        # Build the value
        if isinstance(self.__fromValue,TmItemClass):
            if useConfig.get(ValueFormat) == ENG:
                currentValue = self.__fromValue._getEng()
            else:
                currentValue = self.__fromValue._getRaw() 
            fromValue = str(currentValue)
        else:
            fromValue = str(self.__fromValue)
                         
        if self.__toValue is not None:
            if isinstance(self.__toValue,TmItemClass):
                if useConfig.get(ValueFormat) == ENG:
                    currentToValue = self.__toValue._getEng()
                else:
                    currentToValue = self.__toValue._getRaw() 
                self.value = COMP_SYMBOLS[self.__comparison] +\
                             "[" + fromValue + ", " + str(currentToValue) + "]"
            else:
                self.value = COMP_SYMBOLS[self.__comparison] +\
                             "[" + fromValue + "," + str(self.__toValue) + "]"
        else:
            self.value = COMP_SYMBOLS[self.__comparison] + fromValue
                 
        self.status = status
        self.failed = failed
        self.reason = reason
        if notify:
            self.__tmClass.updateVerificationStatus( self )
        
    #===========================================================================
    def run(self):

        # Update verification status info 
        self.__updateInfo("IN PROGRESS", False)
        self.__shouldStop = False
        
        result = False
        reason = " "
        self.error = None
        
        LOG("[V] Starting verification with config: " + repr(self.__stepConfig))
        
        try:
            if self.__comparison == eq:
                result = self.__tmClass.eq( self.__tmItem, self.__fromValue, self.__stepConfig )
            elif self.__comparison == neq:
                result = self.__tmClass.neq( self.__tmItem, self.__fromValue, self.__stepConfig )
            elif self.__comparison == lt:
                result = self.__tmClass.lt( self.__tmItem, self.__fromValue, self.__stepConfig )
            elif self.__comparison == gt:
                result = self.__tmClass.gt( self.__tmItem, self.__fromValue, self.__stepConfig )
            elif self.__comparison == le:
                result = self.__tmClass.le( self.__tmItem, self.__fromValue, self.__stepConfig )
            elif self.__comparison == ge:
                result = self.__tmClass.ge( self.__tmItem, self.__fromValue, self.__stepConfig )
            elif self.__comparison == bw:
                result = self.__tmClass.between( self.__tmItem, self.__fromValue, self.__toValue, self.__stepConfig )
            elif self.__comparison == nbw:
                result = self.__tmClass.not_between( self.__tmItem, self.__fromValue, self.__toValue, self.__stepConfig )
                
            actualValue = self.getActualValue()
                
            LOG("[V] Comparison result: " + repr(result))
            if not result:
                if self.__inExpression:
                    reason = "[EXPRESSION] Actual value: " + str(actualValue)
                else:
                    reason = "Actual value: " + str(actualValue)
            else:
                if self.__inExpression:
                    reason = "[EXPRESSION] Value is " + str(actualValue)
                else:
                    reason = "Value is " + str(actualValue)
            
        except DriverException,ex:
            LOG("[V] Verification process failed: " + str(ex), LOG_ERROR)
            self.error = ex
            reason = ex.message
            if (ex.reason != "unknown"):
                reason += ". " + ex.reason
            result = False
            
        finally:
            if not self.stopped:
                LOG("[V] Declaring verification process success: " + repr(result))
                if result:
                    self.__updateInfo("SUCCESS", False, reason)
                else:
                    # If PromptUser is false, do not mark it as failed
                    if self.__stepConfig[PromptUser] == True:
                        self.__updateInfo("FAILED", True, reason)
                    # If PromptUser is false but the failed check is caused by system failure, report it
                    # unless PromptFailure is False
                    else:
                        if (self.error is None) or (self.__stepConfig[PromptFailure] == False):
                            self.__updateInfo("SUPERSEDED", True, reason)
                        else:
                            self.__updateInfo("FAILED", True, reason)

    #===========================================================================
    def stopVerification(self):
        self.stopped = True

    #===========================================================================
    def getDefinition(self):
        return self.__definition

    #===========================================================================
    def getParamName(self):
        desc = self.__tmItem.description()
        if desc != "": desc = ": " + desc
        return self.__tmItem.name() + desc

    #===========================================================================
    def getActualValue(self):
        useConfig = self.__stepConfig.copy()
        useConfig[Wait] = False
        useConfig[Notify] = False
        actualValue = self.__tmItem.value( useConfig )
        return actualValue
