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

if [[ "$DRIVER" == "hifly" ]]; then
    DRIVER="--enable-hiflydriver --with-exif=6"
elif [[ "$DRIVER" == "scorpio" ]]; then
    DRIVER="--enable-scorpiodriver"
elif [[ "$DRIVER" == "fuzzer" ]]; then
    DRIVER="--enable-fuzzerdriver"
elif [[ "$DRIVER" == "none" ]]; then
    DRIVER=""
fi

mkdir -p build
rm -rf build/* > /dev/null 2>&1


autoreconf -fi . || die "autoconf failed"

cd build
set -x
../configure --enable-address-sanitizer  --prefix=$SPELL_HOME $DRIVER --enable-pace CPPFLAGS="-I$SPELL_COTS/include -fno-omit-frame-pointer $@" LDFLAGS="-rdynamic -ldw -Wl,--copy-dt-needed-entries -L$SPELL_COTS/lib" || die "configure failed"
set +x
make -j4 -s || die "build failed"

make install || die "install failed"
