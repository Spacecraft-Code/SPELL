// ################################################################################
// FILE       : SPELLipc_Executor.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Inter process communication, data definitions for executor process
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

#ifndef __SPELL_IPC_EXECUTOR_H__
#define __SPELL_IPC_EXECUTOR_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_IPC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

/** Define the message identifiers */
namespace MessageId
{
	/** Write messages */
	static const std::string MSG_ID_WRITE("MSG_WRITE");
	/** Prompt messages */
	static const std::string MSG_ID_PROMPT("MSG_PROMPT");
	/** Prompt start, for monitoring guis */
	static const std::string MSG_ID_PROMPT_START("MSG_PROMPT_START");
	/** Prompt end, for monitoring guis */
	static const std::string MSG_ID_PROMPT_END("MSG_PROMPT_END");
	/** Notifications */
	static const std::string MSG_ID_NOTIFICATION("MSG_NOTIFICATION");

////////////////////////////////////////////////////////////////////////////
// User action messages
	/** Set user action */
	static const std::string MSG_ID_SET_UACTION("MSG_SET_UACTION");
	/** Enable user action */
	static const std::string MSG_ID_ENABLE_UACTION("MSG_ENABLE_UACTION");
	/** Disable user action */
	static const std::string MSG_ID_DISABLE_UACTION("MSG_DISABLE_UACTION");
	/** Dismiss user action */
	static const std::string MSG_ID_DISMISS_UACTION("MSG_DISMISS_UACTION");

////////////////////////////////////////////////////////////////////////////
// Other messages

	/** Add client to the executor */
	static const std::string MSG_ID_ADD_CLIENT("MSG_ADD_CLIENT");
	/** Remove client from executor */
	static const std::string MSG_ID_REMOVE_CLIENT("MSG_REMOVE_CLIENT");
	/** Set headless (background) mode */
	static const std::string MSG_ID_BACKGROUND("MSG_BACKGROUND");
	/** Set callstack node depth */
	static const std::string MSG_ID_NODE_DEPTH("MSG_SHOW_NODE_DEPTH");
	/** Message for enabling or disabling variable analysis */
	static const std::string MSG_ID_WVARIABLES_ENABLE("MSG_WVARIABLES_ENABLE");
	static const std::string MSG_ID_WVARIABLES_DISABLE("MSG_WVARIABLES_DISABLE");
};

