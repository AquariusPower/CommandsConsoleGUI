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

package com.github.commandsconsolegui.spCmd.varfield;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.DiscardableInstanceI;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.IMultiInstanceOverride;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RefHolder;
import com.github.commandsconsolegui.spAppOs.misc.RegisteredClasses;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageVarCmdFieldI implements IManager<VarCmdFieldAbs>{
	private static ManageVarCmdFieldI instance = new ManageVarCmdFieldI();
	public static ManageVarCmdFieldI i(){return instance;}
	
	public static final class CompositeControl extends CompositeControlAbs<ManageVarCmdFieldI>{
		private CompositeControl(ManageVarCmdFieldI casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	public ManageVarCmdFieldI() {
		DelegateManagerI.i().addManager(this, VarCmdFieldAbs.class);
//		ManageSingleInstanceI.i().add(this);
	}
	
	private ArrayList<VarCmdFieldAbs> avcfList = new ArrayList<VarCmdFieldAbs>();
	public  final RefHolder hvhVarList = new RefHolder(avcfList);
	
	@Override
	public ArrayList<VarCmdFieldAbs> getHandledListCopy() {
		return new ArrayList<VarCmdFieldAbs>(avcfList);
	}
//	public ArrayList<VarCmdFieldAbs> getListFullCopy(){
//		return new ArrayList<VarCmdFieldAbs>(avcfList);
//	}
	
	/**
	 * Only fields that should have direct console access are accepted here.
	 */
	@Override
	public boolean addHandled(VarCmdFieldAbs vcf){
		if(!vcf.isField())return false;
		if(vcf.getOwner() instanceof IMultiInstanceOverride)return false; //only single instances allowed
		
		PrerequisitesNotMetException.assertNotAlreadyAdded(avcfList,vcf,this);
//		if(vcf==null){
//			throw new PrerequisitesNotMetException("cant be null", this);
//		}
//		
//		if(avcfList.contains(vcf)){
//			throw new PrerequisitesNotMetException("already added", vcf, avcfList, this);
//		}
		
//		vcf.setManager(this);
		
		if(!avcfList.contains(vcf)){
			return avcfList.add(vcf);
		}
		
		return false;
	}
	
	public <T extends VarCmdFieldAbs> ArrayList<T> getListCopy(Class<T> clFilter){
		ArrayList<T> a = new ArrayList<T>();
		for(VarCmdFieldAbs vcf:avcfList){ 
			if(clFilter.isInstance(vcf)){ // (vcf.getClass().getDeclaredClasses()) (vcf.getClass().isAssignableFrom(clFilter)) (vcf instanceof clFilter) (clFilter.isAssignableFrom(vcf.getClass()))
				@SuppressWarnings("unchecked") T o = (T)vcf;
				a.add(o);
			}
//			else{
//				for(Class cl:MiscI.i().getSuperClassesOf(vcf)){
//					if(cl.getName().equals(clFilter.getName())){
////					if(cl.isAssignableFrom(clFilter)){
//						a.add((T)vcf);
//					}
//				}
//			}
		}
		
		return a;
	}
	
	public ArrayList<VarCmdFieldAbs> removeAllWhoseOwnerIsBeingDiscarded(){
		ArrayList<VarCmdFieldAbs> avcfDiscarded = new ArrayList<VarCmdFieldAbs>();
		
		for(VarCmdFieldAbs vcf:getHandledListCopy()){
			if(DiscardableInstanceI.i().isBeingDiscardedRecursiveOwner(vcf.getOwner())){
				discard(vcf);
				avcfDiscarded.add(vcf);
			}
		}
		
		return avcfDiscarded;
	}

	/**
	 * When discarding a class object that has fields of this class,
	 * the global fields list {@link #avcfList} must also be updated/synchronized.
	 */
	private void discard(VarCmdFieldAbs vcf) {
		if(!avcfList.contains(vcf)){
			/**
			 * if owner is set, it should be on the main list,
			 * shall not try to remove if not on the main list...
			 */
			throw new PrerequisitesNotMetException("inconsistency", vcf, vcf.getOwner(), avcfList);
		}
		
		avcfList.remove(vcf);
	}
	
	RegisteredClasses<VarCmdFieldAbs> rsc = new RegisteredClasses<VarCmdFieldAbs>();
	public RegisteredClasses<VarCmdFieldAbs> getClassReg(){
		return rsc;
	}
	
//	public static class RegisteredSuperClasses<E extends VarCmdFieldAbs>{
//		TreeMap<String,Class<E>> tmSubClass = new TreeMap<String,Class<E>>();
//		public void registerSuperClassesFrom(E obj){
//			for(Class cl:MiscI.i().getSuperClassesOf(obj)){
//				registerSuperClass(cl);
//			}
//		}
//		public void registerSuperClass(Class<E> cl){
//			tmSubClass.put(cl.getName(),cl);
//		}
//		public boolean isContainSubClass(String strClassTypeKey){
//			return (tmSubClass.get(strClassTypeKey)) != null;
//		}
//	}
	
	/**
	 * each call will only return true once per list change
	 * @return
	 */
	public boolean isListChanged() {
		return hvhVarList.isChangedAndUpdateHash();
	}
	
	@Override public String getUniqueId() {return MiscI.i().prepareUniqueId(this);}
	
	public static class VarMgr<V extends VarCmdFieldAbs>{
		Class<V> cl;
		IManager<V> imgr;
		RegisteredClasses<IManager<V>> rscManager = new RegisteredClasses<IManager<V>>();
	}
	private HashMap<IManager,VarMgr> hmManagers = new HashMap<IManager,VarMgr>();
	/**
	 * each class can have many managers
	 * @param imgrKey
	 * @param cl
	 */
	public <V extends VarCmdFieldAbs> void putVarManager(IManager<V> imgrKey, Class<V> cl){
		PrerequisitesNotMetException.assertNotAlreadySet("manager", hmManagers.get(imgrKey), cl, this);
		
		VarMgr<V> vm = new VarMgr<V>();
		vm.cl=cl;
		vm.imgr=imgrKey;
		vm.rscManager.addClassesOf(imgrKey,true,true);
		
		hmManagers.put(imgrKey,vm);
		
//		if(!DelegateManagerI.i().isContainsManager(imgrKey)){
//			DelegateManagerI.i().addManager(imgrKey, cl);
//		}
	}
	
	public <V extends VarCmdFieldAbs> boolean isHasVarManager(IManager<V> imgrKey){
		return hmManagers.get(imgrKey)!=null;
	}
	
	public boolean isVarManagerContainClassTypeName(String strClassTypeName){
		for(VarMgr vm:hmManagers.values()){
			if(vm.rscManager.isContainClassTypeName(strClassTypeName)){
				return true;
			}
		}
		return false;
	}

	public <T extends VarCmdFieldAbs> ArrayList<IManager> getManagerListFor(Class<T> cl) {
		ArrayList<IManager> a = new ArrayList<IManager>();
		for(VarMgr vm:hmManagers.values()){
			if(vm.cl==cl)a.add(vm.imgr);
		}
		return a;
//		VarMgr vm = tmManagers.get(cl);
//		if(vm==null)return null;
//		return vm.imgr;
	}
}