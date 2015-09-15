###################################################################################
## MODULE     : spell.config.base
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Base classes for configuration
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
from spell.lib.exception import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import xml.dom.minidom
from xml.dom.minidom import Node
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
class ConfigError(SpellException): pass
 
#*******************************************************************************
# Module globals
#*******************************************************************************


################################################################################
class PropertyList(object):
    """
    Generic class for storing properties (key/value pairs) easily
    """
    
    # Holds the property map
    __properties = {}
    
    #===========================================================================
    def __init__(self):
        self.__properties = {}
        
    #===========================================================================
    def __setitem__(self, key, value):
        self.__properties[key] = value
        
    #===========================================================================
    def __getitem__(self, key):
        if not self.__properties.has_key(key):
            return None
        return self.__properties.get(key)
    
    #===========================================================================
    def get(self, key):
        return self.__properties.get(key)

    #===========================================================================
    def keys(self):
        return self.__properties.keys()

    #===========================================================================
    def all(self):
        return self.__properties

################################################################################
class ConfigItem(PropertyList):

    """
    Base class for each configuration entity. Each configuration entity has the 
    form
            <node name="???" >
                <property1>...</property1>
                <property2>...</property2>
                <property3>...</property3>
                <complex_property>
                    ...
                </complex_property>
            </node>
            
    The __parseName() method extracts the 'name' attribute of the node. The
    __parseProperties() method receives a list of known property names (in the
    example, this list should be [ 'property1', 'property2', 'property3' ]).
    All the node names in this list will become properties of the configuration
    item. Other XML child nodes like 'complex_property' will be ignored by
    these methods, and are suposed to be parsed by child classes.
    """
    
    #===========================================================================
    def __init__(self, node, properties):
        PropertyList.__init__(self)
        self.__parseId(node)
        self.__parseProperties(node,properties)

    #===========================================================================
    def __parseId(self, node):
        value = str(node.getAttribute("id"))
        if value is None or len(value)==0:
            raise ConfigError("Could not parse item id")
        self["id"] = str(value)
    
    #===========================================================================
    def __parseProperties(self, node, properties):
        for propertyName in properties:
            foundProperty = False
            for child in node.childNodes:
                if (child.nodeType == Node.ELEMENT_NODE and
                    child.nodeName == propertyName):
                    
                    for schild in child.childNodes:
                        if schild.nodeType == Node.TEXT_NODE:
                            value = str(schild.data)
                            break
                    if value is None or len(value)==0:
                        raise ConfigError("Missing value for " + propertyName)
                    self[propertyName] = str(value)
                    foundProperty = True
                    break
            if not foundProperty:
                LOG("WARNING: Did not find property: '" + propertyName + "' on item '" + self['id'] + "'", level = LOG_CNFG )
            