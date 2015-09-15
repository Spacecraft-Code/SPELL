###################################################################################
## MODULE     : spell.utils.customlog
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Custom logger (temporary)
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
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import time,os,sys,inspect
from datetime import datetime

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'LOG', 'LOG_INFO', 'LOG_DEBUG', 'LOG_WARN', 'LOG_ERROR', 'LOG_MAIN',
            'LOG_INIT', 'LOG_CNFG', 'LOG_PROC', 'LOG_LANG', 'LOG_COMM' ]

LOG_INFO  = '[ INFO  ]'
LOG_DEBUG = '[ DEBUG ]'
LOG_WARN  = '[ WARN  ]'
LOG_ERROR = '[ ERROR ]'

LOG_MAIN = '[ MAIN ]'
LOG_INIT = '[ INIT ]'
LOG_CNFG = '[ CNFG ]' 
LOG_PROC = '[ PROC ]'
LOG_LANG = '[ LANG ]'
LOG_COMM = '[ COMM ]'

SHOW_LEVELS = [ LOG_MAIN, LOG_INIT, LOG_CNFG, LOG_PROC, LOG_LANG, LOG_COMM ] 

################################################################################
class LoggerClass(object):
    
    __filename = None
    __fileobj = None
    __showToLevel = LOG_COMM
    showlog = True
    
    LOG_MAXLENGTH = 35

    #===========================================================================
    def __init__(self):
        self.__filename = None
        self.__fileobj = None 
        self.__showToLevel = SHOW_LEVELS.index(LOG_COMM)
        self.showLog = True

    #===========================================================================
    def setLogFile(self, fileName, timestamp = None ):
        if self.__fileobj is not None:
            os.close(self.__fileobj.fileno())
        self.initLogFile(fileName,timestamp)

    #===========================================================================
    def showLevel(self, level = None):
        if level is None:
            self.__showToLevel = SHOW_LEVELS.index(LOG_COMM)
        else:
            self.__showToLevel = SHOW_LEVELS.index(level)
        sys.stderr.write("LOGGING LEVEL: " + SHOW_LEVELS[self.__showToLevel] + "\n")

    #===========================================================================
    def initLogFile(self, filename, timestamp = None):
        home = os.getenv("SPELL_LOG")
        if home == None:
            raise BaseException("SPELL_LOG environment variable not set")
        if not os.path.exists(home):
            raise BaseException("Cannot find log directory")
        if timestamp is None:
            timestamp = time.strftime('%Y-%m-%d_%H%M%S') 
        self.__filename = home + os.sep + timestamp + "_" + filename + ".log"
        self.__fileobj = file(self.__filename, 'wt')
        self.write('Created on ' + time.strftime('%d-%b-%Y %H:%M:%S') + '\n\n')

    #===========================================================================
    def getLogFile(self):
        return self.__fileobj
        
    #===========================================================================
    def __call__(self, msg, severity = LOG_INFO, level = LOG_PROC):
        if severity not in [LOG_ERROR,LOG_WARN]:
            if not (SHOW_LEVELS.index(level)<=self.__showToLevel): return

        if not level in SHOW_LEVELS:
            sys.stderr.write("ERROR: unknown log level: " + repr(level))
            sys.stderr.write(logStr)
            return
                    
        if self.showlog or self.__fileobj:
            timestamp = str(datetime.now())[:-3]
            try:
                stack = inspect.stack()
                pname = stack[1][0].f_locals['self'].__class__.__name__
                mname = str(stack[1][3])
                mname.strip("__")
                origLogname = pname + "::" + mname + "()"
            except:
                origLogname = "(main)"
            len = LoggerClass.LOG_MAXLENGTH
            logname = origLogname[0:len - 2]
            if logname != origLogname: logname = logname + '..'
        
            fileStr = "%s\t%s\t%s\t%s\t%s\n" % ( logname.ljust(len),\
                     severity, timestamp, level, msg)
            self.write(fileStr)
            
        if self.showlog:
            logStr = "[ %s ] %s [ %s ] %s: %s\n" % ( logname.ljust(len),\
                     severity, timestamp, level, msg)
            sys.stderr.write(logStr)

    #===========================================================================
    def write(self, msg):
        if self.__fileobj:
            self.__fileobj.write(msg)
            self.__fileobj.flush()

################################################################################
