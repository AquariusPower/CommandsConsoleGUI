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

package misc;

import java.util.ArrayList;

import misc.ReflexFill.IReflexFillCfg;
import misc.ReflexFill.IReflexFillCfgVariant;
import console.VarIdValueOwner;
import console.VarIdValueOwner.IVarIdValueOwner;

/**
 * This class is intended to be used only as class field variables.
 *
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class FloatingDoubleVar implements IReflexFillCfgVariant, IVarIdValueOwner{
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
	private static String	strCodePrefixVariant = "fdv";
	private static ArrayList<FloatingDoubleVar> afdvList = new ArrayList<FloatingDoubleVar>();
	Double dValue;
	private IReflexFillCfg	rfcfgOwner;
	private String	strVarId;
	private VarIdValueOwner	vivo;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		FloatingDoubleVar.ihe=ihe;
		bConfigured=true;
	}
	
	public FloatingDoubleVar(IReflexFillCfg rfcfgOwnerUseThis, FloatingDoubleVar fdv) {
		this(rfcfgOwnerUseThis, fdv.dValue);
	}
	public FloatingDoubleVar(IReflexFillCfg rfcfgOwnerUseThis, Float fInitialValue) {
		this(rfcfgOwnerUseThis, fInitialValue==null?null:fInitialValue.doubleValue());
	}
	/**
	 * @param rfcfgOwnerUseThis use null if this is not a class field, but a local variable
	 * @param dInitialValue if null, the variable will be removed from console vars.
	 */
	public FloatingDoubleVar(IReflexFillCfg rfcfgOwnerUseThis, Double dInitialValue) {
		if(rfcfgOwnerUseThis!=null)afdvList.add(this); //only fields allowed
		this.rfcfgOwner=rfcfgOwnerUseThis;
		this.dValue=dInitialValue;
	}
	
	@Override
	public void setObjectValue(Object objValue) {
		if(objValue instanceof Double){
			dValue = ((Double)objValue);
		}else
		if(objValue instanceof FloatingDoubleVar){
			dValue = ((FloatingDoubleVar)objValue).dValue;
		}else
		{
			dValue = ((Float)objValue).doubleValue();
		}
		
		if(vivo!=null)vivo.setObjectValue(objValue);
	}

	@Override
	public String getCodePrefixVariant() {
		return FloatingDoubleVar.strCodePrefixVariant ;
	}

	@Override
	public IReflexFillCfg getOwner() {
		return rfcfgOwner;
	}
	
	public Float getFloat(){
		if(dValue==null)return null;
		return dValue.floatValue();
	}
	public Double getDouble(){
		return dValue;
	}
	public static ArrayList<FloatingDoubleVar> getListCopy(){
		return new ArrayList<FloatingDoubleVar>(afdvList);
	}
	
	@Override
	public String getVarId() {
		if(strVarId==null)strVarId=ReflexFill.i().getVarId(rfcfgOwner, strCodePrefixVariant, this);
		return strVarId;
	}

	@Override
	public String getReport() {
		return getVarId()+" = "+Misc.i().fmtFloat(getDouble(),3);
	}

	@Override
	public Object getValueRaw() {
		return getDouble();
	}

	@Override
	public void setConsoleVarLink(VarIdValueOwner vivo) {
		this.vivo=vivo;
	}
	
	@Override
	public String toString() {
		if(dValue==null)return null;
		return Misc.i().fmtFloat(dValue,3);
	}
}
