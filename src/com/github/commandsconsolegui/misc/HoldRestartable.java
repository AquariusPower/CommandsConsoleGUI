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

import java.util.ArrayList;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <T>
 */
public class HoldRestartable<T extends IRestartable> {
	private static ArrayList<HoldRestartable> ahrList=new ArrayList<HoldRestartable>();
	public static void revalidateAndUpdateAllRestartableHoldersFor(IRestartable irDiscarding, IRestartable irNew){
		for(HoldRestartable hr:new ArrayList<HoldRestartable>(ahrList)){
			if(hr.bDiscardSelf || DiscardableInstanceI.i().isSelfOrRecursiveOwnerBeingDiscarded(hr.objOwner)){
				ahrList.remove(hr);
			}else{
				if(hr.irRef==irDiscarding){
					hr.setRef(irNew);
				}
			}
		}
	}
	
	private IRestartable	irRef;
	private Object	objOwner;
	private boolean	bDiscardSelf;

	public HoldRestartable(Object objOwner){
		if(objOwner==null)throw new PrerequisitesNotMetException("invalid owner, use 'this'",this);
		this.objOwner = objOwner;
		ahrList.add(this);
	}
	public HoldRestartable(Object objOwner, IRestartable ir){
		this(objOwner);
		setRef(ir);
	}
	
//	public void requestDiscardSelf(Object objOwner, IRestartable ir){
	public void discardSelf(HoldRestartable<? extends IRestartable> hrReplacer){
		if(this.objOwner==null)throw new PrerequisitesNotMetException("owner is null", this, hrReplacer);
		if(this.irRef==null)throw new PrerequisitesNotMetException("holded is null", this, hrReplacer);
		
		if(DiscardableInstanceI.i().isSelfOrRecursiveOwnerBeingDiscarded(this.objOwner)){
			MsgI.i().devInfo("was already going to be discarded, this call is redundant", this, hrReplacer);
		}
		
		if(
				this.objOwner==hrReplacer.objOwner && 
				this.irRef==hrReplacer.irRef &&
				!DiscardableInstanceI.i().isSelfOrRecursiveOwnerBeingDiscarded(hrReplacer.objOwner)
		){
			this.bDiscardSelf=true;
//			this.objOwner=null;
		}else{
			throw new PrerequisitesNotMetException("cannot discard without a valid replacer",this,hrReplacer);
		}
	}
	
	public void setRef(IRestartable irRef){
		if(this.irRef!=null){
			if(!this.irRef.isBeingDiscarded()){
				throw new PrerequisitesNotMetException("cannot update the holded if it is not being discarded", this.irRef, irRef, this);
			}
			
			if(this.irRef.getClass()!=irRef.getClass()){
				throw new PrerequisitesNotMetException("old and new must be of the same concrete class", this.irRef, irRef, this);
			}
		}
		
		this.irRef=irRef; //there is no problem if it is the same object, help on making restarting logics less complex a bit
	}
	
	public T getRef(){
		return (T)irRef;
	}
	public boolean isSet() {
		return irRef!=null;
	}
}

