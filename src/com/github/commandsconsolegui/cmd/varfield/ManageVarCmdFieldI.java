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

import java.util.ArrayList;

import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.DiscardableInstanceI;
import com.github.commandsconsolegui.misc.HashChangeHolder;
import com.github.commandsconsolegui.misc.IManager;
import com.github.commandsconsolegui.misc.IMultiInstanceOverride;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;

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
	
	private ArrayList<VarCmdFieldAbs> avcfList = new ArrayList<VarCmdFieldAbs>();
	public  final HashChangeHolder hvhVarList = new HashChangeHolder(avcfList);
	
	@Override
	public ArrayList<VarCmdFieldAbs> getListCopy() {
		return new ArrayList<VarCmdFieldAbs>(avcfList);
	}
//	public ArrayList<VarCmdFieldAbs> getListFullCopy(){
//		return new ArrayList<VarCmdFieldAbs>(avcfList);
//	}
	
	/**
	 * Only fields that should have direct console access are accepted here.
	 */
	@Override
	public boolean add(VarCmdFieldAbs vcf){
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
		
		return avcfList.add(vcf);
	}
	
	public <T extends VarCmdFieldAbs> ArrayList<T> getListCopy(Class<T> clFilter){
		ArrayList<T> a = new ArrayList<T>();
		for(VarCmdFieldAbs vcf:avcfList){
			if(clFilter.isInstance(vcf)){
				a.add((T)vcf);
			}
		}
		
		return a;
	}
	
	public ArrayList<VarCmdFieldAbs> removeAllWhoseOwnerIsBeingDiscarded(){
		ArrayList<VarCmdFieldAbs> avcfDiscarded = new ArrayList<VarCmdFieldAbs>();
		
		for(VarCmdFieldAbs vcf:getListCopy()){
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
	
	/**
	 * each call will only return true once per list change
	 * @return
	 */
	public boolean isListChanged() {
		return hvhVarList.isChangedAndUpdateHash();
	}
	
}
