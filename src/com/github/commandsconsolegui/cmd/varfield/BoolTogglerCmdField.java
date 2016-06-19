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

package com.github.commandsconsolegui.cmd.varfield;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.VarIdValueOwnerData;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.HandleExceptionsRaw;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class can provide automatic boolean console command options to be toggled.<br>
 * You just need to create the variable properly and it will be automatically recognized.<br>
 * 
 * It is intended to change internal states, not to ex. toggle user interfaces, for that use
 * a normal command.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class BoolTogglerCmdField extends VarCmdFieldAbs{
	public static final String strTogglerCodePrefix="btg";
	protected static ArrayList<BoolTogglerCmdField> abtgList = new ArrayList<BoolTogglerCmdField>();
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
	
	protected boolean bPrevious;
	protected boolean bCurrent;
	protected String strCommand;
	protected IReflexFillCfg	rfcfgOwner;
	protected String strHelp="";

	protected String	strReflexFillCfgCodePrefixVariant;
	protected VarIdValueOwnerData	vivo;
	protected String	strVarId;
	protected boolean bDoCallOnChange = true;
	protected Callable<Boolean>	caller;
	protected boolean	bConstructed;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		if(ihe==null)throw new NullPointerException("invalid instance for "+IHandleExceptions.class.getName()); // KEEP ON TOP
		BoolTogglerCmdField.ihe=ihe;
		bConfigured=true;
	}
	
	public static ArrayList<BoolTogglerCmdField> getListCopy(){
		return new ArrayList<BoolTogglerCmdField>(abtgList);
	}
	
	private BoolTogglerCmdField(){
		abtgList.add(this);
	}
	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitValue){
		this( rfcfgOwnerUseThis,  bInitValue, BoolTogglerCmdField.strTogglerCodePrefix, "");
	}
	/**
	 * 
	 * @param rfcfgOwnerUseThis always pass as "this", the very class must implement it.
	 * @param bInitValue
	 * @param strReflexFillCfgCodePrefixVariant if null, will be default. Can be emtpy "".
	 */
	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitValue, String strReflexFillCfgCodePrefixVariant){
		this( rfcfgOwnerUseThis,  bInitValue,  strReflexFillCfgCodePrefixVariant, "");
	}
	/**
	 * 
	 * @param rfcfgOwnerUseThis always pass as "this", the very class must implement it.
	 * @param bInitialValue
	 * @param strReflexFillCfgCodePrefixVariant if null, will be default. Can be emtpy "".
	 * @param strHelp
	 */
	public BoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitialValue, String strReflexFillCfgCodePrefixVariant, String strHelp){
		this();
		
//		ReflexFill.assertAndGetField(rfcfgOwnerUseThis, this);
		
		this.strReflexFillCfgCodePrefixVariant=strReflexFillCfgCodePrefixVariant;
		if(this.strReflexFillCfgCodePrefixVariant==null){
			this.strReflexFillCfgCodePrefixVariant=BoolTogglerCmdField.strTogglerCodePrefix;
		}
		
		this.rfcfgOwner=rfcfgOwnerUseThis;
		this.strHelp=strHelp;
		set(bInitialValue);
		
		this.bConstructed=true;
	}
//	public BoolTogglerCmd(boolean bInitValue, String strCustomCmdId){
//		this();
//		set(bInitValue);
//		setCustomCmdId(strCustomCmdId);
//	}
	
	/**
	 * sets the command identifier that user will type in the console
	 * @param strCmd
	 */
	public void setCustomCmdId(String strCmd) {
		/**
		 * must be an exception as it can have already been read/collected with automatic value.
		 */
		if(this.strCommand!=null)throw new NullPointerException("asked for '"+strCmd+"' but was already set to: "+this.strCommand);
		this.strCommand=strCmd;
	}
	public String getCmdId(){
		if(strCommand!=null)return strCommand;
		strCommand = ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner,this);
		return strCommand;
	}
	
	@Override
	public String getHelp(){
		return strHelp==null?"":strHelp;
	}
	
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
	protected boolean isChangedAndRefresh(){
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
	public String toString() {
		return ""+bCurrent;
	}

//	@Override
//	public int getReflexFillCfgVariant() {
//		return iReflexFillCfgVariant;
//	}

	@Override
	public String getCodePrefixVariant() {
		return strReflexFillCfgCodePrefixVariant;
	}
	
	public String getCmdIdAsCommand(boolean bForceState) {
		return getCmdId()+" "+bForceState;
	}

	@Override
	public IReflexFillCfg getOwner() {
		return rfcfgOwner;
	}

	public IConsoleCommandListener getOwnerAsCmdListener() {
		if(rfcfgOwner instanceof CommandsDelegator){
			return GlobalCommandsDelegatorI.i().getPseudoListener();
		}
		
		if(rfcfgOwner instanceof IConsoleCommandListener){
			return (IConsoleCommandListener)rfcfgOwner;
		}
		
		return null;
	}

	@Override
	public String getReport() {
		return getCmdId()+" = "+bCurrent;
	}

	public void set(boolean b){
		set(b,true);
	}
	public void set(boolean b, boolean bUseCallQueue){
		this.bCurrent=b;
		
		if(bConstructed){
			if(isChangedAndRefresh()){
				if(bDoCallOnChange){
					if(this.caller==null){
						throw new PrerequisitesNotMetException("null caller for "+this.getReport());
					}
					
					CallQueueI.i().appendCall(this.caller);
				}
			}
		}
	}
	
	@Override
	public void setObjectValue(Object objValue) {
		this.set((Boolean)objValue);
		if(vivo!=null)vivo.setObjectValue(objValue);
	}

	@Override
	public String getVarId() {
		if(strVarId!=null)return strVarId;
		strVarId = ReflexFillI.i().getVarId(
				rfcfgOwner, BoolTogglerCmdField.strTogglerCodePrefix, this, -1);
//		return getCmdId();
		return strVarId;
	}

	@Override
	public Object getValueRaw() {
		return this.bCurrent;
	}

	@Override
	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
		this.vivo=vivo;
	}
	
//	public static String getPrefix() {
//		return rfcfgOwner.;
//	}
//	
//	public static String getSuffix() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	public BoolTogglerCmdField setCallOnChange(Callable<Boolean> caller){
		this.caller=caller;
		return this;
	}
	
	/**
	 * In case just the boolean status will be necessary.
	 * @return 
	 */
	public BoolTogglerCmdField setCallNothingOnChange(){
		bDoCallOnChange=false;
		return this;
	}
}
