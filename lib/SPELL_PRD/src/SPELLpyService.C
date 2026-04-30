// ################################################################################
// FILE       : SPELLservice.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of spell service model
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
// Local includes ----------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_WRP/SPELLpyHandle.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"

// System includes ---------------------------------------------------------
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <stdio.h>
#include "SPELL_PRD/SPELLpyService.H"
#include "SPELL_PRD/SPELLserviceWatchdogPython.H"

//=============================================================================
// CONSTRUCTOR : SPELLpyService::SPELLpyService()
//=============================================================================
SPELLpyService::SPELLpyService(PyObject* serviceImpl) :
m_source("")
{
    SPELLsafePythonOperations ops("SPELLpyService::SPELLpyService");
    SPELLpyHandle pyServiceName = SPELLpythonHelper::instance().callMethod(
            serviceImpl, "getName", NULL);
    m_name = PyString_AsString(pyServiceName.get());
    SPELLpyHandle basePath = SPELLpythonHelper::instance().callMethod(serviceImpl, "getBasePath", NULL);
    m_basePath = PyString_AsString(basePath.get());
    basePath = SPELLpythonHelper::instance().callMethod(serviceImpl, "getMainFile", NULL);
    m_mainServiceFile = PyString_AsString(basePath.get());

    m_service = serviceImpl;
    Py_IncRef(serviceImpl);
    std::cout << "Found Spell Service " << m_name << std::endl;
    m_source = SPELLprocedureSourceCode(this->getMainServiceFile());
}

//=============================================================================
// DESTRUCTOR : SPELLpyService::~SPELLpyService
//=============================================================================
SPELLpyService::~SPELLpyService()
{
    Py_DecRef(m_service);
}

bool SPELLpyService::useCommandOverride() const {
    SPELLsafePythonOperations ops("useCommandOverride");
    SPELLpyHandle userOverride = SPELLpythonHelper::instance().callMethod(m_service,"useCommandOverride",NULL);

    SPELLpythonHelper::instance().checkError();

    return userOverride.get() == Py_True;
}

std::string SPELLpyService::getCommandOverride(int startTimeout, int loginTimeout, std::string config, std::string contextName, int ipcPort, std::string instanceId,
        std::string warmstartFile) const {
    SPELLsafePythonOperations ops("getCommandOverride");

    SPELLpyHandle newCommand = PyObject_CallMethod(m_service,(char*)"getCommandOverride", (char*)"(iississ)", startTimeout, loginTimeout, config.c_str(), contextName.c_str(), ipcPort, instanceId.c_str(), warmstartFile.c_str(),NULL);
    SPELLpythonHelper::instance().checkError();
    return PyString_AsString(newCommand.get());
}

void SPELLpyService::startWatchDog(void (*callback)(std::string&))
{
    SPELLsafePythonOperations ops("startWatchDog");
    PyObject* callbackObj = SpellServiceWatchdog_Create(callback);
    SPELLpyHandle newCommand = PyObject_CallMethod(m_service,(char*)"_startWatchDog", (char*)"(O)", callbackObj,NULL);
    SPELLpythonHelper::instance().checkError();
}

void SPELLpyService::start()
{
    SPELLsafePythonOperations ops("start");

    SPELLpyHandle newCommand = PyObject_CallMethod(m_service,(char*)"_start", (char*)"()",NULL);
    SPELLpythonHelper::instance().checkError();
}

PyObject* SPELLpyService::getStartFunction() const
{
    SPELLsafePythonOperations ops("_getStartCode");

    PyObject* funcCode = PyObject_CallMethod(m_service,(char*)"_getStartCode", (char*)"()",NULL);
    SPELLpythonHelper::instance().checkError();

    return PyMethod_Function(funcCode);
}
