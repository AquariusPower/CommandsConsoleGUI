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
package com.github.commandsconsolegui.spAppOs;

import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.misc.IInstance;
import com.github.commandsconsolegui.spAppOs.misc.IManaged;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;


/**
 * Each managed can be managed by different managers for different reasons ex.:
 * KeyBoundVarField and generic VarCmdFieldAbs same instance 
 * can be managed by ManageKeyBind (specific key binding) 
 * and ManageVarCmdFieldI (just a global list ref holder). 
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class DelegateManagerI {
	private static DelegateManagerI instance = new DelegateManagerI();
	public static DelegateManagerI i(){return instance;}
	
//	TreeMap<Object,IManager> tmManagers = new TreeMap<Object, IManager>();
	
	public static class ManagerData{
		IManager imgr;
		Class clManaged;
	}
	ArrayList<ManagerData> amgrd = new ArrayList<ManagerData>();
	private boolean	bUpdateAllOnceLater;
	
	/**
	 * will also add the manager to another manager if appliable
	 * @param imgr
	 * @param clManaged
	 */
	public void addManager(IManager imgr, Class clManaged){
//		tmManagers.put(clManaged,imgr);
		ManagerData imgrd = new ManagerData();
		imgrd.imgr = imgr;
		imgrd.clManaged = clManaged;
		
		for(ManagerData mgrd:amgrd){
			if(mgrd.imgr==imgr){
				throw new PrerequisitesNotMetException("already set, use only at the manager constructor!",imgr,this);
			}
		}
		
		amgrd.add(imgrd);
		
//		aobjStillAddedToNoManager.add(imgr);
		add(imgr,true); //this adds the manager as managed to possible other managers
		
		boolean bUpdateAllNow=true;
		if (imgr instanceof IInstance) {
			IInstance ii = (IInstance) imgr;
			if(!ii.isInstanceReady()){
				bUpdateAllNow=false;
				bUpdateAllOnceLater=true;
			}
		}
		
		/**
		 * this is important to update all manageds to the new manager
		 */
		if(bUpdateAllNow)traverseRefreshAllManagedAndManagers();
//		for(Object obj:aobjStillAddedToNoManager.toArray()){
//			add(obj,true);
//		}
	}
	
	private void traverseRefreshAllManagedAndManagers(){
		for(ManagerData mgrd:amgrd){
			for(Object obj:mgrd.imgr.getListCopy()){
				add(obj,true);
			}
		}
	}
	
	public void add(IManaged img){
		add((Object)img,false);
	}
	
	/**
	 * this method allows not implementing {@link IManaged} and still be managed.
	 * @param obj
	 */
	public void add(Object obj){
		add(obj,false);
	}
	
	ArrayList<Object> aobjStillAddedToNoManager = new ArrayList<Object>();
	private void add(Object obj, boolean bIgnoreIfNotAdded){
		int iAddCount=0;
		boolean bAlreadyAddedToAnyManager=false;
		for(ManagerData mgrd:amgrd){
			if(mgrd.clManaged.isInstance(obj)){
				if(mgrd.imgr.getListCopy().contains(obj)){
					bAlreadyAddedToAnyManager=true;
				}else{
					if(mgrd.imgr.add(obj)){
						iAddCount++;
					}
				}
			}
		}
		
		if(iAddCount>0 || bAlreadyAddedToAnyManager){
			if(aobjStillAddedToNoManager.contains(obj))aobjStillAddedToNoManager.remove(obj);
		}else{
//			if(!bIgnoreIfNotAdded){
//				throw new PrerequisitesNotMetException("added to no managers",obj,this);
//			}
			if(!aobjStillAddedToNoManager.contains(obj))aobjStillAddedToNoManager.add(obj);
		}
		
//		if(!aobjManaged.contains(obj))aobjManaged.add(obj);
//		for(imgr:tmManagers.val())
//		tmManagers.get(obj.getClass());
	}
	
	public boolean isAmICallingThis() {
		//TODO more restrictive like? //StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
		for(StackTraceElement ste:Thread.currentThread().getStackTrace()){
			if(ste.getClassName().equals(DelegateManagerI.class.getName()))return true;
		}
		
		return false;
	}

	public void update(float fTpf) {
		if(bUpdateAllOnceLater){
			traverseRefreshAllManagedAndManagers();
			bUpdateAllOnceLater=false;
		}
		
		if(aobjStillAddedToNoManager.size()==0)return;
		for(Object obj:aobjStillAddedToNoManager.toArray()){
			add(obj,true);
		}
	}
}
