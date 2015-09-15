// ################################################################################
// FILE       : SPELLconstants.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL language constants
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

#ifndef __SPELL_CONSTANTS_H__
#define __SPELL_CONSTANTS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \defgroup SPELL_WRP Wrappers (SPELL_WRP)
 *
 * This module provides wrapper classes for manipulating entities living on
 * the Python layer like the SPELL registry and the SPELL driver manager.
 *
 * */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
/** Separator for lists in general */
#define LIST_SEPARATOR '\3'
#define LIST_SEPARATOR_STR "\3"
/** For variable messages */
#define VARIABLE_SEPARATOR '\4'
#define VARIABLE_SEPARATOR_STR "\4"
/** For variable messages */
#define VARIABLE_PROPERTY_SEPARATOR '\5'
#define VARIABLE_PROPERTY_SEPARATOR_STR "\5"


// =========================================================================
/** Executor configuration parameters */
namespace ExecutorConstants
{
const std::string RunInto                = "RunInto";
const std::string ExecDelay              = "ExecDelay";
const std::string PromptDelay            = "PromptWarningDelay";
const std::string ByStep                 = "ByStep";
const std::string MaxVerbosity           = "MaxVerbosity";
const std::string BrowsableLib           = "BrowsableLib";
const std::string UserLib                = "UserLib";
const std::string ForceTcConfirm         = "ForceTcConfirm";
const std::string SaveStateMode          = "SaveStateMode";
const std::string WatchVariables         = "WatchVariables";
const std::string ENABLED                = "ENABLED";
const std::string SAVE_ON_LINE           = "ON_LINE";
const std::string SAVE_ON_DEMAND         = "ON_DEMAND";
const std::string SAVE_ON_STEP           = "ON_STEP";
const std::string TRUE_VALUE			 = "True";
const std::string FALSE_VALUE			 = "False";
};
// =========================================================================

// =========================================================================
/** Database parameters */
namespace DatabaseConstants
{
	/* Database names */
	const std::string SCDB       = "SCDB";
	const std::string GDB        = "GDB";
	const std::string PROC       = "PROC";
	const std::string IVARS 	 = "IVARS";
	const std::string ARGS       = "ARGS";

	/* Key names */
	const std::string STEP 		 = "STEP";
	const std::string NAME       = "NAME";
	const std::string PREV_STEP  = "PREV_STEP";
	const std::string OUTPUT_DATA = "OUTPUT_DATA";
	const std::string INPUT_DATA  = "INPUT_DATA";
	const std::string PARENT     = "PARENT";
}

// =========================================================================
/** Stringified python constants */
namespace PythonConstants
{
	/** String representation of Python True */
	const std::string True                   = "True";
	/** String representation of Python False */
	const std::string False                  = "False";
	/** String representation of Python None */
	const std::string None                   = "None";
};
// =========================================================================

// =========================================================================
/** SPELL language constants */
namespace LanguageConstants
{
	const std::string RAW                    = "Raw format";
	const std::string ENG                    = "Eng format";
	const std::string DEF                    = "Default format";

	const unsigned int ABORT                 = 1;
	const unsigned int CANCEL                = 2;
	const unsigned int SKIP                  = 4;
	const unsigned int RECHECK               = 8;
	const unsigned int RESEND                = 16;
	const unsigned int REPEAT                = 32;
	const unsigned int NOACTION              = 64;
	const unsigned int PROMPT                = 128;
	const unsigned int NOPROMPT              = 256;

	const std::string eq                     = "equal to";
	const std::string neq                    = "not equal to";
	const std::string lt                     = "less than";
	const std::string gt                     = "greater than";
	const std::string le                     = "less or equal than";
	const std::string ge                     = "greater or equal to";
	const std::string bw                     = "between";
	const std::string nbw                    = "not between";

	const unsigned int SCOPE_PROC            = 1;
	const unsigned int SCOPE_SYS             = 2;
	const unsigned int SCOPE_CFG             = 4;
	const unsigned int SCOPE_STEP            = 8;
	const unsigned int SCOPE_PROMPT          = 16;

