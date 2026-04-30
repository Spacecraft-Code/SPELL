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

lib_LTLIBRARIES = libSPELL_SDB_v2.la libSPELL_SDB_v3.la

libSPELL_SDB_v2_la_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON2_CPPFLAGS}

libSPELL_SDB_v2_la_LDFLAGS = ${AM_LDFLAGS} ${PYTHON2_LDFLAGS} ${PYTHON2_EXTRA_LIBS} ${PYTHON2_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

libSPELL_SDB_v2_la_SOURCES = src/SPELLpyDatabaseMethods.C \
                          src/SPELLpyDatabaseModule.C \
                          src/SPELLdatabase.C \
                          src/SPELLdatabaseFile.C \
                          src/SPELLdatabaseFileSPB.C \
                          src/SPELLdatabaseFactory.C
                          
libSPELL_SDB_v3_la_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON3_CPPFLAGS}

libSPELL_SDB_v3_la_LDFLAGS = ${AM_LDFLAGS} ${PYTHON3_LDFLAGS} ${PYTHON3_EXTRA_LIBS} ${PYTHON3_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

libSPELL_SDB_v3_la_SOURCES = src/SPELLpyDatabaseMethods.C \
                          src/SPELLpyDatabaseModule.C \
                          src/SPELLdatabase.C \
                          src/SPELLdatabaseFile.C \
                          src/SPELLdatabaseFileSPB.C \
                          src/SPELLdatabaseFactory.C
