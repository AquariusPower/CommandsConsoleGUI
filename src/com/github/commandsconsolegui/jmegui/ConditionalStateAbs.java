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

package com.github.commandsconsolegui.jmegui;

//import com.github.commandsconsolegui.jmegui.ReattachSafelyState.ERecreateConsoleSteps;
import java.io.IOException;
import java.util.ArrayList;

import com.github.commandsconsolegui.globals.GlobalHolderAbs.IGlobalOpt;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.misc.IConfigure;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.RetryOnFailure;
import com.github.commandsconsolegui.misc.RetryOnFailure.CompositeControl;
import com.github.commandsconsolegui.misc.RetryOnFailure.IRetryListOwner;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Node;

/**
 * Life cycle steps: configure, initialize, enable, disable, discard <br>
 * <br>
 * Every step can be validated and delayed until proper conditions are met.<br>
 * This allows for lazy assignments and instantiations: things will happen when they can.<br>
 * WARNING: a step not completed must self clean/undo before such method returns!<br>
 * <br>
 * After each step request, you must verify if it succeeded
 * before making new decisions.<br>
 * <br>
 * Most important steps will actually happen during {@link #doItAllProperly(float)},
 * where they will be properly validated and retried at specified time interval (default 0, every frame).<br>
 * <br>
 * SELFNOTE: Keep this class depending solely in JME!<br>
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class ConditionalStateAbs implements Savable,IGlobalOpt,IConfigure<ConditionalStateAbs>,IRetryListOwner{
	private StackTraceElement[] asteDbgInstance;
	
	public ConditionalStateAbs(){
		super();
		
		MiscI.i().assertFieldsHaveDefaultValue(this);
		
		asteDbgInstance = Thread.currentThread().getStackTrace();
		
		/**
		 * make a hollow instance while loading
		 */
		if(!MiscI.i().stackTraceContainsClass(asteDbgInstance,Savable.class)){
//			this.strCaseInsensitiveId=null;
//			this.bConfigured = bConfigured;
//			this.lLastMainTopCoreUpdateTimeMilis = lLastMainTopCoreUpdateTimeMilis;
			arList = new ArrayList<RetryOnFailure>();
			rInit = new RetryOnFailure(this,ERetryDelayMode.Init.s());
			rEnable = new RetryOnFailure(this,ERetryDelayMode.Enable.s());
			rUpdate = new RetryOnFailure(this,ERetryDelayMode.Update.s());
			rDisable = new RetryOnFailure(this,ERetryDelayMode.Disable.s());
			rDiscard = new RetryOnFailure(this,ERetryDelayMode.Discard.s());
//			this.bProperlyInitialized = bProperlyInitialized;
			this.bEnabled = false;
			this.bEnabledRequested = true;
//			this.bDisabledRequested = bDisabledRequested;
			this.bHoldProperInitialization = false;
//			this.bLastUpdateSuccessful = bLastUpdateSuccessful;
//			this.bHoldUpdates = bHoldUpdates;
//			this.bEnableSuccessful = bEnableSuccessful;
//			this.bHoldEnable = bHoldEnable;
//			this.bDisableSuccessful = bDisableSuccessful;
//			this.bHoldDisable = bHoldDisable;
//			this.bDiscardRequested = bDiscardRequested;
//			this.asmParent = asmParent;
//			this.bDiscarded = bDiscarded;
//			this.cfg = cfg;
//			this.icfgOfInstance = icfgOfInstance;
//			this.bRestartRequested = bRestartRequested;
//			this.bTryingToEnable = bTryingToEnable;
//			this.bTryingToDisable = bTryingToDisable;
		}
	}
	
	
	
	/**
	 * This Id is only required if there is more than one state of the same class.
	 */
	private String strCaseInsensitiveId;
	
//	public static interface IConditionalStateAbsForConsoleUI{
//		public abstract void requestRestart();
//	}
	
//	public static interface ICfgParm{}
	
//	private Node nodeGUI;
	
	// CONFIGURE 
	private boolean bConfigured;
	
	private Long lLastMainTopCoreUpdateTimeMilis = null;
	
