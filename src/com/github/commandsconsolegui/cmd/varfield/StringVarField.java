/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower>
	
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

import com.github.commandsconsolegui.misc.HandleExceptionsRaw;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class is intended to be used only as class field variables.
 * It automatically creates console variables.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower>
 *
 */
public class StringVarField extends VarCmdFieldAbs<String,StringVarField>{
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
//	private static String	strCodePrefixVariant = "svf";
//	private static ArrayList<StringVarField> ailvList = new ArrayList<StringVarField>();
	
	String strValue;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		StringVarField.ihe=ihe;
		bConfigured=true;
	}
	
	public StringVarField(IReflexFillCfg rfcfgOwnerUseThis, StringVarField ilv,String strHelp) {
		this(rfcfgOwnerUseThis, ilv.strValue,strHelp);
	}
	/**
	 * @param rfcfgOwnerUseThis use null if this is not a class field, but a local variable
	 * @param lInitialValue if null, the variable will be removed from console vars.
	 */
	public StringVarField(IReflexFillCfg rfcfgOwnerUseThis, String strInitialValue,String strHelp) {
//		if(rfcfgOwnerUseThis!=null)ailvList.add(this); //only fields allowed
//		super(rfcfgOwnerUseThis!=null); //only fields allowed
		super(rfcfgOwnerUseThis);
//		this.setOwner(rfcfgOwnerUseThis);
		this.strValue=strInitialValue;
		this.setHelp(strHelp);
//		this.bReflexingIdentifier = rfcfgOwnerUseThis!=null;
	}
	
//	public StringVarField setValue(String str){
//		this.strValue=str;
//		if(super.getConsoleVarLink()!=null)setObjectRawValue(this.strValue);
//		return this;
//	}
	
	@Override
//	public StringVarField setObjectValue(CommandsDelegator.CompositeControl ccCD, Object objValue) {
	public StringVarField setObjectRawValue(Object objValue) {
		if(objValue instanceof StringVarField){
			strValue = ((StringVarField)objValue).strValue;
		}else
		{
			strValue = ""+objValue; //TODO too much permissive?
		}
		
//		super.setObjectValue(ccCD,objValue);
		super.setObjectRawValue(objValue);
		
		return this;
	}

//	@Override
//	public String getCodePrefixVariant() {
//		return StringVarField.getCodePrefixDefault();
//	}

//	public static ArrayList<StringVarField> getListCopy(){
//		return new ArrayList<StringVarField>(ailvList);
//	}
	
//	@Override
//	public String getVarId() {
//		if(strVarId==null){
//			super.setUniqueCmdId(ReflexFillI.i().createIdentifierWithFieldName(getOwner(), this, true));
//		}
//		
//		return strVarId;
//	}

	@Override
	public String getReport() {
		return getUniqueVarId()+" = "+(strValue==null?"null":"\""+strValue+"\"");
	}

	@Override
	public Object getValueRaw() {
		return strValue;
	}

//	@Override
//	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
//		this.vivo=vivo;
//	}
	
//	@Override
//	public String toString() {
//		if(strValue==null)return null;
//		return ""+strValue;
//	}
	@Override
	public String getValueAsString() {
		return getStringValue();
	}
	@Override
	public String getValueAsString(int iIfFloatPrecision) {
		return getValueAsString();
	}

	public String getStringValue() {
		return strValue;
	}

//	@Override
//	public String getHelp() {
//		return strHelp==null?"":strHelp;
//	}

	@Override
	public String getVariablePrefix() {
		return "String";
	}

	@Override
	protected StringVarField getThis() {
		return this;
	}

//	public static String getCodePrefixDefault() {
//		return "svf";
//	}
	private static String strCodePrefixDefault="svf";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
}
