// ################################################################################
// FILE       : SPELLipc.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Inter process communication, data definitions
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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

#ifndef __SPELL_IPC_H__
#define __SPELL_IPC_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \defgroup SPELL_IPC Inter-process communications (SPELL_IPC)
 *
 * This module provides mechanisms to establish socket-based communication
 * channels between the SPELL processes.
 *
 * */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
/** Types of SPELL messages */
typedef enum SPELLipcMessageType_
{
    MSG_TYPE_ONEWAY,
    MSG_TYPE_NOTIFY,
    MSG_TYPE_NOTIFY_ASYNC,
    MSG_TYPE_REQUEST,
    MSG_TYPE_PROMPT,
    MSG_TYPE_RESPONSE,
    MSG_TYPE_WRITE,
    MSG_TYPE_ERROR,
    MSG_TYPE_EOC,
    MSG_TYPE_NOTYPE,
    MSG_TYPE_UNKNOWN,
    MSG_TYPE_PING,
}
SPELLipcMessageType;

// DEFINES /////////////////////////////////////////////////////////////////

/** IPC buffer side for reading incoming socket data */
#define IPC_BUFFER_SIZE     65
/** Maximum number of retries for timed-out requests */
#define IPC_REQUEST_MAX_RETRIES 3
/** Error timeouts */
#define IPC_ERROR_TIMEOUT_ECODE 900

/** Define the message identifiers */
namespace MessageId
{
	/** Error messages */
	static const std::string MSG_ID_ERROR("MSG_ERROR");
	/** Generic identifier */
	static const std::string GENERIC_ID("GEN");
	/** Cancel messages */
	static const std::string MSG_ID_CANCEL("MSG_CANCEL");
	/** Prompt answer messages */
	static const std::string MSG_ID_PROMPT_ANSWER("MSG_ANSWER");
	/** Timeout messages */
	static const std::string MSG_ID_TIMEOUT("MSG_TIMEOUT");
	/** No-peer */
	static const std::string MSG_ID_NOCLIENT("MSG_NOCLIENT");
};

/** Defines message fields */
namespace MessageField
{
	/** Message identifier field */
	static const std::string FIELD_ID("Id");
	/** Message root. Deprecated */
	static const std::string FIELD_TYPE("root");
	/** IPC message key */
	static const std::string FIELD_IPC_KEY("IpcKey");
	/** IPC request sequence number */
	static const std::string FIELD_SEQUENCE("IpcSeq");
	/** Sender identifier */
	static const std::string FIELD_SENDER_ID("SenderId");
	/** Receiver identifier */
	static const std::string FIELD_RECEIVER_ID("ReceiverId");
	/** Error message */
	static const std::string FIELD_ERROR("ErrorMsg");
	/** Error reason or explanation */
	static const std::string FIELD_REASON("ErrorReason");
	/** Fatal error flag field */
	static const std::string FIELD_FATAL("FatalError");
	/** Host name */
	static const std::string FIELD_HOST("Host");
	/** Message times */
	static const std::string FIELD_TIME("Time");
	/** Current message chunk, for splitted messages */
	static const std::string FIELD_CHUNK("CurrentChunk");
	/** Total number of chunks, for splitted messages */
	static const std::string FIELD_TOTAL_CHUNKS("TotalChunks");
	/** Boolean field */
	static const std::string  FIELD_BOOL("Bool");                // Question/confirmation requests
	/** Sequence number for oneway messages */
	static const std::string FIELD_MSG_SEQUENCE("Sequence");
	/** Hols a time identifier */
	static const std::string FIELD_TIME_ID("TimeId");
	/** Time messages */
	static const std::string FIELD_DRIVER_TIME("DriverTime");
};

/** Defines values for the message fields */
namespace MessageValue
{
	/** True value */
	static const std::string DATA_TRUE("True");
	/** False value */
	static const std::string DATA_FALSE("False");
};

/** Defines the message type tags */
namespace MessageType
{
	/** Oneway (asynchronous) messages */
	static const std::string MSG_TYPE_ONEWAY_STR("command");
	/** Notification messages */
	static const std::string MSG_TYPE_NOTIFY_STR("notify");
	/** Asynchronous notification messages */
	static const std::string MSG_TYPE_NOTIFY_ASYNC_STR("notify_async");
	/** Generic request */
	static const std::string MSG_TYPE_REQUEST_STR("request");
	/** Prompt request */
	static const std::string MSG_TYPE_PROMPT_STR("prompt");
	/** Generic response */
	static const std::string MSG_TYPE_RESPONSE_STR("response");
	/** Write oneway message */
	static const std::string MSG_TYPE_WRITE_STR("write");
	/** Error message */
	static const std::string MSG_TYPE_ERROR_STR("error");
	/** End-of-conversation message */
	static const std::string MSG_TYPE_EOC_STR("eoc");
	/** No type */
	static const std::string MSG_TYPE_NOTYPE_STR("NOTYPE");
	/** Unknown */
	static const std::string MSG_TYPE_UNKNOWN_STR("UNKNOWN");
	/** Unknown */
	static const std::string MSG_TYPE_PING_STR("ping");
	/** For mapping message types to tag names */
	static const std::string TypeStr[] =
	{
		MSG_TYPE_ONEWAY_STR,
		MSG_TYPE_NOTIFY_STR,
		MSG_TYPE_NOTIFY_ASYNC_STR,
		MSG_TYPE_REQUEST_STR,
		MSG_TYPE_PROMPT_STR,
		MSG_TYPE_RESPONSE_STR,
		MSG_TYPE_WRITE_STR,
		MSG_TYPE_ERROR_STR,
		MSG_TYPE_EOC_STR,
		MSG_TYPE_NOTYPE_STR,
		MSG_TYPE_UNKNOWN_STR,
		MSG_TYPE_PING_STR
	};
	static const unsigned int NumTypes = sizeof(TypeStr) / sizeof(std::string);
};

/** Special IPC internal parameters */
namespace IPCinternals
{
	/** Separator for file and data lines */
	static const std::string CODE_SEPARATOR("%C%");
	/** Separator for items in notifications */
	//static const std::string ARG_SEPARATOR(",,"); //moved to notifications
	/** Separator for keys and values in prompts */
	static const std::string KEY_SEPARATOR(":");
	/** Separator for options in prompts */
	static const std::string OPT_SEPARATOR("\4");
};

/*@}*/
#endif

