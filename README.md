# SPELL

**Spacecraft Procedure Execution Language and Library** — a ground software framework for spacecraft operations, developed by SES Engineering.

> Current version: **2.6.8.4**  
> See [RELEASENOTES](RELEASENOTES) for the full changelog.

## Overview

SPELL provides:
- A Python-based **USL (User Script Language)** for writing spacecraft procedures
- A distributed process architecture: `SPELL-Listener` → `SPELL-Context` → `SPELL-Executor(s)`
- A driver layer for connecting to different Ground Control Systems (GCS)

## Requirements

- GCC/G++ with pthreads (32-bit build: `gcc -m32` / `g++ -m32`)
- Python development headers
- `xerces-c`, `log4cplus`, `librt`, `libdl`, `libutil`
- `SPELL_COTS` environment variable pointing to third-party dependencies directory
- Optional: `cppunit` (unit tests), `ncurses`+`menu` (command-line executor)

## Building

### Quick build scripts (recommended)

```bash
# Standard (32-bit, with submodule proto support)
./build_spell.sh <install-path> hifly|scorpio|fuzzer|none [extra-configure-flags]

# Ubuntu (64-bit, with address sanitizer)
./build_spell_ubuntu.sh <install-path> hifly|scorpio|fuzzer|none [extra-configure-flags]

# Driver only
./build_driver.sh <install-path> hifly|scorpio|fuzzer|example [deps] [extra-configure-flags]
```

`SPELL_COTS` must be set before running any build script.

### Manual build (Autotools)

```bash
autoreconf -fi
mkdir -p build && cd build
../configure --prefix=$SPELL_HOME [options] \
  CPPFLAGS="-I$SPELL_COTS/include" \
  LDFLAGS="-L$SPELL_COTS/lib"
make -j4 && make install
```

### Configure options

| Option | Default | Description |
|---|---|---|
| `--enable-executor` | on | Build SPELL-Executor |
| `--enable-context` | on | Build SPELL-Context |
| `--enable-listener` | on | Build SPELL-Listener |
| `--enable-shell` | on | Build SPELL-Shell |
| `--enable-executorcmd` | off | Command-line executor (requires ncurses) |
| `--enable-gui` | off | SPELL-GUI (requires `--with-eclipse=PATH`) |
| `--enable-hiflydriver` | off | Hifly GCS driver |
| `--enable-scorpiodriver` | off | Scorpio GCS driver |
| `--enable-fuzzerdriver` | off | Fuzzer driver |
| `--enable-exampledriver` | off | Example/reference driver |
| `--enable-driverstd` | off | Standalone driver |
| `--enable-pace` | off | PACE support |
| `--enable-tests` | off | Unit tests (requires cppunit) |
| `--enable-address-sanitizer` | off | Build with AddressSanitizer |
| `--enable-documentation` | off | Doxygen docs |
| `--enable-procs` | off | Sample procedures |
| `--with-eclipse=PATH` | — | Eclipse installation for GUI build |
| `--with-log4cplus=PATH` | `/usr` | log4cplus library path |
| `--with-exif=6` | — | EXIF version (required for hifly driver) |

## Runtime

### Environment variables

| Variable | Purpose |
|---|---|
| `SPELL_HOME` | Installation root (`bin/`, `lib/`, `config/`, `data/`) |
| `SPELL_COTS` | Third-party dependencies root |
| `SPELL_DATA` | Runtime data directory (default: `$SPELL_HOME/data`) |
| `SPELL_CONFIG` | Configuration directory (default: `$SPELL_HOME/config`) |

Source `$SPELL_HOME/bin/setup-environment.sh` to set `LD_LIBRARY_PATH` and derived variables.

### Starting the server

```bash
$SPELL_HOME/bin/SPELL-Server <SATELLITE_NAME>
# Looks for config at: $SPELL_CONFIG/server/server_<SAT>_<hostname>.xml
```

### Running unit tests

```bash
$SPELL_HOME/bin/SPELL-RunTests
# Runs all SPELL_unittest_* binaries from $SPELL_HOME/bin/
# Logs to $SPELL_HOME/log/unittests_<MODULE>.log
# Test data expected at $SPELL_HOME/data/UnitTests/
```

## Architecture

### Process hierarchy

```
SPELL-Listener  (entry point, default port 9980)
    └── SPELL-Context  (manages spacecraft context, spawns executors)
            └── SPELL-Executor  (one per running procedure, embeds CPython)
```

All processes communicate over TCP sockets via `lib/SPELL_IPC`. Configuration is loaded from XML files in `config/`.

### Key directories

| Directory | Contents |
|---|---|
| `lib/` | C++ libraries (SPELL_EXC, SPELL_IPC, SPELL_WRP, etc.) |
| `include/` | C++ headers mirroring `lib/` structure |
| `src/spell/` | Python `spell` package (USL functions, adapters, utilities) |
| `src/executor/` | SPELL-Executor binary |
| `src/context/` | SPELL-Context binary |
| `src/listener/` | SPELL-Listener binary |
| `drivers/` | GCS driver implementations (example, standalone, and others) |
| `config/` | XML configuration (server, contexts, language defaults) |
| `procs/` | Sample USL procedures |
| `data/` | Runtime data directories (AsRun, Warmstart, Procedures, etc.) |
| `scripts/linux/` | Startup and utility scripts |
| `deploy/` | Kubernetes/Helm deployment manifests |
| `azure_pipelines/` | CI/CD pipeline definitions |

### Python USL

Procedures import from the `spell` package:

```python
from spell.lang.functions import Send, Verify, GetTM, Prompt, Abort
from spell.lang.modifiers import OnFailure, Timeout, Wait
from spell.lang.constants import ENG, RAW, ABORT, SKIP
```

### Implementing a driver

Use `drivers/example/` as the reference. Each driver must implement these modules in its `src/` directory: `tc.py`, `tm.py`, `ev.py`, `usr.py`, `resources.py`, `task.py`, `config.py`, `modifiers.py`.

## License

SPELL is distributed under the [GNU General Public License v3](COPYING.GPLv3). Python library components use [LGPLv3](COPYING.LGPLv3).

Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
