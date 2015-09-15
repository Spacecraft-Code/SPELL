// ################################################################################
// FILE       : SPELLtime.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the time object
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// Project includes --------------------------------------------------------

std::vector<long> SPELLticker::s_intime;

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime()
{
    setCurrent();
}

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime( unsigned long secs, bool delta )
{
    set(secs,0,delta);
}

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime( unsigned long secs, unsigned int msecs, bool delta )
{
    set(secs,msecs,delta);
}

//=============================================================================
// CONSTRUCTOR: SPELLtime::SPELLtime
//=============================================================================
SPELLtime::SPELLtime( const SPELLtime& other )
{
    set(other.m_secs,other.m_msecs,other.m_delta);
}

//=============================================================================
// DESTRUCTOR: SPELLtime::~SPELLtime
//=============================================================================
SPELLtime::~SPELLtime()
{
    // Nothing to do
}

//=============================================================================
// METHOD: SPELLtime::operator=
//=============================================================================
SPELLtime& SPELLtime::operator=( const SPELLtime& other )
{
    if (this != &other) // protect against invalid self-assignment
    {
        m_secs = other.m_secs;
        m_msecs = other.m_msecs;
        m_delta = other.m_delta;
    }
    // by convention, always return this
    return *this;
}

//=============================================================================
// METHOD: SPELLtime::operator+
//=============================================================================
SPELLtime SPELLtime::operator+(const SPELLtime& other)
{
    SPELLtime result(0,true);
    result.set( m_secs + other.m_secs, m_msecs + other.m_msecs, m_delta && other.m_delta );
    return result;
}

//=============================================================================
// METHOD: SPELLtime::operator+=
//=============================================================================
SPELLtime& SPELLtime::operator+=(SPELLtime& other)
{
    set( m_secs + other.m_secs, m_msecs + other.m_msecs, m_delta && other.m_delta );
    return *this;
}

//=============================================================================
// METHOD: SPELLtime::operator-
//=============================================================================
SPELLtime SPELLtime::operator-(const SPELLtime& other)
{
    SPELLtime result(0,true);
    bool delta = (m_delta && other.m_delta) || (!m_delta && !other.m_delta);
    long secs = m_secs - other.m_secs;
    int msecs = m_msecs - other.m_msecs;
    result.set( (secs>0)? secs: 0, (msecs>0)? msecs: 0, delta );
    return result;
}

