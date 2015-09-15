###################################################################################
## MODULE     : interface.tc_sim_item
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: TC item for simulated model
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
from spell.lib.adapter.tc_item import TcItemClass

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

#*******************************************************************************
# Import definition
#*******************************************************************************
__all__ = ['TcItemSimClass']

#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class TcItemSimClass(TcItemClass):

    tmItemNames = None
    updateExpression = None
    execTime = None

    #==========================================================================    
    def __init__(self, model, name, description, tmItemNames, updateExpression, execTime):
        TcItemClass.__init__(self,model.tcClass,name,description)
        self.tmItemNames = tmItemNames
        self.updateExpression = updateExpression
        self.execTime = execTime
        
    #==========================================================================    
    def getTmItemNames(self):
        return self.tmItemNames
    
    #==========================================================================    
    def getTmChange(self):
        return self.updateExpression

    #==========================================================================    
    def getExecTime(self):
        return self.execTime

    #==========================================================================
    def __str__(self):
        return "[TC=" + repr(self.name()) + ", DESC=" + repr(self.desc()) + ", TM=" + repr(self.tmItemNames) + ", UPD=" + repr(self.updateExpression) + ", EXC=" + repr(self.execTime) + "]"    

################################################################################
