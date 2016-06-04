/* 
	Copyright (c) 2016, AquariusPower <https://github.com/AquariusPower>
	
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

import java.util.concurrent.Callable;

import com.github.commandsconsolegui.jmegui.ReattachSafelyState.ERecreateConsoleSteps;
import com.github.commandsconsolegui.jmegui.ReattachSafelyState.ReattachSafelyValidateSteps;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

/**
 * Life cycle steps: configure, pre-initialize, initialize, enable, disable, cleanup<br>
 * <br>
 * Every step can be validated and delayed until proper conditions are met.<br>
 * WARNING: a step not completed must self clean/undo before such method returns!<br>
 * <br>
 * This class modifies the way {@link #AppState} is managed.<br>
 * This basically means that, after each step request, you must verify if it succeeded
 * before making new decisions.<br>
 * <br>
 * Most important steps will actually happen during {@link #updateProperly(float)},
 * where they will be properly validated and retried at specified time interval (default 0).<br>
 * <br>
 * SELFNOTE: Keep this class depending solely in JME!<br>
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class ConditionalAppStateAbs implements AppState, ReattachSafelyValidateSteps {
	protected Node nodeGUI;
	
	// CONFIGURE 
	private boolean bConfigured;
	private Long lTimeReferenceMilis = null;
	
	/** true to delay initialization*/
	protected boolean bPreInitHold=false;
	private boolean	bPreInitialized;
	
	// TRY INIT 
	private long lInitRetryStartMilis;
	/** 0 means retry at every update*/
	protected long lInitRetryDelayMilis;
	
	// PROPERLY INIT
	private boolean	bProperlyInitialized;
	
	/** it can be initially disabled if you prefer.*/
	protected boolean	bEnabled = true;
	private boolean	bEnabledRequested;
	private boolean	bDisabledRequested;
	
	private long lCleanupRequestMilis;
	/** 0 means retry at every queue processing step*/
	protected long lCleanupRetryDelayMilis;
	
	/** set to true to allow instant configuration but wait before properly initializing*/
	protected boolean bHoldProperInitialization = false;
	
	/** you can skip configure() by setting this one*/
	private Application app;
	
	private boolean	bLastUpdateSuccessful;
	private boolean	bHoldUpdates;
	
	private boolean	bEnableSuccessful;
	private boolean	bHoldEnable;
	
	private boolean	bDisableSuccessful;
	private boolean	bHoldDisable;
	
	private boolean	bCleaningUp;
	private AppStateManager	asmCurrent;
	
