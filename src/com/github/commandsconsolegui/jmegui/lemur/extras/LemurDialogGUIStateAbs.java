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

package com.github.commandsconsolegui.jmegui.lemur.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurMiscHelpersStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurMiscHelpersStateI.BindKey;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.list.SelectionModel;

/**
* 
* More info at {@link BaseDialogStateAbs}
*	TODO implement docking dialogs, a small icon will be created at app window edges
* 
* @author AquariusPower <https://github.com/AquariusPower>
*
*/
public abstract class LemurDialogGUIStateAbs<T> extends BaseDialogStateAbs<T> {
	protected Label	lblTitle;
	protected Label	lblTextInfo;
	protected ListBox<DialogListEntryData<T>>	lstbxEntriesToSelect;
	protected VersionedList<DialogListEntryData<T>>	vlVisibleEntriesList = new VersionedList<DialogListEntryData<T>>();
	protected int	iVisibleRows;
	protected Integer	iEntryHeightPixels; //TODO this is init is failing why? = 20; 
	protected Vector3f	v3fEntryListSize;
	protected Container	cntrEntryCfg;
	protected SelectionModel	selectionModel;
	protected BoolTogglerCmdField btgAutoScroll = new BoolTogglerCmdField(this, true).setCallNothingOnChange();
//	protected ButtonCommand	bc;
	protected boolean	bRefreshScroll;
	protected HashMap<String, LemurDialogGUIStateAbs<T>> hmModals = new HashMap<String, LemurDialogGUIStateAbs<T>>();
	protected Long	lClickActionMilis;
//	protected DialogListEntryData<T>	dataSelectRequested;
	protected Label	lblSelectedEntryStatus;
	protected ArrayList<BindKey>	abkList = new ArrayList<BindKey>();
	private KeyActionListener	actSimpleActions;
	
	@Override
	public Container getContainerMain(){
		return (Container)super.getContainerMain();
	}
	
	public static class CfgParm extends BaseDialogStateAbs.CfgParm{
		protected Float fDialogHeightPercentOfAppWindow;
		protected Float fDialogWidthPercentOfAppWindow;
		protected Float fInfoHeightPercentOfDialog;
		protected Integer iEntryHeightPixels;
//		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI) {
//			super(strUIId, bIgnorePrefixAndSuffix, nodeGUI);
//		}
		/**
		 * 
		 * @param strUIId
		 * @param bIgnorePrefixAndSuffix
		 * @param nodeGUI
		 * @param fDialogHeightPercentOfAppWindow (if null will use default) the percentual height to cover the application screen/window
		 * @param fDialogWidthPercentOfAppWindow (if null will use default) the percentual width to cover the application screen/window
		 * @param fInfoHeightPercentOfDialog (if null will use default) the percentual height to show informational text, the list and input field will properly use the remaining space
		 * @param iEntryHeightPixels
		 */
		public CfgParm(boolean	bOptionSelectionMode,String strUIId,
				Node nodeGUI, Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Integer iEntryHeightPixels)//, BaseDialogStateAbs<T> modalParent)
		{
			super(bOptionSelectionMode,strUIId, nodeGUI);//, modalParent);
			
			this.fDialogHeightPercentOfAppWindow = fDialogHeightPercentOfAppWindow;
			this.fDialogWidthPercentOfAppWindow = fDialogWidthPercentOfAppWindow;
			this.fInfoHeightPercentOfDialog = fInfoHeightPercentOfDialog;
			this.iEntryHeightPixels = iEntryHeightPixels;
		}
	}
	@Override
	public LemurDialogGUIStateAbs<T> configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
		
//		DialogMouseCursorListenerI.i().configure(null);
		
		if(cfg.fDialogHeightPercentOfAppWindow==null){
			cfg.fDialogHeightPercentOfAppWindow=0.75f;
		}
		
		if(cfg.fDialogWidthPercentOfAppWindow==null){
			cfg.fDialogWidthPercentOfAppWindow=0.75f;
		}
		
		if(cfg.fInfoHeightPercentOfDialog==null){
			cfg.fInfoHeightPercentOfDialog=0.25f;
		}
		
