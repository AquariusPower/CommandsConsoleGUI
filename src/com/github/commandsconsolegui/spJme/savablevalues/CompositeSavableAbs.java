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

package com.github.commandsconsolegui.spJme.savablevalues;

import java.io.IOException;
import java.nio.channels.UnsupportedAddressTypeException;

import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.IHasOwnerInstance;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spJme.misc.SavableHelperI;
import com.github.commandsconsolegui.spJme.misc.SavableHelperI.ISavableFieldAccess;
import com.github.commandsconsolegui.spJme.misc.SavableHelperI.SaveSkipper;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;


/**
 * declare fields like ex.:
 * float fHeightDefault=10,fHeight=fHeightDefault; 
 * 
 * if default is not declared, current value will always be saved, or used as default when reading.
 * 
 * 
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public abstract class CompositeSavableAbs<OWNER,THIS extends CompositeSavableAbs<OWNER,THIS>> implements ISavableFieldAccess,IHasOwnerInstance<OWNER> { //IDiscardableInstance
	public static final class CompositeControl extends CompositeControlAbs<CompositeSavableAbs>{
		private CompositeControl(CompositeSavableAbs casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	public static class SaveSkipperCS<OWNER> extends SaveSkipper<OWNER>{
//	public SaveSkipperCS(ISavableFieldAccess isfa) {
		public SaveSkipperCS(CompositeControlAbs cc, ISavableFieldAccess isfa) {
			super(cc, isfa);
		}
	}
	private SaveSkipperCS<OWNER> ss = new SaveSkipperCS<OWNER>(ccSelf,this);
	
	public CompositeSavableAbs(){
		ss.setThisInstanceIsALoadedTmp();
		initAllSteps();
	} //required by savable
	public CompositeSavableAbs(OWNER owner){
		setOwner(owner);
		initAllSteps();
	}
	public abstract THIS getThis();
	
	/**
	 * this way, super methods can come on the beggining of the current class method
	 */
	private void initAllSteps(){
		initPre();
		initialize();
		initPos();
	}
	
	public boolean isThisInstanceALoadedTmp(){
		return ss.isThisInstanceIsALoadedTmp(); //ss.owner==null;
	}
	
	/** currently this is just to give flexibility */
	protected void initPre() {}
	
	/** 
	 * this is where field's values can be instantiated,
	 * avoid instantiating field's values outside here or of constructors, as their order/placement
	 * in the class source may cause trouble ex.: if an instantiation is coded after the declaration of 
	 * the constructor, such field will still be null at the constructor!!!
	 */
	protected void initialize(){}
	
	/** this is after everything is instantiated */
	private void initPos() {
		SavableHelperI.i().prepareFields(this);
	}
	
	private THIS setOwner(OWNER owner){
		ss.setOwner(owner);
		return getThis();
	}
	
	@Override
	public OWNER getOwner() {
		return ss.getOwner(ccSelf);
	}
	
	public boolean applyValuesFrom(THIS svLoadedSource) {
		return SavableHelperI.i().applyValuesFrom(this, svLoadedSource);
	}	
	
	@Override
	public SaveSkipper<?> getSkipper() {
		return ss;
	}
	
	/**
	 * This happens on the object being directly used by the application.
	 */
	@Override
	public void write(JmeExporter ex) throws IOException {
		SavableHelperI.i().write(this,ex);
	}
	
	/**
	 * This happens on a new instance, requires {@link #applyValuesFrom(CompositeSavableAbs)} to be made useful.
	 */
	@Override
	public void read(JmeImporter im) throws IOException {
		SavableHelperI.i().read(this,im);
	}
	
//	@Override
//	public boolean isBeingDiscarded(){
//		return DiscardableInstanceI.i().isSelfOrRecursiveOwnerBeingDiscarded(getOwner());
//	}
	
	/**
	 * 
	 * TODO create a command to access savable fields of classes using this composite savable, so there will have no direct access like vars but they can still be accessible
	 * 
	 * @param rfcvField
	 * @return
	 */
	@Deprecated
	public final ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField){
		throw new PrerequisitesNotMetException("");
	}
	
}