	const unsigned int WARNING               = 1;
	const unsigned int ERROR                 = 2;
	const unsigned int FATAL                 = 4;
	const unsigned int INFORMATION           = 8;

	const std::string DEC                    = "dec";
	const std::string HEX                    = "hex";
	const std::string OCT                    = "oct";
	const std::string BIN                    = "bin";

	const std::string LONG                   = "long";
	const std::string FLOAT                  = "float";
	const std::string BOOLEAN                = "bool";
	const std::string STRING                 = "string";
	const std::string DATETIME               = "datetime";
	const std::string RELTIME                = "reltime";

	const std::string DISPLAY                = "DISPLAY";
	const std::string LOGVIEW                = "LOG";
	const std::string DIALOG                 = "DIALOG";

	const std::string ITEM_SUCCESS           = "SUCCESS";
	const std::string ITEM_FAILED            = "FAILED";
	const std::string ITEM_PROGRESS          = "IN PROGRESS";

	const unsigned int PROMPT_OK             = 1;
	const unsigned int PROMPT_CANCEL         = 2;
	const unsigned int PROMPT_YES            = 4;
	const unsigned int PROMPT_NO             = 8;
	const unsigned int PROMPT_NUM            = 16;
	const unsigned int PROMPT_ALPHA          = 32;
	const unsigned int PROMPT_LIST           = 64;
	const unsigned int PROMPT_YES_NO         = 128;
	const unsigned int PROMPT_OK_CANCEL      = 512;
	const unsigned int PROMPT_DATE           = 1024;

	const std::string TIME_MODE_LIVE         = "live mode";
	const std::string TIME_MODE_FWD          = "forwards retrieval mode";
	const std::string TIME_MODE_BWD          = "backwards retrieval mode";

	const std::string ACTION_ABORT           = "A";
	const std::string ACTION_REPEAT          = "R";
	const std::string ACTION_RESEND          = "S";
	const std::string ACTION_RECHECK         = "C";
	const std::string ACTION_SKIP            = "K";
	const std::string ACTION_NOACTION        = "N";
	const std::string ACTION_CANCEL          = "Q";
	const std::string ACTION_CANCEL_PMT      = "P";


};
// =========================================================================

// =========================================================================
/** SPELL language modifiers */
namespace LanguageModifiers
{
	const std::string Timeout                = "Timeout";
	const std::string Retries                = "Retries";
	const std::string InWaitFor				 = "InWaitFor"; //Internal
	const std::string Wait                   = "Wait";
	const std::string ValueFormat            = "ValueFormat";
	const std::string ValueType              = "ValueType";
	const std::string Radix                  = "Radix";
	const std::string Units                  = "Units";
	const std::string Notify                 = "Notify";
	const std::string Strict                 = "Strict";
	const std::string TryAll                 = "TryAll";
	const std::string OnFailure              = "OnFailure";
	const std::string Block                  = "Block";
	const std::string Sequence               = "Sequence";
	const std::string Host                   = "Host";
	const std::string Time                   = "Time";
	const std::string Until                  = "Until";
	const std::string Delay                  = "Delay";
	const std::string SendDelay              = "SendDelay";
	const std::string LoadOnly               = "LoadOnly";
	const std::string addInfo                = "addInfo";
	const std::string Default                = "Default";
	const std::string Severity               = "Severity";
	const std::string Scope                  = "Scope";
	const std::string Mode                   = "Mode";
	const std::string Confirm                = "Confirm";
	const std::string ConfirmCritical        = "ConfirmCritical";
	const std::string Type                   = "Type";
	const std::string HandleError            = "HandleError";
	const std::string AllowInterrupt         = "AllowInterrupt";
	const std::string GiveChoice             = "GiveChoice";
	const std::string PromptUser             = "PromptUser";
	const std::string Retry                  = "Retry";
	const std::string AdjLimits              = "AdjLimits";
	const std::string OnFalse                = "OnFalse";
	const std::string OnTrue                 = "OnTrue";
	const std::string OnSkip                 = "OnSkip";
	const std::string Tolerance              = "Tolerance";
	const std::string LoRed                  = "LoRed";
	const std::string LoYel                  = "LoYel";
	const std::string HiRed                  = "HiRed";
	const std::string HiYel                  = "HiYel";
	const std::string Expected               = "Expected";
	const std::string Midpoint               = "Midpoint";
	const std::string Limits                 = "Limits";
	const std::string IgnoreCase             = "IgnoreCase";
	const std::string LoBoth                 = "LoBoth";
	const std::string HiBoth                 = "HiBoth";
	const std::string Interval               = "Interval";
	const std::string Message                = "Message";
	const std::string Printer                = "Printer";
	const std::string Format                 = "Format";
	const std::string Automatic              = "Automatic";
	const std::string Visible                = "Visible";
	const std::string Blocking               = "Blocking";
	const std::string Verbosity              = "Verbosity";
	const std::string ReleaseTime			 = "ReleaseTime";
	const std::string Procedure              = "Procedure";
	const std::string Range                  = "Range";
	const std::string Select                 = "Select";
}
// =========================================================================

