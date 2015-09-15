###################################################################################
## MODULE     : spell.lang.helpers.filehelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for generic features
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
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.constants.notification import *
from spell.lib.adapter.utctime import *
from spell.lib.registry import *
from spell.lib.adapter.interface import Interface
from spell.lib.adapter.databases.database import Database
from spell.lib.adapter.file import File

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import *

#*******************************************************************************
# System Imports
#*******************************************************************************
import sys,os, stat


################################################################################
class LoadDictionary_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the LoadDictionary wrapper.
    """    
    
    __prefix = None
    __database = None
    __retry    = False
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__database = None
        self.__retry = False
        self.__prefix = None
        self._opName = "Database" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No dictionary given")

        if not self.__retry:
            # Get the database name
            self.__database = args[0]
            if type(self.__database)!=str:
                raise SyntaxException("Expected a database name")
            if not "://" in self.__database:
                raise SyntaxException("Database name must have URI format")
            idx = self.__database.find("://")
            self.__prefix = self.__database[0:idx]
        else:
            self.__retry = False
        
        idx = self.__database.find("//")
        toShow = self.__database[idx+2:]
        
        self._setActionString( ACTION_REPEAT ,  "Try to load the database " + repr(toShow) + " again")

        self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_PR, "Loading")
        self._write("Loading database " + repr(toShow))

        db = None
        try:
            db = REGISTRY['DBMGR'].loadDatabase(self.__database)
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "Loaded")
        except DriverException,ex:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "Failed")
            self._write("Failed to load database " + repr(toShow), {Severity:ERROR})
            raise ex
        
        return [False,db,NOTIF_STATUS_OK,"Database loaded"]

    #===========================================================================
    def _doRepeat(self):
        from spell.lang.functions import Prompt,Display
        Display("Load database failed, getting new name", WARNING )
        idx = self.__database.find("//")
        toShow = self.__database[idx+2:]
        newName = str(Prompt("Enter new database name (previously " + repr(toShow) + "): ", Type=ALPHA, Notify=False ))
        if not newName.startswith(self.__prefix):
            newName =  self.__prefix + "://" + newName
        self.__database = newName
        self.__retry = True
        return [True,None]

################################################################################
class SaveDictionary_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the SaveDictionary wrapper.
    """    
    
    __prefix = None
    __database = None
    __retry    = False
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__database = None
        self.__retry = False
        self.__prefix = None
        self._opName = "Database" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No dictionary given")

        if not self.__retry:
            self.__database = args[0]
            if not isinstance(self.__database, Database):
                raise SyntaxException("Expected a database object (%s)" % repr(self.__database))
        else:
            self.__retry = False
        
        toShow = self.__database.id()
        
        self._setActionString( ACTION_SKIP   ,  "Skip saving the dictionary " + repr(toShow) + " and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip saving the dictionary " + repr(toShow) + " and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to save the dictionary " + repr(toShow) + " again")

        try:
            self.__database.commit()
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "SUCCESS")
            self._write("Database " + repr(toShow) + " saved")
        except BaseException,ex:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "FAILED")
            self._write("Failed to save database " + str(ex) , {Severity:ERROR})
            raise DriverException(str(ex))
        
        return [False, True, NOTIF_STATUS_OK, "Database saved"]

    #===========================================================================
    def _doSkip(self):
        self._write("Save dictionary skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Save dictionary skipped", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry saving dictionary", {Severity:WARNING} )
        return [True,None]


################################################################################
class CreateDictionary_Helper(LoadDictionary_Helper):

    """
    DESCRIPTION:
        Helper for the CreateDictionary wrapper.
    """    
    
    #===========================================================================
    def __init__(self):
        super(CreateDictionary_Helper, self).__init__()
        self.__retry    = False
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No dictionary given")

        if not self.__retry:
            # Get the database name
            self.__database = args[0]
            if type(self.__database)!=str:
                raise SyntaxException("Expected a database name")
            if not "://" in self.__database:
                raise SyntaxException("Database name must have URI format")
            idx = self.__database.find("://")
            self.__prefix = self.__database[0:idx]
        else:
            self.__retry = False
            
        idx = self.__database.find("//")
        toShow = self.__database[idx+2:]
        
        self._setActionString( ACTION_SKIP   ,  "Skip creating the dictionary " + repr(toShow) + " and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to create the dictionary " + repr(toShow) + " again")

        self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_PR, "Creating")
        db = None

        try:
            db = REGISTRY['DBMGR'].createDatabase(self.__database)
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "Created")
        except DriverException,ex:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "Failed")
            raise ex
        
        return [False,db,NOTIF_STATUS_OK,"Database created"]

    #===========================================================================
    def _doSkip(self):
        self._write("Create dictionary skipped", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry creating dictionary", {Severity:WARNING} )
        return [True,None]

################################################################################
class OpenFile_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the OpenFile wrapper.
    """    
    
    #===========================================================================
    def __init__(self):
        super(OpenFile_Helper, self).__init__()
        self._opName = ""
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No path given")

        filename = args[0]
        if isinstance(filename,File):
            baseName = filename.basename()
            theFile = filename
        elif type(filename)==str:
            baseName = os.path.basename(filename)
            theFile = File(filename)
        else:
            raise SyntaxException("Cannot open", "Expected a string or a File object")

        if theFile.isDir():
            raise DriverException("Cannot open file", "A directory has been provided")

        self._setActionString( ACTION_SKIP   ,  "Skip opening the file " + repr(baseName) + " and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to open the file " + repr(baseName) + " again")

        self._notifyValue( "File", repr(baseName), NOTIF_STATUS_PR, "Opening")
        
        try:
            mode = READ
            if self.hasConfig(Mode):
                mode = self.getConfig(Mode)

            self._write("Opening file " + repr(baseName) + " in mode " + mode, config={Severity:INFORMATION})
            
            theFile.open(mode)

            self._write("File open")
                
        except DriverException,ex:
            self._notifyValue( "File", repr(baseName), NOTIF_STATUS_FL, "Open failed")
            raise ex

        self._notifyValue( "File", repr(baseName), NOTIF_STATUS_OK, "Open success")

        return [False,theFile,NOTIF_STATUS_OK,"File open"]

    #===========================================================================
    def _doSkip(self):
        self._write("Open file skipped", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry opening file", {Severity:WARNING} )
        return [True,None]

################################################################################
class CloseFile_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the CloseFile wrapper.
    """    
    
    #===========================================================================
    def __init__(self):
        super(CloseFile_Helper, self).__init__()
        self._opName = ""
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No file object given")

        theFile = args[0]
        if not isinstance(theFile, File):
            raise SyntaxException("Expected a file object")

        if theFile.isDir():
            raise DriverException("Cannot close file", "A directory has been provided")

        self._setActionString( ACTION_SKIP   ,  "Skip closing the file " + repr(theFile.basename()) + " and return True")
        self._setActionString( ACTION_CANCEL ,  "Skip closing the file " + repr(theFile.basename()) + " and return False")
        self._setActionString( ACTION_REPEAT ,  "Try to close the file " + repr(theFile.basename()) + " again")

        self._notifyValue( "File", repr(theFile.basename()), NOTIF_STATUS_PR, "Closing")
        
        try:
            
            self._write("Closing file: " + repr(theFile.basename()), config={Severity:INFORMATION})

            theFile.close()

            self._write("File closed", config={Severity:INFORMATION})
                
        except DriverException,ex:
            self._notifyValue( "File", repr(theFile.basename()), NOTIF_STATUS_FL, "Close failed")
            raise ex

        self._notifyValue( "File", repr(theFile.basename()), NOTIF_STATUS_OK, "Close success")

        return [False,theFile,NOTIF_STATUS_OK,"File closed"]

    #===========================================================================
    def _doSkip(self):
        self._write("Close file skipped", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doCancel(self):
        self._write("Close file cancelled", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry closing file", {Severity:WARNING} )
        return [True,None]

################################################################################
class WriteFile_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the WriteFile wrapper.
    """    
    
    #===========================================================================
    def __init__(self):
        super(WriteFile_Helper, self).__init__()
        self._opName = ""
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)<2:
            raise SyntaxException("Incorrect arguments")

        theFile = args[0]
        if not isinstance(theFile, File):
            raise SyntaxException("Expected a file object as first argument")
        
        theData = args[1]
        if type(theData) != str and type(theData) != list:
            raise SyntaxException("Expected a string or list of strings as second argument")

        if theFile.isDir():
            raise DriverException("Cannot write to file", "A directory has been provided")

        self._setActionString( ACTION_SKIP   ,  "Skip writing to file " + repr(theFile.basename()) + " and return True")
        self._setActionString( ACTION_CANCEL ,  "Skip writing to file " + repr(theFile.basename()) + " and return False")
        self._setActionString( ACTION_REPEAT ,  "Try to write to file " + repr(theFile.basename()) + " again")

        try:
            if type(theData)==str:
                theFile.write(theData)
            else:
                for d in theData:
                    theFile.writeln(str(d))
                
        except DriverException,ex:
            raise ex

        return [False,True,NOTIF_STATUS_OK,"Data written"]

    #===========================================================================
    def _doSkip(self):
        self._write("Write to file skipped", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doCancel(self):
        self._write("Write to file cancelled", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry write to file", {Severity:WARNING} )
        return [True,None]

################################################################################
class ReadFile_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the ReadFile wrapper.
    """    
    
    #===========================================================================
    def __init__(self):
        super(ReadFile_Helper, self).__init__()
        self._opName = ""
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)<1:
            raise SyntaxException("Incorrect arguments")

        theFile = args[0]
        if not isinstance(theFile, File):
            raise SyntaxException("Expected a file object as argument")
        
        if theFile.isDir():
            raise DriverException("Cannot read file", "A directory has been provided")
        
        self._setActionString( ACTION_SKIP   ,  "Skip reading from file " + repr(theFile.basename()) + " and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to read from file " + repr(theFile.basename()) + " again")

        data = None
        try:
            data = theFile.read()
        except DriverException,ex:
            raise ex

        return [False,data,NOTIF_STATUS_OK,"Data read"]

    #===========================================================================
    def _doSkip(self):
        self._write("Read from file skipped", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry read from file", {Severity:WARNING} )
        return [True,None]

################################################################################
class DeleteFile_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the ReadFile wrapper.
    """    
    
    #===========================================================================
    def __init__(self):
        super(DeleteFile_Helper, self).__init__()
        self._opName = ""
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)<1:
            raise SyntaxException("Incorrect arguments")

        theFile = args[0]
        
        if not isinstance(theFile,File) and type(theFile)!= str:
            raise SyntaxException("Cannot delete", "Expected a file object or string")
        
        if type(theFile)==str:
            theFile = File(theFile)
        
        if theFile.isDir():
            raise DriverException("Cannot delete file", "A directory has been provided")
        
        self._setActionString( ACTION_SKIP   ,  "Skip deleting file " + repr(theFile) + " and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to delete file " + repr(theFile) + " again")

        # Will do the checks and raise the appropriate exceptions
        theFile.delete()

        return [False,True,NOTIF_STATUS_OK,"File deleted"]

    #===========================================================================
    def _doSkip(self):
        self._write("Delete file skipped", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry deleting file", {Severity:WARNING} )
        return [True,None]

################################################################################
class ReadDirectory_Helper(WrapperHelper):

    #===========================================================================
    def __init__(self):
        super(ReadDirectory_Helper, self).__init__()
        self._opName = ""
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)<1:
            raise SyntaxException("Incorrect arguments")

        theDir = args[0]
        if not isinstance(theDir,File) and type(theDir)!= str:
            raise SyntaxException("Cannot read directory", "Expected a file object or string")
        
        if type(theDir)==str:
            theDir = File(theDir)
        
        if not theDir.isDir():
            raise DriverException("Cannot read directory", "A file has been provided")
        
        self._setActionString( ACTION_SKIP   ,  "Skip reading directory " + repr(theDir) + " and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to read the directory " + repr(theDir) + " again")

        # Will do the checks and raise the appropriate exceptions
        result = theDir.read()

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doSkip(self):
        self._write("Read directory skipped", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry read directory", {Severity:WARNING} )
        return [True,None]

