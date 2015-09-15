///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ControllerAdapter.java
//
// DATE      : Jun 19, 2014
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class ControllerAdapter implements IProcedureController
{
    @Override
    public void notifyProcedurePrompt(InputData inputData){}
    @Override
    public void notifyProcedureCancelPrompt(InputData inputData){}
    @Override
    public void notifyProcedureFinishPrompt(InputData inputData){}
    @Override
    public String getListenerId(){ return null; }
    @Override
    public void setError(ErrorData data){}
    @Override
    public void abort() {}
    @Override
    public void clearBreakpoints() {}
    @Override
    public void gotoLine(int lineNumber) {}
    @Override
    public void gotoLabel(String label) {}
    @Override
    public void pause() {}
    @Override
    public void interrupt() {}
    @Override
    public void issueCommand(ExecutorCommand cmd, String[] args) throws CommandFailed {}
    @Override
    public void recover() {}
    @Override
    public void reload() {}
    @Override
    public void refresh() throws Exception {};
    @Override
    public void updateInfo() throws Exception {};
    @Override
    public void updateConfig() throws Exception {};
    @Override
    public void run() {}
    @Override
    public void script(String script){}
    @Override
    public void setBrowsableLib(boolean showLib) {}
    @Override
    public void setRunInto(boolean runInto) {}
    @Override
    public void setStepByStep(boolean value) {}
    @Override
    public void setExecutionDelay(int msec) {}
    @Override
	public void setPromptWarningDelay(int msec) {}
    @Override
    public void setExecutorStatus(ExecutorStatus status) {}
    @Override
    public void setBreakpoint(int lineNumber, BreakpointType type) {}
    @Override
    public void skip() {}
    @Override
    public InputData getPromptData() { return null; }
    @Override
    public void step() {}
    @Override
    public void stepOver() {}
    @Override
    public void moveStack( int level ) {}
	@Override
    public void setForceTcConfirmation(boolean value) {}

}
