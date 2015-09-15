###################################################################################
## MODULE     : spell.lib.adapter.expression
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Expressions for TM conditions
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

from spell.lib.exception import SyntaxException
from spell.lib.adapter.verifier import TmVerifierClass 
from spell.lib.registry import REGISTRY
from spell.lang.constants import WARNING
from spell.lang.modifiers import PromptUser,Severity
from constants.core import COMP_SYMBOLS

import time

AND_TYPE = '@AND@'
OR_TYPE  = '@OR@'

################################################################################
class Expression():
    
    __expression = []
    
    def __init__(self, *args):
        
        if len(args)==0:
            raise SyntaxException("Expected at least one list as argument")
        self.__expression = args
        
    def getConditions(self):
        conditions = []  
        for item in self.__expression:
            if isinstance(item,Expression):
                conditions.append( item.getConditions() )
            else:
                conditions.append(item)
        return [self.getType()] + conditions

    def getType(self):
        return None
    
################################################################################
class ExpressionVerifier():
    
    __expression = []
    __verifiers = []
    __tmVerifiers = []
    
    #===========================================================================
    def __init__(self,expression):
        
        self.__expression = expression
        self.__verifiers = []
        self.__tmVerifiers = None
        
    #===========================================================================
    def prepare(self, useConfig, verifierList, verifierTable):
        # Get composed list of conditions
        conditions = self.__expression.getConditions()
        # Create a verifier per raw condition and store it in the corresponding place of the list
        self.__verifiers = self.createExpressionVerifiers(0, conditions[:], useConfig, verifierList, verifierTable)
        
        self.__tmVerifiers = verifierList
        
    #===========================================================================
    def clear(self):
        del self.__verifiers
        self.__tmVerifiers = None
        
    #===========================================================================
    def evaluate(self):
        
        # Start the verifiers now
        self.startExpressionVerifiers(self.__verifiers)
        
        # And wait for them
        self.waitExpressionVerifiers(self.__tmVerifiers)
        
        # Then evaluate the conditions
        resultList = self.extractExpressionVerifiersResult(self.__verifiers)
        
        overallResult = self.evaluateResults(resultList)
        errors = self.generateVerifierReport()
        return overallResult,errors

    #===========================================================================
    def evaluateResults(self, resultList):
        etype = resultList[0]
        if etype == AND_TYPE:
            return self.evaluateAND(resultList[1:])
        elif etype == OR_TYPE:
            return self.evaluateOR(resultList[1:])
                
    #===========================================================================
    def evaluateAND(self, resultList):
        for item in resultList:
            if type(item)==list:
                partialResult = self.evaluateResults(item)
                if not partialResult: return False
            elif item not in ['SUCCESS']:
                return False
        return True

    #===========================================================================
    def evaluateOR(self, resultList):
        for item in resultList:
            if type(item)==list:
                partialResult = self.evaluateResults(item)
                if partialResult: return True
            elif item in ['SUCCESS']:
                return True
        return False
    
    #===========================================================================
    def generateVerifierReport(self):
        # Show verification information
        verifMessage = ""
        for v in self.__tmVerifiers:
            defn = v.getDefinition()
            paramName = v.getParamName()
            message = "    %-2s" % str(defn[0]) + ": Parameter " + repr(paramName) + \
                      " " + COMP_SYMBOLS[defn[1][1]] + " " + str(defn[1][2])

            # Take into account ternary operators
            if len(defn[1]) >= 4 and type(defn[1][3])!=dict: # it is value of ternary op
                message = message + " and " + str(defn[1][3])
            
            # Configuration
            configText = ""
            if len(defn[1]) >=4 and type(defn[1][-1])==dict:
                configText = ", ";
                configDict = defn[1][-1]
                for key in configDict:
                    if key == PromptUser: continue
                    if len(configText)>0: 
                        configText += " "
                    configText += str(key) + " = " + str(configDict.get(key))

            if len(verifMessage)>0: verifMessage += "\n"
            verifMessage += message + configText
        REGISTRY['CIF'].write(verifMessage)
        
        # Will be true if verification was interrupted
        stopped = False
        
        errors = []
        for v in self.__tmVerifiers:
            defn = v.getDefinition()
            keyName = str(defn[0]) + ": " + defn[1][0]
            # This will be used later to decide if an exception shall be raised
            errors.append( [ keyName, v.reason, (v.error != None), v.failed, v.stopped ] )

            # Verifier step number
            stepNum = str(defn[0])
            
            # Decide how to report the failed verification step
            if v.stopped:
                stopped = True
                break

            elif v.failed:
                message = "Verification " + stepNum + " failed. "
                message += v.reason + "."
                REGISTRY['CIF'].write( message )
        
        if stopped:
            REGISTRY['CIF'].write( "Telemetry expression verification interrupted", {Severity:WARNING} )
        return errors
        
    #===========================================================================
    def waitExpressionVerifiers(self, verifiers):
        while True:
            time.sleep(0.2)
            someAlive = False
            for item in verifiers:
                if isinstance(item,TmVerifierClass) and item.isAlive():
                    someAlive = True
                    break
            if not someAlive:
                return
        
    #===========================================================================
    def startExpressionVerifiers(self, verifiers):
        for item in verifiers:
            if isinstance(item,TmVerifierClass):
                item.start()
            elif type(item) == list:
                self.startExpressionVerifiers(item)

    #===========================================================================
    def stopExpressionVerifiers(self, verifiers):
        for item in verifiers:
            if isinstance(item,TmVerifierClass):
                item.stopVerification()
            elif type(item) == list:
                self.stopExpressionVerifiers(item)
        
    #===========================================================================
    def createExpressionVerifiers(self, stepNum, conditions, globalConfig, verifierList, verifierTable):
        for item in conditions:
            if type(item)==list:
                if ((item[0] == AND_TYPE) or (item[0]) == OR_TYPE):
                    conditions[ conditions.index(item) ] = self.createExpressionVerifiers(stepNum, item, globalConfig, verifierList, verifierTable)
                else:
                    # Get the index before modifying the item, otherwise it will not be found
                    idx = conditions.index(item)
                    # We force PromptUser false in order to avoid FAILED status or prompts to the user
                    # we just want to evaluate here, regardless what the user passes
                    icopy = item[:]
                    if len(icopy) == 3:
                        icopy = icopy + [ {PromptUser:False} ]
                    elif len(icopy) > 3:
                        icopy[-1][PromptUser] = False 
                    v = TmVerifierClass(stepNum,icopy,globalConfig)
                    # Replace the condition by the TM verifier
                    conditions[ idx ] = v
                    # Add it to the TM verifier list and table for the TM interface to work with them
                    verifierList.append(v)
                    verifierTable.append([v.name,v.value,v.status,v.reason,v.updtime])
                    stepNum += 1
        return conditions

    #===========================================================================
    def extractExpressionVerifiersResult(self, verifiers):
        for item in verifiers:
            if isinstance(item,TmVerifierClass):
                verifiers[ verifiers.index(item) ] = item.status
            elif type(item) == list:
                verifiers[ verifiers.index(item) ] = self.extractExpressionVerifiersResult(item)
        return verifiers
    