/** Defines message fields */
namespace MessageField
{
	/** Notification data type */
	static const std::string FIELD_DATA_TYPE("DataType");
	/** Scope of the message */
	static const std::string FIELD_SCOPE("Scope");
	/** Holds the procedure id */
	static const std::string  FIELD_PROC_ID("ProcId");
	/** Holds the group id */
	static const std::string  FIELD_GROUP_ID("GroupId");
	/** Holds the origin id */
	static const std::string  FIELD_ORIGIN_ID("OriginId");
	/** Holds the instance id, used in the instance request */
	static const std::string  FIELD_INSTANCE_ID("InstanceId");
	/** Subprocedure identifier */
	static const std::string  FIELD_SPROC_ID("SprocId");             // Open request from executor
	/** Open mode */
	static const std::string  FIELD_OPEN_MODE("OpenMode");            // Open requests
	/** Free answer to a question */
	static const std::string FIELD_ANSWER("Answer");
	/** Default option for prompts */
	static const std::string FIELD_DEFAULT("Default");
	/** Procedure name */
	static const std::string FIELD_PROC_NAME("ProcName");
	/** Executor parent proc if any */
	static const std::string FIELD_PARENT_PROC("ParentId");
	/** Executor parent proc calling line if any */
	static const std::string FIELD_PARENT_PROC_LINE("ParentLine");
	/** Call stack position */
	static const std::string FIELD_CSP("Csp");
	/** Stage id */
	static const std::string FIELD_STAGE_ID("StageId");
	/** Stage title */
	static const std::string FIELD_STAGE_TL("StageTl");
	/** Holds the executor port */
	static const std::string FIELD_EXEC_STATUS("ExecutorStatus");      // Exec info, Open exec, Attach exec
	/** Holds the controlling gui */
	static const std::string FIELD_GUI_CONTROL("GuiControl");
	/** Holds the list of monitoring guis */
	static const std::string FIELD_GUI_LIST("GuiList");
	/** Holds the executor arguments (procedure arguments) */
	static const std::string FIELD_ARGS("Arguments");
	/** Field holding an arbitrary python script (script command) */
	static const std::string FIELD_SCRIPT("Script");
	/** Force flag for several operations */
	static const std::string FIELD_FORCE("Force");
	/** Field holding the step over flag (run command) */
	static const std::string FIELD_SO("So");
	/** Field holding the target line number (goto on gui) */
	static const std::string FIELD_GOTO_LINE("GotoLine");
	/** Field holding the target label (goto on gui) */
	static const std::string FIELD_GOTO_LABEL("GotoLabel");
	/** Holds the executor condition */
	static const std::string FIELD_CONDITION("Condition");
	/** Holds the controlling gui host */
	static const std::string FIELD_GUI_CONTROL_HOST("GuiControlHost");
	/** Holds the list of monitoring guis hosts */
	static const std::string FIELD_GUI_HOST_LIST("GuiHostList");
	/** Execution mode */
	static const std::string FIELD_EXECUTION_MODE("ExecutorMode");
	/** Toggle breakpoint code id */
	static const std::string FIELD_BREAKPOINT_PROC("BreakpointProc");
	/** Toggle breakpoint target line */
	static const std::string FIELD_BREAKPOINT_LINE("BreakpointLine");
	/** Breakpoint type */
	static const std::string FIELD_BREAKPOINT_TYPE("BreakpointType");
	/** Type of messages for write messages */
	static const std::string FIELD_MSGTYPE("MsgType");
	/** Message text */
	static const std::string FIELD_TEXT("Text");
	/** Message level */
	static const std::string FIELD_LEVEL("Level");
	/** Prompt messages, expected values */
	static const std::string FIELD_EXPECTED("ExpectedValues");
	/** Prompt return value */
	static const std::string FIELD_RVALUE("ReturnValue");
	/** Prompt options */
	static const std::string FIELD_OPTIONS("OptionValues");
	/** Line in the subprocedure */
	static const std::string FIELD_SLINE("SubprocLine");
    /** Holds the current line number  */
    static const std::string FIELD_CURRENT_LINE("CurrentLine");

	/** AsRUN file name */
	static const std::string FIELD_ASRUN_NAME("AsRunName");
	/** Log file name */
	static const std::string FIELD_LOG_NAME("LogName");
	/** Server file type identifier */
	static const std::string FIELD_SERVER_FILE_ID("ServerFileId");

	/** User action label */
	static const std::string FIELD_ACTION_LABEL("ActionLabel");
	/** User action enabled flag */
	static const std::string FIELD_ACTION_ENABLED("ActionEnabled");
	/** User action severity */
	static const std::string FIELD_ACTION_SEVERITY("ActionSeverity");

	//################################################################################
	//# WATCH OF VARIABLES
	//################################################################################

	/** Variable names */
	static const std::string FIELD_VARIABLE_NAME("VarName");
	/** Variable values */
	static const std::string FIELD_VARIABLE_VALUE("VarValue");
	/** Variable types */
	static const std::string FIELD_VARIABLE_TYPE("VarType");
	/** Variable global flags */
	static const std::string FIELD_VARIABLE_GLOBAL("VarGlobal");
	/** Variable deleted flags */
	static const std::string FIELD_VARIABLE_DELETE("VarDelete");
	/** Variable added flags */
	static const std::string FIELD_VARIABLE_ADDED("VarAdded");
	/** Variable scope name */
	static const std::string FIELD_VARIABLE_SCOPE("VarScope");
	/** Variable analysis enabled */
	static const std::string FIELD_WVARIABLES_ENABLED("WVarEnabled");

	//################################################################################
	//# DICTIONARY EDITING
	//################################################################################

