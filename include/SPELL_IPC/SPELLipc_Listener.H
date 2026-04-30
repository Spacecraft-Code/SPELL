// ################################################################################
// FILE       : SPELLipc_Listener.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Inter process communication, data definitions for listener process
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

#ifndef __SPELL_IPC_LISTENER_H__
#define __SPELL_IPC_LISTENER_H__

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

/** Defines message fields */
namespace MessageField
{
	/** Context name */
	static const std::string FIELD_CTX_NAME("ContextName");
	/** Context name */
	static const std::string FIELD_CTX_TRANSMODE_LIST("TransferModeList");
	/** Context port */
	static const std::string FIELD_CTX_PORT("ContextPort");
	/** Context name */
	static const std::string FIELD_CTX_LIST("ContextList");
	/** Driver max procs */
	static const std::string FIELD_CTX_MAXPROC("MaxProc");
	/** Context spacecraft */
    static const std::string FIELD_CTX_SC("ContextSC");
	/** Context status */
    static const std::string FIELD_CTX_STATUS("ContextStatus");
	/** Context driver */
    static const std::string FIELD_CTX_DRV("ContextDriver");
	/** Context family */
    static const std::string FIELD_CTX_FAM("ContextFamily");
	/** Context GCS */
    static const std::string FIELD_CTX_GCS("ContextGCS");
	/** Context description */
    static const std::string FIELD_CTX_DESC("ContextDescription");

//###############################################################################
//# Peer-related fields
//###############################################################################

	/** Server name */
	static const std::string  FIELD_SERVER_NAME("ServerName");
	/** Originator */
	static const std::string  FIELD_ORIGINATOR("Originator");
	/** Peer name */
	static const std::string  FIELD_PEER_NAME("PeerName");
	/** Raw data chunk */
    static const std::string  FIELD_DATA_CHUNK("DataChunk");
	/** Raw data size */
    static const std::string  FIELD_FILE_SIZE("FileSize");
	/** File path */
    static const std::string  FIELD_FILE_PATH("FilePath");
	/** Time ID coma-separated list */
    static const std::string  FIELD_TIME_ID_LIST("TimeId");
	/** Enabled? */
    static const std::string  FIELD_ENABLED("Enabled");
	/** Started? */
    static const std::string  FIELD_STARTED("Started");
	/** Offset */
	static const std::string  FIELD_OFFSET("Offset");
	/** Number of chunks */
	static const std::string  FIELD_NUM_CHUNKS("NumChunks");
	/** Number of files */
	static const std::string  FIELD_NUM_FILES("NumFiles");
};

/** Defines listener process messages */
namespace ListenerMessages
{
	// Login message for listener
	/** Context open notification */
	static const std::string  MSG_CONTEXT_OPEN("MSG_CONTEXT_OPEN");
	/** Contex closed notification */
	static const std::string  MSG_CONTEXT_CLOSED("MSG_CONTEXT_CLOSED");
	/** Sent by listener to GUIs when a context starts/stops */
	static const std::string  MSG_CONTEXT_OP("MSG_CONTEXT_OP");
	
	/** Sent when executor is started */
	static const std::string  MSG_EXEC_LOGIN("MSG_EXEC_LOGIN");
	/** Sent when executor is ended */
	static const std::string  MSG_EXEC_LOGOUT("MSG_EXEC_LOGOUT");
	/** Peer-to-peer notifications */
	static const std::string  MSG_NOTIFY_CLOSED_PROCEDURE("MSG_NOTIFY_CLOSED_PROCEDURE");
	static const std::string  MSG_NOTIFY_NEW_PROCEDURE("MSG_NOTIFY_NEW_PROCEDURE");
}

/*@}*/
#endif

