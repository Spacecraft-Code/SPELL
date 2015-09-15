// ################################################################################
// FILE       : SPELLpythonHelper.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the Python helper
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
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_WRP/SPELLpyHandle.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
// System includes ---------------------------------------------------------
#include <stdarg.h>
#include "Python-ast.h"
#include "opcode.h"
#include "traceback.h"

// See setNewLine method
#undef MIN
#undef MAX
#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define MAX(a, b) ((a) > (b) ? (a) : (b))




// Singleton instance
SPELLpythonHelper* SPELLpythonHelper::s_instance = 0;
// Instance lock
SPELLmutex SPELLpythonHelper::s_instanceLock;

//=============================================================================
// FUNCTION: objargs_mktuple
// DESCRIPTION: helper for creating an argument tuple from a variable argument list.
//=============================================================================
static PyObject * objargs_mktuple(va_list va)
{
    int i, n = 0;
    va_list countva;
    PyObject *result, *tmp;
    countva = va;
    while ( ( (PyObject *) va_arg(countva, PyObject *)) != NULL)
    {
            ++n;
    }
    result = PyTuple_New(n);
    if (result != NULL && n > 0)
    {
        for (i = 0; i < n; ++i)
        {
                tmp = (PyObject *)va_arg(va, PyObject *);
                PyTuple_SET_ITEM(result, i, tmp);
                Py_INCREF(tmp);
        }
    }
    return result;
}

//=============================================================================
// CONSTRUCTOR : SPELLpythonHelper::SPELLpythonHelper
//=============================================================================
SPELLpythonHelper::SPELLpythonHelper()
{
    m_initialized = false;
}

//=============================================================================
// DESTRUCTOR : SPELLpythonHelper::~SPELLpythonHelper
//=============================================================================
SPELLpythonHelper::~SPELLpythonHelper()
{
}

//=============================================================================
// METHOD    : SPELLpythonHelper::instance()
//=============================================================================
SPELLpythonHelper& SPELLpythonHelper::instance()
{
    SPELLmonitor m(s_instanceLock);
    if (s_instance == NULL)
    {
        s_instance = new SPELLpythonHelper();
    }
    return *s_instance;
}

//============================================================================
// METROD    : SPELLpythonHelper::importAllFrom
//============================================================================
void SPELLpythonHelper::importAllFrom( const std::string& package )
{
    LOG_INFO("Import all from " + package);
    PyObject* dict = PyDict_New();
    PyObject* module = PyImport_ImportModuleEx( const_cast<char*>(package.c_str()), dict, dict, NULL);
    if ( module == NULL )
    {
        SPELLcoreException* ex = SPELLerror::instance().errorToException();
        throw *ex;
    }

    // Borrowed
    PyObject* moduleDict = PyModule_GetDict( module );

    std::vector<std::string> tokens = SPELLutils::tokenize( package, "." );

    std::vector<std::string>::iterator it;
    for( it = tokens.begin(); it != tokens.end(); it++ )
    {
        // Skip the first token
        if (it == tokens.begin()) continue;
        // Get the submodule
        // Borrowed reference
        module = PyDict_GetItemString( moduleDict, (*it).c_str() );
        moduleDict = PyModule_GetDict( module );
    }

    PyObject* mainModule = PyImport_AddModule("__main__");
    if (mainModule == NULL)
    {
        THROW_EXCEPTION("Cannot import " + package, "Unable to access main module", SPELL_ERROR_PYTHON_API );
    }
    PyObject* main_dict = PyModule_GetDict(mainModule);
    if (main_dict== NULL)
    {
        THROW_EXCEPTION("Cannot import " + package, "Unable to access main module dictionary", SPELL_ERROR_PYTHON_API );
    }
    PyDict_Merge(main_dict, moduleDict, 0);
    DEBUG("[PYH] Imported all from module " + package);
}