	/** Name of the dictionary to edit */
	static const std::string FIELD_DICT_NAME("DictName");
	/** Merge new variables flag */
	static const std::string FIELD_DICT_MERGENEW("DictMergeNew");
	/** Name of the dictionary contents */
	static const std::string FIELD_DICT_CONTENTS("DictContents");

	//###############################################################################
	//# UI notification fields
	//###############################################################################

	/** Subprocedure identifier for notifications */
	static const std::string FIELD_NOTIF_SUBPROC_ID("SubProcId");
	/** Item type in notifications */
	static const std::string FIELD_NOTIF_ITEM_TYPE("ItemType");
	/** Item name in notifications */
	static const std::string FIELD_NOTIF_ITEM_NAME("ItemName");
	/** Item value in notifications */
	static const std::string FIELD_NOTIF_ITEM_VALUE("ItemValue");
	/** Item status in notifications */
	static const std::string FIELD_NOTIF_ITEM_STATUS("ItemStatus");
	/** Item comments in notifications */
	static const std::string FIELD_NOTIF_ITEM_REASON("ItemReason");
	/** Item time in notifications */
	static const std::string FIELD_NOTIF_ITEM_TIME("ItemTime");
	/** Status notification */
	static const std::string FIELD_NOTIF_STATUS("Status");
	/** Success items count */
	static const std::string FIELD_NOTIF_ITEM_SCOUNT("SCount");
};

/** Defines values for the message fields */
namespace MessageValue
{
	// Executor messages field values
	/** Current line data type for notifications */
	static const std::string DATA_TYPE_LINE("CURRENT_LINE");
	/** Call data type for notifications */
	static const std::string DATA_TYPE_CALL("CALL");
	/** Return data type for notifications */
	static const std::string DATA_TYPE_RETURN("RETURN");
	/** Item data type for notifications */
	static const std::string DATA_TYPE_ITEM("ITEM");
	/** Status data type for notifications */
	static const std::string DATA_TYPE_STATUS("STATUS");

	// Executor status codes (FIELD_EXEC_STATUS)
	/** Status code uninit */
	static const std::string DATA_STATUS_UNINIT("UNINIT");
	/** Status code loaded */
	static const std::string DATA_STATUS_LOADED("LOADED");
	/** Status code paused */
	static const std::string DATA_STATUS_PAUSED("PAUSED");
	/** Status code running */
	static const std::string DATA_STATUS_RUNNING("RUNNING");
	/** Status code finished */
	static const std::string DATA_STATUS_FINISHED("FINISHED");
	/** Status code aborted */
	static const std::string DATA_STATUS_ABORTED("ABORTED");
	/** Status code error */
	static const std::string DATA_STATUS_ERROR("ERROR");
	/** Status code waiting */
	static const std::string DATA_STATUS_WAITING("WAITING");
	/** Status code interrupted */
	static const std::string DATA_STATUS_PROMPT("PROMPT");
	/** Status code interrupted */
	static const std::string DATA_STATUS_INTERRUPTED("INTERRUPTED");
	/** Status code reloading */
	static const std::string DATA_STATUS_RELOADING("RELOADING");
	/** Status code unknown */
	static const std::string DATA_STATUS_UNKNOWN("UNKNOWN");

	// String equivalents for status codes
	static const std::string StatusStr[] =
	{
		DATA_STATUS_UNINIT,
		DATA_STATUS_LOADED,
		DATA_STATUS_PAUSED,
		DATA_STATUS_RUNNING,
		DATA_STATUS_FINISHED,
		DATA_STATUS_ABORTED,
		DATA_STATUS_ERROR,
		DATA_STATUS_WAITING,
		DATA_STATUS_PROMPT,
		DATA_STATUS_INTERRUPTED,
		DATA_STATUS_RELOADING,
		DATA_STATUS_UNKNOWN
	};

