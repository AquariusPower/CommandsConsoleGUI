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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalBaseDialogHelperI;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.MiscLemurHelpersStateI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.MsgI;
//import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Container;

/**
 * This class would be useful to a non Lemur dialog too.
 * 
 * A console command will be automatically created based on the configured {@link #strUIId}.<br>
 * See it at {@link BaseDialogStateAbs}.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <T> see {@link DialogListEntryData}
 * @param <R> is for getThis() concrete auto class type inherited trick
 */
public abstract class BaseDialogStateAbs<T, R extends BaseDialogStateAbs<T,R>> extends CmdConditionalStateAbs implements IReflexFillCfg{
	private Spatial	sptContainerMain;
	private Spatial	sptIntputField;
	private Spatial sptMainList;
	private String	strTitle;
//	private String strStyle;
	private StringVarField svfStyle = new StringVarField(this, (String)null, null);
	
	private Vector3f	v3fMainLocationBkp;
	private Vector3f	v3fMainSize;
	private String	strUserEnterCustomValueToken = "=";
	
	private boolean	bUserEnterCustomValueMode;
	private BaseDialogStateAbs<T,?> diagParent;
	private ArrayList<BaseDialogStateAbs<T,?>> aModalChildList = new ArrayList<BaseDialogStateAbs<T,?>>();
//	private DialogListEntryData<T>	dataToCfgReference;
//	private DialogListEntryData<T> dataFromModal;
	private boolean	bRequestedActionSubmit;
//	private Object[]	aobjModalAnswer;
	private Node cntrNorth;
	private Node cntrSouth;
	private String	strLastFilter = "";
//	private ArrayList<DialogListEntry> aEntryList = new ArrayList<DialogListEntry>();
//	private String	strLastSelectedKey;
	private ArrayList<DialogListEntryData<T>>	adleCompleteEntriesList = new ArrayList<DialogListEntryData<T>>();
	private ArrayList<DialogListEntryData<T>>	adleTmp;
	private DialogListEntryData<T>	dleLastSelected;
//	private V	valueOptionSelected;
	private boolean	bRequestedRefreshList;
//	private DialogListEntryData<T>	dataReferenceAtParent;
//	private T	cmdAtParent;
	private DiagModalInfo<T> dmi = null;
	
	private boolean bOptionChoiceSelectionMode = false;
//	private Long	lChoiceMadeAtMilis = null;
	private ArrayList<DialogListEntryData<T>> adataChosenEntriesList = new ArrayList<DialogListEntryData<T>>();
	
	private BoolTogglerCmdField btgEffectLocation = new BoolTogglerCmdField(this, true);
	private BoolTogglerCmdField btgEffect = new BoolTogglerCmdField(this, true);
	private TimedDelayVarField tdDialogEffect = new TimedDelayVarField(this, 0.15f, "");
	private float	fMinEffectScale=0.01f;
	
	protected abstract <N extends Node> void lineWrapDisableForChildrenOf(N node);
	
	private Comparator<DialogListEntryData<T>> cmpTextAtoZ = new Comparator<DialogListEntryData<T>>() {
		@Override
		public int compare(DialogListEntryData<T> o1, DialogListEntryData<T> o2) {
			return o1.getVisibleText().compareTo(o2.getVisibleText());
		}
	};
	private Comparator<DialogListEntryData<T>> cmpTextZtoA = new Comparator<DialogListEntryData<T>>() {
		@Override
		public int compare(DialogListEntryData<T> o1, DialogListEntryData<T> o2) {
			return o2.getVisibleText().compareTo(o1.getVisibleText());
		}
	};
	private Comparator<DialogListEntryData<T>> getComparatorText(boolean bAtoZ){
		return bAtoZ ? cmpTextAtoZ : cmpTextZtoA;
	}
	
	protected DiagModalInfo<T> getChildDiagModalInfoCurrent(){
		return dmi;
	}
	
	protected R setDiagModalInfoCurrent(DiagModalInfo<T> dmi){
		this.dmi=dmi;
		return getThis();
	}
	
	protected Spatial getContainerMain(){
		return sptContainerMain;
	}
	
