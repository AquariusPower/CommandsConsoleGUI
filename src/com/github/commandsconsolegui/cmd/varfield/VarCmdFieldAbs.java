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
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.DebugI;
import com.github.commandsconsolegui.misc.DebugI.EDebugKey;
import com.github.commandsconsolegui.misc.HashChangeHolder;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.VarCmdUId;

/**
 * TODO migrate most things possible to here
 *
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public abstract class VarCmdFieldAbs <O,S extends VarCmdFieldAbs<O,S>> implements IReflexFillCfgVariant{//, IVarIdValueOwner{
//	private boolean bReflexingIdentifier = true;
	public static enum EVarCmdMode{
		Var,
		Cmd,
		VarCmd,
		;
	}
	private EVarCmdMode evcm = null;
	
//	private String strUniqueVarId = null;
//	private String strUniqueCmdId = null;
//	
//	private String strSimpleId = null;
	
	private String strCodePrefixVariant = null;
	
	/** keep unused, just used in debug as a hint */
	@SuppressWarnings("unused")
	private String strDebugErrorHelper = "ERROR: "+this.getClass().getName()+" not yet properly initialized!!!";
	
	private IReflexFillCfg	rfcfgOwner;
	private VarIdValueOwnerData	vivo;
	private String strHelp=null;
	private CommandData	cmdd;
	private O	objRawValueLazy = null;
	private O	objRawValueDefault = null;
	private boolean	bLazyValueWasSet;
	private VarCmdUId	vcuid;
	private boolean	bConstructed = false;

	private CallableX	callerAssigned;

	private CompositeControlAbs	ccOwner;
	
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
	
	/**
	 * 
	 * @param rfcfgOwner use null if this is not a class field, but a local variable, or for any reason the related console variable creation is to be skipped.
	 * @param evcm
	 */
	public VarCmdFieldAbs(IReflexFillCfg rfcfgOwner, EVarCmdMode evcm){
		this.evcm=evcm;
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
		if(!isVar())throw new PrerequisitesNotMetException("is not var", this);
		
		chkAndInit();
//		if(strUniqueVarId==null){
//			setUniqueId(ReflexFillI.i().createIdentifierWithFieldName(getOwner(), this, true));
//		}
		
//		if(bRemoveType){ //the first prefix separated by "_"
////			return strUniqueVarId.replaceAll("^[[:alnum:]]*_", "");
////			return strUniqueVarId.replaceAll("^[a-zA-Z0-9]*_", "");
//			return vcuid.getUniqueId();
//		}else{
////			return strUniqueVarId;
//			return vcuid.getVarType()+vcuid.getUniqueId();
//		}
		return vcuid.getUniqueId(bRemoveType?null:true);
	}
	
	public boolean isVar(){
		switch(evcm){
//			case Cmd:
//				return true;
			case Var:
				return true;
			case VarCmd:
				return true;
		}
		return false;
	}
	
	public boolean isCmd(){
		switch(evcm){
			case Cmd:
				return true;
//			case Var:
//				return true;
			case VarCmd:
				return true;
		}
		return false;
	}
	
//	protected S setReflexing(boolean b){
//		this.bReflexingIdentifier=b;
//		return getThis();
//	}
	
	protected S setUniqueId(VarCmdUId vcuid){
		if(this.vcuid!=null)PrerequisitesNotMetException.assertNotAlreadySet("UniqueId", this.vcuid, vcuid, vcuid.getUniqueId(null));
		this.vcuid = vcuid;
		
//		String strExceptionId = null;
//		
//		/**
//		 * must be an exception as it can have already been read/collected with automatic value.
//		 * The exception control is strExceptionId.
//		 */
//		
////		if(vcid.isVariable()){
//			if(strUniqueVarId!=null){
//				strExceptionId=strUniqueVarId;
//			}else{
//				strUniqueVarId=vcid.getUniqueId();
//			}
////		}else{
//			if(strUniqueCmdId!=null){
//				strExceptionId=strUniqueCmdId;
//			}else{
//				strUniqueCmdId=vcid.getUniqueId();
//			}
////		}
//		
//		PrerequisitesNotMetException.assertNotAlreadySet("UniqueCmdId", strExceptionId, vcid.getUniqueId());
////		if(strExceptionId!=null){
//////			throw new NullPointerException("asked for '"+id.strUniqueCmdId+"' but was already set to: "+strExceptionId);
////		}
//		
//		strSimpleId = vcid.getSimpleId();
		
		strDebugErrorHelper=null; //clear error helper
		
		return getThis();
	}
	
