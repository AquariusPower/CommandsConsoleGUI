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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.misc.HoldRestartable;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.DebugData;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RefHolder;
import com.github.commandsconsolegui.spAppOs.misc.Request;
import com.github.commandsconsolegui.spAppOs.misc.RunMode;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;
//import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringVarField;
import com.github.commandsconsolegui.spCmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.spJme.AudioUII.EAudio;
import com.github.commandsconsolegui.spJme.ManageConditionalStateI.CompositeControl;
import com.github.commandsconsolegui.spJme.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.github.commandsconsolegui.spJme.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.spJme.extras.UngrabMouseStateI.IUngrabMouse;
import com.github.commandsconsolegui.spJme.globals.GlobalManageDialogJmeI;
import com.github.commandsconsolegui.spJme.misc.ILinkedSpatial;
import com.github.commandsconsolegui.spJme.misc.ManageEffectsJmeStateI;
import com.github.commandsconsolegui.spJme.misc.ManageEffectsJmeStateI.IEffect;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.github.commandsconsolegui.spJme.savablevalues.CompositeSavableAbs;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * This class would be useful to a non Lemur dialog too.
 * 
 * A console command will be automatically created based on the configured {@link #strUIId}.<br>
 * See it at {@link DialogStateAbs}.
 * 
 * Workflow:
 * - configure the modal dialogs at the parent (the choice one is special and may vary from the default one) {@link #getDiagChoice(DialogListEntryData)}
 * - on activating a parent's entry, a modal will be opened
 * - the modal can access the selected parent's entry direcly for its value {@link #getParentReferencedDledListCopy()}
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <ACT> see {@link DialogListEntryData}
 * @param <THIS> is for getThis() concrete auto class type inherited trick
 */
//public abstract class BaseDialogStateAbs<DIAG,CS extends BaseDialogStateAbs.DialogCS<THIS>,THIS extends BaseDialogStateAbs<DIAG,CS,THIS>> extends CmdConditionalStateAbs implements IReflexFillCfg {
public abstract class DialogStateAbs<ACT,THIS extends DialogStateAbs<ACT,THIS>> extends CmdConditionalStateAbs<THIS> implements IReflexFillCfg,IUngrabMouse,ILinkedSpatial {
	private ISpatialLayoutValidator	sptvDialogMainContainer;
	private Spatial	sptIntputField;
	private Spatial sptMainList;
	private String	strTitle;
	private final StringVarField svfStyle = new StringVarField(this, (String)null, null).setDenyNullValue();
	
	private Vector3f	v3fBkpDiagPosB4Effect;
	private Vector3f	v3fBkpDiagSizeB4Effect;
	private String	strUserEnterCustomValueToken = "=";
	
	private boolean	bUserEnterCustomValueMode;
	private HoldRestartable<DialogStateAbs<ACT,?>> hrdiagParent = new HoldRestartable<DialogStateAbs<ACT,?>>(this);
	private HashMap<String, HoldRestartable<DialogStateAbs>> hmhrChildDiagModals = new HashMap<String, HoldRestartable<DialogStateAbs>>();
//	private ArrayList<DialogStateAbs> aModalChildList = new ArrayList<DialogStateAbs>();
	private boolean	bRequestedActionSubmit;
	private Node cntrNorth;
	private Node cntrSouth;
	private String	strLastFilter = "";
	private ArrayList<DialogListEntryData>	adleCompleteEntriesList = new ArrayList<DialogListEntryData>();
	private ArrayList<DialogListEntryData>	adleTmp;
	private DialogListEntryData	dleLastSelected;
	private Request reqRefreshList = new Request(this);
	private DiagModalInfo<ACT> dmi = null;
	
	private boolean bOptionChoiceSelectionMode = false;
//	private Long	lChoiceMadeAtMilis = null;
	private ArrayList<DialogListEntryData<ACT,?>> adataChosenEntriesList = new ArrayList<DialogListEntryData<ACT,?>>();
	
	private final BoolTogglerCmdField btgRestoreIniPosSizeOnce = new BoolTogglerCmdField(this, false);
	
	private final BoolTogglerCmdField btgEffectLocation = new BoolTogglerCmdField(this, true);
	protected final BoolTogglerCmdField btgEffect = new BoolTogglerCmdField(this, true);
	private final TimedDelayVarField tdDialogEffect = new TimedDelayVarField(this, 0.15f, "");
	private float	fMinEffectScale=0.01f;
	
	protected abstract <N extends Node> void lineWrapDisableForChildrenOf(N node);
	
//	public DialogStateAbs() {
////		DelegateManagerI.i().addHandled(this);
//	}
	
	private Comparator<DialogListEntryData> cmpTextAtoZ = new Comparator<DialogListEntryData>() {
		@Override
		public int compare(DialogListEntryData o1, DialogListEntryData o2) {
			return o1.getVisibleText().compareTo(o2.getVisibleText());
		}
	};
	private Comparator<DialogListEntryData> cmpTextZtoA = new Comparator<DialogListEntryData>() {
		@Override
		public int compare(DialogListEntryData o1, DialogListEntryData o2) {
			return o2.getVisibleText().compareTo(o1.getVisibleText());
		}
	};
	private Comparator<DialogListEntryData> getComparatorText(boolean bAtoZ){
		return bAtoZ ? cmpTextAtoZ : cmpTextZtoA;
	}
	
	public boolean isHasEnabledChildDialog(){
		if(getChildDiagModalInfoCurrent()==null)return false;
		THIS diag = getChildDiagModalInfoCurrent().getDiagModal();
		return diag.isEnabled() || diag.isEnabling();
	}
	
	protected DiagModalInfo<ACT> getChildDiagModalInfoCurrent(){
		return dmi;
	}
	
	protected THIS setChildDiagModalInfoCurrent(DiagModalInfo<ACT> dmi){
		setBlockerEnabled(dmi!=null);
		this.dmi=dmi;
		return getThis();
	}
	
	protected Spatial getDialogMainContainer(){
		return (Spatial)sptvDialogMainContainer;
	}
	
	public boolean isLayoutValid(){
		if(sptvDialogMainContainer==null){
			MsgI.i().devWarn("main container not set", this, this.isConfigured(), this.isInitializedProperly());
			return false;
		}
		
		return ((ISpatialLayoutValidator)sptvDialogMainContainer).isLayoutValid();
	}
	
	protected THIS setDialogMainContainer(ISpatialLayoutValidator spt){
		PrerequisitesNotMetException.assertNotAlreadySet(this.sptvDialogMainContainer, spt, "diag main container", this);
		this.sptvDialogMainContainer=spt;
		
//		for(Class<?> cl:MiscI.i().getSuperClassesOf(this)){
//			MiscJmeI.i().retrieveUserData(Spatial.class, this.sptContainerMain, cl.getName(), this, null);
//			this.sptContainerMain.setUserData(cl.getName(), new SavableHolder(this));
			MiscJmeI.i().setUserDataPSH(getDialogMainContainer(), this);
//		}
		
		return getThis();
	}
	
//	public static class CfgParm extends BaseDialogStateAbs.CfgParm{
//		public CfgParm(boolean bOptionSelectionMode, String strUIId, boolean bIgnorePrefixAndSuffix,Node nodeGUI,BaseDialogStateAbs modalParent) {
//			super(strUIId,bIgnorePrefixAndSuffix,	nodeGUI,
//				 /** 
//				 * Dialogs must be initially disabled because they are enabled 
//				 * on user demand. 
//				 */
//				false,
//				modalParent);
//			this.bOptionSelectionMode=bOptionSelectionMode;
//		}
//	}
	
//	public static class CfgParmOld<T> implements ICfgParm{
//		private boolean	bOptionSelectionMode;
//		private String strUIId;
//		private boolean bIgnorePrefixAndSuffix;
//		private Node nodeGUI;
////		private boolean bInitiallyEnabled;
//		private R diagParent;
////		private Long lMouseCursorClickDelayMilis;
//		public CfgParm(boolean bOptionSelectionMode, String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI, R diagParent){//, Long lMouseCursorClickDelayMilis) {
//			super();
//			this.strUIId = strUIId;
//			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
//			this.nodeGUI = nodeGUI;
//			this.bOptionSelectionMode=bOptionSelectionMode;
////			this.bInitiallyEnabled=bInitiallyEnabled;
//			this.diagParent=diagParent;
////			this.lMouseCursorClickDelayMilis=lMouseCursorClickDelayMilis;
//		}
//		public void setUIId(String strUIId){
//			if(this.strUIId!=null)throw new PrerequisitesNotMetException("UI Id already set",this.strUIId,strUIId);
//			this.strUIId=strUIId;
//		}
//	}
	
	public static class CfgParm extends CmdConditionalStateAbs.CfgParm{
		private boolean	bRestartEnableWithoutLoadingOnce = false;
		private boolean	bRestartCopyToSelfAllEntries = true;
//		private boolean	bRestartInstancedFrom = false;
		
		private Vector3f	v3fIniPos;
		private Vector3f	v3fIniSize;
		private boolean	bOptionSelectionMode;
//		private Node nodeGUI;
		private boolean bInitiallyEnabled = false; //the console needs this "true"
//		public CfgParm(String strUIId, Node nodeGUI){//, R diagParent) {
		
		public CfgParm(String strUIId){//, R diagParent) {
			super(strUIId);
//			this.nodeGUI = nodeGUI;
		}
		
		public void setIniSize(Vector3f v3fIniSize) {
			this.v3fIniSize = v3fIniSize;
		}
		public Vector3f getIniSize() {
			return v3fIniSize;
		}
		public void setIniPos(Vector3f v3fIniPos) {
			this.v3fIniPos = v3fIniPos;
		}
		public Vector3f getIniPos() {
			return v3fIniPos;
		}
		
		public void setUIId(String strUIId){
//			if(getId()!=null)throw new PrerequisitesNotMetException("UI Id already set",getId(),strUIId);
			PrerequisitesNotMetException.assertNotAlreadySet(getId(), strUIId, "UI id", this);
			setId(strUIId);
		}
		public boolean isInitiallyEnabled() {
			return bInitiallyEnabled;
		}
		public void setInitiallyEnabled(boolean bInitiallyEnabled) {
			this.bInitiallyEnabled = bInitiallyEnabled;
		}

		public boolean isRestartCopyToSelfAllEntries() {
			return bRestartCopyToSelfAllEntries;
		}

		public void setRestartCopyToSelfAllEntries(boolean bRestartCopyToSelfAllEntries) {
			this.bRestartCopyToSelfAllEntries = bRestartCopyToSelfAllEntries;
		}

		public boolean isRestartEnableWithoutLoadingOnceAndReset() {
			if(bRestartEnableWithoutLoadingOnce){
				bRestartEnableWithoutLoadingOnce=false;
				return true;
			}
			return false;
		}
		
		/**
		 * this will basically grant a first save based on the default values
		 * @return
		 */
		public void setRestartEnableWithoutLoadingOnce(boolean bRestartWithoutLoadingOnce) {
			this.bRestartEnableWithoutLoadingOnce = bRestartWithoutLoadingOnce;
		}

//		/**
//		 * TODO rename to isRestartInstance
//		 * @return
//		 */
//		public boolean isRestartInstancedFrom() {
//			return bRestartInstancedFrom;
//		}
//
//		public void setRestartInstancedFrom(boolean bRestartInstancedFrom) {
//			this.bRestartInstancedFrom = bRestartInstancedFrom;
//		}
	}
	private CfgParm	cfg;
	private boolean	bSaveDialog = true;
	private Vector3f	v3fApplicationWindowSize;
	@Override
	public THIS configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,true);
//		if(isSaveDialog()){
////			dsv = new DialogSavable().setOwner(this);
//		}
		
//		if(!isRestartCfgSet())setRestartCfg(new RestartCfg());
		
		btgRestoreIniPosSizeOnce.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				if(btgRestoreIniPosSizeOnce.b()){
					restoreDefaultPositionSize();
					btgRestoreIniPosSizeOnce.toggle(); //to false
				}
				return true;
			}
		});
		
