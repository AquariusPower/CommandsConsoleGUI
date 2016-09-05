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

import com.github.commandsconsolegui.cmd.CommandData;
import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.VarIdValueOwnerData;
import com.github.commandsconsolegui.misc.DebugI;
import com.github.commandsconsolegui.misc.HashChangeHolder;
import com.github.commandsconsolegui.misc.VarId;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.DebugI.EDebugKey;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;

/**
 * TODO migrate most things possible to here
 *
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public abstract class VarCmdFieldAbs <O,S extends VarCmdFieldAbs<O,S>> implements IReflexFillCfgVariant{//, IVarIdValueOwner{
//	private boolean bReflexingIdentifier = true;
	private String strUniqueVarId = null;
	private String strUniqueCmdId = null;
	private String strSimpleCmdId = null;
	
	private String strCodePrefixVariant = null;
	
	/** keep unused, just used in debug as a hint */
	@SuppressWarnings("unused")
	private String strDebugErrorHelper = "ERROR: "+this.getClass().getName()+" not yet properly initialized!!!";
	
	private IReflexFillCfg	rfcfgOwner;
	private VarIdValueOwnerData	vivo;
	private String strHelp=null;
	private CommandData	cmdd;
	private O	objRawValueLazy;
	private boolean	bLazyValueWasSet;
	private VarId	idt;
	
	private static ArrayList<VarCmdFieldAbs> avcfList = new ArrayList<VarCmdFieldAbs>();
	public static final HashChangeHolder hvhVarList = new HashChangeHolder(avcfList);
	
	public static ArrayList<VarCmdFieldAbs> getListFullCopy(){
		return new ArrayList<VarCmdFieldAbs>(VarCmdFieldAbs.avcfList);
	}
	
	public static <T extends VarCmdFieldAbs> ArrayList<T> getListCopy(Class<T> clFilter){
		ArrayList<T> a = new ArrayList<T>();
		for(VarCmdFieldAbs vcf:VarCmdFieldAbs.avcfList){
			if(clFilter.isInstance(vcf)){
				a.add((T)vcf);
			}
		}
		
		return a;
	}
	
	public VarCmdFieldAbs(IReflexFillCfg rfcfgOwner){
		this.rfcfgOwner=rfcfgOwner;
		if(isField())VarCmdFieldAbs.avcfList.add(this);
	}
