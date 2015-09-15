###################################################################################
## MODULE     : interface.model
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Simulator data model
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
from spell.lib.adapter.config import Configurable
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from loader import ModelLoader
from tm_sim_item import *
from tc_sim_item import *

#*******************************************************************************
# System imports
#*******************************************************************************
import time
import threading,thread,sys
from tm_sim_item import RAISE_EXCEPTION

#*******************************************************************************
# Import definition
#*******************************************************************************
__all__ = ['SimulatorModel']

#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class SimulatorModel(threading.Thread,Configurable):
    
    tmClass = None
    tcClass = None
    currentTime = None
    isWorking = True
    lock = None
    finishEvent = None
    tmItems = {}
    tcItems = {}
    gpItems = {}
    cfgItems = {}

    #===========================================================================    
    def __init__(self):
        threading.Thread.__init__(self)
        Configurable.__init__(self)
        self.tmClass = None
        self.tcClass = None
        self.currentTime = time.time()
        self.isWorking = True
        self.lock = thread.allocate_lock()
        self.finishEvent = threading.Event()
        self.finishEvent.clear()
        self.tmItems = {}
        self.tcItems = {}
        self.gpItems = {}
        self.cfgItems = {}
    
    #===========================================================================    
    def working(self, w = None):
        self.lock.acquire()
        if w is None:
            ret = self.isWorking
        else:
            self.isWorking = w
            ret = None
        self.lock.release()
        return ret
    
    #===========================================================================    
    def setup(self, defFile = None):
        # Load simulated items
        if defFile:
            self.load(defFile)
        self.start()

    #===========================================================================    
    def load(self, defFile):
        loader = ModelLoader(self)
        loader.loadFromFile(defFile)
    
    #===========================================================================    
    def cleanup(self):
        self.working(False)
        self.finishEvent.wait(2)
    
    #===========================================================================    
    def run(self):
        while (self.working()):
            time.sleep(1)
            self.lock.acquire()
            self.currentTime = time.time()
            for itemName in self.tmItems.keys():
                self.tmItems[itemName].refreshSimulatedValue()
            self.lock.release()
        self.finishEvent.set()
                
    def getCurrentTime(self):
        return self.currentTime
    
    #===========================================================================    
    def executeCommand(self, tcItemName):
        
        tcItem = self.getTCitem(tcItemName)
        
        changeDef = tcItem.getTmChange()
        
        if changeDef == RAISE_EXCEPTION:
            raise DriverException("Preconfigured command failure")
        
        tmItemNames = tcItem.getTmItemNames()

        time.sleep(tcItem.getExecTime())
        for tmItemName in tmItemNames:
            tmItem = self.getTMitem(tmItemName)
            self.lock.acquire()
            tmItem.change( changeDef )
            self.lock.release()

    #===========================================================================    
    def changeTMitem(self, tmItemName, value):
        tmItem = self.getTMitem(tmItemName)
        tmItem.change(value)

    #===========================================================================    
    def changeGPitem(self, gpItemName, value):
        gpItem = self.getGPitem(gpItemName)
        gpItem.change(value)

    #===========================================================================    
    def changeCFGitem(self, cfgItemName, value):
        self.getCFGitem(cfgItemName)
        self.gpItems[cfgItemName] = value

    #===========================================================================    
    def getTMitem(self, name):
        if not self.tmItems.has_key(name):
            raise DriverException("Unknown telemetry parameter: " + repr(name))
        else:
            tmItem = self.tmItems[name]
        return tmItem

    #===========================================================================    
    def isGPitem(self, name):
        return self.gpItems.has_key(name)

    #===========================================================================    
    def getGPitem(self, name):
        if not self.gpItems.has_key(name):
            raise DriverException("Unknown ground parameter: " + repr(name))
        else:
            gpItem = self.gpItems[name]
        return gpItem

    #===========================================================================    
    def getTCitem(self, name):
        if not self.tcItems.has_key(name):
            raise DriverException("Unknown telecommand: " + repr(name))
        else:
            tcItem = self.tcItems[name]
        return tcItem

    #===========================================================================    
    def getCFGitem(self, name):
        if not self.cfgItems.has_key(name):
            raise DriverException("Unknown GCS configuration parameter: " + repr(name))
        else:
            cfgItemValue = self.cfgItems[name]
        return cfgItemValue
