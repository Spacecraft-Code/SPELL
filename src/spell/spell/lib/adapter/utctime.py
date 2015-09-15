###################################################################################
## MODULE     : spell.lib.adapter.utctime
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: UTC time interface
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
import datetime,time
from spell.lib.exception import DriverException
from spell.lib.registry import REGISTRY
from spell.utils.log import *

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

# The date/time formats accepted are:
#
# For absolute time:
# 
#  * dd-mmm-yyyy [hh:mm[:ss]]
#  * yyyy-mm-dd [hh:mm[:ss]]
#  * dd/mm/yyyy [hh:mm[:ss]]
#  * dd-mm-yyyy [hh:mm[:ss]]
# 
#  * dd-mmm-yyyy:hh:mm[:ss]
#  * yyyy-mm-dd:hh:mm[:ss]
#  * dd/mm/yyyy:hh:mm[:ss]
#  * dd-mm-yyyy:hh:mm[:ss]
# 
# For relative times:
# 
#  * +ss.nnn or -ss.nnn
#  * +ddd hh:mm[:ss] or -ddd hh:mm[:ss]

__all__ = ['TIME','NOW','TODAY','YESTERDAY','TOMORROW','DAY','HOUR','MINUTE','SECOND']

NOW_STR = 'NOW'
YESTERDAY_STR = 'YESTERDAY'
TODAY_STR = 'TODAY'
TOMORROW_STR = 'TOMORROW'