	// Number of status codes
	static const int NumStatus = sizeof(StatusStr) / sizeof(std::string);

//TODO Remove from here and move to Notifications Class

//###############################################################################
//# UI notification type values
	/** Notification data type item */
//	static const std::string DATA_NOTIF_TYPE_ITEM("ITEM");
	/** Notification data type value */
//	static const std::string DATA_NOTIF_TYPE_VAL("VALUE");
	/** Notification data type verification */
//	static const std::string DATA_NOTIF_TYPE_VERIF("VERIFICATION");
	/** Notification data type execution */
//	static const std::string DATA_NOTIF_TYPE_EXEC("EXECUTION");
	/** Notification data type system */
//	static const std::string DATA_NOTIF_TYPE_SYS("SYSTEM");
	/** Notification data type time */
//	static const std::string DATA_NOTIF_TYPE_TIME("TIME");

//###############################################################################
//# UI notification status values
	/** Success operation */
//	static const std::string DATA_NOTIF_STATUS_OK("SUCCESS");
	/** In progress operation */
//	static const std::string DATA_NOTIF_STATUS_PR("IN PROGRESS");
	/** Failed operation */
//	static const std::string DATA_NOTIF_STATUS_FL("FAILED");
	/** Skipped operation */
//	static const std::string DATA_NOTIF_STATUS_SP("SKIPPED");


//###############################################################################
//# Execution mode values
	/** Manual execution */
	static const std::string DATA_EXEC_MODE_MANUAL("MANUAL");
	/** Procedure execution */
	static const std::string DATA_EXEC_MODE_PROCEDURE("PROCEDURE");

//###############################################################################
//# Severities
	static const std::string DATA_SEVERITY_INFO("INFO");
	static const std::string DATA_SEVERITY_WARN("WARN");
	static const std::string DATA_SEVERITY_ERROR("ERROR");

//###############################################################################
//# Breakpoint types
	/** True value */
	static const std::string BREAKPOINT_PERMANENT("PERMANENT");
	/** False value */
	static const std::string BREAKPOINT_TEMPORARY("TEMPORARY");
	/** False value */
	static const std::string BREAKPOINT_UNKNOWN("UNKNOWN");

//###############################################################################
//# Variable retrieval types
	static const std::string AVAILABLE_ALL("AVAILABLE_ALL");
	static const std::string AVAILABLE_GLOBALS("AVAILABLE_GLOBALS");
	static const std::string AVAILABLE_LOCALS("AVAILABLE_LOCALS");
};

/** Defines requests and messages regarding the executor process */
namespace ExecutorMessages
{
	/** Request for executor status */
	static const std::string REQ_EXEC_STATUS("REQ_EXEC_STATUS");
	/** Response to REQ_EXEC_STATUS */
	static const std::string RSP_EXEC_STATUS("RSP_EXEC_STATUS");

	/** Request changing configuration */
	static const std::string REQ_SET_CONFIG("REQ_SET_CONFIG");
	/** Response to REQ_SET_CONFIG*/
	static const std::string RSP_SET_CONFIG("RSP_SET_CONFIG");
	/** Request the current configuration */
	static const std::string REQ_GET_CONFIG("REQ_GET_CONFIG");
	/** Response to REQ_GET_CONFIG */
	static const std::string RSP_GET_CONFIG("RSP_GET_CONFIG");

	/** Request for toggling breakpoints */
	static const std::string REQ_SET_BREAKPOINT("REQ_SET_BREAKPOINT");
	/** Response to REQ_TOGGLE_BREAKPOINT */
	static const std::string RSP_SET_BREAKPOINT("RSP_SET_BREAKPOINT");

	/** Request for clearing breakpoints */
	static const std::string REQ_CLEAR_BREAKPOINT("REQ_CLEAR_BREAKPOINT");
	/** Response to REQ_CLEAR_BREAKPOINT */
	static const std::string RSP_CLEAR_BREAKPOINT("RSP_CLEAR_BREAKPOINT");

	//################################################################################
	//# WATCH OF VARIABLES
	//################################################################################

