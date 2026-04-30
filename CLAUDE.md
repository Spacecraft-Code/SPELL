# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What is SPELL

SPELL (Spacecraft Procedure Execution Language and Library) is a ground software framework for spacecraft operations, developed by SES Engineering. It provides:
- A Python-based **USL (User Script Language)** for writing spacecraft procedures
- A distributed process architecture: Listener → Context → Executor(s)
- A driver layer for connecting to different Ground Control Systems (GCS)

## Build System

SPELL uses GNU Autotools (autoconf + automake). The build requires:
- GCC/G++ with pthreads
- Python development headers
- `xerces-c` (XML parsing), `log4cplus` (logging), `librt`, `libdl`, `libutil`
- Optional: `cppunit` (unit tests), `ncurses`+`menu` (command-line executor)

```bash
# Standard build flow
autoreconf -fi        # Generate configure script (if needed)
./configure [options] # Configure the build
make                  # Build
make install          # Install to prefix

# Configure options (all default to disabled except executor, shell, listener, context):
./configure --enable-executor     # Build SPELL-Executor binary (default on)
./configure --enable-context      # Build SPELL-Context binary (default on)
./configure --enable-listener     # Build SPELL-Shell binary (default on)
./configure --enable-shell        # Build SPELL-Shell binary (default on)
./configure --enable-executorcmd  # Build command-line executor (requires ncurses)
./configure --enable-gui          # Build SPELL-GUI (requires --with-eclipse=PATH)
./configure --enable-driverstd    # Build standalone driver
./configure --enable-procs        # Include sample procedures
./configure --enable-documentation  # Build Doxygen docs
./configure --with-eclipse=PATH   # Eclipse installation for GUI build
./configure --with-log4cplus=PATH # log4cplus library path (default: /usr)
```

Compiler flags are defined in `common.mk`: includes `$(top_srcdir)/include`, `$SPELL_COTS/include`, Python headers, and log4cplus. All C++ objects are compiled with `-fPIC -g3 -Wall`.

## Runtime Environment Variables

Scripts in `scripts/linux/` require these to be set:

| Variable | Purpose |
|---|---|
| `SPELL_HOME` | Installation root (contains `bin/`, `lib/`, `config/`, `data/`) |
| `SPELL_DATA` | Runtime data directory (defaults to `$SPELL_HOME/data`) |
| `SPELL_CONFIG` | Configuration directory (defaults to `$SPELL_HOME/config`) |

Source `$SPELL_HOME/bin/setup-environment.sh` to set `LD_LIBRARY_PATH` and other derived variables.

## Starting the Server

```bash
$SPELL_HOME/bin/SPELL-Server <SATELLITE_NAME>
# Looks for config at: $SPELL_CONFIG/server/server_<SAT>_<hostname>.xml
```

## Running Unit Tests

After building with tests enabled:
```bash
$SPELL_HOME/bin/SPELL-RunTests
# Runs all SPELL_unittest_* binaries from $SPELL_HOME/bin/
# Logs output to $SPELL_HOME/log/unittests_<MODULE>.log
# Test data expected at $SPELL_HOME/data/UnitTests/
```

## Architecture

### Process Hierarchy

```
SPELL-Listener  (entry point, port 9980)
    └── SPELL-Context  (manages spacecraft context, spawns executors)
            └── SPELL-Executor  (one per running procedure, embeds CPython)
```

All processes communicate over TCP sockets using the IPC layer (`lib/SPELL_IPC`). Configuration is loaded from XML files in `config/`.

### C++ Library Layers (`lib/`)

| Library | Purpose |
|---|---|
| `SPELL_UTIL` | Utilities (logging, time, string handling) |
| `SPELL_IPC` | Inter-process communication (sockets, message mailboxes) |
| `SPELL_SYN` | Synchronization primitives |
| `SPELL_CFG` | XML configuration loading |
| `SPELL_WRP` | Python/C++ wrapper layer (embeds CPython in executor) |
| `SPELL_EXC` | Execution engine (bytecode analysis, callstack, controller) |
| `SPELL_CTX` | Context management |
| `SPELL_LST` | Listener process logic |
| `SPELL_CIF` | Client interface (GUI-facing) |
| `SPELL_CIFC` / `SPELL_CIFS` | CIF client/server implementations |
| `SPELL_PRD` | Procedure management |
| `SPELL_PRC` | Process control |
| `SPELL_WS` | Warm-start / state persistence |
| `SPELL_SDB` | Spacecraft database access |
| `SPELL_DTA` | Data types and structures |

Headers for all libraries live in `include/` (mirroring library names).

### Python Layer (`src/spell/`)

The `spell` Python package is what procedures import. Key sub-packages:

- `spell.lang.functions` — USL functions (`Send`, `Verify`, `GetTM`, `BuildTC`, `Prompt`, `StartProc`, `Abort`, etc.)
- `spell.lang.modifiers` — Modifier constants (`OnFailure`, `Timeout`, `Wait`, `ValueFormat`, etc.)
- `spell.lang.constants` — Value constants (e.g., `ENG`, `RAW`, `ABORT`, `SKIP`)
- `spell.lib.adapter` — Abstract base classes for driver interfaces (TC, TM, EV, USR, RSC, MEM, RNG, etc.)
- `spell.lib.registry` — Runtime registry of active driver interface instances
- `spell.utils` — Logging utilities

### Driver Layer (`drivers/`)

Drivers implement the `spell.lib.adapter` interfaces for a specific GCS. The `drivers/example/` directory is the reference implementation. Each driver provides modules: `tc.py`, `tm.py`, `ev.py`, `usr.py`, `resources.py`, `task.py`, `config.py`, `modifiers.py`.

The `drivers/standalone/` driver runs without a GCS (simulator mode). Driver XML config files go in `config/contexts/`.

### Configuration (`config/`)

- `config/server/server.xml` — Main server config (ports, executor settings, driver list, context list)
- `config/spell/language.xml` — Default modifier values per USL function
- `config/contexts/context_STD.xml` — Context definition linking to satellite family and driver
- `config/gui/config.xml` — GUI configuration

### Sample Procedures (`procs/`)

`procs/procedures/` contains example USL procedures. The `procs/userlib/` directory holds user procedure libraries importable from procedures.