//	public VarCmdFieldAbs(boolean bAddToList){
//		if(bAddToList)VarCmdFieldAbs.avcfList.add(this);
//	}
	
	/**
	 * When discarding a class object that has fields of this class,
	 * the global fields list {@link #avcfList} must also be updated/synchronized.
	 * @param ccSelf
	 */
	public void discardSelf(CommandsDelegator.CompositeControl ccSelf) {
		VarCmdFieldAbs.avcfList.remove(this);
	}
	
	public CommandData getCmdData(){
		return this.cmdd;
	}
	
	public S setCmdData(CommandData cmdd){
		this.cmdd=cmdd;
		return getThis();
	}
	
	/**
	 * 
	 * @param cc
	 * @param vivo if the object already set is different from it, will throw exception
	 * @return
	 */
	public S setConsoleVarLink(CommandsDelegator.CompositeControl cc, VarIdValueOwnerData vivo) {
		cc.assertSelfNotNull();
		
		if(vivo==null){
			throw new PrerequisitesNotMetException("VarLink is null", this);
		}
		
//		if(this.vivo!=null)throw new PrerequisitesNotMetException("already set", this, this.vivo, vivo);
		if(this.vivo != vivo){
			// so, np if was null...
			PrerequisitesNotMetException.assertNotAlreadySet("VarLink", this.vivo, vivo, this);
		}
		
		this.vivo = vivo;
		
		/**
		 * vivo will already come with an updated value, because this method is called
		 * in a "set value" flow.
		 */
//		boolean bUpdateWithInitialValue=false; //do not enable... kept as reference...
//		if(bUpdateWithInitialValue){
		if(isRawValueLazySet()){
//			if(this.vivo.getObjectValue()==null){ //nah... value can be set to null...
//			setObjectRawValue(getValueRaw()); //update it's value with old/previous value? nah...
			setObjectRawValue(getRawValueLazy());
			
			/**
			 * lazy value is not required anymore, so clean it up
			 */
			bLazyValueWasSet=false;
			objRawValueLazy=null;
//			}
		}
		
		return getThis();
	}
	
	public VarIdValueOwnerData getConsoleVarLink(CommandsDelegator.CompositeControl cc){
		cc.assertSelfNotNull();
		return this.vivo;
	}
	/**
	 * DEV: do not expose this one, let only subclasses use it, to avoid messing the field.
	 * @return
	 */
	protected VarIdValueOwnerData getConsoleVarLink() {
		return vivo;
	}
	
	/**
	 * see {@link #getUniqueVarId(boolean)}
	 * @return
	 */
	public String getUniqueVarId() {
		return getUniqueVarId(false);
	}
	/**
	 * @return type_concreteClass[_declaringClass][_prefix]_id[_suffix]
	 */
	public String getUniqueVarId(boolean bRemoveType) {
		if(strUniqueVarId==null){
			setUniqueId(ReflexFillI.i().createIdentifierWithFieldName(getOwner(), this, true));
		}
		
		if(bRemoveType){
//			return strUniqueVarId.replaceAll("^[[:alnum:]]*_", "");
			return strUniqueVarId.replaceAll("^[a-zA-Z0-9]*_", "");
		}else{
			return strUniqueVarId;
		}
	}
	
	
//	protected S setReflexing(boolean b){
//		this.bReflexingIdentifier=b;
//		return getThis();
//	}
	
	protected S setUniqueId(VarId idt){
		this.idt = idt;
		
		String strExceptionId = null;
		
		/**
		 * must be an exception as it can have already been read/collected with automatic value.
		 * The exception control is strExceptionId.
		 */
		if(idt.isVariable()){
			if(strUniqueVarId!=null){
				strExceptionId=strUniqueVarId;
			}else{
				strUniqueVarId=idt.getUniqueId();
			}
		}else{
			if(strUniqueCmdId!=null){
				strExceptionId=strUniqueCmdId;
			}else{
				strUniqueCmdId=idt.getUniqueId();
			}
		}
		
		PrerequisitesNotMetException.assertNotAlreadySet("UniqueCmdId", strExceptionId, idt.getUniqueId());
//		if(strExceptionId!=null){
////			throw new NullPointerException("asked for '"+id.strUniqueCmdId+"' but was already set to: "+strExceptionId);
//		}
		
		strSimpleCmdId = idt.getSimpleId();
		
		strDebugErrorHelper=null; //clear error helper
		
		return getThis();
	}
	
	/**
	 * sets the command identifier that user will type in the console
	 * 
	 * @param strUniqueCmdId
	 * @param bIsVariable
	 * @return
	 */
	protected S setCustomUniqueCmdId(String strUniqueCmdId, boolean bIsVariable){
//		setUniqueId(new IdTmp(bIsVariable,strUniqueCmdId,strUniqueCmdId));
		setUniqueId(new VarId().setAsVariable(bIsVariable).setSimpleId(strUniqueCmdId).setUniqueId(strUniqueCmdId));
		return getThis();
	}

	public String getSimpleCmdId() {
		chkAndInit();
		return strSimpleCmdId;
	}
	
	@Override
	public IReflexFillCfg getOwner() {
		return rfcfgOwner;
	}

