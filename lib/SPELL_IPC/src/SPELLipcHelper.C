// ################################################################################
// FILE       : SPELLipcHelper.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: IPC utilities
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
#include "SPELL_IPC/SPELLipcHelper.H"
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// STATIC: SPELLipcHelper::createResponse
//=============================================================================
SPELLipcMessage SPELLipcHelper::createResponse( std::string id, const SPELLipcMessage& request )
{
    SPELLipcMessage response( id );
    response.setType( MSG_TYPE_RESPONSE );
    response.setSender( request.getReceiver() );
    response.setReceiver( request.getSender() );
    return response;
}

//=============================================================================
// STATIC: SPELLipcHelper::createErrorResponse
//=============================================================================
SPELLipcMessage SPELLipcHelper::createErrorResponse( std::string id, const SPELLipcMessage& request )
{
    SPELLipcMessage response( id );
    response.setType( MSG_TYPE_ERROR );
    response.setSender( request.getReceiver() );
    response.setReceiver( request.getSender() );
    return response;
}
