###################################################################################
## MODULE     : spell.lib.adapter.tc
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telecommand interface
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
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.utils.log import *
from spell.lib.adapter.constants.notification import *
from spell.lib.registry import *
from spell.lib.adapter.utctime import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from config import Configurable
from tc_item import *
from interface import Interface

#*******************************************************************************
# System imports
#*******************************************************************************
import time,sys

################################################################################
# Module import definition

__all__ = ['TcInterface']

INTERFACE_DEFAULTS = { OnFailure:ABORT | SKIP | RESEND | CANCEL,
                       Time: 0,
                       Timeout: 30,
                       Confirm: False,
                       SendDelay: 0,
                       PromptUser:True,
                       OnFalse:NOPROMPT,
                       OnTrue: NOPROMPT,
                       OnSkip: True }

################################################################################
class TcInterface(Configurable,Interface):
    
    """
    DESCRIPTION:
        This class provides the telecommand management interface.
    """
    
    __useConfig = {}
    # To supress useless notifications
    __lastStatus = None
    __lastElement = None
    __tcConfirm = False
    
    #===========================================================================
    def __init__(self):
        Interface.__init__(self, "TC")
        Configurable.__init__(self)
        self.__lastStatus = None
        self.__lastElement = None
        self.__useConfig = {}
        self.__tcConfirm = False
        LOG("Created")

    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )

    #===========================================================================
    def forceTcConfirm(self, confirm = False ):
        self.__tcConfirm = confirm

    #===========================================================================
    def shouldForceTcConfirm(self, confirm = False ):
        return self.__tcConfirm

    #==========================================================================
    def setup(self, ctxConfig, drvConfig ):
        LOG("Setup TC adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #==========================================================================
    def cleanup(self):
        LOG("Cleanup TC adapter interface")
    
    #==========================================================================
    def send(self, *args, **kargs):
        """
        ------------------------------------------------------------------------
        Syntax #1:
            TC.send( "tc name" )
            
            Send the tc or sequence with the given name, with default
            configuration and no arguments 

        ------------------------------------------------------------------------
        Syntax #2:
            TC.send( <tc item> )

            Send the given tc or sequence item, with default
            configuration. IMPORTANT: a tc/sequence item may hold the tc arguments
            list inside it, therefore they are not visible in this call. 
            
        ------------------------------------------------------------------------
        Syntax #3:
            TC.send( "tc name", {config} )
            
            Send the tc or sequence with the given name, with particular
            configuration (see below)
            
        ------------------------------------------------------------------------
        Syntax #4:
            TC.send( <tc item>, {config} )

            Send the given tc or sequence item, with particular configuration 
            (see below). IMPORTANT: a tc/sequence item may hold the tc arguments 
            list inside it, therefore they are not visible in this call.

        ------------------------------------------------------------------------
        Syntax #5:
            TC.send( "tc name", [ <arguments> ] )
            TC.send( <tc item>, [ <arguments> ] )

            Send the tc or sequence with the given name, with default config
            and the given list of arguments. In case of passing a tc item,
            the explicit list of arguments will overwrite any possible list
            of arguments that the tc item contains. 

        ------------------------------------------------------------------------
        Syntax #6:
            TC.send( "tc name", [ <arguments> ], {config} )
            TC.send( <tc item>, [ <arguments> ], {config} )

            Send the tc or sequence with the given name, with particular config
            and the given list of arguments. In case of passing a tc item,
            the explicit list of arguments will overwrite any possible list
            of arguments that the tc item contains. 

        ------------------------------------------------------------------------
        Syntax #7:
            TC.send( [ tc item list ]  )
            TC.send( [ tc item list ], {config} )

            Send a list of tc items, with default or specific global configuration.
            Notice that in this case is not possible to use a tc argument
            list, all tc arguments shall be set for each tc item.
            
            The format of the tc item list is
            
            [ [ <tc_item>, {config} ], ... ]
            
            Where the specific tc configuration dictionary is optional. Notice
            that specific configuration overrides global configuration.
            
        Configuration
        ------------------------------------------------------------------------
        Possible configuration modifiers are:
        
            Time:<date-time string> - TC execution time (timetag commands)
            Confirm:True/False      - Prompt user for confirmation before send
            Block:True/False        - Send commands as a block
            Timeout:<float>         - Execution timeout
            
        TC Arguments
        ------------------------------------------------------------------------
        Arguments can be passed by using the parameter list (syntax #5,#6) or
        by setting them inside a tc item.
        
        a) Parameter list: has the following format
         
            [ [ <name>, <value> , {config} ], ... ]
            
            First element shall be the parameter name. The second element
            may be a constant or variable with the parameter value, a Value
            class instance, or a tm item. In this last case, the value for
            the argument will be the value of the TM parameter at the moment
            of the send() call.
            
            Config dictionary is optional, and may be used to specify format
            of the parameter value, or to configure a tm item value extraction.
            Examples:
            
            [ 'param', 0xAF, { Radix:HEX } ]  
            [ 'param', TM['B167'], { ValueFormat:RAW, Radix:OCT } ]
            [ 'param', X, { ValueType:LONG, Radix:INT, Units:"deg"} ]
            
        b) TC builtin arguments: to assign tc arguments to a tc item
        
            item[<name>] = [ <value> , {config} ]
            
            The same as said in a) applies for <value> and config dictionary. 

        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. ValueFormat) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            Function( param, { Modifier:Value } )    is the same as
            Function( param, modifier = Value ) 
        ------------------------------------------------------------------------
        """
    
        if (len(args)==0 and len(kargs)==0) or\
           (len(args)==0 and not kargs.has_key('tc')) or \
           (len(args)>0 and \
                ( type(args[0])!=str and \
                  type(args[0])!=list and \
                  not isinstance(args[0],TcItemClass) )):
            raise SyntaxException("Expected a command name, item or list")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        tcList = self.__buildTcList(args)

        self.__useConfig = useConfig
        return self.__processTcList(tcList,useConfig)

    #==========================================================================
    def __processTcList(self, tcList, useConfig):
        
        # Force the Confirm=True if requested
        useConfig[Confirm] = True
        
        LOG("Interface configuration:\n\n" + repr(useConfig) + "\n")
        if useConfig.has_key(Block) and useConfig.get(Block):
            if type(tcList)!=list:
                raise DriverException("Expected a command list")
            
            # Use copies to ensure that the driver does not modify the 
            # original items by mistake
            listCopy = []
            for item in tcList:
                listCopy.append( item._copy() )

            LOG("Sending commands as block")
            self._checkCriticalCommands(listCopy, useConfig)
            # Send commands as a block
            return self._sendBlock( listCopy, useConfig )
        else:
            
            # Use copies to ensure that the driver does not modify the 
            # original items by mistake
            listCopy = []
            for item in tcList:
                listCopy.append( item._copy() )

            # Send single command
            if len(listCopy)==1:
                tcitem = listCopy[0]
                LOG("Sending a single command/sequence")
                self._checkCriticalCommands(listCopy, useConfig)
                LOG("Sending " + tcitem.name())
                LOG("Item configuration:\n\n" + repr(tcitem.getConfig()) + "\n")
                self.__lastStatus = None
                self.__lastElement = None
                return self._sendCommand( tcitem, useConfig )
            
            # Send list of commands one by one
            else:
                self._checkCriticalCommands(listCopy, useConfig)
                return self._sendList(listCopy, useConfig)

    #==========================================================================
    def __buildTcList(self, args):
        # Build the tc list
        if type(args[0])==list:
            tcList = args[0]   
        else:
            # If tc name given, get the tc item
            if type(args[0])==str:
                tc = self[args[0]]
            elif isinstance(args[0],TcItemClass):
                tc = args[0]
            else:
                raise SyntaxException("Malformed argument")
            # Using a tc argument list
            if len(args)>1 and type(args[1])==list:
                # Put all arguments inside the tc
                for argument in args[1]:
                    if len(argument)<2:
                        raise SyntaxException("Malformed argument")
                    argName = argument[0]
                    argValue = argument[1]
                    if len(argument)==3:
                        argConfig = argument[2]
                    else:
                        argConfig = None
                    # Set the argument to tc
                    if argConfig is None:
                        tc[argName] = [ argValue ]
                    else:
                        tc[argName] = [ argValue, argConfig ]
            tcList = [ tc ]
        return tcList

    #==========================================================================
    def __getitem__(self, key):
        # If the parameter mnemonic is composed of several words:
        words = key.strip().split()
        mnemonic = key
        description = None
        
        if len(words)>1 and words[0].upper() == 'C':
            mnemonic = words[1]
            description = ' '.join(words[2:])
        else:
            mnemonic = key
            description = ""

        REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, mnemonic, "building", "IN PROGRESS", "Building TC item")

        LOG("Creating TC item for " + repr(mnemonic))
        LOG("Description: " + repr(description))
        item = self._createTcItem(mnemonic,description)
        item._setDescription(description)
            
        REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, mnemonic, "created", "SUCCESS", description)
        return item

    #==========================================================================
    def _createTcItem(self, mnemonic, description = "" ):
        return TcItemClass(self, mnemonic, description)

    #==========================================================================
    def _checkCriticalCommands(self, tcList, config = {} ):
        return None

    #==========================================================================
    def _sendCommand(self, tcItem, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _sendList(self, tcItemList, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _sendBlock(self, tcItemList, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _updateStatus(self, tcItem):
        if self.__useConfig.has_key(Notify) and not self.__useConfig.get(Notify):
            return

        tcItemName = tcItem.name()

        # Add the send delay or releasetime to the name if present
        itemConfig = tcItem.getConfig()
        if itemConfig.has_key (ReleaseTime):
            if (isinstance(itemConfig.get(ReleaseTime), TIME)):
                tcItemName += ";/" + str(itemConfig.get(ReleaseTime))
        elif itemConfig.has_key (SendDelay):
            if (itemConfig.get(SendDelay) != None):
                tcItemName += ";+" + str(itemConfig.get(SendDelay))

        # If the tc item has internal elements (block/sequence) the notification
        # shall be multiple.
        
        # Simple commands
        if not tcItem.isComplex():
            exstage,exstatus = tcItem.getExecutionStageStatus()
            completed        = tcItem.getIsCompleted()
            comment          = tcItem.getComment()
            success          = tcItem.getIsSuccess()
            updtime          = tcItem.getUpdateTime()            
            if success:
                status = "SUCCESS"
                reason = ""
            else:
                if completed:
                    status = "FAILED"
                    reason = "Execution failed (Stage " + repr(exstage) + " is " + repr(exstatus) + ")"
                else:
                    status = "IN PROGRESS"
                    if comment == "":
                        reason = "Status is " + repr(exstatus)
                    else:
                        reason = comment
            REGISTRY['CIF'].notify( NOTIF_TYPE_EXEC, tcItemName, exstage, status, reason, updtime)
        # Multiple (blocks/sequences)
        else:
            itemElements = tcItem.getElements()
            if len(itemElements)>0:
                nameStr = ""
                stageStr = ""
                statusStr = "" 
                reasonStr = ""
                timeStr = ""
                add_sep = False
                for elementId in itemElements:
                    exstage,exstatus = tcItem.getExecutionStageStatus(elementId)
                    comment = tcItem.getComment(elementId)
                    success   = tcItem.getIsSuccess(elementId)
                    completed = tcItem.getIsCompleted(elementId)
                    updtime   = tcItem.getUpdateTime(elementId)
                    if success:
                        status = "SUCCESS"
                        reason = " "
                    else:
                        if completed:
                            status = "FAILED"
                            reason = "Execution failed (Stage " + repr(exstage) + " is " + repr(exstatus) + ")"
                        else:
                            status = "IN PROGRESS"
                            if len(exstage.strip())==0:
                                reason = " "
                            else:
                                reason = "Stage " + repr(exstage) + " is " + repr(exstatus)
                    if add_sep:    
                        nameStr   += ITEM_SEP
                        stageStr  += ITEM_SEP
                        statusStr += ITEM_SEP
                        reasonStr += ITEM_SEP
                        timeStr   += ITEM_SEP
                    nameStr   += elementId
                    stageStr  += exstage
                    statusStr += status
                    reasonStr += reason
                    timeStr   += updtime
                    add_sep = True
                REGISTRY['CIF'].notify( NOTIF_TYPE_EXEC, nameStr, stageStr, statusStr, reasonStr, timeStr)
