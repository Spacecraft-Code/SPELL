// ################################################################################
// FILE       : SPELLpythonMonitors.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Python safe operation monitors
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

#ifndef __SPELL_PYTHON_MONITORS_H__
#define __SPELL_PYTHON_MONITORS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_UTIL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Monitor class to ensure safe Python operations
 **
 ** \par Description and usage:
 **
 ** 	Instantiate on a block of code where Python layer operations are about
 **     to be performed.
 **
 **  	Holds the Python layer interpretation process so that Python objects
 **  	can be safely manipulated from C++ side.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLsafePythonOperations
{
public:
	SPELLsafePythonOperations( const std::string& where )
	{
		//std::cerr << "PYTHON OPERATIONS TRY-IN " << where << std::endl;
		m_state = SPELLpythonHelper::instance().acquireGIL();
		//std::cerr << ">>PYTHON OPERATIONS IN " << where << std::endl;
		m_where = where;
	};
	virtual ~SPELLsafePythonOperations() noexcept(false)
	{
		PyObject* err = PyErr_Occurred();
		if (err != NULL)
		{
			std::cerr << std::endl << "===============================" << std::endl;
			//PyErr_Print();
			PyObject* ptype;
			PyObject* pvalue;
			PyObject* ptraceback;
			PyErr_Fetch( &ptype, &pvalue, &ptraceback );
			std::cerr << "TYPE : " << PYREPR(ptype) << std::endl;
			std::cerr << "VALUE: " << PYREPR(pvalue) << std::endl;
			if (ptraceback)
			{
				std::cerr << "Traceback: " << std::endl;
				PyTracebackObject* traceback = (PyTracebackObject*) ptraceback;
				while(traceback != NULL)
				{
					std::cerr << "at " << PYREPR(traceback->tb_frame->f_code->co_filename) << ":" << traceback->tb_lineno << std::endl;
					traceback = traceback->tb_next;
				}
			}
			std::cerr << "===============================" << std::endl << std::endl;
			// Parse the exception to give better information about the error on python side
			PyObject* spellException = SPELLpythonHelper::instance().getObject("spell.lib.exception", "SpellException");
			std::string excType = PYREPR(ptype);
			if (PyObject_IsInstance( pvalue, spellException ))
			{
				PyObject* msg = PyObject_GetAttrString( pvalue, "message" );
				PyObject* rea = PyObject_GetAttrString( pvalue, "reason" );
				std::string message = "<unknown error>";
				std::string reason  = "";
				if (msg != NULL)
				{
					message = PYSSTR(msg);
				}
				if (rea != NULL )
				{
					reason = PYSSTR(rea);
				}
				/** \todo decide the fatal flag value */
				std::cerr << "RAISING INTERNAL DRIVER EXCEPTION" << std::endl;
				PyErr_Clear();
				THROW_EXCEPTION("Driver error: " + message, reason, SPELL_DRIVER_ERROR );
			}
			else if ( PYSTR(pvalue) == "EXECUTION ABORTED" || PYSTR(pvalue) == "EXECUTION TERMINATED" )
			{
				// Do not raise exceptions if the detected exception is an aborted or finished internal exception
				return;
			}
			else
			{
				std::cerr << "RAISING PYTHON ERROR EXCEPTION" << std::endl;
				PyErr_Clear();
				THROW_EXCEPTION("Python error", PYSSTR(PyObject_Str(pvalue)), SPELL_ERROR_PYTHON_API );
			}
		}
		SPELLpythonHelper::instance().releaseGIL(m_state);
		//std::cerr << "<<PYTHON OPERATIONS OUT " << m_where << std::endl;
	};
private:
	PyGILState_STATE m_state;
	std::string m_where;
};

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Monitor class to ensure safe multi-thread operations
 **
 ** \par Description and usage:
 **
 **  	Enables the Python layer to continue execution in other intepreter & thread
 **     states meanwhile the C++ code is blocked in a waiting or IO operations.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLsafeThreadOperations
{
public:
		SPELLsafeThreadOperations( const std::string& where )
		{
			//std::cerr << "THREAD OPERATIONS IN " << where << std::endl;
			m_state = SPELLpythonHelper::instance().beginSafeAllowThreads();
			m_where = where;
		};
		~SPELLsafeThreadOperations()
		{
			SPELLpythonHelper::instance().endSafeAllowThreads( m_state );
			//std::cerr << "THREAD OPERATIONS OUT " << m_where << std::endl;
		};
private:
	 PyThreadState* m_state;
	 std::string m_where;
};

/*@}*/
#endif
