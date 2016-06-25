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

import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
//import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * This class would be useful to a non Lemur dialog too.
 * 
 * A console command will be automatically created based on the configured {@link #strUIId}.<br>
 * See it at {@link BaseDialogStateAbs}.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class BaseDialogStateAbs<T> extends CmdConditionalStateAbs implements IReflexFillCfg{
	protected Spatial	sptContainerMain;
	protected Spatial	sptIntputField;
	protected String	strTitle;
	
	protected BaseDialogStateAbs<T> diagParent;
	protected ArrayList<BaseDialogStateAbs<T>> aModalChildList = new ArrayList<BaseDialogStateAbs<T>>();
//	protected DialogListEntryData<T>	dataToCfgReference;
//	protected DialogListEntryData<T> dataFromModal;
	private boolean	bRequestedActionSubmit;
//	private Object[]	aobjModalAnswer;
	protected Node cntrNorth;
	protected Node cntrSouth;
	protected String	strLastFilter = "";
//	protected ArrayList<DialogListEntry> aEntryList = new ArrayList<DialogListEntry>();
//	protected String	strLastSelectedKey;
	protected ArrayList<DialogListEntryData<T>>	adleCompleteEntriesList = new ArrayList<DialogListEntryData<T>>();
	protected ArrayList<DialogListEntryData<T>>	adleTmp;
	protected DialogListEntryData<T>	dleLastSelected;
//	private V	valueOptionSelected;
	protected boolean	bRequestedRefreshList;
//	private DialogListEntryData<T>	dataReferenceAtParent;
//	private T	cmdAtParent;
	protected DiagModalInfo<T> dmi = null;
	
	protected boolean bOptionChoiceSelectionMode = false;
//	protected Long	lChoiceMadeAtMilis = null;
	protected ArrayList<DialogListEntryData<T>> adataChosenEntriesList = new ArrayList<DialogListEntryData<T>>();
	
	public DiagModalInfo<T> getDiagModalInfo(){
		return dmi;
	}
	
	public Spatial getContainerMain(){
		return sptContainerMain;
	}
	
	protected BaseDialogStateAbs<T> setContainerMain(Spatial spt){
		this.sptContainerMain=spt;
		return this;
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
//		protected boolean	bOptionSelectionMode;
//		protected String strUIId;
//		protected boolean bIgnorePrefixAndSuffix;
//		protected Node nodeGUI;
////		protected boolean bInitiallyEnabled;
//		protected BaseDialogStateAbs<T> diagParent;
////		protected Long lMouseCursorClickDelayMilis;
//		public CfgParm(boolean bOptionSelectionMode, String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI, BaseDialogStateAbs<T> diagParent){//, Long lMouseCursorClickDelayMilis) {
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
	
	public static class CfgParm<T> extends CmdConditionalStateAbs.CfgParm{
		protected boolean	bOptionSelectionMode;
		protected String strUIId;
		protected boolean bIgnorePrefixAndSuffix;
		protected Node nodeGUI;
//		protected BaseDialogStateAbs<T> diagParent;
		protected boolean bInitiallyEnabled = false; //the console needs this "true"
		public CfgParm(boolean bOptionSelectionMode, String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI){//, BaseDialogStateAbs<T> diagParent) {
			super(strUIId, bIgnorePrefixAndSuffix);
			this.strUIId = strUIId;
			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
			this.nodeGUI = nodeGUI;
			this.bOptionSelectionMode=bOptionSelectionMode;
//			this.diagParent=diagParent;
		}
		public void setUIId(String strUIId){
			if(this.strUIId!=null)throw new PrerequisitesNotMetException("UI Id already set",this.strUIId,strUIId);
			this.strUIId=strUIId;
			super.strId=this.strUIId;
		}
	}
	@Override
	public BaseDialogStateAbs<T> configure(ICfgParm icfg) {
		@SuppressWarnings("unchecked")
		CfgParm<T> cfg = (CfgParm<T>)icfg;
//	protected void configure(String strUIId,boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
		
		this.bOptionChoiceSelectionMode=cfg.bOptionSelectionMode;
		
//		bEnabled=cfg.bInitiallyEnabled;
//		if(!cfg.bInitiallyEnabled)requestDisable();
//		if(!cfg.bInitiallyEnabled){
		
		 /** 
		 * Dialogs must be initially disabled because they are enabled 
		 * on user demand. 
		 */
		if(!cfg.bInitiallyEnabled){
			initiallyDisabled();
			btgState.set(false,false);
		}
		
//		MouseCursor.i().configure(cfg.lMouseCursorClickDelayMilis);
		
		if(cfg.nodeGUI==null)cfg.nodeGUI=GlobalGUINodeI.i();
		super.setNodeGUI(cfg.nodeGUI);//getNodeGUI()
		
//		this.diagParent=cfg.diagParent;
//		updateModalParent();
		
		strCmdPrefix = "toggleUI";
		strCmdSuffix = "";
		
//		ConditionalStateManagerI.i().
		if(cfg.strUIId==null || cfg.strUIId.isEmpty())throw new PrerequisitesNotMetException("invalid UI identifier");
		this.strCaseInsensitiveId=cfg.strUIId;
		
//		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+cfg.strUIId;
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
		if(dle!=null)dleLastSelected = dle;
		
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
		
		return super.updateOrUndo(tpf);
	}
	
	@Override
	protected boolean enableOrUndo() {
		updateAllParts();
		
		getNodeGUI().attachChild(sptContainerMain);
		
		setMouseCursorKeepUngrabbed(true);
		if(diagParent!=null)diagParent.updateModalChild(true,this);
//		updateModalParent(true);
		
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
	public void setMouseCursorKeepUngrabbed(boolean b) {
		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,b);
	}
	
	public void setTitle(String str){
		this.strTitle = str;
	}
	
//	@Override
//	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//		return cd().getReflexFillCfg(rfcv);
//	}

	public Spatial getIntputField() {
		return sptIntputField;
	}

	protected BaseDialogStateAbs<T> setIntputField(Spatial sptIntputField) {
		this.sptIntputField = sptIntputField;
		return this;
	}

	public BaseDialogStateAbs<T> getParentDialog(){
		return this.diagParent;
	}
	
	public void setDiagParent(BaseDialogStateAbs<T> diagParent) {
		if(this.diagParent!=null)throw new PrerequisitesNotMetException("modal parente already set",this.diagParent,diagParent);
		this.diagParent=diagParent;
	}
	
//	public abstract void setAnswerFromModalChild(Object... aobj);
	
	public ArrayList<BaseDialogStateAbs<T>> getModalChildListCopy() {
		return new ArrayList<BaseDialogStateAbs<T>>(aModalChildList);
	}
	protected void updateModalChild(boolean bAdd, BaseDialogStateAbs<T> modal) {
//		if(this.modalParent==null)return;
			
		if(bAdd){
			if(!aModalChildList.contains(modal)){
				aModalChildList.add(modal);
			}
		}else{
			aModalChildList.remove(modal);
		}
	}

//	public void setModalChosenData(DialogListEntryData<T> data) {
//		this.dataFromModal = data;
//	}
	
//	/**
//	 * the answer will be null after this
//	 * @return
//	 */
//	public DialogListEntryData<T> getModalChosenDataAndClearIt(){
//		DialogListEntryData<T> data = dataFromModal;
//		dataFromModal = null;
//		return data;
//	}
	
//	public boolean isCfgDataValueSet() {
//		return dataFromModal!=null;
//	}
	
//	/**
//	 * this data will have it's value modified
//	 * @param dataToCfg
//	 */
//	protected void setDataToApplyModalChoice(DialogListEntryData<T> dataToCfg) {
//		if(!adleFullList.contains(dataToCfg))throw new PrerequisitesNotMetException("data is not on the list", dataToCfg);
//		this.dataToCfgReference=dataToCfg;
//	}
//	protected DialogListEntryData<T> getDataToApplyModalChoiceAndClearIt() {
//		DialogListEntryData<T> data = this.dataToCfgReference;
//		this.dataToCfgReference=null;
//		return data;
//	}
//	public String getCfgDataRefReport() {
//		return this.dataToCfgReference.report();
//	}
	
	public abstract void clearSelection();
	
	public boolean isOptionSelectionMode(){
		return bOptionChoiceSelectionMode;
	}
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	protected abstract void updateInputField();
	
	public abstract DialogListEntryData<T> getSelectedEntryData();
	
	/**
	 * 
	 */
	protected abstract void updateList();
	
	protected abstract void updateTextInfo();
	
	public void requestRefreshList(){
		bRequestedRefreshList=true;
	}
	
	protected String getTextInfo(){
		String str="Info: Type a list filter at input text area and hit Enter.\n";
		
		if(bOptionChoiceSelectionMode){
			str+="Option Mode: when hitting Enter, if an entry is selected, it's value will be chosen.\n";
			
			if(getParentDialog()!=null){
				if(getParentDialog().getDiagModalInfo()!=null){
					for(DialogListEntryData<T> data:getParentDialog().getDiagModalInfo().getDataReferenceAtParentListCopy()){
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
		
		if(bOptionChoiceSelectionMode){
			adataChosenEntriesList.clear();
			if(dataSelected!=null){
				adataChosenEntriesList.add(dataSelected); //TODO could be many, use a checkbox for multi-selection
//			if(getParentDialog()!=null)getParentDialog().setModalChosenData(dataSelected);
//				lChoiceMadeAtMilis=System.currentTimeMillis();
				cd().dumpInfoEntry(this.getId()+": Option Selected: "+dataSelected.toString());
				requestDisable(); //close if there is one entry selected
			}
		}else{
			if(dataSelected.isParent()){
				dataSelected.toggleExpanded();
				requestRefreshList();
			}else{
				actionCustomAtEntry(dataSelected);
			}
		}
		
	}
	
	protected abstract void actionCustomAtEntry(DialogListEntryData<T> dataSelected);

	protected void updateAllParts(){
		updateTextInfo();
		
		updateList();
		
		updateInputField();
	}
	
//	protected void setDataReferenceAtParent(DialogListEntryData<T> dataReferenceAtParent) {
//		this.dataReferenceAtParent=dataReferenceAtParent;
//	}
//
//	protected void setCommandAtParent(T cmdAtParent) {
//		this.cmdAtParent = cmdAtParent;
//	}
//	
//	protected T getCommandAtParent() {
//		return this.cmdAtParent;
//	}
	
	public ArrayList<DialogListEntryData<T>> getDataSelectionListCopy() {
		return new ArrayList<DialogListEntryData<T>>(adataChosenEntriesList);
	}
	

	protected class DiagModalInfo<T>{
		protected BaseDialogStateAbs<T>	diagChildModal;
		
		protected T	actionAtParent;
		protected ArrayList<DialogListEntryData<T>>	adataToPerformResultOfActionAtParentList;
		
		public DiagModalInfo(
			BaseDialogStateAbs<T> diagModalCurrent,
			T cmdAtParent,
			DialogListEntryData<T>... adataReferenceAtParent 
		) {
			super();
			this.diagChildModal = diagModalCurrent;
			this.adataToPerformResultOfActionAtParentList = new ArrayList<DialogListEntryData<T>>(
				Arrays.asList(adataReferenceAtParent));
			this.actionAtParent = cmdAtParent;
		}
		
		@SuppressWarnings("unchecked")
		public <S extends BaseDialogStateAbs<T>> S getDiagModal() {
			return (S)diagChildModal;
		}
//		public void setDiagModal(BaseDialogStateAbs<T> diagModal) {
//			this.diagChildModal = diagModal;
//		}
		public ArrayList<DialogListEntryData<T>> getDataReferenceAtParentListCopy() {
			return new ArrayList<DialogListEntryData<T>>(adataToPerformResultOfActionAtParentList);
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

	public void resetChoice() {
//		lChoiceMadeAtMilis=null;
		adataChosenEntriesList.clear();
	}
}