//============================================================================
// METROD    : SPELLpythonHelper::importUserLibraries
//============================================================================
void SPELLpythonHelper::importUserLibraries( const std::string& libraryPath )
{
    if (libraryPath != "" && libraryPath != "None" )
    {
        if (SPELLutils::isDirectory(libraryPath))
        {
            // Import the main module first
            PyObject* mainModule = PyImport_AddModule("__main__");
            if (mainModule == NULL)
            {
                THROW_EXCEPTION("Cannot import user libraries", "Unable to access main module", SPELL_ERROR_PYTHON_API );
            }
            PyObject* main_dict = PyModule_GetDict(mainModule);
            if (main_dict== NULL)
            {
                THROW_EXCEPTION("Cannot import libraries", "Unable to access main module dictionary", SPELL_ERROR_PYTHON_API );
            }

            // If the import of user libraries has been done, do nothing
            SPELLpyHandle flag = STRPY("__USERLIB__");
            if (PyDict_Contains( main_dict, flag.get()) )
            {
                LOG_WARN("[PYH] No need to re-import user libraries");
                return;
            }

            LOG_INFO("Loading user libraries in " + libraryPath);

            SPELLpythonHelper::instance().addToPath(libraryPath);

            std::list<std::string> files = SPELLutils::getFilesInDir(libraryPath);
            std::list<std::string>::iterator it;
            std::list<std::string>::iterator end = files.end();
            for( it = files.begin(); it != end; it++)
            {
                std::string filename = (*it);
                try
                {
                    if (filename == "__init__.py" ) continue;
                    std::size_t idx = filename.find(".py");
                    if ( idx == filename.length()-3 )
                    {
                        std::string module = filename.substr(0,idx);
                        importUserLibrary( main_dict, module );
                        SPELLpythonHelper::instance().checkError();
                    }
                }
                catch(SPELLcoreException& ex)
                {
                    LOG_ERROR("Failed to import user library " + filename + ": " + ex.what());
                    PyErr_Clear();
                }
            }


            // Now ensure that all libraries contain all imported names as well
            for( it = files.begin(); it != end; it++)
            {
                std::string filename = (*it);
                try
                {
                    if (filename == "__init__.py" ) continue;
                    std::size_t idx = filename.find(".py");
                    if ( idx == filename.length()-3 )
                    {
                        std::string module = filename.substr(0,idx);
                        updateUserLibrary( main_dict, module );
                        SPELLpythonHelper::instance().checkError();
                    }
                }
                catch(SPELLcoreException& ex)
                {
                    LOG_ERROR("Failed to update user library " + filename + ": " + ex.what());
                    PyErr_Clear();
                }
            }

            // Mark the import as done
            PyDict_SetItemString( main_dict, "__USERLIB__", STRPY("__USERLIB__"));
        }
        else
        {
            THROW_EXCEPTION("Unable to load USER libraries", "Library path '" + libraryPath + "' is not a directory", SPELL_ERROR_FILESYSTEM);
        }
    }
}

//============================================================================
// METROD    : SPELLpythonHelper::getUserLibraryDict
//============================================================================
PyObject* SPELLpythonHelper::getUserLibraryDict( const std::string& libraryFile )
{
    // Import the module now
    PyObject* dict = PyDict_New();
    PyObject* module = PyImport_ImportModuleEx( const_cast<char*>(libraryFile.c_str()), dict, dict, NULL);
    if ( module == NULL )
    {
        SPELLcoreException* ex = SPELLerror::instance().errorToException();
        throw *ex;
    }

    // Get the module dictionary
    PyObject* moduleDict = PyModule_GetDict( module );

    std::vector<std::string> tokens = SPELLutils::tokenize( libraryFile, "." );

    std::vector<std::string>::iterator it;
    for( it = tokens.begin(); it != tokens.end(); it++ )
    {
        // Skip the first token
        if (it == tokens.begin()) continue;
        // Get the submodule
        module = PyDict_GetItemString( moduleDict, (*it).c_str() );
        moduleDict = PyModule_GetDict( module );
    }

    return moduleDict;
}

//============================================================================
// METROD    : SPELLpythonHelper::updateUserLibrary
//============================================================================
void SPELLpythonHelper::updateUserLibrary( PyObject* main_dict, const std::string& libraryFile )
{
    LOG_INFO("   - update user library " + libraryFile);

    PyObject* moduleDict = getUserLibraryDict( libraryFile );

    // Copy also from main to module dict
    PyDict_Merge(moduleDict, main_dict, 0);
    //DEBUG("     [PYH] Update done");
}

//============================================================================
// METROD    : SPELLpythonHelper::importUserLibrary
//============================================================================
void SPELLpythonHelper::importUserLibrary( PyObject* main_dict, const std::string& libraryFile )
{
    LOG_INFO("   - import all from user library " + libraryFile);

    PyObject* moduleDict = getUserLibraryDict( libraryFile );

    // Iterate over the module dictionary keys and copy them into main
    SPELLpyHandle keyList = PyDict_Keys( moduleDict );
    int keyCount = PyList_Size( keyList.get() );
    int count = 0;
    for( count = 0; count < keyCount; count++ )
    {
        PyObject* key = PyList_GetItem( keyList.get(), count );
        if (!PyDict_Contains( main_dict, key ))
        {
        	PyObject* value = PyDict_GetItem( moduleDict, key );
        	PyDict_SetItem( main_dict, key, value );
            LOG_INFO("       . imported " + PYSSTR(key));
        }
    }
    DEBUG("     [PYH] Import done");
}

//============================================================================
// METROD    : SPELLpythonHelper::getMainDict()
//============================================================================
PyObject* SPELLpythonHelper::getMainDict()
{
    PyObject* mainModule = PyImport_AddModule("__main__");
    if (mainModule == NULL)
    {
        THROW_EXCEPTION("Cannot access main dictionary", "Unable to access main module", SPELL_ERROR_PYTHON_API );
    }
    PyObject* main_dict = PyModule_GetDict(mainModule);
    return main_dict;
}

//============================================================================
// METROD    : SPELLpythonHelper::install
//============================================================================
void SPELLpythonHelper::install( PyObject* object, const std::string& name )
{
    DEBUG("[PYH] Installing object in globals: " + name);
    install(object,name,"__main__");
}