// =========================================================================
/** SPELL context configuration parameters */
namespace ContextConstants
{
	const std::string ExecutorScript         = "ExecutorScript";
};
// =========================================================================

// =========================================================================
/** Directories and constant paths */
namespace Locations
{
	/** XML configuration file location directory */
	const std::string CONTEXT_DIR             = "contexts";
	/** XML configuration file location directory */
	const std::string SPELL_DIR               = "spell";
};
// =========================================================================

// =========================================================================
/** XML configuration file tags */
namespace XMLTags
{
	// XML configuration file tags - sections
	const std::string TAG_COMMON_SECTION      = "common";
	const std::string TAG_CONTEXTS_SECTION    = "contexts";
	const std::string TAG_EXECUTOR_SECTION    = "executor";
	const std::string TAG_CONTEXT_SECTION     = "context";
	const std::string TAG_LISTENER_SECTION    = "listener";

	const std::string TAG_DRIVERS_SECTION     = "drivers";
	const std::string TAG_DATABASES_SECTION   = "databases";
	const std::string TAG_DRIVER              = "driver";
	const std::string TAG_CONTEXT             = "context";
	const std::string TAG_FAMILIES_SECTION    = "families";

	// XML configuration file tags - tags for context configuration
	const std::string TAG_CTX_NAME            = "name";
	const std::string TAG_CTX_DESC            = "description";
	const std::string TAG_CTX_DRIVER          = "driver";
	const std::string TAG_CTX_SC              = "spacecraft";
	const std::string TAG_CTX_FAM             = "family";
	const std::string TAG_CTX_SN              = "satname";
	const std::string TAG_CTX_GCS             = "gcs";
	const std::string TAG_CTX_PPATH           = "procpath";
	const std::string TAG_CTX_LPATH           = "libpath";
	const std::string TAG_CTX_OPATH           = "output_dir";
	const std::string TAG_CTX_IPATH           = "input_dir";
	const std::string TAG_CTX_ECONFIG         = "executor";
	const std::string TAG_CTX_DCONFIG         = "driverconfig";

	// XML configuration file tags - tags for driver configuration
	const std::string TAG_DRV_NAME            = "name";
	const std::string TAG_DRV_INTERFACES      = "interfaces";
	const std::string TAG_DRV_LIB             = "lib";
	const std::string TAG_DRV_PATH            = "path";
	const std::string TAG_DRV_MAXPROC         = "maxproc";
	const std::string TAG_DRV_PROPERTIES      = "properties";

	// XML configuration file tags - generic tags
	const std::string TAG_PROPERTY            = "property";
	const std::string TAG_LOCATION            = "location";
	const std::string TAG_DATABASE            = "database";
	// XML configuration file tags - generic attributes
	const std::string TAG_ATTR_ID             = "id";
	const std::string TAG_ATTR_NAME           = "name";
	const std::string TAG_ATTR_TYPE           = "type";
	const std::string TAG_ATTR_EXT            = "ext";
	const std::string TAG_ATTR_LOC            = "location";
};
// =========================================================================

/*@}*/

#endif
