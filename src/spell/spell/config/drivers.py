###################################################################################
## MODULE     : spell.config.drivers
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Driver configuration model
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
from base import ConfigItem, ConfigError
from constants import *
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import os
import xml.dom.minidom
from xml.dom.minidom import Node
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'DriverConfig' ]

PROPERTIES=['name', 'interfaces', 'lib', 'maxproc', 'path']

################################################################################
class DriverConfig(ConfigItem):
    
    """
    Configuration entity for Drivers. 
    @see: ConfigItem for details.
    """
    
    #===========================================================================            
    def __init__(self, configFile):
        if not os.path.exists(configFile):
            raise ConfigError("Cannot find driver configuration file: " + configFile) 
        document = xml.dom.minidom.parse(configFile)
        node = document.getElementsByTagName(DRIVER)[0]
        ConfigItem.__init__(self,node, PROPERTIES)
        # Load the driver properties node
        self.__loadDriverProperties(node)
        
    #===========================================================================            
    def getName(self):
        return self["name"]
    
    #===========================================================================            
    def getId(self):
        return self["id"]

    #===========================================================================            
    def getInterfaces(self):
        ifc = self['interfaces']
        if ifc is None:
            return ""
        return ifc

    #===========================================================================            
    def getLibraries(self):
        lib = self['lib']
        if lib is None:
            return ""
        return lib

    #===========================================================================
    def getPackagePath(self):
        path = self['path']
        if path is None:
            return ""
        return path

    #===========================================================================            
    def getMaxProcs(self):
        maxp = self['maxproc']
        if maxp is None:
            return 10
        return maxp
    
    #===========================================================================            
    def __loadDriverProperties(self, node):
        for properties in node.getElementsByTagName("properties"):
            
            for property in properties.getElementsByTagName("property"):
                name = str(property.getAttribute("name"))
                if name is None or len(name)==0:
                    raise ConfigError("Cannot find driver property name")
                for pchild in property.childNodes:
                    if pchild.nodeType == Node.TEXT_NODE:
                        value = pchild.data
                        if value is None or len(value)==0:
                            raise ConfigError("Cannot find driver property value")
                        self[name] = str(value)
                        break

