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

//import com.github.commandsconsolegui.jmegui.ReattachSafelyState.ERecreateConsoleSteps;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.SimulationTime.ISimulationTime;
import com.github.commandsconsolegui.spAppOs.globals.GlobalHolderAbs.IGlobalOpt;
import com.github.commandsconsolegui.spAppOs.globals.GlobalSimulationTimeI;
import com.github.commandsconsolegui.spAppOs.misc.HoldRestartable;
import com.github.commandsconsolegui.spAppOs.misc.ICleanExit;
import com.github.commandsconsolegui.spAppOs.misc.IHandled;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.IRefresh;
import com.github.commandsconsolegui.spAppOs.misc.IRestartable;
import com.github.commandsconsolegui.spAppOs.misc.ISingleInstance;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI;
import com.github.commandsconsolegui.spAppOs.misc.RegisteredClasses;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI.IConfigure;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.Priority.EPriority;
import com.github.commandsconsolegui.spAppOs.misc.Priority.IPriority;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.Request;
import com.github.commandsconsolegui.spAppOs.misc.RetryOnFailure;
import com.github.commandsconsolegui.spAppOs.misc.RetryOnFailure.IRetryListOwner;
import com.github.commandsconsolegui.spCmd.varfield.ManageVarCmdFieldI;
import com.github.commandsconsolegui.spJme.globals.GlobalAppRefI;
import com.github.commandsconsolegui.spJme.globals.GlobalGUINodeI;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
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
 * DevSelfNote: Keep this class depending solely in JME!<br>
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class ConditionalStateAbs<THIS extends ConditionalStateAbs<THIS>> implements IGlobalOpt,IRestartable,ISimulationTime,IConfigure<ConditionalStateAbs<THIS>>,IRetryListOwner,IReflexFillCfg,IRefresh,IPriority,IHandled,ISingleInstance,ICleanExit{
//	public static final class CompositeControl extends CompositeControlAbs<ConditionalStateAbs>{
//		private CompositeControl(ConditionalStateAbs casm){super(casm);};
//	};private CompositeControl ccSelf = new CompositeControl(this);
	
	private StackTraceElement[] asteDbgInstance;
	
	public ConditionalStateAbs(){
		super();
		
		DelegateManagerI.i().addHandled(this);
//		ManageSingleInstanceI.i().add(this);
//		MiscI.i().assertFieldsHaveDefaultValue(this);
		
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
//			this.bEnabledRequested = true;
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
	
	private EPriority esp=EPriority.Normal;
	public EPriority getPriority(){
		return esp;
	}
	protected void setPriority(EPriority esp){
		this.esp=esp;
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
			astr.add(r.getUniqueId());
		}
		return astr;
	}
//	protected static class RetryOnFailure implements IReflexFillCfg{
//		private long lStartMilis=0; // as this is only in case of failure, the 1st attempt will always be ready!
//		
//		/** 0 means retry at every update*/
//		private final IntLongVarField ilvDelayMilis = new IntLongVarField(this, 0L, "retry delay between failed state mode attempts");
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
//	private boolean	bEnabledRequested;
	private Request reqEnable = new Request(this).requestNow();
	private Request reqDisable = new Request(this);
//	private boolean	bDisabledRequested;
	
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
		private boolean	bRestartIsNewInstance;
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
		public boolean isRestartNewInstance() {
			return bRestartIsNewInstance;
		}
	}
	private CfgParm	cfg;
//	private ICfgParm icfgOfInstance;
	private boolean	bRestartRequested;

	private boolean	bTryingToEnable;

	private boolean	bTryingToDisable;

	private HoldRestartable<IRestartable>	hchHolder;

	private boolean	bWasEnabled;

//	private boolean	bInstancedFromRestart;

	private boolean	bFirstEnableDone;

	public boolean isWasEnabledBeforeRestarting(){
		return bWasEnabled;
	}