		super.configure(icfg);
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	public void selectAndChoseOption(DialogListEntryData<T> data){
		if(!isOptionSelectionMode())throw new PrerequisitesNotMetException("not option mode");
		if(data==null)throw new PrerequisitesNotMetException("invalid null data");
		
		selectEntry(data);
		requestActionSubmit();
	}

	public boolean isMyChild(Spatial spt){
		return getContainerMain().hasChild(spt); //this is actually recursive!!!
	}
	
	protected float sizePercOrPixels(float fSizeBase, float fPercOrPixels){
		if(Float.compare(fPercOrPixels, 1.0f)<=0){ //percent
			fSizeBase *= fPercOrPixels;
		}else{ // >1.0f is in pixels
			fSizeBase = fPercOrPixels;
		}
		
		return fSizeBase;
	}
	
	/**
	 * The input field will not require height, will be small on south edge.
	 * @param fDialogPerc the percentual width/height to cover the application screen/window 
	 * @param fInfoPerc the percentual height to show informational text, the list and input field will properly use the remaining space
	 */
	@Override
	protected boolean initGUI(){
		@SuppressWarnings("unchecked")
		CfgParm cfg = (CfgParm)getCfg();
		
		String strStyle = ConsoleLemurStateI.i().STYLE_CONSOLE; //TODO make it custom
		
		Vector3f v3fApplicationWindowSize = new Vector3f(
			app().getContext().getSettings().getWidth(),
			app().getContext().getSettings().getHeight(),
			0);
			
		setContainerMain(new Container(new BorderLayout(), strStyle));
		getContainerMain().setName(getId()+"_Dialog");
		LemurFocusHelperStateI.i().prepareDialogToBeFocused(this);
		CursorEventControl.addListenersToSpatial(getContainerMain(), DialogMouseCursorListenerI.i());
		
		Vector3f v3fDiagWindowSize = new Vector3f(v3fApplicationWindowSize);
		v3fDiagWindowSize.y = sizePercOrPixels(v3fDiagWindowSize.y,cfg.fDialogHeightPercentOfAppWindow);
		v3fDiagWindowSize.x = sizePercOrPixels(v3fDiagWindowSize.x,cfg.fDialogWidthPercentOfAppWindow);
		getContainerMain().setPreferredSize(v3fDiagWindowSize);
		
		///////////////////////// NORTH (title + info/help)
		cntrNorth = new Container(new BorderLayout(), strStyle);
		getNorthContainer().setName(getId()+"_NorthContainer");
		Vector3f v3fNorthSize = v3fDiagWindowSize.clone();
		float fInfoHeightPixels = sizePercOrPixels(v3fDiagWindowSize.y, cfg.fInfoHeightPercentOfDialog);
		v3fNorthSize.y = fInfoHeightPixels;
		getNorthContainer().setPreferredSize(v3fNorthSize);
		
		//title 
		lblTitle = new Label(strTitle,strStyle);
		lblTitle.setName(getId()+"_Title");
		ColorRGBA cLightGreen = new ColorRGBA(0.35f,1f,0.35f,1f);
		lblTitle.setColor(cLightGreen); //TODO make it custom
		getNorthContainer().addChild(lblTitle, BorderLayout.Position.North);
//		CursorEventControl.addListenersToSpatial(lblTitle, DialogMouseCursorListenerI.i());
		
		// simple info
		lblTextInfo = new Label("",strStyle);
		lblTextInfo.setName(getId()+"_TxtInfo");
		getNorthContainer().addChild(lblTextInfo, BorderLayout.Position.Center);
		
		getContainerMain().addChild(getNorthContainer(), BorderLayout.Position.North);
		
		//////////////////////////// CENTER (list)
		// list
		v3fEntryListSize = v3fDiagWindowSize.clone();
//		float fListPerc = 1.0f - cfg.fInfoHeightPercentOfDialog;
//		v3fEntryListSize.y *= fListPerc;
		v3fEntryListSize.y -= fInfoHeightPixels;
		lstbxEntriesToSelect = new ListBox<DialogListEntryData<T>>(
			new VersionedList<DialogListEntryData<T>>(), 
			new CellRendererDialogEntry<T>(strStyle,this),// bOptionSelectionMode), 
			strStyle);
		selectionModel = lstbxEntriesToSelect.getSelectionModel();
		lstbxEntriesToSelect.setName(getId()+"_EntriesList");
		lstbxEntriesToSelect.setSize(v3fEntryListSize); //not preferred, so the input field can fit properly
		//TODO multi was not implemented yet... lstbxVoucherListBox.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		getContainerMain().addChild(lstbxEntriesToSelect, BorderLayout.Position.Center);
		
//		vlstrEntriesList.add("(Empty list)");
		lstbxEntriesToSelect.setModel((VersionedList<DialogListEntryData<T>>)vlVisibleEntriesList);
		
		iEntryHeightPixels = cfg.iEntryHeightPixels;
		
		//////////////////////////////// SOUTH (typing/config)
		cntrSouth = new Container(new BorderLayout(), strStyle);
		getSouthContainer().setName(getId()+"_SouthContainer");
		
//		// configure an entry from the list
//		cntrEntryCfg = new Container(new BorderLayout(), strStyle);
//		cntrEntryCfg.setName(getId()+"_EntryConfig");
//		getSouthContainer().addChild(cntrEntryCfg, Bor)
		
		// status line, about the currently selected entry on the list
		lblSelectedEntryStatus = new Label("Selected Entry Status",strStyle);
		getSouthContainer().addChild(lblSelectedEntryStatus, BorderLayout.Position.North);
		
		// mainly used as a list filter
		setIntputField(new TextField("",strStyle));
		getInputField().setName(getId()+"_InputField");
		LemurFocusHelperStateI.i().addFocusChangeListener(getInputField());
		getSouthContainer().addChild(getInputField(),BorderLayout.Position.South);
		
		getContainerMain().addChild(getSouthContainer(), BorderLayout.Position.South);
		
		Vector3f v3fPos = new Vector3f(
			(v3fApplicationWindowSize.x-v3fDiagWindowSize.x)/2f,
			(v3fApplicationWindowSize.y-v3fDiagWindowSize.y)/2f+v3fDiagWindowSize.y,
			0
		);
		getContainerMain().setLocalTranslation(v3fPos);
		
		getNodeGUI().attachChild(getContainerMain());
		
		return true;
	}
	
