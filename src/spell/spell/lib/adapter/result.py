###################################################################################
## MODULE     : spell.lib.adapter.result
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Result structure evaluable to boolean
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
    def __nonzero__(self):
        return self._test

    #===========================================================================
    def __cmp__(self,other):
        return cmp(self._test,other)
    
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
        return self._properties.keys()

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
        
