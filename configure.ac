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
AC_INIT([SPELL], 
    [m4_esyscmd([scripts/src/version.sh])], 
    [juergen.gall@ses.com, raimondas.sasnauskas@ses.com])
AM_INIT_AUTOMAKE([1.9 tar-pax -Wall -Werror foreign subdir-objects])

AC_CONFIG_SRCDIR([configure.ac])
AC_CONFIG_HEADERS([config.h])
AC_CONFIG_MACRO_DIR([m4])
AM_DISABLE_STATIC

#------------------------------------------------------------------------------# 
# Checks for basic standard programs
#------------------------------------------------------------------------------# 
AC_PROG_CC
AC_PROG_CXX
m4_ifdef([AM_PROG_AR], [AM_PROG_AR])
AC_PROG_LIBTOOL
AC_PROG_INSTALL
AC_PROG_MAKE_SET

#AM_PATH_PYTHON

AC_CHECK_PROG(protoc, protoc, true)
if [ test x$protoc != xtrue ]
then
    AC_MSG_ERROR([Protobuf compiler protoc required to compile SPELL])
fi

#------------------------------------------------------------------------------#
# Checks for some development headers (POSIX-threads, C++ standard library)
#------------------------------------------------------------------------------#
AC_HEADER_STDC
AC_LANG_CPLUSPLUS
PYTHON2_VERSION=2
AC_PYTHON_DEVEL_V2
PYTHON3_VERSION=3
AC_PYTHON_DEVEL_V3
AC_CHECK_HEADER(pthread.h,,AC_MSG_ERROR([pthread.h is required (POSIX threads library)]))
AC_CHECK_HEADER(ncurses.h,,AC_MSG_ERROR([ncurses.h is required]))
PKG_PROG_PKG_CONFIG

AC_CHECK_LIB([rt]       , [clock_gettime]    ,[:],[AC_MSG_ERROR(rt library is required)])
AC_CHECK_LIB([dl]       , [dlopen]           ,[:],[AC_MSG_ERROR(dl library is required)])
AC_CHECK_LIB([util]     , [openpty]          ,[:],[AC_MSG_ERROR(util library is required)])
AC_CHECK_LIB([xerces-c] , [_init]            ,[:],[AC_MSG_ERROR(xerces-c library is required)])
AC_CHECK_LIB([z]        , [inflate]          ,[:],[AC_MSG_ERROR(zlib library is required)])
AC_CHECK_LIB([menu]     , [_init]            ,[:],[AC_MSG_ERROR(menu library is required)])
AC_CHECK_LIB([ncurses]  , [initscr]          ,[:],[AC_MSG_ERROR(ncurses library is required)])
AC_CHECK_LIB([ncursesw]  , [initscr]          ,[:],[AC_MSG_ERROR(ncursesw library is required)])
AC_CHECK_LIB([log4cplus], [_init]            ,[:],[AC_MSG_ERROR(log4cplus library is required)])

#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL hifly driver
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([hiflydriver],
              [AS_HELP_STRING([--enable-hiflydriver],[Compile the hifly driver (default is no) ])],
              [hiflydriver=true],
              [hiflydriver=false])
AM_CONDITIONAL([WITH_DRIVER_HIFLY], [test x$hiflydriver = xtrue])

#------------------------------------------------------------------------------#
# WITH argument to indicate the EXIF version for the hifly driver 2.0
#------------------------------------------------------------------------------#
AC_ARG_WITH([exif],
            [AS_HELP_STRING([--with-exif=5 or 6],[hifly EXIF version to be used ])],
            [
               case "${withval}" in
                   5) with_exif_version=EXIF5 ;;
                   6) with_exif_version=EXIF6 ;;
                   *) AC_MSG_ERROR([bad value ${withval} for --with-exif]) ;;
               esac
            ],
            [])

WITH_EXIF=
AS_IF([ test "x$with_exif_version" != x],
      [AC_SUBST([WITH_EXIF],[${with_exif_version}])],
      [])

#------------------------------------------------------------------------------#
# Hifly driver checks
#------------------------------------------------------------------------------#
AS_IF([test x$hiflydriver = xtrue],
      [
      if [ test -z "$WITH_EXIF" ]
      then
          AC_MSG_ERROR([Must provide the EXIF version (--with-exif) to compile the hifly driver])
      else
          echo "using EXIF version $WITH_EXIF"
      fi
      ],
      [])

AS_IF([test x$hiflydriver = xtrue],
      [
          # just check for existence, don't create -l flags
          AC_CHECK_LIB([boost_system], [_init], [AC_SUBST([BOOST_SYSTEM],[boost_system])], [
            AC_CHECK_LIB([boost-system], [_init], [AC_SUBST([BOOST_SYSTEM],[boost-system])], [
              AC_MSG_ERROR(boost-system library is required)
            ])
          ])

          AC_CHECK_LIB([boost_thread], [_init], [AC_SUBST([BOOST_THREAD],[boost_thread])], [
            AC_CHECK_LIB([boost-thread], [_init], [AC_SUBST([BOOST_THREAD],[boost-thread])], [
              AC_MSG_ERROR(boost-thread library is required)
            ])
          ])
          AC_CHECK_LIB([boost_chrono], [_init], [AC_SUBST([BOOST_CHRONO],[boost_chrono])], [
            AC_CHECK_LIB([boost-chrono], [_init], [AC_SUBST([BOOST_CHRONO],[boost-chrono])], [
              AC_MSG_ERROR(boost-chrono library is required)
            ])
          ])
          AC_CHECK_LIB([omniORB4]    , [_init], [:], [AC_MSG_ERROR(omniORB4 library is required)])
          AC_CHECK_LIB([omnithread]  , [_init], [:], [AC_MSG_ERROR(omnithread library is required)])
      ],
      [])

