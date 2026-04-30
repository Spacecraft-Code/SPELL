// ################################################################################
// FILE       : SPELLerror.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL core exceptions
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

#ifndef __SPELL_ERROR_H__
#define __SPELL_ERROR_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmutex.H"

/** \addtogroup SPELL_UTIL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Definition of all error code numbers used in SPELL applications.
 **
 ** \par Description and usage:
 **
 **  	These values are to be used as the error code identifiers
 **  	for all SPELL core exceptions.
 **
 ** \see SPELLcoreException
 **
 *////////////////////////////////////////////////////////////////////////////
typedef enum SPELLerrorCode_
{
    // SPELL error codes (Python side)
    SPELL_PYERROR_IMPORT = 0,                // Failure to import a module
    SPELL_PYERROR_INDEX  = 1,                // Wrong index in list
    SPELL_PYERROR_KEY    = 2,                // Wrong key in dict
    SPELL_PYERROR_MEMORY = 3,                // Out of memory
    SPELL_PYERROR_NAME   = 4,                // Undefined variable/func
    SPELL_PYERROR_SYNTAX = 5,                // Syntax error
    SPELL_PYERROR_TYPE   = 6,                // Wrong data type used
    SPELL_PYERROR_VALUE  = 7,                // Unexpected value
    SPELL_PYERROR_OTHER  = 8,                // Other errors (uncontrolled)

    // SPELL error codes (internal)
    SPELL_DRIVER_ERROR   = 9,
    SPELL_ERROR_UNKNOWN  = 10,

    SPELL_ERROR_FILESYSTEM = 11,
    SPELL_ERROR_IPC        = 12,
    SPELL_ERROR_CONFIG     = 13,
    SPELL_ERROR_EXECUTION  = 14,
    SPELL_ERROR_PROCESS    = 15,
    SPELL_ERROR_PROCEDURES = 16,
    SPELL_ERROR_UTILS      = 17,
    SPELL_ERROR_SSH        = 18,
    SPELL_ERROR_SYNC       = 19,
    SPELL_ERROR_WSTART     = 20,
    SPELL_ERROR_ENVIRONMENT = 21,
    SPELL_ERROR_PYTHON_API = 22,
    SPELL_ERROR_LANGUAGE   = 23,

    // IPC errors
    SPELL_ERROR_CONNECTION_RESET = 9970,
    SPELL_ERROR_UNKNOWN_MSG_TYPE = 9980
}
SPELLerrorCode;


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Root of all executor exceptions.
 **
 ** \par Description and usage:
 **
 **  	Mechanism for error handling in all SPELL modules. The basic constructor
 **  	requires an error message and an explanation of the cause. Error codes
 **  	can be used for further processing.
 **
 ** \see SPELLerrorCode
 **
 *////////////////////////////////////////////////////////////////////////////
