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

package com.github.commandsconsolegui.jmegui.lemur.extras;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.MiscLemurHelpersStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.MiscLemurHelpersStateI.BindKey;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GridPanel;
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
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.list.SelectionModel;

/**
* 
* More info at {@link BaseDialogStateAbs}
*	TODO implement docking dialogs, a small icon will be created at app window edges
* 
* TODO migrate from {@link BaseDialogStateAbs} to here, everything that is not usable at {@link ConsoleLemurStateI}
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
public abstract class LemurDialogGUIStateAbs<T,R extends LemurDialogGUIStateAbs<T,R>> extends BaseDialogStateAbs<T,R> {
	private Label	lblTitle;
	private Label	lblTextInfo;
//	private ListBox<DialogListEntryData<T>>	lstbxEntriesToSelect;
	private VersionedList<DialogListEntryData<T>>	vlVisibleEntriesList = new VersionedList<DialogListEntryData<T>>();
	private int	iVisibleRows;
//	private Integer	iEntryHeightPixels; //TODO this is init is failing why? = 20; 
	private Vector3f	v3fEntryListSize;
	private Container	cntrEntryCfg;
	private SelectionModel	selectionModel;
	private BoolTogglerCmdField btgAutoScroll = new BoolTogglerCmdField(this, true).setCallNothingOnChange();
//	private ButtonCommand	bc;
	private boolean	bRefreshScroll;
	private HashMap<String, LemurDialogGUIStateAbs<T,?>> hmModals = new HashMap<String, LemurDialogGUIStateAbs<T,?>>();
	private Long	lClickActionMilis;
//	private DialogListEntryData<T>	dataSelectRequested;
	private Label	lblSelectedEntryStatus;
	private ArrayList<BindKey>	abkList = new ArrayList<BindKey>();
	private KeyActionListener	actSimpleActions;
	private TimedDelayVarField tdListboxSelectorAreaBlinkFade = new TimedDelayVarField(1f,"");
	private CellRendererDialogEntry<T>	cr;
//	private StringVarField svfStyle = new StringVarField(this, null, null);
//	private String strStyle;
	private BoolTogglerCmdField btgEffectListEntries = new BoolTogglerCmdField(this, true);
//	private TimedDelayVarField tdEffectListEachEntry = new TimedDelayVarField(this, 0.05f, "");
//	private float fEffectListEntryDelay=0.05f;
	private FloatDoubleVarField fdvEffectListEntryDelay = new FloatDoubleVarField(this, 0.15f, "");
	
//	public abstract T getCmdDummy();
	
	@Override
	public Container getContainerMain(){
		return (Container)super.getContainerMain();
	}
	
	private FloatDoubleVarField fdvEntryHeightMultiplier = new FloatDoubleVarField(this,1f,"");
	
	public static class CfgParm extends BaseDialogStateAbs.CfgParm{
		private Float fDialogHeightPercentOfAppWindow;
		private Float fDialogWidthPercentOfAppWindow;
		private Float fInfoHeightPercentOfDialog;
		
//		private Integer iEntryHeightPixels;
		private Float fEntryHeightMultiplier = 1.0f;
		
//		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI) {
//			super(strUIId, bIgnorePrefixAndSuffix, nodeGUI);
//		}
		/**
		 * 
		 * @param strUIId
		 * @param bIgnorePrefixAndSuffix
		 * @param fDialogHeightPercentOfAppWindow (if null will use default) the percentual height to cover the application screen/window
		 * @param fDialogWidthPercentOfAppWindow (if null will use default) the percentual width to cover the application screen/window
		 * @param fInfoHeightPercentOfDialog (if null will use default) the percentual height to show informational text, the list and input field will properly use the remaining space
		 */
		public CfgParm(String strUIId,
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Float fEntryHeightMultiplier)//, BaseDialogStateAbs<T> modalParent)
		{
			super(strUIId);//, nodeGUI);//, modalParent);
			
			this.fDialogHeightPercentOfAppWindow = fDialogHeightPercentOfAppWindow;
			this.fDialogWidthPercentOfAppWindow = fDialogWidthPercentOfAppWindow;
			this.fInfoHeightPercentOfDialog = fInfoHeightPercentOfDialog;
			if(fEntryHeightMultiplier!=null)this.fEntryHeightMultiplier = fEntryHeightMultiplier;
		}
	}
	private CfgParm	cfg;
	private boolean	bRunningEffectAtAllListEntries;
	private float	fMinScale = 0.01f;
	private boolean	bPreparedForListEntriesEffects;
	private Integer	iFinalEntryHeightPixels;
	private Quaternion	quaBkpMain;
	@Override
	public R configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
		fdvEntryHeightMultiplier.setObjectRawValue(cfg.fEntryHeightMultiplier);
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
		
		super.configure(cfg);
		
		return storeCfgAndReturnSelf(cfg);
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
	
	private float sizePercOrPixels(float fSizeBase, float fPercOrPixels){
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
		if(!super.initGUI())return false;
//		if(getStyle()==null){
//			setStyle(ConsoleLemurStateI.i().STYLE_CONSOLE);
//		}
		
		Vector3f v3fApplicationWindowSize = new Vector3f(
			app().getContext().getSettings().getWidth(),
			app().getContext().getSettings().getHeight(),
			0);
			
		setContainerMain(new Container(new BorderLayout(), getStyle()));
		quaBkpMain = getContainerMain().getLocalRotation().clone();
		getContainerMain().setName(getId()+"_Dialog");
		LemurFocusHelperStateI.i().prepareDialogToBeFocused(this);
		CursorEventControl.addListenersToSpatial(getContainerMain(), DialogMouseCursorListenerI.i());
		
		Vector3f v3fDiagWindowSize = new Vector3f(v3fApplicationWindowSize);
		v3fDiagWindowSize.y = sizePercOrPixels(v3fDiagWindowSize.y,cfg.fDialogHeightPercentOfAppWindow);
		v3fDiagWindowSize.x = sizePercOrPixels(v3fDiagWindowSize.x,cfg.fDialogWidthPercentOfAppWindow);
		MiscLemurHelpersStateI.i().setGrantedSize(getContainerMain(), v3fDiagWindowSize, false);
		
		///////////////////////// NORTH (title + info/help)
		setCntrNorth(new Container(new BorderLayout(), getStyle()));
		getNorthContainer().setName(getId()+"_NorthContainer");
		Vector3f v3fNorthSize = v3fDiagWindowSize.clone();
		/**
		 * TODO info height should be automatic. Or Info should be a list with vertical scroll bar, and constrainted to >= 1 lines.
		 */
		float fInfoHeightPixels = sizePercOrPixels(v3fDiagWindowSize.y, cfg.fInfoHeightPercentOfDialog);
		v3fNorthSize.y = fInfoHeightPixels;
		MiscLemurHelpersStateI.i().setGrantedSize(getNorthContainer(), v3fNorthSize, false);
		
		//title 
//		Container cntrTitleBox = new Container(new BorderLayout(), getStyle());
//		cntrTitleBox.setName(getId()+"_TitleBox");
//		cntrTitleBox.addChild(lblTitle, BorderLayout.Position.Center);
		
		lblTitle = new Label(getTitle(),getStyle());
		lblTitle.setName(getId()+"_Title");
		ColorRGBA cLightGreen = new ColorRGBA(0.35f,1f,0.35f,1f);
		lblTitle.setColor(cLightGreen); //TODO make it custom
		getNorthContainer().addChild(lblTitle, BorderLayout.Position.North);
		
//		CursorEventControl.addListenersToSpatial(lblTitle, DialogMouseCursorListenerI.i());
		
		// simple info
		lblTextInfo = new Label("",getStyle());
		lblTextInfo.setName(getId()+"_TxtInfo");
		MiscLemurHelpersStateI.i().lineWrapDisableFor(lblTextInfo);
		getNorthContainer().addChild(lblTextInfo, BorderLayout.Position.Center);
		
		getContainerMain().addChild(getNorthContainer(), BorderLayout.Position.North);
		
		//////////////////////////// CENTER (list)
		// list
		v3fEntryListSize = v3fDiagWindowSize.clone();
//		float fListPerc = 1.0f - cfg.fInfoHeightPercentOfDialog;
//		v3fEntryListSize.y *= fListPerc;
		v3fEntryListSize.y -= fInfoHeightPixels;
		setMainList(new ListBox<DialogListEntryData<T>>(
			new VersionedList<DialogListEntryData<T>>(), 
			getCellRenderer(), 
			getStyle()));
		selectionModel = getMainList().getSelectionModel();
		getMainList().setName(getId()+"_EntriesList");
		getMainList().setSize(v3fEntryListSize); //not preferred, so the input field can fit properly
		//TODO multi was not implemented yet... lstbxVoucherListBox.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		getContainerMain().addChild(getMainList(), BorderLayout.Position.Center);
		
//		vlstrEntriesList.add("(Empty list)");
		getMainList().setModel((VersionedList<DialogListEntryData<T>>)vlVisibleEntriesList);
		
//		LemurMiscHelpersStateI.i().bugFix(null, LemurMiscHelpersStateI.i().btgBugFixListBoxSelectorArea, getListEntries());
		
//		/**
//		 * TODO entry height should be automatic... may be each entry could have its own height.
//		 */
//		iEntryHeightPixels = cfg.iEntryHeightPixels;
		
		//////////////////////////////// SOUTH (typing/config)
		setCntrSouth(new Container(new BorderLayout(), getStyle()));
		getSouthContainer().setName(getId()+"_SouthContainer");
		
//		// configure an entry from the list
//		cntrEntryCfg = new Container(new BorderLayout(), getStyle());
//		cntrEntryCfg.setName(getId()+"_EntryConfig");
//		getSouthContainer().addChild(cntrEntryCfg, Bor)
		
		// status line, about the currently selected entry on the list
		lblSelectedEntryStatus = new Label("Selected Entry Status",getStyle());
		MiscLemurHelpersStateI.i().lineWrapDisableFor(lblSelectedEntryStatus);
		getSouthContainer().addChild(lblSelectedEntryStatus, BorderLayout.Position.North);
		
		// mainly used as a list filter
		setInputField(new TextField("",getStyle()));
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
	
	@Override
	protected ListBox<DialogListEntryData<T>> getMainList() {
		return (ListBox<DialogListEntryData<T>>)super.getMainList();
	}
	
	protected CellRendererDialogEntry<T> getCellRenderer(){
		if(cr==null)cr = new CellRendererDialogEntry<T>(getStyle(),this);// bOptionSelectionMode),
		return cr;
	}
	
//	@Override
//	public void requestFocus(Spatial spt) {
//		LemurFocusHelperStateI.i().requestFocus(spt);
//	}
	
	@Override
	protected boolean enableAttempt() {
		if(!super.enableAttempt())return false;
		
		LemurFocusHelperStateI.i().requestFocus(getInputField());
		
		bPreparedForListEntriesEffects=false;
//		prepareEffectListEntries(false);
		
		return true;
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!super.disableAttempt())return false;
		
//		if(getInputField().equals(LemurFocusHelperStateI.i().getFocused())){
			LemurFocusHelperStateI.i().removeFocusableFromList(getInputField());
//		}
			
			// this is to prepare for the next enable
//			prepareEffectListEntries(false);
			
//		bPreparedForListEntriesEffects=false;
		
		return true;
	}
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	@Override
	protected void updateInputField(){
		if(isInputToUserEnterCustomValueMode()){
			if(getInputText().isEmpty() || getInputText().equals(getUserEnterCustomValueToken())){
				applyDefaultValueToUserModify();
			}
		}else{
			getInputField().setText(getLastFilter());
		}
	}
	
