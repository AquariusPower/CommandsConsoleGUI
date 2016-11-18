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

package com.github.commandsconsolegui.spAppOs.misc;

import java.util.Arrays;

/**
 * To easily detect if the reference value has changed using its hash.
 * 
 * TODO what about hash clash? could make this method useless if absolute precision is required?
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class RefHolder<T> implements IDebugReport{
	
	private T objRefHolded;
	private int	iHash;
	private long lCount;
	private long	lLastChangeTime;
	@SuppressWarnings("unused") private StackTraceElement[] steDbgLastSetAt; //used mainly during debugs
	
	/**
	 * 
	 * @param objRef should hold some ref, if not must be explicitly set to null
	 */
	public RefHolder(T objRef){
		setHolded(objRef);
//		this.objRef=objRef;
	}
	
	public T getHolded(){
		return objRefHolded;
	}
	/**
	 * like {@link #getHolded()}
	 * @return
	 */
	public T getRef(){
		return objRefHolded;
	}
	
	public boolean isChangedAndUpdateHash(){
		return isChangedAndUpdateHash(objRefHolded);
	}
	public boolean isChangedAndUpdateHash(T holdedNew){
		setHolded(holdedNew);
//		this.objRef = objRefNew;
		
		int iHashTmp = getHolded().hashCode();
		if(iHashTmp!=iHash){
			iHash=iHashTmp;
			lCount++;
			steDbgLastSetAt=Thread.currentThread().getStackTrace();
			lLastChangeTime=System.nanoTime();
			return true;
		}
		
		return false;
	}
	public void setHolded(Object holdedNew){
		if(this.objRefHolded!=null){
			if(this.objRefHolded.getClass()!=holdedNew.getClass()){
				throw new PrerequisitesNotMetException("must be of the same concrete class", this.objRefHolded, this.objRefHolded.getClass(), holdedNew, holdedNew.getClass());
			}
		}
		
//		isChangedAndUpdateHash((T)objRef);
		this.objRefHolded = (T)holdedNew;
		
//		if(this.holded instanceof IHolded){
//			((IHolded)this.holded).setHolder(this);
//		}
	}
	
	public long getChangedCount(){
		return lCount;
	}
	
	public long getLastChangeTimeNano(){
		return lLastChangeTime;
	}
	
	@Override
	public String getFailSafeDebugReport(){
		String str="";
		
		str+=MiscI.i().asReport("RefHolded",objRefHolded,true);
		
		return str;
	}

	@Override
	public String toString() {
		return String.format(
			"RefHolder [iHash=%s, lCount=%s, lLastChangeTime=%s, steDbgLastSetAt=%s], objRefHolded=%s",
			iHash, lCount, lLastChangeTime, Arrays.toString(steDbgLastSetAt), getFailSafeDebugReport());
	}

	
}
