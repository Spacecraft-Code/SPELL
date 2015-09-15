// ################################################################################
// FILE       : SPELLbase.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Base includes for SPELL framework
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

/*! \mainpage SPELL Execution Framework
 *
 * \section intro_sec Introduction
 *
 * This is the source code documentation of the SPELL Execution Framework, corresponding
 * to releases 2.0 and later (C++ / Python hybrid version).
 *
 */

#ifndef __SPELL_BASE_H__
#define __SPELL_BASE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
#include "Python.h"
#include "frameobject.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <list>
#include <vector>
#include <iterator>
#include <map>
#include <queue>
#include <set>
#include <exception>
#include <algorithm>
#include <cstdio>
#include <dirent.h>
#include <iomanip>
#include <locale>
#include <sys/stat.h>
#include <netdb.h>
#include <arpa/inet.h>
#include "log4cplus/appender.h"
#include "log4cplus/fileappender.h"
#include "log4cplus/layout.h"
#include "log4cplus/ndc.h"
#include "log4cplus/configurator.h"

// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------

/** \defgroup SPELL_UTIL Utilities (SPELL_UTIL)
 *
 * The module SPELL_UTILS provides classes and functions that are used widely
 * in the SPELL source code to perform different operations.
 *
 * These operations include:
 *
 * - Access the Python layer and manipulate Python objects
 * - Common data conversions (string-to-number, Python object to string, etc)
 * - Logging
 * - Argument processing
 * - Time management
 *
 * */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
#define BYTE unsigned char

/*@}*/
#endif
