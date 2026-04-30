// ################################################################################
// FILE       : SPELLPing.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL command line executor main program
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

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// System includes -----------------------------------------------------------
// Local includes ------------------------------------------------------------
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_IPC/SPELLipcClientInterface.H"
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipc_Context.H"

// GLOBALS ///////////////////////////////////////////////////////////////////

// Initialization/configuration variables

static int logging = 0;
static std::string context = "";
static std::string configFile = "";
static int port  = 19888;

// STATIC ////////////////////////////////////////////////////////////////////


//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " -p <context port> [-d]" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - p : context port" <<  std::endl;
    std::cerr << "         - d : enable debug traces" <<  std::endl;
    std::cerr << std::endl;
}

//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "dp:n:c:")) != -1)
    {
        switch(code)
        {
        case 'd':
            logging = 1;
            break;
        case 'p':
            port = atoi(optarg);
            std::cout << "* Port " << port << std::endl;
            break;
        }
    }
    
    return 0;
}

//============================================================================
// MAIN PROGRAM
//============================================================================
int main( int argc, char** argv )
{
    if ( parseArgs(argc,argv) != 0 ) return 1;

    // Show the environment configuration
    SPELLutils::showEnvironment();
    // The time identifier is used to create unique file names

    SPELLipcClientInterface* ipc = new SPELLipcClientInterface("Ping","localhost",port);

    std::cout << "SPELL-Ping ### Connecting to Context" << std::endl;
    ipc->connect();
    bool connectSuccessful = ipc->isConnected();
    std::cout << "SPELL-Ping ### Connection Successful: " <<  (connectSuccessful ? "yes" : "no") << std::endl;
    if (connectSuccessful) {
        SPELLipcMessage* msg = new SPELLipcMessage(ContextMessages::REQ_CURRENT_TIME);
        msg->setType(MSG_TYPE_REQUEST);
        msg->setSender("PING");
        msg->setReceiver("CTX");
        std::cout << "SPELL-Ping ### Sending Ping." << std::endl;
        SPELLipcMessage answer = ipc->sendRequest(*msg,5000);
        std::cout << "SPELL-Ping ### Ping was sent." << std::endl;
        bool errorFound = answer.hasField(MessageField::FIELD_FATAL);

        std::cout << "SPELL-Ping ### Sending EOC" << std::endl;
        SPELLipcMessage* eoc = new SPELLipcMessage("EOC");
        eoc->setType( MSG_TYPE_EOC );

        ipc->sendMessage(*eoc);

        ipc->disconnect();
        if (errorFound){
            std::cout << "SPELL-Ping ### Ping not successful." << std::endl;
            return -2;
        } else {
            std::cout << "SPELL-Ping ### Ping successful." << std::endl;
            return 0;
        }
    } else {
        std::cout << "SPELL-Ping ### Connection failed." << std::endl;
        return -1;
    }
}
