###################################################################################
## MODULE     : spell.lib.factory
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Driver factory
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
# SPELL Imports
#*******************************************************************************
import spell.lib.empty.config
import spell.lib.empty.tm
import spell.lib.empty.tc
import spell.lib.empty.ev
import spell.lib.empty.task
import spell.lib.empty.resources
import spell.lib.empty.ranging
import spell.lib.empty.memory
import spell.lib.empty.usr
import spell.lib.adapter.gcstime
from spell.lib.adapter.dbmgr import *

from spell.lib.adapter.constants.core import *
from spell.utils.log import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import sys
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
class FactoryError(BaseException): pass
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = ['Factory', 'FactoryError' ] 

KNOWN_INTERFACES = [ 'TM', 'TC', 'EV', 'RSC', 'TIME', 'TASK', 'USER', 'CONFIG', 'DBMGR', 'RNG', 'MEM' ]

IFC_PACKAGES = { 'TM':'tm', 'TC':'tc', 'EV':'ev', 'RNG':'ranging', 'MEM': 'memory',
                 'RSC':'resources', 'TIME':'gcstime', 'TASK':'task', 
                 'USER':'usr', 'CONFIG':'config' }

__instance__ = None

###############################################################################
class FactoryClass(object):
    
    """
    DESCRIPTION:
        This class is in charge of the instantiation of all proper driver
        classes which support the different adapter interfaces. 
    """
    __driver = None

    #==========================================================================
    def __init__(self):
        self.__driver = None
        
    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = FactoryClass()
        return __instance__
        
    #==========================================================================
    def setup(self, driver):
        """
        DESCRIPTION:
            Setup the factory for using the given driver
            
        ARGUMENTS:
            driver      Driver identifier
            
        RETURNS:
            Nothing

        RAISES:
            Nothing
        """
        LOG("Configure factory for using driver: " + driver)
        self.__driver = driver

    #==========================================================================
    def createGenInterface(self, ifcName):
        """
        DESCRIPTION:
            Create the configuration interface for the configured driver
            
        ARGUMENTS:
            
        RETURNS:
            The requested object

        RAISES:
            Nothing
        """
        LOG("Create " + ifcName + " interface")
        
        if not ifcName in KNOWN_INTERFACES:
            raise FactoryError("Unknown interface: " + repr(ifcName))
        
        try:
            # Import the required package
            LOG("Resolving %s" % ifcName)

            # Obtain the interface instance
            interface = eval(ifcName)
        except:
            LOG("Interface " + repr(ifcName) + 
                " could not be resolved.", severity = LOG_ERROR )
            return self.createEmptyInterface(ifcName)
        
        return interface            

    #==========================================================================
    def createInterface(self, ifcName):
        """
        DESCRIPTION:
            Create the configuration interface for the configured driver
            
        ARGUMENTS:
            
        RETURNS:
            The requested object

        RAISES:
            Nothing
        """
        LOG("Create " + ifcName + " interface")
        
        if not ifcName in KNOWN_INTERFACES:
            raise FactoryError("Unknown interface: " + repr(ifcName))
        
        # Build the full package name
        packageRoot = self.__driver + "." + IFC_PACKAGES.get(ifcName)
        
        try:
            # Import the required package
            LOG("Root " + packageRoot + ' with ' + ifcName)
            importedPackage = __import__(packageRoot, globals(),  locals(), [ifcName], -1)

            # Obtain the interface instance
            interface = importedPackage.__dict__.get(ifcName)
            
            if interface is None:
                raise FactoryError("Unable to create interface " + repr(ifcName) + " from '" + packageRoot + "'")

        except ImportError,err:
            LOG("Interface " + repr(ifcName) + 
                " is not available on driver: " + repr(err), severity = LOG_ERROR )
            return self.createEmptyInterface(ifcName)
        
        return interface            


    #==========================================================================
    def createEmptyInterface(self, ifcName):
        """
        DESCRIPTION:
            Create the empty interface 
            
        ARGUMENTS:
            
        RETURNS:
            The requested object

        RAISES:
            Nothing
        """
        LOG("Create " + ifcName + " EMPTY interface")
        if ifcName == 'CONFIG':
            return spell.lib.empty.config.ConfigInterface()
        elif ifcName == 'TM':
            return spell.lib.empty.tm.TmInterface()
        elif ifcName == 'TC':
            return spell.lib.empty.tc.TcInterface()
        elif ifcName == 'EV':
            return spell.lib.empty.ev.EvInterface()
        elif ifcName == 'TASK':
            return spell.lib.empty.task.TaskInterface()
        elif ifcName == 'USER':
            return spell.lib.empty.usr.UserInterface()
        elif ifcName == 'RSC':
            return spell.lib.empty.resources.ResourceInterface()
        elif ifcName == 'TIME':
            return spell.lib.adapter.gcstime.TimeInterface()
        elif ifcName == 'RNG':
            return spell.lib.empty.ranging.RngInterface()
        elif ifcName == 'MEM':
            return spell.lib.empty.memory.MemInterface()
        else:
            raise FactoryError("Cannot create empty interface '" + ifcName + "'")

###############################################################################
Factory = FactoryClass
