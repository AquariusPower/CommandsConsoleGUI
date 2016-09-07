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

package com.github.commandsconsolegui.cmd.varfield;

import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs.EVarCmdMode;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.HandleExceptionsRaw;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.VarCmdUId;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

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
public class BoolTogglerCmdField extends VarCmdFieldAbs<Boolean,BoolTogglerCmdField>{
//	public static final String strCodePrefix="btg";
//	private static ArrayList<BoolTogglerCmdField> abtgList = new ArrayList<BoolTogglerCmdField>();
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
	
	private boolean bPrevious;
	private boolean bCurrent = false;

//	private String	strReflexFillCfgCodePrefixVariant;
	private boolean bDoCallOnChange = true;
	private CallableX	caller;
	private boolean	bConstructed;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		if(ihe==null)throw new NullPointerException("invalid instance for "+IHandleExceptions.class.getName()); // KEEP ON TOP
		BoolTogglerCmdField.ihe=ihe;
		bConfigured=true;
	}
	
//	public static ArrayList<BoolTogglerCmdField> getListCopy(){
//		ArrayList<BoolTogglerCmdField> a = VarCmdFieldAbs.getListCopy(BoolTogglerCmdField.class);
//		return a;
////		return new ArrayList<BoolTogglerCmdField>(abtgList);
//	}
	
//	private BoolTogglerCmdField(){
//		super(true);
////		abtgList.add(this);
//	}
	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitValue){
		this( rfcfgOwnerUseThis,  bInitValue, "");
	}
//	/**
//	 * 
//	 * @param rfcfgOwnerUseThis always pass as "this", the very class must implement it.
//	 * @param bInitValue
//	 * @param strReflexFillCfgCodePrefixVariant if null, will be default. Can be emtpy "".
//	 */
//	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitValue, String strReflexFillCfgCodePrefixVariant){
//		this( rfcfgOwnerUseThis,  bInitValue,  strReflexFillCfgCodePrefixVariant, "");
//	}
	/**
	 * 
	 * @param rfcfgOwnerUseThis always pass as "this", the very class must implement it.
	 * @param bInitialValue
	 * @param strHelp
	 */
//	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitialValue, String strReflexFillCfgCodePrefixVariant, String strHelp){
	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitialValue, String strHelp){
		super(rfcfgOwnerUseThis,EVarCmdMode.VarCmd);
		
//		ReflexFill.assertAndGetField(rfcfgOwnerUseThis, this);
		
//		setCodePrefixVariant(strReflexFillCfgCodePrefixVariant);
		
//		this.strReflexFillCfgCodePrefixVariant=MiscI.i().assertAndGetValidId(strReflexFillCfgCodePrefixVariant,BoolTogglerCmdField.getCodePrefixDefault());
//		this.strReflexFillCfgCodePrefixVariant=strReflexFillCfgCodePrefixVariant;
//		if(this.strReflexFillCfgCodePrefixVariant==null){
//			this.strReflexFillCfgCodePrefixVariant=BoolTogglerCmdField.getCodePrefixDefault();
//		}else{
//			if(!MiscI.i().isValidIdentifierCmdVarAliasFuncString(this.strReflexFillCfgCodePrefixVariant)){
//				throw new PrerequisitesNotMetException("invalid code prefix", this.strReflexFillCfgCodePrefixVariant);
//			}
//		}
		
//		setOwner(rfcfgOwnerUseThis);
		setHelp(strHelp);
		setObjectRawValue(bInitialValue);
		
		this.bConstructed=true;
	}
//	public BoolTogglerCmd(boolean bInitValue, String strCustomCmdId){
//		this();
//		set(bInitValue);
//		setCustomCmdId(strCustomCmdId);
//	}
	
//	/**
//	 * sets the command identifier that user will type in the console
//	 * @param strCmd
//	 */
//	public void setCustomCmdId(String strCmd) {
//		/**
//		 * must be an exception as it can have already been read/collected with automatic value.
//		 */
//		PrerequisitesNotMetException.assertNotAlreadySet("CustomCmdId", getUniqueCmdId(), strCmd);
////		if(getUniqueCmdId()!=null){
//////			throw new NullPointerException("asked for '"+strCmd+"' but was already set to: "+getUniqueCmdId());
////		}
//		
//		setUniqueId(new VarCmdId().setAsVariable(false).setSimpleId(strCmd).setUniqueId(strCmd));
//	}
	
//	public String getCmdId(){
//		if(super.strCmdId==null){
//			super.setId(ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner,this,false));
//		}
//		
//		return super.strCmdId;
//	}
	
//	@Override
//	public String getCoreId() {
//		if(super.strCmdId==null)getCmdId();
//		return super.getCoreId();
//	}
	
