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

shift
DRIVER=$1
[[ "$DRIVER" == "hifly" ]] || [[ "$DRIVER" == "scorpio" ]] || [[ "$DRIVER" == "fuzzer" ]] ||  [[ "$DRIVER" == "example" ]] || die "Unknown driver \"$DRIVER\""
shift
DEPS=$1
shift

! [ -z $SPELL_COTS ] || die "Set SPELL_COTS environment variable"
[ -d $SPELL_COTS ] || die "cannot find the SPELL_COTS directory: $SPELL_COTS"
[ "$(ls -A $SPELL_COTS)" ] || die "SPELL_COTS directory $SPELL_COTS is empty"

export LD_LIBRARY_PATH=$SPELL_COTS/lib
export PATH=$SPELL_COTS/bin:$PATH


if [[ "$DRIVER" == "hifly" ]]; then
    DRIVER="--enable-hiflydriver --with-exif=6"
    DRIVER_FOLDER="hifly2"
elif [[ "$DRIVER" == "scorpio" ]]; then
    DRIVER="--enable-scorpiodriver"
    DRIVER_FOLDER="scorpio"
elif [[ "$DRIVER" == "fuzzer" ]]; then
    DRIVER="--enable-fuzzerdriver"
    DRIVER_FOLDER="fuzzer"
elif [[ "$DRIVER" == "example" ]]; then
    DRIVER="--enable-exampledriver"
    DRIVER_FOLDER="example"
fi

autoreconf -fi .|| die "autoreconf failed!"
mkdir -p build
rm -rf build/* > /dev/null 2>&1
cd build
set -x
LSAN_OPTIONS=verbosity=1:log_threads=1 ASAN_OPTIONS=halt_on_error=0 ../configure --disable-dependency-tracking --enable-address-sanitizer --prefix=$SPELL_HOME $DRIVER --enable-pace CPPFLAGS="-w -I$SPELL_COTS/include $@" LDFLAGS="-Wl,--copy-dt-needed-entries -L$SPELL_COTS/lib" 

for dep in ${DEPS}; do
    pushd lib/${dep}
    make -j4 -s && make install
    popd
done

cd drivers/${DRIVER_FOLDER}
set +x
make -j4 -s && make install
