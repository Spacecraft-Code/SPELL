###################################################################################
## MODULE     : config
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Configuration interface of the driver connection layer
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
from spell.lib.registry import REGISTRY
from spell.lib.exception import DriverException
from spell.config.reader import Config

#*******************************************************************************
# Local Imports
#*******************************************************************************
from interface.model import SimulatorModel

#*******************************************************************************
# System Imports
#*******************************************************************************
import os

###############################################################################
# Module import definition

__all__ = ['CONFIG']

INTERFACE_DEFAULTS = {}

###############################################################################
# Superclass
import spell.lib.adapter.config
superClass = spell.lib.adapter.config.ConfigInterface
        
###############################################################################
class ConfigInterface(superClass):

    __ready = False

    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        self.__ready = False
        LOG("Created")
    
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)

        SIM = SimulatorModel()
        REGISTRY['SIM'] = SIM
        
        LOG("Setup standalone CFG interface")
        dataPath = Config.getRuntimeDir()
        driverConfig = self.getDriverConfig()
        simulationPath = driverConfig['SimPath']
        simulationFile = self.getContextConfig().getDriverParameter('Simulation')
        home = Config.getHome()
        
        if home is None:
            raise DriverException("SPELL home is not defined")
        
        LOG("Loading simulation: " + simulationFile)
        simulationFile = dataPath + os.sep +  simulationPath + \
                         os.sep + simulationFile
                         
        SIM.tmClass = REGISTRY['TM']
        SIM.tcClass = REGISTRY['TC']
        SIM.setup( simulationFile )
        
        self.__ready = True

    #==========================================================================
    def cleanup(self, shutdown = False):
        if self.__ready:
            superClass.cleanup(self, shutdown)
            LOG("Cleanup standalone CFG interface")
            REGISTRY['SIM'].cleanup()
            REGISTRY.remove('SIM')
            self.__ready = False
                
###############################################################################
# Interface instance
CONFIG = ConfigInterface()
