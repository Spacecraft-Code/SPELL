###################################################################################
## MODULE     : spell.lib.adapter.pcs
## DATE       : Sep 19, 2016
## PROJECT    : SPELL
## DESCRIPTION: PCS interface
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
from .config import Configurable
from .pcs_item import *
from .interface import Interface

#*******************************************************************************
# System imports
#*******************************************************************************
import time,sys

################################################################################
# Module import definition

__all__ = ['PcsInterface']

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
class PcsInterface(Configurable, Interface):

    """
    DESCRIPTION:
        This class provides the PCS management interface.
    """

    __useConfig = {}
    # To suppress useless notifications
    __lastStatus = None
    __lastElement = None
    __tcConfirm = False

    #===========================================================================
    def __init__(self):
        Interface.__init__(self, "PCS")
        Configurable.__init__(self)
        self.__lastStatus = None
        self.__lastElement = None
        self.__useConfig = {}
        self.__tcConfirm = False
        LOG("Created PCS Interface")

    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )

    #===========================================================================
    def forcePtcConfirm(self, confirm = False ):
        self.__tcConfirm = confirm

    #===========================================================================
    def shouldForcePtcConfirm(self, confirm = False ):
        return self.__tcConfirm

    #==========================================================================
    def setup(self, ctxConfig, drvConfig ):
        LOG("Setup PCS adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #==========================================================================
    def cleanup(self):
        LOG("Cleanup PCS adapter interface")

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

        REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, mnemonic, "building", "IN PROGRESS", "Building PTC item")

        LOG("Creating PTC item for " + repr(mnemonic))
        LOG("Description: " + repr(description))
        item = self._createPtcItem(mnemonic,description)
        item._setDescription(description)

        REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, mnemonic, "created", "SUCCESS", description)
        return item

    #==========================================================================
    def _createPtcItem(self, mnemonic, description = "" ):
        return PtcItemClass(self, mnemonic, description)

    #### Language functions ####

    #===========================================================================
    def PCS_GetStatus(self, *args, **kargs):
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._PCS_GetStatus(useConfig)

    #===========================================================================
    def PCS_IsArqEnabled(self, *args, **kargs ):
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._PCS_IsArqEnabled(useConfig)

    #==========================================================================
    def PCS_IsVerifyModeEnabled(self, *args, **kargs ):
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._PCS_IsVerifyModeEnabled(useConfig)

    #==========================================================================
    def PCS_AbortPtc(self, *args, **kargs ):
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._PCS_AbortPtc(useConfig)

    #===========================================================================
    def PCS_Send(self, *args, **kargs ):

        ### Check function syntax
