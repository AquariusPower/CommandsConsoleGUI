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
 * It automatically creates console variables.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class IntegerLongVar implements IReflexFillCfgVariant, IVarIdValueOwner{
	private static boolean	bConfigured;
	private static IHandleExceptions	ihe = HandleExceptionsRaw.i();
	private static String	strCodePrefixVariant = "ilv";
	private static ArrayList<IntegerLongVar> ailvList = new ArrayList<IntegerLongVar>();
	Long lValue;
	private IReflexFillCfg	rfcfgOwner;
	private String	strVarId;
	private VarIdValueOwner	vivo;
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		IntegerLongVar.ihe=ihe;
		bConfigured=true;
	}
	
	public IntegerLongVar(IReflexFillCfg rfcfgOwnerUseThis, IntegerLongVar ilv) {
		this(rfcfgOwnerUseThis, ilv.lValue);
	}
	public IntegerLongVar(IReflexFillCfg rfcfgOwnerUseThis, Integer iInitialValue) {
		this(rfcfgOwnerUseThis, iInitialValue==null?null:iInitialValue.longValue());
	}
	/**
	 * @param rfcfgOwnerUseThis use null if this is not a class field, but a local variable
	 * @param lInitialValue if null, the variable will be removed from console vars.
	 */
	public IntegerLongVar(IReflexFillCfg rfcfgOwnerUseThis, Long lInitialValue) {
		if(rfcfgOwnerUseThis!=null)ailvList.add(this); //only fields allowed
		this.rfcfgOwner=rfcfgOwnerUseThis;
		this.lValue=lInitialValue;
	}
	
	@Override
	public void setObjectValue(Object objValue) {
		if(objValue instanceof Long){
			lValue = ((Long)objValue);
		}else
		if(objValue instanceof IntegerLongVar){
			lValue = ((IntegerLongVar)objValue).lValue;
		}else
		{
			lValue = ((Integer)objValue).longValue();
		}
		
		if(vivo!=null)vivo.setObjectValue(objValue);
	}

	@Override
	public String getCodePrefixVariant() {
		return IntegerLongVar.strCodePrefixVariant ;
	}

	@Override
	public IReflexFillCfg getOwner() {
		return rfcfgOwner;
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
	
	public static ArrayList<IntegerLongVar> getListCopy(){
		return new ArrayList<IntegerLongVar>(ailvList);
	}
	
	@Override
	public String getVarId() {
		if(strVarId==null)strVarId=ReflexFill.i().getVarId(rfcfgOwner, strCodePrefixVariant, this);
		return strVarId;
	}

	@Override
	public String getReport() {
		return getVarId()+" = "+getLong();
	}

	@Override
	public Object getValueRaw() {
		return getLong();
	}

	@Override
	public void setConsoleVarLink(VarIdValueOwner vivo) {
		this.vivo=vivo;
	}
	
	@Override
	public String toString() {
		if(lValue==null)return null;
		return ""+lValue;
	}
	
}