//	@Override
//	protected void enableSuccess() {
//		super.enableSuccess();
//		updateInputField();
//	}
	
	@Override
	protected TextField getInputField(){
		return (TextField)super.getInputField();
	}
	
	@Override
	public DialogListEntryData<T> getSelectedEntryData() {
		Integer iSel = selectionModel.getSelection();
		if(iSel==null)return null;
		return	vlVisibleEntriesList.get(iSel);
	}
	
	private void updateFinalEntryHeightPixels(){
//		if(cfg.iEntryHeightPixels==null){
			this.iFinalEntryHeightPixels = (int)FastMath.ceil(getEntryHeightPixels() 
//				* cfg.fEntryHeightMultiplier);
				* fdvEntryHeightMultiplier.f());
//		}
	}
	
	private GridModel<Panel> getListBoxGridPanelModel(){
		return getMainList().getGridPanel().getModel();
	}
	
	protected Integer getEntryHeightPixels(){
		// query for an entry from the list
		//if(vlVisibleEntriesList.size()==0)return null;
		
		// create a new cell
		GridModel<Panel> gm = getListBoxGridPanelModel();
		Panel pnl = gm.getCell(0, 0, null);
		float fHeight = pnl.getPreferredSize().getY();
		// a simple value would be: MiscJmeI.i().retrieveBitmapTextFor(new Button("W")).getLineHeight()
		
		return (int)FastMath.ceil(fHeight);
	}
	
	/**
	 * override ex. to avoid reseting
	 */
	private void resetList(){
		vlVisibleEntriesList.clear();
		clearSelection();
//		selectionModel.setSelection(-1);
	}
	
	///**
	//* call {@link #updateList(ArrayList)} from the method overriding this
	//*/
	
	private void addWithParents(DialogListEntryData<T> dled){
		if(vlVisibleEntriesList.contains(dled))return;
		
		vlVisibleEntriesList.add(dled);
		
		DialogListEntryData<T> dledParent = dled.getParent();
		while(dledParent!=null){
			if(!vlVisibleEntriesList.contains(dledParent)){
				vlVisibleEntriesList.add(vlVisibleEntriesList.indexOf(dled),dledParent);
			}
			dled=dledParent;
			dledParent=dledParent.getParent();
		}
	}
	
	@Override
	protected void updateList(){
//		updateList(adleCompleteEntriesList);
		DialogListEntryData<T> dledLastSelectedBkp = getLastSelected(); 
		
		resetList();
		
		prepareTree();
		
		for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
			if(!getLastFilter().isEmpty()){
				if(dled.getVisibleText().toLowerCase().contains(getLastFilter())){
					addWithParents(dled);
//					vlVisibleEntriesList.add(dled);
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
		
		/**
		 * update visible rows
		 * 
		 * if there are too many rows, they will be shrinked...
		 * so this grants they have a good height.
		 * 
		 * TODO sum each individual top entries height? considering they could have diff heights of course
		 */
		if(getListBoxGridPanelModel().getRowCount()>0){
			updateFinalEntryHeightPixels();
			
			iVisibleRows = (int) (v3fEntryListSize.y/getFinalEntryHeightPixels());
			getMainList().setVisibleItems(iVisibleRows);
			if(vlVisibleEntriesList.size()>0){
				if(getSelectedEntryData()==null){
					selectRelativeEntry(0);
				}
			}
		}
	}
	
//	/**
//	 * basic functionality
//	 * 
//	 * @param aValueList
//	 */
//	private void updateList(ArrayList<DialogListEntryData<T>> adle){
//	}
	
	/**
	 * for entry visibility
	 * @param dled
	 * @return
	 */
	private boolean checkAllParentTreesExpanded(DialogListEntryData<T> dled){
		DialogListEntryData<T> dledParent = dled.getParent();
		while(dledParent!=null){
			if(!dledParent.isTreeExpanded())return false;
			dledParent = dledParent.getParent();
		}
		return true;
	}
	
	/**
	 * 
	 * @return -1 if none
	 */
	public int getSelectedIndex(){
		return vlVisibleEntriesList.indexOf(getLastSelected());
	}
	
	private void autoScroll(){
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
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		if(bRefreshScroll)autoScroll();
		
//		updateSelectEntryRequested();
		
		if(!getInputText().startsWith(getUserEnterCustomValueToken())){
			if(!getInputText().equalsIgnoreCase(getLastFilter())){
	//			setLastFilter(getInputText());
				applyListKeyFilter();
				updateList();
			}
		}
		
		updateEffectListEntries(isTryingToEnable());
//		if(isTryingToEnable()){
//			if(isEffectsDone()){ // play list effect after main one completes
////				if(btgEffectListEntries.b()){
////					if(!tdEffectListEachEntry.isActive()){
////						if(!bEffectListAllEntriesCompleted){
////							tdEffectListEachEntry.setActive(true);
////						}
////					}
////				}
//		
////				if(tdEffectListEachEntry.isActive()){ //dont use btgEffectListEntries.b() as it may be disabled during the grow effect
//				if(bRunningEffectAtAllListEntries){
//					updateEffectListEntries(isTryingToEnable());
//				}
//			}
//		}
		
		MiscLemurHelpersStateI.i().updateBlinkListBoxSelector(getMainList());//,true);
		
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
	public R setTitle(String str) {
		super.setTitle(str);
		lblTitle.setText(str);
		return getThis();
	}
	
	/**
	 * 
	 * @return max-1 (if total 1, max index 0)
	 */
	private int getMaxIndex(){
//		return getListEntries().getVisibleItems()
		return vlVisibleEntriesList.size()-1;
//			+( ((int)getListEntries().getSlider().getModel().getMaximum()) -1);
	}
	
	private int getTopEntryIndex(){
		int iVisibleItems = getMainList().getVisibleItems();
		int iTotEntries = vlVisibleEntriesList.size();
		if(iVisibleItems>iTotEntries){
			return 0; //is not overflowing the max visible items amount
		}
		
		int iSliderInvertedIndex=(int)getMainList().getSlider().getModel().getValue();
		int iTopEntryIndex = (int)(iTotEntries -iSliderInvertedIndex -iVisibleItems);
		
		return iTopEntryIndex;
	}
	
	private int getBottomEntryIndex(){
		return getTopEntryIndex()+iVisibleRows-1;
	}
	
	private void scrollTo(int iIndex){
		//getListEntries().getSlider().getModel().getValue();
//		getListEntries().getSlider().getModel().setValue(getMaxIndex()-iIndex);
		getMainList().getSlider().getModel().setValue(
			vlVisibleEntriesList.size()-getMainList().getVisibleItems()-iIndex);
		
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
						selectRelativeEntry(-getMainList().getVisibleItems());
						break;
					case KeyInput.KEY_PGDN:
						selectRelativeEntry(getMainList().getVisibleItems());
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
						if(bControl){
							if(isInputToUserEnterCustomValueMode() && getInputText().isEmpty()){
								applyDefaultValueToUserModify();
							}else{
								getInputField().setText("");
							}
						}
						break;
					case KeyInput.KEY_NUMPADENTER:
					case KeyInput.KEY_RETURN:
						actionSubmit();
						break;
					case KeyInput.KEY_V: 
						if(bControl){
							MiscLemurHelpersStateI.i().insertTextAtCaratPosition(getInputField(),
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
	
	private BindKey bindKey(String strActionPerformedHelp, int iKeyCode, int... aiKeyModifiers){
		BindKey bk = MiscLemurHelpersStateI.i().bindKey(getInputField(), actSimpleActions,
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
	
	@Override
	protected R setInputText(String str) {
		getInputField().setText(str);
		return getThis();
	}
	
	public R addModalDialog(LemurDialogGUIStateAbs<T,?> diagModal){
		diagModal.setDiagParent(this);
		hmModals.put(diagModal.getId(),diagModal);
		return getThis();
	}
	
//	public DiagModalInfo<T> getDiagModalCurrent(){
//		return dmi;
//	}
	
//	public void openModalDialog(String strDialogId, DialogListEntryData<T> dataToAssignModalTo, T cmd){
	public void openModalDialog(String strDialogId, DialogListEntryData<T> dledToAssignModalTo, T cmd){
		LemurDialogGUIStateAbs<T,?> diagModalCurrent = hmModals.get(strDialogId);
		if(diagModalCurrent!=null){
			setDiagModalInfoCurrent(new DiagModalInfo(diagModalCurrent,cmd,dledToAssignModalTo));
			diagModalCurrent.requestEnable();
		}else{
			throw new PrerequisitesNotMetException("no dialog set for id: "+strDialogId);
		}
	}
	
	public void applyResultsFromModalDialog(){
		if(getChildDiagModalInfoCurrent()==null)throw new PrerequisitesNotMetException("no modal active");
		
		getChildDiagModalInfoCurrent().getDiagModal().resetChoice();
//		dmi.getDiagModal().adataSelectedEntriesList.clear();
		setDiagModalInfoCurrent(null);
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
			+"'"+dled.getTextValue()+"'"
		);
		
//		LemurMiscHelpersStateI.i().bugFix(null, LemurMiscHelpersStateI.i().btgBugFixListBoxSelectorArea, getListEntries());
		MiscLemurHelpersStateI.i().listboxSelectorAsUnderline(getMainList());
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
			throw new PrerequisitesNotMetException("data not present on the list", dledSelectRequested, getMainList());
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
		
		DialogMouseCursorListenerI.i().clearLastButtonHoverIn();
		
//		iSel = selectionModel.getSelection();
		cd().dumpDebugEntry(getId()+":"
			+"SelectedEntry="+iSel+","
			+"SliderValue="+MiscI.i().fmtFloat(getMainList().getSlider().getModel().getValue()));
		return iSel;
//		return iSel==null?-1:iSel;
	}

	public abstract boolean execTextDoubleClickActionFor(DialogListEntryData<T> dled);

	public abstract boolean execActionFor(EMouseCursorButton e, Spatial capture);

	@Override
	protected void updateSelected(DialogListEntryData<T> dledPreviouslySelected){
		if(dledPreviouslySelected==null)return;
		
		int i = vlVisibleEntriesList.indexOf(dledPreviouslySelected);
		if(i>=0){
			setSelectedEntryIndex(i);//selectionModel.setSelection(i);
		}else{
			updateSelected(getAbove(dledPreviouslySelected), dledPreviouslySelected.getParent());
		}
	}
	@Override
	protected void updateSelected(final DialogListEntryData<T> dledAbove, final DialogListEntryData<T> dledParentTmp){
		/**
		 * need to wait it actually get selected
		 */
		CallQueueI.i().addCall(new CallableX() {
			@Override
			public Boolean call() {
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

//	public ArrayList<DialogListEntryData<T>> getListCopy() {
//		return new ArrayList<DialogListEntryData<T>>(adleCompleteEntriesList);
//	}
	
	/**
	 * Lemur must have a chance to configure everything before we play with it.
	 * So this must happen at update and not at enable.
	 */
	private void prepareEffectListEntries(boolean bEnabling) {
//	private void prepareEffectListEntries(boolean bApplyNow) {
//		bRunningEffectAtAllListEntries=true;
		
		GridPanel gp = getMainList().getGridPanel();
		for(int iC=0;iC<gp.getVisibleColumns();iC++){
			for(int iR=0;iR<gp.getVisibleRows();iR++){
				Panel pnl = gp.getCell(iR, iC);
				if(pnl!=null)MiscLemurHelpersStateI.i().setScaleXY(pnl, fMinScale, 1f);
			}
		}
		
		if(bEnabling){
			bPreparedForListEntriesEffects=true;
			bRunningEffectAtAllListEntries=true;
		}
	}
	protected void updateEffectListEntries(boolean bGrow) {
		if(!btgEffectListEntries.b())return;
		if(!isTryingToEnable())return; // only actually interesting during enable
		if(!bPreparedForListEntriesEffects)prepareEffectListEntries(true);
		if(!isDialogEffectsDone())return; // play list effect after main one completes
//		if(!bPreparedForListEntriesEffects)prepareEffectListEntries(true);
		if(!bRunningEffectAtAllListEntries)return;
		
		GridPanel gp = getMainList().getGridPanel();
		
		int iTotal = gp.getVisibleColumns() * gp.getVisibleRows();
		int iCount = 0;
		
//		int iMaxConcurrent = 3;
//		int iCountConcurrent = 0;
		
		float fLastCalculatedEntryScale = 0f;
		for(int iC=0;iC<gp.getVisibleColumns();iC++){
			for(int iR=0;iR<gp.getVisibleRows();iR++){
				Panel pnl = gp.getCell(iR, iC);
				iCount++;
				
				if(pnl!=null){
					if(Float.compare(pnl.getLocalScale().x,1f)==0){
						continue;
					}
					
					fLastCalculatedEntryScale=updateEffectOnEntry(pnl,bGrow);
					if(fLastCalculatedEntryScale<0.33f){
						break;
					}
//					iCountConcurrent++;
					
//					fScale-=0.33f;
//					if(fScale<0f)fScale=fMinScale;
					
//					if(iCountConcurrent==iMaxConcurrent)break; //to update one entry step per frame
				}
			}
		}
		
		if(iCount==iTotal && Float.compare(fLastCalculatedEntryScale,1f)==0){ //the last entry scale must be 1f
			bRunningEffectAtAllListEntries=false; // ended
//			tdEffectListEachEntry.setActive(false);
		}
		
//		return bRunningEffectAtAllListEntries;
	}
	
	private float updateEffectOnEntry(Spatial spt, boolean bGrow) {
		if(Float.compare(spt.getLocalScale().x,fMinScale)==0){
			AudioUII.i().play(EAudio.DisplayEntryEffect);
		}
		
//		MiscJmeI.i().user
//		TimedDelayVarField td = (TimedDelayVarField)spt.getUserData("TimedDelayEffect");
		TimedDelayVarField td = MiscJmeI.i().retrieveUserDataTimedDelay(
			spt, "tdListEntryEffect", fdvEffectListEntryDelay.f());
		
		float fScale = 1f;
		float fPerc = td.getCurrentDelayPercentual(false);
		if(Float.compare(fPerc,1f)==0){
			td.updateTime(); //prepare for next entry
		}
		fScale = fPerc;
		
		if(!bGrow)fScale=1f-fScale; //shrink
		
		MiscLemurHelpersStateI.i().setScaleXY(spt, fScale, 1f);
		
		return fScale;
	}

	public Integer getFinalEntryHeightPixels() {
		return iFinalEntryHeightPixels;
	}
	
	@Override
	protected <N extends Node> void lineWrapDisableForChildrenOf(N node) {
		ListBox<String> lstbx = (ListBox<String>)node;
		MiscLemurHelpersStateI.i().lineWrapDisableForListboxEntries(lstbx);
	}
	
	public T getCmdDummy() {
		return (T) MiscLemurHelpersStateI.i().getCmdDummy();
	}

	protected abstract String getDefaultValueToUserModify();

	protected void applyDefaultValueToUserModify() {
		setInputText(getUserEnterCustomValueToken()+getDefaultValueToUserModify());
	}
	
	@Override
	public void focusGained() {
//		if(quaBkpMain!=null){
//			getContainerMain().setLocalRotation(quaBkpMain);
//		}
	}
	
	@Override
	public void focusLost() {
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1,1), new Vector3f(1,1,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(1,0,1), new Vector3f(0,1,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1f,1f), new Vector3f(1,0,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1,1), new Vector3f(1,0,0));
	}
}
