// ################################################################################
// FILE       : SPELLnotifications.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Notification data structures
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

#ifndef __SPELL_NOTIFICATIONS_H__
#define __SPELL_NOTIFICATIONS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------

// Project includes --------------------------------------------------------

/** \addtogroup SPELL_CIF */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
//============================================================================
/** Notification types */
typedef enum NotifyType_
{
    NOTIFY_ITEM,
    NOTIFY_VALUE,
    NOTIFY_VERIFICATION,
    NOTIFY_EXECUTION,
    NOTIFY_SYSTEM,
    NOTIFY_TIME
}
NotificationType;
//============================================================================

//============================================================================

//============================================================================
/** Notification information structure */
/*typedef struct ItemNotification_
{
    NotificationType       type;
    std::string            name;
    std::string            value;
    std::string            status;
    std::string            comment;
    std::string            time;
    std::string            stack;
}
ItemNotification;*/
//============================================================================

// DEFINES /////////////////////////////////////////////////////////////////



//============================================================================
/** String mappings for notification types */
namespace NotificationValue
{
//###############################################################################
//# UI notification type values
	/** Notification data type item */
	static const std::string DATA_NOTIF_TYPE_ITEM("ITEM");
	/** Notification data type value */
	static const std::string DATA_NOTIF_TYPE_VAL("VALUE");
	/** Notification data type verification */
	static const std::string DATA_NOTIF_TYPE_VERIF("VERIFICATION");
	/** Notification data type execution */
	static const std::string DATA_NOTIF_TYPE_EXEC("EXECUTION");
	/** Notification data type system */
	static const std::string DATA_NOTIF_TYPE_SYS("SYSTEM");
	/** Notification data type time */
	static const std::string DATA_NOTIF_TYPE_TIME("TIME");

//###############################################################################
//# UI notification status values
	/** Success operation */
	static const std::string DATA_NOTIF_STATUS_OK("SUCCESS");
	/** In progress operation */
	static const std::string DATA_NOTIF_STATUS_PR("IN PROGRESS");
	/** Failed operation */
	static const std::string DATA_NOTIF_STATUS_FL("FAILED");
	/** Skipped operation */
	static const std::string DATA_NOTIF_STATUS_SP("SKIPPED");


} //namespace NotificationValue

//============================================================================
namespace NotificationSeparator
{
	/** Separator for file and data lines */
	//static const std::string CODE_SEPARATOR("%C%");
	/** Separator for items in notifications */
	static const std::string ARG_SEPARATOR(",,");
	/** Separator for keys and values in prompts */
	//static const std::string KEY_SEPARATOR(":");
	/** Separator for options in prompts */
	//static const std::string OPT_SEPARATOR("|");
};


//============================================================================
/** String mappings for notification types */
static std::string NOTIF_TYPE_STR[] =
{
	NotificationValue::DATA_NOTIF_TYPE_ITEM,
	NotificationValue::DATA_NOTIF_TYPE_VAL,
	NotificationValue::DATA_NOTIF_TYPE_VERIF,
	NotificationValue::DATA_NOTIF_TYPE_EXEC,
	NotificationValue::DATA_NOTIF_TYPE_SYS,
	NotificationValue::DATA_NOTIF_TYPE_TIME
};

class ItemNotification
{
public:

    std::string getTokenizedTime();
    std::string getTokenizedComment();
    int getSuccessfulCount();

    NotificationType       type;
    std::string            name;
    std::string            value;
    std::string            status;
    std::string            comment;
    std::string            time;
    std::string            stack;


//protected: //-----------------------------------------------------------------



};
/*@}*/
#endif
