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

SRC=${srcdir}/spell
DST=${exec_prefix}/spell

PROTO=${abs_top_srcdir}/proto
PROTOS=${top_srcdir}/protos
STUB_DST=${DST}/spell/stubs

all-local:
		find ${SRC} -type f -name "*.py" -print0 | xargs -0 -n1 sh -c LD_LIBRARY_PATH=${SPELL_COTS}/lib ${PYTHON2} ${top_srcdir}/scripts/src/SPELL-Compile
		find ${SRC} -type f -name "*.py" -print0 | xargs -0 -n1 sh -c LD_LIBRARY_PATH=${SPELL_COTS}/lib ${PYTHON3} ${top_srcdir}/scripts/src/SPELL-Compile
	
install-exec-hook:
		mkdir -p ${DST}
		cp -ur ${SRC} ${DST}
		rm -f ${DST}/Makefile*
		mkdir -p ${STUB_DST}
		touch ${STUB_DST}/__init__.py
		${PYTHON2} -m grpc_tools.protoc -I${PROTOS} --python_out=${STUB_DST} --grpc_python_out=${STUB_DST} ${PROTOS}/SPELLDriver.proto ${PROTOS}/SPELLExecutor.proto
		mkdir -p spell/stubs/
		ln -sf ${PROTO}/OBus.proto spell/stubs/
		${PYTHON2} -m grpc_tools.protoc --proto_path=${builddir} --python_out=${DST} --grpc_python_out=${DST} spell/stubs/*
