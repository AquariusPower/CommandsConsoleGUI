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

import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <O>
 * @param <S>
 */
public abstract class NumberVarFieldAbs<O, S extends NumberVarFieldAbs<O,S>> extends VarCmdFieldAbs<O,S> {//implements Comparable<O>{
	private O nValue;
	private O nMin;
	private O nMax;
	
	public NumberVarFieldAbs(IReflexFillCfg rfcfgOwner) {
		super(rfcfgOwner,EVarCmdMode.Var);
	}

	public O getMin() {
		return nMin;
	}

	public S setMin(O nMin) {
		PrerequisitesNotMetException.assertNotAlreadySet("min", this.nMin, nMin, this);
		this.nMin = nMin;
		return getThis();
	}

	public O getMax() {
		return nMax;
	}

	public S setMax(O nMax) {
		PrerequisitesNotMetException.assertNotAlreadySet("max", this.nMax, nMax, this);
		this.nMax = nMax;
		return getThis();
	}

	public S setMinMax(O nMin,O nMax) {
		setMin(nMin);
		setMax(nMax);
		return getThis();
	}
	
	/**
	 * TODO add console info message in case of under/overflow?
	 */
	@Override
	public S setObjectRawValue(Object objValue) {
		if( getMin()!=null && (nValue==null || cmpWith(getMin())<0) )nValue=getMin();
		if( getMax()!=null && (nValue==null || cmpWith(getMax())>0) )nValue=getMax();
		
		return super.setObjectRawValue(objValue);
	}
	
	private int cmpWith(O nOther){
		if(nValue instanceof Double){
			return ((Double)nValue).compareTo((Double)nOther);
		}else
		if(nValue instanceof Float){
			return ((Float)nValue).compareTo((Float)nOther);
		}else
		if(nValue instanceof Integer){
			return ((Integer)nValue).compareTo((Integer)nOther);
		}else
		if(nValue instanceof Long){
			return ((Long)nValue).compareTo((Long)nOther);
		}else{
			throw new PrerequisitesNotMetException("unsupported type", nOther.getClass());
		}
	}
	
	protected O getValue() {
		return nValue;
	}

	protected void setValue(O nValue) {
		this.nValue = nValue;
	}

//	@Override
//	public int compareTo(O o) {
//		return 0;
//	}
}