//	private long	lLastUpdateTimeNano;
	
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
	public THIS configure(ICfgParm icfg){
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
		if(isDiscarded())throw new PrerequisitesNotMetException("cannot re-use after discarded");
		
		if(this.bConfigured)throw new PrerequisitesNotMetException("already configured");
		
		/**
		 * Internal Configurations
		 */
		// the Unique State Id
		if(cfg.strId==null){
			cfg.strId = MiscI.i().getClassName(this,true);
		}
		
		ConditionalStateAbs csa = ManageConditionalStateI.i().getConditionalState(this.getClass(), cfg.strId);
		if(csa!=null)throw new PrerequisitesNotMetException("conflicting state Id "+cfg.strId);
		this.strCaseInsensitiveId = cfg.strId;
		
//		if(!ManageConditionalStateI.i().attach(this)){
//			throw new PrerequisitesNotMetException("state already attached: "
//				+this.getClass()+"; "+this);
//		}
		
		// configs above
		this.bConfigured=true;
		MsgI.i().debug("cfg",this.bConfigured,this);
		
		storeCfg(cfg);
		return getThis();
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
//	protected <T extends ConditionalStateAbs> T storeCfgAndReturnSelf(ICfgParm icfg){
	private THIS storeCfg(CfgParm icfg){
		if(this.cfg!=null && this.cfg!=icfg){
			throw new PrerequisitesNotMetException(
				"cfg already set", this, this.cfg, icfg);
		}
		
		validateCfg(icfg);
		
		this.cfg=icfg;
		
		return getThis();
//		return (T)this;
	}
	
	protected void validateCfg(CfgParm cfg){
		if(!MiscI.i().isInnerClassOfConcrete(cfg, this)){
			if(!MiscI.i().isGetThisTrickImplementation(this)){
				throw new PrerequisitesNotMetException(
					"The stored "+CfgParm.class.getSimpleName()+" must be the cfg of the instantiated concrete class one, so it can be used to restart it", this, cfg);
			}
		}
	}
	
//	private ICfgParm getCfg(){
//		return icfgOfInstance;
//	}
	
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
	public long getCurrentTimeMilis(){
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
		if(cfg==null)throw new PrerequisitesNotMetException(
			"the instantiated class needs to set the configuration params to be used on a restart!");
		
		validateCfg(cfg);
//		if(!MiscI.i().isInnerClassOfConcrete(cfg, this)){
////		if(!icfgOfInstance.getClass().getName().startsWith(this.getClass().getName()+"$")){
////		if(!icfgOfInstance.getClass().equals(this.getClass())){
//			throw new PrerequisitesNotMetException(
//				"The stored cfg params must be of the instantiated class to be used on restarting it. "
//				+this.getClass().getName());
//		}
		
		return true;
	}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean initAttempt(){return true;}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean updateAttempt(float tpf){
//		lLastUpdateTimeNano=System.nanoTime();
		return true;
	}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean enableAttempt(){return true;}
	/** a failed attempt may be undone or it can just be gradually stepping towards success */
	protected boolean disableAttempt(){return true;}
	
//	public long getLastUpdateTimeNano(){
//		return lLastUpdateTimeNano;
//	}
	
	private boolean doItInitializeProperly(float tpf){
		if(bHoldProperInitialization)return false;
		
		if(!rInit.isReady())return false;
		
		if(!initCheckPrerequisites() || !initAttempt()){
			initFailed();
			rInit.updateStartTime();
			MsgI.i().debug("init",false,this);
			return false;
		}else{
			initSuccess();
		}
		
		bProperlyInitialized=true;
		MsgI.i().debug("init",bProperlyInitialized,this);
		
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
	public boolean doItAllProperly(ManageConditionalStateI.CompositeControl cc, float tpf) {
		cc.assertSelfNotNull();
		ManageConfigI.i().assertConfigured(this);
		
		updateLastMainTopCoreUpdateTimeMilis();
//		assertIsPreInitialized();
//		if(bRestartRequested)return false;
//		if(bDiscardRequested)return false;
		
		if(!bProperlyInitialized){
			if(reqEnable.isReady()){
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
		if(reqEnable.isReady() && !bEnabled){
			bTryingToEnable=true;
			bTryingToDisable=false;
			if(bHoldEnable)return false;
			if(!rEnable.isReady())return false;
			
			bEnableSuccessful=enableAttempt();
//			MsgI.i().dbg("enabled",bEnableSuccessful,this);
			bEnabled=bEnableSuccessful;
			
			if(bEnableSuccessful){
				enableSuccess();
				reqEnable.reset(); //otherwise will keep trying
			}else{
				enableFailed();
				rEnable.updateStartTime();
			}
		}else
		if(reqDisable.isReady() && bEnabled){
			bTryingToDisable=true;
			bTryingToEnable=false;
			if(bHoldDisable)return false;
			if(!rDisable.isReady())return false;
			
			bDisableSuccessful=disableAttempt();
//			MsgI.i().dbg("disabled",bDisableSuccessful,this);
			bEnabled=!bDisableSuccessful;
			
			if(bDisableSuccessful){
				disableSuccess();
				reqDisable.reset(); //otherwise will keep trying
			}else{
				disableFailed();
				rDisable.updateStartTime();
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
				rUpdate.updateStartTime();
				
				MsgI.i().debug("update",false,this); //only on fail to avoid too much log!
				return false;
			}
			/**
			 * SelfNote: More code can go only here.
			 */
		}
		
		return true;
	}

	protected void enableFailed(){}
	protected void disableFailed(){}
	protected void enableSuccess(){
		bFirstEnableDone=true;
	}
	protected void disableSuccess(){}
	protected void initSuccess(){}
	protected void initFailed(){}
	protected void updateSuccess(){}
	protected void updateFailed(){}
	
	public boolean isFirstEnableDone(){
		return this.bFirstEnableDone;
	}
	
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
		assertNotDiscarded();
//		assertIsPreInitialized();
		
		if(bEnabledRequested){
			/**
			 * this exception must be here because this request will only be listened if the state
			 * is configured (threfore added to the state list).
			 */
			if(!this.bConfigured)throw new PrerequisitesNotMetException("not configured yet: "+this);
			
			if(!isEnabled()){
				reqEnable.requestNow();
				reqDisable.reset();
			}
		}else{
			if(isEnabled()){
				reqEnable.reset();
				reqDisable.requestNow();
			}
		}
	}
	
	protected void cancelEnableRequest(){
		if(reqEnable.isRequestActive()){
			reqEnable.reset();
		}
	}
	
	public boolean isDisabling(){
		return reqDisable.isRequestActive();
	}
	
	public boolean isEnabling(){
		return reqEnable.isRequestActive();
	}
	
	private void assertNotDiscarded(){
		if(isDiscarded())throw new PrerequisitesNotMetException("cannot use a discarded state", this);
	}
	
	/**
	 * this will simply avoid initially enabling under request
	 */
	protected void initiallyDisabled(){
		reqEnable.reset();
//		this.bEnabledRequested = false;
	}
	
//	public boolean isInstancedFromRestart(){
//		return this.bInstancedFromRestart;
//	}
	
	public void requestEnable(){
		setEnabledRequest(true);
	}
	
	public void requestDisable(){
		setEnabledRequest(false);
	}
	
	public boolean isEnabled() {
		return bEnabled;
	}
	
	@Override
	public boolean isBeingDiscarded() {
		return bDiscardRequested;
	}
	
//	private void removeAllOwnedVarCmdFieldsFromMainList(){
//		//remove all of it's owned fields from the list
//		for(VarCmdFieldAbs vcf:VarCmdFieldAbs.getListFullCopy()){
//			if(vcf.getOwner()==this){
//				vcf.discardSelf(ccSelf);
//			}
//		}
//	}
	
	/**
	 * By discarding, it will be easier to setup a fresh, consistent and robust state.
	 * If you need values from the discarded state, just copy/move/clone them to the new one.
	 */
	public boolean prepareToDiscard(ManageConditionalStateI.CompositeControl cc) {
		cc.assertSelfNotNull();
		if(!bProperlyInitialized)return false; //TODO log warn
		if(!isBeingDiscarded())throw new PrerequisitesNotMetException("not discarding");
		
		ManageVarCmdFieldI.i().removeAllWhoseOwnerIsBeingDiscarded();
		
		if(isEnabled()){
			boolean bRetry=false;
			
			if(!rDiscard.isReady()){
//			if(getUpdatedTime() < (lCleanupRequestMilis+lCleanupRetryDelayMilis)){
				bRetry=true;
			}else{
				bRetry = !disableAttempt(); //TODO request disable instead?
				if(bRetry){ //failed, retry
					rDiscard.updateStartTime();
//					lCleanupRequestMilis=getUpdatedTime();
				}
			}
			
			if(bRetry){
				MsgI.i().debug(""+ELogAction.discard,false,this);
				return false;
			}
		}
		
		asmParent=null;
		
		MsgI.i().debug(""+ELogAction.discard,true,this);
		
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
	
	public void setAppStateManagingThis(ManageConditionalStateI.CompositeControl cc,ManageConditionalStateI asmParent) {
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
	 * @param casDiscarding
	 * @return
	 */
	public THIS copyToSelfValuesFrom(THIS casDiscarding){
//		casDiscarding.putRestartCfgAt(this);
//		this.putRestartCfgAt(t);
//		setRestartCfg(casDiscarding, rcfg)
//		cas.getHolder(this.getClass()).isChangedAndUpdateHash(this);
//		bInstancedFromRestart=true;
//		HoldRestartable.updateAllRestartableHolders(casDiscarding, this);
//		cas.getHolder().setHolded(this);
		return getThis();
	}
	
//	protected void putRestartCfgAt(ConditionalStateAbs csa){
//		csa.setRestartCfg(this, rcfgForNewInstance);
//	}
	
//	public THIS setInstancedFromRestart(ConditionalStateManagerI.CompositeControl cc){
//		cc.assertSelfNotNull();
//		rcfg.setInstancedFromRestart(true);
////		bInstancedFromRestart=true;
//		return getThis();
//	}
	
//	public static class RestartCfg{}
//	
//	private RestartCfg rcfgForNewInstance;
//	private RestartCfg rcfgSelf;
//	protected RestartCfg getRestartCfg() {
//		return rcfgSelf;
//	}
//	public RestartCfg getRestartCfg(ConditionalStateManagerI.CompositeControl cc) {
//		cc.assertSelfNotNull();
//		return getRestartCfg();
//	}
//	public boolean isRestartCfgSet(){
//		return rcfgSelf!=null;
//	}
//	/**
//	 * Use only on the concrete class.
//	 * @param rcfg
//	 * @return
//	 */
//	protected THIS setRestartCfg(ConditionalStateAbs csaDiscarding, RestartCfg rcfg){
//		PrerequisitesNotMetException.assertIsTrue("discarding", csaDiscarding.isBeingDiscarded(), csaDiscarding, rcfg, this);
//		PrerequisitesNotMetException.assertNotAlreadySet("Restart Cfg", this.rcfgSelf, rcfg, this);
//		
//		this.rcfgSelf=rcfg;
//		
//		return getThis();
//	}
	
	public THIS createAndConfigureSelfCopy() {
		try {
			cfg.bRestartIsNewInstance=(true);
			THIS t = (THIS)this.getClass().newInstance().configure(cfg).copyToSelfValuesFrom(this);
//			this.putRestartCfgAt(t);
			return t;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PrerequisitesNotMetException("new instance configuration failed", this)
				.initCauseAndReturnSelf(e);
//			NullPointerException npe = new NullPointerException("new instance configuration failed");
//			npe.initCause(e);
//			throw npe;
		}
	}

	public void applyDiscardedStatus(ManageConditionalStateI.CompositeControl cc) {
		cc.assertSelfNotNull();
		bDiscarded=true;
	}
	
	public void requestRestart(){
		bRestartRequested = true;
		bWasEnabled=isEnabled();
	}
	
	public boolean isRestartRequested() {
		return bRestartRequested;
	}
	
//	@Override
//	public void assertConfigured(){
//		if(!isConfigured()){
//			throw new PrerequisitesNotMetException("not configured yet!", this);
//		}
//	}
	
	public void assertInitializedProperly(){
		if(!isInitializedProperly()){
			throw new PrerequisitesNotMetException("not properly initialized yet!", this);
		}
	}
	
	/**
	 * @return
	 */
	@Override
	public String getUniqueId() {
		ManageConfigI.i().assertConfigured(this); //because the id may depend on custom prefixes available only after configure
		
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
			MiscI.i().findByUniqueId(arList,strId).setRetryDelay(lMilis);
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
	
	public boolean isTryingToEnable() {
		return bTryingToEnable;
	}

	public boolean isTryingToDisable() {
		return bTryingToDisable;
	}

//	public boolean applyBoolTogglerChange(BoolTogglerCmdField btgSource) {
//		return false;
//	};
	
	
	@Override
	public long getCurrentNano(){
		return GlobalSimulationTimeI.i().getNano();
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this, value);
	}
	
//	@Override
//	public <H> HashChangeHolder<H> getHolder(Class<H> cl) {
//		return (HashChangeHolder<H>) hchHolder;
//	}
	
	@Override
	public HoldRestartable<IRestartable> getHolder() {
		if(this.hchHolder==null)throw new PrerequisitesNotMetException("holder should have been set!",this);
		return hchHolder;
	}
	
	@Override
	public void setHolder(HoldRestartable<IRestartable> hch) {
		PrerequisitesNotMetException.assertNotAlreadySet(this.hchHolder, hch, "holder", this);
		this.hchHolder=hch;
	}

	/**
	 * implement this only on concrete classes
	 * @return
	 */
	protected abstract THIS getThis();

	
//	CallableX callerRequestRetryUntilEnabled = new CallableX(this) {
//		@Override
//		public Boolean call() {
//			if(isEnabled())return true;
//			
//			requestEnable();
//			
//			return false;
//		}
//	};
//	public void requestRetryUntilEnabled() {
//		CallQueueI.i().addCall(callerRequestRetryUntilEnabled);
//	}
	
	/**
	 * an update will always happen,
	 * a refresh is about things that would not be updated during the
	 * normal update!
	 * 
	 * if a refresh can be performed, override this one
	 */
	@Override
	public void requestRefresh() {}
	
	RegisteredClasses<IManager> rscManager = new RegisteredClasses<IManager>();
//	private IManager	imgr;
//	@Override
//	public boolean isHasManagers() {
//		return getManager()!=null;
//	}
	@Override
	public ArrayList<IManager> getManagerList() {
		return rscManager.getTargetListCopy();
	}
	@Override
	public void addManager(IManager imgr) {
		rscManager.addClassesOf(imgr, true, true);
//		return this.imgr=imgr;
	}
	
	@Override
	public boolean isCanCleanExit() {
		return true; //just to not freeze the exiting
	}
	
}
