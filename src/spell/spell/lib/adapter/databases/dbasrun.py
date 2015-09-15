###################################################################################
## MODULE     : spell.lib.adapter.databases.dbasrun
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Database for AsRun files
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

from database import *

################################################################################
class DatabaseAsRun(Database):
    
    __filename = None
    __file = None
    
    #===========================================================================
    def __init__(self, name, path, defaultExt = None):
        if defaultExt is not None:
            self.__filename = path + "." + defaultExt
        else:
            self.__filename = path
            
        # Obtain the SPELL data directory
        data = os.getenv("SPELL_DATA")
        if data is None:
            raise DriverException("SPELL data directory not defined")
            
        self.__filename = data + os.sep + self.__filename

        LOG("Instanciated: " + self.__filename)
    
    #===========================================================================
    def id(self):
        return self.__filename

    #===========================================================================
    def create(self):
        self.__file = open(self.__filename, 'w')
        LOG("Created: " + self.__filename)
        return self
    
    #===========================================================================
    def __getitem__(self, key):
        return None

    #===========================================================================
    def __setitem__(self, key, value):
        pass

    #===========================================================================
    def set(self, key, value, format = None):
        pass
            
    #===========================================================================
    def write(self, timestamp, identifier, *kargs):
        args = '\t'.join([ str(k) for k in kargs ])
        text = "%s\t%s\t%s\n" % (timestamp,identifier,args)        
        self.__file.write(text)
        self.__file.flush()

    #===========================================================================
    def getFilename(self):
        return self.__filename
    
    #===========================================================================
    def reload(self):
        pass

    #===========================================================================
    def commit(self):
        pass
    
    #===========================================================================
    def keys(self):
        return []