	protected R setContainerMain(Spatial spt){
		this.sptContainerMain=spt;
		
//		for(Class<?> cl:MiscI.i().getSuperClassesOf(this)){
//			MiscJmeI.i().retrieveUserData(Spatial.class, this.sptContainerMain, cl.getName(), this, null);
//			this.sptContainerMain.setUserData(cl.getName(), new SavableHolder(this));
			MiscJmeI.i().setUserDataSH(this.sptContainerMain, this);
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
		private boolean	bOptionSelectionMode;
//		private Node nodeGUI;
		private boolean bInitiallyEnabled = false; //the console needs this "true"
//		public CfgParm(String strUIId, Node nodeGUI){//, R diagParent) {
		public CfgParm(String strUIId){//, R diagParent) {
			super(strUIId);
//			this.nodeGUI = nodeGUI;
		}
		public void setUIId(String strUIId){
//			if(getId()!=null)throw new PrerequisitesNotMetException("UI Id already set",getId(),strUIId);
			PrerequisitesNotMetException.assertNotAlreadySet("UI id", getId(), strUIId, this);
			setId(strUIId);
		}
		public boolean isInitiallyEnabled() {
			return bInitiallyEnabled;
		}
		public void setInitiallyEnabled(boolean bInitiallyEnabled) {
			this.bInitiallyEnabled = bInitiallyEnabled;
		}
	}
	private CfgParm	cfg;
	@Override
	public R configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
//	private void configure(String strUIId,boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
		
//		this.bOptionChoiceSelectionMode=cfg.bOptionSelectionMode;
		
//		bEnabled=cfg.bInitiallyEnabled;
//		if(!cfg.bInitiallyEnabled)requestDisable();
//		if(!cfg.bInitiallyEnabled){
		
		 /** 
		 * Dialogs must be initially disabled because they are enabled 
		 * on user demand. 
		 */
		if(!cfg.isInitiallyEnabled()){
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
			MsgI.i().warn("using automatic UI id for", this);
		}
//		this.strCaseInsensitiveId=cfg.strUIId;
		
//		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+cfg.getId();
//		btgShowDialog.setCustomCmdId(this.strCmd);
		
		super.configure(cfg);//new CmdConditionalStateAbs.CfgParm(cfg.strUIId, cfg.bIgnorePrefixAndSuffix));
		return storeCfgAndReturnSelf(icfg);
	}
	
	protected Spatial getInputField() {
		return sptIntputField;
	}
