// ################################################################################
// FILE       : SPELLpythonHelper.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Python utilities and manipulation functions
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

#ifndef __SPELL_PYHELPER_H__
#define __SPELL_PYHELPER_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLtime.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmutex.H"
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_UTIL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Provides utility methods for Python layer operations.
 **
 ** \par Description and usage:
 **
 ** 	Singleton used from anywhere in the SPELL code to manipulate
 ** 	Python objects or to access the Python layer from C++ side.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLpythonHelper
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLpythonHelper();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Provide the helper instance.
		 *
		 * \return The singleton instance.
		**////////////////////////////////////////////////////////////////////
		static SPELLpythonHelper& instance();

		//////////////////////////////////////////////////////////////////////
		/** Initialize python support.
		 *
		 * \throws SPELLcoreException if the Python thread support cannot be initialized.
		 * \throws SPELLcoreException if the SPELL_HOME environment variable is not defined.
		 * \throws SPELLcoreException if the SPELL_COTS environment variable is not defined.
		 *
		**////////////////////////////////////////////////////////////////////
		void initialize();

		//////////////////////////////////////////////////////////////////////
		/** Cleanup python support.
		**////////////////////////////////////////////////////////////////////
		void finalize();

		//////////////////////////////////////////////////////////////////////
		/** Check if initialized.
		**////////////////////////////////////////////////////////////////////
		bool isInitialized() {
			return m_initialized;
		};

		//////////////////////////////////////////////////////////////////////
		/** Add directory to Python path.
		 *
		 * \param path IN: path to append to the Python path.
		**////////////////////////////////////////////////////////////////////
		void addToPath( const std::string& path );

		//////////////////////////////////////////////////////////////////////
		/** Import all items from the given package into main package.
		 *
		 * \throws SPELLcoreException if the given module cannot be imported.
		 * \throws SPELLcoreException if the __main__ module cannot be accessed.
		 * \throws SPELLcoreException if the __main__ module dictionary cannot be accessed.
		 *
		 * \param package IN: the package to import objects from.
		**////////////////////////////////////////////////////////////////////
		void importAllFrom( const std::string& package );

		//////////////////////////////////////////////////////////////////////
		/** Import an user library module
		 *
		 * \throws SPELLcoreException if the given module cannot be imported.
		 * \throws SPELLcoreException if the __main__ module cannot be accessed.
		 * \throws SPELLcoreException if the __main__ module dictionary cannot be accessed.
		 *
		 * \param package IN: the package to import objects from.
		**////////////////////////////////////////////////////////////////////
		void importUserLibraries( const std::string& libraryPath );

		//////////////////////////////////////////////////////////////////////
		/** Install an object in main dictionary
		 *
		 * \param object IN: the object to install.
		 * \param name IN: the name to be used for the object.
		 *
		 * \throws SPELLcoreException if the install fails due to failure to get the module.
		 * \throws SPELLcoreException if the install fails due to failure to get the module dictionary.
		 * \throws SPELLcoreException if the install fails due to failure to insert the instance.
		 *
		**////////////////////////////////////////////////////////////////////
		void install( PyObject* object, const std::string& name );

		//////////////////////////////////////////////////////////////////////
		/** Install an object in the given module
		 *
		 * \param object IN: the object to install
		 * \param name IN: the name to be used for the object.
		 * \param module IN: module to install the object into.
		 *
		 * \throws SPELLcoreException if the install fails due to failure to get the module.
		 * \throws SPELLcoreException if the install fails due to failure to get the module dictionary.
		 * \throws SPELLcoreException if the install fails due to failure to insert the instance.
		 *
		**////////////////////////////////////////////////////////////////////
		void install( PyObject* object, const std::string& name, const std::string& module );

		//////////////////////////////////////////////////////////////////////
		/** Load the SPELL framework modules
		 *
		**////////////////////////////////////////////////////////////////////
		void loadFramework();

		//////////////////////////////////////////////////////////////////////
		/** Obtain globals dictionary
		 *
		 * \throws SPELLcoreException if the main dictionary cannot be accessed.
		 *
		 * \return Borrowed reference of the main dictionary.
		**////////////////////////////////////////////////////////////////////
		PyObject* getMainDict();

		//////////////////////////////////////////////////////////////////////
		/** Compile the given script.
		 *
		 * \param filePath IN: absolute path to the script to compile.
		 *
		 * \return The resulting code object.
		**////////////////////////////////////////////////////////////////////
		PyCodeObject* compile( const std::string& filePath );

		//////////////////////////////////////////////////////////////////////
		/** Compile the given source code.
		 *
		 * \param source IN: Python code to compile
		 *
		 * \return The resulting code object.
		**////////////////////////////////////////////////////////////////////
		PyCodeObject* compileScript( const std::string& source );

		//////////////////////////////////////////////////////////////////////
		/** Change the current line in the given frame.
		 *
		 * \param frame INOUT: frame to manipulate
		 * \param new_lineno IN: new line number
		 * \param new_lasti IN: new last instruction offset
		 *
		 * \return True if success
		**////////////////////////////////////////////////////////////////////
		bool setNewLine( PyFrameObject* frame, const int& new_lineno, const int& new_lasti );

		//////////////////////////////////////////////////////////////////////
		/** Create a frame for the given code object.
		 *
		 * \param filename IN: associated file name
		 * \param code IN: code object to execute
		 *
		 * \return The new frame
		**////////////////////////////////////////////////////////////////////
		PyFrameObject* createFrame( const std::string& filename, PyCodeObject* code );

		//////////////////////////////////////////////////////////////////////
		/** Call the given method of the given object, with variable arguments
		 *
		 * \param obj IN: object providing the desired method.
		 * \param method IN: name of the method to be invoked
		 * \param ... IN: variable argument list.
		 *
		 * \throws SPELLcoreException if the method does not exist
		 * \throws SPELLcoreException if the variable arguments cannot be extracted.
		 *
		 * \return The result of the invocation
		**////////////////////////////////////////////////////////////////////
		PyObject* callMethod( PyObject* obj, const std::string& method, ... );

		//////////////////////////////////////////////////////////////////////
		/** Call the given function of the given module, with variable arguments
		 *
		 * \param module IN: module providing the desired function.
		 * \param function IN: name of the function to be invoked
		 * \param ... IN: variable argument list.
		 *
		 * \throws SPELLcoreException if the function does not exist
		 * \throws SPELLcoreException if the variable arguments cannot be extracted.
		 *
		 * \return The result of the invocation
		**////////////////////////////////////////////////////////////////////
		PyObject* callFunction( const std::string& module, const std::string& function, ... );

		//////////////////////////////////////////////////////////////////////
		/** Call the given SPELL function with variable arguments
		 *
		 * \param function IN: SPELL function to be invoked
		 * \param args IN: fixed argument list
		 * \param kargs IN: kweyword argument list
		 *
		 * \throws SPELLcoreException if the function does not exist
		 *
		 * \return The result of the invocation
		**////////////////////////////////////////////////////////////////////
		PyObject* callSpellFunction( const std::string& function, PyObject* args, PyObject* kargs );

		//////////////////////////////////////////////////////////////////////
		/** Obtain a given module object.
		 *
		 * \param module IN: module name
		 * \param object IN: object instance name
		 *
		 * \throws SPELLcoreException if the module does not exist.
		 * \throws SPELLcoreException if it is not possible to access the module dictionary.
		 *
		 * \return The object instance or NULL.
		**////////////////////////////////////////////////////////////////////
		PyObject* getObject( const std::string& module, const std::string& object );

		//////////////////////////////////////////////////////////////////////
		/** Evaluate a Python expression.
		 *
		 * \param expression IN: expression to be evaluated.
		 * \param file IN: execute in file mode, so multiline expressions are accepted
		 *
		 * \throws SPELLcoreException from checkError(), caused by any Python layer error.
		 *
		 * \return The result of the evaluation.
		**////////////////////////////////////////////////////////////////////
		PyObject* eval( const std::string& expression, bool file );

		//////////////////////////////////////////////////////////////////////
		/** Create a new instance of an object class.
		 *
		 * \param classObj IN: class object
		 * \param argsTuple IN: tuple for positional arguments
		 * \param kwdsDict IN: dictionary for keyword arguments
		 *
		 * \return The new instance of the object.
		**////////////////////////////////////////////////////////////////////
		PyObject* newInstance( PyObject* classObj, PyObject* argsTuple, PyObject* kwdsDict );

		//////////////////////////////////////////////////////////////////////
		/** Check if a Python object is a TIME instance
		 *
		 * \param instance IN: instance to be evaluated.
		 *
		 * \return True if it is a TIME instance, false otherwise
		**////////////////////////////////////////////////////////////////////
		bool isTime( PyObject* instance );

		//////////////////////////////////////////////////////////////////////
		/** Check if a Python object is a SPELL database
		 *
		 * \param instance IN: instance to be evaluated.
		 *
		 * \return True if it is a SPELL database instance, false otherwise
		**////////////////////////////////////////////////////////////////////
		bool isDatabase( PyObject* instance );

		//////////////////////////////////////////////////////////////////////
		/** Evaluate a TIME expression.
		 *
		 * \param expression IN: expression to be evaluated.
		 *
		 * \throws SPELLcoreException if the TIME Python class cannot be accessed.
		 * \throws SPELLcoreException if the passed expression is not a TIME instance.
		 *
		 * \return The time object
		**////////////////////////////////////////////////////////////////////
		SPELLtime evalTime( PyObject* expression );

		//////////////////////////////////////////////////////////////////////
		/** Evaluate a TIME expression from a string.
		 *
		 * \param expression IN: string expression to be evaluated.
		 *
		 * \throws SPELLcoreException if the TIME Python class cannot be accessed.
		 * \throws SPELLcoreException if the passed expression cannot be processed.
		 *
		 * \return The time object
		**////////////////////////////////////////////////////////////////////
		SPELLtime evalTime( const std::string& expression );

		//////////////////////////////////////////////////////////////////////
		/** Create a Python TIME instance from given seconds.
		 *
		 * \throws SPELLcoreException if the TIME Python class cannot be accessed.
		 *
		 * \return The time object
		**////////////////////////////////////////////////////////////////////
		PyObject* pythonTime( const SPELLtime& time );

		//////////////////////////////////////////////////////////////////////
		/** Create a Python TIME instance from given expression
		 *
		 * \throws SPELLcoreException if the TIME Python class cannot be accessed.
		 *
		 * \return The time object
		**////////////////////////////////////////////////////////////////////
		PyObject* pythonTime( const std::string& expression );

		//////////////////////////////////////////////////////////////////////
		/** Release locks affecting the Python interpreter layer so that Python
		 * code interpretation can continue even if the calling code is blocked.
		 * This is basically needed when the business code will be waiting on
		 * a condition or IO operation, and still we want Python code to keep
		 * executing (i.e. threads on the Python side).
		 *
		 * \return The Python thread state to be restored later on.
		**////////////////////////////////////////////////////////////////////
		PyThreadState* beginSafeAllowThreads();

		//////////////////////////////////////////////////////////////////////
		/** Re-acquire locks affecting the Python interpreter layer so that Python
		 * code interpretation is on hold again, while the calling code is executed.
		 * This must be called after the beginSafeAllowThreads call, once the
		 * blocking operation in the business code is finished.
		 *
		 * \param tstate IN: The Python thread state to be restored.
		 *
		**////////////////////////////////////////////////////////////////////
		void endSafeAllowThreads( PyThreadState* tstate );

		//////////////////////////////////////////////////////////////////////
		/** Must be called whenever the business code will manipulate Python
		 * objects on the Python layer.
		 *
		 * \return The Python GIL state.
		**////////////////////////////////////////////////////////////////////
		PyGILState_STATE acquireGIL();

		//////////////////////////////////////////////////////////////////////
		/** Must be called whenever the business code has finished manipulating
		 * Python objects on the Python layer.
		 *
		 * \param state IN: The Python GIL state.
		**////////////////////////////////////////////////////////////////////
		void releaseGIL( PyGILState_STATE state );

		//////////////////////////////////////////////////////////////////////
		/** Check for errors in the Python layer.
		**////////////////////////////////////////////////////////////////////
		void checkError();

		//////////////////////////////////////////////////////////////////////
		/** Check instance match
		**////////////////////////////////////////////////////////////////////
		bool isInstance( PyObject* object, const std::string& className, const std::string& package );

		//////////////////////////////////////////////////////////////////////
		/** Check subclass instance match
		**////////////////////////////////////////////////////////////////////
		bool isSubclassInstance( PyObject* object, const std::string& className, const std::string& package );

		//////////////////////////////////////////////////////////////////////
		/** Check class type
		**////////////////////////////////////////////////////////////////////
		bool isClass( PyObject* object );

		//////////////////////////////////////////////////////////////////////
		/** Check instance type
		**////////////////////////////////////////////////////////////////////
		bool isInstance( PyObject* object );

		//////////////////////////////////////////////////////////////////////
		/** Read procedure files. This is used when the read source code
		 * is going to be compiled with the Python API. Special care has to
		 * be taken regarding encoding and unicode strings.
		**////////////////////////////////////////////////////////////////////
		std::string readProcedureFile( const std::string& filename );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLpythonHelper();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Import the given user library module
		 *
		 *////////////////////////////////////////////////////////////////////
		void importUserLibrary( PyObject* dict, const std::string& libraryFile );

		//////////////////////////////////////////////////////////////////////
		/** Update the globals in the given user library module
		 *
		 *////////////////////////////////////////////////////////////////////
		void updateUserLibrary( PyObject* dict, const std::string& libraryFile );

		//////////////////////////////////////////////////////////////////////
		/** Get the module dictionary of the given library
		 *
		 *////////////////////////////////////////////////////////////////////
		PyObject* getUserLibraryDict( const std::string& libraryFile );

		//////////////////////////////////////////////////////////////////////
		/** Load the given module on Python side and provide its reference.
		 *
		 * \param module IN: module name (absolute)
		 *
		 * \throws SPELLcoreException if the module cannot be imported.
		 *
		 * \return The module instance object.
		**////////////////////////////////////////////////////////////////////
		PyObject* getModule( const std::string& module );

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** True if initialized */
		bool m_initialized;
		/** Holds the helper instance */
		static SPELLpythonHelper* s_instance;
		/** Instance synchrnonization */
		static SPELLmutex s_instanceLock;
};

/*@}*/
#endif
