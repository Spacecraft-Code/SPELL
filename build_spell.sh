#!/bin/bash

function die() {
    echo -e >&2 "ERROR: $@"
    echo "Usage: $(basename $0) install-path hifly|scorpio|fuzzer"
    exit 1
}

# sanity checks
[ $# -ge 2 ] || die "Invalid arguments.\n\
Please specify installation path and the driver."
SPELL_HOME=$1
[ -d $SPELL_HOME ] || die "cannot find the given installation directory: $SPELL_HOME"
! [ "$(ls -A $SPELL_HOME)" ] || die "installation directory $SPELL_HOME must be empty"
shift
DRIVER=$1
[[ "$DRIVER" == "hifly" ]] || [[ "$DRIVER" == "scorpio" ]] || [[ "$DRIVER" == "fuzzer" ]] || [[ "$DRIVER" == "none" ]] || die "Unknown driver \"$DRIVER\""
shift

! [ -z $SPELL_COTS ] || die "Set SPELL_COTS environment variable"
[ -d $SPELL_COTS ] || die "cannot find the SPELL_COTS directory: $SPELL_COTS"
[ "$(ls -A $SPELL_COTS)" ] || die "SPELL_COTS directory $SPELL_COTS is empty"

export LD_LIBRARY_PATH=$SPELL_COTS/lib
export PATH=$SPELL_COTS/bin:$PATH

export CC="gcc -m32"
export CXX="g++ -m32"

if [[ "$DRIVER" == "hifly" ]]; then
    DRIVER="--enable-hiflydriver --with-exif=6"
elif [[ "$DRIVER" == "scorpio" ]]; then
    DRIVER="--enable-scorpiodriver"
elif [[ "$DRIVER" == "fuzzer" ]]; then
    DRIVER="--enable-fuzzerdriver"
elif [[ "$DRIVER" == "none" ]]; then
    DRIVER=""
fi

git submodule init
git submodule update
cd proto
git checkout develop
cd ..

autoreconf -fi
mkdir -p build
rm -rf build/* > /dev/null 2>&1
cd build
set -x
../configure --prefix=$SPELL_HOME $DRIVER --enable-pace CPPFLAGS="-std=c++11 -I$SPELL_COTS/include $@" LDFLAGS="-L$SPELL_COTS/lib"
set +x
make -j4 -s && make install