	/** Request for retrieving variable names */
	static const std::string REQ_GET_VARIABLES("REQ_GET_VARIABLES");
	/** Response */
	static const std::string RSP_GET_VARIABLES("RSP_GET_VARIABLES");
	/** Request for changing variables */
	static const std::string REQ_CHANGE_VARIABLE("REQ_CHANGE_VARIABLE");
	/** Response */
	static const std::string RSP_CHANGE_VARIABLE("RSP_CHANGE_VARIABLE");
	/** Request to check if variable analysis is enabled */
	static const std::string REQ_WVARIABLES_ENABLED("REQ_WVARIABLES_ENABLED");
	/** Response */
	static const std::string RSP_WVARIABLES_ENABLED("RSP_WVARIABLES_ENABLED");

	//################################################################################
	//# DICTIONARY EDITING
	//################################################################################

	/** Request for global dictionary variable */
	static const std::string REQ_GET_DICTIONARY("REQ_GET_DICTIONARY");
	/** Response */
	static const std::string RSP_GET_DICTIONARY("RSP_GET_DICTIONARY");

	/** Update for global dictionary variable */
	static const std::string REQ_UPD_DICTIONARY("REQ_UPD_DICTIONARY");
	/** Response */
	static const std::string RSP_UPD_DICTIONARY("RSP_UPD_DICTIONARY");

	//################################################################################
	//# SPECIALS
	//################################################################################

	/** Oneway message for variable change notifications */
	static const std::string MSG_VARIABLE_CHANGE("MSG_VARIABLE_CHANGE");

	/** Oneway message for variable scope change notifications */
	static const std::string MSG_SCOPE_CHANGE("MSG_SCOPE_CHANGE");

	//################################################################################
	//# REQUESTS TO CONTEXT
	//################################################################################

	/** Sent by the executor to the context when ready to work */
	static const std::string REQ_NOTIF_EXEC_OPEN("REQ_NOTIF_EXEC_OPEN");
	/** Response to REQ_NOTIF_EXEC_OPEN */
	static const std::string RSP_NOTIF_EXEC_OPEN("RSP_NOTIF_EXEC_OPEN");
	/** Sent by executor when closing */
	static const std::string MSG_NOTIF_EXEC_CLOSE("MSG_NOTIF_EXEC_CLOSE");
	/** Sent by the executor to the context to open a child procedure */
	static const std::string REQ_OPEN_EXEC("REQ_OPEN_EXEC");
	/** Sent by the executor to the context to kill a child procedure */
	static const std::string REQ_KILL_EXEC("REQ_KILL_EXEC");
	/** Sent by the executor to the context to close a child procedure */
	static const std::string REQ_CLOSE_EXEC("REQ_CLOSE_EXEC");
	/** To acquire an instance id */
	static const std::string REQ_INSTANCE_ID("REQ_INSTANCE_ID");

	//################################################################################
	//# COMMANDS FROM SPELL CLIENTS (coming through context)
	//################################################################################

	// Commands sent by the GUI

	/** Run command */
	static const std::string MSG_CMD_RUN("CMD_RUN");
	/** Reload comand */
	static const std::string MSG_CMD_RELOAD("CMD_RELOAD");
	/** Recover command */
	static const std::string MSG_CMD_RECOVER("CMD_RECOVER");
	/** Step command */
	static const std::string MSG_CMD_STEP("CMD_STEP");
	/** Step over command */
	static const std::string MSG_CMD_STEP_OVER("CMD_STEP_OVER");
	/** Skip command */
	static const std::string MSG_CMD_SKIP("CMD_SKIP");
	/** Goto command */
	static const std::string MSG_CMD_GOTO("CMD_GOTO");
	/** Pause command */
	static const std::string MSG_CMD_PAUSE("CMD_PAUSE");
	/** Abort command */
	static const std::string MSG_CMD_ABORT("CMD_ABORT");
	/** Close command */
	static const std::string MSG_CMD_CLOSE("CMD_CLOSE");
	/** Block command (for CIF) */
	static const std::string MSG_CMD_BLOCK("CMD_BLOCK");
	/** Script command */
	static const std::string MSG_CMD_SCRIPT("CMD_SCRIPT");
	/** Action command */
	static const std::string MSG_CMD_ACTION("CMD_ACTION");

	static const std::string ACKNOWLEDGE("ACKNOWLEDGE");
};

/*@}*/
#endif

