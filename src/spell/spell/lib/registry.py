###################################################################################
## MODULE     : spell.lib.registry
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Adapter components registry
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
class RegistryError(BaseException): pass
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'REGISTRY', 'RegistryError' ]

__instance__ = None

################################################################################
class RegistryClass(object):
    
    __objects = {}
    
    #===========================================================================
    def __init__(self):
        self.__objects  = {}
        
    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = RegistryClass()
        return __instance__
        
    #===========================================================================
    def __getitem__(self, key):
        if not self.__objects.has_key(key):
            raise RegistryError("No such object: " + repr(key))
        return self.__objects.get(key)
        
    #===========================================================================
    def __setitem__(self, key, object):
        self.__objects[key] = object

    #===========================================================================
    def exists(self, key):
        return self.__objects.has_key(key)

    #===========================================================================
    def remove(self, key):
        if self.__objects.has_key(key):
            self.__objects.pop(key)

    #===========================================================================
    def interfaces(self):
        return self.__objects.keys()
    
################################################################################
REGISTRY = RegistryClass.instance()