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

package com.github.commandsconsolegui.misc;

import java.util.ArrayList;

import com.github.commandsconsolegui.console.IConsoleCommandListener;
import com.github.commandsconsolegui.console.VarIdValueOwner;
import com.github.commandsconsolegui.console.VarIdValueOwner.IVarIdValueOwner;
import com.github.commandsconsolegui.misc.ReflexFill.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFill.IReflexFillCfgVariant;

/**
 * This class can provide automatic boolean console command options to toggle.
 * You just need to create the variable properly and it will be automatically recognized.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class BoolTogglerCmd implements IReflexFillCfgVariant, IVarIdValueOwner{
	public static final String strTogglerCodePrefix="btg";
	protected static ArrayList<BoolTogglerCmd> abtgList = new ArrayList<BoolTogglerCmd>();
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
	
	protected boolean bPrevious;
	protected boolean bCurrent;
	protected String strCommand;
	protected IReflexFillCfg	rfcfgOwner;
	protected String strHelp="";

	private String	strReflexFillCfgCodePrefixVariant;
	private VarIdValueOwner	vivo;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		BoolTogglerCmd.ihe=ihe;
		bConfigured=true;
	}
	
	public static ArrayList<BoolTogglerCmd> getListCopy(){
		return new ArrayList<BoolTogglerCmd>(abtgList);
	}
	
	private BoolTogglerCmd(){
		abtgList.add(this);
	}
	public BoolTogglerCmd(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitValue){
		this( rfcfgOwnerUseThis,  bInitValue, BoolTogglerCmd.strTogglerCodePrefix, "");
	}
	/**
	 * 
	 * @param rfcfgOwnerUseThis always pass as "this", the very class must implement it.
	 * @param bInitValue
	 * @param strReflexFillCfgCodePrefixVariant
	 */
	public BoolTogglerCmd(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitValue, String strReflexFillCfgCodePrefixVariant){
		this( rfcfgOwnerUseThis,  bInitValue,  strReflexFillCfgCodePrefixVariant, "");
	}
	/**
	 * 
	 * @param rfcfgOwnerUseThis always pass as "this", the very class must implement it.
	 * @param bInitialValue
	 * @param strReflexFillCfgCodePrefixVariant
	 * @param strHelp
	 */
	public BoolTogglerCmd(IReflexFillCfg rfcfgOwnerUseThis, boolean bInitialValue, String strReflexFillCfgCodePrefixVariant, String strHelp){
		this();
		
//		ReflexFill.assertAndGetField(rfcfgOwnerUseThis, this);
		
		this.strReflexFillCfgCodePrefixVariant=strReflexFillCfgCodePrefixVariant;
		this.rfcfgOwner=rfcfgOwnerUseThis;
		this.strHelp=strHelp;
		set(bInitialValue);
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
	protected void setCustomCmdId(String strCmd) {
		/**
		 * must be an exception as it can have already been read/collected with automatic value.
		 */
		if(this.strCommand!=null)throw new NullPointerException("asked for '"+strCmd+"' but was already set to: "+this.strCommand);
		this.strCommand=strCmd;
	}
	public String getCmdId(){
		if(strCommand!=null)return strCommand;
		strCommand = ReflexFill.i().createIdentifierWithFieldName(rfcfgOwner,this);
		return strCommand;
	}
	
	public String getHelp(){
		return strHelp;
	}
	
	public boolean get(){return bCurrent;}
	public boolean getBoolean(){return bCurrent;}
	public boolean getBool(){return bCurrent;}
	public boolean is(){return bCurrent;}
	public boolean b(){return bCurrent;}
	
	/**
	 * @return true if value changed
	 */
	public boolean checkChangedAndUpdate(){
		if(bCurrent != bPrevious){
			bPrevious=bCurrent;
			return true;
		}
		return false;
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
		this.bCurrent=b;
	}
	
	@Override
	public void setObjectValue(Object objValue) {
		this.set((Boolean)objValue);
		if(vivo!=null)vivo.setObjectValue(objValue);
	}

	@Override
	public String getVarId() {
		return getCmdId();
	}

	@Override
	public Object getValueRaw() {
		return this.bCurrent;
	}

	@Override
	public void setConsoleVarLink(VarIdValueOwner vivo) {
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
}
