// ################################################################################
// FILE       : SPELLsocket.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the socket wrapper
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
#include "SPELL_IPC/SPELLsocket.H"
#include "SPELL_IPC/SPELLipcError.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <poll.h>
#include <fcntl.h>


// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLsocket::SPELLsocket
//=============================================================================
SPELLsocket::SPELLsocket( int fd )
{
    m_socketFd = fd;
    m_connected = true;
    ::fcntl(m_socketFd, F_SETFD, ::fcntl(m_socketFd, F_GETFD) | FD_CLOEXEC );
}

//=============================================================================
// DESTRUCTOR: SPELLsocket::~SPELLsocket
//=============================================================================
SPELLsocket::~SPELLsocket()
{
    DEBUG("[SKT] Socket destroyed: " + ISTR(m_socketFd))
}

//=============================================================================
// METHOD: SPELLsocket::receive
//=============================================================================
int SPELLsocket::receive( void* buffer, int size )
{
    //DEBUGX(std::string tmp);
    if (!m_connected) return 0;
    int readLen = recv( m_socketFd, buffer, size, 0 );
    //DEBUGX(tmp.assign((char*) buffer, ((char*)buffer) + readLen));
    //DEBUG("SPELL SOCKET RECEIVED (" + ISTR(readLen) + "):" + SPELLutils::dumpString(tmp));
    return readLen;
}

//=============================================================================
// METHOD: SPELLsocket::receiveAll
//=============================================================================
int SPELLsocket::receiveAll( void* buffer, int size )
{
    if (!m_connected) return 0;
    int readLen = recv( m_socketFd, buffer, size, MSG_WAITALL );
    return readLen;
}

//=============================================================================
// METHOD: SPELLsocket::send
//=============================================================================
int SPELLsocket::send( const void* buffer, int size )
{
    //DEBUGX(std::string tmp);
    if (!m_connected) return 0;
    int numWritten = write(m_socketFd,buffer,size);
    //DEBUGX(tmp.assign((char*) buffer, ((char*)buffer) + numWritten));
    //DEBUG("SPELL SOCKET WROTE (" + ISTR(numWritten) + "):" + SPELLutils::dumpString(tmp));
    return numWritten;
}

//=============================================================================
// METHOD: SPELLsocket::shutdownRead
//=============================================================================
void SPELLsocket::shutdownRead()
{
    m_connected = false;
    DEBUG("[SKT] Socket shutdown RD")
    ::shutdown(m_socketFd, SHUT_RD);
    ::close(m_socketFd);
}

//=============================================================================
// METHOD: SPELLsocket::shutdownWrite
//=============================================================================
void SPELLsocket::shutdownWrite()
{
    m_connected = false;
    DEBUG("[SKT] Socket shutdown WR")
    ::shutdown(m_socketFd, SHUT_WR);
    ::close(m_socketFd);
}

//=============================================================================
// METHOD: SPELLsocket::shutdown
//=============================================================================
int SPELLsocket::shutdown()
{
    m_connected = false;
    DEBUG("[SKT] Socket shutdown RDWR")
    ::shutdown(m_socketFd, SHUT_RDWR);
    return ::close(m_socketFd);
}

//=============================================================================
// METHOD: SPELLsocket::close
//=============================================================================
void SPELLsocket::close()
{
    m_connected = false;
    DEBUG("[SKT] Socket close")
    ::close(m_socketFd);
}

//=============================================================================
// METHOD: SPELLsocket::isConnected
//=============================================================================
SPELLsocket* SPELLsocket::acceptClient( bool* disconnected )
{
    // Master file descriptor list
    fd_set master;
    // Temp file descriptor list for select()
    fd_set read_fds;
    // Client address
    struct sockaddr_in client_addr;
    // New connection fd
    int newfd;

    FD_ZERO(&master);
    FD_ZERO(&read_fds);

    FD_SET(m_socketFd, &master);
    FD_SET(m_socketFd, &read_fds);

    // Timeout for the select
    struct timeval timeout;
    timeout.tv_sec = 1;
    timeout.tv_usec = 0;

    // Reset the timeout, which may be modified by select
    timeout.tv_sec = 2;
    timeout.tv_usec = 0;

    // Wait on the file descriptors
    int numfds = select( m_socketFd+1, &read_fds, NULL, NULL, &timeout);

    if( numfds == -1)
    {
        if (m_connected)
        {
            LOG_ERROR("Error on socket select: " + ISTR(errno));
        }
        *disconnected = true;
        return NULL;
    }

    if(FD_ISSET(m_socketFd, &read_fds))
    {   // we got one...
        // New connections
        socklen_t addrlen = sizeof(client_addr);
        if((newfd = accept(m_socketFd, (struct sockaddr *)&client_addr, &addrlen)) == -1)
        {
            if (m_connected)
            {
                LOG_ERROR("Error on socket accept: " + ISTR(errno));
            }
            *disconnected = true;
            return NULL;
        }
        else
        {
            *disconnected = false;
            return new SPELLsocket( newfd );
        }
    }

    *disconnected = false;
    return NULL;
}