	protected Container getNorthContainer() {
		return (Container)cntrNorth;
	}
	
	protected Container getSouthContainer() {
		return (Container)cntrSouth;
	}
	
//	@Override
//	public void requestFocus(Spatial spt) {
//		LemurFocusHelperStateI.i().requestFocus(spt);
//	}
	
	@Override
	protected boolean enableOrUndo() {
		if(!super.enableOrUndo())return false;
		
		LemurFocusHelperStateI.i().requestFocus(getInputField());
		
		return true;
	}
	
	@Override
	protected boolean disableOrUndo() {
		if(!super.disableOrUndo())return false;
		
//		if(getInputField().equals(LemurFocusHelperStateI.i().getFocused())){
			LemurFocusHelperStateI.i().removeFocusableFromList(getInputField());
//		}
		
		return true;
	}
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	@Override
	protected void updateInputField(){
		getInputField().setText(strLastFilter);
	}
	
	protected TextField getInputField(){
		return (TextField)getIntputField();
	}
	
	@Override
	public DialogListEntryData<T> getSelectedEntryData() {
		Integer iSel = selectionModel.getSelection();
		if(iSel==null)return null;
		return	vlVisibleEntriesList.get(iSel);
	}
	
	protected void updateEntryHeight(){
		//TODO use font height? or button height?
		iEntryHeightPixels = 20; //blind placeholder
	}
	
	/**
	 * override ex. to avoid reseting
	 */
	protected void resetList(){
		vlVisibleEntriesList.clear();
		clearSelection();
//		selectionModel.setSelection(-1);
	}
	
	///**
	//* call {@link #updateList(ArrayList)} from the method overriding this
	//*/
	
