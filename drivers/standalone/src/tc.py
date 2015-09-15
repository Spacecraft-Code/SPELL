###################################################################################
## MODULE     : tc
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telecommand interface of the driver connection layer
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lib.registry import REGISTRY
from spell.lang.modifiers import *
from spell.lang.functions import Prompt

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['TC']

###############################################################################
# Superclass
import spell.lib.adapter.tc
superClass = spell.lib.adapter.tc.TcInterface

###############################################################################
class TcInterface(superClass):
    
    """
    DESCRIPTION:
        Telecommand interface. 
    """
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone TC interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone TC interface")
    
    #===========================================================================
    def __getitem__(self, key):
        # If the telecommand mnemonic is composed of several words:
        words = key.split()
        mnemonic = key
        if len(words)>1 and words[0].upper() == 'C':
            mnemonic = words[1]
        else:
            mnemonic = key
        LOG("Return simulated item '" + mnemonic + "'")
        return REGISTRY['SIM'].getTCitem(mnemonic)

    #===========================================================================
    def driverUpdateActionList(self, theOptions, exception = None):
        theOptions.append( self._createAction('J', "Custom action: " + repr(exception)) )
        return theOptions

    #===========================================================================
    def driverPerformAction(self, code):
        if code == 'J':
            REGISTRY['CIF'].write("Standalone driver example action", {Severity:WARNING})
            return [False,True]
        return [False,False]

    #===========================================================================
    def driverBeforeAction(self, code):
        REGISTRY['CIF'].write("Standalone driver before action '" + str(code) + "'", {Severity:WARNING})

    #===========================================================================
    def driverAfterAction(self, code):
        REGISTRY['CIF'].write("Standalone driver after action '" + str(code) + "'", {Severity:WARNING})

    #==========================================================================
    def _sendCommand(self, tcItem, config = {} ):
        LOG("Sending command: " + repr(tcItem.name()))
        return self.__fakeExecution(tcItem)

    #==========================================================================
    def _sendList(self, tcItemList, config = {} ):
        # Sending commands one by one
        fakeCommand = self._createTcItem("List","Command list")
        itemNames = []
        for tcitem in tcItemList:
            if type(tcitem)==str:
                itemNames += [tcitem]
            else:
                itemNames += [tcitem.name()]
        fakeCommand._setElements(itemNames)
        return self.__fakeComplexExecution(fakeCommand)

    #==========================================================================
    def _sendBlock(self, tcItemList, config ):
        """
        DESCRIPTION:
        
        ARGUMENTS:
            
        RETURNS:
       
        RAISES:

        """
        for item in tcItemList:
            LOG("Sending block command: " + item.name())
        return self.__fakeExecution(tcItemList)
    
    #==========================================================================
    def __fakeExecution(self, item):
        
        LOG(" --> Executing command " + item.name())
        if len(item._getParams())>0:
            LOG("   Parameters:")
        else:
            LOG("   No parameters found")
        for p in item._getParams():
            LOG("   - " + repr(p.name) + " " + repr(p.value))
            
        REGISTRY['SIM'].executeCommand(item.name())
            
        item._setExecutionStageStatus("Uplinked","Passed")
        item._setExecutionStageStatus("Idle","Passed")

        if (item.name() == 'TC_FAIL'):
            item._setExecutionStageStatus("Execution","Failed")
            item._setCompleted(False)
            raise DriverException("Command failure")
        else:
            item._setExecutionStageStatus("Execution","Success")
            item._setCompleted(True)
        return True

    #==========================================================================
    def __fakeComplexExecution(self, item):

        LOG(" --> Executing list")
        item._setExecutionStageStatus("Execution", "Ongoing")
        
        itemList = item.getElements()[1:]
        
        for simpleItem in itemList:
            idx = simpleItem.find("@")
            name = simpleItem[idx+1:]
            LOG(" --> Executing list command " + name)
            resendCommand = True
            while resendCommand:
                try:
                    REGISTRY['SIM'].executeCommand(name)
        
                    item._setExecutionStageStatus("Uplinked","Passed", elementId = simpleItem)
                    item._setExecutionStageStatus("Idle","Passed", elementId = simpleItem)
        
                    if (name == 'TC_FAIL'):
                        item._setExecutionStageStatus("Execution","Failed", elementId = simpleItem)
                        item._setCompleted(False, elementId = simpleItem)
                        item._setExecutionStageStatus("Execution", "Failed")
                        raise DriverException("Command failure")
                    else:
                        item._setExecutionStageStatus("Execution","Success", elementId = simpleItem)
                        item._setCompleted(True, elementId = simpleItem)
                        
                    resendCommand = False
                    
                except DriverException,ex:

                    msg = "Execution of command " + name + " failed.\nDo you want to resend it?"
                    resendCommand = Prompt(msg,Type=YES_NO, Scope=SCOPE_PROC)
                    
                    if not resendCommand:
                        raise ex
                
        item._setExecutionStageStatus("Execution", "Finished")
        item._setCompleted(True)
        return True

################################################################################
# Interface handle
TC = TcInterface()
