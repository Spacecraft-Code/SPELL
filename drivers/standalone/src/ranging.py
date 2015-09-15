###################################################################################
## MODULE     : ranging
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Ranging interface
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2013 SES ENGINEERING, Luxembourg S.A.R.L.
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
from spell.utils.log import LOG
from spell.lang.constants import WARNING
from spell.lang.modifiers import Severity
from spell.lib.registry import REGISTRY

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['RNG']

###############################################################################
# Superclass
import spell.lib.adapter.ranging
superClass = spell.lib.adapter.ranging.RngInterface

###############################################################################
class RngInterface( superClass ):

    """
    This class provides the Ranging management interface. 
    """

    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone RNG interface")
        
    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone RNG interface")
    
    #===========================================================================
    def _enableRanging(self, *args, **kargs):
        REGISTRY['CIF'].write("ENABLE RANGING", {Severity:WARNING})
        return 

    #===========================================================================
    def _disableRanging(self, *args, **kargs):
        REGISTRY['CIF'].write("DISABLE RANGING", {Severity:WARNING})
        return 

               
################################################################################
# Interface handle
RNG = RngInterface()