	@Override
	protected void updateList(){
//		updateList(adleCompleteEntriesList);
		DialogListEntryData<T> dledLastSelectedBkp = dleLastSelected; 
		
		resetList();
		
		sortEntries();
		
		for(DialogListEntryData<T> dled:adleCompleteEntriesList){
			if(!strLastFilter.isEmpty()){
				if(dled.getText().toLowerCase().contains(strLastFilter)){
					vlVisibleEntriesList.add(dled);
				}
			}else{
				if(dled.getParent()==null){
					vlVisibleEntriesList.add(dled); //root entries
				}else{
					if(checkAllParentTreesExpanded(dled)){
						vlVisibleEntriesList.add(dled);
					}
				}
			}
		}
		
		updateSelected(dledLastSelectedBkp);
		
		// update visible rows
		updateEntryHeight();
		iVisibleRows = (int) (v3fEntryListSize.y/iEntryHeightPixels);
		lstbxEntriesToSelect.setVisibleItems(iVisibleRows);
		if(vlVisibleEntriesList.size()>0){
			if(getSelectedEntryData()==null){
				selectRelativeEntry(0);
			}
		}
	}
	
//	/**
//	 * basic functionality
//	 * 
//	 * @param aValueList
//	 */
//	protected void updateList(ArrayList<DialogListEntryData<T>> adle){
//	}
	
	/**
	 * for entry visibility
	 * @param dled
	 * @return
	 */
	protected boolean checkAllParentTreesExpanded(DialogListEntryData<T> dled){
		DialogListEntryData<T> dledParent = dled.getParent();
		while(dledParent!=null){
			if(!dledParent.isTreeExpanded())return false;
			dledParent = dledParent.getParent();
		}
		return true;
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
		
		for(DialogListEntryData<T> dle:new ArrayList<DialogListEntryData<T>>(adleTmp)){
			if(dle.getParent()==null){ //root ones
				recursiveAddEntries(dle);
			}
		}
	}
	
	protected void addEntry(DialogListEntryData<T> dle) {
		if(dle==null)throw new PrerequisitesNotMetException("cant be null!");
		adleCompleteEntriesList.add(dle);
	}

	/**
	 * 
	 * @return -1 if none
	 */
	public int getSelectedIndex(){
		return vlVisibleEntriesList.indexOf(dleLastSelected);
	}
	
	protected void autoScroll(){
		if(!btgAutoScroll.b())return;
		
		Integer iSelected = getSelectedIndex();
		if(iSelected!=null){ //TODO this is buggy...
			int iTopEntryIndex = getTopEntryIndex();
			int iBottomItemIndex = getBottomEntryIndex();
			Integer iScrollTo=null;
			
			if(iSelected>=iBottomItemIndex){
				iScrollTo=iSelected-iBottomItemIndex+iTopEntryIndex;
			}else
			if(iSelected<=iTopEntryIndex){
				iScrollTo=iSelected-1;
			}
			
			if(iScrollTo!=null){
				scrollTo(iScrollTo);
			}
		}
		
		bRefreshScroll=false;
	}
	
	@Override
	protected boolean updateOrUndo(float tpf) {
		if(!super.updateOrUndo(tpf))return false;
		
		if(bRefreshScroll)autoScroll();
		
//		updateSelectEntryRequested();
		
		if(!getInputText().equalsIgnoreCase(super.strLastFilter)){
			super.strLastFilter=getInputText();
			applyListKeyFilter();
			updateList();
		}
		
		return true;
	}
	
	/**
	 * default is the class name, will look like the dialog title
	 */
	@Override
	protected void updateTextInfo(){
//		lblTextInfo.setText("DIALOG for "+this.getClass().getSimpleName());
		lblTextInfo.setText(getTextInfo());
	}
	
	@Override
	public void setTitle(String str) {
		super.setTitle(str);
		lblTitle.setText(str);
	}
	
	/**
	 * 
	 * @return max-1 (if total 1, max index 0)
	 */
	protected int getMaxIndex(){
//		return lstbxEntriesToSelect.getVisibleItems()
		return vlVisibleEntriesList.size()-1;
//			+( ((int)lstbxEntriesToSelect.getSlider().getModel().getMaximum()) -1);
	}
	
