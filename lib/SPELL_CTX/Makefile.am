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

lib_LTLIBRARIES = libSPELL_CTX_v2.la libSPELL_CTX_v3.la

libSPELL_CTX_v2_la_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON2_CPPFLAGS}

libSPELL_CTX_v2_la_LDFLAGS = ${AM_LDFLAGS} ${PYTHON2_LDFLAGS} ${PYTHON2_EXTRA_LIBS} ${PYTHON2_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

libSPELL_CTX_v2_la_SOURCES = src/SPELLclient.C \
                          src/SPELLclientInterestList.C \
                          src/SPELLclientIPC.C \
                          src/SPELLclientManager.C \
                          src/SPELLclientNotifier.C \
                          src/SPELLcontext.C \
                          src/SPELLdataHelper.C \
                          src/SPELLexecutor.C \
                          src/SPELLexecutorIPC.C \
                          src/SPELLexecutorOperation.C \
                          src/SPELLexecutorManager.C \
                          src/SPELLexecutorModel.C \
                          src/SPELLservice.C \
                          src/SPELLserviceManager.C \
                          src/SPELLlistenerIPC.C  \
                          src/SPELLpersistencyTable.C \
                          src/SPELLsharedData.C \
                          src/SPELLdataTable.C 

                          
libSPELL_CTX_v3_la_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON3_CPPFLAGS}

libSPELL_CTX_v3_la_LDFLAGS = ${AM_LDFLAGS} ${PYTHON3_LDFLAGS} ${PYTHON3_EXTRA_LIBS} ${PYTHON3_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

libSPELL_CTX_v3_la_SOURCES = src/SPELLclient.C \
                          src/SPELLclientInterestList.C \
                          src/SPELLclientIPC.C \
                          src/SPELLclientManager.C \
                          src/SPELLclientNotifier.C \
                          src/SPELLcontext.C \
                          src/SPELLdataHelper.C \
                          src/SPELLexecutor.C \
                          src/SPELLexecutorIPC.C \
                          src/SPELLexecutorOperation.C \
                          src/SPELLexecutorManager.C \
                          src/SPELLexecutorModel.C \
                          src/SPELLservice.C \
                          src/SPELLserviceManager.C \
                          src/SPELLlistenerIPC.C  \
                          src/SPELLpersistencyTable.C \
                          src/SPELLsharedData.C \
                          src/SPELLdataTable.C 
                          