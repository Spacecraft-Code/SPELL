// ################################################################################
// FILE       : SPELLexecutorOperation.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Definition of the executor operations
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

#ifndef __SPELL_EXECUTOR_OPERATION_H__
#define __SPELL_EXECUTOR_OPERATION_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLclientMode.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
// Project includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcMessage.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorOperation
{
public:

	typedef enum Type_
	{
		EXEC_OP_OPEN,
		EXEC_OP_CLOSE,
		EXEC_OP_KILL,
		EXEC_OP_ATTACH,
		EXEC_OP_DETACH,
		EXEC_OP_STATUS,
		EXEC_OP_SUMMARY,
		EXEC_OP_CRASH,
		EXEC_OP_UNKNOWN
	}
	Type;


	SPELLexecutorOperation();
	std::string toString() const;
	void completeMessage( SPELLipcMessage& msg ) const;

public:
	std::string instanceId;
	std::string parentId;
	std::string groupId;
	std::string originId;
	int clientKey;
	SPELLclientMode clientMode;
	Type type;
	SPELLexecutorStatus status;
	std::string condition;
	std::string errorMessage;
	std::string errorReason;
	std::string stageId;
	std::string stageTitle;
};

/*@}*/
#endif