//=============================================================================
// METHOD: SPELLtime::operator-=
//=============================================================================
SPELLtime& SPELLtime::operator-=(SPELLtime& other)
{
    bool delta = (m_delta && other.m_delta) || (!m_delta && !other.m_delta);
    long secs = m_secs - other.m_secs;
    int msecs = m_msecs - other.m_msecs;
    set( (secs>0)? secs: 0, (msecs>0)? msecs: 0, delta );
    return *this;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator<( const SPELLtime& other ) const
{
    if (m_secs < other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs < other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator>
//=============================================================================
bool SPELLtime::operator>( const SPELLtime& other ) const
{
    if (m_secs > other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs > other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator>
//=============================================================================
bool SPELLtime::operator>( const unsigned long& secs ) const
{
    if (m_secs > secs) return true;
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator<( const unsigned long& secs ) const
{
    if (m_secs < secs) return true;
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator==( const SPELLtime& other ) const
{
    if (m_secs == other.m_secs)
    {
        if (m_msecs == other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<
//=============================================================================
bool SPELLtime::operator==( const unsigned long& secs ) const
{
    if (m_secs == secs)
    {
        if (m_msecs == 0) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator>=
//=============================================================================
bool SPELLtime::operator>=( const SPELLtime& other ) const
{
    if (m_secs >= other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs >= other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::operator<=
//=============================================================================
bool SPELLtime::operator<=( const SPELLtime& other ) const
{
    if (m_secs <= other.m_secs) return true;
    if (m_secs == other.m_secs)
    {
        if (m_msecs <= other.m_msecs) return true;
    }
    return false;
}

//=============================================================================
// METHOD: SPELLtime::toString
//=============================================================================
std::string SPELLtime::toString() const
{
    if (m_secs==0)
    {
        return "0";
    }
    
    std::string timeStr = "";
    //Relative date
    if (isDelta())
    {
    	time_t seconds = m_secs;
        if (seconds<60) // Very short date format
        {
            timeStr = ISTR(seconds);
        }
		else
		{
			//Days
			if (seconds >= 86400) // Long date format
			{
				char buffer[10];
				sprintf( buffer, "+%03ld ", seconds/86400 );
				timeStr = buffer;
				seconds = seconds % 86400;
			}
			//Hours
			if ( seconds/3600 < 10 ) timeStr += "0";
			timeStr += ISTR(seconds/3600)+":";
			seconds = seconds % 3600;
			//Minutes
			if ( seconds/60 < 10 ) timeStr += "0";
			timeStr += ISTR(seconds/60)+":";
			seconds = seconds % 60;
			//Seconds
			if ( seconds < 10 ) timeStr += "0";
			timeStr += ISTR(seconds);
		}
	}
    //Absolute date e.g. 2009-12-07 16:55:12
    else
    {
        time_t seconds = m_secs;
        struct tm* ptm = localtime(&seconds);
        if (ptm==NULL)
        {
            return "\?\?\?\?-\?\?-\?\? \?\?:\?\?:\?\?";
        }
        timeStr += ISTR(1900+ptm->tm_year) + "-";
        std::string month = ISTR(ptm->tm_mon+1);
        if (month.length()==1) month = "0" + month;
        std::string day = ISTR(ptm->tm_mday);
        if (day.length()==1) day = "0" + day;
        timeStr += month + "-" + day + " ";

		//Hours
		if ( ptm->tm_hour < 10 ) timeStr += "0";
		timeStr += ISTR(ptm->tm_hour)+":";
		//Minutes
		if ( ptm->tm_min < 10 ) timeStr += "0";
		timeStr += ISTR(ptm->tm_min)+":";
		//Seconds
		if ( ptm->tm_sec < 10 ) timeStr += "0";
		timeStr += ISTR(ptm->tm_sec);
    }
    return timeStr;
}

//=============================================================================
// METHOD: SPELLtime::toString
//=============================================================================
std::string SPELLtime::toTIMEString() const
{
    if (m_secs==0)
    {
        return "+000 00:00:00";
    }

    std::string timeStr = "";
    //Relative date
    if (isDelta())
    {
        long seconds = m_secs;
        //Days
        if (seconds >= 86400) // Long date format
        {
            char buffer[10];
            sprintf( buffer, "+%03ld ", seconds/86400 );
            timeStr = buffer;
            seconds = seconds % 86400;
        }
        else
        {
        	timeStr = "+";
        }
        //Hours
        if ( seconds/3600 < 10 ) timeStr += "0";
        timeStr += ISTR(seconds/3600)+":";
        seconds = seconds % 3600;
        //Minutes
        if ( seconds/60 < 10 ) timeStr += "0";
        timeStr += ISTR(seconds/60)+":";
        seconds = seconds % 60;
        //Seconds
        if ( seconds < 10 ) timeStr += "0";
        timeStr += ISTR(seconds);
    }
    //Absolute date e.g. e.g. 2009-12-07:16:55:12
    else
    {
        time_t seconds = m_secs;
        struct tm* ptm = localtime(&seconds);
        if (ptm==NULL)
        {
            return "\?\?\?\?-\?\?-\?\? \?\?:\?\?:\?\?";
        }

        // Absolute dates convert always to full date format including year, month, etc.
        timeStr += ISTR(1900+ptm->tm_year) + "-";
        std::string month = ISTR(ptm->tm_mon+1);
        if (month.length()==1) month = "0" + month;
        std::string day = ISTR(ptm->tm_mday);
        if (day.length()==1) day = "0" + day;
        timeStr += month + "-" + day + ":";

		//Hours
		if ( ptm->tm_hour < 10 ) timeStr += "0";
		timeStr += ISTR(ptm->tm_hour)+":";
		//Minutes
		if ( ptm->tm_min < 10 ) timeStr += "0";
		timeStr += ISTR(ptm->tm_min)+":";
		//Seconds
		if ( ptm->tm_sec < 10 ) timeStr += "0";
		timeStr += ISTR(ptm->tm_sec);
    }
    return timeStr;
}

//=============================================================================
// METHOD: SPELLtime::setCurrent
//=============================================================================
void SPELLtime::setCurrent()
{
    SPELLutils::SPELLtimeDesc time = SPELLutils::getSystemTime();
    set( time.seconds, time.useconds/1000, false );
}

//=============================================================================
// METHOD: SPELLtime::set
//=============================================================================
void SPELLtime::set( unsigned long secs, unsigned int msecs )
{
    set(secs,msecs,true);
}

//=============================================================================
// METHOD: SPELLtime::set
//=============================================================================
void SPELLtime::set( unsigned long secs, unsigned int msecs, bool delta )
{
    m_secs = secs;
    m_msecs = msecs;
    m_delta = delta;
    //DEBUG("[TIME] Set time: " + ISTR(m_secs) + ", " + ISTR(m_msecs) + ", " + BSTR(m_delta))
}

//=============================================================================
// STATIC: SPELLticker::tickIn
//=============================================================================
void SPELLticker::tickIn( const std::string& function, unsigned int line )
{
    SPELLutils::SPELLtimeDesc time = SPELLutils::getSystemTime();

    long usec = time.seconds * 1000000 + time.useconds;
    long idx = s_intime.size();

    std::string indent('-', idx);
    int pos = function.find("::");
    int pos2 = function.rfind(" ",pos);
	std::string where = "[" + function.substr(pos2,function.size()-pos2) + ":" + ISTR(line) + "] ";

	std::cerr << std::left << std::setw(70) << where << std::left << std::setw(50) << indent << "IN  (" << idx << ")" << std::endl;
	s_intime.push_back(usec);
};

//=============================================================================
// STATIC: SPELLtime::tickOut
//=============================================================================
void SPELLticker::tickOut( const std::string& function, unsigned int line )
{
    SPELLutils::SPELLtimeDesc time = SPELLutils::getSystemTime();

    std::string indent('-', s_intime.size());

    long in = 0;
    if (s_intime.size()>0)
	{
    	in = s_intime.back();
    	s_intime.pop_back();
	}
    long idx = s_intime.size();
    long usec = time.seconds * 1000000 + time.useconds;
    long delta = usec - in;

    int pos = function.find("::");
    int pos2 = function.rfind(" ",pos);
	std::string where = "[" + function.substr(pos2,function.size()-pos2) + ":" + ISTR(line) + "] ";
	if (delta > 1000)
	{
		std::cerr << std::left << std::setw(70) << where << std::left << std::setw(50) << indent << "OUT (" << idx << "): " << delta/1000 << " ms." << std::endl;
	}
	else
	{
		std::cerr << std::left << std::setw(70) << where << std::left << std::setw(50) << indent << "OUT (" << idx << "): " << delta << " us." << std::endl;
	}
};
