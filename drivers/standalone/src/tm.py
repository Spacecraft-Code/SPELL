###################################################################################
## MODULE     : tm
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telemetry interface of the driver connection layer
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.utctime import *
from spell.lib.registry import REGISTRY

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import sys,os

###############################################################################
# Module import definition

__all__ = ['TM']

###############################################################################
# Superclass
import spell.lib.adapter.tm
superClass = spell.lib.adapter.tm.TmInterface

###############################################################################
class TmInterface( superClass ):
    
    __currentParam = None
    __aborted = False
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone TM interface")
        
    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone TM interface")
    
    #==========================================================================
    def _injectItem(self, param, value, config ):
        REGISTRY['SIM'].changeGPitem(param,value)
        return True
    
    #==========================================================================
    def _restoreNormalLimits(self, config ):
        REGISTRY['CIF'].write("Reset all GCS limit definitions", {Severity:WARNING})
        return True 

    #===========================================================================
    def __getitem__(self, key):
        # If the parameter mnemonic is composed of several words:
        words = key.split()
        mnemonic = key
        if len(words)>1 and words[0].upper() == 'T':
            mnemonic = words[1]
        else:
            mnemonic = key
        LOG("Return simulated item '" + mnemonic + "'")
        if REGISTRY['SIM'].isGPitem(mnemonic):
            return REGISTRY['SIM'].getGPitem(mnemonic)
        return REGISTRY['SIM'].getTMitem(mnemonic)

    #==========================================================================
    def _refreshItem(self, param, config ):
        self.__aborted = False
        self.__currentParam = param
        name = param.name()
        param._setStatus(True)
        if name == "INVALID":
            param._setStatus(False)
        elif name == "TIMEOUT":
            import time
            time.sleep(1000)

        eng = (config.get(ValueFormat) == ENG)

        if (config.get(Wait)==True):
            timeout = config.get(Timeout)
            if timeout > 0:
                param.waitUpdate(timeout)
            else:
                param.waitUpdate()
        
        self.__currentParam = None
        if self.__aborted:
            self.__aborted = False
            REGISTRY['CIF'].write("Telemetry acquisition of " + name + " aborted", {Severity:WARNING})
            return [None,param._getStatus()]
        
        if eng:
            value = param._getEng()
        else:
            value = param._getRaw()
            
        param._setTime(TIME(NOW))
            
        return [value, param._getStatus()]

    #===========================================================================
    def _setLimit(self, param, limit, value, config ):
        REGISTRY['CIF'].write("Set limit for " + repr(param) + ": " + repr(limit) + "=" + repr(value))
        result = False
        return result

    #===========================================================================
    def _getLimit(self, param, limit, config ):
        REGISTRY['CIF'].write("Get limit for " + repr(param) + ": " + repr(limit))
        result = False
        return result

    #===========================================================================
    def _getLimits(self, param, config ):
        REGISTRY['CIF'].write("Get limits for " + repr(param))
        result = False
        return result

    #===========================================================================
    def _setLimits(self, param, limits, config ):
        REGISTRY['CIF'].write("Set limits for " + repr(param) + ": " + repr(limits))
        result = False
        return result
               
    #===========================================================================
    def _loadLimits( self, limitsList, useConfig ):
        result = False
        REGISTRY['CIF'].write("Loading limits from a file is not supported", WARNING)
        return result

    #===========================================================================
    def _onDriverAbort( self ):
        if self.__currentParam:
            self.__currentParam.abortUpdate()

    #===========================================================================
    def _onDriverInterrupt( self ):
        if self.__currentParam:
            self.__currentParam.abortUpdate()
               
################################################################################
# Interface handle
TM = TmInterface()
