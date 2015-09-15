###################################################################################
## MODULE     : spell.lib.adapter.databases.dbsvn
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Database based on local subversioned files
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

from dbfilespb import *
import os.path
from spell.utils.log import *

################################################################################
class DatabaseSubversionSPB(DatabaseFileSPB):
    
    #===========================================================================
    def __init__(self, name, path, defaultExt = None):
        try:
            import pysvn
            self.__client = pysvn.Client()
        except:
            self.__client = None
            raise DriverException("Cannot import pysvn")

        super(DatabaseSubversionSPB, self).__init__(name, path, defaultExt)
        
    #===========================================================================
    def load(self):
        super(DatabaseSubversionSPB, self).load()

    #===========================================================================
    def status(self):
        client = self.__client
        filename = self._getPathName()

        LOG('Checking the status of the Subversion database %s' % filename)

        if not os.path.exists(filename):
            return None

        try:
            statuslst = client.status(filename, recurse = False, update = True)
        except Exception, ex:
            raise DriverException("Cannot get the Subversion database status (%s)" % ex)

        return statuslst[0]

    #===========================================================================
    def create(self):
        client = self.__client
        filename = self._getPathName()

        status = self.status()
        exists = False

        if status is not None:
            if status.entry is not None:
                exists = True

        if not exists:
            super(DatabaseSubversionSPB, self).create()
            client.add(filename) 
            client.propset('svn:needs-lock', '*', filename)
            client.checkin([filename], '\n')
        else:
            self.lock()
            super(DatabaseSubversionSPB, self).create()
            self.unlock()

        return self
    
    #===========================================================================
    def __del__(self):
        self.unlock()
        
    #===========================================================================
    def lock(self):
        import pysvn

        client = self.__client
        filename = self._getPathName()

        status = self.status()

       	if status.entry is None:
            raise DriverException("The database is not under revision control")

        if status.entry.schedule == pysvn.wc_schedule.add:
            client.checkin([filename], '\n')

        client.lock(filename, '\n', force = True)

    #===========================================================================
    def unlock(self):
        client = self.__client
        filename = self._getPathName()

        status = self.status()
        
        if status.entry.lock_token is None: return
        
        LOG('Unlocking Subversion database ' + filename)
        client.unlock(filename)        

    #===========================================================================
    def commit(self):
        client = self.__client
        filename = self._getPathName()

        self.lock()

        super(DatabaseSubversionSPB, self).commit()

        LOG('Saving Subversion database ' + filename)
        msg = os.getenv("HOSTNAME") + '\n'
        client.checkin([filename], msg, keep_locks = True)

        self.unlock()