//============================================================================
// METROD    : SPELLpythonHelper::install
//============================================================================
void SPELLpythonHelper::install( PyObject* object, const std::string& name, const std::string& module )
{
    PyObject* moduleObj = PyImport_AddModule( module.c_str() );
    if (moduleObj == NULL)
    {
        THROW_EXCEPTION("Cannot install " + name, "Unable to access module " + module, SPELL_ERROR_PYTHON_API );
    }
    PyObject* dict = PyModule_GetDict(moduleObj);
    if (dict== NULL)
    {
        THROW_EXCEPTION("Cannot install " + name, "Unable to access module dictionary", SPELL_ERROR_PYTHON_API );
    }
    if( PyDict_SetItemString( dict, name.c_str() , object) != 0)
    {
        THROW_EXCEPTION("Cannot install " + name, "Unable to set object instance on dictionary", SPELL_ERROR_PYTHON_API );
    }
    Py_INCREF(object);
}

//=============================================================================
// METHOD    : SPELLpythonHelper::callMethod
//=============================================================================
PyObject* SPELLpythonHelper::callMethod( PyObject* obj, const std::string& method, ... )
{
    DEBUG("[PYH] Call method " + method + " on " + PYREPR(obj));

    SPELLsafePythonOperations ops("SPELLpythonHelper::callMethod " + method);


    // Returns new reference
    SPELLpyHandle methodC = PyObject_GetAttrString(obj, method.c_str() );
    if (methodC.get() == NULL)
    {
        // Must clear the error from Python side, we are threating it already here
        // with the core exception
        THROW_EXCEPTION("Unable to call method " + method, "No such method", SPELL_ERROR_PYTHON_API );
    }

    va_list vargs;
    va_start(vargs, method);
    SPELLpyHandle args = objargs_mktuple(vargs);
    va_end(vargs);

    if (args.get() == NULL)
    {
    	THROW_EXCEPTION("Unable to call method " + method, "Error when extracting arguments", SPELL_ERROR_PYTHON_API );
    }

    PyObject* result = PyObject_Call( methodC.get(), args.get(), NULL);
    return result;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::callFunction
//=============================================================================
PyObject* SPELLpythonHelper::callFunction( const std::string& module, const std::string& function, ... )
{
    DEBUG("[PYH] Begin call function " + module + "." + function);

    // Borrowed
    PyObject* functionC = getObject( module, function );
    if (functionC == NULL)
    {
        THROW_EXCEPTION("Unable to call function " + function, "No such function", SPELL_ERROR_PYTHON_API );
    }
    va_list vargs;
    va_start(vargs, function);
    SPELLpyHandle args = objargs_mktuple(vargs);
    va_end(vargs);
    if (args.get() == NULL)
    {
        THROW_EXCEPTION("Unable to call method " + function, "Error when extracting arguments", SPELL_ERROR_PYTHON_API );
    }
    PyObject* result = PyObject_Call( functionC, args.get(), NULL);
    return result;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::callSpellFunction
//=============================================================================
PyObject* SPELLpythonHelper::callSpellFunction( const std::string& function, PyObject* args, PyObject* kargs )
{
    DEBUG("[PYH] Begin call SPELL function " + function);

    PyObject* functionC = PyDict_GetItemString( getMainDict(), function.c_str());
    PyObject* result = NULL;
    if (functionC == NULL)
    {
        THROW_EXCEPTION("Unable to call language function " + function, "No such function", SPELL_ERROR_PYTHON_API );
    }
    result = PyObject_Call( functionC, args, kargs );
    DEBUG("[PYH] End call SPELL function " + function);
    return result;

}

//=============================================================================
// METHOD    : SPELLpythonHelper::eval
//=============================================================================
PyObject* SPELLpythonHelper::eval( const std::string& expression, bool file )
{
    DEBUG("[PYH] Evaluate '" + expression + "'");
    SPELLsafePythonOperations ops("SPELLpythonHelper::eval( " + expression + ")");
    PyObject* result = PyRun_String( expression.c_str(), file ? Py_file_input : Py_eval_input, getMainDict(), NULL );
    checkError();
    DEBUG("[PYH] Evaluate done");
    return result;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::getObject
//=============================================================================
PyObject* SPELLpythonHelper::getObject( const std::string& module, const std::string& object )
{
    DEBUG("[PYH] Get object " + module + "." + object);
    PyObject* obj = NULL;
    PyObject* moduleObj = getModule(module);
    if (moduleObj == NULL)
    {
        THROW_EXCEPTION("Unable to get object " + object, "Unable to access module " + module, SPELL_ERROR_PYTHON_API );
    }
    PyObject* dict = PyModule_GetDict(moduleObj);
    if (dict== NULL)
    {
        THROW_EXCEPTION("Unable to get object " + object, "Unable to access module dictionary", SPELL_ERROR_PYTHON_API );
    }
    obj = PyDict_GetItemString(dict, object.c_str());
    if (obj == NULL)
    {
        LOG_ERROR("[PYH] Module dictionary: " + PYCREPR(dict));
        THROW_EXCEPTION("Unable to get object " + object, "Unable to get object from module dictionary", SPELL_ERROR_PYTHON_API );
    }
    DEBUG("[PYH] Get object done");
    return obj;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::getModule
//=============================================================================
PyObject* SPELLpythonHelper::getModule( const std::string& module )
{
    DEBUG("[PYH] Get module " + module);
    PyObject* moduleObj = NULL;
    moduleObj = PyImport_ImportModule( module.c_str() );
    if (moduleObj == NULL || moduleObj == Py_None )
    {
        THROW_EXCEPTION("Unable to get module " + module, "Import failed", SPELL_ERROR_PYTHON_API );
    }
    DEBUG("[PYH] Get module done");
    return moduleObj;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::initialize
//=============================================================================
void SPELLpythonHelper::initialize()
{
    m_initialized = false;

    LOG_INFO("[PYH] Initializing Python");
    // Initialize python, builtins, main module, etc.
    Py_Initialize();
    PyEval_InitThreads();

    if (!PyEval_ThreadsInitialized())
    {
        THROW_EXCEPTION("Unable to initialize", "Cannot initialize threads", SPELL_ERROR_PYTHON_API);
    }

    char* home = getenv( "SPELL_HOME" );
    if (home == NULL)
    {
        THROW_EXCEPTION("Unable to initialize", "SPELL_HOME variable not defined", SPELL_ERROR_ENVIRONMENT);
    }
    std::string homestr = home;

    LOG_INFO("[PYH] Initializing Python path");

    addToPath(".");
    addToPath( homestr );
    addToPath( homestr + PATH_SEPARATOR + "lib" );
    addToPath( homestr + PATH_SEPARATOR + "spell" );
    addToPath( homestr + PATH_SEPARATOR + "drivers" );
    char *pythonpath = getenv("PYTHONPATH");
    if (pythonpath != NULL)
    {
        addToPath(pythonpath);
    }

    // Initialize the system arguments to empty list (no script is executed)
    char** argv = new char*[1];
    argv[0] = (char*) "";
    PySys_SetArgv(1,argv);

    m_initialized = true;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::loadFramework()
//=============================================================================
void SPELLpythonHelper::loadFramework()
{
    importAllFrom("spell.lang.functions");
    importAllFrom("spell.lang.constants");
    importAllFrom("spell.lang.modifiers");
    importAllFrom("spell.lang.user");
    importAllFrom("spell.lib.adapter.utctime");
    importAllFrom("spell.lib.adapter.file");
    importAllFrom("math");
}

//=============================================================================
// METHOD    : SPELLpythonHelper::finalize
//=============================================================================
void SPELLpythonHelper::finalize()
{
    LOG_INFO("[PYH] Cleaning up Python");
    Py_Finalize();
    m_initialized = false;
    LOG_INFO("[PYH] Python finalized");
}

//=============================================================================
// METHOD    : SPELLpythonHelper::addToPath
//=============================================================================
void SPELLpythonHelper::addToPath( const std::string& path )
{
    LOG_INFO("[PYH] Append to python path: " + path);

    // Retrieve first the current path
    std::string libs = "";
    PyObject* sys = PySys_GetObject( (char*) "path" );
    int size = PyList_Size(sys);
    for(int idx=0; idx<size; idx++)
    {
    	std::string path_element = PYSTR(PyList_GetItem(sys,idx));
    	if (!libs.empty()) libs += ":";
    	libs += path_element;
    }
	if (!libs.empty()) libs += ":";
	libs += path;

    PySys_SetPath( const_cast<char*>(libs.c_str()) );
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isInstance()
//=============================================================================
bool SPELLpythonHelper::isInstance( PyObject* object, const std::string& className, const std::string& package )
{
    DEBUG("[PYH] Is instance " + className);
    PyObject* theClass = getObject(package, className);
    if (theClass == NULL)
    {
        THROW_EXCEPTION("Unable to evaluate instance match", "Cannot get class " + className + " in " + package, SPELL_ERROR_PYTHON_API );
    }
    return (PyObject_IsInstance(object,theClass));
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isSubclassInstance()
//=============================================================================
bool SPELLpythonHelper::isSubclassInstance( PyObject* object, const std::string& className, const std::string& package )
{
    DEBUG("[PYH] Is subclass instance " + className);
    PyObject* theClass = getObject(package, className);
    if (theClass == NULL)
    {
        THROW_EXCEPTION("Unable to evaluate instance match", "Cannot get class " + className + " in " + package, SPELL_ERROR_PYTHON_API );
    }
    PyObject* args = PyTuple_New(1);
    PyTuple_SetItem(args,0,theClass);
    Py_INCREF(theClass);
    PyObject* objClass = (PyObject*) object->ob_type->tp_base;
    return (PyObject_IsSubclass(objClass, args));
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isInstance()
//=============================================================================
bool SPELLpythonHelper::isClass( PyObject* object )
{
    // This is bl**dy awful!! but PyClass_Check does not want to work...
    std::string typeName = PYCREPR(object->ob_type);
    return (typeName == "<type 'type'>");
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isInstance()
//=============================================================================
bool SPELLpythonHelper::isInstance( PyObject* object )
{
    // This is bl**dy awful!! but PyClass_Check does not want to work...
    std::string typeName = PYCREPR(object->ob_type);
    return (typeName.find("<class '") == 0);
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isTime
//=============================================================================
bool SPELLpythonHelper::isTime( PyObject* instance )
{
    return isInstance( instance, "TIME", "spell.lib.adapter.utctime" );
}

//=============================================================================
// METHOD    : SPELLpythonHelper::isDatabase
//=============================================================================
bool SPELLpythonHelper::isDatabase( PyObject* instance )
{
    return isInstance( instance, "Database", "spell.lib.adapter.databases.database" );
}

//=============================================================================
// METHOD    : SPELLpythonHelper::evalTime
//=============================================================================
SPELLtime SPELLpythonHelper::evalTime( PyObject* expression )
{
    DEBUG("[PYH] Evaluate time from python object");
    if (isTime(expression))
    {
        SPELLpyHandle isRel = callMethod(expression,"isRel",NULL);
        if (isRel.get() == Py_True)
        {
            SPELLpyHandle pySecs = callMethod(expression,"rel",NULL);
            long secs = PyLong_AsLong(pySecs.get());
            DEBUG("[PYH] Evaluate time from python object done");
            return SPELLtime( secs, 0, true );
        }
        else
        {
            // New reference
            SPELLpyHandle pySecs = callMethod(expression,"abs",NULL);
            int seconds = 0;
            if (PyFloat_Check(pySecs.get()))
            {
                double fsecs = PyFloat_AsDouble(pySecs.get());
                // New reference
                SPELLpyHandle pyDoub = PyLong_FromDouble(fsecs);
                seconds = PyLong_AsLong(pyDoub.get());
            }
            else
            {
                seconds = PyLong_AsLong(pySecs.get());
            }
            DEBUG("[PYH] Evaluate time from python object done");
            return SPELLtime( seconds, 0, false );
        }
    }
    else
    {
        THROW_FATAL_EXCEPTION("Unable to evaluate time", "Not a TIME instance", SPELL_ERROR_PYTHON_API );
    }
    // Just to avoid warnings...
    return SPELLtime();
}

//=============================================================================
// METHOD    : SPELLpythonHelper::evalTime
//=============================================================================
SPELLtime SPELLpythonHelper::evalTime( const std::string& expression )
{
    DEBUG("[PYH] Evaluate time from string expression");
    PyObject* theClass = getObject("spell.lib.adapter.utctime", "TIME");
    SPELLpyHandle args = PyTuple_New(1);
    PyObject* pyStr = SSTRPY(expression);
    PyTuple_SetItem(args.get(),0,pyStr);

    PyObject* instance = PyObject_Call( theClass, args.get(), NULL );

    if (instance == NULL)
    {
        THROW_FATAL_EXCEPTION("Unable to evaluate time", "Input was: '" + expression + "'", SPELL_ERROR_LANGUAGE );
    }

    SPELLtime time = evalTime(instance);

    DEBUG("[PYH] Evaluate time from string expression done");
    return time;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::pythonTime
//=============================================================================
PyObject* SPELLpythonHelper::pythonTime( const std::string& expression )
{
    return eval("TIME('" + expression + "')",false);
}

//=============================================================================
// METHOD    : SPELLpythonHelper::pythonTime
//=============================================================================
PyObject* SPELLpythonHelper::pythonTime( const SPELLtime& time )
{
    DEBUG("[PYH] Convert time to python time");
    PyObject* theClass = getObject("spell.lib.adapter.utctime", "TIME");

    if (theClass == NULL)
    {
        THROW_FATAL_EXCEPTION("Unable to create TIME", "Cannot access class object", SPELL_ERROR_LANGUAGE );
    }

    SPELLpyHandle args = PyTuple_New(1);
    PyObject* pyStr = SSTRPY( time.toTIMEString() );
    PyTuple_SetItem(args.get(),0,pyStr);

    // New reference
    PyObject* instance = PyObject_CallObject( theClass, args.get() );

    if (instance == NULL)
    {
        THROW_FATAL_EXCEPTION("Unable to create TIME", "Input was: '" + time.toTIMEString() + "'", SPELL_ERROR_LANGUAGE );
    }

    DEBUG("[PYH] Convert time to python time done");
    return instance;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::beginSafeAllowThreads
//=============================================================================
PyThreadState* SPELLpythonHelper::beginSafeAllowThreads()
{
    PyThreadState *tstate = PyThreadState_GET();
    if (tstate != NULL)
    {
//        std::cerr << "BEGIN ALLOW THREADS ##########################################################" << std::endl;
        tstate = PyEval_SaveThread();
//        std::cerr << "FRAME: " << PSTR(tstate->frame) << std::endl;
    }
    return tstate;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::endSafeAllowThreads
//=============================================================================
void SPELLpythonHelper::endSafeAllowThreads( PyThreadState* tstate )
{
    if (tstate != NULL)
    {
//        std::cerr << "END ALLOW THREADS ############################################################" << std::endl;
//        std::cerr << "FRAME: " << PSTR(tstate->frame) << std::endl;
        PyEval_RestoreThread(tstate);
    }
}

//=============================================================================
// METHOD    : SPELLpythonHelper::acquireGIL();
//=============================================================================
PyGILState_STATE SPELLpythonHelper::acquireGIL()
{
//    std::cerr << "ACQUIRE GIL ##################################################################" << std::endl;
    PyGILState_STATE state = PyGILState_Ensure();
//    std::cerr << "FRAME: " << PSTR(PyEval_GetFrame()) << std::endl;
//    std::cerr << "STATE: " << state << std::endl;
    return state;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::releaseGIL();
//=============================================================================
void SPELLpythonHelper::releaseGIL( PyGILState_STATE state )
{
//    std::cerr << "RELEASE GIL ##################################################################" << std::endl;
//    std::cerr << "FRAME: " << PSTR(PyEval_GetFrame()) << std::endl;
//    std::cerr << "STATE: " << state << std::endl;
    PyGILState_Release(state);
}

//=============================================================================
// METHOD    : SPELLpythonHelper::readProcedureFile()
//=============================================================================
std::string SPELLpythonHelper::readProcedureFile( const std::string& filename )
{
    // Holds the source code
    std::string source = "";
    std::ifstream file;
    file.open( filename.c_str() );
    if (!file.is_open())
    {
        THROW_EXCEPTION("Cannot parse file " + filename, "Unable to open", SPELL_ERROR_FILESYSTEM );
    }
    // Open the file for read only
    try
    {
        while(!file.eof())
        {
            std::string line = "";
            std::getline(file,line);
            if (!file.eof() || !line.empty())
            {
               source += line + "\n";
            }
        }
        
        if (!source.empty())
        {
           source +="exit";
        }
    }
    catch(...)
    {
        file.close();
        throw;
    }
    file.close();

    SPELLutils::replace( source, "\r\n", "\n" );

    return source;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::compile();
//=============================================================================
PyCodeObject* SPELLpythonHelper::compile( const std::string& filePath )
{
    DEBUG("[PYH] Compiling procedure " + filePath)

    // Will hold the AST preprocessed bytecode
    mod_ty ast;
    // Will hold the resulting code object
    PyCodeObject* code = NULL;
    // Compiler flags set to default
    PyCompilerFlags flags;
    flags.cf_flags = PyCF_SOURCE_IS_UTF8;
    // The arena is required for compilation process
    PyArena *arena = NULL;

    try
    {
        // The arena is required for compilation process
        arena = PyArena_New();
        if(arena == NULL)
        {
            THROW_EXCEPTION("Unable to compile script", "Could not create arena", SPELL_ERROR_PYTHON_API );
        }

        // Read the source code
        std::string source = readProcedureFile( filePath );
        if (source == "")
        {
            THROW_EXCEPTION("Unable to compile script", "Cannot read source code", SPELL_ERROR_PYTHON_API );
        }

        // Compile the script to obtain the AST code
        ast = PyParser_ASTFromString( source.c_str(), filePath.c_str(), Py_file_input, &flags, arena);
        if (ast == NULL) // Could not get ast
        {
            SPELLcoreException* exception = SPELLerror::instance().errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                THROW_EXCEPTION("Unable to compile script", "Could not get AST code", SPELL_ERROR_PYTHON_API );
            }
        }

        // Construct the code object from AST
        code = PyAST_Compile(ast, filePath.c_str(), &flags, arena);
        if (code == NULL)
        {
            SPELLcoreException* exception = SPELLerror::instance().errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                THROW_EXCEPTION("Unable to compile script", "Could not compile the code", SPELL_ERROR_PYTHON_API );
            }
        }
    }
    catch(SPELLcoreException& ex)
    {
        PyArena_Free(arena);
        code = NULL;
        throw ex;
    }
    // Cleanup after compilation
    PyArena_Free(arena);

    DEBUG("[PYH] Compilation success");
    return code;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::compileScript();
//=============================================================================
PyCodeObject* SPELLpythonHelper::compileScript( const std::string& source )
{
    DEBUG("[PYH] Compiling source code script");

    // Will hold the code object
    PyCodeObject* code = NULL;
    // Will hold the AST preprocessed bytecode
    mod_ty ast;
    // Compiler flags set to default
    PyCompilerFlags flags;
    flags.cf_flags = 0;
    // The arena is required for compilation process
    PyArena *arena = NULL;

    try
    {
        // The arena is required for compilation process
        arena = PyArena_New();
        if(arena == NULL)
        {
            THROW_EXCEPTION("Unable to compile script", "Could not create arena", SPELL_ERROR_PYTHON_API );
        }

        // Compile the script to obtain the AST code
        ast = PyParser_ASTFromString( source.c_str(), "<string>", Py_file_input, &flags, arena);
        if (ast == NULL) // Could not get ast
        {
            SPELLcoreException* exception = SPELLerror::instance().errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                THROW_EXCEPTION("Unable to compile script", "Could not get AST code", SPELL_ERROR_PYTHON_API );
            }
        }

        // Construct the code object from AST
        code = PyAST_Compile(ast, "<string>", &flags, arena);
        if (code == NULL)
        {
            SPELLcoreException* exception = SPELLerror::instance().errorToException();
            if (exception != NULL)
            {
                exception->setError( "Unable to compile script: " + exception->getError() );
                throw *exception;
            }
            else
            {
                THROW_EXCEPTION("Unable to compile script", "Could not compile the code", SPELL_ERROR_PYTHON_API );
            }
        }
    }
    catch(SPELLcoreException& ex)
    {
        PyArena_Free(arena);
        code = NULL;
        throw ex;
    }
    // Cleanup after compilation
    PyArena_Free(arena);
    DEBUG("[PYH] Compilation success");
    return code;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::setNewLine();
//=============================================================================
bool SPELLpythonHelper::setNewLine( PyFrameObject* frame, const int& new_lineno, const int& new_lasti )
{
    LOG_WARN("[PYH] Changing line number on frame " + PYCREPR(frame));

    /////////////////////////////////////////////////////////
    // THIS IS NASTY: copied from Python 2.5 implementation
    /////////////////////////////////////////////////////////

    int new_iblock = 0;            /* The new value of f_iblock */

    char* code = NULL;             /* The bytecode for the frame... */
    Py_ssize_t code_len = 0;       /* ...and its length */
    int min_addr = 0;              /* Scanning the SETUPs and POPs */
    int max_addr = 0;              /* (ditto) */

    int delta_iblock = 0;          /* (ditto) */
    int min_delta_iblock = 0;      /* (ditto) */
    int min_iblock = 0;            /* (ditto) */

    int f_lasti_setup_addr = 0;    /* Policing no-jump-into-finally */
    int new_lasti_setup_addr = 0;  /* (ditto) */
    int blockstack[CO_MAXBLOCKS];  /* Walking the 'finally' blocks */
    int in_finally[CO_MAXBLOCKS];  /* (ditto) */
    int blockstack_top = 0;        /* (ditto) */
    int setup_op = 0;              /* (ditto) */

    // Fail if the line comes before the start of the code block.
    if (new_lineno < frame->f_code->co_firstlineno)
    {
    	THROW_EXCEPTION("Cannot set new line", "Line comes before start of code block", SPELL_ERROR_PYTHON_API);
    }

    /* We're now ready to look at the bytecode. */
    PyString_AsStringAndSize(frame->f_code->co_code, &code, &code_len);
    min_addr = MIN(new_lasti, frame->f_lasti);
    max_addr = MAX(new_lasti, frame->f_lasti);

    /* You can't jump onto a line with an 'except' statement on it -
     * they expect to have an exception on the top of the stack, which
     * won't be true if you jump to them.  They always start with code
     * that either pops the exception using POP_TOP (plain 'except:'
     * lines do this) or duplicates the exception on the stack using
     * DUP_TOP (if there's an exception type specified).  See compile.c,
     * 'com_try_except' for the full details.  There aren't any other
     * cases (AFAIK) where a line's code can start with DUP_TOP or
     * POP_TOP, but if any ever appear, they'll be subject to the same
     * restriction (but with a different error message). */
    if (code[new_lasti] == DUP_TOP || code[new_lasti] == POP_TOP)
    {
    	THROW_EXCEPTION("Cannot set new line", "Cannot go inside a try/except block", SPELL_ERROR_PYTHON_API);
    }

    /* You can't jump into or out of a 'finally' block because the 'try'
     * block leaves something on the stack for the END_FINALLY to clean
     * up.  So we walk the bytecode, maintaining a simulated blockstack.
     * When we reach the old or new address and it's in a 'finally' block
     * we note the address of the corresponding SETUP_FINALLY.  The jump
     * is only legal if neither address is in a 'finally' block or
     * they're both in the same one.  'blockstack' is a stack of the
     * bytecode addresses of the SETUP_X opcodes, and 'in_finally' tracks
     * whether we're in a 'finally' block at each blockstack level. */
    f_lasti_setup_addr = -1;
    new_lasti_setup_addr = -1;
    memset(blockstack, '\0', sizeof(blockstack));
    memset(in_finally, '\0', sizeof(in_finally));
    blockstack_top = 0;
    int addr = 0;
    for (addr = 0; addr < code_len; addr++)
    {
        unsigned char op = code[addr];
        switch (op)
        {
        case SETUP_LOOP:
        case SETUP_EXCEPT:
        case SETUP_FINALLY:
            blockstack[blockstack_top++] = addr;
            in_finally[blockstack_top-1] = 0;
            break;
        case POP_BLOCK:
            if(blockstack_top > 0)
            {
				setup_op = code[blockstack[blockstack_top-1]];
				if (setup_op == SETUP_FINALLY)
				{
					in_finally[blockstack_top-1] = 1;
				}
				else {
					blockstack_top--;
				}
            }
            break;
        case END_FINALLY:
            /* Ignore END_FINALLYs for SETUP_EXCEPTs - they exist
             * in the bytecode but don't correspond to an actual
             * 'finally' block.  (If blockstack_top is 0, we must
             * be seeing such an END_FINALLY.) */
            if (blockstack_top > 0)
            {
                setup_op = code[blockstack[blockstack_top-1]];
                if (setup_op == SETUP_FINALLY)
                {
                    blockstack_top--;
                }
            }
            break;
        }

        /* For the addresses we're interested in, see whether they're
         * within a 'finally' block and if so, remember the address
         * of the SETUP_FINALLY. */
        if (addr == new_lasti || addr == frame->f_lasti)
        {
            int i = 0;
            int setup_addr = -1;
            for (i = blockstack_top-1; i >= 0; i--)
            {
                if (in_finally[i])
                {
                    setup_addr = blockstack[i];
                    break;
                }
            }
            if (setup_addr != -1)
            {
                if (addr == new_lasti)
                {
                    new_lasti_setup_addr = setup_addr;
                }
                if (addr == frame->f_lasti)
                {
                    f_lasti_setup_addr = setup_addr;
                }
            }
        }
        if (op >= HAVE_ARGUMENT)
        {
            addr += 2;
        }
    }
    /* Verify that the blockstack tracking code didn't get lost. */
    assert(blockstack_top == 0);

    /* After all that, are we jumping into / out of a 'finally' block? */
    if (new_lasti_setup_addr != f_lasti_setup_addr)
    {
    	THROW_EXCEPTION("Cannot set new line", "Cannot jump outside a try/except block", SPELL_ERROR_PYTHON_API);
    }

    /* Police block-jumping (you can't jump into the middle of a block)
     * and ensure that the blockstack finishes up in a sensible state (by
     * popping any blocks we're jumping out of).  We look at all the
     * blockstack operations between the current position and the new
     * one, and keep track of how many blocks we drop out of on the way.
     * By also keeping track of the lowest blockstack position we see, we
     * can tell whether the jump goes into any blocks without coming out
     * again - in that case we raise an exception below. */
    delta_iblock = 0;
    for (addr = min_addr; addr < max_addr; addr++)
    {
        unsigned char op = code[addr];
        switch (op)
        {
        case SETUP_LOOP:
        case SETUP_EXCEPT:
        case SETUP_FINALLY:
            delta_iblock++;
            break;
        case POP_BLOCK:
            delta_iblock--;
            break;
        }

        min_delta_iblock = MIN(min_delta_iblock, delta_iblock);
        if (op >= HAVE_ARGUMENT)
        {
            addr += 2;
        }
    }

    /* Derive the absolute iblock values from the deltas. */
    min_iblock = frame->f_iblock + min_delta_iblock;
    if (new_lasti > frame->f_lasti)
    {
        /* Forwards jump. */
        new_iblock = frame->f_iblock + delta_iblock;
    }
    else
    {
        /* Backwards jump. */
        new_iblock = frame->f_iblock - delta_iblock;
    }

    /* Are we jumping into a block? */
    if (new_iblock > min_iblock)
    {
    	THROW_EXCEPTION("Cannot set new line", "Cannot jump inside a block", SPELL_ERROR_PYTHON_API);
    }

    /* Pop any blocks that we're jumping out of. */
    while (frame->f_iblock > new_iblock)
    {
        PyTryBlock *b = PyFrame_BlockPop(frame);
        while ((frame->f_stacktop - frame->f_valuestack) > b->b_level)
        {
            PyObject *v = (*--frame->f_stacktop);
            Py_DECREF(v);
        }
    }
    /* Finally set the new f_lineno and f_lasti and return OK. */
    frame->f_lineno = new_lineno;
    frame->f_lasti = new_lasti;
    LOG_WARN("[PYH] Set frame lineno " + ISTR(new_lineno) + ":" + ISTR(new_lasti));
    return true;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::createFrame();
//=============================================================================
PyFrameObject* SPELLpythonHelper::createFrame( const std::string& filename, PyCodeObject* code )
{
    DEBUG("[PYH] Creating frame for code " + PYCREPR(code));

    PyFrameObject* frame = NULL;

    PyObject* mainModule = PyImport_AddModule("__main__");

    if (mainModule == NULL)
    {
        THROW_EXCEPTION("Unable to create frame", "Cannot access main module", SPELL_ERROR_PYTHON_API );
    }

    // Do it only the first time
    PyObject* mainDict = PyModule_GetDict(mainModule);

    if (mainDict== NULL)
    {
        THROW_EXCEPTION("Unable to create frame", "Cannot access main dictionary", SPELL_ERROR_PYTHON_API );
    }

    // Set the file name if necessary
    if (PyDict_GetItemString(mainDict, "__file__") == NULL)
    {
        SPELLpyHandle f = SSTRPY(filename);
        if (f.get() == NULL)
        {
            return NULL;
        }
        if (PyDict_SetItemString(mainDict, "__file__", f.get()) < 0)
        {
            return NULL;
        }
    }
    DEBUG("[PYH] Frame creation");
    frame = PyFrame_New( PyThreadState_Get(), code, mainDict, mainDict );
    Py_INCREF(frame);
    return frame;
}

//=============================================================================
// METHOD    : SPELLpythonHelper::checkError
//=============================================================================
void SPELLpythonHelper::checkError()
{
    if (!m_initialized) return;
    SPELLsafePythonOperations ops("SPELLpythonHelper::checkError()");
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
            THROW_EXCEPTION("Driver error: " + message, reason, SPELL_DRIVER_ERROR );
        }
        else if ( PYREPR(pvalue) == "EXECUTION ABORTED" || PYREPR(pvalue) == "EXECUTION TERMINATED" )
        {
            // Do not raise exceptions if the detected exception is an aborted or finished internal exception
            return;
        }
        else
        {
            std::cerr << "RAISING PYTHON ERROR EXCEPTION" << std::endl;
            THROW_EXCEPTION("Python error", PYSSTR(pvalue), SPELL_ERROR_PYTHON_API );
        }
    }
}

//=============================================================================
// METHOD    : SPELLpythonHelper::newInstance
//=============================================================================
PyObject* SPELLpythonHelper::newInstance( PyObject* classObj, PyObject* argsTuple, PyObject* kwdsDict )
{
    DEBUG("[PYH] New instance begin " + PYREPR(classObj));
    PyObject* result;
    if (argsTuple == NULL)
    {
        SPELLpyHandle args = PyTuple_New(0);
        result = PyEval_CallObjectWithKeywords(classObj, args.get(), kwdsDict);
    }
    else
    {
    	Py_XINCREF(argsTuple);
        SPELLpyHandle args = argsTuple;
        result = PyEval_CallObjectWithKeywords(classObj, args.get(), kwdsDict);
    }
    // New reference returned
    Py_XINCREF(result);
    DEBUG("[PYH] New instance end " + PYREPR(result));
    return result;
}
