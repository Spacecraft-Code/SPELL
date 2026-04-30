###################################################################################
## MODULE     : spell.lib.adapter.result
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Result structure evaluable to boolean
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['Result']

###############################################################################
class Result(object):

    """
    Test result structure. Evaluates as boolean expression and
    gives operation details
    """
    _properties = {}
    _test = False

    #===========================================================================
    def __init__(self):
        self._properties = {}
        self._test = False

    #===========================================================================
    def __bool__(self):
        return bool(self._test)

    #===========================================================================
    def __nonzero__(self):
        return bool(self._test)
      
    #===========================================================================
    def __eq__(self, other):
        return self._test == other

    #===========================================================================
    def __gt__(self, other):
        return self._test > other

    #===========================================================================
    def __lt__(self, other):
        return self._test < other

    #===========================================================================
    def __ge__(self, other):
        return self._test >= other

    #===========================================================================
    def __le__(self, other):
        return self._test <= other

    #===========================================================================
    def __ne__(self,other):
        return self._test != other

    #===========================================================================
    def __getitem__(self,key):
        return self._properties[key]

    #===========================================================================
    def __setitem__(self,key,value):
        self._properties[key] = value
        self._evaluate()

    #===========================================================================
    def _evaluate(self):
        self._test = False

    #===========================================================================
    def keys(self):
        return list(self._properties.keys())

    #===========================================================================
    def __str__(self):
        return str(self._test)

    #===========================================================================
    def __repr__(self):
        return repr(self._test)

###############################################################################
class TmResult(Result):

    """
    TM verification test result structure. Evaluates as boolean expression and
    gives operation details in the form 'parameter name':'verification status'
    """

    #===========================================================================
    def __init__(self):
        Result.__init__(self)

    #===========================================================================
    def _evaluate(self):
        for key in self._properties:
            if not self[key]:
                self._test = False
                return
        self._test = True

###############################################################################
class PtcResult(Result):

    """
    TM verification test result structure. Evaluates as boolean expression and
    gives operation details in the form 'parameter name':'verification status'
    """

    #===========================================================================
    def __init__(self):
        Result.__init__(self)
        self['code']    = []
        self['success'] = []
        self['data']    = []
        self['stage']   = []
        self['error']   = []

    #===========================================================================
    def _evaluate(self):

        if 'code' not in self._properties or not all([ x == 0 for x in self['code']]) or \
           'success' not in self._properties or not all(self['success']):
            self._test = False
            return
        self._test = True

    def __len__(self):
        return len(self['success'])

    def _addEntry(self,success,stage,data,error,code):
        try:

            self['code']    += [code]
            self['success'] += [success]
            self['data']    += [data]
            self['stage']   += [stage]
            self['error']   += [error]
            self._evaluate()
            return True
        except:
            import traceback,sys
            LOG_ERROR(traceback.format_exc())
            sys.exc_clear()
            del traceback
            del sys
        return False

    def _addUpdateEntry(self,success,stage,data,error,code):
        if data not in self['data']:
            return self._addEntry(success,stage,data,error,code)
        idx = self['data'].index(data)
        try:
            self['code'][idx]    = code
            self['success'][idx] = success
            self['data'][idx]    = data
            self['stage'][idx]   = stage
            self['error'][idx]   = error
            self._evaluate()
            return True
        except:
            import traceback,sys
            LOG_ERROR(traceback.format_exc())
            sys.exc_clear()
            del traceback
            del sys
        return False

    def code(self,idx = None):
        if idx is None:
            return self['code'][:]
        return self['code'][idx]
    def success(self,idx = None):
        if idx is None:
            return self['success'][:]
        return self['success'][idx]
    def data(self,idx = None):
        if idx is None:
            return self['data'][:]
        return self['data'][idx]
    def stage(self,idx = None):
        if idx is None:
            return self['stage'][:]
        return self['stage'][idx]
    def error(self,idx = None):
        if idx is None:
            return self['error'][:]
        return self['error'][idx]