//	private void configure(String strUIId,boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
		
//		this.bOptionChoiceSelectionMode=cfg.bOptionSelectionMode;
		
//		bEnabled=cfg.bInitiallyEnabled;
//		if(!cfg.bInitiallyEnabled)requestDisable();
//		if(!cfg.bInitiallyEnabled){
		
		 /** 
		 * Dialogs must be initially disabled because they are enabled 
		 * on user demand. 
		 */
		if(!cfg.isInitiallyEnabled() && !cfg.isRestartNewInstance()){
			initiallyDisabled();
			btgEnabled.setObjectRawValue(false);//,false);
		}
		
//		MouseCursor.i().configure(cfg.lMouseCursorClickDelayMilis);
		
//		if(cfg.nodeGUI==null)cfg.nodeGUI=GlobalGUINodeI.i();
//		setNodeGUI(cfg.nodeGUI);//getNodeGUI()
		
//		this.diagParent=cfg.diagParent;
//		updateModalParent();
		
//		setCmdPrefix("toggleUI");
//		setCmdSuffix("");
		
//		ConditionalStateManagerI.i().
		if(cfg.getId()==null || cfg.getId().isEmpty()){
//			throw new PrerequisitesNotMetException("invalid UI identifier");
			String str=MiscI.i().getClassName(this,true);
			cfg.setId(str);
			MsgI.i().devInfo("using automatic UI id "+str+" for", this);
		}
//		this.strCaseInsensitiveId=cfg.strUIId;
		
//		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+cfg.getId();
//		btgShowDialog.setCustomCmdId(this.strCmd);
		
		super.configure(cfg);//new CmdConditionalStateAbs.CfgParm(cfg.strUIId, cfg.bIgnorePrefixAndSuffix));
		
//		storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	protected void setSaveDialog(boolean b){
		this.bSaveDialog=b;
	}
	
	private boolean isSaveDialog() {
		return bSaveDialog;
	}

	protected Spatial getInputField() {
		return sptIntputField;
	}
//	public abstract String getInputText();
	public String getInputText(){
		return bdh().getTextFromField(getInputField());
	}
	private ManageDialogAbs bdh(){
		return GlobalManageDialogJmeI.i();
	}
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and instantiate everything to make it actually be able to work.
	 */
	@Override
	protected boolean initAttempt() {
		if(!isDiagStyleSet()){
			setStyle(GlobalManageDialogJmeI.i().STYLE_CONSOLE);
		}
		if(!super.initAttempt())return false;
		
		if(!initGUI())return false;
		if(!initKeyMappings())return false;
		
		CallableX cxRefresh = new CallableX(this) {
			@Override
			public Boolean call() {
				// if sort is true or false, will update on change.
				requestRefreshUpdateList();
				return true;
			}
		};
		btgSortListEntries.setCallerAssigned(cxRefresh);
		btgSortListEntriesAtoZ.setCallerAssigned(cxRefresh);
		
		tdUpdateRefreshList.updateTime();
		
		if(!isCompositeSavableSet())setCompositeSavable(new SaveDiag(this));
		
		return true;
	}
	
	@Override
	protected void initSuccess() {
		super.initSuccess();
		
//		DelegateManagerI.i().addHandled(this);
//		GlobalManageDialogJmeI.i().addHandled(this);
	}
	
	protected boolean initGUI(){
		return true;
	}
	
	protected abstract boolean initKeyMappings();
	
	protected THIS setInputTextAsUserTypedValue(String str){
		if(!isInputToUserEnterCustomValueMode())throw new PrerequisitesNotMetException("not user typing value mode", this);
		
		setInputText(getUserEnterCustomValueToken()+str);
		
		return getThis();
	}
	
	/**
	 * 
	 * @return null means there is no user typed value (whatever is there is as a list filter not as a value)
	 */
	public String getInputTextAsUserTypedValue(){
		if(!isInputToUserEnterCustomValueMode())throw new PrerequisitesNotMetException("not user typing value mode", this);
		
		String str = getInputText().trim();
		if(str.startsWith(getUserEnterCustomValueToken())){
			str = str.substring(getUserEnterCustomValueToken().length()).trim();
			
			/**
			 * if string must be surrounded by quotes, so empty one would be ""
			 * if empty, there was no value typed, so ignore it.
			 */
			if(!str.isEmpty())return str;
		}
		
		return null; //not a typed value, was a filter
	}
	
	protected void requestActionSubmit() {
		bRequestedActionSubmit=true;
	}
	