class SPELLcoreException
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * \param error IN: error description.
     * \param reason IN: error cause or explanation.
     * \param code IN: the associated error code.
     * \param fatal IN: true if the error is fatal and not recoverable.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( const std::string& error, const std::string& reason,
    		            const std::string& at, unsigned int line,
    		            SPELLerrorCode code, bool fatal )
    {
        m_error = error;
        m_reason = reason;
        m_code = code;
        m_fatal = fatal;
        m_at = at;
        m_line = line;
        loggit();
    }

    //////////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * \param error IN: error description.
     * \param reason IN: error cause or explanation.
     * \param code IN: the associated error code.
     * \param fatal IN: true if the error is fatal and not recoverable.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( const std::string& error, const std::string& reason,
    		            SPELLerrorCode code, bool fatal )
    {
        m_error = error;
        m_reason = reason;
        m_code = code;
        m_fatal = fatal;
        m_at = "none";
        m_line = 0;
        loggit();
    }

    //////////////////////////////////////////////////////////////////////////
    /** Const-copy constructor.
     *
     * \param original IN: exception to be copied.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( const SPELLcoreException& original )
    {
        m_error = original.m_error;
        m_reason = original.m_reason;
        m_code = original.m_code;
        m_fatal = original.m_fatal;
        m_at = original.m_at;
        m_line = original.m_line;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Copy constructor.
     *
     * \param original IN: exception to be copied.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException( SPELLcoreException& original )
    {
        m_error = original.m_error;
        m_reason = original.m_reason;
        m_code = original.m_code;
        m_fatal = original.m_fatal;
        m_at = original.m_at;
        m_line = original.m_line;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Assignment operator
     *
     * \param original IN: exception to be copied.
     *////////////////////////////////////////////////////////////////////////
    SPELLcoreException& operator=( const SPELLcoreException& original )
    {
    	if (this == &original)      // Same object?
	    {
    		return *this;
    	}
        m_error = original.m_error;
        m_reason = original.m_reason;
        m_code = original.m_code;
        m_fatal = original.m_fatal;
        m_at = original.m_at;
        m_line = original.m_line;
        return *this;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Equality operator
     *
     * \param original IN: exception to be compared.
     *////////////////////////////////////////////////////////////////////////
    bool operator==( const SPELLcoreException& other )
    {
    	if (this == &other)      // Same object?
	    {
    		return true;
    	}
        if (m_error != other.m_error) return false;
        if (m_reason != other.m_reason) return false;
        if (m_code != other.m_code) return false;
        if (m_fatal != other.m_fatal) return false;
        if (m_at != other.m_at) return false;
        if (m_line != other.m_line) return false;
        return true;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Destructor.
     *////////////////////////////////////////////////////////////////////////
    virtual ~SPELLcoreException() throw() {};

    // METHODS ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    /** Obtain the error message.
     *
     * \return The error message.
     *////////////////////////////////////////////////////////////////////////
    const std::string& getError() const {
        return m_error;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Set the error message.
     *
     * \param error IN: the error message.
     *////////////////////////////////////////////////////////////////////////
    void setError( std::string error ) {
        m_error = error;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Obtain the error reason.
     *
     * \return The error cause or explanation.
     *////////////////////////////////////////////////////////////////////////
    const std::string& getReason() const {
        return m_reason;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Set the error reason.
     *
     * \param reason IN: cause or explanation of the error.
     *////////////////////////////////////////////////////////////////////////
    void setReason( const std::string& reason ) {
        m_reason = reason;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Obtain the error code if any.
     *
     * \return The error code of the exception.
     *////////////////////////////////////////////////////////////////////////
    SPELLerrorCode getCode() const {
        return m_code;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Check if the error is fatal (unrecoverable).
     *
     * \return True if the error is fatal.
     *////////////////////////////////////////////////////////////////////////
    bool isFatal() const {
        return m_fatal;
    };

    //////////////////////////////////////////////////////////////////////////
    /** Convert to readable string.
     *
     * \return The error message as standard string.
     *////////////////////////////////////////////////////////////////////////
    const std::string what() const
    {
        std::string msg = m_error;
        if (m_reason != "") msg += ": " + m_reason;
        if (m_code != SPELL_ERROR_UNKNOWN) msg += " (error code " + ISTR(m_code) + ") (fatal:" + BSTR(m_fatal) + ")";
        return msg;
    }

    //////////////////////////////////////////////////////////////////////////
    /** Location
     *////////////////////////////////////////////////////////////////////////
    const std::string at() const
    {
    	return m_at + ":" + ISTR(m_line);
    }

    //////////////////////////////////////////////////////////////////////////
    /** Dump to logger.
     *////////////////////////////////////////////////////////////////////////
    void loggit() const
    {
    	if (m_line != 0)
    	{
    		LOG_ERROR("AT: " + at());
    	}
    	LOG_ERROR("EXCEPTION: " + what());
    }

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the error description */
		std::string     m_error;
		/** Holds the error reason */
		std::string     m_reason;
		/** Holds the error code */
		SPELLerrorCode  m_code;
		/** True if the system cannot be recovered after this error */
		bool            m_fatal;
		/** Location */
		std::string     m_at;
		/** Line */
		unsigned int    m_line;
};

#define THROW_EXCEPTION( __msg__, __reas__, __code__ ) {                               \
	                                    SPELLcoreException __exc__( __msg__, __reas__, __FILE__, __LINE__, __code__, false );\
	                                    throw __exc__;                      \
                                   }

#define THROW_FATAL_EXCEPTION( __msg__, __reas__, __code__ ) {                               \
	                                    SPELLcoreException __exc__( __msg__, __reas__, __FILE__, __LINE__, __code__, true );\
	                                    throw __exc__;                      \
                                   }

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Python binding object for "Execution Aborted" exceptions thrown
 **  from the C++ side. This exception is used when aborting the procedure
 **  execution programatically.
 **
 ** \par Description and usage:
 **
 **		This object allows the C++ layer throwing exceptions which are
 **		detected by the Python interpreter code.
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct PyExecutionAbortedObject_
{
    PyObject_HEAD;
    PyObject*    message;
}
PyExecutionAbortedObject;

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Python binding object for "Execution Terminated" exceptions thrown
 **  from the C++ side. This exception is used when forcing the procedure
 **  execution to terminate programatically.
 **
 ** \par Description and usage:
 **
 **		This object allows the C++ layer throwing exceptions which are
 **		detected by the Python interpreter code.
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct PyExecutionTerminatedObject_
{
    PyObject_HEAD;
}
PyExecutionTerminatedObject;

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Helper macro for injecting DriverException instances in the
 **  Python layer and returning NULL
 **
 ** \par Description and usage:
 **
 **		Used from C-0functions corresponding to Python binding objects.
 **
 ** \param msg IN: error message for the exception
 ** \param reason IN: error cause for the exception
 **
**////////////////////////////////////////////////////////////////////////////
#define THROW_DRIVER_EXCEPTION(msg,reason) SPELLerror::instance().throwDriverException( msg, reason ); return NULL

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Helper macro for injecting DriverException instances in the
 **  Python layer.
 **
 ** \par Description and usage:
 **
 **		Used from C-0functions corresponding to Python binding objects.
 **
 ** \param msg IN: error message for the exception
 ** \param reason IN: error cause for the exception
 **
**////////////////////////////////////////////////////////////////////////////
#define THROW_DRIVER_EXCEPTION_NR(msg,reason) SPELLerror::instance().throwDriverException( msg, reason )

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Helper macro for injecting SyntaxException instances in the
 **  Python layer and returning NULL
 **
 ** \par Description and usage:
 **
 **		Used from C-0functions corresponding to Python binding objects.
 **
 ** \param msg IN: error message for the exception
 ** \param reason IN: error cause for the exception
 **
**////////////////////////////////////////////////////////////////////////////
#define THROW_SYNTAX_EXCEPTION(msg,reason) SPELLerror::instance().throwSyntaxException( msg, reason ); return NULL

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Helper macro for injecting SyntaxException instances in the
 **  Python layer.
 **
 ** \par Description and usage:
 **
 **		Used from C-0functions corresponding to Python binding objects.
 **
 ** \param msg IN: error message for the exception
 ** \param reason IN: error cause for the exception
 **
**////////////////////////////////////////////////////////////////////////////
#define THROW_SYNTAX_EXCEPTION_NR(msg,reason) SPELLerror::instance().throwSyntaxException( msg, reason )

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Helper macro for injecting RuntimeError instances in the
 **  Python layer and returning NULL
 **
 ** \par Description and usage:
 **
 **		Used from C-0functions corresponding to Python binding objects.
 **
 ** \param msg IN: error message for the exception
 ** \param reason IN: error cause for the exception
 **
**////////////////////////////////////////////////////////////////////////////
#define THROW_RUNTIME_EXCEPTION(msg,reason) SPELLerror::instance().throwRuntimeException( msg, reason ); return NULL

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Helper macro for injecting RuntimeError instances in the
 **  Python layer.
 **
 ** \par Description and usage:
 **
 **		Used from C-0functions corresponding to Python binding objects.
 **
 ** \param msg IN: error message for the exception
 ** \param reason IN: error cause for the exception
 **
**////////////////////////////////////////////////////////////////////////////
#define THROW_RUNTIME_EXCEPTION_NR(msg,reason) SPELLerror::instance().throwRuntimeException( msg, reason )

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Allows managing and controlling Python errors.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLerror
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLerror();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Get the singleton instance
		**////////////////////////////////////////////////////////////////////
		static SPELLerror& instance();

		//////////////////////////////////////////////////////////////////////
		/** Clear the errors
		**////////////////////////////////////////////////////////////////////
		void clearErrors();

		//////////////////////////////////////////////////////////////////////
		/** Ensure that the error data is synchornized with Python error data
		**////////////////////////////////////////////////////////////////////
		void updateErrors();

		//////////////////////////////////////////////////////////////////////
		/** Set the execution aborted exception
		**////////////////////////////////////////////////////////////////////
		void setExecutionAborted();

		//////////////////////////////////////////////////////////////////////
		/** Set the execution terminated exception
		**////////////////////////////////////////////////////////////////////
		void setExecutionTerminated();

		//////////////////////////////////////////////////////////////////////
		/** Get if the current error is execution terminated
		**////////////////////////////////////////////////////////////////////
		bool isExecutionTerminated();

		//////////////////////////////////////////////////////////////////////
		/** Get if the current error is execution aborted
		**////////////////////////////////////////////////////////////////////
		bool isExecutionAborted();

		//////////////////////////////////////////////////////////////////////
		/** Get the current error
		**////////////////////////////////////////////////////////////////////
		SPELLcoreException* getError();

		//////////////////////////////////////////////////////////////////////
		/** Get the current error location
		**////////////////////////////////////////////////////////////////////
		std::string getErrorLocation() { return m_errorLocation; };

		//////////////////////////////////////////////////////////////////////
		/** True if in error
		**////////////////////////////////////////////////////////////////////
		bool inError();

		//////////////////////////////////////////////////////////////////////
		/** Convert a Python exception to internal representation
		**////////////////////////////////////////////////////////////////////
		SPELLcoreException* errorToException( PyObject* err, PyObject* ptype, PyObject* pvalue, PyObject* ptraceback );

		//////////////////////////////////////////////////////////////////////
		/** Translate current Python error to internal representation
		 *
		 * \return The core exception with the error information or NULL.
		**////////////////////////////////////////////////////////////////////
		SPELLcoreException* errorToException();

		//////////////////////////////////////////////////////////////////////
		/** Throw a runtime exception in the Python layer
		**////////////////////////////////////////////////////////////////////
		void throwRuntimeException( const std::string& message, const std::string& reason );

		//////////////////////////////////////////////////////////////////////
		/** Throw a syntax exception in the Python layer
		**////////////////////////////////////////////////////////////////////
		void throwSyntaxException( const std::string& message, const std::string& reason );

		//////////////////////////////////////////////////////////////////////
		/** Throw a driver exception in the Python layer
		**////////////////////////////////////////////////////////////////////
		void throwDriverException( const std::string& message, const std::string& reason );

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////

		enum CtrlException
		{
			EXC_ABORTED    = 0,
			EXC_TERMINATED = 1,
			EXC_NONE       = 2
		};

    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLerror();

		//////////////////////////////////////////////////////////////////////
		/** Get Python error
		**////////////////////////////////////////////////////////////////////
		void getPythonError();

		//////////////////////////////////////////////////////////////////////
		/** Get Python error
		**////////////////////////////////////////////////////////////////////
		void setPythonError( PyObject* instance, PyObject* errClass, const std::string& message, const std::string& reason );

    // METHODS ///////////////////////////////////////////////////////////////

    // DATA MEMBERS //////////////////////////////////////////////////////////

		static SPELLerror* s_instance;
		static SPELLmutex s_instanceLock;

		PyObject* m_excExecutionAborted;
		PyObject* m_excExecutionTerminated;

		CtrlException       m_ctrlExceptionSet;
		SPELLcoreException* m_currentError;
		std::string         m_errorLocation;
};


/*@}*/
#endif
