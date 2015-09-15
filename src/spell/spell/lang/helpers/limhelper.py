###################################################################################
## MODULE     : spell.lang.helpers.limhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for limit management
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

from basehelper import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *
from spell.lib.exception import SyntaxException
from spell.lang.functions import *
from spell.lib.registry import *
from spell.lib.adapter.tm_item import TmItemClass

################################################################################
class AdjustLimits_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the AdjustLimits wrapper.
    """    
    __verifyList = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Limit adjustment"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)==0:
            raise SyntaxException("Expected a TM verification list")
        self.__verifyList = args[0]
        if type(self.__verifyList)!=list:
            raise SyntaxException("Expected a TM verification list")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        
        result = False
        for condition in self.__verifyList:
            paramName = condition[0]
            paramValue = condition[2]
            operator = condition[1]
            if operator != eq: continue
            if type(paramValue)==str: # Status parameters
                limits = {Expected:paramValue}
                result = REGISTRY['TM'].setLimits( paramName, limits, config = self.getConfig() )
            else:
                tolerance = self.getConfig(Tolerance)
                limits = {}
                limits[LoRed] = paramValue - tolerance 
                limits[LoYel] = paramValue - tolerance 
                limits[HiYel] = paramValue + tolerance 
                limits[HiRed] = paramValue + tolerance 
                result = REGISTRY['TM'].setLimits( paramName, limits, config = self.getConfig() )
                
        return [False,result,None,None]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry adjust limits", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip limit adjustment", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel limit adjustment", {Severity:WARNING} )
        return [False, False]