//	/** true to delay initialization*/
//	private boolean bPreInitHold=false;
//	private boolean	bPreInitialized;
	
	private ArrayList<RetryOnFailure> arList;
	@Override
	public ArrayList<RetryOnFailure> getRetryListForManagement(RetryOnFailure.CompositeControl cc) {
		return arList;
	}
	
	public ArrayList<String> getRetryIdListCopy() {
		ArrayList<String> astr = new ArrayList<String>();
		for(RetryOnFailure r:arList){
			astr.add(r.getId());
		}
		return astr;
	}
//	protected static class RetryOnFailure implements IReflexFillCfg{
//		private long lStartMilis=0; // as this is only in case of failure, the 1st attempt will always be ready!
//		
//		/** 0 means retry at every update*/
//		private IntLongVarField ilvDelayMilis = new IntLongVarField(this, 0L, "retry delay between failed state mode attempts");
////		long lDelayMilis=0;
//
//		private String	strId;
//
//		private ConditionalStateAbs	cond;
//		
//		public RetryOnFailure(ConditionalStateAbs cond, String strId){
//			this.cond=cond;
//			this.strId=strId;
//			
//			if(cond.findRetryModeById(strId)!=null)throw new PrerequisitesNotMetException("conflicting retry id", strId); 
//			cond.aretry.add(this);
//		}
//		
//		public boolean isId(String strId){
//			return (this.strId.equals(strId));
//		}
//		
//		public void setRetryDelay(long lMilis){
//			this.ilvDelayMilis.setObjectRawValue(lMilis);
//		}
//		
//		/**
//		 * @return
//		 */
//		boolean isReadyToRetry(){
//			return cond.getUpdatedTime() > (lStartMilis+ilvDelayMilis.getLong());
//		}
//
//		public void resetStartTime() {
//			lStartMilis=cond.getUpdatedTime();
//		}
//
//		@Override
//		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//			ReflexFillCfg rfcfg = GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
//			if(rfcfg==null)rfcfg=new ReflexFillCfg(rfcv);
////			rfcfg.setPrefixCustomId(cond.getId());
//			rfcfg.setPrefixCustomId(cond.getId()+ReflexFillI.i().getCommandPartSeparator()+this.strId);
//			return rfcfg;
//		}
//		
//	}
	private RetryOnFailure rInit;
	private RetryOnFailure rEnable;
	private RetryOnFailure rUpdate;
	private RetryOnFailure rDisable;
	private RetryOnFailure rDiscard;
	
	// PROPERLY INIT
	private boolean	bProperlyInitialized;
	
	/** it must be initially disabled, the request will properly enable it*/
	private boolean	bEnabled;
	
	/** initially all will be wanted as enabled by default */
	private boolean	bEnabledRequested;
	private boolean	bDisabledRequested;
	
	/** set to true to allow instant configuration but wait before properly initializing*/
	private boolean bHoldProperInitialization;
	
	private boolean	bLastUpdateSuccessful;
	private boolean	bHoldUpdates;
	
	private boolean	bEnableSuccessful;
	private boolean	bHoldEnable;
	
	private boolean	bDisableSuccessful;
	private boolean	bHoldDisable;
	
	private boolean	bDiscardRequested;
	private AppState	asmParent;

	private boolean	bDiscarded;
	
	/**
	 * see {@link ICfgParm}
	 */
	public static class CfgParm implements ICfgParm{
//		private Application app;
		private String strId;
//		public CfgParm(Application app, String strId) {
		public CfgParm(String strId) {
			super();
//			this.app = app;
			this.strId = strId;
		}
		public String getId(){
			return strId;
		}
		public CfgParm setId(String strId){
			if(strId==null || strId.isEmpty()){
				throw new PrerequisitesNotMetException("invalid id", strId);
			}
			
			this.strId=strId;
			
			return this;
		}
	}
	private CfgParm	cfg;
	private ICfgParm icfgOfInstance;
	private boolean	bRestartRequested;

	private boolean	bTryingToEnable;

	private boolean	bTryingToDisable;
	
	/**
	 * Configure simple references assignments and variable values.<br>
	 * Configure must not be laze, cannot fail, throw exception is something cant be done!
	 * Must be used before initialization.<br>
	 * Put here only things that will not change on {@link #cleanupProperly()} !<br>
	 * Each sub-class must implement its own {@link CfgParm} ({@link ICfgParm}) to keep coding-flow 
	 * clear.<br>
	 * 
	 * @param cp
	 */
	@Override
	public ConditionalStateAbs configure(ICfgParm icfg){
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
		if(isDiscarded())throw new PrerequisitesNotMetException("cannot re-use after discarded");
		
//	private void configure(Application app){
		if(this.bConfigured)throw new PrerequisitesNotMetException("already configured");
		
		// internal configurations
//		if(cfg.app==null)throw new PrerequisitesNotMetException("app is null");
		
		// the Unique State Id
		if(cfg.strId==null){
//			throw new PrerequisitesNotMetException("id cant be null");
			cfg.strId = MiscI.i().getClassName(this,true);
		}
		
		@SuppressWarnings("unchecked") //so obvious...
		ConditionalStateAbs csa = ConditionalStateManagerI.i().getConditionalState(
			(Class<ConditionalStateAbs>)this.getClass(), cfg.strId);
		if(csa!=null)throw new PrerequisitesNotMetException("conflicting state Id "+cfg.strId);
		this.strCaseInsensitiveId = cfg.strId;
		
		if(!ConditionalStateManagerI.i().attach(this)){
//		if(!app.getStateManager().attach(this)){
			throw new PrerequisitesNotMetException("state already attached: "
				+this.getClass()+"; "+this);
		}
//		preInitRequest();
		
		// configs above
		this.bConfigured=true;
		MsgI.i().dbg("cfg",this.bConfigured,this);
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	/**
	 * Implementation methodology:
	 * 
	 * This can be used in every sub-class {@link #configure(ICfgParm)} method,
	 * the last one, the instantiated, will overwrite the value!
	 * 
	 * This is most useful in case a class is modified to be abstract.
	 * 
	 * @param icfg
	 * @return
	 */
	protected <T extends ConditionalStateAbs> T storeCfgAndReturnSelf(ICfgParm icfg){
		if(this.icfgOfInstance!=null && this.icfgOfInstance!=icfg){
			throw new PrerequisitesNotMetException(
				"cfg already set", this, this.icfgOfInstance, icfg);
		}
		
		if(!MiscI.i().isInnerClassOfConcrete(icfg, this)){
//		if(!icfg.getClass().getTypeName().startsWith(this.getClass().getTypeName()+"$")){
			throw new PrerequisitesNotMetException(
				"must be the cfg of the concrete class one", this, icfg);
		}
		
		this.icfgOfInstance=icfg;
		return (T)this;
	}
	
	private ICfgParm getCfg(){
		return icfgOfInstance;
	}
	
//	private void msgDbg(String str, boolean bSuccess) {
//		MsgI.i().dbg(str, bSuccess, this);
//	}

	@Override
	public boolean isConfigured(){
		return bConfigured;
	}
	
	
	
	/**
	 * you can override system time with your simulation time.
	 */
	protected void updateLastMainTopCoreUpdateTimeMilis(){
		lLastMainTopCoreUpdateTimeMilis = System.currentTimeMillis();
	}
	
	@Override
	public long getLastMainTopCoreUpdateTimeMilis(){
		return lLastMainTopCoreUpdateTimeMilis;
	}
	
	public Application getApp(){
		return app();
	}
	public Application app(){
		return GlobalAppRefI.i();
//		return cfg.app;
	}
	
	public boolean isInitializedProperly(){
		return bProperlyInitialized;
	}
	
	/**
	 * tip to use {@link #setHoldUpdates(boolean)}
	 * @return
	 */
	public boolean isLastUpdateSucessful(){
		return bLastUpdateSuccessful;
	}
	
	public boolean isHoldEnable() {
		return bHoldEnable;
	}

	public void setHoldEnable(boolean bHoldEnable) {
		this.bHoldEnable = bHoldEnable;
	}

	public boolean isHoldDisable() {
		return bHoldDisable;
	}

	public void setHoldDisable(boolean bHoldDisable) {
		this.bHoldDisable = bHoldDisable;
	}
	
	public boolean isHoldUpdates() {
		return bHoldUpdates;
	}
	
	public void setHoldUpdates(boolean b){
		this.bHoldUpdates=b;
	}
	
	public boolean isEnableSuccessful() {
		return bEnableSuccessful;
	}

	public boolean isDisableSuccessful() {
		return bDisableSuccessful;
	}
	
	/**
	 * store the cfg object with {@link ConditionalStateAbs#storeCfgAndReturnSelf()}
	 * @return
	 */
	protected boolean initCheckPrerequisites(){
		if(icfgOfInstance==null)throw new PrerequisitesNotMetException(
			"the instantiated class needs to set the configuration params to be used on a restart!");
		
		if(!MiscI.i().isInnerClassOfConcrete(icfgOfInstance, this)){
//		if(!icfgOfInstance.getClass().getName().startsWith(this.getClass().getName()+"$")){
//		if(!icfgOfInstance.getClass().equals(this.getClass())){
			throw new PrerequisitesNotMetException(
				"The stored cfg params must be of the instantiated class to be used on restarting it. "
				+this.getClass().getName());
		}
		
		return true;
	}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean initAttempt(){return true;}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean updateAttempt(float tpf){return true;}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean enableAttempt(){return true;}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean disableAttempt(){return true;}
	
	private boolean doItInitializeProperly(float tpf){
		if(bHoldProperInitialization)return false;
		
		if(!rInit.isReady())return false;
		
		if(!initCheckPrerequisites() || !initAttempt()){
			initFailed();
			rInit.resetStartTime();
			MsgI.i().dbg("init",false,this);
			return false;
		}else{
			initSuccess();
		}
		
		bProperlyInitialized=true;
		MsgI.i().dbg("init",bProperlyInitialized,this);
		
		return true;
	}
	
	/**
	 * This will:
	 * - Initialize properly
	 * - enable/disable properly
	 * - update properly
	 * 
	 * Use {@link #updateAttempt(float)}<br>
	 */
	public boolean doItAllProperly(ConditionalStateManagerI.CompositeControl cc, float tpf) {
		cc.assertSelfNotNull();
		assertConfigured();
		
		updateLastMainTopCoreUpdateTimeMilis();
//		assertIsPreInitialized();
//		if(bRestartRequested)return false;
//		if(bDiscardRequested)return false;
		
		if(!bProperlyInitialized){
			if(bEnabledRequested){
				if(!doItInitializeProperly(tpf))return false;
			}
		}else{
			if(!doUpdateEnableDisableProperly(tpf))return false;
		}
		
		return true;
	};
	
	/**
	 * @param tpf
	 * @return
	 */
	private boolean doUpdateEnableDisableProperly(float tpf) {
		if(bEnabledRequested && !bEnabled){
			bTryingToEnable=true;
			bTryingToDisable=false;
			if(bHoldEnable)return false;
			if(!rEnable.isReady())return false;
			
			bEnableSuccessful=enableAttempt();
//			MsgI.i().dbg("enabled",bEnableSuccessful,this);
			bEnabled=bEnableSuccessful;
			
			if(bEnableSuccessful){
				enableSuccess();
				bEnabledRequested=false; //otherwise will keep trying
			}else{
				enableFailed();
				rEnable.resetStartTime();
			}
		}else
		if(bDisabledRequested && bEnabled){
			bTryingToDisable=true;
			bTryingToEnable=false;
			if(bHoldDisable)return false;
			if(!rDisable.isReady())return false;
			
			bDisableSuccessful=disableAttempt();
//			MsgI.i().dbg("disabled",bDisableSuccessful,this);
			bEnabled=!bDisableSuccessful;
			
			if(bDisableSuccessful){
				disableSuccess();
				bDisabledRequested=false; //otherwise will keep trying
			}else{
				disableFailed();
				rDisable.resetStartTime();
			}
		}
		
		if(bEnabled){
			if(bHoldUpdates)return false;
			if(!rUpdate.isReady())return false;
			
			bLastUpdateSuccessful = updateAttempt(tpf);
			if(bLastUpdateSuccessful){
				updateSuccess();
			}else{
				updateFailed();
				rUpdate.resetStartTime();
				
				MsgI.i().dbg("update",false,this); //only on fail to avoid too much log!
				return false;
			}
			/**
			 * SelfNote: More code can go only here.
			 */
		}
		
		return true;
	}

	protected abstract void enableFailed();
	protected abstract void disableFailed();
	protected abstract void enableSuccess();
	protected abstract void disableSuccess();
	protected abstract void initSuccess();
	protected abstract void initFailed();
	protected void updateSuccess(){}
	protected void updateFailed(){}

	public void requestToggleEnabled(){
//		assertIsPreInitialized();
		setEnabledRequest(!isEnabled());
	}
	
	/**
	 * use {@link #requestEnable()} or {@link #requestDisable()} instead
	 * 
	 * @param bEnabledRequested if false, will be a Disable Request.
	 */
	public void setEnabledRequest(boolean bEnabledRequested) {
//		assertIsPreInitialized();
		
		if(bEnabledRequested){
			/**
			 * this exception must be here because this request will only be listened if the state
			 * is configured (threfore added to the state list).
			 */
			if(!this.bConfigured)throw new PrerequisitesNotMetException("not configured yet: "+this);
			
			this.bEnabledRequested = true;
			this.bDisabledRequested = false;
		}else{
			this.bDisabledRequested = true;
			this.bEnabledRequested = false;
		}
//		lEnableDisableRequestTimeMilis=updateTime();
	};
	
	/**
	 * this will simply avoid initially enabling under request
	 */
	protected void initiallyDisabled(){
		this.bEnabledRequested = false;
	}
	
	public void requestEnable(){
		setEnabledRequest(true);
	}
	
	public void requestDisable(){
		setEnabledRequest(false);
	}
	
	public boolean isEnabled() {
		return bEnabled;
	}
	
	public boolean isDiscarding() {
		return bDiscardRequested;
	}
	
	/**
	 * By discarding, it will be easier to setup a fresh, consistent and robust state.
	 * If you need values from the discarded state, just copy/move/clone them to the new one.
	 */
	public boolean prepareAndCheckIfReadyToDiscard(ConditionalStateManagerI.CompositeControl cc) {
		cc.assertSelfNotNull();
		if(!bProperlyInitialized)return false; //TODO log warn
		if(!isDiscarding())throw new PrerequisitesNotMetException("not discarding");
		
		if(bEnabled){
			boolean bRetry=false;
			
			if(!rDiscard.isReady()){
//			if(getUpdatedTime() < (lCleanupRequestMilis+lCleanupRetryDelayMilis)){
				bRetry=true;
			}else{
				bRetry = !disableAttempt();
				if(bRetry){ //failed, retry
					rDiscard.resetStartTime();
//					lCleanupRequestMilis=getUpdatedTime();
				}
			}
			
			if(bRetry){
				MsgI.i().dbg(""+ELogAction.discard,false,this);
				return false;
			}
		}
		
		asmParent=null;
		
		MsgI.i().dbg(""+ELogAction.discard,true,this);
		
		return true;
	}
	
	enum ELogAction{
		discard,
	}
	
	@Override
	public boolean isDiscarded(){
		return bDiscarded;
	}
	
	public boolean isAttachedToManager(){
		return asmParent!=null;
	}
	
	public AppState getAppStateManagingThis(){
		return asmParent;
	}
	
	public Node getNodeGUI() {
//		return nodeGUI;
		return GlobalGUINodeI.i();
	}

//	protected void setNodeGUI(Node nodeGUI) {
//		if(nodeGUI==null)throw new PrerequisitesNotMetException("null node gui");
//		this.nodeGUI = nodeGUI;
//	}
	
	public void setAppStateManagingThis(ConditionalStateManagerI.CompositeControl cc,ConditionalStateManagerI asmParent) {
		cc.assertSelfNotNull();
//		assertCompositeControlNotNull(cc);
		if(this.asmParent!=null)throw new PrerequisitesNotMetException(
			"already managed by "+this.asmParent+"; request made to attach at "+asmParent);
		this.asmParent=asmParent;
	}
	
	/**
	 * Discard the state object is better because:
	 * It is a sure vanilla instance of the object to be properly initialized.
	 * No unwanted child (fields) state will remain.
	 * 
	 * If anything is required to be on the new instance, just copy it's value.
	 */
	public void requestDiscard(){
		bDiscardRequested = true;
	}
	
	/**
	 * Everything that is not initially configured can be copied thru this method,
	 * like the current state/value of anything.
	 * 
	 * @param cas
	 * @return
	 */
	public ConditionalStateAbs copyCurrentValuesFrom(ConditionalStateAbs cas){
		return this;
	}
	
	public ConditionalStateAbs createAndConfigureSelfCopy() {
		try {
			return this.getClass().newInstance().configure(icfgOfInstance).copyCurrentValuesFrom(this);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PrerequisitesNotMetException("new instance configuration failed", this)
				.initCauseAndReturnSelf(e);
//			NullPointerException npe = new NullPointerException("new instance configuration failed");
//			npe.initCause(e);
//			throw npe;
		}
	}

	public void applyDiscardedStatus(ConditionalStateManagerI.CompositeControl cc) {
		cc.assertSelfNotNull();
		bDiscarded=true;
	}
	
	public void requestRestart(){
		bRestartRequested = true;
	}
	
	public boolean isRestartRequested() {
		return bRestartRequested;
	}
	
	@Override
	public void assertConfigured(){
		if(!isConfigured()){
			throw new PrerequisitesNotMetException("not configured yet!", this);
		}
	}
	
	public void assertInitializedProperly(){
		if(!isInitializedProperly()){
			throw new PrerequisitesNotMetException("not properly initialized yet!", this);
		}
	}
	
	/**
	 * @return
	 */
	public String getId() {
		assertConfigured();
		
		if(strCaseInsensitiveId==null){
			throw new PrerequisitesNotMetException("id cant be null", 
				this.isConfigured()?"configured(ok)":"this object was not configured!!!", 
				this);
		}
		return strCaseInsensitiveId;
	}
	
//	/**
//	 * see {@link #setRetryDelay(EDelayMode, long)}
//	 * @param lMilis
//	 */
//	public void setRetryDelay(long lMilis){
//		prepareRetryDelay(null, lMilis);
//	}
	
	/**
	 * each state can have its delay value set individually also.
	 * 
	 * @param e
	 * @param lMilis if null will not apply
	 * @return the previously setup delay milis if the mode was specified 
	 */
	protected void setRetryDelayFor(Long lMilis, String... astrId){
		if(astrId.length==0){
//			astrId=Arrays.asList(ERetryDelayMode.values()).toArray(new String[0]);
			astrId=new String[ERetryDelayMode.values().length];
			for(ERetryDelayMode e:ERetryDelayMode.values())astrId[e.ordinal()]=e.toString();
		}
		
		for(String strId:astrId){
			RetryOnFailure.findRetryModeById(arList,strId).setRetryDelay(lMilis);
		}
	}
//	protected Long prepareRetryDelay(EDelayMode e, Long lMilis){
//		EDelayMode[] ae = {e};
//		
//		if(e==null)ae=EDelayMode.values();
//		
//		for(EDelayMode eDoIt:ae){
//			Retry.find(eDoIt.s()).setRetryDelay(lMilis);
////			switch(eDoIt){
////				case Init:
////					rInit.setRetryDelay(lMilis);
////					break;
////				case Enable:
////					rEnable.setRetryDelay(lMilis);
////					break;
////				case Update:
////					rUpdate.setRetryDelay(lMilis);
////					break;
////				case Disable:
////					rDisable.setRetryDelay(lMilis);
////					break;
////				case Discard:
////					rDiscard.setRetryDelay(lMilis);
////					break;
////			}
//		}
//		
//		if(e!=null){
//			return Retry.find(e.s()).lDelayMilis;
//		}
//		
//		return null;
//	}
	
	public static enum ERetryDelayMode{
		Init,
		Enable,
		Update,
		Disable,
		Discard,
		;
		public String s(){return this.toString();}
	}
	
  @Override
	public void write(JmeExporter ex) throws IOException{
  };
  @Override
	public void read(JmeImporter im) throws IOException{
  }

	public boolean isTryingToEnable() {
		return bTryingToEnable;
	}

	public boolean isTryingToDisable() {
		return bTryingToDisable;
	}

//	public boolean applyBoolTogglerChange(BoolTogglerCmdField btgSource) {
//		return false;
//	};
}