//=============================================================================
// STATIC: SPELLsocket::connect
//=============================================================================
SPELLsocket* SPELLsocket::connect( const std::string& server, const int& port )
{
    struct sockaddr_in serv_addr;
    struct hostent*    serverHost;

    int socketFd = socket(AF_INET, SOCK_STREAM, 0);
    if (socketFd< 0)
    {
        throw SPELLipcError("Error on socket creation: errno " + ISTR(errno));
    }

    DEBUG("   - Getting host");
    serverHost = gethostbyname(server.c_str());
    if (serverHost == NULL)
    {
        throw SPELLipcError("Error when getting server host name: errno " + ISTR(errno));
    }
    DEBUG("   - Server hostname: " + std::string(server));

    /** \todo socket options */

    bzero( (char*) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy( (char*) serverHost->h_addr, (char*)&serv_addr.sin_addr.s_addr, serverHost->h_length);
    serv_addr.sin_port = htons(port);
    DEBUG("   - Connecting to server at port " + ISTR(port));
    if (::connect(socketFd,(struct sockaddr *)&serv_addr,sizeof(serv_addr)) < 0)
    {
        throw SPELLipcError("Error on socket connect: errno " + ISTR(errno));
    }
    return new SPELLsocket(socketFd);
}

//=============================================================================
// STATIC: SPELLsocket::listen
//=============================================================================
SPELLsocket* SPELLsocket::listen( int* port )
{

    struct sockaddr_in serv_addr;
    bzero( (char*) &serv_addr, sizeof(serv_addr));

    int socketFd;
    // Get the listener socket
    if((socketFd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
    {
        throw SPELLipcError("Error on socket creation, errno " +ISTR(errno) );
    }

    // Set reuse address
    int yes = 1;
    if(setsockopt(socketFd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1)
    {
        throw SPELLipcError("Error on socket configuration (reuse addr), errno " + ISTR(errno));
    }

    // Set no linger
    struct linger linger;
    linger.l_onoff = 1; /*0 = off (l_linger ignored), nonzero = on */
    linger.l_linger =0; /*0 = discard data, nonzero = wait for data sent */
    if(setsockopt(socketFd, SOL_SOCKET, SO_LINGER, &linger, sizeof(linger)) == -1)
    {
        throw SPELLipcError("Error on socket configuration (linger), errno " + ISTR(errno));
    }

    // Set keep alive
    if(setsockopt(socketFd, SOL_SOCKET, SO_KEEPALIVE, &yes, sizeof(int)) == -1)
    {
        throw SPELLipcError("Error on socket configuration (keep alive), errno " + ISTR(errno));
    }

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(*port);
    memset(&(serv_addr.sin_zero), '\0', 8);

    if (bind(socketFd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0)
    {
        throw SPELLipcError("Error on socket bind, errno " + ISTR(errno));
    }

    socklen_t address_len = sizeof serv_addr;
    // Get the actual port (chosen by OS if m_serverPort was zero for bind)
    if (getsockname( socketFd, (struct sockaddr*) &serv_addr, &address_len )<0)
    {
        std::cerr << errno << std::endl;
        throw SPELLipcError("Error getting socket address, errno " + ISTR(errno) );
    }
    *port = ntohs(serv_addr.sin_port);
    DEBUG("Listening on port " + ISTR(*port));

    if (::listen(socketFd,5) == -1)
    {
        throw SPELLipcError("Error on listen, errno " + ISTR(errno));
    }
    return new SPELLsocket( socketFd );
}

//=============================================================================
// METHOD: SPELLsocket::waitData
//=============================================================================
bool SPELLsocket::waitData( int timeout )
{
    struct pollfd polldata;

    polldata.fd = m_socketFd;
    polldata.events = POLLIN | POLLPRI;
    polldata.revents = 0;

    int dataIn = poll( &polldata, 1, timeout );
    return (dataIn != 0);
}
