###################################################################################
## MODULE     : interface.loader
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Simulation data model loaded
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
import sys
import traceback

from spell.utils.log import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from tm_sim_item import *
from tc_sim_item import *

#*******************************************************************************
# System imports
#*******************************************************************************
import xml.dom.minidom
from xml.dom.minidom import Node
from tm_sim_item import UPDATE_ACQUISITION, UPDATE_NONE, UPDATE_TIME

#*******************************************************************************
# Import definition
#*******************************************************************************
__all__ = ['ModelLoader']

#*******************************************************************************
# Module globals
#*******************************************************************************
TAG_TELEMETRY   = 'telemetry'
TAG_TELECOMMAND = 'telecommand'
TAG_GROUND      = 'ground'
TAG_RESOURCE    = 'resource'
ATTR_NAME       = 'name'
ATTR_DESC       = 'description'
ATTR_TYPE       = 'type'
ATTR_PERIOD     = 'period'
ATTR_VALUE      = 'value'
ATTR_DEFAULT    = 'default'
TAG_EXEC_TIME   = 'exec_time'
TAG_UPDATE      = 'update'
TAG_ITEM        = 'item'
TAG_ARGUMENTS   = 'arguments'
TAG_ARGUMENT    = 'argument'
TAG_RAW         = 'raw'
TAG_ENG         = 'eng'
VALUE_ACQ       = 'acq'
VALUE_TIMER     = 'timer'
VALUE_NONE      = 'none'

