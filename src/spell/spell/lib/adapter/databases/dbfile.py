###################################################################################
## MODULE     : spell.lib.adapter.databases.dbfile
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Database based on local text files
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
import sys,os
from spell.lib.exception import DriverException

################################################################################
class DatabaseFile(Database):
    
    _name = None
    _filename = None
    _defaultExt = None
    
    #===========================================================================
    def __init__(self, name, path, defaultExt = None):
        super(DatabaseFile, self).__init__()
        
        self._name = name
        self._defaultExt = defaultExt
        
        dataHome = os.getenv("SPELL_DATA", (os.getenv("SPELL_HOME") + os.sep + "data") ).replace('/', os.sep)
        if dataHome is None:
            raise DriverException("SPELL_DATA environment variable not defined")

        # Append the data home to the path
        thePath = dataHome + os.sep + path.replace('/', os.sep)

        # First check if there is an extension in the file. If there is no
        # extension, append the default extension (if there is one).
        filename = os.path.basename(thePath)
        if (not len(filename.split("."))>1) and (defaultExt is not None): 
            thePath += "." + defaultExt
            
        self._filename = thePath

        LOG("Instanciated: " + name)
    
    #===========================================================================
    def id(self):
        return os.path.basename(self._filename)

    #===========================================================================
    def create(self):
        super(DatabaseFile, self).__init__()
        try:
            open(self._filename, 'w').close()
        except Exception, ex:
            raise DriverException('Cannot create database', str(ex))
        return self
    
    #===========================================================================
    def load(self):
        LOG("Load DB from file: " + repr(self._filename))

        # If the file exists go on directly. Otherwise try to find it
        # no matter the case of the name
        if not os.path.exists(self._filename):
            basepath = os.path.dirname(self._filename)
            filename = os.path.basename(self._filename)
            found = False
            for root, dirs, files in os.walk( basepath, topdown=False ):
                for f in files:
                    if f.upper() == filename.upper():
                        path = basepath + os.sep + f
                        found = True
            if not found:
                raise DriverException("Database file not found: " + repr(self._filename))
            self._filename = path

        if not os.path.exists(self._filename):
            raise DriverException("Cannot load: no such file: " + self._filename)
        
        # Read the data from the file
        self._readData()
        
    #===========================================================================
    def _readData(self):
        # Load the file contents
        lines = file(self._filename).readlines()
        self._vkeys = []
        self._types = {}
        self._properties = {}
        # Will hold the line to be imported
        lineToProcess = None
        for line in lines:

            # If there is something in the buffer, apend the next line
            if lineToProcess:
                # But remove the backslash ans spaces first
                lineToProcess = lineToProcess[0:-1].strip()
                lineToProcess += line.strip()
            else:
                # Else just add the current line
                lineToProcess = line.strip()
            
            # Ignore comments and blank lines
            if lineToProcess.startswith("#"):
                lineToProcess = None 
                continue
            if (lineToProcess is None) or (len(lineToProcess)==0): continue

            # If the line ends with backslash we need to concatenate 
            # with the next line
            if lineToProcess.find("\\")!=-1:
                continue
            
            # Now we can process the line data
            key = lineToProcess.split()[0]
            orig_value = " ".join(lineToProcess.split()[1:])
            value,vtype = ImportValue(orig_value)
            
            # Warn about duplicated data
            if self._properties.has_key(key):
                LOG("WARNING: duplicated database key: " + repr(key))
            else:
                self[key] = value
                if vtype:
                    self._types[key] = vtype
                    
            # Reset the line to process
            lineToProcess = None 
        # End for

    #===========================================================================
    def __delitem__(self, key):
        Database.__delitem__(self, key)
        if key in self._types.keys():
            del self._types[key]

    #===========================================================================
    def _writeData(self, theFile):
        for key in self._vkeys:
            value = self._properties.get(key)
            os.write( theFile, key + '\t')
            if self._types.has_key(key):
                vtype = self._types.get(key)
                value = ExportValue(value,vtype)
                os.write( theFile, value )                    
            else:
                if type(value)==str:
                    os.write( theFile, '"' + value + '"' )
                else:
                    os.write( theFile, str(value))
            # End if
            os.write( theFile, '\n' )
        # End for

    #===========================================================================
    def reload(self):
        self.load()

    #===========================================================================
    def commit(self):
        try:
            os.remove(self._filename)
            db = os.open(self._filename, os.O_CREAT | os.O_WRONLY)
        except Exception, ex:
            raise DriverException('Cannot write in database', str(ex))

        self._writeData(db)
        os.close(db)
        
    #===========================================================================
    def keys(self):
        return self._vkeys

    #===========================================================================
    def __repr__(self):
        representation = "{"
        for key in self.keys():
            if (representation != "{"): representation += ",\n "
            try:
                item = repr(self.get(key))
            except BaseException,ex:
                import traceback
                traceback.print_exc( file = sys.stderr )
                item = "<???>"
            representation += repr(key) + " = " + item
        representation += "}"
        return representation

    #===========================================================================
    def has_key(self, key):
        return (key in self._vkeys)

    #===========================================================================
    def _getPathName(self):
        return self._filename