//	private Retry rUpdateList = new Retry();
	
//	private final TimedDelayVarField tdUpdateRefreshList = new TimedDelayVarField(this, /*0.3f*/0.0f, "PROBLEM: this can cause crash as the list will remain unchanged while the dialog is resized, and the specific list resize related to the dialog new size will not be checked for failure before being accepted...");
	private final TimedDelayVarField tdUpdateRefreshList = new TimedDelayVarField(this, 0.3f, "");
	
	/**
	 * this will react to changes on the list
	 */
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		DialogListEntryData<ACT,?> dle = getSelectedEntryData();
		if(dle!=dleLastSelected)AudioUII.i().play(AudioUII.EAudio.SelectEntry);
//		if(dle!=null)
		dleLastSelected = dle;
		
//		if(isCfgDataValueSet()) {
//			updateAllParts();
//		}
		
//		if(bRequestedRefreshList){
		if(reqRefreshList.isReady()){
			if(tdUpdateRefreshList.isReady(true)){
//			if(rUpdateList.isReadyToRetry()){
				updateList();
				lineWrapDisableForChildrenOf((Node)sptMainList);
//				bRequestedRefreshList=false;
				reqRefreshList.reset();
//			}
			}
		}
		
		if(bRequestedActionSubmit){
			actionSubmit();
			bRequestedActionSubmit=false;
		}
		
		if(tdDialogEffect.isActive()){ //dont use btgGrowEffect.b() as it may be disabled during the grow effect
			updateEffect(!isTryingToDisable());
		}
		
		if(hchInputText.isChangedAndUpdateHash(getInputText())){
			AudioUII.i().play(EAudio.TypingTextLetters);
		}
		
		return true;
	}
	RefHolder<String> hchInputText=new RefHolder<String>("");
	private DebugData	dbg;
	
	/**
	 * TODO try this instead: https://github.com/jMonkeyEngine-Contributions/Lemur/wiki/Effects-and-Animation#open-close-effect
	 * @param bGrow
	 * @return
	 */
	protected boolean updateEffect(boolean bGrow){
		boolean bCompleted=false;
		
		// SCALE
		Vector3f v3fScaleCopy = getDialogMainContainer().getLocalScale().clone();
		float fValAdd = tdDialogEffect.getCurrentDelayCalc(1f-fMinEffectScale,false);
		float fVal=0f;
		if(bGrow){
			fVal = fMinEffectScale+fValAdd;
			if(Float.compare(fVal,1f)>=0){
				fVal=1f;
				bCompleted=true;
			}
		}else{ //shrink
			fVal = 1f - fValAdd;
			if(Float.compare(fVal,fMinEffectScale)<=0){
				fVal=fMinEffectScale;
				bCompleted=true;
			}
		}
		v3fScaleCopy.x = v3fScaleCopy.y = fVal;
		
		getDialogMainContainer().setLocalScale(v3fScaleCopy);
		
		prepareForEffect();
		
		// LOCATION
		if(btgEffectLocation.b()){
			Vector3f v3f = new Vector3f(v3fBkpDiagPosB4Effect);
			float fPerc = getEffectPerc();
			float fHalfWidth = (v3fBkpDiagSizeB4Effect.x/2f);
			float fHalfHeight = (v3fBkpDiagSizeB4Effect.y/2f);
			if(!bGrow)fPerc = 1.0f - fPerc;
			v3f.x = v3f.x + fHalfWidth - (fHalfWidth*fPerc);
			v3f.y = v3f.y - fHalfHeight + (fHalfHeight*fPerc);
			MiscJmeI.i().setLocationXY(getDialogMainContainer(),v3f);
		}
		
		if(bCompleted){
			tdDialogEffect.setActive(false);
			
			MiscJmeI.i().setLocationXY(getDialogMainContainer(),v3fBkpDiagPosB4Effect);
			v3fBkpDiagPosB4Effect=null;
		}
		
		return bCompleted;
	}
	
	private void prepareForEffect() {
		if(v3fBkpDiagPosB4Effect==null){
			v3fBkpDiagPosB4Effect = getDialogMainContainer().getLocalTranslation().clone();
		}
		
		if(v3fBkpDiagSizeB4Effect==null){
			v3fBkpDiagSizeB4Effect = getMainSizeCopy();
		}
	}

	protected float getEffectPerc(){
		return tdDialogEffect.getCurrentDelayPercentual(false);
	}
	
	protected int getEffectMaxTimeMilis(){
		return (int)tdDialogEffect.getDelayLimitMilis();
	}
	
	boolean bParentDiagIsRequired=false;
	private IEffect effLinkToParent;
	/**
	 * usually because a parent list entry will be required to perform things here and give it back.
	 * @param b
	 */
	protected void setParentDiagIsRequired(boolean  b){
		this.bParentDiagIsRequired=b;
	}
	public boolean isParentDiagRequired(){
		return this.bParentDiagIsRequired;
	}
	
	@Override
	protected boolean enableAttempt() {
		if(!super.enableAttempt())return false;
		if(!isLayoutValid()){
			MsgI.i().devWarn("gui elements layout is invalid", this);
			return false;
		}
		if(isParentDiagRequired() && (getParentDialog()==null || getParentDialog().getDialogMainContainer()==null)){
			MsgI.i().warn("a parent dialog is required to enable this one", this);
			cancelEnableRequest();
			return false;
		}
		
		getNodeGUI().attachChild(getDialogMainContainer());
		
//		if(hrdiagParent.isSet()){
//			hrdiagParent.getRef().addModalDialog(this);
//		}
		
		if(btgEffect.b() && ( !cfg.isRestartNewInstance() || isFirstEnableDone() ) ){
			Vector3f v3fScale = getDialogMainContainer().getLocalScale();
			v3fScale.x=v3fScale.y=fMinEffectScale;
			tdDialogEffect.setActive(true);
			prepareForEffect();
		}
		
		if(getInputText().isEmpty()){
			if(bUserEnterCustomValueMode)setInputText(getUserEnterCustomValueToken());
		}
		
		ManageCallQueueI.i().addCall(new CallableX(this,100) {
			@Override
			public Boolean call() {
				if(getParentDialog()!=null){
					return createDiagLinkToParentEffect() != null;
				}
				
				return true;
			}
		});
		
		return true;
	}
	
	protected IEffect createDiagLinkToParentEffect() {
		float fZdispl=1f; //1f is like 100 stacked gui elements because each will heighten by 0.01f by lemur at least
		if(effLinkToParent==null){
			effLinkToParent = GlobalManageDialogJmeI.i().getLinkToParentEffectClone();
//			effLinkToParent = new EffectElectricity(this)
			effLinkToParent.setOwner(this);
		}
		
		effLinkToParent
			.setFollowFromTarget(getParentDialog().getDialogMainContainer(), new Vector3f(0,0,fZdispl))
			.setFollowToTarget(this.getDialogMainContainer(), null)
			.setPlay(true)
			;
			
	//		ManageEffectsJmeStateI.i().addEffect(eff);
//		}
		
		return effLinkToParent;
	}

	@Override
	protected void enableSuccess() {
		super.enableSuccess();
		updateAllPartsNow();
		loadOnEnable();
	}
	
	protected void loadOnEnable(){
		if(isSaveDialog()){
			GlobalCommandsDelegatorI.i().addCmdToQueue(scfLoad);
		}
	}
	
	public boolean isDialogEffectsDone(){
		return !tdDialogEffect.isActive();
	}
	