//	@Override
//	public String getHelp(){
//		return strHelp==null?"":strHelp;
//	}
	
	/** same as {@link #b()}*/
	public boolean get(){return bCurrent;}
	/** same as {@link #b()} but returns in Boolean */
	public Boolean getBoolean(){return bCurrent;}
	/** same as {@link #b()}*/
	public boolean getBool(){return bCurrent;}
	/** same as {@link #b()}*/
	public boolean is(){return bCurrent;}
	/** same as {@link #b()}*/
	public boolean b(){return bCurrent;}
	
	/**
	 * 
	 * @return true once if value changed, and update the reference to wait for next change
	 */
	private boolean isChangedAndRefresh(){
//		DebugI.i().conditionalBreakpoint(strCommand.equals("cmd_TestDialog_CmdConditionalStateAbs_State_Toggle"));
		if(bCurrent && bPrevious)return false;
		if(!bCurrent && !bPrevious)return false;
		
//		if(Boolean.compare(bCurrent,bPrevious)!=0){
			bPrevious=bCurrent;
			return true;
//		}
//		return false;
	}
	
	
	@Override
	public String getValueAsString() {
		return ""+getRawValue();
	}
	@Override
	public String getValueAsString(int iIfFloatPrecision) {
		return getValueAsString();
	}
	
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

	@Override
	public String getReport() {
		return getUniqueCmdId()+" = "+bCurrent;
	}

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
	public BoolTogglerCmdField setObjectRawValue(Object objValue) {
		if(objValue==null)throw new PrerequisitesNotMetException(BoolTogglerCmdField.class.getSimpleName()+" can't be set to null!");
		
		if(objValue instanceof BoolTogglerCmdField){
			this.bCurrent = ((BoolTogglerCmdField)objValue).b();
		}else
		if(objValue instanceof String){
			String str=(String)objValue;
			if(str.equalsIgnoreCase("true")){
				this.bCurrent = true;
			}else
			if(str.equalsIgnoreCase("false")){
				this.bCurrent = false;
			}else{
				throw new PrerequisitesNotMetException("invalid boolean value", str);
			}
		}else
		{
			this.bCurrent = (Boolean)objValue; //default is expected type
		}
//		if(bConstructed)
		super.setObjectRawValue(this.bCurrent);
		
		if(bConstructed){
			if(isChangedAndRefresh()){
				if(bDoCallOnChange){
					if(this.caller==null){
						MsgI.i().warn("null caller for "+this.getReport(), this);
//						throw new PrerequisitesNotMetException("null caller for "+this.getReport());
					}else{
						CallQueueI.i().addCall(this.caller);
					}
				}
			}
		}
		return this;
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
//		return this;
//	}
	
//	@Override
////	public BoolTogglerCmdField setObjectValue(CommandsDelegator.CompositeControl ccCD, Object objValue) {
//	public BoolTogglerCmdField setObjectRawValue(Object objValue) {
//		this.set((Boolean)objValue);
////		super.setObjectValue(ccCD,objValue);
//		super.setObjectRawValue(objValue);
//		return this;
//	}

//	@Override
//	public String getVarId() {
//		if(strVarId==null){
//			super.setUniqueCmdId(ReflexFillI.i().createIdentifierWithFieldName(getOwner(), this, true));
//		}
//		return strVarId;
//	}

	@Override
	public Object getRawValue() {
		return this.bCurrent;
	}

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
	
	public BoolTogglerCmdField setCallOnChange(CallableX caller){
		if(!bDoCallOnChange){
			// to avoid developer forgot already configured to call nothing
			throw new PrerequisitesNotMetException("was set to call nothing already!",this,getHelp());
		}
		
		this.caller=caller;
		return this;
	}
	
	/**
	 * In case just the main boolean value will be used.
	 * @return 
	 */
	public BoolTogglerCmdField setCallNothingOnChange(){
		// to avoid developer forgotten previously configured caller
		PrerequisitesNotMetException.assertNotAlreadySet("caller", caller, null, this, getHelp());
//		if(caller!=null){
////			throw new PrerequisitesNotMetException("caller already set!",this,getHelp());
//		}
		
//		if(!bDoCallOnChange){
//			// to avoid double setup
//			throw new PrerequisitesNotMetException("will already call nothing!",this,getHelp());
//		}
		
		bDoCallOnChange=false;
		return this;
	}
	
	@Override
	public String getVariablePrefix() {
		return "Bool";
	}

	public boolean isEqualToAndEnabled(BoolTogglerCmdField btg) {
		if(this==btg){
			return b();
		}
		
		return false;
	}

//	@Override
//	public BoolTogglerCmdField setHelp(String str) {
//		this.strHelp = str;
//		return this;
//	}

//	public static void removeFromList(BoolTogglerCmdField bt) {
//		abtgList.remove(bt);
//	}
	@Override
	protected BoolTogglerCmdField getThis() {
		return this;
	}
	
	/**
	 * 
	 * @return current status
	 */
	public boolean toggle(){
		setObjectRawValue(!getBoolean());
		return getBoolean();
	}
	
	private static String strCodePrefixDefault="btg";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
}
