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

import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.IConstructed;
import com.github.commandsconsolegui.spAppOs.misc.IDebugReport;
import com.github.commandsconsolegui.spAppOs.misc.IHandled;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallerInfo;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.DebugData;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.EDbgStkOrigin;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RefHolder;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.RegisteredClasses;
import com.github.commandsconsolegui.spAppOs.misc.RunMode;
import com.github.commandsconsolegui.spCmd.CommandData;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.ConsoleVariable;

/**
 * Do NOT put var cmds to be saved as JME j3o.
 * These are console variables to be used/saved only at console init file.
 * 
 * Objects instances should not have console variables unless they are unique: 
 * one single instance for each concrete class.
 *
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public abstract class VarCmdFieldAbs<VAL,THIS extends VarCmdFieldAbs<VAL,THIS>> implements IReflexFillCfgVariant,IDebugReport,IConstructed,IHandled{//, IVarIdValueOwner{
	public static final class CompositeControl extends CompositeControlAbs<VarCmdFieldAbs>{
		private CompositeControl(VarCmdFieldAbs casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
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
	private ConsoleVariable<VAL>	cvarLinkAndValueStorage;
	private String strHelp=null;
	private CommandData	cmdd;
	
	/** the lazy value will exist/be used while the console variable link {@link #cvarLinkAndValueStorage} still dont exist */
	private VAL	objRawLazyValue = null;
	
	/** this is the initial value (at construction time) */
	private VAL	objRawValueDefault = null;
	
	private boolean	bRawLazyValueAvailable;
	private VarCmdUId	vcuid;
	private boolean	bConstructed = false;
	private boolean bAllowNullValue = true;

	private CallableX	callerAssigned;

	private CompositeControlAbs	ccOwner;
	
//	private static ArrayList<VarCmdFieldAbs> avcfList = new ArrayList<VarCmdFieldAbs>();
//	public static final HashChangeHolder hvhVarList = new HashChangeHolder(avcfList);
//	
//	public static ArrayList<VarCmdFieldAbs> getListFullCopy(){
//		return new ArrayList<VarCmdFieldAbs>(VarCmdFieldAbs.avcfList);
//	}
//	
//	public static <T extends VarCmdFieldAbs> ArrayList<T> getListCopy(Class<T> clFilter){
//		ArrayList<T> a = new ArrayList<T>();
//		for(VarCmdFieldAbs vcf:VarCmdFieldAbs.avcfList){
//			if(clFilter.isInstance(vcf)){
//				a.add((T)vcf);
//			}
//		}
//		
//		return a;
//	}
//	
//	public static ArrayList<VarCmdFieldAbs> removeAllWhoseOwnerIsBeingDiscarded(){
//		ArrayList<VarCmdFieldAbs> avcfDiscarded = new ArrayList<VarCmdFieldAbs>();
//		
//		for(VarCmdFieldAbs vcf:getListFullCopy()){
//			if(DiscardableInstanceI.i().isBeingDiscardedRecursiveOwner(vcf.getOwner())){
//				vcf.discardSelf();
//				avcfDiscarded.add(vcf);
//			}
//		}
//		
//		return avcfDiscarded;
//	}
	
	/**
	 * 
	 * @param rfcfgOwner use null if this is not a class field, but a local variable, or for any reason the related console variable creation is to be skipped.
	 * @param evcm
	 */
	public VarCmdFieldAbs(IReflexFillCfg rfcfgOwner, EVarCmdMode evcm, VAL valueDefault, Class<VAL> clValueTypeConstraint, boolean bAllowNullValue){
		if(!bAllowNullValue)setDenyNullValue();
		
		ManageVarCmdFieldI.i().getClassReg().addClassesOf(this,true,false);
		this.evcm=evcm;
		
		setOwner(rfcfgOwner);
		
		this.clValueTypeConstraint=clValueTypeConstraint;
//		if(isField())
		ManageVarCmdFieldI.i().addHandled(this); //generic semi-dummy-manager just to hold the main list
//		Class<VAL> cl;
		switch(this.evcm){
			case Var:
			case VarCmd:
				setObjectRawValue(valueDefault,true); //DO NOT CALL THE PRIVATE ONE HERE! as it cant be overriden!
				break;
		}
	}
	
	private void setOwner(IReflexFillCfg rfcfgOwner){
		this.rfcfgOwner=rfcfgOwner;
		if(this.rfcfgOwner!=null){
			rscOwner.addClassesOf(this.rfcfgOwner,true,true);
		}
	}
	
	private Class<VAL> clValueTypeConstraint;

	private boolean	bAllowCallerAssignedToBeRunOnValueChange = true;

