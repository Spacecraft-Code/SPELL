// ################################################################################
// FILE       : SPELLexecutorUtils.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor utilities
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

#ifndef __SPELL_EXEC_UTILS_H__
#define __SPELL_EXEC_UTILS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLscheduleCondition.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLpyArgs.H"


/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executor utilities
 **
**////////////////////////////////////////////////////////////////////////////
namespace SPELLexecutorUtils
{
	//////////////////////////////////////////////////////////////////////////////
	/** Extract the data defining a time interval for WaitFor() function
	 * notifications from the given arguments, and configure the given condition.
	 *
	 * \param arguments IN: Python arguments
	 * \param condition INOUT: condition to configure.
	 **
	**////////////////////////////////////////////////////////////////////////////
	void getIntervalData( SPELLpyArgs& arguments, SPELLscheduleCondition& condition );

	//////////////////////////////////////////////////////////////////////////////
	/** Configure the given scheduling condition using the keyword arguments
	 * (SPELL modifiers) passed with the Python arguments.
	 *
	 * \param arguments IN: Python arguments
	 * \param condition INOUT: condition to configure.
	 **
	**////////////////////////////////////////////////////////////////////////////
	void configureConditionModifiers( SPELLpyArgs& arguments, SPELLscheduleCondition& condition );

	//////////////////////////////////////////////////////////////////////////////
	/** Configure the given scheduling condition using the positional arguments
	 * (SPELL modifiers) passed with the Python arguments.
	 *
	 * \param arguments IN: Python arguments
	 * \param condition INOUT: condition to configure.
	 * \param configObj IN: config Python object
	 **
	**////////////////////////////////////////////////////////////////////////////
	void configureConditionArguments( SPELLpyArgs& arguments, SPELLscheduleCondition& condition, PyObject* configObj );

	//////////////////////////////////////////////////////////////////////////////
	/** Configure the scheduling condition as a time condition, obtaining the
	 * data from the Python arguments.
	 *
	 * \param arguments IN: Python arguments
	 * \param condition INOUT: condition to configure.
	 **
	**////////////////////////////////////////////////////////////////////////////
	void setTimeCondition( SPELLpyArgs& arguments, SPELLscheduleCondition& condition );

	//////////////////////////////////////////////////////////////////////////////
	/** Configure the scheduling condition as a TM verification condition, obtaining the
	 * data from the Python arguments.
	 *
	 * \param arguments IN: Python arguments
	 * \param condition INOUT: condition to configure.
	 * \param configObj IN: config Python object
	 **
	**////////////////////////////////////////////////////////////////////////////
	void setVerificationCondition( SPELLpyArgs& arguments, SPELLscheduleCondition& condition, PyObject* configObj );

	//////////////////////////////////////////////////////////////////////////////
	/** Configure the scheduling condition as an evaluation condition, obtaining the
	 * data from the Python arguments.
	 *
	 * \param arguments IN: Python arguments
	 * \param condition INOUT: condition to configure.
	 **
	**////////////////////////////////////////////////////////////////////////////
	void setEvaluationCondition( SPELLpyArgs& arguments, SPELLscheduleCondition& condition );

	//////////////////////////////////////////////////////////////////////////////
	/** Convert the given status to standard string
	**////////////////////////////////////////////////////////////////////////////
	std::string statusToString( SPELLexecutorStatus st );

	//////////////////////////////////////////////////////////////////////////////
	/** Convert the given status to Python string
	**////////////////////////////////////////////////////////////////////////////
	PyObject* statusToPyString( SPELLexecutorStatus st );

	//////////////////////////////////////////////////////////////////////////////
	/** Convert the given string to status code
	**////////////////////////////////////////////////////////////////////////////
	SPELLexecutorStatus stringToStatus( std::string st );

	//////////////////////////////////////////////////////////////////////////////
	/** Convert the given Python string to status code
	**////////////////////////////////////////////////////////////////////////////
	SPELLexecutorStatus pyStringToStatus( PyObject* pyst );

};

/*@}*/
#endif
