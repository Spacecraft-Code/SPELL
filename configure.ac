################################################################################
#                                                                               
#  FILE       : configure.ac                                                   
#  DESCRIPTION: automake and autoconf configuration file, and source for the   
#               'configure' script. Describes the structure and organization   
#               of all makefiles to be generated in order to build SPELL.      
#                                                                              
#  LICENSE ------------------------------------------------------------------  
#                                                                              
#
#  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
#
#  This file is part of SPELL.
#
# SPELL is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# SPELL is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with SPELL. If not, see <http://www.gnu.org/licenses/>.
#
################################################################################

AC_INIT([SPELL], [2.4], [])
AM_INIT_AUTOMAKE([1.9 tar-pax -Wall -Werror foreign])
AC_CONFIG_HEADERS([config.h])
AC_CONFIG_MACRO_DIR([m4])
CXXFLAGS=


#------------------------------------------------------------------------------# 
# Checks for basic standard programs
#------------------------------------------------------------------------------# 
AC_PROG_CC
AC_PROG_CXX
AC_PROG_LIBTOOL
AC_PROG_INSTALL
AC_PROG_MAKE_SET
AM_PATH_PYTHON


#------------------------------------------------------------------------------#
# Checks for some development headers (POSIX-threads,C++ standard library)
#------------------------------------------------------------------------------#
AC_HEADER_STDC
AC_LANG_CPLUSPLUS
AC_CHECK_HEADER(pthread.h,,AC_MSG_ERROR(POSIX threads  library is required))

AC_CHECK_LIB([rt]       , [clock_gettime]    ,,[AC_MSG_ERROR(rt library is required)]      )
AC_CHECK_LIB([dl]       , [dlopen]           ,,[AC_MSG_ERROR(dl library is required)]      )
AC_CHECK_LIB([util]     , [openpty]          ,,[AC_MSG_ERROR(util library is required)]    )
AC_CHECK_LIB([xerces-c] , [_init]            ,,[AC_MSG_ERROR(xerces-c library is required)])
AX_PYTHON_DEVEL([])


#------------------------------------------------------------------------------#
# Conditional: check the platform in order to know which scripts to use
#------------------------------------------------------------------------------#
AM_CONDITIONAL(PLATFORM_LINUX, test ! -z `echo $host_os | grep "linux"`)

#------------------------------------------------------------------------------#
# Conditional: check rsync availability
#------------------------------------------------------------------------------#
AM_CONDITIONAL(HAVE_RSYNC, test `which rsync`)

#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL Executor component
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([executor],
              [AS_HELP_STRING([--enable-executor],[Compile the executor process (default is yes) ])] ,
              [executor=true],
              [executor=true])
AM_CONDITIONAL([WITH_EXECUTOR], [test x$executor = xtrue]) 


#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL Executor Command Line
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([executorcmd], 
              [AS_HELP_STRING([--enable-executorcmd],[Compile the command line executor (default is no) ])],
              [executorcmd=true],
              [executorcmd=false])
AM_CONDITIONAL([WITH_EXECUTOR_CMD], [test x$executorcmd = xtrue])


#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL GUI component
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([gui],
              [AS_HELP_STRING([--enable-gui],[Compile the SPELL-GUI (default is no) ])],
              [gui=true],
              [gui=false])
AM_CONDITIONAL([WITH_GUI], [test x$gui = xtrue])


#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL standalone driver
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([driverstd],
              [AS_HELP_STRING([--enable-driverstd],[Compile the SPELL standalone driver (default is no) ])],
              [driverstd=true],
              [driverstd=false])
AM_CONDITIONAL([WITH_DRIVER_STD], [test x$driverstd = xtrue])


#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL Shell component
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([shell],
              [AS_HELP_STRING([--enable-shell],[Compile the SPELL shell (default is yes) ])],
              [shell=true],
              [shell=true])
AM_CONDITIONAL([WITH_SHELL], [test x$shell = xtrue])


#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL Listener component
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([listener],
              [AS_HELP_STRING([--enable-listener],[Compile the SPELL listener process (default is yes) ])],
              [listener=true],
              [listener=true])
AM_CONDITIONAL([WITH_LISTENER], [test x$listener = xtrue])


#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL Context component
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([context],
              [AS_HELP_STRING([--enable-context],[Compile the SPELL context process (default is yes) ])],
              [context=true],
              [context=true])
AM_CONDITIONAL([WITH_CONTEXT], [test x$context = xtrue])

#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the documentation build
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([documentation],
              [AS_HELP_STRING([--enable-documentation],[Compile the SPELL documentation (default is no) ])],
              [documentation=true],
              [documentation=false])
AM_CONDITIONAL([WITH_DOCUMENTATION], [test x$documentation = xtrue])


#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the sample procedures
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([procs],
              [AS_HELP_STRING([--enable-procs],[Include the sample procedures (default is no) ])],
              [procs=true],
              [procs=false])
AM_CONDITIONAL([WITH_PROCS], [test x$procs = xtrue])

