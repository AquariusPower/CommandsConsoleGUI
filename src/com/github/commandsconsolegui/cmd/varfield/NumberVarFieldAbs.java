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
 * @param <VAL>
 * @param <THIS>
 */
public abstract class NumberVarFieldAbs<VAL, THIS extends NumberVarFieldAbs<VAL,THIS>> extends VarCmdFieldAbs<VAL,THIS> {//implements Comparable<O>{
//	private O nValue;
	private VAL nMin;
	private VAL nMax;
	
	public NumberVarFieldAbs(IReflexFillCfg rfcfgOwner, VAL valueDefault, Class<VAL> clValueTypeConstraint) {
		super(rfcfgOwner, EVarCmdMode.Var, valueDefault, clValueTypeConstraint);
	}

	public VAL getMin() {
		return nMin;
	}

	public THIS setMin(VAL nMin) {
		PrerequisitesNotMetException.assertNotAlreadySet("min", this.nMin, nMin, this);
		this.nMin = nMin;
		return getThis();
	}

	public VAL getMax() {
		return nMax;
	}

	public THIS setMax(VAL nMax) {
		PrerequisitesNotMetException.assertNotAlreadySet("max", this.nMax, nMax, this);
		this.nMax = nMax;
		return getThis();
	}

	public THIS setMinMax(VAL nMin,VAL nMax) {
		setMin(nMin);
		setMax(nMax);
		return getThis();
	}
	
	/**
	 * TODO add console info message in case of under/overflow?
	 */
	@Override
	public THIS setObjectRawValue(Object objValue) {
		super.setObjectRawValue(objValue); //this may put a call on queue
		
		/**
		 * these will also re-put the same call on queue, just that, whenever it is run
		 * the value will be already fixed within limits  
		 */
		
		// so if null, the preference will be the min value
		if( getMin()!=null && (getValue()==null || cmpWith(getMin())<0) )setValue(getMin());
		
		if( getMax()!=null && (getValue()==null || cmpWith(getMax())>0) )setValue(getMax());
		
		return getThis();
	}
	
	public THIS setNumber(Number n){
		this.setObjectRawValue(n);
		return getThis();
	}
	
	private int cmpWith(VAL nOther){
		if(getRawValue() instanceof Double){
			return ((Double)getRawValue()).compareTo((Double)nOther);
		}else
		if(getRawValue() instanceof Float){
			return ((Float)getRawValue()).compareTo((Float)nOther);
		}else
		if(getRawValue() instanceof Integer){
			return ((Integer)getRawValue()).compareTo((Integer)nOther);
		}else
		if(getRawValue() instanceof Long){
			return ((Long)getRawValue()).compareTo((Long)nOther);
		}else{
			throw new PrerequisitesNotMetException("unsupported type", nOther.getClass(), this);
		}
	}
	
//	public O getValue() {
//		return nValue;
//	}

//	protected void setValue(O nValue) {
//		this.nValue = nValue;
//	}

//	@Override
//	public int compareTo(O o) {
//		return 0;
//	}
}