//	public abstract Vector3f getMainSize();
	public Vector3f getMainSizeCopy(){
		return bdh().getSizeCopyFrom(getDialogMainContainer());
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!super.disableAttempt())return false;
		if(isHasEnabledChildDialog()){
			return false;
		}
		
		if(btgEffect.b() && !isRestartRequested()){
			Vector3f v3fScaleCopy = getDialogMainContainer().getLocalScale().clone();
			
			if(Float.compare(v3fScaleCopy.x,1f)==0){
				tdDialogEffect.setActive(true);
				v3fBkpDiagPosB4Effect = getDialogMainContainer().getLocalTranslation().clone();
				return false;
			}else{
				if(Float.compare(v3fScaleCopy.x,fMinEffectScale)>0){
					return false;
				}
			}
		}
		
		getDialogMainContainer().removeFromParent();
		
		if(btgEffect.b()){ //ensure scale is reset to normal
			getDialogMainContainer().setLocalScale(1f);
		}
		
//		if(hrdiagParent.isSet()){
//			hrdiagParent.getRef().removeModalDialog(this);
//		}
		
		if(effLinkToParent!=null)effLinkToParent.setPlay(false);
//		ManageEffectsJmeStateI.i().discardEffectsForOwner(this);
		
		return true;
	}
	
	@Override
	protected void disableSuccess() {
		super.disableSuccess();
		if(isSaveDialog())GlobalCommandsDelegatorI.i().addCmdToQueue(scfSave);
	}
	
//	/**
//	 * This is important to prevent other parts of the application from 
//	 * vanishing with the mouse cursor (by grabbing it).
//	 * @param b
//	 */
//	protected THIS setMouseCursorKeepUngrabbed(boolean b) {
//		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,b);
//		return getThis();
//	}
	
	protected THIS setTitle(String str){
		this.strTitle = str;
		return getThis();
	}
	
//	@Override
//	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//		return cd().getReflexFillCfg(rfcv);
//	}
	
//	/**
//	 * @param ccSelf 
//	 * @return
//	 */
//	public Spatial getInputFieldForManagement(LemurFocusHelperStateI.CompositeControl ccSelf) {
//		ccSelf.assertSelfNotNull();
//		return sptIntputField;
//	}
	
//	public float getInputFieldHeight(){
//		return MiscJmeI.i().retrieveBitmapTextFor((Node)sptIntputField).getLineHeight();
//	}
	
	protected THIS setInputField(Spatial sptIntputField) {
		this.sptIntputField = sptIntputField;
		return getThis();
	}
	
	public boolean isInputToUserEnterCustomValueMode(){
		return bUserEnterCustomValueMode;
	}
	public THIS setInputToUserEnterCustomValueMode(boolean bEnable){
		this.bUserEnterCustomValueMode=bEnable;
		return getThis();
	}
	
//	protected abstract R setInputText(String str);
	protected THIS setInputText(String str){
		bdh().setTextAt(getInputField(),str);
		return getThis();
	}
	
	protected DialogStateAbs<ACT,?> getParentDialog(){
//		return this.diagParent;
		return hrdiagParent.getRef();
	}
	
//	protected void applyDiscardingParent() {
//		if(this.diagParent.isPreparingToBeDiscarded()){
//			this.diagParent=null;
//		}
//	}
	
	public THIS removeModalDialog(DialogStateAbs diagModal){
		diagModal.setDiagParent(null);
		if(hmhrChildDiagModals.remove(diagModal.getUniqueId())==null){
			throw new PrerequisitesNotMetException("was not added?",diagModal,this);
		}
		return getThis();
	}
	
	public THIS addModalDialog(DialogStateAbs diagModal){
		PrerequisitesNotMetException.assertNotAlreadySet(diagModal.getParentDialog(), this, "parent", diagModal);
		diagModal.setDiagParent(this);
		hmhrChildDiagModals.put(diagModal.getUniqueId(), new HoldRestartable(this,diagModal));
		return getThis();
	}
	
	protected THIS setDiagParent(DialogStateAbs diagParent) {
		if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg); 
//		if(this.diagParent!=null)throw new PrerequisitesNotMetException("modal parent already set",this.diagParent,diagParent);
//		PrerequisitesNotMetException.assertNotAlreadySet("modal parent", this.diagParent, diagParent, this);
		hrdiagParent.setRef(diagParent);
//		this.diagParent = diagParent;
		return getThis();
	}
	
//	public abstract void setAnswerFromModalChild(Object... aobj);
	
	public ArrayList<DialogStateAbs> getModalChildListCopy() {
		ArrayList<DialogStateAbs> a = new ArrayList<DialogStateAbs>();
		for(HoldRestartable<DialogStateAbs> hr:hmhrChildDiagModals.values()){
			a.add(hr.getRef());
		}
		return a;
	}
	