#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the scorpio driver
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([scorpiodriver],
              [AS_HELP_STRING([--enable-scorpiodriver],[Compile the scorpio driver (default is no) ])],
              [scorpiodriver=true],
              [scorpiodriver=false])
AM_CONDITIONAL([WITH_DRIVER_SCORPIO], [test x$scorpiodriver = xtrue])

#------------------------------------------------------------------------------#
# Scorpio driver checks
#------------------------------------------------------------------------------#
AS_IF([test x$scorpiodriver = xtrue],
      [
          AC_CHECK_LIB([zmq], [_init], [:], [AC_MSG_ERROR(zmq library is required)])
          AC_CHECK_PROG(swig, swig, true)
          if [ test x$swig != xtrue ]
          then
              AC_MSG_ERROR([SWIG required to compile the scorpio driver])
          fi
          SWIGFULL=`swig -version | grep "SWIG Version" | cut -d' ' -f3`
          AX_COMPARE_VERSION([$SWIGFULL],[ge],[1.3.36],[],
                             [AC_MSG_ERROR(SWIG version >= 1.3.36 required)])
      ],
      [])

#------------------------------------------------------------------------------#
# ENABLE argument to activate or deactivate the SPELL Unit Tests component
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([tests],
              [AS_HELP_STRING([--enable-tests],[Compile the SPELL tests (default is no) ])],
              [tests=true],
              [tests=false])
AM_CONDITIONAL([WITH_TESTS], [test x$tests = xtrue])

AS_IF([test x$tests = xtrue],
      [
      AC_CHECK_LIB([cppunit], [_init], [:], [AC_MSG_ERROR(cppunit library is required)])
      ],
      [])

#------------------------------------------------------------------------------#
# ENABLE argument to include PACE
#------------------------------------------------------------------------------#
AC_ARG_ENABLE([pace],
              [AS_HELP_STRING([--enable-pace],[Include PACE (default is no) ])],
              [pace=true],
              [pace=false])
AM_CONDITIONAL([WITH_PACE], [test x$pace = xtrue])


# Address Sanitizer

AC_MSG_CHECKING([whether to build with Address Sanitizer support])
AC_ARG_ENABLE(address-sanitizer,
    [AS_HELP_STRING([--enable-address-sanitizer],[Enable Address Sanitizer support @<:@default=no@:>@.])],
    [
        case "${enableval}" in
        no|yes)
            enable_address_sanitizer=${enableval}
            ;;
        *)
            AC_MSG_ERROR([Invalid value ${enableval} for --enable-address-sanitizer])
            ;;
        esac
    ],
    [enable_address_sanitizer=no])
AC_MSG_RESULT(${enable_address_sanitizer})



convert_warning_flags_to_positive() {
    flags_to_check=""
    warning_flag_backup=""
    for option in ${1}; do
        case "$option"
        in
           -Wno-*)
                flags_to_check="${flags_to_check} -W${option##-Wno-}"
                warning_flag_backup="${warning_flag_backup} ${option##-Wno-}";;
            *)
                flags_to_check="${flags_to_check} ${option}";;
        esac
    done
}

restore_negative_form_of_warning_flags() {
    final_flags=${1}
    for positive_option in ${warning_flag_backup}; do
        final_flags=$(echo $ECHO_N $final_flags | $SED "s|-W${positive_option}|-Wno-${positive_option}|g")
    done
    unset flags_to_check
    unset warning_flag_backup
}

check_prospective_CFLAGS() {
    convert_warning_flags_to_positive "${PROSPECTIVE_CFLAGS}"
    #AX_CHECK_COMPILER_OPTIONS([C], ${flags_to_check})
    restore_negative_form_of_warning_flags "${CFLAGS}"
    CFLAGS=${final_flags}
    unset final_flags

}

check_prospective_CXXFLAGS() {
    convert_warning_flags_to_positive "${PROSPECTIVE_CXXFLAGS}"
    #AX_CHECK_COMPILER_OPTIONS([C++], ${flags_to_check})
    restore_negative_form_of_warning_flags "${CXXFLAGS}"
    CXXFLAGS=${final_flags}
    unset final_flags
}

if test "${enable_address_sanitizer}" = "yes" ; then

    PROSPECTIVE_CFLAGS="-fsanitize-recover=address -fsanitize=address -O1 -fno-omit-frame-pointer -g"
    PROSPECTIVE_CXXFLAGS="-fsanitize-recover=address -fsanitize=address -O1 -fno-omit-frame-pointer -g "

    # Check if the compilers support address sanitizer
    check_prospective_CFLAGS
    check_prospective_CXXFLAGS
fi


#------------------------------------------------------------------------------#
# Specification of all the makefiles to be generated.
#------------------------------------------------------------------------------#
MAKEFILES=Makefile

AC_CONFIG_FILES([Makefile \

                 lib/Makefile \

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

                 config/Makefile \
                 data/Makefile \
                 scripts/Makefile \

                 src/Makefile            \
                 src/context/Makefile    \
                 src/executor/Makefile   \
                 src/executorcmd/Makefile\
                 src/listener/Makefile   \
                 src/service/Makefile   \
                 src/shell/Makefile      \
                 src/spell/Makefile      \
                 src/ping/Makefile      \

                 drivers/Makefile        \
                 drivers/example/Makefile\
                 drivers/hifly2/Makefile \
                 drivers/scorpio/Makefile
               ])
AC_OUTPUT
