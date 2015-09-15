#!/usr/bin/env bash

LIBNAME=$1
MAKEFILE_AM=$LIBNAME/Makefile.am

[[ -z "$1" ]] && echo "ERROR: no library name given" && exit 1
[[ ! -d $LIBNAME/src ]] && echo "ERROR: source directory not found: $LIBNAME/src" && exit 1
[[ -f $MAKEFILE_AM ]] && echo "WARNING: overwritting Makefile.am" 

echo 'include ${top_srcdir}/common.mk' > $MAKEFILE_AM
echo "lib_LTLIBRARIES= lib${LIBNAME}.la" >> $MAKEFILE_AM
echo "lib${LIBNAME}_la_LDFLAGS = -version-info 1:0:0" >> $MAKEFILE_AM
echo "lib${LIBNAME}_la_SOURCES = \\" >> $MAKEFILE_AM

cd $LIBNAME/src > /dev/null
SOURCES=`find . -name "*.C"` 
cd - > /dev/null

SRC_COUNT=`echo $SOURCES | wc -w`
COUNT=1
for SRC in $SOURCES
do
	SRC=`echo $SRC | sed 's/.\///'`
	if [[ "$COUNT" != "$SRC_COUNT" ]]
	then
	       	echo "                src/$SRC \\" >> $MAKEFILE_AM
	else
	       	echo "                src/$SRC" >> $MAKEFILE_AM
	fi
	let COUNT=$COUNT+1
done
echo " " >> $MAKEFILE_AM

