###################################################################################
## MODULE     : spell.config.constants
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Configuration constants definitions
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
# SPELL Imports
#*******************************************************************************

#*******************************************************************************
# Local Imports
#*******************************************************************************

#*******************************************************************************
# System Imports
#*******************************************************************************

#*******************************************************************************
# Exceptions
#*******************************************************************************

#*******************************************************************************
# Module globals
#*******************************************************************************

# Default configuration file name
CONFIG_FILE_NAME = "spell_config.xml"

# Sections of the configuration file
LISTENER = "listener"
CONTEXT  = "context"
EXECUTOR = "executor"
DRIVERS  = "drivers"
DRIVER   = "driver"
CONTEXTS = "contexts"
FAMILIES = "families"
LANGUAGE = "language"
COMMON   = "common"

# Sections listed here will become groups of properties in the config reader
CONFIG_SECTIONS = [ LISTENER, CONTEXT, EXECUTOR, COMMON ]

################################################################################
