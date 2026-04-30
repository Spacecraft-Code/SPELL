// ################################################################################
// FILE       : SPELLexecutorOperation.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor operation notifications
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorOperation.H"
#include "SPELL_CTX/SPELLdataHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLexecutorOperation
//=============================================================================
SPELLexecutorOperation::SPELLexecutorOperation()
{
	instanceId = "";
	parentId = "";
	groupId = "";
	originId = "";
	clientKey = -1;
	clientMode = CLIENT_MODE_UNKNOWN;
	type = EXEC_OP_UNKNOWN;
	status = STATUS_UNKNOWN;
	condition = "";
	errorMessage = "";
	errorReason = "";
	stageId = "(none)";
	stageTitle = "(none)";
}

//=============================================================================
// METHOD: SPELLexecutorOperation::
//=============================================================================
std::string SPELLexecutorOperation::toString() const
{
	switch(type)
	{
		case EXEC_OP_OPEN:   return MessageValue::DATA_EXOP_OPEN;
		case EXEC_OP_CLOSE:  return MessageValue::DATA_EXOP_CLOSE;
		case EXEC_OP_KILL:   return MessageValue::DATA_EXOP_KILL;
		case EXEC_OP_ATTACH: return MessageValue::DATA_EXOP_ATTACH;
		case EXEC_OP_DETACH: return MessageValue::DATA_EXOP_DETACH;
		case EXEC_OP_STATUS: return MessageValue::DATA_EXOP_STATUS;
		case EXEC_OP_SUMMARY: return MessageValue::DATA_EXOP_SUMMARY;
		case EXEC_OP_CRASH:  return MessageValue::DATA_EXOP_CRASH;
		case EXEC_OP_UNKNOWN:  return MessageValue::DATA_EXOP_UNKNOWN;
		default:
			LOG_ERROR("Unknown executor operation");
			break;
	}
	return "";
};

//=============================================================================
// METHOD: SPELLexecutorOperation::
//=============================================================================
void SPELLexecutorOperation::completeMessage( SPELLipcMessage& msg ) const
{
	msg.set( MessageField::FIELD_PROC_ID, instanceId );
	msg.set( MessageField::FIELD_GROUP_ID, groupId );
	msg.set( MessageField::FIELD_ORIGIN_ID, originId );
	msg.set( MessageField::FIELD_PARENT_PROC, parentId );
	msg.set( MessageField::FIELD_EXOP, toString() );
	msg.set( MessageField::FIELD_GUI_KEY, ISTR(clientKey) );
	msg.set( MessageField::FIELD_GUI_MODE, SPELLdataHelper::clientModeToString(clientMode) );
	msg.set( MessageField::FIELD_EXEC_STATUS, SPELLdataHelper::executorStatusToString(status) );
	msg.set( MessageField::FIELD_STAGE_ID, stageId);
	msg.set( MessageField::FIELD_CONDITION, condition );
	msg.set( MessageField::FIELD_STAGE_TL, stageTitle);
	msg.set( MessageField::FIELD_ERROR, errorMessage );
	msg.set( MessageField::FIELD_REASON, errorReason );
}
