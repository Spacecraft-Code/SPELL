###################################################################################
## MODULE     : spell.config.contexts
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Context configuration model
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
from spell.lib.exception import *
from spell.utils.log import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from base import ConfigItem,ConfigError
from constants import *
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import xml.dom.minidom
import os,sys
from xml.dom.minidom import Node
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'ContextConfig' ]

# List of properties that should appear in a context entity
PROPERTIES = ['driver','spacecraft','satname','family','gcs','procpath','libpath', 'output_dir', 'input_dir', 'description']

################################################################################
class ContextConfig(ConfigItem):
    
    """
    Configuration entity for Contexts. 
    @see: ConfigItem for details.
    """
    
    # Holds the driver information (data below 'driverinfo' node)
    __driverInfo = {}
    # Holds the database information
    __databaseInfo = {}
    # Holds the database locations
    __databaseLocations = {}
    # Holds the language configuration
    __languageConfig = {}
    # Holds the function configuration
    __functionConfig = {}
    # Holds the executor configuration
    __executorConfig = {}
            
    #===========================================================================            
    def __init__(self, reader, configFile):
        if not os.path.exists(configFile):
            raise ConfigError("Cannot find context configuration file: " + configFile) 
        document = xml.dom.minidom.parse(configFile)
        node = document.getElementsByTagName(CONTEXT)[0]
        ConfigItem.__init__(self,node,PROPERTIES)
        self.__driverConfig = {}
        self.__databaseInfo = {}
        self.__databaseLocations = {}
        self.__languageConfig = {}
        self.__functionConfig = {}
        self.__executorConfig = {}
        self.__loadDriverConfig(node)
        self.__loadDatabases(node)
        self.__loadLanguage(reader, node)
        self.__loadExecutorConfig(reader, node)

    #===========================================================================            
    def getId(self):
        return self["id"]
        
    #===========================================================================            
    def getName(self):
        return self["name"]

    #===========================================================================            
    def getSC(self):
        return self["spacecraft"]

    #===========================================================================            
    def getSatName(self):
        return self["satname"]
    
    #===========================================================================            
    def getDriver(self):
        return self["driver"]

    #===========================================================================            
    def getFamily(self):
        return self["family"]
    
    #===========================================================================            
    def getGCS(self):
        return self["gcs"]

    #===========================================================================            
    def getProcPath(self):
        return self["procpath"]

    #===========================================================================            
    def getLibPath(self):
        return self["libpath"]

    #===========================================================================            
    def getOutputPath(self):
        return self["output_dir"]

    #===========================================================================            
    def getInputPath(self):
        return self["input_dir"]

    #===========================================================================            
    def getDescription(self):
        return self["description"]

    #===========================================================================            
    def getDriverParameter(self,key):
        if not self.__driverConfig.has_key(key):
            return None
        return self.__driverConfig[key]

    #===========================================================================            
    def getDriverParameterList(self):
        return self.__driverConfig.keys()[:]

    #===========================================================================            
    def getInterfaceConfig(self, interfaceName):
        return self.__languageConfig.get(interfaceName)

    #===========================================================================            
    def getFunctionConfig(self, functionName):
        return self.__functionConfig.get(functionName)

    #===========================================================================            
    def changeInterfaceConfig(self, interfaceName, modifier, value):
        LOG("Modified interface " + interfaceName + " configuration: " + modifier + "=" + repr(value))
        self.__languageConfig.get(interfaceName)[modifier] = value

    #===========================================================================            
    def changeFunctionConfig(self, functionName, modifier, value):
        LOG("Modified function " + functionName + " configuration: " + modifier + "=" + repr(value))
        self.__functionConfig.get(functionName)[modifier] = value

    #===========================================================================            
    def getExecutorConfig(self):
        return self.__executorConfig

    #===========================================================================            
    def getDatabaseInfo(self,dbName):
        if not self.__databaseInfo.has_key(dbName):
            return None
        return self.__databaseInfo[dbName]

    #==========================================================================
    def getLocationType(self, location):
        return self.__databaseLocations[location][0]

    #==========================================================================
    def getLocationExt(self, location):
        return self.__databaseLocations[location][1]

    #==========================================================================
    def getLocationPath(self, location):
        return self.__databaseLocations[location][2]

    #===========================================================================            
    def __loadDriverConfig(self, node):
        elements = node.getElementsByTagName("driverconfig")
        if elements is None or len(elements)==0:
            raise ConfigError("Could not find driver information for context")
        for driverinfo in elements:
            for property in driverinfo.getElementsByTagName("property"):
                name = str(property.getAttribute("name"))
                if name is None or len(name)==0:
                    raise ConfigError("Cannot find driverconfig property name")
                for pchild in property.childNodes:
                    if pchild.nodeType == Node.TEXT_NODE:
                        value = pchild.data
                        if value is None or len(value)==0:
                            raise ConfigError("Cannot find driverinfo property value")
                        self.__driverConfig[name] = str(value)
                        break

    #===========================================================================            
    def __loadDatabases(self, node):
        elements = node.getElementsByTagName("databases")
        if elements is None or len(elements)==0:
            raise ConfigError("Could not find database information for context")
        
        for database in elements:
            # Load database nodes
            for property in database.getElementsByTagName("database"):
                name = str(property.getAttribute("name"))
                location = str(property.getAttribute("location"))
                if name is None or len(name)==0:
                    raise ConfigError("Cannot find database name")
                if location is None or len(location)==0:
                    raise ConfigError("Cannot find database location")
                for pchild in property.childNodes:
                    if pchild.nodeType == Node.TEXT_NODE:
                        filename = pchild.data
                        if filename is None or len(filename)==0:
                            raise ConfigError("Cannot find db info property filename")
                        self.__databaseInfo[name] = [str(location),str(filename)]
                        break
            # Load location nodes
            for property in database.getElementsByTagName("location"):
                name = str(property.getAttribute("name"))
                type = str(property.getAttribute("type"))
                ext  = str(property.getAttribute("ext"))
                if name is None or len(name)==0:
                    raise ConfigError("Cannot find location name")
                if type is None or len(type)==0:
                    raise ConfigError("Cannot find location type")
                for pchild in property.childNodes:
                    if pchild.nodeType == Node.TEXT_NODE:
                        locationPath = pchild.data
                        if locationPath is None or len(locationPath)==0:
                            raise ConfigError("Cannot find location path")
                        self.__databaseLocations[name] = [type, ext, locationPath]
                        break
                    
       
    #===========================================================================            
    def __loadExecutorConfig(self, reader, node):
        elements = node.getElementsByTagName("executor")
        if elements is None or len(elements)==0:
            return
        self.__executorConfig = {}
        # First get the common defaults
        commons = reader.getSection(EXECUTOR)
        if commons:
            self.__executorConfig.update(commons)
            
        # Now override with context defaults
        for database in elements:
            for property in database.getElementsByTagName("property"):
                name = str(property.getAttribute("name"))
                if name is None or len(name)==0:
                    raise ConfigError("Cannot find property name")
                for pchild in property.childNodes:
                    if pchild.nodeType == Node.TEXT_NODE:
                        try:
                            value = eval(str(pchild.data))
                            self.__executorConfig[name] = value
                        except:
                            self.__executorConfig[name] = str(pchild.data)

