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

DRIVER=example
DRIVER_SRC=src
DRIVER_CFG=config

all-local:
	if [[ ! -d ${DRIVER_SRC} ]] ;\
	then \
		cp -r ${top_srcdir}/drivers/${DRIVER}/${DRIVER_SRC} . > /dev/null ;\
	fi  
	${top_srcdir}/py-compiledir ${DRIVER_SRC}

clean-local:
	find ${DRIVER_SRC} -name "*.pyc" | xargs rm -f
	find ${DRIVER_SRC} -name "*.pyo" | xargs rm -f
	rm -f ${DRIVER_SRC}/build.date

install-exec-hook:
	if [[ ! -d ${exec_prefix}/drivers ]] ;\
        then \
                mkdir ${exec_prefix}/drivers ;\
        fi
	rm -rf ${exec_prefix}/drivers/${DRIVER}
	cp -r ${DRIVER_SRC} ${exec_prefix}/drivers/${DRIVER}
	find ${exec_prefix}/drivers/${DRIVER} -name ".svn" | xargs rm -rf
	find ${exec_prefix}/drivers/${DRIVER} -name ".py" | xargs rm -f
	mkdir -p ${exec_prefix}/config/spell
	cp -f ${DRIVER_CFG}/driver_${DRIVER}.xml ${exec_prefix}/config/spell/.