	protected int getTopEntryIndex(){
		int iVisibleItems = lstbxEntriesToSelect.getVisibleItems();
		int iTotEntries = vlVisibleEntriesList.size();
		if(iVisibleItems>iTotEntries){
			return 0; //is not overflowing the max visible items amount
		}
		
		int iSliderInvertedIndex=(int)lstbxEntriesToSelect.getSlider().getModel().getValue();
		int iTopEntryIndex = (int)(iTotEntries -iSliderInvertedIndex -iVisibleItems);
		
		return iTopEntryIndex;
	}
	
	protected int getBottomEntryIndex(){
		return getTopEntryIndex()+iVisibleRows-1;
	}
	
	protected void scrollTo(int iIndex){
		//lstbxEntriesToSelect.getSlider().getModel().getValue();
//		lstbxEntriesToSelect.getSlider().getModel().setValue(getMaxIndex()-iIndex);
		lstbxEntriesToSelect.getSlider().getModel().setValue(
			vlVisibleEntriesList.size()-lstbxEntriesToSelect.getVisibleItems()-iIndex);
		
	}
	
	@Override
	protected boolean initKeyMappings(){
		actSimpleActions = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
	//		boolean bShift = key.hasModifier(0x01);
				
				switch(key.getKeyCode()){
					case KeyInput.KEY_ESCAPE:
						setEnabledRequest(false);
						break;
					case KeyInput.KEY_UP:
							selectRelativeEntry(-1);
						break;
					case KeyInput.KEY_DOWN:
							selectRelativeEntry(1);
						break;
					case KeyInput.KEY_PGUP:
						selectRelativeEntry(-lstbxEntriesToSelect.getVisibleItems());
						break;
					case KeyInput.KEY_PGDN:
						selectRelativeEntry(lstbxEntriesToSelect.getVisibleItems());
						break;
					case KeyInput.KEY_HOME:
						selectRelativeEntry(-vlVisibleEntriesList.size()); //uses underflow protection
//						if(bControl)selectEntry(vlEntriesList.get(0));
						break;
					case KeyInput.KEY_END:
						selectRelativeEntry(vlVisibleEntriesList.size()); //uses overflow protection
//						if(bControl)selectEntry(vlEntriesList.get(vlEntriesList.size()-1));
						break;
					case KeyInput.KEY_DELETE:
						if(bControl)getInputField().setText("");
						break;
					case KeyInput.KEY_NUMPADENTER:
					case KeyInput.KEY_RETURN:
						actionSubmit();
						break;
					case KeyInput.KEY_V: 
						if(bControl){
							LemurMiscHelpersStateI.i().insertTextAtCaratPosition(getInputField(),
								MiscI.i().retrieveClipboardString(true));
//							getInputField().setText(getInputField().getText()
//								+MiscI.i().retrieveClipboardString(true));
						}
						break;
				}
			}
	
		};
		
		bindKey("Navigate list to first entry", KeyInput.KEY_HOME, KeyAction.CONTROL_DOWN);
		bindKey("Navigate list to last entry", KeyInput.KEY_END,KeyAction.CONTROL_DOWN);
		bindKey("Navigate list to previous page", KeyInput.KEY_PGUP);
		bindKey("Navigate list to next page", KeyInput.KEY_PGDN);
		bindKey("Navigate list to previous entry", KeyInput.KEY_UP);
		bindKey("Navigate list to next entry", KeyInput.KEY_DOWN);
		
		bindKey("Clear the input field text", KeyInput.KEY_DELETE,KeyAction.CONTROL_DOWN);
		
		bindKey("Accept entry choice (on choice dialogs only)", KeyInput.KEY_RETURN);
		bindKey("Accept entry choice (on choice dialogs only)", KeyInput.KEY_NUMPADENTER);
		
		bindKey("Close dialog", KeyInput.KEY_ESCAPE);
		
		return true;
	}
	
	protected BindKey bindKey(String strActionPerformedHelp, int iKeyCode, int... aiKeyModifiers){
		BindKey bk = LemurMiscHelpersStateI.i().bindKey(getInputField(), actSimpleActions,
			strActionPerformedHelp, iKeyCode, aiKeyModifiers);
		abkList.add(bk);
		return bk;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,"showDialogKeyBinds"+getId(),null,"")){
//			cc.dumpSubEntry("ESC - close");
//			cc.dumpSubEntry("Up/Down/PgUp/PgDn/Ctrl+Home|End - nav. list entry");
//			cc.dumpSubEntry("Enter - accept selected choice at config dialog");
			cc.dumpSubEntry("DoubleClick - open config/accept choice at config dialog");
			
			if(abkList.size()==0){
				cc.dumpWarnEntry("open the dialog first to let keys be bound");
			}else{
				for(BindKey bk:abkList){
					cc.dumpSubEntry(bk.getHelp());
				}
			}
			
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cc);
//			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public String getInputText() {
		return getInputField().getText();
	}
	
	@SuppressWarnings("unchecked")
	public <S extends LemurDialogGUIStateAbs<T>> S addModalDialog(LemurDialogGUIStateAbs<T> diagModal){
		diagModal.setDiagParent(this);
		hmModals.put(diagModal.getId(),diagModal);
		return (S)this;
	}
	
	public DiagModalInfo<T> getDiagModalCurrent(){
		return dmi;
	}
	
