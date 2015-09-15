###################################################################################
## MODULE     : spell.lib.empty.resources
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Resources interface
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
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['RSC']

###############################################################################
# Superclass
import spell.lib.adapter.resources
superClass = spell.lib.adapter.resources.ResourceInterface

###############################################################################
class ResourceInterface(superClass):
    
    """
    DESCRIPTION:
        Empty resource management interface. 
    """
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig,drvConfig)
        LOG("Setup empty RSC interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup empty RSC interface")
    
    #==========================================================================
    def _setLink(self, name, enable, config = {}):
        raise DriverException("Cannot perform operation", "Not supported by driver")
        
    #==========================================================================
    def _checkLink(self, name):
        raise DriverException("Cannot perform operation", "Not supported by driver")
    
    #==========================================================================
    def _setResource(self, name, value):
        raise DriverException("Cannot perform operation", "Not supported by driver")

    #==========================================================================
    def _getResource(self, name):
        raise DriverException("Cannot perform operation", "Not supported by driver")

################################################################################
# Interface handle
RSC = ResourceInterface()
        
            