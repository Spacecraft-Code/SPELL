// ################################################################################
// FILE       : SPELLtime.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Time utility class
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

#ifndef __SPELL_TIME_H__
#define __SPELL_TIME_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_UTIL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Time management class.
 **
 ** \par Description and usage:
 **
 **  	Used to manipulate time values and convert them to string representations.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLtime
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * Automatically gets and intializes the instance with the current time.
		**////////////////////////////////////////////////////////////////////
		SPELLtime();

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param secs IN: time in seconds
		 * \param delta IN: true if relative time
		**////////////////////////////////////////////////////////////////////
		SPELLtime( unsigned long secs, bool delta );

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param secs IN: time in seconds
		 * \param msecs IN: mseconds
		 * \param delta IN: true if relative time
		**////////////////////////////////////////////////////////////////////
		SPELLtime( unsigned long secs, unsigned int msecs, bool delta );

		//////////////////////////////////////////////////////////////////////
		/** Copy constructor.
		 *
		 * \param other IN: time instance to be copied.
		**////////////////////////////////////////////////////////////////////
		SPELLtime( const SPELLtime& other );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLtime();

    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Assignment operator.
		 *
		 * \param other IN: time instance whose values must be added.
		 *
		 * \return The instance with the updated values.
		**////////////////////////////////////////////////////////////////////
		SPELLtime& operator=( const SPELLtime& other );

		//////////////////////////////////////////////////////////////////////
		/** Less-than comparison operator.
		 *
		 * \param other IN: time instance to compare with.
		 *
		 * \return True if this instance represents a time earlier than the given
		 * instance.
		**////////////////////////////////////////////////////////////////////
		bool operator<( const SPELLtime& other ) const;

		//////////////////////////////////////////////////////////////////////
		/** Greater-than comparison operator.
		 *
		 * \param other IN: time instance to compare with.
		 *
		 * \return True if this instance represents a time later than the given
		 * instance.
		**////////////////////////////////////////////////////////////////////
		bool operator>( const SPELLtime& other ) const;

		//////////////////////////////////////////////////////////////////////
		/** Greater-than operator.
		 *
		 * \param secs IN: time in seconds to compare with.
		 *
		 * \return True if this instance represents a time later than the given
		 * seconds.
		**////////////////////////////////////////////////////////////////////
		bool operator>( const unsigned long& secs ) const;

		//////////////////////////////////////////////////////////////////////
		/** Less-than comparison operator.
		 *
		 * \param secs IN: time in seconds to compare with.
		 *
		 * \return True if this instance represents a time earlier than the given
		 * seconds.
		**////////////////////////////////////////////////////////////////////
		bool operator<( const unsigned long& secs ) const;

		//////////////////////////////////////////////////////////////////////
		/** Equality comparison operator.
		 *
		 * \param other IN: time instance to compare with.
		 *
		 * \return True if this instance represents the same time as the given
		 * instance.
		**////////////////////////////////////////////////////////////////////
		bool operator==( const SPELLtime& other ) const;

		//////////////////////////////////////////////////////////////////////
		/** Equality comparison operator.
		 *
		 * \param secs IN: seconds to compare with.
		 *
		 * \return True if this instance represents the same time as the given
		 * seconds.
		**////////////////////////////////////////////////////////////////////
		bool operator==( const unsigned long& secs ) const;

		//////////////////////////////////////////////////////////////////////
		/** Greater-or-equal-than comparison operator.
		 *
		 * \param other IN: time instance to compare with.
		 *
		 * \return True if this instance represents the same time as the given
		 * instance, or a later time.
		**////////////////////////////////////////////////////////////////////
		bool operator>=( const SPELLtime& other ) const;

		//////////////////////////////////////////////////////////////////////
		/** Less-or-equal-than comparison operator.
		 *
		 * \param other IN: time instance to compare with.
		 *
		 * \return True if this instance represents the same time as the given
		 * instance, or an earlier time.
		**////////////////////////////////////////////////////////////////////
		bool operator<=( const SPELLtime& other ) const;

		//////////////////////////////////////////////////////////////////////
		/** Addition operator

		 * Performs the addition and does not update this instance.
		 *
		 * \param other IN: time instance to add.
		 *
		 * \return The time instance with the added values.
		**////////////////////////////////////////////////////////////////////
		SPELLtime operator+(const SPELLtime& other);

		//////////////////////////////////////////////////////////////////////
		/** Addition-assignment operator.
		 *
		 * Performs the addition and updates this instance.
		 *
		 * \param other IN: time instance to add.
		 *
		 * \return The time instance with the added values.
		**////////////////////////////////////////////////////////////////////
		SPELLtime& operator+=(SPELLtime& other);

		//////////////////////////////////////////////////////////////////////
		/** Substraction operator

		 * Performs the substraction and does not update this instance.
		 *
		 * \param other IN: time instance to substract.
		 *
		 * \return The time instance with the substracted values.
		**////////////////////////////////////////////////////////////////////
		SPELLtime operator-(const SPELLtime& other);

		//////////////////////////////////////////////////////////////////////
		/** Substraction-assignment operator.
		 *
		 * Performs the substraction and updates this instance.
		 *
		 * \param other IN: time instance to substract.
		 *
		 * \return The time instance with the substracted values.
		**////////////////////////////////////////////////////////////////////
		SPELLtime& operator-=(SPELLtime& other);

		//////////////////////////////////////////////////////////////////////
		/** Convert to string.
		 *
		 * \return Formated time string corresponding to this instance.
		**////////////////////////////////////////////////////////////////////
		std::string toString() const;

		//////////////////////////////////////////////////////////////////////
		/** Convert to string compatible with SPELL TIME instances.
		 *
		 * \return Formated time string corresponding to this instance.
		**////////////////////////////////////////////////////////////////////
		std::string toTIMEString() const;

		//////////////////////////////////////////////////////////////////////
		/** Set to the current time.
		**////////////////////////////////////////////////////////////////////
		void setCurrent();

		//////////////////////////////////////////////////////////////////////
		/** Set time to the given time seconds.
		 *
		 * \param secs IN: time in seconds
		 * \param msecs IN: mseconds
		**////////////////////////////////////////////////////////////////////
		void set( unsigned long secs, unsigned int msecs );

		//////////////////////////////////////////////////////////////////////
		/** Set to the given time.
		 *
		 * \param secs IN: time in seconds
		 * \param msecs IN: mseconds
		 * \param delta IN: true if relative time
		**////////////////////////////////////////////////////////////////////
		void set( unsigned long secs, unsigned int msecs, bool delta );

		//////////////////////////////////////////////////////////////////////
		/** Get the seconds.
		 *
		 * \return The seconds.
		**////////////////////////////////////////////////////////////////////
		unsigned long getSeconds() const {
			return m_secs;
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the milliseconds.
		 *
		 * \return The milliseconds.
		**////////////////////////////////////////////////////////////////////
		unsigned int getMilliseconds() const {
			return m_msecs;
		};

		//////////////////////////////////////////////////////////////////////
		/** Check if this instance contains a delta time.
		 *
		 * \return True if the time is relative.
		**////////////////////////////////////////////////////////////////////
		bool isDelta() const {
			return m_delta;
		};

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
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

		/** Holds the seconds from the epoc */
		unsigned long m_secs;
		/** Holds the mseconds */
		unsigned int m_msecs;
		/** True if delta time */
		bool m_delta;
};

class SPELLticker
{
public:
	static void tick( const std::string& function, unsigned int line );
	static void tickIn( const std::string& function, unsigned int line );
	static void tickOut( const std::string& function, unsigned int line );

private:
	typedef std::vector<long> TimeIn;

	static TimeIn s_intime;
};

#ifdef WITH_TICK
#define TICK_IN SPELLticker::tickIn( __PRETTY_FUNCTION__, __LINE__ )
#define TICK_OUT SPELLticker::tickOut( __PRETTY_FUNCTION__, __LINE__ )
#else
#define TICK_IN ;
#define TICK_OUT ;
#endif

/*@}*/
#endif