//	public static class TimedDelay{
//		long lTimeStart;
//		long lDelay;
//		public boolean isReadyUpdate(){
//			
//		}
//	}
	
	/**
	 * Configure simple references assignments and variable values.
	 * Must be used before initialization.
	 * Put here only things that will not change on {@link #cleanupProperly()} !
	 * 
	 * @param app
	 * @return helps on implementing a retry to configure in case of non critical failures
	 */
	public boolean configureValidating(Application app){
		if(this.bConfigured)throw new NullPointerException("already configured");
		
		// internal configurations
		this.app=app;
		if(this.app==null)throw new NullPointerException("app is null");
		
		// configs above
		this.bConfigured=true;
		
		preInitRequest();
		
		return isConfigured();
	}
	
	public boolean isConfigured(){
		return bConfigured;
	}
	
	/**
	 * This can be used to override system time with your simulation time.
	 * 
	 * @param lMilis if null, will use system time.
	 */
	public void updateTimeMilis(Long lMilis){
		this.lTimeReferenceMilis=lMilis;
	}
	
	private long updateTime(){
		return lTimeReferenceMilis==null ? System.currentTimeMillis() : lTimeReferenceMilis;
	}
	
	public Application getApp(){
		return app();
	}
	public Application app(){
		return app;
	}
	
	
	/**
	 * This will self configure/add to state manager.
	 * This is to be called after a {@link #cleanupProperly()}.
	 * 
	 * @return
	 */
	protected boolean preInitRequest() {
		assertIsConfigured();
		
		if(bPreInitialized)return true;
		
		if(!bPreInitHold){
			if(!app.getStateManager().attach(this)){
				throw new NullPointerException("state already attached: "+this.getClass().getName()+"; "+this);
			}
			
			bPreInitialized=true;
			
			return bPreInitialized;
		}
		
		return false;
	}
	
	protected void assertIsConfigured() {
		if(!isConfigured())throw new NullPointerException("not configured yet!");
	}
	
	protected void assertIsPreInitialized() {
		if(!bPreInitialized)throw new NullPointerException("not pre-initialized yet!");
	}

	public boolean isPreInitHold() {
		return bPreInitHold;
	}

	public void setPreInitHold(boolean bPreInitHold) {
		this.bPreInitHold = bPreInitHold;
	}
	
	/**
	 * dummified<br>
	 * implement {@link #initializeValidating()} instead<br>
	 * the initialization will actually happen at {@link #updateProperly(float)}<br>
	 */
	@Deprecated
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		// KEEP EMPTY! dummified!
	};
	
	/**
	 * use {@link #isInitializedProperly()} instead
	 */
	@Deprecated
	@Override
	public boolean isInitialized() {
		return isInitializedProperly();
	}
	
	public boolean isPreInitialized(){
		return bPreInitialized;
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
	
	protected abstract boolean checkInitPrerequisites();
	protected abstract boolean initializeValidating();
	protected abstract boolean updateValidating(float tpf);
	protected abstract boolean enableValidating();
	protected abstract boolean disableValidating();
	protected abstract boolean cleanupValidating();
	
	/**
	 * 
	 */
	@Deprecated
	@Override
	public void update(float tpf) {
		/**
		 * this code is implemented here to keep compatibility to calls made to {@link AppState}
		 */
		updateProperly(tpf);
	}
	
	/**
	 * Better not override, just use {@link #updateValidating(float)} instead!<br>
	 * TODO make it final? would provide less flexibility to developers tho...
	 */
	public boolean updateProperly(float tpf) {
		assertIsConfigured();
		assertIsPreInitialized();
		
		if(!bProperlyInitialized){
			if(bHoldProperInitialization)return false;
			
			if(updateTime() < (lInitRetryStartMilis+lInitRetryDelayMilis)){
				return false;
			}
			
			if(!checkInitPrerequisites() && !initializeValidating()){
				lInitRetryStartMilis=updateTime();
				return false;
			}
			
			bProperlyInitialized=true;
		}
		
		if(bEnabledRequested && !bEnabled){
			if(bHoldEnable)return false;
			bEnableSuccessful=enableValidating();
			bEnabled=bEnableSuccessful;
		}else
		if(bDisabledRequested && bEnabled){
			if(bHoldDisable)return false;
			bDisableSuccessful=disableValidating();
			bEnabled=!bDisableSuccessful;
		}
		
		if(bEnabled){
			if(bHoldUpdates)return false;
			
			bLastUpdateSuccessful = updateValidating(tpf);
			if(!bLastUpdateSuccessful)return bLastUpdateSuccessful;
			/**
			 * SelfNote: More code can go only here.
			 */
		}
		
		return false;
	};
	
	public void toggleRequest(){
//		assertIsPreInitialized();
		setEnabledRequest(!isEnabled());
	}
	
	@Deprecated
	@Override
	public void setEnabled(boolean bEnabledRequested) {
		/**
		 * this code is implemented here to keep compatibility to calls made to {@link AppState}
		 */
		setEnabledRequest(bEnabledRequested);
	}
	
	/**
	 * use {@link #requestEnable()} or {@link #requestDisable()} instead
	 * 
	 * @param bEnabledRequested if false, will be a Disable Request.
	 */
	public void setEnabledRequest(boolean bEnabledRequested) {
		assertIsPreInitialized();
		
		if(bEnabledRequested){
			this.bEnabledRequested = true;
			this.bDisabledRequested = false;
		}else{
			this.bDisabledRequested = true;
			this.bEnabledRequested = false;
		}
//		lEnableDisableRequestTimeMilis=updateTime();
	};

	public void requestEnable(){
		setEnabled(true);
	}
	
	public void requestDisable(){
		setEnabled(false);
	}
	
	@Override
	public boolean isEnabled() {
		return bEnabled;
	}
	
	@Deprecated
	@Override 
	public void cleanup() {
		/**
		 * this code is implemented here to keep compatibility to calls made to {@link AppState}
		 */
		cleanupProperly();
	}
	
	public boolean isCleaningUp() {
		return bCleaningUp;
	}
	
	/**
	 * Better not override, just use {@link #cleanupValidating()} instead!<br>
	 * TODO make it final? would provide less flexibility to developers tho...
	 */
	public void cleanupProperly() {
		if(!bProperlyInitialized)return; //TODO log warn
		
		bCleaningUp = true;
		
		if(bEnabled){
			boolean bReAddToQueue=false;
			
			if(updateTime() < (lCleanupRequestMilis+lCleanupRetryDelayMilis)){
				bReAddToQueue=true;
			}else{
				bReAddToQueue=!disableValidating();
				if(bReAddToQueue){ //failed, retry
					lCleanupRequestMilis=updateTime();
				}
			}
			
			if(bReAddToQueue){
				app.enqueue(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						cleanupProperly(); // stackless "recursiveness"
						return null;
					}
				});
				return;
			}
		}
		
		cleanupValidating();
		
		bEnabled=false;
		bLastUpdateSuccessful=false;
		
		bEnabledRequested=false;
		bDisabledRequested=false;
		
		bPreInitialized=false;
		bProperlyInitialized=false;
		
		lInitRetryStartMilis=0;
		bHoldProperInitialization=false;
		
		bLastUpdateSuccessful=false;
		bHoldUpdates=false;
		
		bEnableSuccessful=false;
		bHoldEnable=false;
		
		bDisableSuccessful=false;
		bHoldDisable=false;
		
		/**
		 * SKIP THESE!
		 * {@link #bConfigured}
		 */
		
		bCleaningUp=false;
	}
	
	@Override public void stateAttached(AppStateManager stateManager) {
		if(asmCurrent!=null)throw new NullPointerException("already attached to "+asmCurrent+"; requested attach to "+stateManager);
		asmCurrent = stateManager;
	}
	
	@Override public void stateDetached(AppStateManager stateManager) {
		if(asmCurrent==null)throw new NullPointerException("not attached but still requested detach from "+stateManager+"?");
		if(!asmCurrent.equals(stateManager))throw new NullPointerException("attached to another "+asmCurrent+" and being detached from "+stateManager+"?");
		asmCurrent = null;
	}
	
	public boolean isAttachedToAStateManager(){
		return asmCurrent!=null;
	}
	
	public AppStateManager getStateManagingThis(){
		return asmCurrent;
	}
	
	// EASY SKIPPERS
	@Override public void render(RenderManager rm) {}
	@Override public void postRender(){}
	@Override public boolean reattachValidateStep(ERecreateConsoleSteps ercs) {return true;}


