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

import com.github.commandsconsolegui.cmd.VarIdValueOwnerData;
import com.github.commandsconsolegui.misc.HandleExceptionsRaw;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class is intended to be used only as class field variables.
 * It automatically creates console variables.
 *
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class FloatDoubleVarField extends VarCmdFieldAbs{
	protected static boolean	bConfigured;
	protected static IHandleExceptions	ihe = HandleExceptionsRaw.i();
	protected static String	strCodePrefixVariant = "fdv";
//	protected static ArrayList<FloatDoubleVarField> afdvList = new ArrayList<FloatDoubleVarField>();
	protected Double dValue;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		FloatDoubleVarField.ihe=ihe;
		bConfigured=true;
	}
	
	public FloatDoubleVarField(IReflexFillCfg rfcfgOwnerUseThis, FloatDoubleVarField fdv, String strHelp) {
		this(rfcfgOwnerUseThis, fdv.dValue, strHelp);
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
		super(rfcfgOwnerUseThis!=null); //only fields allowed
		this.rfcfgOwner=rfcfgOwnerUseThis;
		this.dValue=dInitialValue;
		this.strHelp=strHelp;
		this.bReflexingIdentifier = rfcfgOwnerUseThis!=null;
	}
	
	@Override
	public void setObjectValue(Object objValue) {
		if(objValue instanceof Double){
			dValue = ((Double)objValue);
		}else
		if(objValue instanceof FloatDoubleVarField){
			dValue = ((FloatDoubleVarField)objValue).dValue;
		}else
		{
			dValue = ((Float)objValue).doubleValue();
		}
		
		if(vivo!=null)vivo.setObjectValue(objValue);
	}

	@Override
	public String getCodePrefixVariant() {
		return FloatDoubleVarField.strCodePrefixVariant ;
	}

	@Override
	public IReflexFillCfg getOwner() {
		return rfcfgOwner;
	}
	
	public Float getFloat(){
		if(dValue==null)return null;
		return dValue.floatValue();
	}
	public float floatValue(){
		return dValue.floatValue();
	}
	public Double getDouble(){
		return dValue;
	}
	public double doubleValue(){
		return dValue.doubleValue();
	}
	
//	public static ArrayList<FloatDoubleVarField> getListCopy(){
//		return new ArrayList<FloatDoubleVarField>(afdvList);
//	}
	
	@Override
	public String getVarId() {
//		if(strVarId==null)strVarId=ReflexFillI.i().getVarId(rfcfgOwner, strCodePrefixVariant, this, -1);
		if(strVarId==null){
			super.setId(ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner, this, true));
		}
		
		return strVarId;
	}

	@Override
	public String getReport() {
		return getVarId()+" = "+MiscI.i().fmtFloat(getDouble(),3);
	}

	@Override
	public Object getValueRaw() {
		return getDouble();
	}

	@Override
	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
		this.vivo=vivo;
	}
	
	@Override
	public String toString() {
		if(dValue==null)return null;
		return MiscI.i().fmtFloat(dValue,3);
	}

	@Override
	public String getHelp() {
		return strHelp==null?"":strHelp;
	}

	@Override
	public String getVariablePrefix() {
		return "Float";
	}

}