//	/**
//	 * 
//	 * @param bAdd false to remove
//	 * @param diag
//	 */
//	private void manageModalChild(boolean bAdd, DialogStateAbs diag) {
//		if(bAdd){
//			if(!aModalChildList.contains(diag)){
//				aModalChildList.add(diag);
//			}
//		}else{
//			aModalChildList.remove(diag);
//		}
//	}

	public abstract void clearSelection();
	
	public boolean isOptionSelectionMode(){
		return isOptionChoiceSelectionMode();
	}
	
	protected abstract void updateList();
	protected abstract void updateTextInfo();
	protected abstract DialogListEntryData<ACT,?> getSelectedEntryData();
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	protected void updateInputField(){
		if(isInputToUserEnterCustomValueMode()){
			if(getInputText().isEmpty() || getInputText().equals(getUserEnterCustomValueToken())){
				applyDefaultValueToUserModify();
			}
		}else{
			setInputText(getLastFilter());
//			getInputField().setText(getLastFilter());
		}
	}
	
	protected String getDefaultValueToUserModify() {
		return "";
	}
	
	protected void applyDefaultValueToUserModify() {
		setInputText(getUserEnterCustomValueToken()+getDefaultValueToUserModify());
	}
	
	public void requestRefreshUpdateList(){
		reqRefreshList.requestNow();
//		CallQueueI.i().addCall(new CallableX(this) {
//			@Override
//			public Boolean call() {
//				bRequestedRefreshList=true;
//				return true;
//			}
//		});
	}
	
	public void openModalDialog(String strDialogId, DialogListEntryData dledToAssignModalTo, Object cmd){
		DialogStateAbs diagModalCurrent = hmhrChildDiagModals.get(strDialogId).getRef();
		if(diagModalCurrent!=null){
			setChildDiagModalInfoCurrent(new DiagModalInfo(diagModalCurrent,cmd,dledToAssignModalTo));
			diagModalCurrent.requestEnable();
		}else{
			throw new PrerequisitesNotMetException("no dialog set for id: "+strDialogId);
		}
	}
	
	protected abstract void setBlockerEnabled(boolean b);

	@Override
	public void requestRestart() {
		// do not allow restart with childs enabled, this also grants many consistencies
		for(HoldRestartable<DialogStateAbs> hr:hmhrChildDiagModals.values()){
			if(hr.getRef().isEnabled()){
				AudioUII.i().play(EAudio.Failure);
				return;
			}
		}
		
		super.requestRestart();
	}

	protected String getTextInfo(){
		String str="";
		
		str+="Help("+DialogStateAbs.class.getSimpleName()+"):\n";
		str+="\tType a list filter at input text area and hit Enter.\n";
		
		if(isOptionChoiceSelectionMode()){
			str+="\tOption Mode: when hitting Enter, if an entry is selected, it's value will be chosen.\n";
			str+="\tBut if the input begins with '"+getUserEnterCustomValueToken()+"', when hitting Enter, that specific value will be returned to the parent.\n";
			
			for(DialogListEntryData<ACT,?> dled:getParentReferencedDledListCopy()){
				str+="\tRefAtParent: "+dled.getVisibleText()+"\n";
			}
			
		}
		
		return str;
	}
	
	/**
	 * NO! use {@link #getParentReferencedDledListCopy()} instead
	 * Keep this as a warning!
	 * @return
	 */
	@Deprecated
	protected DialogListEntryData<ACT,?> getDledReferenceAtParent(){
		throw new PrerequisitesNotMetException("NO! see this method documentation...");
	}
	/**
	 * Will not be set anymore!
	 * use {@link #getParentReferencedDledListCopy()} instead!
	 * Keep this as a warning!
	 */
	@Deprecated
	protected void setDledReferenceAtParent(){
		throw new PrerequisitesNotMetException("NO! see this method documentation...");
	}
	
	protected ArrayList<DialogListEntryData> getParentReferencedDledListCopy() {
		PrerequisitesNotMetException.assertNotNull(getParentDialog(), "parent diag", this);
			
		ArrayList<DialogListEntryData> adled = new ArrayList<DialogListEntryData>();
//		if(getParentDialog()!=null){
			DialogStateAbs<ACT,?>.DiagModalInfo<ACT> dmiSonsOfPapa = getParentDialog().getChildDiagModalInfoCurrent();
			if(dmiSonsOfPapa!=null){
				if(dmiSonsOfPapa.getDiagModal()!=this){
					throw new PrerequisitesNotMetException("current parent's modal dialog should be 'this'",dmiSonsOfPapa,dmiSonsOfPapa.getDiagModal(),this);
				}
				
				for(DialogListEntryData<ACT,?> dled:dmiSonsOfPapa.getParentReferencedDledListCopy()){
					adled.add(dled);
				}
			}
//		}
		return adled;
	}
	
	/**
	 * override empty to disable filter
	 */
	protected void applyListKeyFilter(){
		String str = getInputText();
		
		if(bUserEnterCustomValueMode){
			if(
					getUserEnterCustomValueToken()!=null && 
					!getUserEnterCustomValueToken().isEmpty() && 
					str.startsWith(getUserEnterCustomValueToken())
			){
				return; //is in type value mode, so skip updating filter
			}
		}
		
		this.strLastFilter=str.toLowerCase();
	}
	
	/**
	 * what happens when pressing ENTER keys
	 */
	protected void actionSubmit(){
		applyListKeyFilter();
		updateAllPartsNow();
		
		DialogListEntryData<ACT,?> dataSelected = getSelectedEntryData(); //this value is in this console variable now
		
		if(isOptionChoiceSelectionMode()){
			adataChosenEntriesList.clear();
			if(dataSelected!=null){
				adataChosenEntriesList.add(dataSelected); //TODO could be many, use a checkbox for multi-selection
				
				cd().dumpInfoEntry(this.getUniqueId()+": Option Selected: "+dataSelected.toString());
				requestDisable(); //close if there is one entry selected
			}
		}else{
			if(dataSelected!=null){
				if(dataSelected.isParent()){
					dataSelected.toggleExpanded();
				}else{
					actionMainAtEntry(dataSelected,null);
				}
			}
		}
		
	}
	
	private void updateAllPartsNow(){
		updateTextInfo();
		
		updateList(); //requestRefreshUpdateList(); //
		
		updateInputField();
	}
	
	public ArrayList<DialogListEntryData<ACT,?>> getDataSelectionListCopy() {
		return new ArrayList<DialogListEntryData<ACT,?>>(adataChosenEntriesList);
	}
	
	protected class DiagModalInfo<ACT>{
		private THIS	diagChildModal;
		
		private ACT	actionAtParent;
		private ArrayList<DialogListEntryData>	adledToPerformResultOfActionAtParentList;
		
		public DiagModalInfo(
			THIS diagModalCurrent,
			ACT cmdAtParent,
			DialogListEntryData<ACT,?>... adledReferenceAtParent 
		) {
			super();
			this.diagChildModal = diagModalCurrent;
			this.adledToPerformResultOfActionAtParentList = new ArrayList<DialogListEntryData>(
				Arrays.asList(adledReferenceAtParent));
			this.actionAtParent = cmdAtParent;
		}
		
		@SuppressWarnings("unchecked")
		public THIS getDiagModal() {
			return diagChildModal;
		}
//		public void setDiagModal(R diagModal) {
//			this.diagChildModal = diagModal;
//		}
		public ArrayList<DialogListEntryData> getParentReferencedDledListCopy() {
			return new ArrayList<DialogListEntryData>(adledToPerformResultOfActionAtParentList);
		}
//		public void setDataReferenceAtParent(DialogListEntryData<T> dataReferenceAtParent) {
//			this.dataSelectedAtParent = dataReferenceAtParent;
//		}
		public ACT getCmdAtParent() {
			return actionAtParent;
		}
//		public void setCmdAtParent(T cmdAtParent) {
//			this.cmdOriginAtParent = cmdAtParent;
//		}
		
	}
	
	public boolean isChoiceMade() {
//		return lChoiceMadeAtMilis!=null;
		return adataChosenEntriesList.size()>0;
	}

	protected THIS resetChoice() {
//		lChoiceMadeAtMilis=null;
		adataChosenEntriesList.clear();
		return getThis();
	}

	public boolean isOptionChoiceSelectionMode() {
		return bOptionChoiceSelectionMode;
	}

	protected THIS setOptionChoiceSelectionMode(boolean bOptionChoiceSelectionMode) {
		this.bOptionChoiceSelectionMode = bOptionChoiceSelectionMode;
		return getThis();
	}

	protected void clearList() {
		adleCompleteEntriesList.clear();
	}

	protected ArrayList<DialogListEntryData> getCompleteEntriesListCopy() {
		return new ArrayList<DialogListEntryData>(adleCompleteEntriesList);
	}

	protected DialogListEntryData<ACT,?> getAbove(DialogListEntryData<ACT,?> dled){
		int iDataAboveIndex = adleCompleteEntriesList.indexOf(dled)-1;
		if(iDataAboveIndex>=0){
			return adleCompleteEntriesList.get(iDataAboveIndex);
		}
		return null;
	}

	/**
	 * for proper sorting
	 * @param dled
	 */
	protected void recursiveAddNestedEntries(DialogListEntryData<ACT,?> dled){
		adleTmp.remove(dled);
		adleCompleteEntriesList.add(dled);
		if(dled.isParent()){
			if(btgSortListEntries.b()){
				dled.sortChildren(getComparatorText(btgSortListEntriesAtoZ.b()));
			}
			
			for(DialogListEntryData<ACT,?> dledChild:dled.getChildrenCopy()){
				if(!dledChild.getParent().equals(dled)){
					throw new PrerequisitesNotMetException("invalid parent", dled, dledChild);
				}
				recursiveAddNestedEntries(dledChild);
			}
		}
	}
	
	protected final BoolTogglerCmdField btgSortListEntries = new BoolTogglerCmdField(this, true);
	protected final BoolTogglerCmdField btgSortListEntriesAtoZ = new BoolTogglerCmdField(this, true);
	
	protected void prepareTree(){
		adleTmp = new ArrayList<DialogListEntryData>(adleCompleteEntriesList);
		
		if(btgSortListEntries.b()){
			// will work basically for the root entries only
			Collections.sort(adleTmp, getComparatorText(btgSortListEntriesAtoZ.b()));
		}
		
		adleCompleteEntriesList.clear();
		
		// work on root ones
		for(DialogListEntryData dled:new ArrayList<DialogListEntryData>(adleTmp)){
			if(dled.getParent()==null){ 
				recursiveAddNestedEntries(dled);
			}
		}
	}
	
	protected DialogListEntryData<ACT,?> addEntry(DialogListEntryData<ACT,?> dled) {
		if(dled==null)throw new PrerequisitesNotMetException("cant be null!");
		
		if(dled.getOwner()==null){
			dled.setOwner(this);
		}else{
			if(dled.getOwner()!=this){
				throw new PrerequisitesNotMetException("inconsistency", this, dled.getOwner(), dled);
			}
		}
		
		if(adleCompleteEntriesList.contains(dled)){
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("list already contains", adleCompleteEntriesList, dled, this);
		}else{
			adleCompleteEntriesList.add(dled);
		}
		return dled;
	}

	protected void removeEntry(DialogListEntryData<ACT,?> dled){
		removeEntry(dled, null);
	}
	private void removeEntry(DialogListEntryData<ACT,?> dled, DialogListEntryData<ACT,?> dledParent){
//		int iDataAboveIndex = -1;
		DialogListEntryData<ACT,?> dledAboveTmp = null;
		if(getSelectedEntryData().equals(dled)){
//			iDataAboveIndex = adleCompleteEntriesList.indexOf(data)-1;
//			if(iDataAboveIndex>=0)dataAboveTmp = adleCompleteEntriesList.get(iDataAboveIndex);
			dledAboveTmp = getAbove(dled);
		}
		DialogListEntryData<ACT,?> dledParentTmp = dled.getParent();
		
		ArrayList<DialogListEntryData> adledChildList = dled.getChildrenCopy();
		boolean bIsParent=false;
		if(adledChildList.size()>0)bIsParent=true;
		for(DialogListEntryData<ACT,?> dledChild:adledChildList){
			removeEntry(dledChild,dled); //recursive
		}
		
		if(adleCompleteEntriesList.remove(dled)){
			if(dledParent==null){ //play sound only for the initial entry
				AudioUII.i().play(bIsParent?
					EAudio.RemoveSubTreeEntry : EAudio.RemoveEntry);
			}
		}else{
			throw new PrerequisitesNotMetException("missing data at list", dled);
		}
		
		dled.setParent(null);
		
		if(dledAboveTmp!=null){
			updateSelected(dledAboveTmp,dledParentTmp);
		}
		
		requestRefreshUpdateList();
		
	}
	
