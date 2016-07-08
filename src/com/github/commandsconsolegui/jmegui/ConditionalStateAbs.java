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

//import com.github.commandsconsolegui.jmegui.ReattachSafelyState.ERecreateConsoleSteps;
import java.io.IOException;

import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
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
 * WARNING: a step not completed must self clean/undo before such method returns!<br>
 * <br>
 * After each step request, you must verify if it succeeded
 * before making new decisions.<br>
 * <br>
 * Most important steps will actually happen during {@link #doItAllProperly(float)},
 * where they will be properly validated and retried at specified time interval (default 0).<br>
 * <br>
 * SELFNOTE: Keep this class depending solely in JME!<br>
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class ConditionalStateAbs implements Savable{
	
	/**
	 * This Id is only required if there is more than one state of the same class.
	 */
	protected String strCaseInsensitiveId = null;
	
//	public static interface IConditionalStateAbsForConsoleUI{
//		public abstract void requestRestart();
//	}
	
//	public static interface ICfgParm{}
	
	private Node nodeGUI;
	
	// CONFIGURE 
	private boolean bConfigured;
	private Long lTimeReferenceMilis = null;
	
//	/** true to delay initialization*/
//	protected boolean bPreInitHold=false;
//	private boolean	bPreInitialized;
	
	// TRY INIT 
	protected class Retry{
		long lStartMilis=0;
		
		/** 0 means retry at every update*/
		long lDelayMilis=0;
		
		public void setRetryDelay(long lMilis){
			this.lDelayMilis=lMilis;
		}
		
		boolean canRetryNow(){
			return getUpdatedTime() > (lStartMilis+lDelayMilis);
		}

		public void resetStartTime() {
			lStartMilis=getUpdatedTime();
		}
		
	}
	protected Retry rInit = new Retry();
	protected Retry rEnable = new Retry();
	protected Retry rDisable = new Retry();
	protected Retry rDiscard = new Retry();
	
	// PROPERLY INIT
	private boolean	bProperlyInitialized;
	
	/** it must be initially disabled, the request will properly enable it*/
	protected boolean	bEnabled = false;
	
	private boolean	bEnabledRequested = true; //initially all will be wanted as enabled by default
	private boolean	bDisabledRequested;
	
	/** set to true to allow instant configuration but wait before properly initializing*/
	protected boolean bHoldProperInitialization = false;
	
	private Application app;
	
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
	 * Each subclass can have the same name "CfgParm".<br>
	 * <br>
	 * Just reference the CfgParm of the superclass directly ex.:<br> 
	 * 	new ConditionalAppStateAbs.CfgParm()<br>
	 * <br>
	 * This is also very important when restarting (configuring a new and fresh robust instance)<br> 
	 */
	public static interface ICfgParm{}
	
	/**
	 * see {@link ICfgParm}
	 */
	public static class CfgParm implements ICfgParm{
		protected Application app;
		protected String strId;
		public CfgParm(Application app, String strId) {
			super();
			this.app = app;
			this.strId = strId;
		}
	}
	private ICfgParm icfgOfInstance;

	private boolean	bRestartRequested;

	/**
	 * Configure simple references assignments and variable values.<br>
	 * Must be used before initialization.<br>
	 * Put here only things that will not change on {@link #cleanupProperly()} !<br>
	 * Each sub-class must implement its own {@link CfgParm} ({@link ICfgParm}) to keep coding-flow 
	 * clear.<br>
	 * 
	 * @param cp
	 */
	public ConditionalStateAbs configure(ICfgParm icfg){
		CfgParm cfg = (CfgParm)icfg;
		
//	protected void configure(Application app){
		if(this.bConfigured)throw new PrerequisitesNotMetException("already configured");
		
		// internal configurations
		if(cfg.app==null)throw new PrerequisitesNotMetException("app is null");
		this.app=cfg.app;
		
		// the Unique State Id
		if(cfg.strId==null){
//			throw new PrerequisitesNotMetException("id cant be null");
			cfg.strId = this.getClass().getSimpleName();
		}
		@SuppressWarnings("unchecked") //so obvious...
		ConditionalStateAbs csa = ConditionalStateManagerI.i().getConditionalState(
			(Class<ConditionalStateAbs>)this.getClass(), cfg.strId);
		if(csa!=null)throw new PrerequisitesNotMetException("conflicting state Id "+cfg.strId);
		this.strCaseInsensitiveId = cfg.strId;
		
		if(!ConditionalStateManagerI.i().attach(this)){
//		if(!app.getStateManager().attach(this)){
			throw new PrerequisitesNotMetException("state already attached: "
				+this.getClass().getName()+"; "+this);
		}
//		preInitRequest();
		
		// configs above
		this.bConfigured=true;
		msgDbg("cfg",this.bConfigured);
		
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
		this.icfgOfInstance=icfg;
		return (T)this;
	}
	
	protected ICfgParm getCfg(){
		return icfgOfInstance;
	}
	
	protected void msgDbg(String str, boolean bSuccess) {
		MsgI.i().msgDbg(str, bSuccess, this);
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
	
	private long getUpdatedTime(){
		return lTimeReferenceMilis==null ? System.currentTimeMillis() : lTimeReferenceMilis;
	}
	
	public Application getApp(){
		return app();
	}
	public Application app(){
		return app;
	}
	
	protected void assertIsConfigured() {
		if(!isConfigured())throw new PrerequisitesNotMetException("not configured yet!");
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
	
	protected boolean initCheckPrerequisites(){
		if(icfgOfInstance==null)throw new PrerequisitesNotMetException(
			"the instantiated class needs to set the configuration params to be used on a restart!");
		
		if(!icfgOfInstance.getClass().getName().startsWith(this.getClass().getName()+"$")){
//		if(!icfgOfInstance.getClass().equals(this.getClass())){
			throw new PrerequisitesNotMetException(
				"the stored cfg params must be of the instantiated class to be used on restarting it: "
				+this.getClass().getName());
		}
		
		return true;
	}
	protected boolean initOrUndo(){return true;}
	protected boolean updateOrUndo(float tpf){return true;}
	protected boolean enableOrUndo(){return true;}
	protected boolean disableOrUndo(){return true;}
	
	private boolean doItInitializeProperly(float tpf){
		if(bHoldProperInitialization)return false;
		
		if(!rInit.canRetryNow())return false;
		
		if(!initCheckPrerequisites() || !initOrUndo()){
			rInit.resetStartTime();
			msgDbg("init",false);
			return false;
		}
		
		bProperlyInitialized=true;
		msgDbg("init",bProperlyInitialized);
		
		return true;
	}
	
	/**
	 * This will:
	 * - Initialize properly
	 * - enable/disable properly
	 * - update properly
	 * 
	 * Use {@link #updateOrUndo(float)}<br>
	 */
	public boolean doItAllProperly(ConditionalStateManagerI.CompositeControl cc, float tpf) {
		cc.assertSelfNotNull();
		assertIsConfigured();
//		assertIsPreInitialized();
//		if(bRestartRequested)return false;
//		if(bDiscardRequested)return false;
		
		if(!bProperlyInitialized){
			if(bEnabledRequested){
				if(!doItInitializeProperly(tpf))return false;
			}
		}else{
			if(!doItUpdateOrEnableOrDisableProperly(tpf))return false;
		}
		
		return true;
	};
	
	private boolean doItUpdateOrEnableOrDisableProperly(float tpf) {
		if(bEnabledRequested && !bEnabled){
			if(bHoldEnable)return false;
			if(!rEnable.canRetryNow())return false;
			
			bEnableSuccessful=enableOrUndo();
			msgDbg("enabled",bEnableSuccessful);
			bEnabled=bEnableSuccessful;
			
			if(bEnableSuccessful){
				bEnabledRequested=false; //otherwise will keep trying
			}else{
				rEnable.resetStartTime();
			}
		}else
		if(bDisabledRequested && bEnabled){
			if(bHoldDisable)return false;
			if(!rDisable.canRetryNow())return false;
			
			bDisableSuccessful=disableOrUndo();
			msgDbg("disabled",bDisableSuccessful);
			bEnabled=!bDisableSuccessful;
			
			if(bDisableSuccessful){
				bDisabledRequested=false; //otherwise will keep trying
			}else{
				rDisable.resetStartTime();
			}
		}
		
		if(bEnabled){
			if(bHoldUpdates)return false;
			
			bLastUpdateSuccessful = updateOrUndo(tpf);
			if(!bLastUpdateSuccessful){
				msgDbg("update",false); //only on fail!!!
				return false;
			}
			/**
			 * SelfNote: More code can go only here.
			 */
		}
		
		return true;
	}

	public void toggleRequest(){
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
			
			if(!rDiscard.canRetryNow()){
//			if(getUpdatedTime() < (lCleanupRequestMilis+lCleanupRetryDelayMilis)){
				bRetry=true;
			}else{
				bRetry = !disableOrUndo();
				if(bRetry){ //failed, retry
					rDiscard.resetStartTime();
//					lCleanupRequestMilis=getUpdatedTime();
				}
			}
			
			if(bRetry){
				msgDbg(""+ELogAction.discard,false);
				return false;
			}
		}
		
		asmParent=null;
		
		msgDbg(""+ELogAction.discard,true);
		
		return true;
	}
	
	enum ELogAction{
		discard,
	}
	
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
		return nodeGUI;
	}

	protected void setNodeGUI(Node nodeGUI) {
		if(nodeGUI==null)throw new PrerequisitesNotMetException("null node gui");
		this.nodeGUI = nodeGUI;
	}
	
	public void setAppStateManagingThis(ConditionalStateManagerI.CompositeControl cc,ConditionalStateManagerI asmParent) {
		cc.assertSelfNotNull();
//		assertCompositeControlNotNull(cc);
		if(this.asmParent!=null)throw new PrerequisitesNotMetException(
			"already managed by "+this.asmParent+"; request made to attach at "+asmParent);
		this.asmParent=asmParent;
	}

	public void requestDiscard(){
		bDiscardRequested = true;
	}
	
	/**
	 * everything that is not configured can be copied thru this method
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
			NullPointerException npe = new NullPointerException("object copy failed");
			npe.initCause(e);
			throw npe;
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

	public String getId() {
		if(strCaseInsensitiveId==null)throw new PrerequisitesNotMetException("id cant be null");
		return strCaseInsensitiveId;
	}
	
	/**
	 * each state can have its delay value set individually also.
	 * @param lMilis
	 */
	public void setRetryDelay(long lMilis){
		rInit.setRetryDelay(lMilis);
		rEnable.setRetryDelay(lMilis);
		rDisable.setRetryDelay(lMilis);
		rDiscard.setRetryDelay(lMilis);
	}

  @Override
	public void write(JmeExporter ex) throws IOException{
  	
  };
  @Override
	public void read(JmeImporter im) throws IOException{
  	
  }

//	public boolean applyBoolTogglerChange(BoolTogglerCmdField btgSource) {
//		return false;
//	};
}
