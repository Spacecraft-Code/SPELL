// ################################################################################
// FILE       : SPELLresourceInfo.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Procedure resource information
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

#ifndef __SPELL_RESOURCE_INFO_H__
#define __SPELL_RESOURCE_INFO_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Resource information
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct SPELLresourceInfo_
{
	SPELLresourceInfo_()
	{
		resourceName = "";
		resourceType = "";
		resourceGlobalValue = "";
		resourceLocalValue = "";
		resourceValueRange.clear();
		resourceGCSName = "";
	}

	SPELLresourceInfo_( const std::string& name,
				   const std::string& type,
				   const std::string& globalValue,
				   const std::string& localValue,
				   const std::vector<std::pair<std::string, std::string> >& valueRange,
				   const std::string& gcsName )
	{
		resourceName = name;
		resourceType = type;
		resourceGlobalValue = globalValue;
		resourceLocalValue = localValue;
		resourceValueRange.assign(valueRange.begin(), valueRange.end());
		resourceGCSName = gcsName;
	}

	SPELLresourceInfo_& operator=( const SPELLresourceInfo_& other )
	{
		if (this != &other)
		{
			resourceName = other.resourceName;
			resourceType = other.resourceType;
			resourceGlobalValue = other.resourceGlobalValue;
			resourceLocalValue = other.resourceLocalValue;
			resourceValueRange.assign(other.resourceValueRange.begin(), other.resourceValueRange.end());
			resourceGCSName = other.resourceGCSName;
		}
		return *this;
	}

	SPELLresourceInfo_( const SPELLresourceInfo_& other )
	{
		resourceName = other.resourceName;
		resourceType = other.resourceType;
		resourceGlobalValue = other.resourceGlobalValue;
		resourceLocalValue = other.resourceLocalValue;
		resourceValueRange.assign(other.resourceValueRange.begin(), other.resourceValueRange.end());
		resourceGCSName = other.resourceGCSName;
	}

	/** Holds the resource name */
	std::string resourceName;
	/** Holds the resource type string */
	std::string resourceType;
	/** Holds the resource global value */
	std::string resourceGlobalValue;
	/** Holds the resource local value */
	std::string resourceLocalValue;
	/** List of values that the resource can have. Empty means any value */
	std::vector<std::pair<std::string, std::string> > resourceValueRange;
	/** Holds the resource GCS name */
	std::string resourceGCSName;
}
SPELLresourceInfo;

/*@}*/
#endif