//	public abstract String getInputText();
	public String getInputText(){
		return bdh().getTextFromField(getInputField());
	}
	private BaseDialogHelper bdh(){
		return GlobalBaseDialogHelperI.i();
	}
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and instantiate everything to make it actually be able to work.
	 */
	@Override
	protected boolean initAttempt() {
		if(getStyle()==null)setStyle(ConsoleLemurStateI.i().STYLE_CONSOLE);
		if(!super.initAttempt())return false;
		
		if(!initGUI())return false;
		if(!initKeyMappings())return false;
		
		CallableX cxRefresh = new CallableX() {
			@Override
			public Boolean call() {
				requestRefreshList();
				return true;
			}
		};
		btgSortListEntries.setCallOnChange(cxRefresh);
		btgSortListEntriesAtoZ.setCallOnChange(cxRefresh);
		
		return true;
	}
	
	protected boolean initGUI(){
		return true;
	}
	
	protected abstract boolean initKeyMappings();
	
	protected R setInputTextAsUserTypedValue(String str){
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
	
	/**
	 * this will react to changes on the list
	 */
	@Override
	protected boolean updateAttempt(float tpf) {
		DialogListEntryData<T> dle = getSelectedEntryData();
		if(dle!=dleLastSelected)AudioUII.i().play(AudioUII.EAudio.SelectEntry);
//		if(dle!=null)
		dleLastSelected = dle;
		
//		if(isCfgDataValueSet()){
//			updateAllParts();
//		}
		
		if(bRequestedRefreshList){
			updateList();
			lineWrapDisableForChildrenOf((Node)sptMainList);
			bRequestedRefreshList=false;
		}
		
		if(bRequestedActionSubmit){
			actionSubmit();
			bRequestedActionSubmit=false;
		}
		
		if(tdDialogEffect.isActive()){ //dont use btgGrowEffect.b() as it may be disabled during the grow effect
			updateEffect(!isTryingToDisable());
		}
		
		return super.updateAttempt(tpf);
	}
	
	protected boolean updateEffect(boolean bGrow){
		boolean bCompleted=false;
		
		// SCALE
		Vector3f v3fScaleCopy = sptContainerMain.getLocalScale().clone();
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
		
		sptContainerMain.setLocalScale(v3fScaleCopy);
		
		// LOCATION
		if(btgEffectLocation.b()){
			Vector3f v3f = new Vector3f(v3fMainLocationBkp);
			float fPerc = getEffectPerc();
			float fHalfWidth = (v3fMainSize.x/2f);
			float fHalfHeight = (v3fMainSize.y/2f);
			if(!bGrow)fPerc = 1.0f - fPerc;
			v3f.x = v3f.x + fHalfWidth - (fHalfWidth*fPerc);
			v3f.y = v3f.y - fHalfHeight + (fHalfHeight*fPerc);
			MiscLemurHelpersStateI.i().setLocationXY(sptContainerMain,v3f);
		}
		
		if(bCompleted){
			tdDialogEffect.setActive(false);
			
			MiscLemurHelpersStateI.i().setLocationXY(sptContainerMain,v3fMainLocationBkp);
			v3fMainLocationBkp=null;
		}
		
		return bCompleted;
	}
	
	protected float getEffectPerc(){
		return tdDialogEffect.getCurrentDelayPercentual(false);
	}
	
	@Override
	protected boolean enableAttempt() {
		if(!super.enableAttempt())return false;
		
		getNodeGUI().attachChild(sptContainerMain);
		
		setMouseCursorKeepUngrabbed(true);
		if(diagParent!=null)diagParent.updateModalChild(true,this);
//		updateModalParent(true);
		
		if(btgEffect.b()){
			Vector3f v3fScale = sptContainerMain.getLocalScale();
			v3fScale.x=v3fScale.y=fMinEffectScale;
			tdDialogEffect.setActive(true);
			v3fMainLocationBkp = sptContainerMain.getLocalTranslation().clone();
			v3fMainSize = getMainSize();
		}
		
		if(getInputText().isEmpty()){
			if(bUserEnterCustomValueMode)setInputText(getUserEnterCustomValueToken());
		}
		
		return true;
	}
	
	@Override
	protected void enableSuccess() {
		super.enableSuccess();
		updateAllParts();
	}
	
	public boolean isDialogEffectsDone(){
		return !tdDialogEffect.isActive();
	}
	
//	public abstract Vector3f getMainSize();
	public Vector3f getMainSize(){
		return bdh().getSizeFrom(getContainerMain());
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!super.disableAttempt())return false;
		
		if(btgEffect.b()){
			Vector3f v3fScaleCopy = sptContainerMain.getLocalScale().clone();
			
			if(Float.compare(v3fScaleCopy.x,1f)==0){
				tdDialogEffect.setActive(true);
				v3fMainLocationBkp = sptContainerMain.getLocalTranslation().clone();
				return false;
			}else{
				if(Float.compare(v3fScaleCopy.x,fMinEffectScale)>0){
					return false;
				}
			}
		}
		
		sptContainerMain.removeFromParent();
		
		if(btgEffect.b()){ //reset scale to normal
			sptContainerMain.setLocalScale(1f);
		}
		
		setMouseCursorKeepUngrabbed(false);
		if(diagParent!=null)diagParent.updateModalChild(false,this);
//		updateModalParent(false);
		
		return true;
	}
	
	/**
	 * This is important to prevent other parts of the application from 
	 * vanishing with (grabbing) the mouse cursor.
	 * @param b
	 */
	protected R setMouseCursorKeepUngrabbed(boolean b) {
		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,b);
		return getThis();
	}
	
	protected R setTitle(String str){
		this.strTitle = str;
		return getThis();
	}
	