#------------------------------------------------------------------------------#
# WITH argument to indicate the path to the Eclipse installation used to 
# compile the RCP applications. Shall be of version 3.7.
#------------------------------------------------------------------------------#
AC_ARG_WITH([eclipse],
            [AS_HELP_STRING([--with-eclipse=PATH],[eclipse installation used to compile RCP components])],
            [ 
              echo "using eclipse at ${withval}"
              if [[ ! -d ${withval} ]]
              then
                  AC_MSG_ERROR([Unable to find the eclipse installation: ${withval}])
              fi
              with_eclipse_inst=${withval}
            ],
            [])

WITH_ECLIPSE=
AS_IF([ test "x$with_eclipse_inst" != x],
      [AC_SUBST([WITH_ECLIPSE],[${with_eclipse_inst}])],
      [])

#------------------------------------------------------------------------------#
# WITH argument to indicate the path to the Log4CPlus library
#------------------------------------------------------------------------------#
AC_ARG_WITH([log4cplus],
            [AS_HELP_STRING([--with-log4cplus=PATH],[path to the Log4C++ libraries])],
            [
              echo "using Log4C++ libraries at ${withval}"
              if [[ ! -d ${withval} ]]
              then
                  AC_MSG_ERROR([Unable to find the Log$C++ libraries directory: ${withval}])
              fi
              with_log4cplus=${withval}
            ],
            [
              echo "using default Log4C++ libraries path (/usr)"
              with_log4cplus=/usr
            ])

WITH_LOG4CPLUS=
AS_IF([ test "x$with_log4cplus" != x],
      [AC_SUBST([WITH_LOG4CPLUS],[${with_log4cplus}])],
      [])

#------------------------------------------------------------------------------#
# Some checks to do in case of compiling SPELL-DEV or SPELL-GUI
#------------------------------------------------------------------------------#
AS_IF([ test x$gui = xtrue],
      [
          if [[ -z "$WITH_ECLIPSE" ]]
          then
              AC_MSG_ERROR([Must provide an eclipse installation path (--with-eclipse) to compile the SPELL-GUI])
          fi
      ],
      [])

#------------------------------------------------------------------------------#
# Some checks to do in case of compiling tests
#------------------------------------------------------------------------------#
AS_IF([ test x$tests = xtrue],
      [
	   AC_MSG_WARN(Tests and unit tests programs enabled)
           AC_CHECK_LIB([cppunit],[_init],,[AC_MSG_ERROR(cppunit library is required to compile unit tests)])
      ],
      [
	   AC_MSG_WARN(Tests and unit tests programs disabled)
      ])

#------------------------------------------------------------------------------#
# Some checks to do in case of compiling SPELL executor command line
#------------------------------------------------------------------------------#
AS_IF([ test x$executorcmd = xtrue],
      [
           AC_CHECK_LIB([menu],[_init],,[AC_MSG_ERROR(menu library is required to compile the command line executor)])

           # Checks for ncurses.
           AC_CHECK_HEADERS([ncurses.h], [
               AC_CHECK_LIB(ncurses, initscr, [
                  LIBS="$LIBS -lncurses"
                  AC_DEFINE(HAVE_LIBNCURSES, 1, [Define to 1 if you have the 'ncurses' library (-lncurses).])
               ], AC_MSG_ERROR(The ncurses library is required in order to build the command line executor!))
           ], AC_MSG_ERROR(The ncurses header is required in order to build the command line executor!))
      ],
      [])

AM_CONDITIONAL([WITH_LIBS], [test x$executor$executorcmd$shell$tests != xfalsefalsefalsefalse ])

#------------------------------------------------------------------------------#
# Some checks to do in case of compiling documentation
#------------------------------------------------------------------------------#
AM_CONDITIONAL([HAVE_DOXYGEN], [test x$`which doxygen &> /dev/null` != x])

#------------------------------------------------------------------------------#
# Specification of all the makefiles to be generated.
#------------------------------------------------------------------------------#

MAKEFILES=Makefile

AC_CONFIG_FILES([Makefile \

                 lib/Makefile   \

                 lib/SPELL_CIF/Makefile  \
                 lib/SPELL_CTX/Makefile  \
                 lib/SPELL_EXC/Makefile  \
                 lib/SPELL_LST/Makefile  \
                 lib/SPELL_PRD/Makefile  \
                 lib/SPELL_WS/Makefile   \
                 lib/SPELL_CFG/Makefile  \
                 lib/SPELL_CIFC/Makefile \
                 lib/SPELL_CIFS/Makefile \
                 lib/SPELL_IPC/Makefile  \
                 lib/SPELL_PRC/Makefile  \
                 lib/SPELL_SYN/Makefile  \
                 lib/SPELL_WRP/Makefile  \
                 lib/SPELL_UTIL/Makefile \
                 lib/SPELL_SDB/Makefile  \
                 lib/SPELL_DTA/Makefile  \

                 doc/Makefile \
                 procs/Makefile \
                 config/Makefile \
                 rcp/Makefile \
                 data/Makefile \
                 scripts/Makefile \
                 scripts/linux/Makefile \

                 src/Makefile            \
                 src/spell/Makefile      \
                 src/executor/Makefile   \
                 src/executorcmd/Makefile\
                 src/context/Makefile    \
                 src/listener/Makefile   \
                 src/shell/Makefile      \
                 src/spel-gui/Makefile   \

                 drivers/Makefile        \
                 drivers/standalone/Makefile \
               ])
AC_OUTPUT()

