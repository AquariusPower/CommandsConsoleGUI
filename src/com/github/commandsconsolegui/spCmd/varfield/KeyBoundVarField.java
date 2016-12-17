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

package com.github.commandsconsolegui.spCmd.varfield;

import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.KeyBind;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;

/**
 * This class is intended to be used only as class field variables.
 * It automatically creates console variables.
 * 
 * TODO set limit min and max, optinally throw exception or just fix the value to not over/underflow
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class KeyBoundVarField extends VarCmdFieldAbs<KeyBind,KeyBoundVarField>{
	private String	strFullUserCommand;
	
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis, KeyBind kb) {
		super(rfcfgOwnerUseThis, EVarCmdMode.VarCmd, kb, KeyBind.class, true);
//		this.strFullCommand=strFullCommand;
		constructed();
	}
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis) {
		this(rfcfgOwnerUseThis, (KeyBind)null);
	}
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis, String strBindCfg) {
		this(rfcfgOwnerUseThis, new KeyBind().setFromKeyCfg(strBindCfg));
	}
	/**
	 * @param lInitialValue if null, the variable will be removed from console vars.
	 */
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis, int iKeyActionCode, int... aiKeyModifierCodeList) {
		this(rfcfgOwnerUseThis, join(iKeyActionCode,aiKeyModifierCodeList));
	}
	
	public KeyBoundVarField setUserCommand(ArrayList<String> astr) {
		setUserCommand(String.join(" ", astr.toArray(new String[0])));
		return getThis();
	}
	public KeyBoundVarField setUserCommand(String strFullUserCommand){
		if(isField())throw new PrerequisitesNotMetException("cannot set a user command to a keybound field, such field UId is the command!", this, strFullUserCommand);
		
		this.strFullUserCommand=strFullUserCommand;
		
		setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				GlobalCommandsDelegatorI.i().addCmdToQueue(KeyBoundVarField.this.strFullUserCommand);
				return true;
			}
		});
		
		return getThis();
	}
	
	private static KeyBind join(int iAct, int... aiMod){
		KeyBind kb = new KeyBind();
		kb.setActionKey(iAct);
		
		kb.addModifier(aiMod);
		
		return kb;
	}
	private static Integer[] _join(int iAct, int... aiMod){
		Integer[] aiBoundCfg = new Integer[aiMod.length+1];
		aiBoundCfg[0]=iAct;
		for(int iIndex=0;iAct<aiMod.length;iAct++){
			aiBoundCfg[iIndex+1]=aiMod[iIndex];
		}
		return aiBoundCfg;
	}
	
	public String getBindCfg(){
		return getValue().getBindCfg();
	}
	
	@Override
	public KeyBoundVarField setObjectRawValue(Object objValue) {
		setObjectRawValue(objValue,true);
		return getThis();
	}
	
	/**
	 * @param bPreventCallerRunOnce (dummified, will be overriden by true)
	 */
	@Override
	public KeyBoundVarField setObjectRawValue(Object objValue,boolean bPreventCallerRunOnce) {
		if(objValue == null){
			//keep this empty skipper nullifier
		}else
		if(objValue instanceof String){
//			objValue = parseToBoundCfg((String)objValue, false); //coming from user action will just warn on failure
			objValue = new KeyBind().setFromKeyCfg((String)objValue);
		}else
		if(objValue instanceof KeyBind){
			//expected value
		}else
		if(objValue instanceof Integer[]){
			objValue = new KeyBind().setFromKeyCodes((Integer[])objValue);
		}else
		if(objValue instanceof Integer){
			objValue = new KeyBind().setFromKeyCodes((Integer)objValue);
		}else{
			throw new PrerequisitesNotMetException("unsupported class type", objValue.getClass());
		}
		
		if(objValue!=null){
			((KeyBind)objValue).setOwner(this);
//			GlobalManageKeyBindI.i().add(this);
		}
		
		super.setObjectRawValue(objValue,true); //must NEVER execute just on bind change...
		
		return getThis();
	}
	
//	@Override
//	protected void applyManager(){
//		/**
//		 * set only one manager reference here,
//		 * do not call super one!
//		 */
//		if(getManger()==null && !isValueNull()){
//			if(GlobalManageKeyBindI.iGlobal().isSet()){
//				GlobalManageKeyBindI.i().add(this);
//				setManager(GlobalManageKeyBindI.i());
//			}else{
//				MsgI.i().devWarn("global manager not ready yet...", GlobalManageKeyBindI.class, this);
//			}
//		}
//	}
	
	@Override
	public String getVariablePrefix() {
		return "Bind";
	}
	
	@Override
	protected KeyBoundVarField getThis() {
		return this;
	}

	private final String strCodePrefixDefault="bind";
	private boolean	bUseCallQueue = true;
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
	
	@Override
	protected String getFailSafeDebugValueReport(Object val) {
		if(val==null)return ""+null;
		
		return getBindCfg();
	}
	
	@Override
	public String getValueAsString(int iFloatingPrecision) {
		return getBindCfg();
	}
	
	public KeyBind getKeyBind(){
		return getValue();
	}
	
	public void runIfActivatedOrResetIfDeactivating() {
		if(getValue().isCanBeRunNowOrReset()){
			if(isUseCallQueue()){
				callerAssignedQueueNow();
			}else{
				callerAssignedRunNow();
			}
		}
	}
	
	private boolean isUseCallQueue() {
		return bUseCallQueue;
	}
	
	/**
	 * 
	 * @param bUseCallQueue if false, will try to promptly run the caller
	 * @return
	 */
	public KeyBoundVarField setUseCallQueue(boolean bUseCallQueue) {
		this.bUseCallQueue = bUseCallQueue;
		return getThis();
	}
	public String getUserCommand() {
		return strFullUserCommand;
	}
	
	public String getKeyBindRunCommand() {
		if(isField()){
			return getUniqueVarId(true);
		}else{
			return getUserCommand();
		}
	}
	
	@Override
	public ArrayList<IManager> getManagerList() {
		ArrayList<IManager> a = super.getManagerList();
		a.addAll(ManageVarCmdFieldI.i().getManagerListFor(KeyBoundVarField.class));
		return a;
	}
	
	@Override
	public void addManager(IManager imgr) {
		super.addManager(imgr);
		if(!ManageVarCmdFieldI.i().isHasVarManager(imgr)){
			ManageVarCmdFieldI.i().putVarManager(imgr, KeyBoundVarField.class);
		}
	}
}
