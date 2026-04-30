###################################################################################
## MODULE     : spell.lib.adapter.dbmgr
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: SPELL database manager
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
from spell.utils.log import *
from spell.lib.exception import DriverException
from spell.lib.adapter.config import Configurable
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.databases.database import *
from spell.lib.adapter.databases.dbasrun import *
from spell.lib.adapter.databases.dbsvn import *
from spell.lib.adapter.databases.dbfile import *
from spell.lib.adapter.databases.dbraw import *
from spell.lib.adapter.databases.dbfilespb import *
from spell.lib.adapter.databases.dbfileeph import *
from spell.lib.adapter.databases.dbsvnspb import *
from spell.lib.adapter.databases.datasource import HttpDataSource
#from libSPELL_SDB import DatabaseFile, DatabaseFileSPB

#*******************************************************************************
# Local Imports
#*******************************************************************************

#*******************************************************************************
# System Imports
#*******************************************************************************
import os,sys

#*******************************************************************************
# Import Definition
#*******************************************************************************
__all__ = [ 'DBMGR' ]

#*******************************************************************************
# Module Globals
#*******************************************************************************

# Predefined database types
DB_TYPE_FILE          = 'file'
#DB_TYPE_SQLITE = 'sqlite'
#DB_TYPE_MYSQL  = 'sql'
DB_TYPE_SVN           = 'svn'
DB_TYPE_RAW           = 'raw'
DB_TYPE_SPB           = 'spb'
DB_TYPE_EPH_A2100     = 'eph_a2100'
DB_TYPE_EPH_OSCS2     = 'eph_oscs2'
DB_TYPE_EPH_SB4000    = 'eph_sb4000'
DB_TYPE_ASRUN         = 'asrun'
DB_TYPE_SPB_SVN       = 'spb-svn'
DB_TYPE_HTTP          = 'http'
DB_TYPE_HTTPS          = 'https'

