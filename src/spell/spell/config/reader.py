###################################################################################
## MODULE     : spell.config.reader
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Configuration reader
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
from spell.lang.modifiers import *
from spell.lang.constants import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from base import PropertyList, ConfigError
from constants import *
from drivers import DriverConfig
from contexts import ContextConfig
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import xml.dom.minidom
from xml.dom.minidom import Node
import os,sys,traceback
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'Config' ]

__instance__ = None

###############################################################################
class ConfigReaderClass(object):
    
    """
    This class is in charge of loading a configuration file, parse it and
    store/provide all the configuration information of the SPELL aplication.
    
    This class should not be instantiated, but the module-global variable CFG
    should be imported.
    """
    
    # Holds the configuration file name
    filename = None
    # Holds the XML document
    document = None
    # Holds the map of property lists. There is a property list per defined
    # section (see constants.CONFIG_SECTIONS)    
    properties = {}
    # Holds the map of known drivers (driver name is the key)
    drivers = {}
    # Holds the map of knwon contextes (name is the key)
    contexts = {}
    # Holds the common language configuration
    language = {}
    # Holds the common function configuration
    functions = {}
    # Holds the spacecraft families
    families = {}
    # Boolean flag, true if the file was successfully parsed
    readOk = False
        
    #==========================================================================
    def __init__(self):
        self.filename = None
        self.document = None
        self.properties = {}
        self.drivers = {}
        self.language = {}
        self.functions = {}
        self.contexts = {}
        self.readOk = False

    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = ConfigReaderClass()
        return __instance__

    #==========================================================================
    def load(self, configFile):
        self.readOk = True
        self.filename = configFile
        try:
            if not os.path.exists(configFile):
                raise ConfigError("Unable to load configuration", "Cannot find configuration file: '" + configFile + "'")
            try:
                self.document = xml.dom.minidom.parse(configFile)
            except IOError,e:
                raise ConfigError("Unable to load configuration", "Could not load configuration file: " +  configFile + ":" + str(e))
    
            # Load the configuration sections
            for section in CONFIG_SECTIONS:
                LOG("Loading properties for section " + section, level = LOG_CNFG)
                self.loadSection(section)

            # Load the language
            self.loadLanguage()
                
            # Load the drivers
            self.loadDrivers()
            
            # Load the contexts
            self.loadContexts()
            
            # Load the spacecraft families
            self.loadFamilies()

        except ConfigError,ex:
            traceback.print_exc()
            self.readOk = False
            raise ConfigError(ex)
            
    #==========================================================================
    def loadSection(self, sectionName):
        count = 0
        for node in self.document.getElementsByTagName(sectionName):
            if count > 0: break
            count = count + 1
            
            self.properties[sectionName] = PropertyList()
            
            for property in node.childNodes:
                if property.nodeType == Node.ELEMENT_NODE:
                    name = str(property.getAttribute("name"))
                    for child in property.childNodes:
                        if child.nodeType == Node.TEXT_NODE:
                            value = str(child.data)
                    self.properties[sectionName][name] = value

    #==========================================================================
    def loadLanguage(self):
        count = 0
        for node in self.document.getElementsByTagName(LANGUAGE):
            if count > 0: break
            count = count + 1
            
            languageFiles = []
            for property in node.childNodes:
                if property.nodeType == Node.ELEMENT_NODE:
                    for child in property.childNodes:
                        if child.nodeType == Node.TEXT_NODE:
                            path = str(child.data)
                            config_dir = os.getenv("SPELL_CONFIG", Config.getHome() + os.sep + 'config')
                            dirname = config_dir + os.sep + 'spell' 
                            langFile = dirname + os.sep + path
                            if os.path.exists(langFile):
                                LOG("   - Language config: " + str(path), level = LOG_CNFG)
                                languageFiles += [langFile]
                            else:
                                LOG("   - Cannot find language config: " + str(path), LOG_ERROR, level = LOG_CNFG)
                    
        for langFile in languageFiles:
            lang_document = xml.dom.minidom.parse(langFile)
            for node in lang_document.getElementsByTagName(LANGUAGE):
                for defaults in node.childNodes:
                    if defaults.nodeType == Node.ELEMENT_NODE:
                        interfaceName = defaults.getAttribute('interface')
                        functionName = defaults.getAttribute('function')
                        if len(interfaceName)>0:
                            LOG("   - Loading defaults for interface " + interfaceName, level = LOG_CNFG)
                            self.language[interfaceName] = {}
                        elif len(functionName)>0:
                            LOG("   - Loading defaults for function " + functionName, level = LOG_CNFG)
                            self.functions[functionName] = {}
                        for modifier in defaults.childNodes:
                            if modifier.nodeType == Node.ELEMENT_NODE:
                                modifierName = str(modifier.getAttribute('name'))
                                for child in modifier.childNodes:
                                    if child.nodeType == Node.TEXT_NODE:
                                        try:
                                            value = eval(str(child.data))
                                            if len(interfaceName)>0:
                                                self.language[interfaceName][modifierName] = value
                                            elif len(functionName)>0:
                                                self.functions[functionName][modifierName] = value
                                        except:
                                            LOG("   - Error in modifier definition: " + modifierName, LOG_ERROR, level = LOG_CNFG)
                                
    #==========================================================================
    def loadDrivers(self):
        count = 0
        for node in self.document.getElementsByTagName(DRIVERS):
            if count > 0: break
            count = count + 1

            for driver in node.childNodes:
                if driver.nodeType == Node.ELEMENT_NODE:
                    for child in node.childNodes:
                        if child.nodeType == Node.ELEMENT_NODE:
                            for n in child.childNodes:
                                if n.nodeType == Node.TEXT_NODE:
                                    driverFile = str(n.data)
                            LOG("   - Loading driver file: " + driverFile, level = LOG_CNFG)
                            config_dir = os.getenv("SPELL_CONFIG", Config.getHome() + os.sep + 'config')
                            dirname = config_dir + os.sep + 'spell' 
                            driverFile = dirname + os.sep + driverFile
                            driverConfig = DriverConfig(driverFile)
                            self.drivers[driverConfig.getId()] = driverConfig
                            LOG("Registered driver " + driverConfig.getId(), level = LOG_CNFG)

    #==========================================================================
    def loadContexts(self):
        count = 0
        for node in self.document.getElementsByTagName(CONTEXTS):
            if count > 0: break
            count = count + 1
            
            for ctx in node.childNodes:
                if ctx.nodeType == Node.ELEMENT_NODE:
                    for child in node.childNodes:
                        if child.nodeType == Node.ELEMENT_NODE:
                            for n in child.childNodes:
                                if n.nodeType == Node.TEXT_NODE:
                                    contextFile = str(n.data)
                            LOG("    - Loading context file: " + contextFile, level = LOG_CNFG)
                            config_dir = os.getenv("SPELL_CONFIG", Config.getHome() + os.sep + 'config')
                            dirname = config_dir + os.sep + 'contexts' 
                            contextFile = dirname + os.sep + contextFile
                            contextConfig = ContextConfig(self,contextFile)
                            self.contexts[contextConfig.getId()] = contextConfig
                            LOG("Registered context " + contextConfig.getId(), level = LOG_CNFG)

    #==========================================================================
    def loadFamilies(self):
        count = 0
        for node in self.document.getElementsByTagName(FAMILIES):
            if count > 0: break
            count = count + 1
            
            for family in node.childNodes:
                if family.nodeType == Node.ELEMENT_NODE:
                    name = str(family.getAttribute("name"))
                    list = []
                    for child in family.childNodes:
                        if child.nodeType == Node.TEXT_NODE:
                            list = str(child.data)
                    if list and len(list)>0: list = list.split(",")
                    else: list = []
                    self.families[name] = list

    #==========================================================================
    def getAvailableContexts(self):
        return self.contexts.keys()

    #==========================================================================
    def getContextConfig(self, key):
        if not key in self.contexts:
            raise ConfigError("Unable to get context information", "No such context: " + str(key))
        return self.contexts[key]

    #==========================================================================
    def getAvailableDrivers(self):
        return self.drivers.keys()

    #==========================================================================
    def getCommonLanguageConfig(self):
        return self.language

    #==========================================================================
    def getCommonFunctionsConfig(self):
        return self.functions
          
    #==========================================================================
    def getDriverConfig(self, key):
        if not key in self.drivers:
            raise ConfigError("Unable to get driver information, no such driver: " + str(key))
        return self.drivers[key]

    #==========================================================================
    def getProperty(self, section, key):
        sect = self.properties.get(section)
        if sect:
            return sect.get(key)
        else:
            return None

    #==========================================================================
    def getSection(self, section):
        return self.properties.get(section).all()

    #==========================================================================
    def getSpacecraftFamily(self, sc):
        family = self.families.get(sc)
        if family:
            return family[:]
        return None

    #==========================================================================
    def validate(self):
        if not self.readOk:         return False
        if len(self.drivers)==0:    return False
        if len(self.contexts)==0:   return False
        return True

    #==========================================================================
    @staticmethod
    def getHome():
        home = os.getenv("SPELL_HOME")
        if home is None or len(home)=="":
            raise ConfigError("Cannot get home", "SPELL_HOME environment variable not defined.")
        return home

    #==========================================================================
    @staticmethod
    def getUserDataDir():
        data = os.getenv("SPELL_DATA", Config.getHome() + os.sep + "data")
        if data is None or len(data)=="":
            raise ConfigError("Cannot get data home", "SPELL_DATA environment variable not defined.")
        return data

    #==========================================================================
    @staticmethod
    def getRuntimeDir():
        data = os.getenv("SPELL_SYS_DATA", Config.getHome() + os.sep + "data")
        if data is None or len(data)=="":
            raise ConfigError("Cannot get runtime home", "SPELL_SYS_DATA environment variable not defined.")
        return data

    #==========================================================================
    @staticmethod
    def resolvePath(path):
        path = path.replace("\\", os.sep)
        path = path.replace("/", os.sep)
        if "$" in path:
            items = path.split(os.sep)
            final = []
            for item in items:
                if item.startswith("$"):
                    item = os.getenv(item[1:], "")
                final += [item]
            path = os.sep.join(final)
            return path
        else:
            return path
        
###############################################################################
# Singleton instance of the configuration reader
Config = ConfigReaderClass                    
                    
###############################################################################
# For testing purposes
if __name__ == "__main__":
    
    Config.instance().load("config_test.xml")
    ctxs = Config.instance().getAvailableContexts()
    print ctxs
    drvs = Config.instance().getAvailableDrivers()
    print drvs
    print
    for ctx in ctxs:
        print "----------------------------------"
        print Config.instance().getContextConfig(ctx)
    print
    for drv in drvs:
        print "----------------------------------"
        print Config.instance().getDriverConfig(drv)
                         
