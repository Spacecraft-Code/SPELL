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

bin_PROGRAMS = SPELL-Executor SPELL-Executor_v3

SPELL_Executor_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON2_CPPFLAGS}

SPELL_Executor_LDFLAGS = ${AM_LDFLAGS} ${PYTHON2_LDFLAGS} ${PYTHON2_EXTRA_LIBS} ${PYTHON2_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

SPELL_Executor_SOURCES = src/SPELLexecutorMain.C

SPELL_Executor_LDADD = ${top_builddir}/lib/SPELL_EXC/libSPELL_EXC_v2.la   \
                       ${top_builddir}/lib/SPELL_CIFS/libSPELL_CIFS_v2.la \
                       ${top_builddir}/lib/SPELL_DTA/libSPELL_DTA_v2.la   \
                       ${top_builddir}/lib/SPELL_WS/libSPELL_WS_v2.la     \
                       ${top_builddir}/lib/SPELL_WRP/libSPELL_WRP_v2.la   \
                       ${top_builddir}/lib/SPELL_CIF/libSPELL_CIF_v2.la   \
                       ${top_builddir}/lib/SPELL_IPC/libSPELL_IPC_v2.la   \
                       ${top_builddir}/lib/SPELL_PRD/libSPELL_PRD_v2.la   \
                       ${top_builddir}/lib/SPELL_SYN/libSPELL_SYN_v2.la   \
                       ${top_builddir}/lib/SPELL_CFG/libSPELL_CFG_v2.la   \
                       ${top_builddir}/lib/SPELL_UTIL/libSPELL_UTIL_v2.la
                       
SPELL_Executor_v3_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON3_CPPFLAGS}

SPELL_Executor_v3_LDFLAGS = ${AM_LDFLAGS} ${PYTHON3_LDFLAGS} ${PYTHON3_EXTRA_LIBS} ${PYTHON3_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

SPELL_Executor_v3_SOURCES = src/SPELLexecutorMain.C

SPELL_Executor_v3_LDADD = ${top_builddir}/lib/SPELL_EXC/libSPELL_EXC_v3.la   \
                       ${top_builddir}/lib/SPELL_CIFS/libSPELL_CIFS_v3.la \
                       ${top_builddir}/lib/SPELL_DTA/libSPELL_DTA_v3.la   \
                       ${top_builddir}/lib/SPELL_WS/libSPELL_WS_v3.la     \
                       ${top_builddir}/lib/SPELL_WRP/libSPELL_WRP_v3.la   \
                       ${top_builddir}/lib/SPELL_CIF/libSPELL_CIF_v3.la   \
                       ${top_builddir}/lib/SPELL_IPC/libSPELL_IPC_v3.la   \
                       ${top_builddir}/lib/SPELL_PRD/libSPELL_PRD_v3.la   \
                       ${top_builddir}/lib/SPELL_SYN/libSPELL_SYN_v3.la   \
                       ${top_builddir}/lib/SPELL_CFG/libSPELL_CFG_v3.la   \
                       ${top_builddir}/lib/SPELL_UTIL/libSPELL_UTIL_v3.la
                       