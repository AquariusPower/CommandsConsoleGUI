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

import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class is intended to be used only as class field variables.
 * It automatically creates console variables.
 * 
 * TODO set limit min and max, optinally throw exception or just fix the value to not over/underflow
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class IntLongVarField extends NumberVarFieldAbs<Long,IntLongVarField>{
//	private static boolean	bConfigured;
//	private static IHandleExceptions	ihe = SimpleHandleExceptionsI.i();
//	private static String	strCodePrefix = "ilv";
//	private static ArrayList<IntLongVarField> ailvList = new ArrayList<IntLongVarField>();
	
//	private Long lValue;
//	private Long	lMin;
//	private Long	lMax;
	
//	public static void configure(IHandleExceptions ihe){
//		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
//		IntLongVarField.ihe=ihe;
//		bConfigured=true;
//	}
	
	public IntLongVarField(IReflexFillCfg rfcfgOwnerUseThis, IntLongVarField ilv) {
		this(rfcfgOwnerUseThis, ilv.getValue(),null);
	}
	public IntLongVarField(IReflexFillCfg rfcfgOwnerUseThis, Integer iInitialValue, String strHelp) {
		this(rfcfgOwnerUseThis, iInitialValue==null?null:iInitialValue.longValue(), strHelp);
	}
	/**
	 * @param lInitialValue if null, the variable will be removed from console vars.
	 */
	public IntLongVarField(IReflexFillCfg rfcfgOwnerUseThis, Long lInitialValue, String strHelp) {
//		if(rfcfgOwnerUseThis!=null)ailvList.add(this); //only fields allowed
//		super(rfcfgOwnerUseThis!=null); //only fields allowed
		super(rfcfgOwnerUseThis, lInitialValue, Long.class);
//		this.setOwner(rfcfgOwnerUseThis);
//		setObjectRawValue(lInitialValue);
		setHelp(strHelp);
//		this.bReflexingIdentifier = rfcfgOwnerUseThis!=null;
		constructed();
	}
	
//	public IntLongVarField setValue(Long l){
//		this.lValue=l;
//		if(super.getConsoleVarLink()!=null)setObjectRawValue(this.lValue);
//return getThis();
//	}
	
	public IntLongVarField setLong(Long l) {
		this.setObjectRawValue(l);
		return getThis();
	}
	
	public IntLongVarField setInteger(Integer i) {
		this.setObjectRawValue(i);
		return getThis();
	}
	
	@Override
//	public IntLongVarField setObjectValue(CommandsDelegator.CompositeControl cc, Object objValue) {
	public IntLongVarField setObjectRawValue(Object objValue) {
		if(objValue == null){
			//keep this empty skipper nullifier
		}else
		if(objValue instanceof Integer){
			objValue=( ((Integer)objValue).longValue() );
		}else
		if(objValue instanceof Long){
			objValue=( ((Long)objValue) );
		}else
		if(objValue instanceof IntLongVarField){
			objValue=( ((IntLongVarField)objValue).getValue() );
		}else
		if(objValue instanceof Float){
			objValue=( (long)Math.round(((Float)objValue).doubleValue()) );
		}else
		if(objValue instanceof Double){
			objValue=( (long)Math.round((Double)objValue) );
		}else
		if(objValue instanceof FloatDoubleVarField){
			objValue=( (long)Math.round(((FloatDoubleVarField)objValue).getDouble()) );
		}else
		if(objValue instanceof String){
			objValue=( Long.parseLong((String)objValue) );
		}else{
			throw new PrerequisitesNotMetException("unsupported class type", objValue.getClass());
		}
		
//		if(lMin!=null && (lValue==null || lValue<lMin))lValue=lMin;
//		if(lMax!=null && (lValue==null || lValue>lMax))lValue=lMax;
		
//		super.setObjectValue(cc,objValue);
		super.setObjectRawValue(objValue);
		
		return getThis();
	}
	
//	public IntLongVarField setMinMax(Long lMin,Long lMax) {
//		setMin(lMin);
//		setMax(lMax);
//return getThis();
//	}
//	public IntLongVarField setMin(Long lMin) {
//		this.lMin=lMin;
//return getThis();
//	}
//	public IntLongVarField setMax(Long lMax) {
//		this.lMax=lMax;
//return getThis();
//	}
//	public Long getMin() {
//		return lMin;
//	}
//	public Long getMax() {
//		return lMax;
//	}
	
//	@Override
//	public String getHelp(){
//		return strHelp==null?"":strHelp;
//	}
	
//	@Override
//	public String getCodePrefixVariant() {
//		return IntLongVarField.getCodePrefixDefault();
//	}

	public Integer getInt() {
		if(getValue()==null)return null;
		return getValue().intValue();
	}
	public int intValue() {
		return getValue().intValue();
	}
	
	public Long getLong() {
		return getValue();
	}
	public long longValue(){
		return getValue().longValue();
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

//	@Override
//	public String getReport() {
//		return getUniqueVarId()+" = "+getLong();
//	}

//	@Override
//	public Object getRawValue() {
//		return getLong();
//	}

//	@Override
//	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
//		this.vivo=vivo;
//	}
	
//	@Override
//	public String toString() {
//		if(lValue==null)return null;
//		return ""+lValue;
//	}
//	@Override
//	public String getValueAsString() {
//		return ""+getRawValue();
//	}
//	@Override
//	public String getValueAsString(int iIfFloatPrecision) {
//		return getValueAsString();
//	}

	@Override
	public String getVariablePrefix() {
		return "Int";
	}
	
	@Override
	protected IntLongVarField getThis() {
		return this;
	}

//	public static String getCodePrefixDefault() {
//		return "ilv";
//	}
	private String strCodePrefixDefault="ilv";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
}
