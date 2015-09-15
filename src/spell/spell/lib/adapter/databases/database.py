###################################################################################
## MODULE     : spell.lib.adapter.databases.database
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Database base class
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

import os,sys
from spell.lib.exception import DriverException
from spell.utils.log import *
from spell.config.reader import *
from spell.config.constants import COMMON
from spell.utils.vimport import ImportValue,ExportValue

################################################################################
class Database(object):
    
    # Holds the ordered list of keys
    _vkeys = []
    # Holds the data types per key
    _types = {}
    # Holds the key-value pairs
    _properties = {}
    
    #===========================================================================
    def __init__(self):
        self._vkeys = []
        self._types = {}
        self._properties = {}
        # Disallow instantiation
        if type(self) is Database:
            raise NotImplemented()
    
    #===========================================================================
    def __repr__(self):
        representation = "{"
        for key in self._properties.keys():
            if (representation != "{"): representation += ",\n "
            try:
                item = repr(self.get(key))
            except:
                item = "<???>"
            representation += repr(key) + " = " + item
        representation += "}"
        return representation

    #===========================================================================
    def __len__(self):
        return len(self._properties)

    #===========================================================================
    def __nonzero__(self):
        return len(self._properties)>0

    #===========================================================================
    def create(self):
        raise NotImplemented()
    
    #===========================================================================
    def load(self):
        raise NotImplemented()
    
    #===========================================================================
    def reload(self):
        raise NotImplemented()

    #===========================================================================
    def id(self):
        raise NotImplemented()

    #===========================================================================
    def commit(self):
        raise NotImplemented()

    #===========================================================================
    def __getitem__(self, key):
        if not self._properties.has_key(key):
            raise DriverException("No such key: " + repr(key))
        return self._properties.get(key)

    #===========================================================================
    def __setitem__(self, key, value):
        if not key in self._properties.keys():
            self._vkeys.append(key)
        self._properties[key] = value

    #===========================================================================
    def __delitem__(self, key):
        if key in self._properties.keys():
            del self._properties[key]
        if key in self._vkeys:
            idx = self._vkeys.index(key)
            del self._vkeys[idx]

    #===========================================================================
    def set(self, key, value, format = None):
        if not key in self._properties.keys():
            self._vkeys.append(key)
        self._properties[key] = value
        if format:
            self._types[key] = format

    #===========================================================================
    def get(self, key):
        if not self._properties.has_key(key):
            return None
        return self._properties.get(key)

    #===========================================================================
    def keys(self):
        return self._vkeys

################################################################################