//	private S setOwner(IReflexFillCfg rfcfgOwner) {
//		this.rfcfgOwner = rfcfgOwner;
//		return getThis();
//	}

	protected void chkAndInit(){
		if(strUniqueCmdId==null){
			setUniqueId(ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner,this,false));
		}
	}
	public String getUniqueCmdId(){
		chkAndInit();
		return strUniqueCmdId;
	}
	protected S setUniqueCmdId(String strUniqueCmdId) {
		this.strUniqueCmdId = strUniqueCmdId;
		return getThis();
	}

	public String getHelp(){
		return strHelp==null?"":strHelp;
	}
	
	/**
	 * public but can be set only once.
	 * @param strHelp
	 * @return
	 */
	public S setHelp(String strHelp) {
		PrerequisitesNotMetException.assertNotAlreadySet("help", this.strHelp, strHelp);
		
		if(strHelp!=null && !strHelp.isEmpty()){
			this.strHelp = strHelp;
		}
		
		return getThis();
	}
	
	public abstract String getReport(); //this is safe to be public because it is just a report string
	public abstract Object getValueRaw(); //this is safe to be public because it is a base access to the concrete class simple value ex.: will return a primitive Long on the concrete class
	
	/**
	 * implement only at concrete class (not the midlevel abstract ones)
	 * @return
	 */
	protected abstract S getThis();
	
	/**
	 * It is the unmodified/original value.
	 * If var link is not set, the raw value will be lazily stored.
	 *  
	 * @param objValue
	 * @return
	 */
	public S setObjectRawValue(Object objValue) { // do not use O at param here, ex.: bool toggler can be used on overriden
//		public S setObjectValue(CommandsDelegator.CompositeControl ccCD, Object objValue) {
//		ccCD.assertSelfNotNull();
		
//	if(isField()){
//	if(vivo==null){
//		throw new PrerequisitesNotMetException("var link not set for field", this, objValue);
//	}else{
//		vivo.setObjectValue(objValue);
//	}
		if(vivo!=null){
			vivo.setObjectValue(objValue);
		}else{
			bLazyValueWasSet=true; //as such value can be actually null
			this.objRawValueLazy = (O)objValue;
		}
		
		return getThis();
	}
	
	protected boolean isRawValueLazySet(){
		return bLazyValueWasSet;
	}
	
	protected Object getRawValueLazy(){
		return objRawValueLazy;
	}
	
	/**
	 * used at {@link ReflexFillI#assertAndGetField()}
	 */
	@Override
	public boolean isReflexing() { // will only reflex for field name!
		return isField();
	}
	public boolean isField(){ // will be a field if it has an owner. Must have var link!
		return rfcfgOwner!=null;
	}

	@Override
	public String getCodePrefixVariant() {
		if(strCodePrefixVariant==null){
			strCodePrefixVariant=MiscI.i().assertGetValidId(null, getCodePrefixDefault());
		}
		
		return strCodePrefixVariant;
	}

	public S setCodePrefixVariant(String strCodePrefixVariant) {
		this.strCodePrefixVariant = MiscI.i().assertGetValidId(
			strCodePrefixVariant, getCodePrefixDefault());
		return getThis();
	}
	
	public abstract String getCodePrefixDefault();
	
	@Override
	public boolean isCodePrefixVariantEqualDefault() {
		return getCodePrefixVariant().equals(getCodePrefixDefault());
	}

	public VarId getIdTmp(CommandsDelegator.CompositeControl cc) {
		cc.assertSelfNotNull();
		return idt;
	}
	
	public abstract String getValueAsString();
	public abstract String getValueAsString(int iIfFloatPrecision);
	
	@Override
	public String toString() {
		if(DebugI.i().isKeyEnabled(EDebugKey.VarToString)){
			throw new PrerequisitesNotMetException("use getReport() instead!", this);
//			/**
//			 * use this method only for exceptions info, nothing else!
//			 */
//			int i=0;int i2=i;i=i2;//put breakpoint here!
		}
//		return strUniqueVarId+"='"+getValueAsString(3)+"'";
		return super.toString();
	}

	public VarId getIdTmpCopy() {
		return idt.clone();
	}
}