#        for param in self.__executorConfig:
#            LOG("Executor parameter " + param + "=" + str(self.__executorConfig[param]), level = LOG_CNFG)
                
    #===========================================================================            
    def __loadLanguage(self, reader, node):
        elements = node.getElementsByTagName("language")
        self.__languageConfig = {}
        self.__functionConfig = {}
        
        # Obtain common defaults for interfaces
        commons = reader.getCommonLanguageConfig()
        if commons:
            self.__languageConfig.update(commons)
            
        # Get the common defaults for functions
        commons = reader.getCommonFunctionsConfig()
        if commons:
            self.__functionConfig.update(commons)
        # Now override the commons with the context specification
            
        # Now load functions and interfaces
        if elements is None or len(elements)==0:
            LOG("No language configuration in context " + self['id'], level = LOG_CNFG )
            return
        for language in elements:
            for defaults in language.getElementsByTagName("defaults"):
                interfaceName = str(defaults.getAttribute("interface"))
                functionName = str(defaults.getAttribute("function"))
                if interfaceName and len(interfaceName)>0:
                    self.__loadInterfaceDefaults( reader, interfaceName, defaults )
                elif functionName and len(functionName)>0: 
                    self.__loadFunctionDefaults( reader, functionName, defaults )
                else:
                    LOG("Unknown defaults: " + repr(defaults), LOG_WARN, level = LOG_CNFG )

#        for interface in self.__languageConfig:
#            LOG("Interface " + interface + " configuration:", level = LOG_CNFG)
#            for modifierName in self.__languageConfig[interface]:
#                LOG("   - " + modifierName + "=" + str(self.__languageConfig[interface][modifierName]), level = LOG_CNFG )
#        for func in self.__functionConfig:
#            LOG("Function " + func + " configuration:", level = LOG_CNFG)
#            for modifierName in self.__functionConfig[func]:
#                LOG("   - " + modifierName + "=" + str(self.__functionConfig[func][modifierName]), level = LOG_CNFG )

    #===========================================================================
    def __loadInterfaceDefaults(self, reader, name, defaults ):
        if not name in self.__languageConfig:
            self.__languageConfig[name] = {}

        modifiers = defaults.getElementsByTagName("modifier")
        for modifier in modifiers:
            modifierName = str(modifier.getAttribute("name"))
            for child in modifier.childNodes:
                if child.nodeType == Node.TEXT_NODE:
                    try:
                        value = eval(str(child.data))
                        self.__languageConfig[name][modifierName] = value
                    except:
                        LOG("   - Error in modifier definition: " + modifierName + "=" + str(child.data), LOG_ERROR, level = LOG_CNFG )

    #===========================================================================
    def __loadFunctionDefaults(self, reader, name, defaults ):
        if not name in self.__functionConfig:
            self.__functionConfig[name] = {}    

        modifiers = defaults.getElementsByTagName("modifier")
        for modifier in modifiers:
            modifierName = str(modifier.getAttribute("name"))
            for child in modifier.childNodes:
                if child.nodeType == Node.TEXT_NODE:
                    try:
                        value = eval(str(child.data))
                        self.__functionConfig[name][modifierName] = value
                    except:
                        LOG("   - Error in modifier definition: " + modifierName + "=" + str(child.data), LOG_ERROR, level = LOG_CNFG )

