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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.MiscLemurHelpersStateI;
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
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 * @param <T> see {@link DialogListEntryData}
 * @param <R> is for getThis() concrete auto class type inherited trick
 */
public abstract class BaseDialogStateAbs<T, R extends BaseDialogStateAbs<T,R>> extends CmdConditionalStateAbs implements IReflexFillCfg{
	private Spatial	sptContainerMain;
	private Spatial	sptIntputField;
	private String	strTitle;
//	private String strStyle;
	private StringVarField svfStyle = new StringVarField(this, (String)null, null);
	
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
	
	private Comparator<DialogListEntryData<T>> cmpTextAtoZ = new Comparator<DialogListEntryData<T>>() {
		@Override
		public int compare(DialogListEntryData<T> o1, DialogListEntryData<T> o2) {
			return o1.getText().compareTo(o2.getText());
		}
	};
	private Comparator<DialogListEntryData<T>> cmpTextZtoA = new Comparator<DialogListEntryData<T>>() {
		@Override
		public int compare(DialogListEntryData<T> o1, DialogListEntryData<T> o2) {
			return o2.getText().compareTo(o1.getText());
		}
	};
	private Comparator<DialogListEntryData<T>> getComparatorText(boolean bAtoZ){
		return bAtoZ ? cmpTextAtoZ : cmpTextZtoA;
	}
	
	public DiagModalInfo<T> getDiagModalCurrent(){
		return dmi;
	}
	
	public R setDiagModalInfo(DiagModalInfo<T> dmi){
		this.dmi=dmi;
		return getThis();
	}
	
	public Spatial getContainerMain(){
		return sptContainerMain;
	}
	
	protected R setContainerMain(Spatial spt){
		this.sptContainerMain=spt;
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
	private Vector3f	v3fMainLocationBkp;
	private Vector3f	v3fMainSize;
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
			btgState.setObjectRawValue(false);//,false);
		}
		
//		MouseCursor.i().configure(cfg.lMouseCursorClickDelayMilis);
		
//		if(cfg.nodeGUI==null)cfg.nodeGUI=GlobalGUINodeI.i();
//		setNodeGUI(cfg.nodeGUI);//getNodeGUI()
		
//		this.diagParent=cfg.diagParent;
//		updateModalParent();
		
		setCmdPrefix("toggleUI");
		setCmdSuffix("");
		
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
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and instantiate everything to make it actually be able to work.
	 */
	@Override
	protected boolean initAttempt() {
		if(!initGUI())return false;
		if(!initKeyMappings())return false;
		return super.initAttempt();
	}
	
	protected abstract boolean initGUI();
	protected abstract boolean initKeyMappings();
	public abstract String getInputText();
	
	public void requestActionSubmit() {
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
		updateAllParts();
		
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
		
		return super.enableAttempt();
	}
	
	public boolean isDialogEffectsDone(){
		return !tdDialogEffect.isActive();
	}
	
	public abstract Vector3f getMainSize();
	
	@Override
	protected boolean disableAttempt() {
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
		
		return super.disableAttempt();
	}
	
	/**
	 * This is important to prevent other parts of the application from 
	 * vanishing with (grabbing) the mouse cursor.
	 * @param b
	 */
	public R setMouseCursorKeepUngrabbed(boolean b) {
		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,b);
		return getThis();
	}
	
	public R setTitle(String str){
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
	protected Spatial getInputField() {
		return sptIntputField;
	}
	
	public float getInputFieldHeight(){
		return MiscJmeI.i().retrieveBitmapTextFor((Node)sptIntputField).getLineHeight();
	}
	
	protected R setIntputField(Spatial sptIntputField) {
		this.sptIntputField = sptIntputField;
		return getThis();
	}

	public R getParentDialog(){
		return (R)this.diagParent;
	}
	
	public R setDiagParent(BaseDialogStateAbs<T,?> diagParent) {
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
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	protected abstract void updateInputField();
	protected abstract void updateList();
	protected abstract void updateTextInfo();
	public abstract DialogListEntryData<T> getSelectedEntryData();
	
	public void requestRefreshList(){
		bRequestedRefreshList=true;
	}
	
	protected String getTextInfo(){
		String str="Info: Type a list filter at input text area and hit Enter.\n";
		
		if(isOptionChoiceSelectionMode()){
			str+="Option Mode: when hitting Enter, if an entry is selected, it's value will be chosen.\n";
			
			if(getParentDialog()!=null){
				if(getParentDialog().getDiagModalCurrent()!=null){
					for(DialogListEntryData<T> data:getParentDialog().getDiagModalCurrent().getDataReferenceAtParentListCopy()){
						str+="ParentCfgData: "+data.getText()+"\n";
					}
				}
			}
			
		}
		
		return str;
	}
	
	/**
	 * override empty to disable filter
	 */
	protected void applyListKeyFilter(){
		this.strLastFilter=getInputText();
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
					requestRefreshList();
				}else{
					actionCustomAtEntry(dataSelected);
				}
			}
		}
		
	}
	
	
	/**
	 * Main/default action to be performed on the selected list entry.
	 * Usually when pressing Enter or double click.
	 * 
	 * @param dataSelected
	 */
	protected void actionCustomAtEntry(DialogListEntryData<T> dataSelected){
		AudioUII.i().playOnUserAction(AudioUII.EAudio.SubmitSelection);
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
		public ArrayList<DialogListEntryData<T>> getDataReferenceAtParentListCopy() {
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

	public R resetChoice() {
//		lChoiceMadeAtMilis=null;
		adataChosenEntriesList.clear();
		return getThis();
	}

	public boolean isOptionChoiceSelectionMode() {
		return bOptionChoiceSelectionMode;
	}

	public R setOptionChoiceSelectionMode(boolean bOptionChoiceSelectionMode) {
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
			for(DialogListEntryData<T> dledChild:dled.getChildrenCopy()){
				if(!dledChild.getParent().equals(dled)){
					throw new PrerequisitesNotMetException("invalid parent", dled, dledChild);
				}
				recursiveAddNestedEntries(dledChild);
			}
		}
	}
	
	BoolTogglerCmdField btgSortListEntries = new BoolTogglerCmdField(this, true);
	BoolTogglerCmdField btgSortListEntriesAtoZ = new BoolTogglerCmdField(this, true);
	
	protected void prepareTree(){
		adleTmp = new ArrayList<DialogListEntryData<T>>(adleCompleteEntriesList);
		
		if(btgSortListEntries.b()){
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
	
	protected void addEntry(DialogListEntryData<T> dled) {
		if(dled==null)throw new PrerequisitesNotMetException("cant be null!");
		adleCompleteEntriesList.add(dled);
	}

	public void removeEntry(DialogListEntryData<T> dled){
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
	
	protected abstract void updateSelected(DialogListEntryData<T> dledPreviouslySelected);
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

	public String getLastFilter() { //no problem be public
		return strLastFilter;
	}

	protected R setLastFilter(String inputText) {
		this.strLastFilter=inputText;
		return getThis();
	}
	
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

//	public void setTitle(String strTitle) {
//		this.strTitle = strTitle;
//	}
	
}