//	Long	lInitializationCompletedMilis;
////	long	lInitializationStartedMilis;
////	long	lInitializationMaxDelayMilis=1000;
////	long	lMessageCoolDownDelayMilis=3000;
////	long	lLastMessageMilis;
//	
//	TimedDelayVarField tdDebugMessageDelay = new TimedDelayVarField(this,3.0f);
//	TimedDelayVarField tdInitDelayLimit = new TimedDelayVarField(this,5.0f);
//	private boolean	bConfigured;
//	private boolean bInitializedAfterPrerequisites = false;
//
//	protected CommandsDelegatorI	cd;
//
//	protected SimpleApplication	sapp;
//	
//	public void configure(){
//		if(bConfigured)throw new NullPointerException("already configured.");
//
//		this.sapp = GlobalSappRefI.i().get();
//	//	if(!this.sapp.getStateManager().attach(this))throw new NullPointerException("state already attached...");
//		this.sapp.getStateManager().attach(this);
//		
//		this.cd = GlobalCommandsDelegatorI.i().get();
//		this.cd.addConsoleCommandListener(this);
//		
//		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
//		
//		bConfigured=true;
//	}
//	
//	public abstract boolean initPrerequisitesCheck();
//	
//	public void initOnEnable(){
//		if(!bConfigured)throw new NullPointerException("not configured yet");
//		tdInitDelayLimit.setActive(true);
//		CheckInitAndCleanupI.i().assertStateNotAlreadyInitializedAtInitializer(this);
//	}
//	
//	@Override
//	protected void onEnable() {
//		if(!bInitializedAfterPrerequisites){
//			if(!initPrerequisitesCheck())return;
//			bInitializedAfterPrerequisites=true;
//		}
//		
//		initOnEnable();
//		
//		// more code can go here...
//	}
//	
//	/**
//	 * after the cleanup, the state will be removed from the state manager!!!
//	 */
//	@Override
//	protected void cleanup(Application app) {
//		CheckInitAndCleanupI.i().assertStateIsInitializedAtCleanup(this);
//		bInitializedAfterPrerequisites=false;
//	}
//	
//	/**
//	 * put at the end of the initialization of the instantiated class
//	 */
//	@Override
//	public void initializationCompleted(){
//		this.lInitializationCompletedMilis=updateTime();
//	}
//	
//	@Override
//	public boolean isInitializationCompleted(){
//		if(lInitializationCompletedMilis==null){
//			if(tdInitDelayLimit.isReady()){
//				tdDebugMessageDelay.setActive(true);
//				if(tdDebugMessageDelay.isReady(true)){
//					System.err.println("Warn: ["+tdInitDelayLimit.getCurrentDelayNano()+"] initialization is taking too long for "+this);
////					long lTimeMilis = updateTime();
////					if(lTimeMilis > (lLastMessageMilis+lMessageCoolDownDelayMilis)){
////						if(lTimeMilis > (lInitializationStartedMilis+lInitializationMaxDelayMilis)){
////							System.err.println("Warn: ["+lTimeMilis+"] initialization is taking too long for "+this);
////							lLastMessageMilis = lTimeMilis;
////						}
////					}
//				}
//			}
//		}
//		
//		return lInitializationCompletedMilis!=null;
//	}
//
//	@Override
//	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//		return GlobalCommandsDelegatorI.i().get().getReflexFillCfg(rfcv);
//	}
	
}