//	@Override
//	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//		return cd().getReflexFillCfg(rfcv);
//	}
	
	/**
	 * TODO rename getInputField()
	 * @param ccSelf 
	 * @return
	 */
	public Spatial getInputField(LemurFocusHelperStateI.CompositeControl ccSelf) {
		ccSelf.assertSelfNotNull();
		return sptIntputField;
	}
	
	public float getInputFieldHeight(){
		return MiscJmeI.i().retrieveBitmapTextFor((Node)sptIntputField).getLineHeight();
	}
	
	protected R setInputField(Spatial sptIntputField) {
		this.sptIntputField = sptIntputField;
		return getThis();
	}
	
	public boolean isInputToUserEnterCustomValueMode(){
		return bUserEnterCustomValueMode;
	}
	public R setInputToUserEnterCustomValueMode(boolean bEnable){
		this.bUserEnterCustomValueMode=bEnable;
		return getThis();
	}
	
//	protected abstract R setInputText(String str);
	protected R setInputText(String str){
		bdh().setTextAt(getInputField(),str);
		return getThis();
	}
	
	protected R getParentDialog(){
		return (R)this.diagParent;
	}
	
	protected R setDiagParent(BaseDialogStateAbs<T,?> diagParent) {
//		if(this.diagParent!=null)throw new PrerequisitesNotMetException("modal parent already set",this.diagParent,diagParent);
		PrerequisitesNotMetException.assertNotAlreadySet("modal parent", this.diagParent, diagParent, this);
		this.diagParent = diagParent;
		return getThis();
	}
	
