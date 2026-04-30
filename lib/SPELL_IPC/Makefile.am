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

include ${top_srcdir}/common.mk

lib_LTLIBRARIES = libSPELL_IPC_v2.la libSPELL_IPC_v3.la

libSPELL_IPC_v2_la_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON2_CPPFLAGS} -Wno-pragmas

libSPELL_IPC_v2_la_LDFLAGS =  ${AM_LDFLAGS} ${PYTHON2_LDFLAGS} ${PYTHON2_EXTRA_LIBS} ${PYTHON2_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

libSPELL_IPC_v2_la_SOURCES = src/SPELLipcDataChunk.C \
                          src/SPELLipcIncoming.C \
                          src/SPELLipcMessage.C \
                          src/SPELLipcServerInterface.C \
                          src/SPELLipcServerInterfaceSingle.C \
                          src/SPELLipcChannel.C \
                          src/SPELLsocket.C \
                          src/SPELLipcClientInterface.C \
                          src/SPELLipcHelper.C \
                          src/SPELLipcInput.C \
                          src/SPELLipcMessageMailbox.C \
                          src/SPELLipcOutput.C \
                          src/SPELLipcTrash.C

libSPELL_IPC_v3_la_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON3_CPPFLAGS} -Wno-pragmas

libSPELL_IPC_v3_la_LDFLAGS =  ${AM_LDFLAGS} ${PYTHON3_LDFLAGS} ${PYTHON3_EXTRA_LIBS} ${PYTHON3_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

libSPELL_IPC_v3_la_SOURCES = src/SPELLipcDataChunk.C \
                          src/SPELLipcIncoming.C \
                          src/SPELLipcMessage.C \
                          src/SPELLipcServerInterface.C \
                          src/SPELLipcServerInterfaceSingle.C \
                          src/SPELLipcChannel.C \
                          src/SPELLsocket.C \
                          src/SPELLipcClientInterface.C \
                          src/SPELLipcHelper.C \
                          src/SPELLipcInput.C \
                          src/SPELLipcMessageMailbox.C \
                          src/SPELLipcOutput.C \
                          src/SPELLipcTrash.C
                          