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

import com.github.commandsconsolegui.misc.HandleExceptionsRaw;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class is intended to be used only as class field variables.
 * It automatically creates console variables.
 *
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class FloatDoubleVarField extends NumberVarFieldAbs<Double,FloatDoubleVarField>{
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
//	private static String	strCodePrefix = "fdv";
//	private static ArrayList<FloatDoubleVarField> afdvList = new ArrayList<FloatDoubleVarField>();
//	private Double dValue = null;
//	private Double dMin = null;
//	private Double dMax = null;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		FloatDoubleVarField.ihe=ihe;
		bConfigured=true;
	}
	
	public FloatDoubleVarField(IReflexFillCfg rfcfgOwnerUseThis, FloatDoubleVarField fdv, String strHelp) {
		this(rfcfgOwnerUseThis, fdv.getValue(), strHelp);
	}
	public FloatDoubleVarField(IReflexFillCfg rfcfgOwnerUseThis, Float fInitialValue, String strHelp) {
		this(rfcfgOwnerUseThis, fInitialValue==null?null:fInitialValue.doubleValue(), strHelp);
	}
	/**
	 * @param rfcfgOwnerUseThis use null if this is not a class field, but a local variable
	 * @param dInitialValue if null, the variable will be removed from console vars.
	 */
	public FloatDoubleVarField(IReflexFillCfg rfcfgOwnerUseThis, Double dInitialValue, String strHelp) {
//		if(rfcfgOwnerUseThis!=null)afdvList.add(this); //only fields allowed
//		super(rfcfgOwnerUseThis!=null); //only fields allowed
		super(rfcfgOwnerUseThis); //only fields allowed
//		setOwner(rfcfgOwnerUseThis);
		setObjectRawValue(dInitialValue);
//		this.dValue=dInitialValue;
		setHelp(strHelp);
//		this.bReflexingIdentifier = rfcfgOwnerUseThis!=null;
		constructed();
	}
	
//	public FloatDoubleVarField setValue(Double d){
//		this.dValue=d;
//		if(super.getConsoleVarLink()!=null)setObjectRawValue(this.dValue);
//		return this;
//	}
	
	@Override
//	public FloatDoubleVarField setObjectValue(CommandsDelegator.CompositeControl ccCD, Object objValue) {
	public FloatDoubleVarField setObjectRawValue(Object objValue) {
		if(objValue == null){
			setValue( null );
		}else
		if(objValue instanceof Float){
			setValue( ((Float)objValue).doubleValue() );
		}else
		if(objValue instanceof Double){
			setValue( ((Double)objValue) );
		}else
		if(objValue instanceof FloatDoubleVarField){
			setValue( ((FloatDoubleVarField)objValue).getValue() );
		}else
//		if(objValue instanceof IntLongVarField){
//			setValue( ((IntLongVarField)objValue).getLong().doubleValue() );
//		}else
		if(objValue instanceof String){
			setValue( Double.parseDouble((String)objValue) );
		}else{
			throw new PrerequisitesNotMetException("unsupported class type", objValue.getClass());
		}
//		}else
//		{
//			if(objValue!=null){
//				setValue( ((Float)objValue).doubleValue() );
//			}else{
//				setValue( null );
//			}
//		}
		
//		super.setObjectValue(ccCD,objValue);
		super.setObjectRawValue(objValue);
		
		return this;
	}

//	@Override
//	public String getCodePrefixVariant() {
//		return FloatDoubleVarField.getCodePrefixDefault();
//	}

	public Float getFloat(){
		if(getValue()==null)return null;
		return getValue().floatValue();
	}
	public float floatValue(){
		return getValue().floatValue();
	}
	public float f() {
		return getFloat();
	}
	
	public Double getDouble(){
		return getValue();
	}
	public double doubleValue(){
		return getValue().doubleValue();
	}
	
//	public static ArrayList<FloatDoubleVarField> getListCopy(){
//		return new ArrayList<FloatDoubleVarField>(afdvList);
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
		return getUniqueVarId()+" = "+MiscI.i().fmtFloat(getDouble(),3);
	}

	@Override
	public Object getRawValue() {
		return getDouble();
	}

//	@Override
//	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
//		this.vivo=vivo;
//	}
	
//	@Override
//	public String toString() {
//		if(dValue==null)return null;
//		return MiscI.i().fmtFloat(dValue,3);
//	}
	@Override
	public String getValueAsString() {
		return getValueAsString(3);
	}
	@Override
	public String getValueAsString(int iIfFloatPrecision) {
		return MiscI.i().fmtFloat(getDouble(),iIfFloatPrecision);
	}

//	@Override
//	public String getHelp() {
//		return strHelp==null?"":strHelp;
//	}

	@Override
	public String getVariablePrefix() {
		return "Float";
	}

//	public FloatDoubleVarField setMinMax(Double dMin,Double dMax) {
//		setMin(dMin);
//		setMax(dMax);
//		return this;
//	}
//	public FloatDoubleVarField setMin(Double dMin) {
//		this.dMin=dMin;
//		return this;
//	}
//	public FloatDoubleVarField setMax(Double dMax) {
//		this.dMax=dMax;
//		return this;
//	}
//	public Double getMin() {
//		return dMin;
//	}
//	public Double getMax() {
//		return dMax;
//	}
	
	@Override
	protected FloatDoubleVarField getThis() {
		return this;
	}

//	public static String getCodePrefixDefault() {
//		return "fdv";
//	}
	private static String strCodePrefixDefault="fdv";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}

}