################################################################################
class ModelLoader(object):

    document = None    
    model = None

    #===========================================================================    
    def __init__(self, model):
        self.document = None
        self.model = model
        
    #===========================================================================    
    def loadFromFile(self, configFile):
        
        try:
            self.document = xml.dom.minidom.parse(configFile)
        except BaseException,e:
            traceback.print_exc(file=sys.stderr)
            raise RuntimeError("Unable to load configuration", "Could not load configuration file: " +  configFile + ":" + str(e))

        sys.stderr.write("LOADING SIMULATED ITEMS\n")
        self.__loadTelemetry()
        self.__loadTelecommands()
        self.__loadGroundParameters()
        self.__loadConfigurationResources()
        
        return

    #===========================================================================    
    def __loadTelemetry(self):
        sys.stderr.write("[**] Telemetry:\n")
        count = 0
        for node in self.document.getElementsByTagName(TAG_TELEMETRY):
            
            # Process only the first section in the file
            if count > 0: break
            count = count + 1
            
            for child in node.childNodes:
                
                try:
                    if child.nodeType == Node.ELEMENT_NODE:
                        mnemonic = str(child.getAttribute(ATTR_NAME))
                        desc = str(child.getAttribute(ATTR_DESC))
                        rawExpression = None
                        engExpression = None
                        updateType = None
                        updatePeriod = None
                        for child2 in child.childNodes:
                            if child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_RAW:
                                # Read the raw expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.TEXT_NODE:
                                        rawExpression = str(child3.data)
                                        break
                            elif child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_ENG:
                                # Read the eng expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.TEXT_NODE:
                                        engExpression = str(child3.data)
                                        break
                            elif child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_UPDATE:
                                # Get the update type
                                updateType = str(child2.getAttribute(ATTR_TYPE))
                                if updateType == VALUE_ACQ:
                                    updateType = UPDATE_ACQUISITION
                                elif updateType == VALUE_TIMER:
                                    updateType = UPDATE_TIME
                                else: 
                                    updateType = UPDATE_NONE
                                    
                                if (child2.hasAttribute(ATTR_PERIOD)):
                                    updatePeriod = int(child2.getAttribute(ATTR_PERIOD))
                                else:
                                    updatePeriod = None
                                    
                        tm_item = TmItemSimClass(self.model,mnemonic,desc,rawExpression,engExpression,updateType,updatePeriod)
                        self.model.tmItems[mnemonic] = tm_item
                        sys.stderr.write("Loaded: " + str(tm_item) + "\n")
                except BaseException,ex:
                    traceback.print_exc( file = sys.stderr )
                    sys.stderr.write("Failed to process TM element in XML: " + repr(child) + "\n" )

        return

    #===========================================================================    
    def __loadTelecommands(self):
        sys.stderr.write("[**] Telecommands:\n")
        count = 0
        for node in self.document.getElementsByTagName(TAG_TELECOMMAND):
            
            # Process only the first section in the file
            if count > 0: break
            count = count + 1
            
            for child in node.childNodes:
                
                try:
                    if child.nodeType == Node.ELEMENT_NODE:
                        mnemonic = str(child.getAttribute(ATTR_NAME))
                        desc = str(child.getAttribute(ATTR_DESC))
                        updateExpression = None
                        execTime = None
                        tmPoints = []
                        arguments = {}
                        for child2 in child.childNodes:
                            if child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_UPDATE:
                                # Read the update expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.TEXT_NODE:
                                        updateExpression = str(child3.data)
                                        break
                            elif child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_EXEC_TIME:
                                # Read the eng expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.TEXT_NODE:
                                        execTime = int(str(child3.data))
                                        break
                            elif child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_TELEMETRY:
                                # Read the tm parameters expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.ELEMENT_NODE and child3.nodeName == TAG_ITEM:
                                        tmPoints.append( str(child3.getAttribute(ATTR_NAME)) )
                            elif child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_ARGUMENTS:
                                # Read the tm parameters expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.ELEMENT_NODE and child3.nodeName == TAG_ARGUMENT:
                                        argName = str(child3.getAttribute(ATTR_NAME))
                                        argDefault = str(child3.getAttribute(ATTR_DEFAULT))
                                        arguments[argName] = argDefault
                                    
                        tc_item = TcItemSimClass(self.model,mnemonic,desc,tmPoints,updateExpression,execTime)
                        for arg in arguments.keys():
                            tc_item[arg] = arguments.get(arg)
                        self.model.tcItems[mnemonic] = tc_item
                        sys.stderr.write("Loaded: " + str(tc_item) + "\n")
                except BaseException,ex:
                    traceback.print_exc( file = sys.stderr )
                    sys.stderr.write("Failed to process TC element in XML: " + repr(child) + "\n" )

        return

    #===========================================================================    
    def __loadGroundParameters(self):
        sys.stderr.write("[**] Ground parameters:\n")
        count = 0
        for node in self.document.getElementsByTagName(TAG_GROUND):
            
            # Process only the first section in the file
            if count > 0: break
            count = count + 1
            
            for child in node.childNodes:
                
                try:
                    if child.nodeType == Node.ELEMENT_NODE:
                        mnemonic = str(child.getAttribute(ATTR_NAME))
                        desc = str(child.getAttribute(ATTR_DESC))
                        rawExpression = None
                        engExpression = None
                        updateType = None
                        updatePeriod = None
                        for child2 in child.childNodes:
                            if child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_RAW:
                                # Read the raw expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.TEXT_NODE:
                                        rawExpression = str(child3.data)
                                        break
                            elif child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_ENG:
                                # Read the eng expression
                                for child3 in child2.childNodes:
                                    if child3.nodeType == Node.TEXT_NODE:
                                        engExpression = str(child3.data)
                                        break
                            elif child2.nodeType == Node.ELEMENT_NODE and child2.nodeName == TAG_UPDATE:
                                # Get the update type
                                updateType = str(child2.getAttribute(ATTR_TYPE))
                                if updateType == VALUE_ACQ:
                                    updateType = UPDATE_ACQUISITION
                                elif updateType == VALUE_TIMER:
                                    updateType = UPDATE_TIME
                                else: 
                                    updateType = UPDATE_NONE
                                    
                                if (child2.hasAttribute(ATTR_PERIOD)):
                                    updatePeriod = int(child2.getAttribute(ATTR_PERIOD))
                                else:
                                    updatePeriod = None
                                    
                        gp_item = TmItemSimClass(self.model,mnemonic,desc,rawExpression,engExpression,updateType,updatePeriod)
                        self.model.gpItems[mnemonic] = gp_item
                        sys.stderr.write("Loaded: " + str(gp_item) + "\n")
                except BaseException,ex:
                    traceback.print_exc( file = sys.stderr )
                    sys.stderr.write("Failed to process GP element in XML: " + repr(child) + "\n" )

        return

    #===========================================================================    
    def __loadConfigurationResources(self):
        sys.stderr.write("[**] Configuration resources:\n")
        count = 0
        for node in self.document.getElementsByTagName(TAG_RESOURCE):
            
            # Process only the first section in the file
            if count > 0: break
            count = count + 1
            
            for child in node.childNodes:
                
                try:
                    if child.nodeType == Node.ELEMENT_NODE:
                        mnemonic = str(child.getAttribute(ATTR_NAME))
                        value = str(child.getAttribute(ATTR_VALUE))
                        self.model.cfgItems[mnemonic] = eval("\"" + value + "\"")
                        sys.stderr.write("Loaded: " + "[CFG=" + repr(mnemonic) + ", VAL=" + repr(value) + "]\n")
                        
                except BaseException,ex:
                    traceback.print_exc( file = sys.stderr )
                    sys.stderr.write("Failed to process CFG element in XML: " + repr(child) + "\n" )

        return


################################################################################