//	protected abstract void updateSelected(DialogListEntryData<T> dledPreviouslySelected);
	protected abstract void updateSelected(final DialogListEntryData<ACT,?> dledAbove, final DialogListEntryData<ACT,?> dledParentTmp);

	protected Node getNorthContainer() {
		return cntrNorth;
	}
	
	protected Node getSouthContainer() {
		return cntrSouth;
	}

	protected THIS setContainerNorth(Node cntrNorth) {
		this.cntrNorth = cntrNorth;
		return getThis();
	}

	protected THIS setCntrSouth(Node cntrSouth) {
		this.cntrSouth = cntrSouth;
		return getThis();
	}
	
	protected DialogListEntryData<ACT,?> getLastSelected(){
		return dleLastSelected;
	}
	
	/**
	 * will always be in lower case
	 * @return
	 */
	public String getLastFilter() { //no problem be public
		return strLastFilter;
	}
	
//	protected R setLastFilter(String str) {
//		this.strLastFilter=str;
//		return getThis();
//	}
	
	public String getTitle() {
		return strTitle;
	}
	
	public boolean isDiagStyleSet(){
		return !svfStyle.isValueNull();
	}
	
	public String getDiagStyle() {
		return svfStyle.getValueAsString();
	}

	protected void setStyle(String strStyle) {
//		this.strStyle = strStyle;
//		svfStyle.setObjectRawValue(strStyle);
		svfStyle.setObjectRawValue(strStyle);
	}

	public String getUserEnterCustomValueToken() {
		return strUserEnterCustomValueToken;
	}

	protected void setUserEnterCustomValueToken(String strUserEnterCustomValueToken) {
		this.strUserEnterCustomValueToken = strUserEnterCustomValueToken;
	}

	protected Spatial getMainList() {
		return sptMainList;
	}

	protected void setMainList(Spatial sptListEntries) {
		this.sptMainList = sptListEntries;
	}

//	public void setTitle(String strTitle) {
//		this.strTitle = strTitle;
//	}
	
	/**
	 * Main/default action to be performed on the selected list entry.
	 * Usually when pressing Enter or double click.
	 * 
	 * @param dataSelected
	 */
	protected void actionMainAtEntry(DialogListEntryData<ACT,?> dataSelected, Spatial sptActionSourceElement){
		AudioUII.i().playOnUserAction(AudioUII.EAudio.SubmitSelection);
	}
	
	public abstract void focusGained();
	public abstract void focusLost();
	
	
	/**
	 * TODO remove after completed
	 */
	private static class _TMP_REMOVE_ME_ extends DialogStateAbs{
		@Override		protected DialogListEntryData getSelectedEntryData() {return null;}
		@Override		protected void updateSelected(DialogListEntryData dledAbove,DialogListEntryData dledParentTmp) {}
		@Override		public void focusGained() {		}
		@Override		public void focusLost() {		}
		@Override		protected void updateInputField() {		}
		@Override		protected void updateList() {		}
		
		//TODO verify below what can go to BaseDialogHelper or LemurBaseDialogHelper  
		@Override		protected boolean initKeyMappings() {			return false;		}
		@Override		protected void lineWrapDisableForChildrenOf(Node node) {		}
		@Override		public void clearSelection() {		}
		@Override		protected void updateTextInfo() {		}
		@Override		protected DialogStateAbs getThis() {			return null;		}
		@Override		protected void setPositionSize(Vector3f v3fPos, Vector3f v3fSize) {		}
		@Override		protected DialogStateAbs getDiagChoice(DialogListEntryData dledSelected) { return null;	}
		@Override		public Spatial getLinkedSpatial() {return null;}
		@Override		protected void setBlockerEnabled(boolean b) {		}
	}
	
	/**
	 * simply move around
	 * @param sptDraggedElement
	 * @param v3fDisplacement
	 */
	public void move(Spatial sptDraggedElement, Vector3f v3fDisplacement) {
		getDialogMainContainer().move(v3fDisplacement);
	}
	
	@Override
	public Spatial getLinkedSpatial() {
		return getDialogMainContainer();
	}
	
	protected void restoreDefaultPositionSize(){
	}
	
//	StringVarField svfSavedValues = new 
//	protected void save(){
//		
//	}
	
	public static enum ESaveKey{
//		posX,
//		posY,
//		width,
//		height, 
		FieldList,
		;
		public String s(){return this.toString();}
	}
	
//	enum E{
//		Id,
//		;
//		public String s(){return this.toString();}
//	}
	
