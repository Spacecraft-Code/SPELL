// ################################################################################
// FILE       : SPELLservice.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Service model
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

#ifndef __SPELL_PY_SERVICE_H__
#define __SPELL_PY_SERVICE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_PRD/SPELLprocedureSourceCode.H"
// Local includes ----------------------------------------------------------

// Project includes --------------------------------------------------------

/** \defgroup SPELL_PRD Service management (SPELL_PRD)
 *
 * This module provides mechanisms for reading procedure files and libraries,
 * getting their source code and properties, etc.
 *
 * */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Model of a spell service.
 **
 ** \par Description and usage:
 **
 ** 	Used by the procedure manager to keep track of all available
 **     services and their related information.
 **
 **   	The SPELLprocedureManager creates one model per service found in
 **   	the service search path.
 **
 **////////////////////////////////////////////////////////////////////////////
class SPELLpyService
{
public:
    //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////

    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor
     *
     * \param procPath IN: procedure path
     * \param file IN: file name.
     **////////////////////////////////////////////////////////////////////
    SPELLpyService(PyObject* serviceImpl);

    //////////////////////////////////////////////////////////////////////
    /** Destructor
     **////////////////////////////////////////////////////////////////////
    ~SPELLpyService();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    std::string getName() const
    {
        return m_name;
    }

    std::string getBasePath() const
    {
        return m_basePath;
    }
    std::string getMainServiceFile() const
    {
        return m_mainServiceFile;
    }

    void setBasePath(std::string basePath)
    {
        m_basePath = basePath;
    }

    bool useCommandOverride() const;

    std::string getCommandOverride(int startTimeout, int loginTimeout, std::string config, std::string contextName, int ipcPort, std::string instanceId,
            std::string warmstartFile) const;

    void startWatchDog(void (*callback)(std::string&));

    void start();

    PyObject* getStartFunction() const;
    // DATA MEMBERS //////////////////////////////////////////////////////////

    PyObject* getService() const{
        Py_INCREF(m_service);
        return m_service;
    }

    //////////////////////////////////////////////////////////////////////
    /** Obtain the procedure source code.
     *
     * \returns The procedure source code.
    **////////////////////////////////////////////////////////////////////
    SPELLprocedureSourceCode getSourceCode() const {
        return m_source;
    };
protected:
    //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    // DATA MEMBERS //////////////////////////////////////////////////////////

private:
    //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    PyObject* m_service;
    SPELLprocedureSourceCode m_source;
    std::string m_name;
    std::string m_basePath;
    std::string m_mainServiceFile;
};

/*@}*/
#endif
