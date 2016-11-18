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

import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class can provide automatic boolean console command options to be toggled.<br>
 * You just need to create the variable properly and it will be automatically recognized.<br>
 * 
 * It is intended to change internal states, not to ex. toggle user interfaces, for that use
 * a normal command.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class BoolTogglerCmdFieldAbs<THIS extends BoolTogglerCmdFieldAbs<THIS>> extends VarCmdFieldAbs<Boolean,THIS>{
	private boolean bPrevious;
	private boolean bDoCallOnChange = true;
	
	public BoolTogglerCmdFieldAbs(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitValue){
		this( rfcfgOwnerUseThis,  bInitValue, "");
	}
	
	/**
	 * 
	 * @param rfcfgOwnerUseThis always pass as "this", the very class must implement it.
	 * @param bDefault as boolean so it cant be nullified.
	 * @param strHelp
	 */
//	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitialValue, String strReflexFillCfgCodePrefixVariant, String strHelp){
	public BoolTogglerCmdFieldAbs(IReflexFillCfg rfcfgOwnerUseThis, boolean bDefault, String strHelp){
		super(rfcfgOwnerUseThis, EVarCmdMode.VarCmd, bDefault, Boolean.class);
		setDenyNullValue(); //booleans cannot be null
		setHelp(strHelp);
		constructed();
	}
	
	/** same as {@link #b()}*/
	public boolean get(){return getValue();}
	/** same as {@link #b()} but returns in Boolean TODO any use for this? */
	public Boolean getBoolean(){return getValue();}
	/** same as {@link #b()}*/
	public boolean getBool(){return getValue();}
	/** same as {@link #b()}*/
	public boolean is(){return getValue();}
	/** same as {@link #b()}*/
	public boolean b(){return getValue();}
	
	/**
	 * 
	 * @return true once if value changed, and update the reference to wait for next change
	 */
	private boolean isChangedAndRefresh(){
//		DebugI.i().conditionalBreakpoint(strCommand.equals("cmd_TestDialog_CmdConditionalStateAbs_State_Toggle"));
		if(getValue() && bPrevious)return false;
		if(!getValue() && !bPrevious)return false;
		
//		if(Boolean.compare(bCurrent,bPrevious)!=0){
			bPrevious=getValue();
			return true;
//		}
//		return false;
	}
	
	
//	@Override
//	public String getValueAsString() {
//		return ""+getRawValue();
//	}
//	@Override
//	public String getValueAsString(int iIfFloatPrecision) {
//		return getValueAsString();
//	}
	
//	@Override
//	public String toString() {
//		return ""+bCurrent;
//	}

//	@Override
//	public int getReflexFillCfgVariant() {
//		return iReflexFillCfgVariant;
//	}

//	@Override
//	public String getCodePrefixVariant() {
//		return strReflexFillCfgCodePrefixVariant;
//	}
	
	public String getCmdIdAsCommand(boolean bForceState) {
		return getUniqueCmdId()+" "+bForceState;
	}
	
//	public IConsoleCommandListener getOwnerAsCmdListener() {
//		if(getOwner() instanceof CommandsDelegator){
//			return GlobalCommandsDelegatorI.i().getPseudoListener();
//		}
//		
//		if(getOwner() instanceof IConsoleCommandListener){
//			return (IConsoleCommandListener)getOwner();
//		}
//		
//		return null;
//	}

//	@Override
//	public String getReport() {
//		return getUniqueCmdId()+" = "+bCurrent;
//	}

//	public void set(boolean b){
////		set(b,true);
////	}
////	public void set(boolean b, boolean bUseCallQueue){
//		setValue(b);
//		
//		if(bConstructed){
//			if(isChangedAndRefresh()){
//				if(bDoCallOnChange){
//					if(this.caller==null){
//						MsgI.i().warn("null caller for "+this.getReport(), this);
////						throw new PrerequisitesNotMetException("null caller for "+this.getReport());
//					}else{
//						CallQueueI.i().addCall(this.caller);
//					}
//				}
//			}
//		}
//	}
	
	@Override
	public THIS setObjectRawValue(Object objValue,boolean bPreventCallerRunOnce) {
		if(objValue==null)throw new PrerequisitesNotMetException(BoolTogglerCmdFieldAbs.class.getSimpleName()+" can't be set to null!");
		
		if(objValue instanceof BoolTogglerCmdFieldAbs){
			objValue = ((BoolTogglerCmdFieldAbs)objValue).b();
		}else
		if(objValue instanceof String){
			String str=(String)objValue;
			if(str.equalsIgnoreCase("true")){
				objValue = true;
			}else
			if(str.equalsIgnoreCase("false")){
				objValue = false;
			}else{
				throw new PrerequisitesNotMetException("invalid boolean value", str);
			}
		}else
		{
			objValue = (Boolean)objValue; //default is expected type
		}
		
		super.setObjectRawValue(objValue,bPreventCallerRunOnce);
		
		return getThis();
	}
	
	@Override
	protected void prepareCallerAssigned() {
		if(isConstructed()){
			if(isChangedAndRefresh()){
				if(bDoCallOnChange){
					if(isCallerAssigned()){
						super.prepareCallerAssigned();
					}else{
						MsgI.i().devWarn("caller not set "+this.getFailSafeDebugReport(), this);
					}
				}
			}
		}
	}
	
//	public BoolTogglerCmdField setValue(Boolean b){
//		this.bCurrent=b;
////		if(super.getConsoleVarLink()!=null)
//		super.setObjectRawValue(this.bCurrent);
//		
//		if(bConstructed){
//			if(isChangedAndRefresh()){
//				if(bDoCallOnChange){
//					if(this.caller==null){
//						MsgI.i().warn("null caller for "+this.getReport(), this);
////						throw new PrerequisitesNotMetException("null caller for "+this.getReport());
//					}else{
//						CallQueueI.i().addCall(this.caller);
//					}
//				}
//			}
//		}
//		
//		return getThis();
//	}
	
//	@Override
////	public BoolTogglerCmdField setObjectValue(CommandsDelegator.CompositeControl ccCD, Object objValue) {
//	public BoolTogglerCmdField setObjectRawValue(Object objValue) {
//		this.set((Boolean)objValue);
////		super.setObjectValue(ccCD,objValue);
//		super.setObjectRawValue(objValue);
//		return getThis();
//	}

//	@Override
//	public String getVarId() {
//		if(strVarId==null){
//			super.setUniqueCmdId(ReflexFillI.i().createIdentifierWithFieldName(getOwner(), this, true));
//		}
//		return strVarId;
//	}
	
	
//	@Override
//	public Object getRawValue() {
//		return this.bCurrent;
//	}

//	@Override
//	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
//		this.vivo=vivo;
//	}
	
//	public static String getPrefix() {
//		return rfcfgOwner.;
//	}
//	
//	public static String getSuffix() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
//	public BoolTogglerCmdField setCallOnChange(CallableX caller){
//		if(!bDoCallOnChange){
//			// to avoid developer forgot already configured to call nothing
//			throw new PrerequisitesNotMetException("was set to call nothing already!",this,getHelp());
//		}
//		
//		this.caller=caller;
//		return getThis();
//	}
	@Override
	public THIS setCallerAssigned(CallableX caller) {
		if(!bDoCallOnChange){
			// to avoid developer forgot already configured to call nothing
			throw new PrerequisitesNotMetException("was set to call nothing already!",this,getHelp());
		}
		
		super.setCallerAssigned(caller);
		
		return getThis();
	}
	
	/**
	 * In case just the main boolean value will be used.
	 * @return 
	 */
	public THIS setCallNothingOnChange(){
		// to avoid developer forgotten previously configured caller
		if(isCallerAssigned())throw new PrerequisitesNotMetException("caller already set", this, getHelp());
//		PrerequisitesNotMetException.assertNotAlreadySet("caller", caller, null, this, getHelp());
		
//		if(caller!=null){
////			throw new PrerequisitesNotMetException("caller already set!",this,getHelp());
//		}
		
//		if(!bDoCallOnChange){
//			// to avoid double setup
//			throw new PrerequisitesNotMetException("will already call nothing!",this,getHelp());
//		}
		
		bDoCallOnChange=false;
		return getThis();
	}
	
	@Override
	public String getVariablePrefix() {
		return "Bool";
	}

	public boolean isEqualToAndEnabled(BoolTogglerCmdFieldAbs btg) {
		if(this==btg){
			return b();
		}
		
		return false;
	}

//	@Override
//	public BoolTogglerCmdField setHelp(String str) {
//		this.strHelp = str;
//		return getThis();
//	}

//	public static void removeFromList(BoolTogglerCmdField bt) {
//		abtgList.remove(bt);
//	}
	
//	@Override
//	protected THIS getThis() {
//		return (THIS)this;
//	}
	
	/**
	 * 
	 * @return current status
	 */
	public boolean toggle(){
		setObjectRawValue(!getBoolean());
		return getBoolean();
	}
	
	private String strCodePrefixDefault="btg";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}

//	public BoolTogglerCmdField setAsBugFixerMode() {
//		bBugFixerMode=true;
//		WorkAroundI.i().prepareBugFix(this);
//		return getThis();
//	}

}
