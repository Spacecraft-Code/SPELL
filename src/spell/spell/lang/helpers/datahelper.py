###################################################################################
## MODULE     : spell.lang.helpers.datahelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for data functions
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
import sys


NO_EXPECTED  = "__NONE__"


################################################################################
class SetSharedData_Helper(WrapperHelper):
    
    __name = None
    __value = None
    __scope = None
    __expected = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__name = None
        self.__value = None
        self.__scope = GLOBAL
        self.__expected = NO_EXPECTED
        self._opName = None 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        useConfig = {}
        useConfig.update(self.getConfig())

        if len(args)==1:
            # Ensure that the argument is a list
            if type(args[0])!= list:
                raise SyntaxException("Expected a list with at least name and value as elements")
            
            # If it is a list of lists:
            if type(args[0][1])==list:
                self.__name = []
                self.__value = []
                self.__expected = []
                for item in args[0]:
                    if type(item) != list:
                        raise SyntaxException("Malformed list item")
                    self.__name.append(item[0])
                    self.__value.append(item[1])
                    
                    # In this case the expected values for test-and-set 
                    # shall be given in the list items
                    if len(item)==3:
                        self.__expected.append(item[2])
                    else:
                        self.__expected.append(NO_EXPECTED)
            else:
                # Ensure that the list contains name and value
                if len(args[0])<2:
                    raise SyntaxException("Expected a list with at least name and value as elements")
                
                if len(args[0])==2:
                    self.__name = args[0][0]
                    self.__value = args[0][1]
                    # In this case the expected value shall be given as modifier
                    if Expected in useConfig.keys():
                        self.__expected = useConfig[Expected]
                        self.delConfig(Expected)
                    else:
                        self.__expected = NO_EXPECTED
                elif len(args[0])==3:
                    self.__name = args[0][0]
                    self.__value = args[0][1]
                    self.__expected = args[0][2]
                else:
                    raise SyntaxException("Provided list with too many elements")

        # Name and value provided as modifiers
        elif len(args)==0:
            if Name in useConfig and Value in useConfig:
                self.__name = useConfig[Name]
                self.__value = useConfig[Value]
                self.delConfig(Name)
                self.delConfig(Value)
            else:
                raise SyntaxException("Malformed arguments")
            
            # In this case the expected value shall be given as modifier
            if Expected in useConfig:
                self.__expected = useConfig[Expected]
                self.delConfig(Expected)
            else:
                self.__expected = NO_EXPECTED
            
        else:
            raise SyntaxException("Malformed arguments")
        
        # Process scope
        if Scope in useConfig.keys():
            self.__scope = useConfig[Scope]
            # Remove scope from configuration,as this modifier is used in notifications and prompts
            self.delConfig(Scope)
        else:
            self.__scope = GLOBAL

        # Check value types
        self._checkValidType(self.__value)
                
    #===========================================================================
    def _checkValidType(self, value ):
        
        tt = type(value)
        if tt in [str,int,float,bool]:
            return 
        elif tt == list:
            for element in value:
                self._checkValidType(element)
        elif tt == dict:
            for element in value.keys():
                self._checkValidType(value.get(element))
        else:
            raise SyntaxException("Type error: only Python primitive types can be used in blackboard")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Store information for possible failures
        self.setFailureInfo("DATA", self.__name)

        if type(self.__name)==list:
            self._setActionString( ACTION_REPEAT , "Try to set the shared variables again")
            self._setActionString( ACTION_SKIP   , "Skip setting the shared variables and return True")
            self._setActionString( ACTION_CANCEL , "Skip setting the shared variables and return False")
        else:
            self._setActionString( ACTION_REPEAT , "Try to set the shared variable " + repr(self.__name) + " again")
            self._setActionString( ACTION_SKIP   , "Skip setting the shared variable " + repr(self.__name) + " and return True")
            self._setActionString( ACTION_CANCEL , "Skip setting the shared variable " + repr(self.__name) + " and return False")

        result = None
        try:
            result = REGISTRY['CIF'].setSharedData( self.__name, self.__value, self.__expected, self.__scope )
            ntype = NOTIF_STATUS_OK
            if not result: ntype = NOTIF_STATUS_FL
            if type(self.__name) == list:
                self._notifyValue( repr(self.__name), "[...]", ntype, " ")
            else:
                self._notifyValue( repr(self.__name), repr(self.__value), ntype, " ")
        except DriverException,ex:
            self._notifyValue( repr(self.__name), "???", NOTIF_STATUS_FL, " ")
            raise ex

        return [False,result,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doRepeat(self):
        if type(self.__name)==list:
            self._write("Retry setting the shared variables", {Severity:WARNING} )
        else:
            self._write("Retry setting shared the variable " + repr(self.__name), {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        if type(self.__name)==list:
            self._write("Skip setting the shared variables", {Severity:WARNING} )
        else:
            self._write("Skip setting shared variable " + repr(self.__name), {Severity:WARNING} )
        self._notifyValue( repr(self.__name), "???", NOTIF_STATUS_SP, " ")
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        if type(self.__name)==list:
            self._write("Cancel setting the shared variables", {Severity:WARNING} )
        else:
            self._write("Cancel setting the shared variable " + repr(self.__name), {Severity:WARNING} )
        self._notifyValue( repr(self.__name), "???", NOTIF_STATUS_CL, " ")
        return [False, False]

################################################################################
class ClearSharedData_Helper(WrapperHelper):
    
    __name = None
    __scope = ""
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__name = None
        self.__scope = ""
        self._opName = None 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        useConfig = {}
        useConfig.update(self.getConfig())

        # Variable name or list of names given as positional argument
        if len(args)==1:
            self.__name = args[0]
        elif len(args)==0:
            if Name in useConfig:
                self.__name = useConfig[Name]
                self.delConfig(Name)
            else:
                self.__name = None
        else:
            raise SyntaxException("Malformed arguments")
        
        # Process scope
        if Scope in useConfig.keys():
            self.__scope = useConfig[Scope]
            # Remove scope from configuration,as this modifier is used in notifications and prompts
            self.delConfig(Scope)
        else:
            self.__scope = GLOBAL
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        
        if self.__name:

            self.setFailureInfo("DATA", self.__name)
            
            if type(self.__name) == list:
                self._setActionString( ACTION_REPEAT , "Try to remove the shared variables again")
                self._setActionString( ACTION_SKIP   , "Skip the removal of the shared variables and return True")
                self._setActionString( ACTION_CANCEL , "Skip the removal of the shared variables and return False")
            else:
                self._setActionString( ACTION_REPEAT , "Try to remove the shared variable " + repr(self.__name) + " again")
                self._setActionString( ACTION_SKIP   , "Skip the removal of the shared variable " + repr(self.__name) + " and return True")
                self._setActionString( ACTION_CANCEL , "Skip the removal of the shared variable " + repr(self.__name) + " and return False")
            
        elif self.__scope != GLOBAL:
            
            scName = str(self.__scope)
            if scName == GLOBAL: scName = "Global"
            
            # Store information for possible failures
            self.setFailureInfo("DATA", scName)
            self._setActionString( ACTION_REPEAT , "Try to clear all shared variables in scope " + scName + " again")
            self._setActionString( ACTION_SKIP   , "Skip clearing all shared variables in scope " + scName + " and return True")
            self._setActionString( ACTION_CANCEL , "Skip clearing all shared variables in scope " + scName + " and return False")
            
        else:
            # Store information for possible failures
            self.setFailureInfo("DATA", GLOBAL)
            self._setActionString( ACTION_REPEAT , "Try to clear all global shared variables again")
            self._setActionString( ACTION_SKIP   , "Skip clearing all global shared variables and return True")
            self._setActionString( ACTION_CANCEL , "Skip clearing all global shared variables and return False")

        result = None
        try:
            if self.__name:
                result = REGISTRY['CIF'].clearSharedData( self.__name, self.__scope )
                ntype = NOTIF_STATUS_OK
                if not result: ntype = NOTIF_STATUS_FL
                self._notifyValue( repr(self.__name), "Clear", ntype, " ")
            else:
                result = REGISTRY['CIF'].clearSharedData( self.__scope )
                ntype = NOTIF_STATUS_OK
                if not result: ntype = NOTIF_STATUS_FL
                scName = str(self.__scope)
                if scName == GLOBAL: scName = "Global"
                self._notifyValue( "Scope", scName, ntype, " ")
        except DriverException,ex:
            if self.__name:
                self._notifyValue( repr(self.__name), "", NOTIF_STATUS_FL, " ")
            else:
                scName = str(self.__scope)
                if scName == GLOBAL: scName = "Global"
                self._notifyValue( "Scope", scName, NOTIF_STATUS_FL, " ")
            raise ex

        return [False,result,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doRepeat(self):
        if self.__name:
            if type(self.__name)==list:
                self._write("Retry removing shared variables", {Severity:WARNING} )
            else:
                self._write("Retry removing shared variable " + repr(self.__name), {Severity:WARNING} )
        else:
            self._write("Retry clearing all shared variables", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        if self.__name:
            if type(self.__name)==list:
                self._write("Skip removing shared variables", {Severity:WARNING} )
            elif self.__name:
                self._write("Skip removing shared variable " + repr(self.__name), {Severity:WARNING} )
            self._notifyValue( repr(self.__name), "", NOTIF_STATUS_SP, " ")
        else:
            self._write("Skip clearing all shared variables", {Severity:WARNING} )
            scName = str(self.__scope)
            if scName == GLOBAL: scName = "Global"
            self._notifyValue( "Scope", scName, NOTIF_STATUS_SP, " ")
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        if self.__name:
            if type(self.__name)==list:
                self._write("Cancel removing shared variables", {Severity:WARNING} )
            else:
                self._write("Cancel removing shared variable " + repr(self.__name), {Severity:WARNING} )
            self._notifyValue( repr(self.__name), "", NOTIF_STATUS_CL, " ")
        else:
            self._write("Cancel clearing all shared variables", {Severity:WARNING} )
            scName = str(self.__scope)
            if scName == GLOBAL: scName = "Global"
            self._notifyValue( "Scope", "scName", NOTIF_STATUS_CL, " ")
        return [False, False]

################################################################################
class GetSharedData_Helper(WrapperHelper):
    
    __name = None
    __scope = ""
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__name = None
        self.__scope = GLOBAL
        self._opName = None 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        useConfig = {}
        useConfig.update(self.getConfig())

        # Variable name or list of names given
        if len(args)==1:
            self.__name = args[0]
        elif len(args)==0:
            if Name in useConfig:
                self.__name = useConfig[Name]
                self.delConfig(Name)
            else:
                raise SyntaxException("Malformed arguments")
        else:
            raise SyntaxException("Malformed arguments")

        if Scope in useConfig.keys():
            self.__scope = useConfig[Scope]
            # Remove scope from configuration,as this modifier is used in notifications and prompts
            self.delConfig(Scope)
        else:
            self.__scope = GLOBAL
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Store information for possible failures
        self.setFailureInfo("DATA", self.__name)
        
        if type(self.__name)==list:
            self._setActionString( ACTION_REPEAT , "Try to read the value of the shared variables again")
            self._setActionString( ACTION_SKIP   , "Skip reading the value of the shared variables and return None")
        else:
            self._setActionString( ACTION_REPEAT , "Try to read the value of the shared variable " + repr(self.__name) + " again")
            self._setActionString( ACTION_SKIP   , "Skip reading the value of the shared variable " + repr(self.__name) + " and return True")

        value = None
        try:
            value = REGISTRY['CIF'].getSharedData( self.__name, self.__scope )
            self._notifyValue( repr(self.__name), repr(value), NOTIF_STATUS_OK, " ")
        except DriverException,ex:
            self._notifyValue( repr(self.__name), "???", NOTIF_STATUS_FL, " ")
            raise ex

        return [False,value,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doRepeat(self):
        if type(self.__name)==list:
            self._write("Retry reading shared variables", {Severity:WARNING} )
        else:
            self._write("Retry reading shared variable " + repr(self.__name), {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        if type(self.__name)==list:
            self._write("Skip reading shared variables", {Severity:WARNING} )
        else:
            self._notifyValue( repr(self.__name), "???", NOTIF_STATUS_SP, " ")
            self._write("Skip reading shared variable " + repr(self.__name), {Severity:WARNING} )
        return [False, None]

################################################################################
class GetSharedDataKeys_Helper(WrapperHelper):
    
    __scope = ""
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__scope = GLOBAL
        self._opName = None 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)!=0:
            raise SyntaxException("Malformed arguments")
        
        useConfig = {}
        useConfig.update(self.getConfig())

        if Scope in useConfig.keys():
            self.__scope = useConfig[Scope]
            # Remove scope from configuration,as this modifier is used in notifications and prompts
            self.delConfig(Scope)
        else:
            self.__scope = GLOBAL
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Store information for possible failures
        self.setFailureInfo("DATA", self.__scope)

        self._setActionString( ACTION_REPEAT , "Try to get the shared variable key list again")
        self._setActionString( ACTION_SKIP   , "Skip the acquisition of the shared variable key list and return empty list")

        keys = []
        try:
            keys = REGISTRY['CIF'].getSharedDataKeys( self.__scope )
            self._notifyValue( "Keys", repr(keys), NOTIF_STATUS_OK, " ")
        except DriverException,ex:
            self._notifyValue( "Keys", "???", NOTIF_STATUS_FL, " ")
            raise ex

        return [False,keys,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry getting shared variable keys", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "Keys", "???", NOTIF_STATUS_SP, " ")
        self._write("Skip getting shared variable keys", {Severity:WARNING} )
        return [False, []]

################################################################################
class GetSharedDataScopes_Helper(WrapperHelper):
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self._opName = None 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)!=0:
            raise SyntaxException("Malformed arguments")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Store information for possible failures
        self.setFailureInfo("DATA", "Get Scopes")

        self._setActionString( ACTION_REPEAT , "Try to get the list of shared data scopes again")
        self._setActionString( ACTION_SKIP   , "Skip reading the scope list and return an empty list")

        scopes = []
        try:
            scopes = REGISTRY['CIF'].getSharedDataScopes()
            self._notifyValue( "Scopes", repr(scopes), NOTIF_STATUS_OK, " ")
        except DriverException,ex:
            self._notifyValue( "Scopes", "???", NOTIF_STATUS_FL, " ")
            raise ex

        return [False,scopes,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry getting the list of shared data scopes", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( "Scopes", "???", NOTIF_STATUS_SP, " ")
        self._write("Skip getting the list of shared data scopes", {Severity:WARNING} )
        return [False, []]


################################################################################
class AddSharedDataScope_Helper(WrapperHelper):
    
    __scope = ""
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__scope = None
        self._opName = None 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)!=1:
            raise SyntaxException("Malformed arguments (expected scope name)")

        if type(args[0])!=str:
            raise SyntaxException("Malformed arguments (expected scope name as string)")
        
        useConfig = {}
        useConfig.update(self.getConfig())
        self.__scope = args[0]
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Store information for possible failures
        self.setFailureInfo("DATA", self.__scope)

        self._setActionString( ACTION_REPEAT , "Try to add the shared variable scope again")
        self._setActionString( ACTION_SKIP   , "Skip adding the shared variable scope and return True")
        self._setActionString( ACTION_CANCEL , "Skip adding the shared variable scope and return False")

        result = None
        try:
            result = REGISTRY['CIF'].addSharedDataScope( self.__scope )
            ntype = NOTIF_STATUS_OK
            if not result: ntype = NOTIF_STATUS_FL
            scName = str(self.__scope)
            if scName == GLOBAL: scName = "Global"
            self._notifyValue( "Scope", scName, ntype, " ")
        except DriverException,ex:
            scName = str(self.__scope)
            if scName == GLOBAL: scName = "Global"
            self._notifyValue( "Scope", scName, NOTIF_STATUS_FL, " ")
            raise ex

        return [False,result,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry adding shared variable scope", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        scName = str(self.__scope)
        if scName == GLOBAL: scName = "Global"
        self._notifyValue( "Scope", scName, NOTIF_STATUS_SP, " ")
        self._write("Skip adding shared variable scope", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        scName = str(self.__scope)
        if scName == GLOBAL: scName = "Global"
        self._notifyValue( "Scope", scName, NOTIF_STATUS_CL, " ")
        self._write("Cancel adding shared variable scope", {Severity:WARNING} )
        return [False, False]

################################################################################
class ClearSharedDataScopes_Helper(WrapperHelper):
    
    __scope = ""
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__scope = None
        self._opName = None 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)>1:
            raise SyntaxException("Malformed arguments")

        if len(args)==1 and type(args[0])!=str:
            raise SyntaxException("Malformed arguments (expected scope name as string)")
        
        useConfig = {}
        useConfig.update(self.getConfig())
        
        if len(args)==1:
            self.__scope = args[0]
        else:
            self.__scope = None
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Store information for possible failures
        self.setFailureInfo("DATA", self.__scope)

        self._setActionString( ACTION_REPEAT , "Try to remove the shared variable scope(s) again")
        self._setActionString( ACTION_SKIP   , "Skip removing the shared variable scope(s) and return True")
        self._setActionString( ACTION_CANCEL , "Skip removing the shared variable scope(s) and return False")

        result = None
        try:
            if self.__scope:
                result = REGISTRY['CIF'].removeSharedDataScope( self.__scope )
                ntype = NOTIF_STATUS_OK
                if not result: ntype = NOTIF_STATUS_FL
                scName = str(self.__scope)
                if scName == GLOBAL: scName = "Global"
                self._notifyValue( "Remove", scName, ntype, " ")
            else:
                result = REGISTRY['CIF'].removeSharedDataScopes()
                ntype = NOTIF_STATUS_OK
                if not result: ntype = NOTIF_STATUS_FL
                self._notifyValue( "Remove", "all scopes", ntype, " ")
        except DriverException,ex:
            if self.__scope:
                scName = str(self.__scope)
                if scName == GLOBAL: scName = "Global"
                self._notifyValue( "Remove", scName, NOTIF_STATUS_FL, " ")
            else:
                self._notifyValue( "Remove", "all scopes", NOTIF_STATUS_FL, " ")
            raise ex

        return [False,result,NOTIF_STATUS_OK,OPERATION_SUCCESS]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry removing shared variable scope(s)", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        if self.__scope:
            scName = str(self.__scope)
            if scName == GLOBAL: scName = "Global"
            self._notifyValue( "Remove", scName, NOTIF_STATUS_SP, " ")
        else:
            self._notifyValue( "Remove", "all scopes", NOTIF_STATUS_SP, " ")
        self._write("Skip removing shared variable scope(s)", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        if self.__scope:
            scName = str(self.__scope)
            if scName == GLOBAL: scName = "Global"
            self._notifyValue( "Remove", scName, NOTIF_STATUS_CL, " ")
        else:
            self._notifyValue( "Remove", "all scopes", NOTIF_STATUS_CL, " ")
        self._write("Cancel removing shared variable scope(s)", {Severity:WARNING} )
        return [False, False]

