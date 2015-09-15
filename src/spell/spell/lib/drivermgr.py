###################################################################################
## MODULE     : spell.lib.drivermgr
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Driver manager
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
from spell.config.reader import *
from spell.utils.log import *
from spell.lib.exception import DriverException

#*******************************************************************************
# Local Imports
#*******************************************************************************
from registry import *
from factory import *
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import traceback,sys
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

GENERIC_INTERFACES = [ 'DBMGR' ]
INTERNAL_INTERFACES = [ 'CONFIG' ]
HOME_TAG = "$SPELL_HOME$"
IFC_PACKAGES = { 'TM':'tm', 'TC':'tc', 'EV':'ev', 'MEM': 'memory',
                 'RSC':'resources', 'TASK':'task', 'RNG':'ranging',
                 'USER':'usr', 'CONFIG':'config', 'TIME':'gcstime' }

__all__ = [ 'DriverManager' ]

__instance__ = None

################################################################################
class DriverManagerClass(object):
    
    driverName = None
    driverConfig = None
    contextName = None
    contextConfig = None
    interfaces = []
    libraries = []
    driverPackage = None
    loaded = None
    
    #===========================================================================
    def __init__(self):
        self.driverName = None
        self.driverConfig = None
        self.interfaces = []
        self.libraries = []
        self.loaded = None
        self.driverPackage = None
        self.contextName = None
        self.contextConfig = None
    
    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = DriverManagerClass()
        return __instance__
    
    #===========================================================================
    def setup(self, contextName, specificInterfaces=None):

        # Get the context information
        self.contextName = contextName
        self.contextConfig = Config.instance().getContextConfig(contextName)
        # Obtain the corresponding driver 
        self.driverName = self.contextConfig.getDriver()
        self.driverConfig = Config.instance().getDriverConfig(self.driverName)
        
        REGISTRY['CTX'] = self.contextConfig

        if self.loaded is not None: 
            # Do not load the same driver twice
            if self.loaded == self.driverName: return True 
            success = self.cleanup()
            if not success:
                raise DriverException("Unable to load driver","Could not cleanup previously loaded driver " + str(self.loaded))
            # Remove previously imported driver modules
            self.cleanupCache()

        self.interfaces = []
        self.libraries = []
        self.driverPackage = None
        
        # Get interfaces defined by the driver
        self.interfaces.extend(INTERNAL_INTERFACES)

        if specificInterfaces is not None:
            driverInterfaces = specificInterfaces
        else:
            driverInterfaces = self.driverConfig.getInterfaces()

        if len(self.interfaces) > 0:
            self.interfaces.extend(driverInterfaces.split(","))

        # Get extra libraries required by the driver
        libraries = self.driverConfig.getLibraries()
        if len(libraries)>0:
            self.libraries = libraries.split(",")
        
        # Get the driver package path
        self.driverPackage = self.driverConfig.getPackagePath()
        # Resolve the path
        self.driverPackage = Config.instance().resolvePath(self.driverPackage)

        if self.driverPackage is None or len(self.driverPackage)==0:
            raise DriverException("Unable to load driver", "Driver package path not defined")
        
        LOG("Using driver    :" + self.driverName)
        LOG("Package         :" + self.driverPackage)
        LOG("Using interfaces: " + repr(self.interfaces))
        LOG("Using libraries : " + repr(self.libraries))

        try:         
            # Configure the factory
            Factory.instance().setup(self.driverName)
            
            # Preload libraries
            for lib in self.libraries:
                lib = lib.strip(" \n\r")
                lib = Config.resolvePath(lib)
                LOG("Appending to path: " + lib)
                sys.path.append(lib)
                
            # Put the driver package in first place of the path
            LOG("Adding drivers base path: " + repr(self.driverPackage))
            sys.path.append(self.driverPackage)
                
            # Create driver interfaces
            self.createInterfaces()
            # Initialize driver interfaces
            self.initInterfaces()
            # Set the driver as loaded
            self.loaded = self.driverName
            
        except DriverException,e:
            traceback.print_exc(file = sys.stderr)
            raise e
        except BaseException,e:
            traceback.print_exc(file = sys.stderr)
            raise DriverException("Unable to load driver",repr(e))

    #==========================================================================
    def cleanup(self, force = False, shutdown = False ):
        list = []
        for ifc in self.interfaces:
            list.append(ifc)
        list.reverse()
        # Forcing will try to cleanup everything ignoring errors
        if force:
            for ifc in list:
                # Attempt to clean all interfaces,
                # whatever it happens
                if REGISTRY.exists(ifc):
                    try:
                        LOG("Cleaning up driver interface " + ifc)
                        if ifc == 'CONFIG':
                            REGISTRY[ifc].cleanup(shutdown)
                        else:
                            REGISTRY[ifc].cleanup()
                    except:pass
        else:
            for ifc in list:
                if REGISTRY.exists(ifc):
                    LOG("Cleaning up driver interface " + ifc)
                    if ifc == 'CONFIG':
                        REGISTRY[ifc].cleanup(shutdown)
                    else:
                        REGISTRY[ifc].cleanup()
        self.loaded = None
        return True
    
    #===========================================================================
    def cleanupCache(self):
        # Remove previously imported driver packages if any
        for path in sys.path_importer_cache.keys()[:]:
            if self.driverPackage in path:
                del sys.path_importer_cache[path]
        for moduleName in sys.modules.keys()[:]:
            moduleObj = sys.modules[moduleName]
            if moduleObj and hasattr(moduleObj, "__file__"):
                moduleFile = moduleObj.__file__
                if self.driverPackage in moduleFile:
                    del sys.modules[moduleName]
        # Remove previous package path if any
        if self.driverPackage in sys.path[0]:
            del sys.path[0]

    #==========================================================================
    def createInterfaces(self):
        LOG("Creating interfaces")
        for ifc in self.interfaces:
            REGISTRY[ifc] = Factory.instance().createInterface(ifc)
            #module = __import__( IFC_PACKAGES[ifc] )
            #REGISTRY[ifc] = module.__dict__[ifc]
        LOG("Creating generic interfaces")
        for ifc in GENERIC_INTERFACES:
            REGISTRY[ifc] = Factory.instance().createGenInterface(ifc)
            self.interfaces.append(ifc)

    #==========================================================================
    def initInterfaces(self):
        LOG("Initializing interfaces")
        for ifc in self.interfaces:
            LOG("Initializing " + ifc)
            REGISTRY[ifc].setup(self.contextConfig,self.driverConfig)

    #==========================================================================
    def onCommand(self, commandId):
        for ifc in self.interfaces:
            obj = REGISTRY[ifc] 
            if 'onCommand' in dir(obj):
                obj.onCommand(commandId)
        return

################################################################################
DriverManager = DriverManagerClass
