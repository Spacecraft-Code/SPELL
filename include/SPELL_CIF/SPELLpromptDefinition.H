// ################################################################################
// FILE       : SPELLpromptDefinition.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Parameters defining a prompt
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

#ifndef __SPELL_PROMPT_DEFINITION_H__
#define __SPELL_PROMPT_DEFINITION_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLtime.H"

/** \addtogroup SPELL_CIF */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

//============================================================================
/** Return value for cancelled prompts */
#define PROMPT_CANCELLED "<CANCEL>"
/** Return value for timed-out prompts */
#define PROMPT_TIMEOUT   "<TIMEOUT>"
/** Return value for failed prompts */
#define PROMPT_ERROR     "<ERROR>"
/** Return value for prompts without clients to attend*/
#define PROMPT_NOCLIENT  "<NOCLIENT>"


class SPELLpromptDefinition
{
public:

	/** Represents the list of available options for a prompt */
	typedef std::vector<std::string> Options;

	std::string message;
	Options options;
	Options expected;
	std::string defaultAnswer;
	unsigned int scope;
	unsigned int typecode;
	SPELLtime timeout;
};

/*@}*/
#endif
