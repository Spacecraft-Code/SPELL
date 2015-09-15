###################################################################################
## FILE       : Makefile.am
## DATE       : Mar 17, 2011
## PROJECT    : SPELL
## DESCRIPTION: Automake rules
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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

include ${top_srcdir}/common.mk

install-exec-hook:
	mkdir -p ${exec_prefix}/config
if HAVE_RSYNC 
if WITH_LIBS
	$(RSYNC) ${top_srcdir}/config/server ${exec_prefix}/config/
	$(RSYNC) ${top_srcdir}/config/contexts ${exec_prefix}/config/
endif
	$(RSYNC) ${top_srcdir}/config/spell  ${exec_prefix}/config/
if WITH_GUI
	$(RSYNC) ${top_srcdir}/config/gui ${exec_prefix}/config/
endif

else 
	
if WITH_LIBS
	cp -r ${top_srcdir}/config/server ${exec_prefix}/config/.
	cp -r ${top_srcdir}/config/contexts ${exec_prefix}/config/.
endif
	cp -r ${top_srcdir}/config/spell ${exec_prefix}/config/.
if WITH_GUI
	cp -r ${top_srcdir}/config/gui ${exec_prefix}/config/.
endif
	find ${exec_prefix}/config -name ".svn" | xargs rm -rf
endif 

