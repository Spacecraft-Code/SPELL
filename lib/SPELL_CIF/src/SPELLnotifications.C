// ################################################################################
// FILE       : SPELLnotifications.C
// DATE       : Jan 23, 2014
// PROJECT    : SPELL
// DESCRIPTION: Notification data class
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
// System includes ---------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLutils.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CIF/SPELLnotifications.H"
// Project includes --------------------------------------------------------



// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////
// STATIC //////////////////////////////////////////////////////////////////


//=============================================================================
// METHOD    : ItemNotification::getTokenizedTime
//=============================================================================
std::string ItemNotification::getTokenizedTime()
{
	// Local variables
	std::string l_timeStr;

	l_timeStr = time;

	if (status.find(NotificationSeparator::ARG_SEPARATOR) != std::string::npos )
	{
		std::vector<std::string> statusList = SPELLutils::tokenize(status,NotificationSeparator::ARG_SEPARATOR);
		std::vector<std::string> timeList = SPELLutils::tokenize(l_timeStr, NotificationSeparator::ARG_SEPARATOR);
				if (timeList.size() == 1)
				{
					l_timeStr = "";
					std::string tstamp = SPELLutils::timestamp();
					for( unsigned int count=0; count<statusList.size(); count++)
					{
						if (l_timeStr.size()>0) l_timeStr += NotificationSeparator::ARG_SEPARATOR;
						l_timeStr += tstamp;
					}
				}

	} //if separators on status

	return l_timeStr;
} //getName

//=============================================================================
// METHOD    : ItemNotification::getTokenizedComment
//=============================================================================
std::string ItemNotification::getTokenizedComment()
{
	// Local variables
	std::string l_commentStr;

	l_commentStr = comment;

	if (status.find(NotificationSeparator::ARG_SEPARATOR) != std::string::npos )
	{

		std::vector<std::string> statusList = SPELLutils::tokenize(status,NotificationSeparator::ARG_SEPARATOR);
		std::vector<std::string> l_commentStrList = SPELLutils::tokenize(l_commentStr,NotificationSeparator::ARG_SEPARATOR);
		if (l_commentStrList.size() == 1)
		{
			l_commentStr = "";
			for( unsigned int count=0; count<statusList.size(); count++)
			{
				if (l_commentStr.size()>0) l_commentStr += NotificationSeparator::ARG_SEPARATOR;
				l_commentStr += " ";
			}
		}
	} //if separators on status

	return l_commentStr;
} //getTokenizedComment()

//=============================================================================
// METHOD    : ItemNotification::getSuccessfulCount
//=============================================================================
int ItemNotification::getSuccessfulCount()
{
	//local Variables
	int l_sCount = 0;

	if (status.find(NotificationSeparator::ARG_SEPARATOR) != std::string::npos )
	{
		std::vector<std::string> statusList = SPELLutils::tokenize(status,NotificationSeparator::ARG_SEPARATOR);
		std::vector<std::string>::iterator it;

		for( it = statusList.begin(); it != statusList.end(); it++)
		{
			if (*it == NotificationValue::DATA_NOTIF_STATUS_OK) l_sCount++;
		} //for

	}
	else
	{
		l_sCount = (status == NotificationValue::DATA_NOTIF_STATUS_OK) ? 1 : 0;

	} //self status finds SEPARATOR

	return l_sCount;
} //getSuccesfulCount
