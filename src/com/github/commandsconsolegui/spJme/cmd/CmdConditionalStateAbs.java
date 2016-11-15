/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
	
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

package com.github.commandsconsolegui.spJme.cmd;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.IConsoleCommandListener;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spJme.ConditionalStateAbs;

/**
 * basic state console commands control
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class CmdConditionalStateAbs<THIS extends CmdConditionalStateAbs<THIS>> extends ConditionalStateAbs<THIS> implements IConsoleCommandListener {
	StringCmdField scfRestart = new StringCmdField(this,null);
	protected final BoolTogglerCmdField btgEnabled = new BoolTogglerCmdField(this, true, "toggles the state (enabled/disabled)");
//		.setCallerAssigned(new CallableX(this) {
//			@Override
//			public Boolean call() {
//				if(
//						( isEnabled() && !btgEnabled.b()) ||
//						(!isEnabled() &&  btgEnabled.b())
//				){
//					setEnabledRequest(btgEnabled.b());
//				}
//				
//				return true;
//			}
//		});
	
	public CmdConditionalStateAbs() {
		super();
	}
	
	/**
	 * short/easy access
	 * @return
	 */
	public CommandsDelegator cd(){
//		return cd;
		return GlobalCommandsDelegatorI.i();
	}
	
	public String getStateEnableCommandId(){
		return btgEnabled.getUniqueCmdId();
	}
	
	public static class CfgParm extends ConditionalStateAbs.CfgParm{
		public CfgParm(String strId) {
			super(strId);
		}
	}
	private CfgParm	cfg;
	@Override
	public THIS configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
		super.configure(cfg);
		
		cd().addConsoleCommandListener(this);
		
		btgEnabled.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				setEnabledRequest(btgEnabled.b());
				return true;
			}
		});
		
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
		
		storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(!cd().isInitialized())return false;
		return super.initCheckPrerequisites();
	}
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(scfRestart)){
			requestRestart();
			
			bCommandWorked = true;
		}else
		{
			return ECmdReturnStatus.NotFound; //end of inheritance seek
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	protected void enableSuccess() {
		super.enableSuccess();
		btgEnabled.setObjectRawValue(true);
		cd().dumpInfoEntry(getStateEnableCommandId()+" enable");
	}
	
	@Override
	protected void disableSuccess() {
		btgEnabled.setObjectRawValue(false);
		cd().dumpInfoEntry(getStateEnableCommandId()+" disable");
	}
	
	private boolean bPrefixCmdWithIdToo=false;
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		ReflexFillCfg rfcfgBase = cd().getReflexFillCfg(rfcv);
		if(rfcfgBase!=null){
			rfcfg = new ReflexFillCfg(rfcfgBase,rfcv);
			
			if(rfcfg.isCommandToo() && bPrefixCmdWithIdToo){
//				rfcfg.setPrefix(rfcfgBase.getPrefix()+getId());
				rfcfg.setPrefixCustomId(getUniqueId());
//				rfcfg.setFirstLetterUpperCase(true);
			}
		}else{
			rfcfg = new ReflexFillCfg(rfcv);
		}
		
		return rfcfg;
	}

	public boolean isPrefixCmdWithIdToo() {
		return bPrefixCmdWithIdToo;
	}
	
	protected void setPrefixCmdWithIdToo(boolean bPrefixCmdWithIdToo) {
		this.bPrefixCmdWithIdToo = bPrefixCmdWithIdToo;
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=CmdConditionalStateAbs.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=CmdConditionalStateAbs.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
	
}
