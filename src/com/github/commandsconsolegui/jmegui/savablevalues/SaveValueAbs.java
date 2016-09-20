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

package com.github.commandsconsolegui.jmegui.savablevalues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.VarCmdUId;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <T>
 * @param <S>
 */
@Deprecated
public abstract class SaveValueAbs<T, S extends SaveValueAbs<T,S>> implements Savable,IReflexFillCfgVariant{
	private T objValue;
	private T objDefaultValue;
	private IReflexFillCfg	rfcfgOwner;
	private VarCmdUId	vcuid;
	private String	strUniqueIdOverrideOnLoading;
	
//	private static ArrayList<SaveValueAbs> asvList = new ArrayList<SaveValueAbs>();
	private static HashMap<IReflexFillCfg, ArrayList<SaveValueAbs>> hmsvOwnerList = new HashMap<IReflexFillCfg, ArrayList<SaveValueAbs>>();
	
	public SaveValueAbs(){} //required by savable
	
	public SaveValueAbs(IReflexFillCfg rfcfgOwner) {
		synchronized(hmsvOwnerList){
			ArrayList<SaveValueAbs> asvList = SaveValueAbs.hmsvOwnerList.get(rfcfgOwner);
			if(asvList==null){
				asvList=new ArrayList<SaveValueAbs>();
				SaveValueAbs.hmsvOwnerList.put(rfcfgOwner, asvList);
			}
			asvList.add(this);
		}
		
		this.rfcfgOwner=rfcfgOwner;
	}
	
	public static ArrayList<SaveValueAbs> getSaveValueListCopy(IReflexFillCfg rfcfgOwnerFilter){
		return new ArrayList<SaveValueAbs>(hmsvOwnerList.get(rfcfgOwnerFilter));
//		synchronized(asvList){
//			ArrayList<SaveValueAbs> asvListCopy = new ArrayList<SaveValueAbs>();
//			for(SaveValueAbs sv:asvList){
//				if(sv.rfcfgOwner==rfcfgOwnerFilter)asvListCopy.add(sv);
//			}
//			return asvListCopy;
//		}		
	}
	
	private void chkAndInit(){
		if(vcuid==null){
			setUniqueId(ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner,this));
		}
	}
	
	private void setUniqueId(VarCmdUId vcuid) {
		if(strUniqueIdOverrideOnLoading!=null)throw new PrerequisitesNotMetException("unique id override already set", this.strUniqueIdOverrideOnLoading, vcuid, this);
		this.vcuid=vcuid;
	}

	/**
	 * Each saved owner will actually have an unique field id within it's inner context,
	 * so, such id tho will be identical to other instances of the same owner's class.
	 * But there is no need to be the unique (with the class name), this is about fields limited 
	 * to be unique only within its owner class scope.
	 * @return
	 */
	public String getUniqueId(){
		if(strUniqueIdOverrideOnLoading!=null)return strUniqueIdOverrideOnLoading;
		chkAndInit();
//		return vcuid.getUniqueId(null);
		return vcuid.getSimpleId(); 
	}
	
	public T getValue(){
		return objValue;
	}
	
	public T getDefaultValue(){
		return objDefaultValue;
	}
	
	public S setValue(T objValue) {
		this.objValue = objValue;
		return getThis();
	}
	
	public S setDefaultValue(T objValue) {
		this.objDefaultValue = objValue;
		return getThis();
	}
	
	public abstract S getThis();

//	@Override
//	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//		ReflexFillCfg rfcfg = new ReflexFillCfg(rfcv);
//		rfcfg.setCodingStyleFieldNamePrefix(getCodePrefix());
//		return rfcfg;
//	}
//
//	public static ReflexFillCfg getReflexFillCfg(SaveValueAbs sv, IReflexFillCfgVariant rfcv) {
//		return sv.getReflexFillCfg(rfcv);
//	}
	
	@Override
	public IReflexFillCfg getOwner() {
		return rfcfgOwner;
	}
	
	public void setOwner(IReflexFillCfg rfcfgOwner){
		PrerequisitesNotMetException.assertNotAlreadySet("owner", this.rfcfgOwner, rfcfgOwner, this);
		this.rfcfgOwner=rfcfgOwner;
	}
	
	@Override
	public boolean isReflexing() {
		return rfcfgOwner!=null;
	}

	@Override
	public String getVariablePrefix() {
		return null;
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		if(getValue()==null)throw new PrerequisitesNotMetException("value cannot be null", this);
		if(getDefaultValue()==null)throw new PrerequisitesNotMetException("default value cannot be null", this);
		saveAt(oc);
		oc.write(getUniqueId(), E.id.s(), null);
	}
	
	protected enum E{
		value,
		id,
		;
		public String s(){return this.toString();}
	}
	
	protected abstract void saveAt(OutputCapsule oc) throws IOException;
	
	@Override
	public void read(JmeImporter im) throws IOException {
    InputCapsule ic = im.getCapsule(this);
    this.objValue = loadFrom(ic);
    this.strUniqueIdOverrideOnLoading = ic.readString(E.id.s(),null);
	}

	protected abstract T loadFrom(InputCapsule ic) throws IOException;
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+":ido="+strUniqueIdOverrideOnLoading+";id="+(vcuid==null?null:getUniqueId())+";value="+getValue()+";defaultValue="+getDefaultValue()+";";
	}
}
