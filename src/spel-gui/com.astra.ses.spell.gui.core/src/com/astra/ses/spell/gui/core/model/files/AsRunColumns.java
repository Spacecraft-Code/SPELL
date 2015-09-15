///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.files
// 
// FILE      : AsRunColumns.java
//
// DATE      : 2008-11-21 08:58
//
// Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.model.files;

/*******************************************************************************
 * Representation of AsRun columns
 * 
 ******************************************************************************/
public enum AsRunColumns
{
	TIMESTAMP("Timestamp", 15), SEQUENCE("Seq", 5), TYPE("Type", 7), SUBTYPE("Subtype", 8), STACK_POSITION(
	        "Stack position", 20), DATA_A("Data A", 20), DATA_B("Data B", 6), DATA_C(
	        "Data C", 6), DATA_D("Data D", 6), COMMENTS("Comments", 12);

	/** Column name */
	public String	name;
	/** Column width (percentage) */
	public int	  width;

	/***********************************************************************
	 * Constructor
	 * 
	 * @param name
	 *            the printable name
	 * @param width
	 **********************************************************************/
	private AsRunColumns(String name, int width)
	{
		this.name = name;
		this.width = width;
	}

	/***********************************************************************
	 * Retrieve relevant information for this column
	 * 
	 * @param line
	 * @return
	 **********************************************************************/
	public String visit(AsRunFileLine line)
	{
		String result = "";
		try
		{
			switch (this)
			{
			case SEQUENCE:
				result = Long.toString(line.getSequence());
				break;
			case TIMESTAMP:
				result = line.getTimestamp();
				break;
			case TYPE:
				result = line.getType();
				break;
			case SUBTYPE:
				result = line.getSubType();
				break;
			case STACK_POSITION:
				result = line.getStackPosition();
				break;
			case DATA_A:
				result = line.getDataA();
				break;
			case DATA_B:
				result = line.getDataB();
				break;
			case DATA_C:
				result = line.getDataC();
				break;
			case DATA_D:
				result = line.getDataD();
				break;
			case COMMENTS:
				result = line.getComment();
				break;
			}
		}
		catch (Exception e)
		{
			// Nothing to do
		}
		return result;
	}
}
