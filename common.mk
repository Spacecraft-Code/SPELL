###################################################################################
## FILE       : common.mk
## DATE       : May 11, 2017
## PROJECT    : SPELL
## DESCRIPTION: Common compilation and linking flags
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
AUTOMAKE_OPTIONS = subdir-objects foreign
AM_CPPFLAGS = -I${top_srcdir}/include -Wall -fno-strict-aliasing -fmessage-length=0 -MMD
AM_LDFLAGS = ${LIBS} -lxerces-c -lz -lrt -llog4cplus
RELEASE_LDFLAG = -release ${PACKAGE_VERSION}
