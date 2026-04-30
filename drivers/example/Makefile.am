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

DRIVER=example
DRIVER_SRC=${srcdir}/python
DRIVER_CFG=${srcdir}/config
DRIVER_DST=${exec_prefix}/drivers/${DRIVER}
CONFIG_DST=${exec_prefix}/config/spell

all-local:
		find ${DRIVER_SRC} -type f -name "*.py" -print0 | xargs -0 -n1 sh -c LD_LIBRARY_PATH=${SPELL_COTS}/lib ${PYTHON2} ${top_srcdir}/scripts/src/SPELL-Compile
		find ${DRIVER_SRC} -type f -name "*.py" -print0 | xargs -0 -n1 sh -c LD_LIBRARY_PATH=${SPELL_COTS}/lib ${PYTHON3} ${top_srcdir}/scripts/src/SPELL-Compile

install-exec-hook:
		mkdir -p ${DRIVER_DST}
		cp -ur ${DRIVER_SRC}/* ${DRIVER_DST}
		mkdir -p ${CONFIG_DST}
		cp -u ${DRIVER_CFG}/driver_${DRIVER}.xml ${exec_prefix}/config/spell