//	public abstract void setAnswerFromModalChild(Object... aobj);
	
	public ArrayList<BaseDialogStateAbs<T,?>> getModalChildListCopy() {
		return new ArrayList<BaseDialogStateAbs<T,?>>(aModalChildList);
	}
	protected void updateModalChild(boolean bAdd, BaseDialogStateAbs<T,?> baseDialogStateAbs) {
//		if(this.modalParent==null)return;
			
		if(bAdd){
			if(!aModalChildList.contains(baseDialogStateAbs)){
				aModalChildList.add(baseDialogStateAbs);
			}
		}else{
			aModalChildList.remove(baseDialogStateAbs);
		}
	}

	public abstract void clearSelection();
	
	public boolean isOptionSelectionMode(){
		return isOptionChoiceSelectionMode();
	}
	
	protected abstract void updateList();
	protected abstract void updateTextInfo();
	protected abstract DialogListEntryData<T> getSelectedEntryData();
	
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
	
	public void requestRefreshList(){
		bRequestedRefreshList=true;
	}
	
	protected String getTextInfo(){
		String str="";
		
		str+="Help("+BaseDialogStateAbs.class.getSimpleName()+"):\n";
		str+="\tType a list filter at input text area and hit Enter.\n";
		
		if(isOptionChoiceSelectionMode()){
			str+="\tOption Mode: when hitting Enter, if an entry is selected, it's value will be chosen.\n";
			str+="\tBut if the input begins with '"+getUserEnterCustomValueToken()+"', when hitting Enter, that specific value will be returned to the parent.\n";
			
			for(DialogListEntryData<T> dled:getParentReferencedDledListCopy()){
				str+="\tRefAtParent: "+dled.getVisibleText()+"\n";
			}
			
		}
		
		return str;
	}
	
	/**
	 * NO! use {@link #getParentReferencedDledListCopy()} instead
	 * @return
	 */
	@Deprecated
	protected DialogListEntryData<T> getDledReferenceAtParent(){
		throw new PrerequisitesNotMetException("NO! see this method documentation...");
	}
	/**
	 * NO! use {@link #getParentReferencedDledListCopy()} instead
	 */
	@Deprecated
	protected void setDledReferenceAtParent(){
		throw new PrerequisitesNotMetException("NO! see this method documentation...");
	}
	
	protected ArrayList<DialogListEntryData<T>> getParentReferencedDledListCopy() {
		ArrayList<DialogListEntryData<T>> adled = new ArrayList<DialogListEntryData<T>>();
		if(getParentDialog()!=null){
			BaseDialogStateAbs<T, R>.DiagModalInfo<T> dmi = getParentDialog().getChildDiagModalInfoCurrent();
			if(dmi!=null){
				if(dmi.getDiagModal()!=this){
					throw new PrerequisitesNotMetException("current parent's modal dialog should be 'this'",dmi,dmi.getDiagModal(),this);
				}
				
				for(DialogListEntryData<T> dled:dmi.getParentReferencedDledListCopy()){
					adled.add(dled);
				}
			}
		}
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
		updateAllParts();
		
		DialogListEntryData<T> dataSelected = getSelectedEntryData(); //this value is in this console variable now
		
		if(isOptionChoiceSelectionMode()){
			adataChosenEntriesList.clear();
			if(dataSelected!=null){
				adataChosenEntriesList.add(dataSelected); //TODO could be many, use a checkbox for multi-selection
//				AudioUII.i().play(AudioUII.EAudio.ReturnChosen);
				
//			if(getParentDialog()!=null)getParentDialog().setModalChosenData(dataSelected);
//				lChoiceMadeAtMilis=System.currentTimeMillis();
				cd().dumpInfoEntry(this.getId()+": Option Selected: "+dataSelected.toString());
				requestDisable(); //close if there is one entry selected
			}
		}else{
			if(dataSelected!=null){
				if(dataSelected.isParent()){
					dataSelected.toggleExpanded();
//					requestRefreshList();
				}else{
					actionCustomAtEntry(dataSelected);
				}
			}
		}
		
	}
	
	private void updateAllParts(){
		updateTextInfo();
		
		updateList();
		
		updateInputField();
	}
	
	public ArrayList<DialogListEntryData<T>> getDataSelectionListCopy() {
		return new ArrayList<DialogListEntryData<T>>(adataChosenEntriesList);
	}
	
	protected class DiagModalInfo<T>{
		private R	diagChildModal;
		
		private T	actionAtParent;
		private ArrayList<DialogListEntryData<T>>	adledToPerformResultOfActionAtParentList;
		
		public DiagModalInfo(
			R diagModalCurrent,
			T cmdAtParent,
			DialogListEntryData<T>... adledReferenceAtParent 
		) {
			super();
			this.diagChildModal = diagModalCurrent;
			this.adledToPerformResultOfActionAtParentList = new ArrayList<DialogListEntryData<T>>(
				Arrays.asList(adledReferenceAtParent));
			this.actionAtParent = cmdAtParent;
		}
		
		@SuppressWarnings("unchecked")
		public R getDiagModal() {
			return diagChildModal;
		}
//		public void setDiagModal(R diagModal) {
//			this.diagChildModal = diagModal;
//		}
		public ArrayList<DialogListEntryData<T>> getParentReferencedDledListCopy() {
			return new ArrayList<DialogListEntryData<T>>(adledToPerformResultOfActionAtParentList);
		}
//		public void setDataReferenceAtParent(DialogListEntryData<T> dataReferenceAtParent) {
//			this.dataSelectedAtParent = dataReferenceAtParent;
//		}
		public T getCmdAtParent() {
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

	protected R resetChoice() {
//		lChoiceMadeAtMilis=null;
		adataChosenEntriesList.clear();
		return getThis();
	}

	public boolean isOptionChoiceSelectionMode() {
		return bOptionChoiceSelectionMode;
	}

	protected R setOptionChoiceSelectionMode(boolean bOptionChoiceSelectionMode) {
		this.bOptionChoiceSelectionMode = bOptionChoiceSelectionMode;
		return getThis();
	}

	protected void clearList() {
		adleCompleteEntriesList.clear();
	}

	protected ArrayList<DialogListEntryData<T>> getCompleteEntriesListCopy() {
		return new ArrayList<DialogListEntryData<T>>(adleCompleteEntriesList);
	}

	protected DialogListEntryData<T> getAbove(DialogListEntryData<T> dled){
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
	protected void recursiveAddNestedEntries(DialogListEntryData<T> dled){
		adleTmp.remove(dled);
		adleCompleteEntriesList.add(dled);
		if(dled.isParent()){
			if(btgSortListEntries.b()){
				dled.sortChildren(getComparatorText(btgSortListEntriesAtoZ.b()));
			}
			
			for(DialogListEntryData<T> dledChild:dled.getChildrenCopy()){
				if(!dledChild.getParent().equals(dled)){
					throw new PrerequisitesNotMetException("invalid parent", dled, dledChild);
				}
				recursiveAddNestedEntries(dledChild);
			}
		}
	}
	
	protected BoolTogglerCmdField btgSortListEntries = new BoolTogglerCmdField(this, true);
	protected BoolTogglerCmdField btgSortListEntriesAtoZ = new BoolTogglerCmdField(this, true);
	
	protected void prepareTree(){
		adleTmp = new ArrayList<DialogListEntryData<T>>(adleCompleteEntriesList);
		
		if(btgSortListEntries.b()){
			// will work basically for the root entries only
			Collections.sort(adleTmp, getComparatorText(btgSortListEntriesAtoZ.b()));
		}
		
		adleCompleteEntriesList.clear();
		
		// work on root ones
		for(DialogListEntryData<T> dled:new ArrayList<DialogListEntryData<T>>(adleTmp)){
			if(dled.getParent()==null){ 
				recursiveAddNestedEntries(dled);
			}
		}
	}
	
	protected DialogListEntryData<T> addEntry(DialogListEntryData<T> dled) {
		if(dled==null)throw new PrerequisitesNotMetException("cant be null!");
		if(adleCompleteEntriesList.contains(dled)){
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("list already contains", adleCompleteEntriesList, dled, this);
		}else{
			adleCompleteEntriesList.add(dled);
		}
		return dled;
	}

	protected void removeEntry(DialogListEntryData<T> dled){
		removeEntry(dled, null);
	}
	private void removeEntry(DialogListEntryData<T> dled, DialogListEntryData<T> dledParent){
//		int iDataAboveIndex = -1;
		DialogListEntryData<T> dledAboveTmp = null;
		if(getSelectedEntryData().equals(dled)){
//			iDataAboveIndex = adleCompleteEntriesList.indexOf(data)-1;
//			if(iDataAboveIndex>=0)dataAboveTmp = adleCompleteEntriesList.get(iDataAboveIndex);
			dledAboveTmp = getAbove(dled);
		}
		DialogListEntryData<T> dledParentTmp = dled.getParent();
		
		ArrayList<DialogListEntryData<T>> dledChildren = dled.getChildrenCopy();
		boolean bIsParent=false;
		if(dledChildren.size()>0)bIsParent=true;
		for(DialogListEntryData<T> dledChild:dledChildren){
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
		
		requestRefreshList();
		
	}
	
//	protected abstract void updateSelected(DialogListEntryData<T> dledPreviouslySelected);
	protected abstract void updateSelected(final DialogListEntryData<T> dledAbove, final DialogListEntryData<T> dledParentTmp);

	protected Container getNorthContainer() {
		return (Container)cntrNorth;
	}
	
	protected Container getSouthContainer() {
		return (Container)cntrSouth;
	}

	protected R setCntrNorth(Node cntrNorth) {
		this.cntrNorth = cntrNorth;
		return getThis();
	}

	protected R setCntrSouth(Node cntrSouth) {
		this.cntrSouth = cntrSouth;
		return getThis();
	}
	
	protected DialogListEntryData<T> getLastSelected(){
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
	
	/**
	 * implement this only on concrete classes
	 * @return
	 */
	protected abstract R getThis();

	public String getTitle() {
		return strTitle;
	}

	public String getStyle() {
		return svfStyle.getStringValue();
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
	protected void actionCustomAtEntry(DialogListEntryData<T> dataSelected){
		AudioUII.i().playOnUserAction(AudioUII.EAudio.SubmitSelection);
	}
	
	public abstract void focusGained();
	public abstract void focusLost();
	
	
	/**
	 * TODO remove after completed
	 */
	private static class _TMP_REMOVE_ME_ extends BaseDialogStateAbs{
		@Override		protected DialogListEntryData getSelectedEntryData() {return null;}
		@Override		protected void updateSelected(DialogListEntryData dledAbove,DialogListEntryData dledParentTmp) {}
		@Override		public void focusGained() {		}
		@Override		public void focusLost() {		}
		@Override		protected void updateInputField() {		}
		@Override		protected void updateList() {		}
		
		//TODO below
		@Override		protected boolean initKeyMappings() {			return false;		}
		@Override		protected void lineWrapDisableForChildrenOf(Node node) {		}
		@Override		public void clearSelection() {		}
		@Override		protected void updateTextInfo() {		}
		@Override		protected BaseDialogStateAbs getThis() {			return null;		}
	};
}
