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
from spell.lib.exception import DriverException
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
import spell.lib.adapter.resources.ResourceInterface as superClass

###############################################################################
# The resource management driver interface. This class is in charge of
# managing the underlying GCS resources like configuration parameters.
###############################################################################
class ResourceInterface(superClass):
    
    #==========================================================================
    def __init__(self):
        """
        Perform the initialization of resource handling data here, after the 
        superclass initialization
        """
        superClass.__init__(self)
        # TODO Your initialization stuff here
    
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        """
        Start the internal driver resource mgmt mechanisms
        
        ctxConfig : provides the current context information (S/C name and others)
        drvConfig : provides the configuration parameters for this driver according
                    to the xml config file. 
        """
        superClass.setup(self, ctxConfig, drvConfig)
        # TODO Your startup stuff here

    #==========================================================================
    def cleanup(self):
        """
        Perform the cleanup of the internal driver mechanisms.
        """
        superClass.cleanup(self)
        # TODO Release resources and cleanup stuff here
        
    
    #==========================================================================
    def _setResource(self, name, value, config = {} ):
        """
        Assign the value of one resource or configuration parameter in the GCS
        
        name: resource identifier
        value: resource value
        config: configuration modifiers
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the resource assignment

    #==========================================================================
    def _getResource(self, name, config = {} ):
        """
        Acquire the value of one resource or configuration parameter in the GCS
        
        name: resource identifier
        config: configuration modifiers
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the resource acquisition. Return the resource value:
        # return theValue

    #==========================================================================
    def _getResourceStatus(self, name, config = {} ):
        """
        Acquire the current status of one resource or configuration parameter 
        in the GCS 
        
        name: resource identifier
        config: configuration modifiers
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the resource acquisition. 
        # return theStatus

    #==========================================================================
    def _isResourceOK(self, name, config = {} ):
        """
        Check if the current value of one resource or configuration parameter 
        in the GCS is correct or incorrect 
        
        name: resource identifier
        config: configuration modifiers
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the resource acquisition. 
        # return True/False

    #==========================================================================
    def _setLink(self, name, enable, config = {}):
        """
        Enable or disable a GCS link (TM, TC or others)
        
        name: link  identifier
        enable: True/False, indicates if the link needs to be enabled or disabled
        config: configuration modifiers
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the resource acquisition. 
        # return True/False depending on success
        
    #==========================================================================
    def _checkLink(self, name):
        """
        Check if a GCS link (TM, TC or others) is enabled or disabled
        
        name: link  identifier
        config: configuration modifiers
        """
        raise DriverException("Service not available in this driver")
        # TODO if the service is supported remove the exception raise and
        # implement the resource acquisition. 
        # return True/False if the link is enabled/disabled
    

################################################################################
# Interface handle
RSC = ResourceInterface()
        
            
