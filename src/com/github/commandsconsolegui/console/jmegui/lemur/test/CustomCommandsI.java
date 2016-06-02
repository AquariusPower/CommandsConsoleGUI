/* 
	Copyright (c) 2016, AquariusPower <https://github.com/AquariusPower>
	
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification, are permitted 
	provided that the following conditions are met:

	1.	Redistributions of source code must retain the above copyright notice, this list of conditions 
		and the following disclaimer.

	2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
		and the following disclaimer in the documentation and/or other materials provided with the distribution.
	
	3.	Neither the name of the copyright holder nor the names of its contributors may be used to endorse 
		or promote products derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
	PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
	LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN 
	IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.github.commandsconsolegui.console.jmegui.lemur.test;

import com.github.commandsconsolegui.cmd.IConsoleUI;
import com.github.commandsconsolegui.cmd.ScriptingCommandsDelegatorI;
import com.github.commandsconsolegui.cmd.jmegui.CommandsBackgroundState;
import com.github.commandsconsolegui.console.jmegui.lemur.ConsoleGUILemurStateI;
import com.github.commandsconsolegui.extras.jmegui.FpsLimiterState;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.jme3.input.KeyInput;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CustomCommandsI extends ScriptingCommandsDelegatorI{ //use ConsoleCommands to prevent scripts usage
//	public FpsLimiterState fpslState = new FpsLimiterState();
	
	private ConsoleGuiTestI	sapp;

	public CustomCommandsI(ConsoleGuiTestI sapp){
		super();
		
		this.sapp=sapp;
		
		ConsoleGUILemurStateI.i().configureBeforeInitializing(sapp, this, KeyInput.KEY_F10);
		FpsLimiterState.i().configureBeforeInitializing(sapp, this);
		
		/**
		 *  This allows test3 at endUserCustomMethod() to work.
		 */
		ReflexFillI.i().setUseDefaultCfgIfMissing(true);
	}
	
	@Override
	public ECmdReturnStatus executePreparedCommandRoot() {
		boolean bCommandWorked = false;
		
		if(checkCmdValidity(null,"fpsLimit","[iMaxFps]")){
			Integer iMaxFps = paramInt(1);
			if(iMaxFps!=null){
				FpsLimiterState.i().setMaxFps(iMaxFps);
				bCommandWorked=true;
			}
			dumpSubEntry("FpsLimit = "+FpsLimiterState.i().getFpsLimit());
		}else
		{
			return super.executePreparedCommandRoot();
		}
		
		return cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public void updateToggles() {
		if(btgFpsLimit.checkChangedAndUpdate())FpsLimiterState.i().setEnabled(btgFpsLimit.b());
		super.updateToggles();
	}
	
	@Override
	public String prepareStatsFieldText() {
		String strStatsLast = super.prepareStatsFieldText();
		
		if(EStats.MousePosition.b()){
			strStatsLast+=
					"xy"
						+(int)sapp.getInputManager().getCursorPosition().x
						+","
						+(int)sapp.getInputManager().getCursorPosition().y
						+";";
		}
		
		if(EStats.TimePerFrame.b()){
			strStatsLast+=
					"Tpf"+(FpsLimiterState.i().isEnabled() ? (int)(fTPF*1000.0f) : MiscI.i().fmtFloat(fTPF,6)+"s")
						+(FpsLimiterState.i().isEnabled()?
							"="+FpsLimiterState.i().getFrameDelayByCpuUsageMilis()+"+"+FpsLimiterState.i().getThreadSleepTimeMilis()+"ms"
							:"")
						+";";
		}
		
		return strStatsLast; 
	}
	
	@Override
	public void cmdExit() {
		sapp.stop();
		super.cmdExit();
	}
	
	@Override
	public void configure(IConsoleUI icui) {
		super.configure(icui);
		
		CommandsBackgroundState.i().configure(sapp, icui, this);
		MiscJmeI.i().configure(sapp, this);
	}
}
