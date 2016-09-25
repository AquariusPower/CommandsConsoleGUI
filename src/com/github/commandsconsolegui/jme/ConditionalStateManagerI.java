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

package com.github.commandsconsolegui.jme;

import java.util.ArrayList;

import com.github.commandsconsolegui.GlobalSimulationTimeI;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jme.GlobalAppRefI;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;

/**
 * This is actually a JME Application state.
 * It MUST not be disabled neither ended!
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ConditionalStateManagerI extends AbstractAppState {
	private static ConditionalStateManagerI instance = new ConditionalStateManagerI();
	public static ConditionalStateManagerI i(){return instance;}
	
	/**
	 * restricted access to public methods, helper
	 */
	public static final class CompositeControl extends CompositeControlAbs<ConditionalStateManagerI>{
		private CompositeControl(ConditionalStateManagerI casm){super(casm);};
	}
	private CompositeControl ccSelf = new CompositeControl(this);
	
	ArrayList<ConditionalStateAbs> aCondStateList = new ArrayList<ConditionalStateAbs>();

	private boolean	bConfigured;

//	private boolean	bApplicationIsExiting;
	
	public void configure(Application app){
		app.getStateManager().attach(this);
		bConfigured=true;
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
		
		GlobalSimulationTimeI.i().updateAdd(tpf);
		
		CallQueueI.i().update(tpf);
		
		ArrayList<ConditionalStateAbs> aToDiscard = null;
		for(ConditionalStateAbs cas:aCondStateList){
			if(!cas.doItAllProperly(ccSelf,tpf))continue;
			
			if(cas.isDiscarding()){
				if(aToDiscard==null)aToDiscard=new ArrayList<ConditionalStateAbs>();
				aToDiscard.add(cas);
			}else{
				if(cas.isRestartRequested()){
					if(cas.isEnabled()){
						cas.requestDisable();
					}else{
						cas.requestDiscard();
					}
				}
			}
		}
		
		if(aToDiscard!=null){
			for(ConditionalStateAbs cas:aToDiscard){
				if(cas.prepareAndCheckIfReadyToDiscard(ccSelf)){
					if(cas instanceof IConsoleCommandListener){
						GlobalCommandsDelegatorI.i().removeListener((IConsoleCommandListener)cas);
					}
					
					aCondStateList.remove(cas);
					cas.applyDiscardedStatus(ccSelf);
					
					cas.createAndConfigureSelfCopy(); //this will add the new one to manager too
				}
			}
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if(!enabled){
			throw new PrerequisitesNotMetException("this state MUST never be disabled! "+ConditionalStateManagerI.class.getName());
		}
		super.setEnabled(enabled);
	}
	
//	/**
//	 * use this whenever application exit is requested
//	 */
//	public void applicationIsExiting(){
//		bApplicationIsExiting=true;
//	}
	
	@Override
	public void cleanup() {
//		if(!bApplicationIsExiting){
//		if(false){ //dummyfied
			if(!GlobalAppRefI.iGlobal().isApplicationExiting()){
				throw new PrerequisitesNotMetException("this state MUST never be terminated/cleaned! "+ConditionalStateManagerI.class.getName());
			}
//		}
		
//		GlobalAppRefI.iGlobal().setAppExiting();
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
	
	/**
	 * 
	 * @param c
	 * @param strRequiredId can be null
	 * @return
	 */
	public <T extends ConditionalStateAbs> T getConditionalState(Class<T> c, String strRequiredId) {
		if(strRequiredId==null)return null;
		
		for(ConditionalStateAbs csa:aCondStateList){
			if(c.isAssignableFrom(csa.getClass())){
				if(strRequiredId==null){
					return (T)csa;
				}else{
					if(csa.getId()!=null){
						if(csa.getId().equalsIgnoreCase(strRequiredId)){
							return (T)csa;
						}
					}
				}
			}
		}
		
		return null;
	}

	public boolean isConfigured() {
		return bConfigured;
	}
	
}
