###################################################################################
## MODULE     : spell.lib.adapter.pcs_item
## DATE       : Sep 19, 2016
## PROJECT    : SPELL
## DESCRIPTION: PCS item model
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
from spell.utils.log import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from .config import Configurable
from .value import *

#*******************************************************************************
# System imports
#*******************************************************************************
import sys,traceback,datetime

###############################################################################
# MODULE CONSTANTS

PTC_DEFAULTS   = {Confirm:False, Time:"", Block:False, Timeout:5}

###############################################################################
# IMPORT DEFINITION

__all__ = [ 'PtcItemClass' ]

###############################################################################

###############################################################################
class PtcItemParamClass(object):

    name = None
    value = None

    #==========================================================================
    def __init__(self, name, value, format, radix = DEC, vtype = ENG, units = "" ):
        self.name = name
        self.value = ValueClass(value, format, radix, vtype, units )

###############################################################################
class PtcItemClass(Configurable):

    """
    DESCRIPTION:
        Pseudo Telecommand item in charge of sending a command to the PCS

    TODO: 
        Review the class. Some methods/vars are not required anymore.
    """

    __ptcClass = None
    __parameters = []
    __cmdName = None
    __cmdDescription  = ""
    __executionStage  = ["UKN"]
    __executionStatus = ["UKN"]
    __updateTime      = [""]
    __comment         = [""]
    __elements        = ["ITEM"]
    __completed       = [False]
    __success         = [False]
    __subTcMap        = [{}]
    #==========================================================================
    def __init__(self, tcClass, cmd, description = ""):
        Configurable.__init__(self)
        self.__ptcClass = tcClass
        self.__cmdName = cmd
        self.__cmdDescription = description
        self.__parameters= []
        self.__completed       = [False]
        self.__success         = [False]
        self.__elements        = [ self.__cmdName ]
        self.__executionStage  = ['UKN']
        self.__executionStatus = ['UKN']
        self.__comment         = [" "]
        self.__updateTime      = [" "]
        self.__subTcMap        = [{}]
    #==========================================================================
    def name(self):
        return self.__cmdName

    #==========================================================================
    def _copy(self):
        itemCopy = PtcItemClass( self.__ptcClass, self.__cmdName, self.__cmdDescription )
        itemCopy._setParams(self.__parameters)
        itemCopy.setConfig( self.getConfig() )
        return itemCopy

    #==========================================================================
    def _setName(self, cmdName):
        self.__cmdName = cmdName

    #==========================================================================
    def desc(self):
        return self.__cmdDescription

    #==========================================================================
    def _setDescription(self, desc):
        self.__cmdDescription = desc

    #==========================================================================
    def clear(self):
        LOG("Item clear")
        self._reset()
        for p in self.__parameters:
            self.__parameters.remove(p)
        self.__parameters = []

    #==========================================================================
    def _getParams(self):
        return self.__parameters

    #==========================================================================
    def _setParams(self, parameters):
        self.__parameters = parameters[:]

    #==========================================================================
    def _reset(self):
        LOG("Item execution reset")
        self.__completed       = [False]*len(self.__completed)
        self.__success         = [False]*len(self.__success)
        self.__executionStage  = ["UKN"] + [" "]*(len(self.__executionStage)-1)
        self.__executionStatus = ["UKN"] + [" "]*(len(self.__executionStatus)-1)
        self.__comment         = [" "]   + [" "]*(len(self.__comment)-1)
        self.__updateTime      = [" "]   + [" "]*(len(self.__updateTime)-1)
        self.__subTcMap        = [{}]
        for i in range(0,len(self.__subTcMap)-1):
            self.__subTcMap += [{}]
    #==========================================================================
    def __extractConfig(self, dict, key, default):
        if key in dict:
            return dict.get(key)
        else:
            return default

    #==========================================================================
    def __setitem__(self, name, descList):
        if type(descList)!=list:
            # Value only has been given
            param = PtcItemParamClass(name, descList, ENG )
        else:
            value = descList[0]
            if len(descList)==2:
                if not type(descList[1]==dict):
                    raise SyntaxException("Malformed TC argument")
                argCfg = descList[1]
                format = self.__extractConfig(argCfg,ValueFormat,ENG)
                radix  = self.__extractConfig(argCfg,Radix,DEC)
                vtype   = self.__extractConfig(argCfg,ValueType,LONG)
                units  = self.__extractConfig(argCfg,Units,"")

                param = PtcItemParamClass(name, value,format,radix,vtype,units)
            else:
                # Only value given
                param = PtcItemParamClass(name, value, ENG )
        for p in self.__parameters:
            if (p.name == name):
                idx = self.__parameters.index(p)
                self.__parameters.pop(idx)
                self.__parameters.insert(idx, param)
                return
        self.__parameters.append(param)

    #==========================================================================
    def send(self):
        #TODO Review, which is tcClass ?
        return self.__ptcClass.PCS_Send(self, self.getConfig())

    #==========================================================================
    def configure(self, *args, **kargs ):
        config = self.buildConfig( args, kargs, self.__ptcClass.getConfig(), PTC_DEFAULTS)
        self.setConfig(config)

    #==========================================================================
    def _setElements(self, elements):
        elementIndex = 0
        self.__elements = [ self.__cmdName ]
        for element in elements:
            #self.__elements += [ str(elementIndex) + "@" + element ]
            self.__elements += [ element ]
            elementIndex += 1
        self.__executionStage  = ["UKN"] + [" "]*len(elements)
        self.__executionStatus = ["UKN"] + [" "]*len(elements)
        self.__comment         = [" "]   + [" "]*len(elements)
        self.__updateTime      = [" "]   + [" "]*len(elements)
        self.__completed       = [False] + [False]*len(elements)
        self.__success         = [False] + [False]*len(elements)
        self.__subTcMap        = [{}]
        for i in range(0,len(elements)):
            self.__subTcMap += [{}]
        LOG("Elements set: " + repr(self.__elements))

    def _setExecutionStageInformation(self, elementId,groupId,status):
        if elementId not in self.__elements:
            LOG("Adding new Element id:" + str(elementId) + " " +repr(self.__elements ))
            self.__elements        += [elementId]
            self.__executionStage  += ["UKN"]
            self.__executionStatus += ["UKN"]
            self.__comment         += [" "]
            self.__updateTime      += [" "]
            self.__completed       += [False]
            self.__success         += [False]
            self.__subTcMap        += {}
        idx = self.__elements.index(elementId)
        LOG("Update element information " + elementId + "#" + groupId + " with status " + str(status))
        self.__subTcMap[idx][groupId] = status
        self.__ptcClass._updateStatus(self)

    #==========================================================================
    def _setExecutionStageStatus(self, stage, status, comment="",elementId = None):
        try:
            updTime = str(datetime.datetime.now())[:-3]
            if elementId is None:
                LOG("Set item " + repr(self.__cmdName) + ' stage ' + repr(stage) + ' status ' + repr(status))
                self.__executionStage[0] = stage
                self.__executionStatus[0] = status
                self.__comment[0] = comment
                self.__updateTime[0] = updTime
                self.__subTcMap[0] = {}

            else:
                LOG("Update for element ID " + repr(elementId) + ":" + repr(self.__elements))
                if self.__executionStage[0] == "UKN":
                    self.__executionStage[0] = "Execution"
                    self.__executionStatus[0] = "Ongoing"
                if elementId not in self.__elements:
                    LOG("Adding new Element id:" + str(elementId) + " " +repr(self.__elements ))
                    self.__elements        += [elementId]
                    self.__executionStage  += ["UKN"]
                    self.__executionStatus += ["UKN"]
                    self.__comment         += [" "]
                    self.__updateTime      += [" "]
                    self.__completed       += [False]
                    self.__success         += [False]
                    self.__subTcMap        += {}
                idx = self.__elements.index(elementId)

                if (self.__completed[idx]==True): return
                LOG("Update element " + repr(self.__cmdName) + ":" + repr(elementId) + ' stage ' + repr(stage) + ' status ' + repr(status) + " with index " + repr(idx))
                self.__executionStage[idx] = stage
                self.__executionStatus[idx] = status
                self.__comment[idx] = comment
                self.__updateTime[idx] = updTime
            self.__ptcClass._updateStatus(self)
        except:
            import traceback
            error = traceback.format_exc()
            LOG(error)
            del traceback
    #==========================================================================
    def _setCompleted(self, success, elementId = None):
        try:
            updTime = str(datetime.datetime.now())[:-3]
            if elementId is None:
                LOG("Set item " + repr(self.__cmdName) + ' complete, success=' + repr(success))
                self.__completed[0] = True
                self.__success[0]   = success
                self.__updateTime[0] = updTime
            else:
                LOG("Set element " + repr(self.__cmdName) + ":" + repr(elementId) + ' completed, success='+ repr(success))
                LOG("C: " + repr(self.__completed))
                LOG("S: " + repr(self.__success))
                idx = self.__elements.index(elementId)
                LOG("The element index is " + repr(idx))
                self.__completed[idx] = True
                self.__success[idx]   = success
                self.__updateTime[idx]= updTime
                LOG("C: " + repr(self.__completed))
                LOG("S: " + repr(self.__success))
            self.__ptcClass._updateStatus(self)
        except:
            import traceback
            error = traceback.format_exc()
            LOG(error)
            del traceback
    #==========================================================================
    def getElements(self):
        return self.__elements[:]

    #==========================================================================
    def isComplex(self):
        return len(self.__elements)>2

    #==========================================================================
    def getExecutionStageStatus(self, elementId = None):
        if elementId is None:
            return [ self.__executionStage[0], self.__executionStatus[0] ]
        else:
            LOG("GetExcStatus: id="+str(elementId)+"\nelements=" + repr(self.__elements) + "\nstages=" + repr(self.__executionStage) + "\nstatuses="+repr(self.__executionStatus))

            idx = self.__elements.index(elementId)
            LOG("GetExcStatus: \nIdx=" + str(idx) + "\nstages=" + repr(self.__executionStage) + "\nstatuses="+repr(self.__executionStatus))
            return [ self.__executionStage[idx], self.__executionStatus[idx] ]

    #==========================================================================
    def getIsCompleted(self, elementId = None):
        if elementId is None:
            return self.__completed[0]
        else:
            idx = self.__elements.index(elementId)
            return self.__completed[idx]

    #==========================================================================
    def getIsSuccess(self, elementId = None):
        if elementId is None:
            return self.__success[0]
        else:
            idx = self.__elements.index(elementId)
            return self.__success[idx]

    #==========================================================================
    def getUpdateTime(self, elementId = None):
        if elementId is None:
            return self.__updateTime[0]
        else:
            idx = self.__elements.index(elementId)
            return self.__updateTime[idx]

    #==========================================================================
    def getComment(self, elementId = None):
        if elementId is None:
            return self.__comment[0]
        else:
            idx = self.__elements.index(elementId)
            return self.__comment[idx]

    #==========================================================================
    def getSubTCStatus(self, elementId = None):
        if elementId is None:
            return self.__subTcMap[0]
        else:
            idx = self.__elements.index(elementId)
            return self.__subTcMap[idx]

    #==========================================================================
    def __str__(self):
        res = self.name()
        if self.desc() != "": res = "C " + res + " " + self.desc()
        return res

    #==========================================================================
    def __repr__(self):
        return self.__str__()
