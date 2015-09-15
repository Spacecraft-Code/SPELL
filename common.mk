###################################################################################
## FILE       : common.mk
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

AUTOMAKE_OPTIONS=foreign
#REMOVED -O2
AM_CPPFLAGS = -I$(top_srcdir)/include -I${SPELL_COTS}/include $(PYTHON_CPPFLAGS) -I${WITH_LOG4CPLUS}/include -fPIC -fno-strict-aliasing -g3 -Wall -c -fmessage-length=0 -MMD -MP
AM_LDFLAGS = $(PYTHON_LDFLAGS) $(PYTHON_EXTRA_LIBS) $(PYTHON_EXTRA_LDFLAGS) -L/usr/local/lib -L${WITH_LOG4CPLUS}/lib -llog4cplus
RSYNC=rsync -av --exclude-from=${top_srcdir}/exclude.list 

