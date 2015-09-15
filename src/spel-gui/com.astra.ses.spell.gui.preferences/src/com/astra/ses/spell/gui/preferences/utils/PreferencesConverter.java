///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.utils
// 
// FILE      : PreferencesConverter.java
//
// DATE      : 2010-05-26
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
package com.astra.ses.spell.gui.preferences.utils;

import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.preferences.keys.StatusColorKey;

/*******************************************************************************
 * 
 * CoreUtils has methods for associating objects defined in core plugin to
 * preferences keys
 * 
 ******************************************************************************/
public class PreferencesConverter
{
	/***************************************************************************
	 * Return the Status color preference key associated to the given ItemStatis
	 * 
	 * @param status
	 *            the status from which to get the StatusColorKey
	 * @return the StatusColorKey associated to the given status
	 **************************************************************************/
	public static StatusColorKey getStatusColor(ItemStatus status)
	{
		StatusColorKey result = StatusColorKey.UNKNOWN;
		switch (status)
		{
		case CANCELLED:
			result = StatusColorKey.CANCELLED;
			break;
		case ERROR:
			result = StatusColorKey.ERROR;
			break;
		case FAILED:
			result = StatusColorKey.FAILED;
			break;
		case PROGRESS:
			result = StatusColorKey.IN_PROGRESS;
			break;
		case SKIPPED:
			result = StatusColorKey.SKIPPED;
			break;
		case SUCCESS:
			result = StatusColorKey.SUCCESS;
			break;
		case SUPERSEDED:
			result = StatusColorKey.SUPERSEDED;
			break;
		case TIMEOUT:
			result = StatusColorKey.TIMEOUT;
			break;
		case WAITING:
			result = StatusColorKey.WAITING;
			break;
		case WARNING:
			result = StatusColorKey.WARNING;
			break;
		}
		return result;
	}
}
