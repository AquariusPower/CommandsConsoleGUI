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

package com.github.commandsconsolegui.spJme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.github.commandsconsolegui.spAppOs.globals.GlobalAppOSI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalUpdaterI;
import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.ManageHoldRestartableI;
import com.github.commandsconsolegui.spAppOs.misc.ManageSingleInstanceI;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.Priority;
import com.github.commandsconsolegui.spCmd.IConsoleCommandListener;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;

/**
 * This is actually a JME Application state.
 * It MUST not be disabled neither ended!
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageConditionalStateI extends AbstractAppState implements IManager<ConditionalStateAbs>{
	private static ManageConditionalStateI instance = new ManageConditionalStateI();
	public static ManageConditionalStateI i(){return instance;}
	
	/**
	 * restricted access to public methods, helper
	 */
	public static final class CompositeControl extends CompositeControlAbs<ManageConditionalStateI>{
		private CompositeControl(ManageConditionalStateI casm){super(casm);};
	}
	private CompositeControl ccSelf = new CompositeControl(this);
	
	public ManageConditionalStateI() {
		ManageSingleInstanceI.i().add(this);
	}
	
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
	
	@Override
	public void update(float fTpf) {
		super.update(fTpf);
		
		GlobalUpdaterI.i().update(fTpf);
		
		ArrayList<ConditionalStateAbs> aToDiscard = null;
		for(ConditionalStateAbs cas:aCondStateList){
			if(!cas.doItAllProperly(ccSelf,fTpf))continue;
			
			if(cas.isBeingDiscarded()){
				if(aToDiscard==null)aToDiscard=new ArrayList<ConditionalStateAbs>();
				aToDiscard.add(cas);
			}else{
				if(cas.isRestartRequested()){
					if(cas.isEnabled()){
						if(!cas.isDisabling()){
							cas.requestDisable();
						}
					}else{
						cas.requestDiscard();
					}
				}
			}
		}
		
		if(aToDiscard!=null){
			for(ConditionalStateAbs cas:aToDiscard){
				discardState(cas);
			}
		}
	}
	
	private void discardState(ConditionalStateAbs cas){
		if(cas.prepareToDiscard(ccSelf)){
			cas.applyDiscardedStatus(ccSelf); //this flag is required to everything else work...
			
			aCondStateList.remove(cas);
			
			if(cas instanceof IConsoleCommandListener){
				GlobalCommandsDelegatorI.i().removeListenerAndCmds((IConsoleCommandListener)cas);
			}
			
			ConditionalStateAbs casNew = null;
			if(cas.isRestartRequested()){
				casNew = cas.createAndConfigureSelfCopy(); //this will add the new one to manager too
				
				if(cas.isWasEnabledBeforeRestarting()){
//					casNew.requestRetryUntilEnabled();
					casNew.requestEnable();
				}
			}
			
			ManageHoldRestartableI.i().revalidateAndUpdateAllRestartableHoldersFor(cas,casNew);
			
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if(!enabled){
			throw new PrerequisitesNotMetException("this state MUST never be disabled! "+ManageConditionalStateI.class.getName());
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
			if(!GlobalAppOSI.i().isApplicationExiting()){
				throw new PrerequisitesNotMetException("this state MUST never be terminated/cleaned! "+ManageConditionalStateI.class.getName());
			}
//		}
		
//		GlobalAppRefI.iGlobal().setAppExiting();
	}
	
	public boolean attach(ConditionalStateAbs casToAttach){
//		return attach(casToAttach,null);
//	}
//	/**
//	 * 
//	 * @param casToAttach
//	 * @param iBeforeIndex can be null. If negative, will count index from the end (no overlaps tho).
//	 * @return false if already attached
//	 */
//	private boolean attach(ConditionalStateAbs casToAttach, Integer iBeforeIndex) {
		if(isAttached(casToAttach))return false;
		
//		if(iBeforeIndex==null){
			aCondStateList.add(casToAttach);
//		}else{
//			if(iBeforeIndex<0){
//				iBeforeIndex=aCondStateList.size()+iBeforeIndex+1;
//			}
//			
//			if(iBeforeIndex<0)iBeforeIndex=0;
//			
//			if(iBeforeIndex>aCondStateList.size())iBeforeIndex=aCondStateList.size();
			
//			aCondStateList.add(iBeforeIndex,casToAttach);
//		}
		
		Collections.sort(aCondStateList, Priority.i().getComparator());
			
			
		casToAttach.setAppStateManagingThis(ccSelf,this);
		
		return true;
	}
	
	public int getListSize(){
		return aCondStateList.size();
	}
	
	/**
	 * 
	 * @param cl
	 * @param strRequireId can be null, and will just return the first matching the class
	 * @return
	 */
	public <T> T getConditionalState(Class<T> cl, String strRequireId) {
//	public <T extends ConditionalStateAbs> T getConditionalState(Class<T> cl, String strRequireId) {
//		if(strRequireId==null)return null;
		
		for(ConditionalStateAbs csa:aCondStateList){
			if(cl.isAssignableFrom(csa.getClass())){
				if(strRequireId==null){
					return (T)csa;
				}else{
					if(csa.getUniqueId()!=null){
						if(csa.getUniqueId().equalsIgnoreCase(strRequireId)){
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

	@Override
	public boolean add(ConditionalStateAbs objNew) {
		throw new UnsupportedOperationException("use attach()");
	}
	
	@Override
	public ArrayList<ConditionalStateAbs> getListCopy() {
		return new ArrayList<ConditionalStateAbs>(aCondStateList);
	}

	@Override public String getUniqueId() {return MiscI.i().prepareUniqueId(this);}

}