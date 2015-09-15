###################################################################################
## MODULE     : resources
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Resource management interface of the driver connection layer
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
from spell.lib.registry import REGISTRY

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
        Resource management library interface. This class is in charge of
        managing the underlying system resources.
    """
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone RSC interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone RSC interface")
    
    #==========================================================================
    def _setLink(self, name, enable, config = {}):
        pass
        
    #==========================================================================
    def _checkLink(self, name):
        return True
    
    #==========================================================================
    def _setResource(self, resourceName, resourceValue, config = {} ):
        LOG("Modify simulated resource '" + resourceName + "'")
        REGISTRY['SIM'].changeCFGitem(resourceName,resourceValue)

    #==========================================================================
    def _getResource(self, resourceName, config = {} ):
        LOG("Return simulated resource '" + resourceName + "'")
        return REGISTRY['SIM'].getCFGitem(resourceName)

    #==========================================================================
    def _getResourceStatus(self, resourceName, config = {} ):
        return True

    #==========================================================================
    def _isResourceOK(self, resourceName, config = {} ):
        return True


################################################################################
# Interface handle
RSC = ResourceInterface()
        
            