//	public static class DialogSavable implements Savable, IReflexFillCfg{
//		private BaseDialogStateAbs	diag;
//		
//		private SaveString svsId = new SaveString(this, null);
//		private SaveFloat svfPosX = new SaveFloat(this, null);
//		private SaveFloat svfPosY = new SaveFloat(this, null);
//		private SaveFloat svfWidth = new SaveFloat(this, null);
//		private SaveFloat svfHeight = new SaveFloat(this, null);
//		
//		public DialogSavable(){} //required when loading
//		
//		public BaseDialogStateAbs getOwner(){
//			return diag;
//		}
//		
//		public DialogSavable setOwner(BaseDialogStateAbs diag){
//			this.diag=diag;
//			final BaseDialogStateAbs diagF = diag;
//			
//			CallQueueI.i().addCall(new CallableX(this,1000) {
//				@Override
//				public Boolean call() {
//					if(diagF.cfg.v3fIniPos==null)return false; //retry later
//					
//					svsId.setDefaultValue(diagF.getId());
//					svfPosX.setDefaultValue(diagF.cfg.v3fIniPos.x);
//					svfPosY.setDefaultValue(diagF.cfg.v3fIniPos.y);
//					svfWidth.setDefaultValue(diagF.cfg.v3fIniSize.x);
//					svfHeight.setDefaultValue(diagF.cfg.v3fIniSize.y);
//					
//					return true;
//				}
//			}.setQuietOnFail(true)); //will keep retrying until the dialog shows up and its values get initialized properly
//			
//			return this;
//		}
//		
//		@Override
//		public void write(JmeExporter ex) throws IOException {
//			if(!diag.isInitializedProperly())return; //just skip
//			
//			svsId.setValue(diag.getId());
//			
//			Vector3f v3fPos = diag.getDialogMainContainer().getLocalTranslation().clone();
//			svfPosX.setValue(v3fPos.x);
//			svfPosY.setValue(v3fPos.y);
//			
//			Vector3f v3fSize = diag.getMainSize();
//			svfWidth.setValue(v3fSize.x);
//			svfHeight.setValue(v3fSize.y);
//			
//			OutputCapsule oc = ex.getCapsule(this);
//			oc.writeSavableArrayList(SaveValueAbs.getSaveValueListCopy(this), ESaveKey.FieldList.s(), null);
//		}
//
//		@Override
//		public void read(JmeImporter im) throws IOException {
//	    InputCapsule ic = im.getCapsule(this);
//	    
//	    // apply loaded values to this owner fields
//	    ArrayList<SaveValueAbs> asvFieldList = ic.readSavableArrayList(ESaveKey.FieldList.s(), null);
//    	for(SaveValueAbs svLoaded:asvFieldList){svLoaded.setOwner(this);}
//	    for(SaveValueAbs sv:SaveValueAbs.getSaveValueListCopy(this)){
//	    	for(SaveValueAbs svLoaded:asvFieldList.toArray(new SaveValueAbs[0])){
//	    		if(svLoaded==null){
//	    			GlobalCommandsDelegatorI.i().dumpWarnEntry("null saved field value? failed to load", this);
//	    			return;
//	    		}
//	    		
//		    	if(sv.getUniqueId().equals(svLoaded.getUniqueId())){
//		    		sv.setValue(svLoaded.getValue());
//		    		asvFieldList.remove(svLoaded);
//		    		break;
//		    	}
//	    	}
//	    }
//			
//	  	final HashChangeHolder<Vector3f> hchv3fPos = new HashChangeHolder<Vector3f>(new Vector3f());
//	  	final HashChangeHolder<Vector3f> hchv3fSize = new HashChangeHolder<Vector3f>(new Vector3f());
//			try {
//				hchv3fPos.getObjRef().set(
//					MiscI.i().assertValidFloating(svfPosX.getValue()),
//					MiscI.i().assertValidFloating(svfPosY.getValue()),
//					0);
//				
//				hchv3fSize.getObjRef().set(
//					MiscI.i().assertValidFloating(svfWidth.getValue()),
//					MiscI.i().assertValidFloating(svfHeight.getValue()),
//	    		0);
//			} catch (IllegalArgumentException e) {
//				GlobalCommandsDelegatorI.i().dumpExceptionEntry(e,im,ic,this);
//				return;
//			}
//	  	
//			// apply to the dialog whenever it is ready
//			CallQueueI.i().addCall(new CallableX(this,1000) {
//				private BaseDialogStateAbs diagApplyAt;
//
//				@Override
//				public Boolean call() {
//		    	diagApplyAt = ConditionalStateManagerI.i().getConditionalState(
//			    		BaseDialogStateAbs.class, svsId.getValue());
//		    	if(diagApplyAt==null)return false;
//					if(!diagApplyAt.isInitializedProperly())return false;
//			    
//			    diagApplyAt.setPositionSize(hchv3fPos.getObjRef(), hchv3fSize.getObjRef());
//			    
//			    return true;
//				}
//			}.setQuietOnFail(true)); //will calmily wait dialog get ready
//	 
//		}
//
//		@Override
//		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//			ReflexFillCfg rfcfg = new ReflexFillCfg(rfcv);
//			
//			if(rfcv.getClass().isInstance(SaveFloat.class)){
//				rfcfg.setCodingStyleFieldNamePrefix(rfcv.getCodePrefixVariant());
//			}
//			
//			return rfcfg;
//		}
//		
//	}
	
	protected abstract void setPositionSize(Vector3f v3fPos, Vector3f v3fSize);

//	private DialogSavable dsv;
//	private boolean	bRequestSaveDialog;
//	public DialogSavable getSavable(BaseDialogHelper.CompositeControl cc) {
//		cc.assertSelfNotNull();
//		return dsv;
//	}

//	public void requestSaveDialog() {
//		this.bRequestSaveDialog=true;
//		GlobalDialogHelperI.i().requestSaveDialog(this);
//	}
	
//	public boolean isRequestSaveDialogAndReset(BaseDialogHelper.CompositeControl cc){
//		cc.assertSelfNotNull();
//		boolean b=this.bRequestSaveDialog;
//		this.bRequestSaveDialog=false;
//		return b;
//	}
	
	public static class SaveDiag<T extends DialogStateAbs> extends CompositeSavableAbs<T,SaveDiag<T>> {//implements IReflexFillCfg{
		public SaveDiag(){super();}; //required by savable
		@Override public SaveDiag getThis() {return this;}
		
		public SaveDiag(T owner) {
			super(owner);
		}
		
//		@Override
//		protected void init(boolean bLoadedTmp) {}
		
//		private String strId=null;
		
		private float fPosX;
		private float fPosY;
		private float fWidth;
		private float fHeight;
		private float fNorthHeight;
		private String strInputText;
		private boolean bMaximized;
		
		@Override
		protected void initialize() {
			super.initialize();
			
			this.fPosX=0;
			this.fPosY=0;
			this.fWidth=20; //min on invalid 
			this.fHeight=20; //min on invalid
			this.fNorthHeight=0;
			this.strInputText="";
		}
		
//		@Override
//		public CompositeSavableConcrete setOwner(BaseDialogStateAbs owner) {
//			super.setOwner(owner);
////			strId=getOwner().getId();
//			return getThis();
//		}
		
		@Override
		public void write(JmeExporter ex) throws IOException {
			// basically update everything before writing, in case not done outside here
//			strId=getOwner().getId();
			
			if(!bMaximized){
				setPos(getOwner().getDialogMainContainer().getLocalTranslation().clone());
				setSize(getOwner().getMainSizeCopy());
			}
			
			strInputText = getOwner().getInputText();
			
			fNorthHeight = getOwner().getNorthHeight();
			
			GlobalCommandsDelegatorI.i().dumpDevInfoEntry(toString());
			
			super.write(ex);
		}
		
		private void setSize(Vector3f v3fSize) {
			setWidth(v3fSize.x);
			setHeight(v3fSize.y);
		}
		private void setPos(Vector3f v3fPos) {
			setPosX(v3fPos.x);
			setPosY(v3fPos.y);
		}
		
		@Override
		public void read(JmeImporter im) throws IOException {
			super.read(im);
			
			GlobalCommandsDelegatorI.i().dumpDevInfoEntry(toString());
		}
		
		@Override
		public boolean applyValuesFrom(SaveDiag<T> svLoaded) {
			if(!super.applyValuesFrom(svLoaded))return false;
			
			getOwner().setInputText(svLoaded.strInputText);
			getOwner().applyCurrentSettings(false);
			getOwner().setNorthHeight(svLoaded.fNorthHeight, false);
			
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CompositeSavable [fPosX=");
			builder.append(fPosX);
			builder.append(", fPosY=");
			builder.append(fPosY);
			builder.append(", fWidth=");
			builder.append(fWidth);
			builder.append(", fHeight=");
			builder.append(fHeight);
			builder.append("]");
			return builder.toString();
		}
		
//		@Override
//		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {
//			ReflexFillCfg rfcfg = getOwner().getReflexFillCfg(rfcvField);
//			rfcfg.setConcreteClassOverride(getOwner().getClass());
////			rfcfg.setPrefixCustomId(getOwner().getId());
//			return rfcfg;
//		}
		public float getPosX() {
			return fPosX;
		}
		protected void setPosX(float fPosX) {
			this.fPosX = fPosX;
		}
		public float getPosY() {
			return fPosY;
		}
		protected void setPosY(float fPosY) {
			this.fPosY = fPosY;
		}
		public float getWidth() {
			return fWidth;
		}
		protected void setWidth(float fWidth) {
			this.fWidth = fWidth;
		}
		public float getHeight() {
			return fHeight;
		}
		protected void setHeight(float fHeight) {
			this.fHeight = fHeight;
		}
		public String getInputText() {
			return strInputText;
		}
		protected void setInputText(String strInputText) {
			this.strInputText = strInputText;
		}
		public boolean isMaximized(){
			return this.bMaximized;
		}
		public boolean toggleMaximized() {
			return this.bMaximized=!this.bMaximized;
		}
		public void setMaximized(boolean bMaximized) {
			this.bMaximized = bMaximized;
		}
		@Override
		public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
			return fld.get(this);
		}
		@Override
		public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
			fld.set(this,value);
		}
		
	}
	private SaveDiag sv;
	
	public boolean isCompositeSavableSet(){
		return this.sv!=null;
	}
	protected <CS extends SaveDiag<?>> void setCompositeSavable(CS sv) {
		PrerequisitesNotMetException.assertNotAlreadySet(this.sv, sv, CompositeSavableAbs.class.getSimpleName(), this);
		this.sv = sv;
	}
	protected <CS extends SaveDiag<?>> CS getCompositeSavable(Class<CS> clCS) {
		return (CS)this.sv;
	}
	
	private String strFilePrefix="Dialog_";
	public void save(){
		if(!isSaveDialog())return;
		if(sv==null)return;
		MiscJmeI.i().saveWriteConsoleData(strFilePrefix+getUniqueId(), sv);
	}
	/**
	 * override me!
	 */
	public void reLoad(){
		load(SaveDiag.class);
	}
	public <T extends SaveDiag> T load(Class<T> clCS){
		if(!isSaveDialog())return null;
		T svTmp = MiscJmeI.i().loadReadConsoleData(clCS, strFilePrefix+getUniqueId());
		if(sv.applyValuesFrom(svTmp)){
//			applyCurrentSettings(false);
//			setPositionSize(new Vector3f(sv.fPosX, sv.fPosY, 0), new Vector3f(sv.fWidth,sv.fHeight,0));
		}
		return svTmp;
	}
	
