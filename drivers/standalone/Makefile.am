###################################################################################
## FILE       : Makefile.am
## DATE       : Mar 17, 2011
## PROJECT    : SPELL
## DESCRIPTION: Automake rules
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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

DRIVER=standalone
DRIVER_SRC=${top_srcdir}/drivers/${DRIVER}/src
DRIVER_CFG=${top_srcdir}/drivers/${DRIVER}/config
DRIVER_BUILD=${top_builddir}/drivers/${DRIVER}/build

all-local:
	if [[ -f ${DRIVER_BUILD} ]] ;\
        then \
                rm -rf ${DRIVER_BUILD} ;\
        fi; \
    mkdir -p ${DRIVER_BUILD}
	cp -rap ${DRIVER_SRC}/* ${DRIVER_BUILD}/.
	find ${DRIVER_BUILD} -name ".svn" | xargs rm -rf  

clean-local:
	if [[ -f ${DRIVER_BUILD} ]] ;\
        then \
                rm -rf ${DRIVER_BUILD} ;\
        fi

install-exec-hook:
	if [[ ! -d ${exec_prefix}/drivers ]] ;\
        then \
                mkdir ${exec_prefix}/drivers ;\
        fi
	rm -rf ${exec_prefix}/drivers/${DRIVER}
	mkdir -p ${exec_prefix}/drivers/${DRIVER}
	cp -rap ${DRIVER_BUILD}/* ${exec_prefix}/drivers/${DRIVER}/.
	mkdir -p ${exec_prefix}/config/spell
	cp -f ${DRIVER_CFG}/driver_${DRIVER}.xml ${exec_prefix}/config/spell/.