//	private StackTraceElement[]	asteDebugLastSetOrigin; 
	
//	public VarCmdFieldAbs(boolean bAddToList){
//		if(bAddToList)VarCmdFieldAbs.avcfList.add(this);
//	}
	
//	public void discardSelf(CommandsDelegator.CompositeControl ccSelf) {
//		ccSelf.assertSelfNotNull();
//		discardSelf();
//	}
//	/**
//	 * When discarding a class object that has fields of this class,
//	 * the global fields list {@link #avcfList} must also be updated/synchronized.
//	 */
//	public void discardSelf(VarCmdFieldManagerI.CompositeControl cc) {
//		cc.assertSelfNotNull();
//		
//		if(!VarCmdFieldAbs.avcfList.contains(this)){
//			/**
//			 * if owner is set, it should be on the main list,
//			 * shall not try to remove if not on the main list...
//			 */
//			throw new PrerequisitesNotMetException("inconsistency", this, getOwner(), VarCmdFieldAbs.avcfList);
//		}
//		
//		VarCmdFieldAbs.avcfList.remove(this);
//	}
	
	public CommandData getCmdData(){
		return this.cmdd;
	}
	
	public THIS setCmdData(CommandData cmdd){
		PrerequisitesNotMetException.assertNotAlreadySet("cmd data link to this var", this.cmdd, cmdd, this);
		this.cmdd=cmdd;
		return getThis();
	}
	
	/**
	 * The lazy value will be applied at the console var link's value (that must come null).
	 * @param cc
	 * @param cvar if the object already set is different from it, will throw exception
	 * @return
	 */
	public THIS setConsoleVarLink(CommandsDelegator.CompositeControl cc, ConsoleVariable cvar) {
		cc.assertSelfNotNull();
		
		// validate pre-conditions
		PrerequisitesNotMetException.assertNotNull("ConsoleVarLinkNew", cvar, this);
		PrerequisitesNotMetException.assertNotAlreadySet("ConsoleVarLinkCurrent", this.cvarLinkAndValueStorage, cvar, this);
		PrerequisitesNotMetException.assertNotAlreadySet("ConsoleVarOwner", cvar.getRestrictedVarOwner(), this);
		PrerequisitesNotMetException.assertNotAlreadySet("ConsoleVarValue", cvar.getRawValue(), cvar, this);
		PrerequisitesNotMetException.assertIsTrue("isRawLazyValueSet()", isRawLazyValueSet(), cvar, this);
		if(!isAllowNullValue() && isRawLazyValueNull()){//this.toString()
			MsgI.i().devWarn("Null values are not allowed but the lazy one is null.", cvar, this);
		}
		
		
//		/**
//		 * console var will come with a value to be set here
//		 */
////		boolean bSetNormalMode=false;
//		if(cvar.getRawValue()==null && isValueNull()){
//			/**
//			 * The console var link's value is null.
//			 * The lazy value is null too.
//			 * This will be called just to let the console variable do it's internal work, 
//			 * even if nulls are not allowed, just for completeness.
//			 */
//			cvar.setRawValue(ccSelf,null);
//		}else{
////			if(cvar.getRawValue()!=null){
////				throw new PrerequisitesNotMetException(
////					"The console var should not come with a value as the lazy one here was not applied at it yet.", 
////					cvar.getRawValue(), cvar, this);
////			}
//			
//			if(isAllowNullValue() || !isRawLazyValueNull()){
//				cvar.setRawValue(ccSelf,getRawValueLazy());
////				bSetNormalMode=true;
//			}else{
//				throw new PrerequisitesNotMetException(
//					"Null values are not allowed but the lazy one is null.",
//					cvar, this);
//			}
//		}
		
		/**
		 * simply/direcly apply the lazy value at the console variable
		 */
		cvar.setRawValue(ccSelf,objRawLazyValue);//getRawValueLazy());
		
		/**
		 * after console var checks above to not interfere in both setup
		 */
		cvar.setRestrictedVarOwner(this);
		
		/**
		 * lazy value is not required anymore, so clean it up
		 */
		bRawLazyValueAvailable=false;
		objRawLazyValue=null;
		
		/**
		 * The console var must be completely setup BEFORE this to avoid reference this backwards!!!!
		 * from this code line on, if this var is accessed, it's value will be based on 
		 * the console var link and not the lazy one anymore, even from withing this very method 
		 * even if it has not returned yet! so, this must be THE LAST THING!!!!
		 */
		this.cvarLinkAndValueStorage = cvar; //LAST THING!!
		rscConsVar.addClassesOf(cvarLinkAndValueStorage, true, true);
		
////		this.cvarLinkAndValueStorage.setRestrictedVarOwner(this);
//		if(bSetNormalMode){
//			/**
//			 * console var link must have already been set
//			 */
//			setObjectRawValue(getRawValueLazy());
//		}
		
		return getThis();
	}
	
	public ConsoleVariable getConsoleVarLink(CommandsDelegator.CompositeControl cc){
		cc.assertSelfNotNull();
		return this.cvarLinkAndValueStorage;
	}
	/**
	 * DEV: do not expose this one, let only subclasses use it, to avoid messing the field.
	 * @return
	 */
	protected ConsoleVariable getConsoleVarLink() {
		return cvarLinkAndValueStorage;
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
	
	protected THIS setUniqueId(VarCmdUId vcuid){
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
	public THIS setHelp(String strHelp) {
		PrerequisitesNotMetException.assertNotAlreadySet("help", this.strHelp, strHelp);
		
		if(strHelp!=null && !strHelp.isEmpty()){
			this.strHelp = strHelp;
		}
		
		return getThis();
	}
	
	/**
	 * this is safe to be public because it is just a report string
	 */
	@Override
	public String getFailSafeDebugReport(){
		String str="";
		if(vcuid==null){
			str+="(Not a class field.";
			if(getOwner()==null)str+=" No owner.";
			str+=")";
		}else{
			if(isCmd()){
				str+=getUniqueCmdId()+" ";
			}else
			if(isVar()){
				str+=getUniqueVarId();
			}
		}
		
		return str+" = " //+getValueReport();
			+getFailSafeDebugValueReport(getRawValueUnsafely())
			+"("+getFailSafeDebugValueReport(objRawValueDefault)+")";
	}
	
	protected String getFailSafeDebugValueReport(Object val){
		if(val==null)return ""+null;
		
		if(val instanceof String){
			return "\""+val+"\"";
		}
		
		return ""+val;
	}
	
//	private String getValueReport(){
//		return getFailSafeDebugValueReport(getRawValue())+"("+getFailSafeDebugValueReport(getRawValueDefault())+")";
//	}
	
	/**
	 * Let each concrete class determine the best naming for the main get() method
	 * @return
	 */
	protected VAL getValue(){
		return (VAL)getRawValue();
	}
	
	/**
	 * From the time when null is not allowed anymore:
	 * this must fail (if value is still null) to make it sure this method
	 * is not being called improperly, like in a place that would accept a null value
	 * but it was expected to NOT be a null value anymore! 
	 */
	public Object getRawValue(){
		assertConstructed();
		return assertIfNullValueIsAllowed(getRawValueUnsafely());
	}
	
	private Object getRawValueUnsafely(){
		if(cvarLinkAndValueStorage!=null){
			return cvarLinkAndValueStorage.getRawValue();
		}else{
			return objRawLazyValue;
		}
	}
	
	public boolean isRawLazyValueNull(){
		return objRawLazyValue==null;
	}
	
	/**
	 * this also works about the lazy one
	 * @return
	 */
	public boolean isValueNull(){
		return getRawValueUnsafely()==null;
	}
	
	/**
	 * implement only at concrete class (not the midlevel abstract ones)
	 * @return
	 */
	protected abstract THIS getThis();
	
	private void assertConstructed(){
		PrerequisitesNotMetException.assertIsTrue("constructed", bConstructed, this);
	}
	
	protected Object assertIfNullValueIsAllowed(Object objValue){
		if(!bAllowNullValue && objValue==null){
			throw new PrerequisitesNotMetException("null value not allowed",this);
		}
		return objValue;
	}
	
	/**
	 * see {@link #setObjectRawValue(Object, boolean, boolean)}
	 * 
	 * @param value
	 * @return
	 */
	public THIS setValue(VAL value) {
		setObjectRawValue(value);
		return getThis();
	}
	
	/**
	 * see {@link #setObjectRawValue(Object, boolean, boolean)}
	 * 
	 * @param objValue
	 * @return
	 */
	public THIS setObjectRawValue(Object objValue) { // do not use O at param here, ex.: bool toggler can be used on overriden
		setObjectRawValue(objValue,false); //MUST CALL an overridenable one
		return getThis();
	}
	
	/**
	 * see {@link #setObjectRawValue(Object, boolean, boolean)}
	 * @param objValue
	 * @param bPreventCallerRunOnce
	 * @return
	 */
	public THIS setObjectRawValue(Object objValue,boolean bPreventCallerRunOnce) { // do not use O at param here, ex.: bool toggler can be used on overriden
		setObjectRawValue(objValue,false,bPreventCallerRunOnce);
		return getThis();
	}
	
	RegisteredClasses<IReflexFillCfg> rscOwner = new RegisteredClasses<IReflexFillCfg>();
//	RegisteredClasses<IManager> rscManager = new RegisteredClasses<IManager>();
	RegisteredClasses<ConsoleVariable> rscConsVar = new RegisteredClasses<ConsoleVariable>();
	
//	private IManager<VarCmdFieldAbs>	imgr;

	private DebugData	dbg;
	
	private void assertSettingFromRegisteredType(){
		if(!isConstructed())return;
		if(getOwner()==null)return;
		
		// so it must be called directly at the latest setter
		StackTraceElement steSetter = Thread.currentThread().getStackTrace()[2];
		
//		for(StackTraceElement ste:asteDebugLastSetOrigin){
		RefHolder<StackTraceElement[]> rh = ManageDebugDataI.i().getStack(dbg,EDbgStkOrigin.LastSetValue);
		//rh.toString()
		for(StackTraceElement ste:rh.getRef()){
//			if(steSetter.getMethodName().equals(ste.getMethodName()))continue;
			if(ManageVarCmdFieldI.i().getClassReg().isContainClassTypeName(ste.getClassName()))continue;
			if(rscOwner.isContainClassTypeName(ste.getClassName()))break;//continue;
			if(rscConsVar.isContainClassTypeName(ste.getClassName()))break; //it's public set access is restricted to CommandsDelegator composite
			if(GlobalCommandsDelegatorI.i().getRegisteredClasses().isContainClassTypeName(ste.getClassName()))break; //it's public set access is restricted to CommandsDelegator composite
			if(ManageVarCmdFieldI.i().isVarManagerContainClassTypeName(ste.getClassName()))break;
//			if(rscManager.isContainClassTypeName(ste.getClassName()))break;
			
			throw new PrerequisitesNotMetException("not being set at owner class type", ste.getClassName(), getOwner().getClass().getName(), this);
		}
	}
	
	/**
	 * It is the unmodified/original value.
	 * If var link is not set, the raw value will be lazily stored.
	 * The value will be set at the console var link's value or here at the lazy one.
	 *  
	 * @param objValueNew on its overridens, a string value should always be parseable, as it can be the by-hand user input at console!
	 * @return
	 */
	private THIS setObjectRawValue(Object objValueNew, boolean bSetDefault, boolean bOnValueChangePreventCallerRunOnce) { // do not use O at param here, ex.: bool toggler can be used on overriden
		if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg,EDbgStkOrigin.LastSetValue);
//		asteDebugLastSetOrigin=Arrays.copyOfRange(Thread.currentThread().getStackTrace(),1,100); // 100 should suffice avoiding big array allocation... Short.MAX_VALUE); //least the 1st to easy the comparisons...
		if(RunMode.bValidateDevCode)assertSettingFromRegisteredType();
		assertIfNullValueIsAllowed(objValueNew);
		
		if(objValueNew!=null && !clValueTypeConstraint.isInstance(objValueNew)){
			throw new PrerequisitesNotMetException("cannot change value type", clValueTypeConstraint, objValueNew.getClass());
		}
		
		/**
		 * the first non null value being set will be the default if the default is already null
		 * TODO is this too much guessing/automacity? 
		 */
		if(!isAllowNullValue() && objValueNew!=null && objRawValueDefault==null){
			bSetDefault=true;
		}
		
		if(bSetDefault){ //can be null the default
			objRawValueDefault=(VAL)objValueNew;
		}
		
		Object objCurrentValue = null;
		if(cvarLinkAndValueStorage!=null){
			objCurrentValue = cvarLinkAndValueStorage.getRawValue();
//			if(vivo.getRawValue()!=objValue)prepareCallerAssigned(false);
			
			/**
			 * this cast is IMPORTANT!!!
			 * to grant the setup is in the right type!
			 */
			cvarLinkAndValueStorage.setRawValue(ccSelf,(VAL)objValueNew);
		}else{ // will work with lazy while there is no console var...
			bRawLazyValueAvailable=true; //such value can also be actually null
			
			// so updates just the lazy one
			objCurrentValue = this.objRawLazyValue; //before setting it of course! :P
			this.objRawLazyValue = (VAL)objValueNew; //cast restrictor/requirement
		}
		
		if(objCurrentValue!=objValueNew){
			boolean bAllowCallerRun=bAllowCallerAssignedToBeRunOnValueChange;
			if(bOnValueChangePreventCallerRunOnce)bAllowCallerRun=false;
			if(bAllowCallerRun){
				prepareCallerAssigned(false);
			}
		}
		
//		applyManager();
		
		return getThis();
	}
	
	protected void prepareCallerAssigned() {
		prepareCallerAssigned(false);
	}
	protected Boolean prepareCallerAssigned(boolean bRunNow) {
		if(isConstructed()){ 
			if(callerAssigned!=null){ 
				//TODO seems a bit confusing... more code (even if looking redundant) could make it more clear
				if(bRunNow || getCallerAssignedInfo().isAllowQueue()){
					callerAssigned.setRetryOnFail(!bRunNow);
					return ManageCallQueueI.i().addCall(callerAssigned,bRunNow);
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

	protected boolean isRawLazyValueSet(){
		return bRawLazyValueAvailable;
	}
	
	protected Object getRawValueLazy(){
		assertConstructed();
		return assertIfNullValueIsAllowed(objRawLazyValue);
	}
	
	/**
	 * used at {@link ReflexFillI#assertAndGetField()}
	 */
	@Override
	public boolean isReflexing() { // will only reflex for field name!
		return isField();
	}
	/**
	 * TODO make this precise, it is a wild guess..
	 * @return
	 */
	public boolean isField(){ // will be a field if it has an owner. Must have var link!
		return rfcfgOwner!=null;
	}

	@Override
	public String getCodePrefixVariant() {
		if(strCodePrefixVariant==null){ //already validated during its set
			strCodePrefixVariant=MiscI.i().assertGetValidId(null, getCodePrefixDefault());
		}
		
		return strCodePrefixVariant;
	}
	public THIS setCodePrefixVariant(String strCodePrefixVariant) {
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
	
	public String getValueAsString() {
//		VAL value = getValue();
//		if(value==null)return null; //value can be null
//		
//		return ""+value;
		return getValueAsString(-1);
	}
	/**
	 * 
	 * @param iFloatingPrecision if -1 will not apply precision restriction, and only works for floating values
	 * @return
	 */
	public String getValueAsString(int iFloatingPrecision) {
		Object value = getValue();
		if(value==null)return null;//value can be null
		
		if(iFloatingPrecision>=0){
			switch(EType.forClass(value.getClass())){
				case Double:
					value = MiscI.i().fmtFloat((double)value, iFloatingPrecision);
					break;
				case Float:
					value = MiscI.i().fmtFloat((float)value, iFloatingPrecision);
					break;
				case Boolean:
				case Int:
				case Long:
				case String:
					// keep empty
					break;
			}
		}
		
		return ""+value;
	}
	
	public VarCmdUId getIdTmpCopy() {
		return vcuid.clone();
	}
	
	public VAL getValueDefault(){
		return (VAL)getRawValueDefault();
	}
	
	public Object getRawValueDefault() {
		assertConstructed();
		return assertIfNullValueIsAllowed(objRawValueDefault);
//		return objRawValueDefault;
	}
	
	/**
	 * Changes on raw value will trigger this caller.
	 * Also, if this is a command, the caller can be directly invoked by the commands delegator (no queue). 
	 * 
	 * @param caller
	 * @return
	 */
	public THIS setCallerAssigned(CallableX caller){
		PrerequisitesNotMetException.assertNotAlreadySet("caller", this.callerAssigned, caller, this);
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
	@Override
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
			callerAssigned.getParamsForMaintenance().setReplaceAllParams(aobjParams);
		}
		
		return prepareCallerAssigned(true);
	}
	public void callerAssignedQueueNow(){
		prepareCallerAssigned(false);
	}
	
	public CallerInfo getCallerAssignedInfo(){
		return callerAssigned.getInfo();
	}
	
	public CallableX getCallerAssignedForMaintenance(CompositeControlAbs cc) {
		this.ccOwner = cc.assertSelfNotNullEqualsStored(this.ccOwner);
		
		return callerAssigned;
	}
	
	public boolean isAllowNullValue(){
		return this.bAllowNullValue;
	}
	
	/**
	 * will not allow null values after the time this is set!
	 * @return
	 */
	public THIS setDenyNullValue() {
		this.bAllowNullValue = false;
		return getThis();
	}
	
	public boolean isConsoleVarLinkSet(){
		return cvarLinkAndValueStorage!=null;
	}
	
	public boolean isConsoleVarLink(ConsoleVariable<VAL> cvar){
		return this.cvarLinkAndValueStorage==cvar;
	}

	public boolean isUniqueCmdIdEqualTo(String strCmdChk){
		chkAndInit();
		
		strCmdChk=strCmdChk.trim();
		
//		if(bIgnoreCaseOnComparison){
		return getUniqueCmdId().equalsIgnoreCase(strCmdChk); //useful for user typed commands
//		}else{
//			return getUniqueCmdId().equals(strCmdChk);
//		}
	}

	public void setAllowCallerAssignedToBeRun(boolean b) {
		this.bAllowCallerAssignedToBeRunOnValueChange=b;
	}

//	//@Override
//	public String toStringb() {
//		/**
//		 * this is allowed in debug mode so the IDE can show it's value
//		 */
//		if(!DebugI.i().isInIDEdebugMode()){
//			throw new PrerequisitesNotMetException("use getReport() instead!", this);
//		}
//		
//		
//	}	
//	
//	/**
//	 * this is allowed in debug mode to let the IDE show it's values 
//	 */
//	//@Override
//	public String toStringa() {
//	//	/**
//	//	 * enable the debug key to show improper use of toString()
//	//	 */
//	//	if(DebugI.i().isKeyEnabled(EDebugKey.VarToStringDenied)){
//		if(!DebugI.i().isInIDEdebugMode()){
//	//		boolean bIgnoreOnce=false; // bIgnoreOnce=true // evaluate in debug, to ignore once.
//	//		if(!bIgnoreOnce){
//				throw new PrerequisitesNotMetException("use getReport() instead!", this);
//	//		}
//		}
//		
//		String str="("+this.getClass().getName()+")";
//	//	if(DebugI.i().isInIDEdebugMode()){
//	//		try{
//	//			str+=getReport();
//	//		}catch(Exception ex){
//	////			System.err.println("[IGNOREING EXCEPTION IN DEBUG MODE]");
//	////			ex.printStackTrace();
//	//			str+="[IGNORING EXCEPTION IN DEBUG MODE]";
//	//		}
//	//	}else{
//			str+=getReport();
//	//	}
//		str+=" (" + super.toString() + ")";
//		
//		return str;
//	}

	/**
	 * This is intended to be used ONLY as non-reusable debug information!
	 * Use {@link #getFailSafeDebugReport()} for coding! 
	 */
	@Override
	public String toString() {
		return vcuid.toString();
	}
	
	@Override
	public ArrayList<IManager> getManagerList() {
//		return imgr;
//		return rscManager.getTargetList();
//		ArrayList<IManager> a = new ArrayList<IManager>();
//		a.add(ManageVarCmdFieldI.i().getManagerFor(VarCmdFieldAbs.class));
//		return a;
		return ManageVarCmdFieldI.i().getManagerListFor(VarCmdFieldAbs.class);
	}
	
//	@Override
//	public boolean isHasManagers() {
//		return rscManager.
//		return getManager()!=null;
//	}
		
	@Override
	public void addManager(IManager imgr) {
//		if(RunMode.bDebugIDE)PrerequisitesNotMetException.assertNotAlreadySet("manager", this.imgr,imgr,this);
		
//		this.imgr=imgr;
		
		if(!ManageVarCmdFieldI.i().isHasVarManager(imgr)){
			ManageVarCmdFieldI.i().putVarManager(imgr, VarCmdFieldAbs.class);
		}
//		rscManager.addSuperClassesOf(imgr, true, true);
//		rscManager.addSuperClassesOf(GlobalCommandsDelegatorI.i(), true, true);
//		rscManager.addSuperClassesOf(cvarLinkAndValueStorage, true, true); //it's public set access is restricted to CommandsDelegator composite
		
//		return getThis();
	}

	/**
	 * Override this to properly apply a manager 
	 */
//	protected void applyManager(){}
//	{
//		if(getManger()==null){
//			ManageVarCmdFieldI.i().add(this);
//			setManager(ManageVarCmdFieldI.i());
//		}
//	}
	
}
