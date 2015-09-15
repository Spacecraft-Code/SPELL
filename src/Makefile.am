###################################################################################
## FILE       : Makefile.am
## DATE       : Mar 17, 2011
## PROJECT    : SPELL
## DESCRIPTION: Automake rules
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## SPELL is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## SPELL is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################
AUTOMAKE_OPTIONS = foreign
SUBDIRS=spell

if WITH_EXECUTOR
SUBDIRS += executor 
endif

if WITH_EXECUTOR_CMD
SUBDIRS += executorcmd
endif

if WITH_SHELL
SUBDIRS += shell
endif

if WITH_LISTENER
SUBDIRS += listener
endif

if WITH_CONTEXT
SUBDIRS += context
endif

if WITH_GUI
SUBDIRS += spel-gui
endif

