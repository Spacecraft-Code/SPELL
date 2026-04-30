// ################################################################################
// FILE       : SPELLchildManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the child procedure manager
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
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_CIFS/SPELLserverCif.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"



//=============================================================================
// CONSTRUCTOR    : SPELLchildManager::SPELLchildManager
//=============================================================================
SPELLchildManager::SPELLchildManager()
{
    reset();
    DEBUG("[C] SPELLchildManager created");

}

//=============================================================================
// DESTRUCTOR    : SPELLchildManager::~SPELLchildManager
//=============================================================================
SPELLchildManager::~SPELLchildManager()
{
    DEBUG("[C] SPELLchildManager destroyed");
}

//=============================================================================
// METHOD    : SPELLchildManager::reset
//=============================================================================
void SPELLchildManager::reset()
{
    m_childProc = "";
    m_childStatus = STATUS_UNINIT;
    m_childError = "";
    m_childErrorReason = "";
    m_childAlive = false;
}

//=============================================================================
// METHOD    : SPELLchildManager::notifyChildStatus
//=============================================================================
void SPELLchildManager::notifyChildStatus( const SPELLexecutorStatus st )
{
    SPELLmonitor m(m_lock);
    m_childStatus = st;
}

//=============================================================================
// METHOD    : SPELLchildManager::notifyChildError
//=============================================================================
void SPELLchildManager::notifyChildError( const std::string& error, const std::string& reason )
{
    SPELLmonitor m(m_lock);
    m_childStatus = STATUS_ERROR;
    m_childError = error;
    m_childErrorReason = reason;
}

//=============================================================================
// METHOD    : SPELLchildManager::notifyChildClosed
//=============================================================================
void SPELLchildManager::notifyChildClosed()
{
    SPELLmonitor m(m_lock);
    if (m_childAlive)
    {
        m_childStatus = STATUS_ERROR;
        m_childError = "Subprocedure lost";
        m_childErrorReason = "Closed by somebody else";
    }
    m_childAlive = false;
}

//=============================================================================
// METHOD    : SPELLchildManager::notifyChildKilled
//=============================================================================
void SPELLchildManager::notifyChildKilled()
{
    SPELLmonitor m(m_lock);
    if (m_childAlive)
    {
        m_childStatus = STATUS_ERROR;
        m_childError = "Subprocedure lost";
        m_childErrorReason = "Killed by somebody else";
    }
    m_childAlive = false;
}

//=============================================================================
// METHOD    : SPELLchildManager::openChildProcedure
//=============================================================================
void SPELLchildManager::openChildProcedure( const std::string& procId,
        const std::string& args,
        const bool automatic,
        const bool blocking,
        const bool visible )
{
    SPELLmonitor m(m_lock);

    m_childStart.clear();

    SPELLprocedureManager::ProcList procList = SPELLprocedureManager::instance().getProcList();

    // We assume that the list is ordered by priorities already
    SPELLprocedureManager::ProcList::iterator it;
    SPELLprocedureManager::ProcList::iterator end = procList.end();
    std::string suitableProc = "";

    for( it = procList.begin(); it != end; it++)
    {
        std::string id_name = (*it);
        std::vector<std::string> tokens = SPELLutils::tokenize(id_name, "|");
        std::vector<std::string> path_tokens = SPELLutils::tokenize(tokens[0], "/");
        std::string foundId = path_tokens[ path_tokens.size()-1 ];
        if ((procId == foundId)||(procId == tokens[0]))
        {
            suitableProc = tokens[0];
            break;
        }
    }
    if (suitableProc == "")
    {
        THROW_EXCEPTION("Unable to launch subprocedure " + procId, "No suitable identifier found", SPELL_ERROR_EXECUTION);
    }
    LOG_INFO("[CM] Starting subprocedure " + suitableProc);

    int callingLine = SPELLexecutor::instance().getCallstack().getCurrentLine();

    SPELLserverCif& cif = dynamic_cast<SPELLserverCif&>(SPELLexecutor::instance().getCIF());
    std::string subProcInstance = cif.openSubprocedure( suitableProc, callingLine, args, automatic, blocking, visible );

    if (subProcInstance != "")
    {
        LOG_INFO("[CM] Instance id is " + subProcInstance);
        m_childProc = subProcInstance;
        m_childAlive = true;
        m_childStatus = STATUS_LOADED;
    }
    else
    {
        THROW_EXCEPTION("Unable to start subprocedure " + suitableProc, "Cannot get instance id", SPELL_ERROR_PYTHON_API);
        /** \todo error handling */
    }
    m_childStart.set();
}

//=============================================================================
// METHOD    : SPELLchildManager::closeChildProcedure
//=============================================================================
void SPELLchildManager::closeChildProcedure()
{
    SPELLmonitor m(m_lock);
    reset();
    SPELLserverCif& cif = dynamic_cast<SPELLserverCif&>(SPELLexecutor::instance().getCIF());
    cif.closeSubprocedure( m_childProc );
}

//=============================================================================
// METHOD    : SPELLchildManager::killChildProcedure
//=============================================================================
void SPELLchildManager::killChildProcedure()
{
    SPELLmonitor m(m_lock);
    reset();
    SPELLserverCif& cif = dynamic_cast<SPELLserverCif&>(SPELLexecutor::instance().getCIF());
    cif.killSubprocedure( m_childProc );
}

//=============================================================================
// METHOD    : SPELLchildManager::hasChild
//=============================================================================
const bool SPELLchildManager::hasChild()
{
    SPELLmonitor m(m_lock);
    return (m_childProc != "");
}

//=============================================================================
// METHOD    : SPELLchildManager::isAlive
//=============================================================================
const bool SPELLchildManager::isAlive()
{
    m_childStart.wait();
    return m_childAlive;
}

//=============================================================================
// METHOD    : SPELLchildManager::getChildStatus
//=============================================================================
const SPELLexecutorStatus SPELLchildManager::getChildStatus()
{
    m_childStart.wait();
    return m_childStatus;
}

//=============================================================================
// METHOD    : SPELLchildManager::getChildError
//=============================================================================
const std::string& SPELLchildManager::getChildError()
{
    m_childStart.wait();
    return m_childError;
};

//=============================================================================
// METHOD    : SPELLchildManager::getChildErrorReason
//=============================================================================
const std::string& SPELLchildManager::getChildErrorReason()
{
    m_childStart.wait();
    return m_childErrorReason;
};
