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

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
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
	
	BoolTogglerCmdField btgGrowEffect = new BoolTogglerCmdField(this, false); //TODO WIP
	
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
		private Node nodeGUI;
		private boolean bInitiallyEnabled = false; //the console needs this "true"
		public CfgParm(String strUIId, Node nodeGUI){//, R diagParent) {
			super(strUIId);
			this.nodeGUI = nodeGUI;
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
			btgState.setValue(false);//,false);
		}
		
//		MouseCursor.i().configure(cfg.lMouseCursorClickDelayMilis);
		
		if(cfg.nodeGUI==null)cfg.nodeGUI=GlobalGUINodeI.i();
		setNodeGUI(cfg.nodeGUI);//getNodeGUI()
		
//		this.diagParent=cfg.diagParent;
//		updateModalParent();
		
		setCmdPrefix("toggleUI");
		setCmdSuffix("");
		
//		ConditionalStateManagerI.i().
		if(cfg.getId()==null || cfg.getId().isEmpty())throw new PrerequisitesNotMetException("invalid UI identifier");
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
	protected boolean initOrUndo() {
		if(!initGUI())return false;
		if(!initKeyMappings())return false;
		return super.initOrUndo();
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
	protected boolean updateOrUndo(float tpf) {
		DialogListEntryData<T> dle = getSelectedEntryData();
		if(dle!=dleLastSelected)AudioUII.i().play(AudioUII.EAudio.EntrySelect);
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
		
//		if(btgGrowEffect.b()){
			Vector3f v3fScale = sptContainerMain.getLocalScale();
			if(v3fScale.x<1f)v3fScale.x+=0.01f;
			if(v3fScale.x>1f)v3fScale.x=1f;
			if(v3fScale.y<1f)v3fScale.y+=0.01f;
			if(v3fScale.y>1f)v3fScale.y=1f;
//		}
		
		return super.updateOrUndo(tpf);
	}
	
	@Override
	protected boolean enableOrUndo() {
		updateAllParts();
		
		getNodeGUI().attachChild(sptContainerMain);
		
		setMouseCursorKeepUngrabbed(true);
		if(diagParent!=null)diagParent.updateModalChild(true,this);
//		updateModalParent(true);
		
		if(btgGrowEffect.b()){
			Vector3f v3fScale = sptContainerMain.getLocalScale();
			v3fScale.x=0.01f;
			v3fScale.y=0.01f;
		}
		
		return super.enableOrUndo();
	}
	
	@Override
	protected boolean disableOrUndo() {
		sptContainerMain.removeFromParent();
		
		setMouseCursorKeepUngrabbed(false);
		if(diagParent!=null)diagParent.updateModalChild(false,this);
//		updateModalParent(false);
		
		return super.disableOrUndo();
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
	protected void recursiveAddEntries(DialogListEntryData<T> dled){
		adleTmp.remove(dled);
		adleCompleteEntriesList.add(dled);
		if(dled.isParent()){
			for(DialogListEntryData<T> dledChild:dled.getChildrenCopy()){
				if(!dledChild.getParent().equals(dled)){
					throw new PrerequisitesNotMetException("invalid parent", dled, dledChild);
				}
				recursiveAddEntries(dledChild);
			}
		}
	}

	protected void sortEntries(){
		adleTmp = new ArrayList<DialogListEntryData<T>>(adleCompleteEntriesList);
		adleCompleteEntriesList.clear();
		
		for(DialogListEntryData<T> dled:new ArrayList<DialogListEntryData<T>>(adleTmp)){
			if(dled.getParent()==null){ //root ones
				recursiveAddEntries(dled);
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
		svfStyle.setValue(strStyle);
	}

//	public void setTitle(String strTitle) {
//		this.strTitle = strTitle;
//	}
	
}
