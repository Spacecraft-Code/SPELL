// ################################################################################
// FILE       : SPELLbytecode.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Bytecode parser & analyzer. Used to identify special byte codes
//				that affect the executor features at runtime.
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

#ifndef __SPELL_BCODE_H__
#define __SPELL_BCODE_H__

#ifndef JUMP_IF_TRUE
#define JUMP_IF_TRUE JUMP_IF_TRUE_OR_POP
#define JUMP_IF_FALSE JUMP_IF_FALSE_OR_POP
#endif

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
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
 ** \brief Bytecode analyzer.
 **
 ** \par Description and usage:
 **
 ** 	Used by the executor to find information about the procedure source
 ** 	code lines.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLbytecode
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLbytecode( PyCodeObject* code );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLbytecode();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Check if the given line is executable (used for multiline skips).
		 *
		 * \param lineNo IN: the line number
		 *
		 * \return True if the line is executable (cannot be multi-skipped)
		**////////////////////////////////////////////////////////////////////
		bool isExecutable( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the given line is to be kept with next one (used for multiline skips).
		 *
		 * \param lineNo IN: the line number
		 *
		 * \return True if the line needs to be kept with the next one (cannot be single-skipped)
		**////////////////////////////////////////////////////////////////////
		bool isKeepWithNext( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the given line is a return <constant>
		 *
		 * \param lineNo IN: the line number
		 *
		 * \return True if the line contains a return <constant> statement
		**////////////////////////////////////////////////////////////////////
		bool isReturnConstant( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the given line is within a try-except block
		 *
		 * \param lineNo IN: the line number
		 *
		 * \return True if the line is inside try block
		**////////////////////////////////////////////////////////////////////
		bool isInTryBlock( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the given line is within a try-except block
		 *
		 * \param lineNo IN: the line number
		 *
		 * \return True if the line is inside try block
		**////////////////////////////////////////////////////////////////////
		bool isInExceptBlock( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the given line is within a try-except block
		 *
		 * \param lineNo IN: the line number
		 *
		 * \return True if the line is inside try block
		**////////////////////////////////////////////////////////////////////
		bool isInFinallyBlock( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the given line is the last one of a try block
		 *
		 * \param lineNo IN: the line number
		 *
		 * \return True if the line is the last of a try block
		**////////////////////////////////////////////////////////////////////
		bool isTryBlockEnd( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Get the last line of the current try block, if applicable
		 *
		 * \param lineNo IN: the current line number
		 *
		 * \return The end line of the block or -1 if not applicable
		**////////////////////////////////////////////////////////////////////
		int tryBlockEndLine( unsigned int lineNo ) const;

		//////////////////////////////////////////////////////////////////////
		/** Get the last address of the procedure bytecode.
		 *
		 * \return The last bytecode address.
		**////////////////////////////////////////////////////////////////////
		const int getLastAddress() const {
			return m_lastAddr;
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the last line number of the procedure.
		 *
		 * \return The last line number.
		**////////////////////////////////////////////////////////////////////
		const int getLastLine() const
		{
			return m_lastLine;
		};

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
		/** Bytecode information for a given line of the procedure */
		typedef struct BLine_
		{
			BLine_()
			{
				executable = false;
				keepWithNext = false;
				returnConst = false;
				lineno = -1;
				opcode = -1;
				offset = -1;
			};

			unsigned int lineno;
			unsigned int offset;
			unsigned int opcode;
			bool executable;
			bool keepWithNext;
			bool returnConst;
		}
		BLine;
		/** List of line information */
		typedef std::list<BLine> LineList;

		typedef struct _tryblock
		{
			unsigned int try_lineno;
			unsigned int end_try_lineno;
			unsigned int except_lineno;
			unsigned int end_except_lineno;
			unsigned int end_lineno;
		} TryBlock;

		typedef std::list<TryBlock> TryBlockList;

    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Analyze the given bytecode.
		**////////////////////////////////////////////////////////////////////
		void analyze();

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Reference to the code object (borrowed reference) */
		PyCodeObject*   m_code;
		/** Line information list */
		LineList        m_lines;
		/** Try blocks information list */
		TryBlockList    m_tryBlocks;
		/** Holds the last bytecode address */
		int			    m_lastAddr;
		/** Holds the last line of the proc */
		int				m_lastLine;
};

/*@}*/
#endif
