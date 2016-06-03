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

package com.github.commandsconsolegui.jmegui.cmd;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.ConditionalAppStateAbs;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;

/**
 * basic state console commands control
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class CmdConditionalAppStateAbs extends ConditionalAppStateAbs implements IConsoleCommandListener, IReflexFillCfg {
	CommandsDelegatorI cd;
	private String	strCmdIdentifier;
	protected String strCmdPrefix="toggle";
	protected String strCmdSuffix="State";
	
	public void configure(String strCmdIdentifier) {
		super.configure(GlobalSappRefI.i().get());
		cd=GlobalCommandsDelegatorI.i().get();
		
		this.strCmdIdentifier=strCmdPrefix+strCmdIdentifier+strCmdSuffix;
		
		cd.addConsoleCommandListener(this);
		
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,strCmdIdentifier,"[bEnabledForce]")){
			Boolean bEnabledForce = cc.paramBoolean(1);
			if(!isInitialized() && bEnabledForce){
				if(!preInitRequest()){
					cc.dumpWarnEntry("unable to initialize "+strCmdIdentifier);
				}
			}
			
			if(bEnabledForce!=null){
				setEnabled(bEnabledForce);
			}else{
				toggleRequest();
			}
			
			bCommandWorked = true;
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cd.getReflexFillCfg(rfcv);
	}
}