//	public void openModalDialog(String strDialogId, DialogListEntryData<T> dataToAssignModalTo, T cmd){
	public void openModalDialog(String strDialogId, DialogListEntryData<T> dataToAssignModalTo, T cmd){
		LemurDialogGUIStateAbs<T> diagModalCurrent = hmModals.get(strDialogId);
		if(diagModalCurrent!=null){
			dmi = new DiagModalInfo(diagModalCurrent,cmd,dataToAssignModalTo);
			diagModalCurrent.requestEnable();
		}else{
			throw new PrerequisitesNotMetException("no dialog set for id: "+strDialogId);
		}
	}
	
	public void applyResultsFromModalDialog(){
		if(dmi==null)throw new PrerequisitesNotMetException("no modal active");
		
		dmi.getDiagModal().resetChoice();
//		dmi.getDiagModal().adataSelectedEntriesList.clear();
		dmi = null;
	}
	
	public boolean isListBoxEntry(Spatial spt){
		if(getSelectedIndex()>=0){
			boolean bForce=true;
			if(bForce){
				return true;
			}else{
				if(spt instanceof Panel){ //TODO the capture is actually the dialog container, can it be the listbox entry Panel?
					//TODO check if it is correcly at the ListBox
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void clearSelection() {
		selectionModel.setSelection(-1); //clear selection
//		setSelectedEntryIndex(-1);
	}
	public void setSelectedEntryIndex(int i){
		if(i<0)i=0;
		if(i>getMaxIndex())i=getMaxIndex();
		selectionModel.setSelection(i);
//		dleLastSelected = vlEntriesList.get(i);
		
		DialogListEntryData<T> dled = vlVisibleEntriesList.get(i);
		DialogListEntryData<T> dledParent = dled.getParent();
		lblSelectedEntryStatus.setText(""
			+"i="+i+", "
			+"uid="+dled.getUId()+", "
			+"puid="+(dledParent==null?"(ROOT)":dledParent.getUId())+", "
			+"'"+dled.getText()+"'"
		);
	}
	public void selectEntry(DialogListEntryData<T> dledSelectRequested) {
//		this.dataSelectRequested = data;
//	}
//	
//	public void updateSelectEntryRequested() {
//		if(dataSelectRequested==null)return;
//		
		int i=vlVisibleEntriesList.indexOf(dledSelectRequested);
		if(i>=0){
			setSelectedEntryIndex(i);
//			selectionModel.setSelection(i);
//			dataSelectRequested=null;
			cd().dumpDebugEntry(getId()+",SelectIndex="+i+","+dledSelectRequested.toString());
		}else{
			throw new PrerequisitesNotMetException("data not present on the list", dledSelectRequested, lstbxEntriesToSelect);
		}
	}
	
	/**
	 * 
	 * @param iAddIndex (down), can be negative (up)
	 * @return
	 */
	public Integer selectRelativeEntry(int iAddIndex){
		Integer iSel = selectionModel.getSelection();
		
		int iMaxIndex=getMaxIndex();
		if(iMaxIndex<0)return null;
		
		if(iSel==null)iSel=0;
		
		iSel+=iAddIndex;
		
		if(iSel<0)iSel=0;
		if(iSel>iMaxIndex){
			iSel=iMaxIndex;
		}
//		
//			if(iMax>0){
//				iSel=0;
//			}else{
//				iSel=-1;
//			}
//		}
		
		setSelectedEntryIndex(iSel);
//		selectionModel.setSelection(iSel);
		bRefreshScroll=true;
		
//		iSel = selectionModel.getSelection();
		cd().dumpDebugEntry(getId()+":"
			+"SelectedEntry="+iSel+","
			+"SliderValue="+MiscI.i().fmtFloat(lstbxEntriesToSelect.getSlider().getModel().getValue()));
		return iSel;
//		return iSel==null?-1:iSel;
	}

	public abstract boolean execTextDoubleClickActionFor(DialogListEntryData<T> dled);

	public abstract boolean execActionFor(EMouseCursorButton e, Spatial capture);

	public void removeEntry(DialogListEntryData<T> dled){
//		int iDataAboveIndex = -1;
		DialogListEntryData<T> dledAboveTmp = null;
		if(getSelectedEntryData().equals(dled)){
//			iDataAboveIndex = adleCompleteEntriesList.indexOf(data)-1;
//			if(iDataAboveIndex>=0)dataAboveTmp = adleCompleteEntriesList.get(iDataAboveIndex);
			dledAboveTmp = getAbove(dled);
		}
		DialogListEntryData<T> dledParentTmp = dled.getParent();
		
		for(DialogListEntryData<T> dledChild:dled.getChildrenCopy()){
			removeEntry(dledChild);
		}
		
		if(!adleCompleteEntriesList.remove(dled)){
			throw new PrerequisitesNotMetException("missing data at list", dled);
		}
		
		dled.setParent(null);
		
		if(dledAboveTmp!=null){
			updateSelected(dledAboveTmp,dledParentTmp);
		}
		
		requestRefreshList();
	}
	
	protected DialogListEntryData<T> getAbove(DialogListEntryData<T> dled){
		int iDataAboveIndex = adleCompleteEntriesList.indexOf(dled)-1;
		if(iDataAboveIndex>=0){
			return adleCompleteEntriesList.get(iDataAboveIndex);
		}
		return null;
	}
	protected void updateSelected(DialogListEntryData<T> dledPreviouslySelected){
		if(dledPreviouslySelected==null)return;
		
		int i = vlVisibleEntriesList.indexOf(dledPreviouslySelected);
		if(i>=0){
			setSelectedEntryIndex(i);//selectionModel.setSelection(i);
		}else{
			updateSelected(getAbove(dledPreviouslySelected), dledPreviouslySelected.getParent());
		}
	}
	protected void updateSelected(final DialogListEntryData<T> dledAbove, final DialogListEntryData<T> dledParentTmp){
		/**
		 * need to wait it actually get selected
		 */
		CallQueueI.i().appendCall(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				DialogListEntryData<T> dledParent = dledParentTmp;
				
				if(vlVisibleEntriesList.contains(dledAbove)){
					if(dledAbove.equals(dledParent)){
						if(!dledParent.isTreeExpanded()){ //was collapsed
							selectEntry(dledParent);
							return true;
						}
					}
					
					if(getSelectedEntryData().equals(dledAbove)){
						/**
						 * select the below one
						 * no problem if it was at the end of the list
						 */
						selectRelativeEntry(+1);
						return true;
					}
					
					selectEntry(dledAbove); //prepare to retry
					
					return false; //will retry
				}else{ //use parent
					while(true){
						if(dledParent==null)break;
						
						if(vlVisibleEntriesList.contains(dledParent)){
							/**
							 * useful when collapsing a tree branch
							 */
							selectEntry(dledParent);
							break;
						}
						
						dledParent = dledParent.getParent();
					}
					
					return true;
				}
			}
		});
	}
	
}
