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

include ${top_srcdir}/common.mk

install-exec-hook:
	mkdir -p ${exec_prefix}/doc
if HAVE_RSYNC 
	$(RSYNC) ${top_srcdir}/doc/* ${exec_prefix}/doc/
else 
	cp -r ${top_srcdir}/doc/* ${exec_prefix}/doc/.
	find ${exec_prefix}/doc -name ".svn" | xargs rm -rf
	rm -f ${exec_prefix}/doc/Makefile*
endif 
