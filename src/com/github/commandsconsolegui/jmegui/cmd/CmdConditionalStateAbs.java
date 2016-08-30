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

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.jmegui.ConditionalStateAbs;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
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
public abstract class CmdConditionalStateAbs extends ConditionalStateAbs implements IConsoleCommandListener, IReflexFillCfg {
//	private CommandsDelegator cd;
	
	StringCmdField scfRestart = new StringCmdField(this,null);
//	private StringCmdField cmdState = null;
	protected final BoolTogglerCmdField btgState = new BoolTogglerCmdField(this, true, null, "toggles the state (enabled/disabled)");
//	private String	strCmdIdentifier;
	private String strCmdPrefix="toggle";
	private String strCmdSuffix="State";
	
	public CmdConditionalStateAbs() {
		// TODO Auto-generated constructor stub
	}
	
//	public CommandsDelegator getCmdDelegator(){
//		return cd();
//	}
	/**
	 * short/easy access
	 * @return
	 */
	public CommandsDelegator cd(){
//		return cd;
		return GlobalCommandsDelegatorI.i();
	}
	
	public String getCmd(){
		return btgState.getUniqueCmdId();
//		return cmdState.toString();
//		return strCmdIdentifier;
	}
	
//	@Deprecated
//	@Override
//	private void configure(Application app) {
//		throw new NullPointerException("deprecated!!!");
//	}
	
//	public static class CfgParm implements ICfgParm{
////		private boolean bIgnorePrefixAndSuffix;
//		private String	strId;
//		public String getId(){return strId;}
//		public CfgParm(String strCmdBaseId) {
//			super();
//			this.strId = strCmdBaseId;
////			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
//		}
//	}
	public static class CfgParm extends ConditionalStateAbs.CfgParm{
		public CfgParm(String strId) {
			super(GlobalAppRefI.i(), strId);
		}
	}
	private CfgParm	cfg;
	@Override
	public CmdConditionalStateAbs configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
//		super.configure(new ConditionalStateAbs.CfgParm(GlobalAppRefI.i(), cfg.strId));
		super.configure(cfg);
		
//		cd=GlobalCommandsDelegatorI.i();
		
//		if(cfg.strId==null || cfg.strId.isEmpty())throw new NullPointerException("invalid cmd id");
//		String strCmdIdentifier = "";
//		if(!cfg.bIgnorePrefixAndSuffix)strCmdIdentifier+=strCmdPrefix;
//		strCmdIdentifier+=cfg.strId;
//		if(!cfg.bIgnorePrefixAndSuffix)strCmdIdentifier+=strCmdSuffix;
//		cmdState = new StringCmdField(strCmdIdentifier,"[bEnabledForce]");
		
		cd().addConsoleCommandListener(this);
		
		btgState.setCallOnChange(new CallableX() {
			@Override
			public Boolean call() throws Exception {
				setEnabledRequest(btgState.b());
				return true;
			}
		});
		
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
		
//		return isConfigured();
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(!cd().isInitialized())return false;
		return super.initCheckPrerequisites();
	}
	
//	@Override
//	protected boolean updateOrUndo(float tpf) {
//		if(!super.updateOrUndo(tpf))return false;
//		
//		updateToggles();
//		
//		return true;
//	}
	
//	@Override
//	protected boolean initOrUndo() {
//		if(!super.initOrUndo())return false;
//		
//		return true;
//	}
	
////	@Override
//	public boolean applyBoolTogglerChange(BoolTogglerCmdField btgSource) {
//		if(btgSource.equals(btgState)){
//			if(btgState.isChangedAndRefresh()){
//				setEnabledRequest(btgState.b());
//				return true;
//			}
//		}else{
//			throw new PrerequisitesNotMetException("missing code support to "+btgSource.getReport());
//		}
//		
//		return false;
//	}

	
//	@Override
//	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
////		if(!isConfigured())throw new PrerequisitesNotMetException("not configured yet! at the inherited of this method, you can skip with "+ECmdReturnStatus.class.getSimpleName()+"."+ECmdReturnStatus.Skip);
////		if(!isInitializedProperly())throw new PrerequisitesNotMetException("not initialized yet!");
//		
//		boolean bCommandWorked = false;
//		
//		if(cc.checkCmdValidity(this,cmdState,null)){
//			Boolean bEnabledForce = cc.paramBoolean(1);
//			
//			if(bEnabledForce!=null){
//				setEnabledRequest(bEnabledForce);
//			}else{
//				toggleRequest();
//			}
//			
//			bCommandWorked = true;
//		}else
//		{
//			return ECmdReturnStatus.NotFound; //end of inheritance seek
//		}
//		
//		return cc.cmdFoundReturnStatus(bCommandWorked);
//	}
@Override
public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
	boolean bCommandWorked = false;
	
	if(cd.checkCmdValidity(this,scfRestart,null)){
//		Boolean bEnabledForce = cc.paramBoolean(1);
//		
//		if(bEnabledForce!=null){
//			setEnabledRequest(bEnabledForce);
//		}else{
//			toggleRequest();
//		}
		requestRestart();
		
		bCommandWorked = true;
	}else
	{
		return ECmdReturnStatus.NotFound; //end of inheritance seek
	}
	
	return cd.cmdFoundReturnStatus(bCommandWorked);
}
	
	@Override
	protected boolean enableOrUndo() {
		if(!super.enableOrUndo())return false;
		
		cd().dumpInfoEntry(getCmd()+" enabled");
		
		return true;
	}
	
	@Override
	protected boolean disableOrUndo() {
		if(!super.disableOrUndo())return false;
		
		btgState.setValue(false);//,false);
		
		cd().dumpInfoEntry(getCmd()+" disabled");
		
		return true;
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
				rfcfg.setPrefixCustomId(getId());
//				rfcfg.setFirstLetterUpperCase(true);
			}
		}else{
			rfcfg = new ReflexFillCfg(rfcv);
		}
		
		return rfcfg;
	}

	public String getCmdPrefix() {
		return strCmdPrefix;
	}

	protected void setCmdPrefix(String strCmdPrefix) {
		this.strCmdPrefix = strCmdPrefix;
	}

	public String getCmdSuffix() {
		return strCmdSuffix;
	}

	protected void setCmdSuffix(String strCmdSuffix) {
		this.strCmdSuffix = strCmdSuffix;
	}

	public boolean isPrefixCmdWithIdToo() {
		return bPrefixCmdWithIdToo;
	}
	
	protected void setPrefixCmdWithIdToo(boolean bPrefixCmdWithIdToo) {
		this.bPrefixCmdWithIdToo = bPrefixCmdWithIdToo;
	}

}
