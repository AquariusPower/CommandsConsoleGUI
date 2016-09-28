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

package com.github.commandsconsolegui.misc;

/**
 * To easily detect if an object has changed.
 * 
 * TODO what about hash clash? could make this method useless if abs precision is required?
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class HashChangeHolder<T> {
	
	private T holded;
	private int	iHash;
	private long lCount;
	private long	lLastChangeTime;
	private StackTraceElement[] ste;

	public HashChangeHolder(T objRef){
		setHolded(objRef);
//		this.objRef=objRef;
	}
	
	public T getHolded(){
		return holded;
	}
	
	public boolean isChangedAndUpdateHash(){
		return isChangedAndUpdateHash(holded);
	}
	public boolean isChangedAndUpdateHash(T holdedNew){
		setHolded(holdedNew);
//		this.objRef = objRefNew;
		
		int iHashTmp = getHolded().hashCode();
		if(iHashTmp!=iHash){
			iHash=iHashTmp;
			lCount++;
			ste=Thread.currentThread().getStackTrace();
			lLastChangeTime=System.nanoTime();
			return true;
		}
		
		return false;
	}
	public void setHolded(Object holdedNew){
		if(this.holded!=null){
			if(this.holded.getClass()!=holdedNew.getClass()){
				throw new PrerequisitesNotMetException("must be of the same concrete class", this.holded, this.holded.getClass(), holdedNew, holdedNew.getClass());
			}
		}
		
//		isChangedAndUpdateHash((T)objRef);
		this.holded = (T)holdedNew;
		
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
}
