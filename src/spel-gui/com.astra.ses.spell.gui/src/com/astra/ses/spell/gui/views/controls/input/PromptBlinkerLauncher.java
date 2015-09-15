///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.input
// 
// FILE      : PromptBlinkerLauncher.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.views.controls.input;

import org.eclipse.swt.widgets.Display;

/**
 * Launches the prompt blinker task after the specified delay, if not cancelled
 * before.
 */
class PromptBlinkerLauncher extends Thread
{
	/** Rate for checking */
	private static final int	CHECK_RATE_MSEC	= 500;
	/** Reference to the input area */
	private InputArea	     m_area;
	/** Target time including the delay before starting the blinker */
	private long	         m_targetTimeMillis;
	/** Cancelling flag */
	private boolean	         m_doIt;

	/**************************************************************************
	 * Constructor.
	 *************************************************************************/
	PromptBlinkerLauncher(InputArea area, long blinkDelay)
	{
		super();
		m_area = area;
		m_targetTimeMillis = System.currentTimeMillis() + blinkDelay;
		m_doIt = true;
		Display.getCurrent().timerExec(CHECK_RATE_MSEC, this);
	}

	/**************************************************************************
	 * Thread run method
	 *************************************************************************/
	@Override
	public void run()
	{
		if (!m_doIt) return;
		if (System.currentTimeMillis() >= m_targetTimeMillis)
		{
			m_area.startBLinking();
			m_doIt = false;
			return;
		}
		if (m_doIt)
		{
			Display.getCurrent().timerExec(CHECK_RATE_MSEC, this);
		}
	}

	/**************************************************************************
	 * Cancel the thread.
	 *************************************************************************/
	void cancel()
	{
		m_doIt = false;
	}

}