################################################################################
class TIME(object):

    __fmt = None
    
    #===========================================================================
    def __init__(self, timestamp):
        ttime = ttime_class()
        self._val = None
        if isinstance(timestamp, TIME):
            if isinstance(timestamp._val, str):
                self._val = ttime.cnv(timestamp._val)
                self.__fmt = ttime.fmt()
            else:
                self._val = timestamp._val
        elif isinstance(timestamp, datetime.datetime) or isinstance(timestamp, datetime.timedelta):
            self._val = timestamp
        elif isinstance(timestamp, str):
            if timestamp in (NOW_STR, TODAY_STR, YESTERDAY_STR, TOMORROW_STR):
                self._val = timestamp
            else:
                self._val = ttime.cnv(timestamp)
                self.__fmt = ttime.fmt()
        else:
            self._val = ttime.cnv(timestamp)
	    
	#Setting time format
	self.__fmt = ttime.fmt()
        
	if self._val is None:
            raise DriverException("Invalid input for date/time: " + repr(timestamp))
    
    #===========================================================================
    @staticmethod
    def epoch(seconds):
        ms = 0
        if isinstance(seconds, float):
            ms = (seconds - int(seconds)) * 1000000
            seconds = int(seconds)
        if isinstance(seconds, int):
            epochtime = time.gmtime(seconds)
            val = TIME('%02i-%02i-%04d:%02i:%02i:%02i.%06i' %
                (epochtime.tm_mday, epochtime.tm_mon, epochtime.tm_year,
                epochtime.tm_hour, epochtime.tm_min, epochtime.tm_sec, ms))
        else:
            raise DriverException("Invalid input for date/time: " + repr(seconds))

        return val

    #===========================================================================
    def value(self):
        ttime = ttime_class()
        
        if isinstance(self._val, str):
            return ttime.cnv(self._val)
        else:
            return self._val
        
    #===========================================================================
    def abs(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            val = REGISTRY['TIME'].getUTC() + val
        return time.mktime(val.timetuple())
    
    #===========================================================================
    def rel(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return val.days * 3600 * 24 + val.seconds + val.microseconds / 1000000 
        return None
    
    #===========================================================================
    def isAbs(self):
        val = self.value()
        if isinstance(val, datetime.datetime):
            return True
        return False
    
    #===========================================================================
    def isRel(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return True
        return False
    
    #===========================================================================
    def julianDay(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return None
        return val.timetuple()[7]
    
    #===========================================================================
    def year(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return None
        return val.year
    
    #===========================================================================
    def month(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return None
        return val.month
    
    #===========================================================================
    def day(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return None
        return val.day
    
    #===========================================================================
    def hour(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return None
        return val.hour
    
    #===========================================================================
    def minute(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return None
        return val.minute
    
    #===========================================================================
    def second(self):
        val = self.value()
        if isinstance(val, datetime.timedelta):
            return None
        return val.second
    
    #===========================================================================
    def __str__(self):
        val = self.value()
        res = None
        if isinstance(val, datetime.datetime):
            res = val.strftime(self.__fmt)
        elif isinstance(val, datetime.timedelta):
            res = ('%+04i %02i:%02i:%02i' 
                % (val.days, val.seconds // 3600, 
                  val.seconds // 60 % 60, val.seconds % 60))
            if val.microseconds != 0: res = res + ".%06i" % val.microseconds
        return res

    #===========================================================================
    def __repr__(self):
        return self.__str__()
    
    #===========================================================================
    def __translate(self, timestamp):
        if isinstance(timestamp, TIME):
            return timestamp
        else:
            return TIME(timestamp)
            
    #===========================================================================
    def __cmp__(self, other):
        ttime = self.__translate(other)
        val = self.value()
        val2 = ttime.value()
        
        if isinstance(val,datetime.timedelta) and isinstance(val2,datetime.datetime):
            return -1
        if isinstance(val,datetime.datetime) and isinstance(val2,datetime.timedelta):
            return 1
        
        if (val2 == val):
            return 0
        elif (val < val2):
            return -1
        elif (val > val2):
            return 1
    
    #===========================================================================
    def __add__(self, timestamp):
        return TIME(self.value() + self.__translate(timestamp).value())

    #===========================================================================
    def __sub__(self, timestamp):
        return TIME(self.value() - self.__translate(timestamp).value())

    #===========================================================================
    def __mul__(self, coef):
        return TIME(self.value() * coef)

    #===========================================================================
    def __radd__(self, timestamp):
        return TIME(self.value() + self.__translate(timestamp).value())

    #===========================================================================
    def __rsub__(self, timestamp):
        return TIME(self.value() - self.__translate(timestamp).value())

    #===========================================================================
    def __rmul__(self, coef):
        return TIME(self.value() * int(coef))

################################################################################
class ttime_class(object):

    __isinitialized = False
    __instance = None
    __fmt = None
    
    #===========================================================================
    def __new__(cls):
        if not isinstance(ttime_class.__instance, cls):
            ttime_class.__instance = object.__new__(cls)        
        return ttime_class.__instance

    #===========================================================================
    def __init__(self):
        if self.__isinitialized:
            return
        super(ttime_class, self).__init__()
        self.__isinitialized = True
        self.__fmt = '%d-%b-%Y %H:%M:%S'

    #===========================================================================
    def fmt(self):
        return self.__fmt
    
    #===========================================================================
    def cnv(self, timestamp):
        
        mydt = datetime.datetime(1,1,1)
        evaluated = False
        datefmtlist = [ 
            '%Y.%j.%H.%M.%S',    '%Y.%j.%H.%M',    '%Y.%j',
            '%d-%b-%Y %H:%M:%S', '%d-%b-%Y %H:%M', '%d-%b-%Y',
            '%Y-%m-%d %H:%M:%S', '%Y-%m-%d %H:%M', '%Y-%m-%d',
            '%Y/%m/%d %H:%M:%S', '%Y/%m/%d %H:%M', '%Y/%m/%d',
            '%d/%m/%Y %H:%M:%S', '%d/%m/%Y %H:%M', '%d/%m/%Y',
            '%d-%m-%Y %H:%M:%S', '%d-%m-%Y %H:%M', '%d-%m-%Y',
            '%d-%b-%Y:%H:%M:%S', '%d-%b-%Y:%H:%M', '%d-%b-%Y',
            '%Y-%m-%d:%H:%M:%S', '%Y-%m-%d:%H:%M', '%Y-%m-%d',
            '%Y/%m/%d:%H:%M:%S', '%Y/%m/%d:%H:%M', '%Y/%m/%d',
            '%d/%m/%Y:%H:%M:%S', '%d/%m/%Y:%H:%M', '%d/%m/%Y',
            '%d-%m-%Y:%H:%M:%S', '%d-%m-%Y:%H:%M', '%d-%m-%Y',
        ]
        
        abshourfmtlist = [
            '%H.%M.%S', '%H:%M:%S', '%H:%M:%S', '%H:%M',
        ]

        # Split timestamp and microseconds

        ms = 0
        
        if isinstance(timestamp, float):
            ms = (timestamp - int(timestamp)) * 1000000
        
        elif isinstance(timestamp, str):
            # Check if the seconds are in mm:ss format. Otherwise dont check for microsecods
            if not timestamp.find(':') == -1:
                items = timestamp.split('.')
                timestamp = items[0]
                if len(items) > 1: ms = int(items[1].ljust(6, '0'))

        # - ISO, European or OpenVMS date formats
        for fmt in datefmtlist:
            if not evaluated:
                try:
                    val = mydt.strptime(timestamp, fmt)
                    val = val.replace(microsecond = ms)
                    self.__fmt = fmt;
                    evaluated = True
                except:
                    pass
        
        # - <int> or <float>
                
        if not evaluated and (isinstance(timestamp, int) or isinstance(timestamp, float) or isinstance(timestamp, long)):
            dd = int(timestamp / (3600 * 24)) 
            hh = int(timestamp / 3600) % 24
            mm = int(timestamp / 60) % 60 
            ss = int(timestamp) % 60

            val = datetime.timedelta(days = dd, hours=hh, minutes=mm, seconds=ss, microseconds=ms)
            
            evaluated = True     
            
        # +|-[dd] hh:mm[:ss]
                
        if not evaluated:
            try:
                tmp = timestamp
                if tmp[0] in ('+', '-'):
                    # Capture the sign ('+' in '+dd hh:mm:ss') and remove it
                    sign = 1
                    if tmp[0] == '-':
                        sign = -1
                    tmp = tmp[1:]
                    
                    # Capture the day ('dd' in 'dd hh:mm:ss' if 'dd' exists)
                    items = tmp.split(' ')                    
                    mytime = items[0]
                    dd = 0
                    
                    # In case we have '+hh:mm:ss' days = 0 and we go on with the time)
                    if len(items) == 2:
                        dd = eval(items[0].lstrip('0') or '0')
                        mytime = items[1]
                        
                    # Capture the time ('hh' 'mm' and 'ss' in 'hh:mm:ss')
                    items = mytime.split(':')
                    hh = eval(items[0].lstrip('0') or '0')
                    mm = eval(items[1].lstrip('0') or '0')
                    ss = int(eval(items[2].lstrip('0') or '0'))
                    
                    # Normalize in case we have hours > 23
                    # E.g. +2 26:00:00 becomes +3 02:00:00
                    dd = dd + hh // 24
                    hh = hh % 24

                    # Apply the sign
                    dd = dd * sign
                    hh = hh * sign
                    mm = mm * sign
                    ss = ss * sign
                    ms = ms * sign
                    
                    val = datetime.timedelta(days = dd, hours=hh, minutes=mm, seconds=ss, microseconds=ms)
                    
                    evaluated = True
            except:
                pass


        # Evaluated TODAY, NOW, YESTERDAY and TOMORROW
        
        if not evaluated:
            evaluated = True
            if REGISTRY.exists('TIME'):
                val = REGISTRY['TIME'].getUTC()
            else:
                val = datetime.datetime.utcnow()
            if timestamp != NOW_STR:
                val = val.replace(hour=0, minute=0, second=0, microsecond=0)
                if timestamp == YESTERDAY_STR:
                    val = val - datetime.timedelta(days = 1)
                elif timestamp == TOMORROW_STR:
                    val = val + datetime.timedelta(days = 1)
                elif timestamp != TODAY_STR:
                    evaluated = False
                            
        # - hh:mm[:ss] is TODAY at hh:mm[:ss]

        for fmt in abshourfmtlist:
            if not evaluated:
                try:
                    mydt = mydt.strptime(timestamp, fmt)
                    val = REGISTRY['TIME'].getUTC()
                    val = val.replace(hour=mydt.hour, minute=mydt.minute, 
                                      second=mydt.second, microsecond=ms)
                    evaluated = True
                except:
                    pass
        
        if evaluated:
            return val

        return None

#*******************************************************************************
# SPELL Definitions
#*******************************************************************************

DAY    = TIME('+1 00:00:00')
HOUR   = TIME('+01:00:00')
MINUTE = TIME('+00:01:00')
SECOND = TIME('+00:00:01')

NOW = TIME(NOW_STR)
TODAY = TIME(TODAY_STR)
YESTERDAY = TIME(YESTERDAY_STR)
TOMORROW = TIME(TOMORROW_STR)
EPOCH = TIME.epoch