//	/**
//	 * sets the command identifier that user will type in the console
//	 * 
//	 * @param strUniqueCmdId
//	 * @param bIsVariable
//	 * @return
//	 */
//	protected S setCustomUniqueCmdId(String strUniqueCmdId, boolean bIsVariable){
////		setUniqueId(new IdTmp(bIsVariable,strUniqueCmdId,strUniqueCmdId));
//		setUniqueId(
//			new VarCmdId()
//				.setAsVariable(bIsVariable)
//				.setSimpleId(strUniqueCmdId)
//				.setUniqueId(strUniqueCmdId)
//		);
//		return getThis();
//	}

	public String getSimpleId() {
//		idt.isVariable()
		chkAndInit();
//		return strSimpleId;
		return vcuid.getSimpleId();
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
		if(vcuid==null){
			setUniqueId(ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner,this));
		}
	}
	public String getUniqueCmdId(){
		if(!isCmd())throw new PrerequisitesNotMetException("is var", this);
		chkAndInit();
//		return strUniqueCmdId;
//		return vcuid.getPrefixCmd()+vcuid.getUniqueId();
		return vcuid.getUniqueId(false);
	}
//	protected S setUniqueCmdId(String strUniqueCmdId) {
//		this.strUniqueCmdId = strUniqueCmdId;
//		return getThis();
//	}

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
	
	/** TODO modify return to O, also on vivo? or will be too much unnecessary complexity? just casts should suffice? */ 
	public abstract Object getRawValue(); //this is safe to be public because it is a base access to the concrete class simple value ex.: will return a primitive Long on the concrete class
	
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
		if(objRawValueDefault==null)objRawValueDefault=(O)objValue;
		
		if(vivo!=null){
			if(vivo.getObjectValue()!=objValue)prepareCallerAssigned(false);
			
			vivo.setObjectValue(objValue);
		}else{
			bLazyValueWasSet=true; //as such value can be actually null
			this.objRawValueLazy = (O)objValue;
		}
		
		return getThis();
	}
	
	protected void prepareCallerAssigned() {
		prepareCallerAssigned(false);
	}
	protected Boolean prepareCallerAssigned(boolean bRunNow) {
		if(isConstructed()){ 
			if(callerAssigned!=null){
				if(bRunNow || callerAssigned.isAllowQueue()){
					callerAssigned.setRetryOnFail(!bRunNow);
					return CallQueueI.i().addCall(callerAssigned,bRunNow);
//				}else{
//					if(callerAssigned.isAllowQueue()){
//						callerAssigned.setRetryOnFail(true);
//						return CallQueueI.i().addCall(callerAssigned,false);
//					}
				}
			}
		}
		
		return false;
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
	
//	@Override
//	public boolean isCodePrefixVariantEqualDefault() {
//		return getCodePrefixVariant().equals(getCodePrefixDefault());
//	}

	public VarCmdUId getVarCmdUIdForManagement(CommandsDelegator.CompositeControl cc) {
		cc.assertSelfNotNull();
		return vcuid;
	}
	
	public abstract String getValueAsString();
	public abstract String getValueAsString(int iIfFloatPrecision);
	
	@Override
	public String toString() {
		if(DebugI.i().isKeyEnabled(EDebugKey.VarToStringDenied)){
			/**
			 * enable the debug key to show improper use of toString()
			 */
			boolean bIgnoreOnce=false; // bIgnoreOnce=true // evaluate in debug, to ignore once.
			if(!bIgnoreOnce){
				throw new PrerequisitesNotMetException("use getReport() instead!", this);
			}
		}
		
		return getReport() + " (" + super.toString() + ")";
	}

	public VarCmdUId getIdTmpCopy() {
		return vcuid.clone();
	}

	public O getRawValueDefault() {
		return objRawValueDefault;
	}
	
	/**
	 * Changes on raw value will trigger this caller.
	 * Also, if this is a command, the caller can be directly invoked by the commands delegator (no queue). 
	 * 
	 * @param caller
	 * @return
	 */
	public S setCallerAssigned(CallableX caller){
		this.callerAssigned=caller;
		return getThis();
	}
	
	public boolean isCallerAssigned(){
		return callerAssigned!=null;
	}
	
	/**
	 * use only at the end of concrete class constructor
	 */
	protected void constructed(){
		bConstructed=true;
	}
	
	/**
	 * this is mainly to skip the initialing value at constructors
	 * @return
	 */
	public boolean isConstructed(){
		return bConstructed;
	}
	
//	public static String getCallerAssignedParamsKey(){
//		return "Params";
//	}
	
	public Boolean callerAssignedRunNow(){
		return callerAssignedRunNow(new Object[]{});
	}
	public Boolean callerAssignedRunNow(Object... aobjParams){
		if(callerAssigned!=null){
//			callerAssigned.putCustomValue(getCallerAssignedParamsKey(), aobjParams);
			callerAssigned.getParamsForMaintenance().setAllParams(aobjParams);
		}
		
		return prepareCallerAssigned(true);
	}
	public void callerAssignedQueueNow(){
		prepareCallerAssigned(false);
	}

	public CallableX getCallerAssignedForMaintenance(CompositeControlAbs cc) {
		this.ccOwner = cc.assertSelfNotNullEqualsStored(this.ccOwner);
		
		return callerAssigned;
	}
	
}