################################################################################
class DatabaseManagerClass(Configurable):

    __ctxConfig = None
    __databases = {}

    #===========================================================================
    def __init__(self):
        Configurable.__init__(self)
        self.__databases = {}
        self.__ctxConfig = None
        LOG("Created")

    #===========================================================================
    def setup(self, ctxConfig, drvConfig):
        self.__ctxConfig = ctxConfig

    #===========================================================================
    def cleanup(self):
        self.__databases.clear()

    #===========================================================================
    def __getitem__(self, key):
        if key not in self.__databases:
            raise DriverException("No such database: " + repr(key))
        return self.__databases.get(key)

    #===========================================================================
    def __fromURItoPath(self, dbName):
        LOG("Create database " + repr(dbName))
        idx = dbName.find('://')
        preloadMode = ''
        if idx != -1:
            # User database
            dbURI = dbName[0:idx]
            sepIdx = dbURI.find('+')
            if sepIdx > 0:
                preloadMode = dbURI[:sepIdx]
                dbURI = dbURI[sepIdx+1:]
            dbFileName = dbName[idx+3:]
            dbType = self.__ctxConfig.getLocationType(dbURI)
            dbPath = self.__ctxConfig.getLocationPath(dbURI) + os.sep + dbFileName
            dbExt = self.__ctxConfig.getLocationExt(dbURI)
            if not dbType:
                dbType = dbURI
            LOG("User path: " + repr(dbPath))
        else:
            # Preconfigured database
            location,filename = self.__ctxConfig.getDatabaseInfo(dbName)
            if location is None:
                raise DriverException("Unknown database location")
            if filename is None:
                raise DriverException("No database file")
            dbType = self.__ctxConfig.getLocationType(location)
            dbPath = self.__ctxConfig.getLocationPath(location) + os.sep + filename
            dbExt = self.__ctxConfig.getLocationExt(location)

        # Translate path tags
        idx = dbPath.find("$SATNAME$")
        if idx != -1:
            dbPath = dbPath[0:idx] + self.__ctxConfig.getSatName() + dbPath[idx+9:]
        idx = dbPath.find("$SATID$")
        if idx != -1:
            dbPath = dbPath[0:idx] + self.__ctxConfig.getSC() + dbPath[idx+7:]

        if preloadMode:
            try:
                source,endpoint = preloadMode.split(':')
                dataSource = self.loadDataSourceURL(source.upper(),'/xml/fld/' + endpoint ,{'file':dbFileName},'txt')
                LOG('Loading file '+dbFileName+' from '+source.upper()+'.')
                dbPath = dataSource.downloadFile()
                LOG('Loaded from ' +source.upper()+ ' written to ' + dbPath)
            except Exception as ex:
                import traceback
                LOG('Failed to Load file from Pharos!\n'+str(traceback.format_exc()))
                raise DriverException('Failed to Load file from Pharos!\n'+str(ex))
        LOG("Database path: " + repr(dbPath))
        return dbType, dbPath, dbExt

    #===========================================================================
    def __getDatabaseInstance(self, dbName):
        dbType, dbPath,dbExt = self.__fromURItoPath(dbName)

        dbType = dbType.lower()
        LOG("Database type: " + repr(dbType))
        db = None
        #from libSPELL_SDB import DatabaseFile
        #from libSPELL_SDB import DatabaseFileSPB
        if dbType == DB_TYPE_RAW:
            db = DatabaseRaw(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_FILE:
            db = DatabaseFile(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_SPB:
            db = DatabaseFileSPB(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_EPH_A2100:
            db = DatabaseFileEphA2100(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_EPH_OSCS2:
            db = DatabaseFileEphOscS2(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_EPH_SB4000:
            db = DatabaseFileEphSB4000(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_SVN:
            db = DatabaseSubversion(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_SPB_SVN:
            db = DatabaseSubversionSPB(dbName, dbPath, dbExt)
        elif dbType == DB_TYPE_ASRUN:
            db = DatabaseAsRun(dbName, dbPath, dbExt)
        else:
            raise DriverException("Unknown database type: " + repr(dbType))
        self.__databases[dbName] = db
        return db

    #===========================================================================
    def createDatabase(self, dbName):
        if dbName not in self.__databases:
            db = self.__getDatabaseInstance(dbName)
        db = self.__databases.get(dbName)
        db.create()
        return db

    #===========================================================================
    def createEmptyDatabase(self, dbName):
        if dbName not in self.__databases:
            db = self.__getDatabaseInstance(dbName)
        db = self.__databases.get(dbName)
        return db
    
    #===========================================================================
    def loadDatabase(self, dbName):
        if dbName not in self.__databases:
            db = self.__getDatabaseInstance(dbName)
        db = self.__databases.get(dbName)
        db.load()
        return db
    
    def loadDataSourceURL(self,sourceName,serviceTargetURL=None,config=None,extension='json'):
        dataSource = self.__ctxConfig.getDataSource(sourceName)
        if not dataSource:
            raise DriverException("Unknown data source specified: " + str(sourceName))
        if not serviceTargetURL:
            source = HttpDataSource(dataSource,dataSource.serviceUrl,config)
            return source
        else:
            source = HttpDataSource(dataSource,dataSource.serviceUrl)
            serviceUrl = serviceTargetURL
           
            if not serviceUrl:
                raise DriverException("Could not find Data Source  " + str(sourceName) + "!")
            print((serviceUrl , extension,config))
            source = source.openService(serviceUrl + '.' + extension,config,extension)
            return source

    def loadDataSource(self,sourceName,serviceName=None,config=None):
        extension='json'
        dataSource = self.__ctxConfig.getDataSource(sourceName)
        if not dataSource:
            raise DriverException("Unknown data source specified: " + str(sourceName))
        if not serviceName:
            source = HttpDataSource(dataSource,dataSource.serviceUrl,config)
            return source
        else:
            source = HttpDataSource(dataSource,dataSource.serviceUrl)
            serviceUrl = None
            for service in source:
                if service["Name"].upper() == serviceName.upper():
                    if "json" not in service["Datatype"]:
                        raise DriverException("Specified Datasource  " + str(sourceName) + " doesn't provide "+str(extension)+" data!")
                    serviceUrl = service["URL"]
                    
            if not serviceUrl:
                raise DriverException("Could not find Data Source  " + str(sourceName) + "!")
            print((serviceUrl , extension,config))
            source = source.openService(serviceUrl + '.' + extension,config)
            return source
     
    def saveToDataSource(self,sourceName,serviceName,obj,config=None):
        if type(obj) == HttpDataSource:
            obj = obj.getJsonObject()
            
        dataSource = self.__ctxConfig.getDataSource(sourceName)
        if not dataSource:
            raise DriverException("Unknown data source specified: " + str(sourceName))
        
        source = HttpDataSource(dataSource,dataSource.serviceUrl)
        source = source.openService(serviceName,config)
        source = source.uploadObject(obj,config)
        return source
            
DBMGR = DatabaseManagerClass()
