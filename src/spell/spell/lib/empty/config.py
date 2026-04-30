###################################################################################
## MODULE     : spell.lib.empty.config
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Configuration interface
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
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *

#*******************************************************************************
# Local Imports
#*******************************************************************************

#*******************************************************************************
# System Imports
#*******************************************************************************


###############################################################################
# Module import definition

__all__ = ['CONFIG']

INTERFACE_DEFAULTS = {}

###############################################################################
# Superclass
import spell.lib.adapter.config
superClass = spell.lib.adapter.config.ConfigInterface

###############################################################################
class ConfigInterface(superClass):

    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")

    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig,drvConfig)
        LOG("Setup empty CFG interface")

    #==========================================================================
    def cleanup(self, shutdown = False):
        superClass.cleanup(self, shutdown)
        LOG("Cleanup empty CFG interface")

###############################################################################
# Interface instance
CONFIG = ConfigInterface()
