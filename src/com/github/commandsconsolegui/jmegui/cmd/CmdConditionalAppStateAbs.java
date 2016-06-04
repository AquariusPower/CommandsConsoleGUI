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
	private CommandsDelegatorI cd;
	
	private String	strCmdIdentifier;
	protected String strCmdPrefix="toggle";
	protected String strCmdSuffix="State";
	
	public CommandsDelegatorI getCmdDelegator(){
		return cd();
	}
	public CommandsDelegatorI cd(){
		return cd;
	}
	
	public String getCmd(){
		return strCmdIdentifier;
	}
	
//	@Deprecated
//	@Override
//	protected void configure(Application app) {
//		throw new NullPointerException("deprecated!!!");
//	}
	
	public static class CfgParm implements ICfgParm{
		String strCmdIdentifier;
		boolean bIgnorePrefixAndSuffix;
		public CfgParm(String strCmdIdentifier, boolean bIgnorePrefixAndSuffix) {
			super();
			this.strCmdIdentifier = strCmdIdentifier;
			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
		}
	}
	@Override
	protected void configure(ICfgParm icfg) {
//	protected void configure(String strCmdIdentifier,boolean bIgnorePrefixAndSuffix) {
		CfgParm cfg = (CfgParm)icfg;
		
		super.configure(new ConditionalAppStateAbs.CfgParm(GlobalSappRefI.i().get()));
		
			cd=GlobalCommandsDelegatorI.i().get();
			
			if(cfg.strCmdIdentifier==null || cfg.strCmdIdentifier.isEmpty())throw new NullPointerException("invalid cmd id");
			this.strCmdIdentifier="";
			if(!cfg.bIgnorePrefixAndSuffix)this.strCmdIdentifier+=strCmdPrefix;
			this.strCmdIdentifier+=cfg.strCmdIdentifier;
			if(!cfg.bIgnorePrefixAndSuffix)this.strCmdIdentifier+=strCmdSuffix;
			
			cd.addConsoleCommandListener(this);
			
			ReflexFillI.i().assertReflexFillFieldsForOwner(this);
		
//		return isConfigured();
	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(!cd.isInitialized())return false;
		return super.initCheckPrerequisites();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,strCmdIdentifier,"[bEnabledForce]")){
			Boolean bEnabledForce = cc.paramBoolean(1);
			if(!isInitializedProperly() && bEnabledForce){
				if(!preInitRequest()){
					cc.dumpWarnEntry("unable to initialize "+strCmdIdentifier);
				}
			}
			
			if(bEnabledForce!=null){
				setEnabledRequest(bEnabledForce);
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