#        if(len(args)!=1):
#            raise SyntaxException("Expected a single Pseudo Command")

        ### Prepare configuration
        if (len(args)==0 and len(kargs)==0) or\
           (len(args)==0 and 'pcs' not in kargs) or \
           (len(args)>0 and \
                ( type(args[0])!=str and \
                  type(args[0])!=list and \
                  not isinstance(args[0],PtcItemClass) )):
            raise SyntaxException("Expected a command name, item or list")

        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        ptcList = self.__buildPtcList(args)

        self.__useConfig = useConfig
        self.__lastStatus = None
        self.__lastElement = None

        return self.__processPtcList(ptcList, useConfig)

    #==========================================================================
    def __buildPtcList(self, args):
        # Build the tc list
        if type(args[0])==list:
            ptcList = args[0]
        else:
            # If tc name given, get the tc item
            if type(args[0])==str:
                ptc = self[args[0]]
            elif isinstance(args[0],PtcItemClass):
                ptc = args[0]
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
                        ptc[argName] = [ argValue ]
                    else:
                        ptc[argName] = [ argValue, argConfig ]
            ptcList = [ ptc ]
        return ptcList
    #==========================================================================
    def __processPtcList(self, ptcList, useConfig):

        # Force the Confirm=True if requested
        useConfig[Confirm] = True

        LOG("Interface configuration:\n\n" + repr(useConfig) + "\n")

        # Use copies to ensure that the driver does not modify the
        # original items by mistake
        listCopy = []
        for item in ptcList:
            listCopy.append( item._copy() )

        # Send single command
        if len(listCopy)==1:
            ptcitem = listCopy[0]
            LOG("Sending a pseudo single command/sequence")
            self._checkCriticalPtcCommands(listCopy, useConfig)
            LOG("Sending " + ptcitem.name())
            LOG("Item configuration:\n\n" + repr(ptcitem.getConfig()) + "\n")
            self.__lastStatus = None
            self.__lastElement = None
            return self._sendPtcCommand( ptcitem, useConfig )
        else:
            self._checkCriticalPtcCommands(listCopy, useConfig)
            return self._sendPtcList(listCopy, useConfig)

    #==========================================================================
    def _checkCriticalPtcCommands(self, ptcList, config = {} ):
        return None

    #==========================================================================
    def _sendPtcCommand(self, ptcItem, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _sendPtcList(self, ptcItemList, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def PCS_Control(self, *args, **kargs ):
        ### Check function syntax
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        self.__useConfig = useConfig
        self.__lastStatus = None
        self.__lastElement = None

        ptcitem = args[0]

        return self._PCS_Control(ptcitem, useConfig)

    #===========================================================================
    def PCS_ReadyToCommand(self, *args, **kargs):
        ## Implementation and call to the driver
        ### Check function syntax

        # Obtain global  config
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._PCS_ReadyToCommand(useConfig)

    #### Calls to driver implementation ####

    #===========================================================================
    def _PCS_GetStatus(self, config = {}  ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    def _PCS_isArqEnabled(self, config = {}  ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _PCS_isVerifyModeEnabled(self, config = {}  ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def _PCS_AbortPtc(self, config = {}  ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #===========================================================================
    # Replaced by
    #     _sendPtcCommand(self, tcItem, config = {} ):
    #     _sendPtcList(self, tcItemList, config = {} ):
    #     _sendPtcBlock(self, tcItemList, config = {} ):
    #
    #def _PCS_Send(self, ptcitem, config = {}  ):
    #    REGISTRY['CIF'].write("Service not implemented on this driver (" + ptcitem + ")" , {Severity:WARNING})
    #    return False

    #===========================================================================
    def _PCS_Control(self, ptcitem, config = {}  ):
        REGISTRY['CIF'].write("Service not implemented on this driver (" + ptcitem + ")", {Severity:WARNING})
        return False

    #===========================================================================
    def _PCS_ReadyToCommand(self, config = {}  ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False


#    #==========================================================================
#    def _sendCommand(self, tcItem, config = {} ):
#        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
#        return False
#
#    #==========================================================================
#    def _sendList(self, tcItemList, config = {} ):
#        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
#        return False
#
#    #==========================================================================
#    def _sendBlock(self, tcItemList, config = {} ):
#        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
#        return False
#

    ### Callback functions ###

    #==========================================================================
    def _updateStatus(self, ptcItem):
        try:
            if Notify in self.__useConfig and not self.__useConfig.get(Notify):
                return

            ptcItemName = ptcItem.name()

            # Add the send delay or releasetime to the name if present
            itemConfig = ptcItem.getConfig()
            if ReleaseTime in itemConfig:
                if (isinstance(itemConfig.get(ReleaseTime), TIME)):
                    ptcItemName += ";/" + str(itemConfig.get(ReleaseTime))
            elif SendDelay in itemConfig:
                if (itemConfig.get(SendDelay) != None):
                    ptcItemName += ";+" + str(itemConfig.get(SendDelay))

            # If the tc item has internal elements (block/sequence) the notification
            # shall be multiple.

            # Simple commands
            if not ptcItem.isComplex():
                internalName     = ptcItem.getElements()[-1]
                exstage,exstatus = ptcItem.getExecutionStageStatus(internalName)
                completed        = ptcItem.getIsCompleted(internalName)
                comment          = ptcItem.getComment(internalName)
                success          = ptcItem.getIsSuccess(internalName)
                updtime          = ptcItem.getUpdateTime(internalName)
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
                subTCs = ptcItem.getSubTCStatus(internalName)
                idx = 0
                sub_add_sep = False
                sub_nameStr   = ''
                sub_stageStr  = ''
                sub_statusStr = ''
                sub_reasonStr = ''
                sub_timeStr   = ''
                subIdx = 1
                while str(idx) in list(subTCs.keys()):
                    subGroup = subTCs[str(idx)]

                    for entry in subGroup:
                        subItemName = str(idx) + str(subIdx) + '@' + str(subIdx) +  '. VWDSPCMD'
                        subStage = 'Unknown'
                        subStatus = 'IN PROGRESS'
                        if entry == 'V':
                            subStage = 'Verified'
                            subStatus = 'SUCCESS'
                        elif entry == 'U':
                            subStage = 'Uploaded'
                        elif entry == 'P':
                            subStage = 'Progress'
                        elif entry == 'F':
                            subStage = 'Failed'
                            subStatus = 'FAILED'
                        #ENDIF
                        if sub_add_sep:
                            sub_nameStr   += ITEM_SEP
                            sub_stageStr  += ITEM_SEP
                            sub_statusStr += ITEM_SEP
                            sub_reasonStr += ITEM_SEP
                            sub_timeStr   += ITEM_SEP
                        sub_nameStr   += subItemName
                        sub_stageStr  += subStage
                        sub_statusStr += subStatus
                        sub_reasonStr += ' '
                        sub_timeStr   += updtime
                        sub_add_sep = True
                        subIdx += 1
                    #ENDFOR
                    if len(reason.strip()) == 0:
                        reason = '#%s:[%s]'%(idx,subGroup)
                    elif idx == 0:
                        reason += ' TC Status: #%s:[%s]'%(idx,subGroup)
                    else:
                        reason += ' #%s:[%s]'%(idx,subGroup)
                    idx += 1
                #ENDWHILE
                #REGISTRY['CIF'].write(reason)
                if sub_nameStr:
                    ptcItemName += ITEM_SEP + sub_nameStr
                    exstage     += ITEM_SEP + sub_stageStr
                    status      += ITEM_SEP + sub_statusStr
                    reason      += ITEM_SEP + sub_reasonStr
                    updtime += ITEM_SEP + sub_timeStr
                REGISTRY['CIF'].notify( NOTIF_TYPE_EXEC, ptcItemName, exstage, status, reason, updtime)
            # Multiple (blocks/sequences)
            else:
                itemElements = ptcItem.getElements()
                if len(itemElements)>0:
                    nameStr = ""
                    stageStr = ""
                    statusStr = ""
                    reasonStr = ""
                    timeStr = ""
                    add_sep = False
                    for elementId in itemElements:
                        exstage,exstatus = ptcItem.getExecutionStageStatus(elementId)
                        comment = ptcItem.getComment(elementId)
                        success   = ptcItem.getIsSuccess(elementId)
                        completed = ptcItem.getIsCompleted(elementId)
                        updtime   = ptcItem.getUpdateTime(elementId)
                        globalIdx       = itemElements.index(elementId)
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
                                    reason = "Stage " + repr(exstage)

                        subTCs = ptcItem.getSubTCStatus(elementId)
                        idx = 0
                        sub_add_sep = False
                        sub_nameStr   = ''
                        sub_stageStr  = ''
                        sub_statusStr = ''
                        sub_reasonStr = ''
                        sub_timeStr   = ''
                        subIdx = 1
                        while str(idx) in list(subTCs.keys()):
                            subGroup = subTCs[str(idx)]

                            for entry in subGroup:
                                subItemName = str(globalIdx) + str(idx) + str(subIdx) + '@' + str(subIdx) +  '. VWDSPCMD'
                                subStage = 'Unknown'
                                subStatus = 'IN PROGRESS'
                                if entry == 'V':
                                    subStage = 'Verified'
                                    subStatus = 'SUCCESS'
                                elif entry == 'U':
                                    subStage = 'Uploaded'
                                elif entry == 'P':
                                    subStage = 'Progress'
                                elif entry == 'F':
                                    subStage = 'Failed'
                                    subStatus = 'FAILED'
                                #ENDIF
                                if sub_add_sep:
                                    sub_nameStr   += ITEM_SEP
                                    sub_stageStr  += ITEM_SEP
                                    sub_statusStr += ITEM_SEP
                                    sub_reasonStr += ITEM_SEP
                                    sub_timeStr   += ITEM_SEP
                                sub_nameStr   += subItemName
                                sub_stageStr  += subStage
                                sub_statusStr += subStatus
                                sub_reasonStr += ' '
                                sub_timeStr   += updtime
                                sub_add_sep= True
                                #REGISTRY['CIF'].notify( NOTIF_TYPE_EXEC, subItemName, subStage, subStatus, '', updtime)
                                subIdx += 1
                            #ENDFOR
                            if len(reason.strip()) == 0:
                                reason = '#%s:[%s]'%(idx,subGroup)
                            elif idx == 0:
                                reason += ' TC Status: #%s:[%s]'%(idx,subGroup)
                            else:
                                reason += ' #%s:[%s]'%(idx,subGroup)
                            idx += 1
                        #ENDWHILE
                        #REGISTRY['CIF'].write(reason)
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
                        if sub_nameStr:
                            nameStr   += ITEM_SEP + sub_nameStr
                            stageStr  += ITEM_SEP + sub_stageStr
                            statusStr += ITEM_SEP + sub_statusStr
                            reasonStr += ITEM_SEP + sub_reasonStr
                            timeStr   += ITEM_SEP + sub_timeStr

                        add_sep = True

                    REGISTRY['CIF'].notify( NOTIF_TYPE_EXEC, nameStr, stageStr, statusStr, reasonStr, timeStr)
        except:
            import traceback
            error = traceback.format_exc()
            LOG(error)
            del traceback
