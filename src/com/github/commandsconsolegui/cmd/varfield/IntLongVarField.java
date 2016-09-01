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

import com.github.commandsconsolegui.misc.HandleExceptionsRaw;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class is intended to be used only as class field variables.
 * It automatically creates console variables.
 * 
 * TODO set limit min and max, optinally throw exception or just fix the value to not over/underflow
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class IntLongVarField extends VarCmdFieldAbs<Long,IntLongVarField>{
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
	private static String	strCodePrefixVariant = "ilv";
//	private static ArrayList<IntLongVarField> ailvList = new ArrayList<IntLongVarField>();
	
	private Long lValue;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		IntLongVarField.ihe=ihe;
		bConfigured=true;
	}
	
	public IntLongVarField(IReflexFillCfg rfcfgOwnerUseThis, IntLongVarField ilv) {
		this(rfcfgOwnerUseThis, ilv.lValue,null);
	}
	public IntLongVarField(IReflexFillCfg rfcfgOwnerUseThis, Integer iInitialValue, String strHelp) {
		this(rfcfgOwnerUseThis, iInitialValue==null?null:iInitialValue.longValue(), strHelp);
	}
	/**
	 * @param rfcfgOwnerUseThis use null if this is not a class field, but a local variable
	 * @param lInitialValue if null, the variable will be removed from console vars.
	 */
	public IntLongVarField(IReflexFillCfg rfcfgOwnerUseThis, Long lInitialValue, String strHelp) {
//		if(rfcfgOwnerUseThis!=null)ailvList.add(this); //only fields allowed
//		super(rfcfgOwnerUseThis!=null); //only fields allowed
		super(rfcfgOwnerUseThis);
//		this.setOwner(rfcfgOwnerUseThis);
		this.lValue=lInitialValue;
		setHelp(strHelp);
//		this.bReflexingIdentifier = rfcfgOwnerUseThis!=null;
	}
	
//	public IntLongVarField setValue(Long l){
//		this.lValue=l;
//		if(super.getConsoleVarLink()!=null)setObjectRawValue(this.lValue);
//		return this;
//	}
	
	@Override
//	public IntLongVarField setObjectValue(CommandsDelegator.CompositeControl cc, Object objValue) {
	public IntLongVarField setObjectRawValue(Object objValue) {
		if(objValue instanceof Long){
			lValue = ((Long)objValue);
		}else
		if(objValue instanceof IntLongVarField){
			lValue = ((IntLongVarField)objValue).lValue;
		}else
		{
			lValue = ((Integer)objValue).longValue();
		}
		
//		super.setObjectValue(cc,objValue);
		super.setObjectRawValue(objValue);
		
		return this;
	}
	
//	@Override
//	public String getHelp(){
//		return strHelp==null?"":strHelp;
//	}
	
	@Override
	public String getCodePrefixVariant() {
		return IntLongVarField.strCodePrefixVariant ;
	}

	public Integer getInt() {
		if(lValue==null)return null;
		return lValue.intValue();
	}
	public int intValue() {
		return lValue.intValue();
	}
	
	public Long getLong() {
		return lValue;
	}
	public long longValue(){
		return lValue.longValue();
	}
	
//	public static ArrayList<IntLongVarField> getListCopy(){
//		return new ArrayList<IntLongVarField>(ailvList);
//	}
	
//	@Override
//	public String getVarId() {
////		if(strVarId==null)strVarId=ReflexFillI.i().getVarId(rfcfgOwner, strCodePrefixVariant, this, -1);
//		if(strVarId==null){
//			super.setUniqueCmdId(ReflexFillI.i().createIdentifierWithFieldName(getOwner(), this, true));
//		}
//		
//		return strVarId;
//	}

	@Override
	public String getReport() {
		return getVarId()+" = "+getLong();
	}

	@Override
	public Object getValueRaw() {
		return getLong();
	}

//	@Override
//	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
//		this.vivo=vivo;
//	}
	
	@Override
	public String toString() {
		if(lValue==null)return null;
		return ""+lValue;
	}

	@Override
	public String getVariablePrefix() {
		return "Int";
	}
	
	@Override
	protected IntLongVarField getThis() {
		return this;
	}
}
