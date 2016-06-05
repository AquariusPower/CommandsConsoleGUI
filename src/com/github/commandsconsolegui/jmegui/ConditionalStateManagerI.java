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

package com.github.commandsconsolegui.jmegui;

import java.util.ArrayList;

import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.DeveloperMistakeException;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;

/**
 * This is actually a JME Application state.
 * It MUST not be disabled neither ended!
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConditionalStateManagerI extends AbstractAppState {
	private static ConditionalStateManagerI instance = new ConditionalStateManagerI();
	public static ConditionalStateManagerI i(){return instance;}
	
	public static class CompositeControl extends CompositeControlAbs<ConditionalStateManagerI>{
		private CompositeControl(ConditionalStateManagerI casm){super(casm);};
	}
	private CompositeControl ccSelf = new CompositeControl(this);
	
	ArrayList<ConditionalStateAbs> aCondStateList = new ArrayList<ConditionalStateAbs>();

	private boolean	bApplicationIsExiting;
	
	public void configure(Application app){
		app.getStateManager().attach(this);
	}
	
	public boolean isAttached(ConditionalStateAbs cas){
		return aCondStateList.contains(cas);
	}
	
	public ArrayList<ConditionalStateAbs> getListClone(){
		return new ArrayList<ConditionalStateAbs>(aCondStateList);
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		ArrayList<ConditionalStateAbs> aToDiscard = null;
		for(ConditionalStateAbs cas:aCondStateList){
			cas.doItAllProperly(ccSelf,tpf);
			
			if(cas.isRestartRequested()){
				if(cas.isEnabled()){
					cas.requestDisable();
				}else{
					cas.requestDiscard();
				}
			}else
			if(cas.isDiscarding()){
				if(aToDiscard==null)aToDiscard=new ArrayList<ConditionalStateAbs>();
				aToDiscard.add(cas);
			}
		}
		
		if(aToDiscard!=null){
			for(ConditionalStateAbs cas:aToDiscard){
				if(cas.prepareAndCheckIfReadyToDiscard(ccSelf)){
					aCondStateList.remove(cas);
					cas.applyDiscardedStatus(ccSelf);
					
					if(cas.isRestartRequested()){
						cas.createAndConfigureSelfCopy(); //this will add the new one to manager too
					}
				}
			}
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if(!enabled){
			throw new DeveloperMistakeException("this state MUST never be disabled! "+ConditionalStateManagerI.class.getName());
		}
		super.setEnabled(enabled);
	}
	
	public void applicationIsExiting(){
		bApplicationIsExiting=true;
	}
	
	@Override
	public void cleanup() {
		if(!bApplicationIsExiting){
			throw new DeveloperMistakeException("this state MUST never be terminated/cleaned! "+ConditionalStateManagerI.class.getName());
		}
	}
	
	public boolean attach(ConditionalStateAbs casToAttach){
		return attach(casToAttach,null);
	}
	/**
	 * 
	 * @param casToAttach
	 * @param iBeforeIndex can be null. If negative, will count index from the end.
	 * @return false if already attached
	 */
	public boolean attach(ConditionalStateAbs casToAttach, Integer iBeforeIndex) {
		if(isAttached(casToAttach))return false;
		
		if(iBeforeIndex==null){
			aCondStateList.add(casToAttach);
		}else{
			if(iBeforeIndex<0){
				iBeforeIndex=aCondStateList.size()+iBeforeIndex+1;
			}
			
			if(iBeforeIndex<0)iBeforeIndex=0;
			
			if(iBeforeIndex>aCondStateList.size())iBeforeIndex=aCondStateList.size();
			
			aCondStateList.add(iBeforeIndex,casToAttach);
		}
		
		casToAttach.setAppStateManagingThis(ccSelf,this);
		
		return true;
	}
	
	public int getListSize(){
		return aCondStateList.size();
	}
	
}