//	public static class RestartCfg extends ConditionalStateAbs.RestartCfg{
//		private boolean	bRestartWithoutLoadingOnce = false;
//		private boolean	bCopyToSelfAllEntries = true;
//
////		public boolean isRestartWithoutLoadingOnce() {
////			return bRestartWithoutLoadingOnce;
////		}
//
//		/**
//		 * this will basically grant a first save based on the default values
//		 * @return
//		 */
//		public void setRestartWithoutLoadingOnce(boolean bRestartWithoutLoadingOnce) {
//			this.bRestartWithoutLoadingOnce = bRestartWithoutLoadingOnce;
//		}
//
//		public boolean isRestartWithoutLoadingOnceAndReset() {
//			if(bRestartWithoutLoadingOnce){
//				setRestartWithoutLoadingOnce(false);
//				return true;
//			}
//			
//			return false;
//		}
//
//		public void setCopyToSelfAllEntries(boolean bCopyToSelfAllEntries) {
//			this.bCopyToSelfAllEntries=bCopyToSelfAllEntries;
//		}
//		
//		public boolean isCopyToSelfAllEntries(){
//			return bCopyToSelfAllEntries;
//		}
//	}
//	@Override
//	protected RestartCfg getRestartCfg() {
//		return (RestartCfg) super.getRestartCfg();
//	}
	
	private int iSaveLoadRetryDelayMilis=100;
//	private int saveLoadWarnAfterFailTimes(){
//		return (getEffectMaxTimeMilis()/iSaveLoadRetryDelayMilis)+1;
//	}
	public final StringCmdField scfSave = new StringCmdField(this)
		.setCallerAssigned(new CallableX(this,iSaveLoadRetryDelayMilis) {@Override public Boolean call() {
			if(!isDialogEffectsDone()){
				setQuietOnFail(true);
				setRetryOnFail(true);
				return false; //wait it end
			}
			
			setQuietOnFail(false);
			setRetryOnFail(false);
			save();
			
			return true;
//		}}.setFailWarnEveryTimes(saveLoadWarnAfterFailTimes())); //100 * 30 = 3s for the dialog effects to finish
		}});
	
//	CallableX callerLoad = new CallableX(this,iSaveLoadRetryDelayMilis) {@Override public Boolean call() {
//		if(!isDialogEffectsDone()){
//			setQuietOnFail(true);
//			setRetryOnFail(true);
//			return false; //wait it end
//		}
//		
//		if(sv==null)return false;
//		
//		setQuietOnFail(false);
//		setRetryOnFail(false);
//		load(sv.getClass());
//		
//		return true;
////	}}.setFailWarnEveryTimes(saveLoadWarnAfterFailTimes()));
//	}};
	public final StringCmdField scfLoad = new StringCmdField(this)
//		.setCallerAssigned(callerLoad);
		.setCallerAssigned(new CallableX(this,iSaveLoadRetryDelayMilis) {@Override public Boolean call() {
			if(!isDialogEffectsDone()){
				setQuietOnFail(true);
				setRetryOnFail(true);
				return false; //wait it end
			}
			
			if(sv==null)return false;
			
			setQuietOnFail(false);
			setRetryOnFail(false);
			load(sv.getClass());
			
			return true;
//		}}.setFailWarnEveryTimes(saveLoadWarnAfterFailTimes()));
		}});
	
	private boolean	bBeingDragged;
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=DialogStateAbs.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=DialogStateAbs.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
	
	private Object objDragManagerKey = null;
	/**
	 * 
	 * @param objDragManagerKey the 
	 * @param b
	 */
	public void setBeingDragged(Object objDragManagerKey, boolean b) {
		if(this.objDragManagerKey==null){
			if(objDragManagerKey==null){
				GlobalCommandsDelegatorI.i().dumpProblemEntry("drag manager cannot be null");
				return;
			}
			this.objDragManagerKey=objDragManagerKey;
		}else{
			if(this.objDragManagerKey!=objDragManagerKey){
				GlobalCommandsDelegatorI.i().dumpProblemEntry("cannot change drag manager", this.objDragManagerKey, objDragManagerKey);
				return;
			}
		}
		
		this.bBeingDragged=b;
	}
	
	public boolean isBeingDragged(){
		return this.bBeingDragged;
	}

	private boolean bRequestHitBorderToContinueDragging = false;
	private String	strLastEntryUniqueId="0";
	private boolean	bSavedBeforeExiting;
	public void setRequestHitBorderToContinueDragging(boolean b) {
		this.bRequestHitBorderToContinueDragging = b;
	}
	public boolean isRequestHitBorderToContinueDragging(){
		return this.bRequestHitBorderToContinueDragging;
	}
	public void setNorthHeight(float fHeight, boolean bUseAsDiagPerc){
		throw new PrerequisitesNotMetException("override or do not call", this);
	}
	public float getNorthHeight() {
		throw new PrerequisitesNotMetException("override or do not call", this);
	}
	public void applyCurrentSettings(boolean bToggleMaximized){
		throw new PrerequisitesNotMetException("override or do not call", this);
	}
	public Vector3f getNorthContainerSizeCopy(){
		throw new PrerequisitesNotMetException("override or do not call", this);
	}
	
	@Override
	public THIS copyToSelfValuesFrom(THIS diagDiscarding) {
		super.copyToSelfValuesFrom(diagDiscarding);
		
		if(cfg.isRestartCopyToSelfAllEntries()){
			adleCompleteEntriesList.addAll(diagDiscarding.getCompleteEntriesListCopy());
		}
		
		return getThis();
	}

	public String getCreateNextEntryUniqueId() { //id is not index as entries can be removed will have gaps
		return strLastEntryUniqueId = MiscI.i().getNextUniqueId(strLastEntryUniqueId);
	}
	
//	public ArrayList<DialogListEntryData<ACT,?>> getCompleteEntriesListCopy(){
//		return new ArrayList<DialogListEntryData<ACT,?>>(adleCompleteEntriesList);
//	}
	
	protected void clearLastFilter(){
		strLastFilter="";
	}
	
	@Override
	public boolean isKeepMouseUngrabbed() {
		return isEnabled();
	}
	
	@Override
	public boolean prepareToDiscard(CompositeControl cc) {
		if(!super.prepareToDiscard(cc))return false;
		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,false);
		return true;
	}

	public float getInputFieldHeight(){
		return MiscJmeI.i().retrieveBitmapTextFor((Node)getInputField()).getLineHeight();
	}
	
	@Override
	public boolean isCanCleanExit() {
		if(!bSavedBeforeExiting){
			save();
			bSavedBeforeExiting=true;
		}
		
		return super.isCanCleanExit();
	}

	protected DialogStateAbs getDiagChoice(DialogListEntryData dledSelected){
		throw new PrerequisitesNotMetException("must be overriden by list maintainers");
	}
}
