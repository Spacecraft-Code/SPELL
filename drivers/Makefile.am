################################################################################
#
#  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
# 
#  This file is part of SPELL.
# 
#  SPELL is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
# 
#  SPELL is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#  GNU General Public License for more details.
# 
#  You should have received a copy of the GNU General Public License
#  along with SPELL. If not, see <http://www.gnu.org/licenses/>.
#
################################################################################

SUBDIRS = example

if WITH_DRIVER_HIFLY
SUBDIRS += hifly2
endif

if WITH_DRIVER_SCORPIO
SUBDIRS += scorpio
endif

SERVICE=${top_srcdir}/drivers/service
SERVICE_DST=${exec_prefix}/drivers/service

all-local:
		find ${SERVICE} -type f -name "*.py" -print0 | xargs -0 -n1 sh -c LD_LIBRARY_PATH=${SPELL_COTS}/lib ${PYTHON2} ${top_srcdir}/scripts/src/SPELL-Compile
		find ${SERVICE} -type f -name "*.py" -print0 | xargs -0 -n1 sh -c LD_LIBRARY_PATH=${SPELL_COTS}/lib ${PYTHON3} ${top_srcdir}/scripts/src/SPELL-Compile

install-exec-hook:
		mkdir -p ${SERVICE_DST}
		cp -ur ${SERVICE}/*.py ${SERVICE_DST}
