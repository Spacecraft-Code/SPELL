// ################################################################################
// FILE       : SPELLcifPromptHelper.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Helper for prompt processing
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_CIFS/SPELLcifPromptHelper.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------


//=============================================================================
// METHOD: SPELLcifPromptHelper::completeOptions()
//=============================================================================
std::string SPELLcifPromptHelper::completeOptions( const SPELLpromptDefinition& def, SPELLipcMessage& msg, const std::string& origMsgToShow )
{
	std::string msgToShow = origMsgToShow;

	msgToShow += "\nAvailable options:\n";

    int keyCount = 0;
    std::string optionStr = "";
    std::string expectedStr = "";

    // Iterate over the option list and build the option and expected values strings
    SPELLpromptDefinition::Options::const_iterator it;
    for( it = def.options.begin(); it != def.options.end(); it++)
    {
        if (optionStr.size()>0) optionStr += IPCinternals::OPT_SEPARATOR;
        if (expectedStr.size()>0) expectedStr += IPCinternals::OPT_SEPARATOR;
        optionStr += (*it);
        expectedStr += def.expected[keyCount];
        keyCount++;

        // For the display message
		msgToShow += "   - " + (*it) + "\n";
    }
    msg.set(MessageField::FIELD_EXPECTED, expectedStr);
    msg.set(MessageField::FIELD_OPTIONS, optionStr);

    return msgToShow;
}
