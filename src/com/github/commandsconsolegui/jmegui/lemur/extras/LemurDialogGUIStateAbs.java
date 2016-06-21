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

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.MouseCursorButtonData;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.extras.InteractionDialogStateAbs;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
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
* More info at {@link InteractionDialogStateAbs}
*	TODO implement docking dialogs, a small icon will be created at app window edges
* 
* @author AquariusPower <https://github.com/AquariusPower>
*
*/
public abstract class LemurDialogGUIStateAbs extends InteractionDialogStateAbs {
	private Label	lblTitle;
	private Label	lblTextInfo;
	private ListBox<DialogListEntryData>	lstbxEntriesToSelect;
	private VersionedList<DialogListEntryData>	vlEntriesList = new VersionedList<DialogListEntryData>();
	private int	iVisibleRows;
	private Integer	iEntryHeightPixels;
	private Vector3f	v3fEntryListSize;
	private Container	cntrEntryCfg;
	private SelectionModel	selectionModel;
	BoolTogglerCmdField btgAutoScroll = new BoolTogglerCmdField(this, true).setCallNothingOnChange();
	
//	@Override
//	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
////		ReflexFillCfg rfcfg = null;
//		
//		ReflexFillCfg rfcfg = new ReflexFillCfg(super.getReflexFillCfg(rfcv));
//		rfcfg.setCommandPrefix(rfcfg.getCommandPrefix()+getId());
//		rfcfg.setFirstLetterUpperCase(true);
////		ReflexFillCfg rfcfgSuper = super.getReflexFillCfg(rfcv);
////		if(rfcv.getClass().isAssignableFrom(BoolTogglerCmdField.class)){
////			rfcfg = new ReflexFillCfg(rfcfgSuper);
////			rfcfg.setCommandPrefix(getId());
////		}
////		
////		if(rfcfg==null)rfcfg = rfcfgSuper;
//		
//		return rfcfg;
//	}
//
	
	@Override
	public Container getContainerMain(){
		return (Container)super.getContainerMain();
	}
	
	public static class CfgParm extends InteractionDialogStateAbs.CfgParm{
		Float fDialogHeightPercentOfAppWindow;
		Float fDialogWidthPercentOfAppWindow;
		Float fInfoHeightPercentOfDialog;
		Integer iEntryHeightPixels;
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
		public CfgParm(boolean	bOptionSelectionMode,String strUIId, boolean bIgnorePrefixAndSuffix,
				Node nodeGUI, Float fDialogHeightPercentOfAppWindow,
				Float fDialogWidthPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Integer iEntryHeightPixels, BaseDialogStateAbs modalParent)
		{
			super(bOptionSelectionMode,strUIId, bIgnorePrefixAndSuffix, nodeGUI, modalParent);
			
			this.fDialogHeightPercentOfAppWindow = fDialogHeightPercentOfAppWindow;
			this.fDialogWidthPercentOfAppWindow = fDialogWidthPercentOfAppWindow;
			this.fInfoHeightPercentOfDialog = fInfoHeightPercentOfDialog;
			this.iEntryHeightPixels = iEntryHeightPixels;
		}
	}
	@Override
	public LemurDialogGUIStateAbs configure(ICfgParm icfg) {
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
	
	public void selectAndChoseOption(DialogListEntryData data){
		if(!isOptionSelectionMode())throw new PrerequisitesNotMetException("not option mode");
		if(data==null)throw new PrerequisitesNotMetException("invalid null data");
		
		selectEntry(data);
		requestActionSubmit();
	}

	public boolean isMyChild(Spatial spt){
		return getContainerMain().hasChild(spt); //this is actually recursive!!!
	}
	
//	@Override
//	protected boolean initGUI(){
//		return initGUI(0.75f, 0.75f, 0.25f, null);
//	}
//protected boolean initGUI(float fDialogHeightPercentOfAppWindow, float fDialogWidthPercentOfAppWindow, float fInfoHeightPercentOfDialog, Integer iEntryHeightPixels){
	/**
	 * The input field will not require height, will be small on south edge.
	 * @param fDialogPerc the percentual width/height to cover the application screen/window 
	 * @param fInfoPerc the percentual height to show informational text, the list and input field will properly use the remaining space
	 */
	/**
	 * The input field will not require height, will be small on south edge.
	 */
	@Override
	protected boolean initGUI(){
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
		
//		Vector3f v3fDiagWindowSize = v3fApplicationWindowSize.mult(fDialogPerc);
		Vector3f v3fDiagWindowSize = new Vector3f(v3fApplicationWindowSize);
		v3fDiagWindowSize.y *= cfg.fDialogHeightPercentOfAppWindow;
		v3fDiagWindowSize.x *= cfg.fDialogWidthPercentOfAppWindow;
		getContainerMain().setPreferredSize(v3fDiagWindowSize);
		
		///////////////////////// NORTH (info/help)
		cntrNorth = new Container(new BorderLayout(), strStyle);
		getNorthContainer().setName(getId()+"_NorthContainer");
		Vector3f v3fNorthSize = v3fDiagWindowSize.clone();
		v3fNorthSize.y *= cfg.fInfoHeightPercentOfDialog;
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
		float fListPerc = 1.0f - cfg.fInfoHeightPercentOfDialog;
		v3fEntryListSize = v3fDiagWindowSize.clone();
		v3fEntryListSize.y *= fListPerc;
		lstbxEntriesToSelect = new ListBox<DialogListEntryData>(
			new VersionedList<DialogListEntryData>(), 
			new CellRendererDialogEntry(strStyle, bOptionSelectionMode), strStyle);
		selectionModel = lstbxEntriesToSelect.getSelectionModel();
		lstbxEntriesToSelect.setName(getId()+"_EntriesList");
		lstbxEntriesToSelect.setSize(v3fEntryListSize); //not preferred, so the input field can fit properly
		//TODO multi was not implemented yet... lstbxVoucherListBox.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		getContainerMain().addChild(lstbxEntriesToSelect, BorderLayout.Position.Center);
		
//		vlstrEntriesList.add("(Empty list)");
		lstbxEntriesToSelect.setModel((VersionedList<DialogListEntryData>)vlEntriesList);
		
		iEntryHeightPixels = cfg.iEntryHeightPixels;
		
		//////////////////////////////// SOUTH (typing/config)
		cntrSouth = new Container(new BorderLayout(), strStyle);
		getSouthContainer().setName(getId()+"_SouthContainer");
		
//		// configure an entry from the list
//		cntrEntryCfg = new Container(new BorderLayout(), strStyle);
//		cntrEntryCfg.setName(getId()+"_EntryConfig");
//		getSouthContainer().addChild(cntrEntryCfg, Bor)
		
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
	
	@Override
	public void clearSelection() {
		selectionModel.setSelection(-1); //clear selection
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
	public DialogListEntryData getSelectedEntryData() {
		Integer iSel = selectionModel.getSelection();
		if(iSel==null)return null;
		return	vlEntriesList.get(iSel);
	}
//	@Override
//	protected Integer getSelectedIndex(){
//		Integer iSel = selectionModel.getSelection();
////		vlEntriesList.get(iSel);
//		if(iSel==null)return null;
//		return iSel;
//	}
	
//	@Override
//	protected String getSelectedEntryKey() {
//		Integer i = getSelectedEntryIndex();
//		if(i==null)return null;
//		if(i>=vlEntriesList.size())return null;
////		super.hmKeyValue.get();
//		return vlEntriesList.get(i).toString(); //TODO this is NOT the right key...
//	}
	
//	@Override
//	protected String getSelectedKey(){
//		Integer i = getSelectedIndex();
//		if(i==null)return null;
//		if(i>=vlstrEntriesList.size())return null;
////		super.hmKeyValue.get();
//		return vlstrEntriesList.get(i);
//	}
	
//	@Override
//	protected V getSelectedValue() {
//		Integer i = getSelectedIndex();
//		if(i==null)return null;
//		if(i>=vlEntriesList.size())return null;
////		super.hmKeyValue.get();
//		return vlEntriesList.get(i); // not super! return super.getSelectedValue();
//	}
	
	/**
	 * call {@link #updateList(ArrayList)} from the method overriding this
	 */
	@Override
	protected void updateList(){
		// update visible rows
//		if(iEntryHeightPixels==null){
//			if(vlEntriesList.size()>0){
//				if(vlEntriesList.get(0) instanceof String){
//					Float fEntryHeight = LemurMiscHelpersStateI.i().guessEntryHeight(lstbxEntriesToSelect);
//					if(fEntryHeight!=null){
//						iEntryHeightPixels=fEntryHeight.intValue(); //calc based on entry (or font) height and listbox height
//						cd().dumpInfoEntry("entry height "+iEntryHeightPixels);
//					}else{
//						iEntryHeightPixels=20; //blind placeholder
//						cd().dumpWarnEntry("blind entry height "+iEntryHeightPixels);
//					}
//				}
//			}
//		}
		iEntryHeightPixels=20; //blind placeholder
		iVisibleRows = (int) (v3fEntryListSize.y/iEntryHeightPixels);
		lstbxEntriesToSelect.setVisibleItems(iVisibleRows);
	}
	
	/**
	 * override ex. to avoid reseting
	 */
	protected void resetList(){
//		hmKeyValue.clear();
		vlEntriesList.clear();
		selectionModel.setSelection(-1);
	}
	
	/**
	 * basic functionality
	 * 
	 * @param aValueList
	 */
	protected void updateList(ArrayList<DialogListEntryData> adle){
		resetList();
		
		for(DialogListEntryData dle:adle){
//			String strKey = formatEntryKey(entry.getValue());
//			if(strKey==null)throw new NullPointerException("entry is null, not formatted?");
			if(strLastFilter.isEmpty() || dle.getText().toLowerCase().contains(strLastFilter)){
				vlEntriesList.add(dle);
//				hmKeyValue.put(entry.getKey(), entry.getValue());
			}
		}
		
		if(dleLastSelected!=null){
			int i = getSelectedIndex();
			if(i>-1)selectionModel.setSelection(i);
		}
	}
//	protected void updateList(ArrayList<V> aValueList){
//		resetList();
//		
//		for(V val:aValueList){
//			String strKey = formatEntryKey(val);
//			if(strKey==null)throw new NullPointerException("entry is null, not formatted?");
//			if(strLastKeyFilter.isEmpty() || strKey.toLowerCase().contains(strLastKeyFilter)){
//				vlEntriesList.add(val);
//				hmKeyValue.put(strKey, val);
//			}
//		}
//		
//		if(strLastSelectedKey!=null){
//			int i = vlEntriesList.indexOf(hmKeyValue.get(strLastSelectedKey));
//			if(i>-1)selectionModel.setSelection(i);
//		}
//	}
	
	/**
	 * 
	 * @return -1 if none
	 */
	public int getSelectedIndex(){
		return vlEntriesList.indexOf(dleLastSelected);
	}
	
	protected void autoScroll(){
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
	}
	
	@Override
	protected boolean updateOrUndo(float tpf) {
		if(!super.updateOrUndo(tpf))return false;
		
		if(btgAutoScroll.b())autoScroll();
//		multiClickAction();
		
		return true;
	}
	
//	@Deprecated
//	private void multiClickAction() {
//		if(eMultiClickAction!=null){
//			/**
//			 * multi click actions execution must be delayed by the multi click delay limit 
//			 */
//			if(!MouseCursorCentralI.i().isMultiClickDelayWithinLimitFrom(lClickActionMilis)){
//				boolean bConsumed=false;
//				switch(eMultiClickAction){
//					case OpenConfigDialog:
//						openCfgDataDialog(null); //TODO usage is wrong 
//						bConsumed=true;
//						break;
//					case OptionModeSubmit:
//						actionSubmit();
//						bConsumed=true;
//						break;
//					case DebugTestMultiClickAction3times:
//						cd().dumpDebugEntry(eMultiClickAction+" at "+getId());
//						bConsumed=true;
//						break;
//				}
//				
//				if(bConsumed){
//					consumeAndResetMultiClickAction();
//				}
//			}
//		}
//	}

	public void openCfgDataDialog(DialogListEntryData dataToCfg){
		LemurDialogGUIStateAbs diag = hmModals.get(EModalDiagType.ListEntryConfig);
		if(diag!=null){
			this.setCfgDataReference(dataToCfg);
			diag.requestEnable();
		}else{
//			throw new PrerequisitesNotMetException("no cfg dialog set");
		}
	}
	
//	@Deprecated
//	enum EMultiClickAction{
//		OptionModeSubmit,
//		OpenConfigDialog, 
//		DebugTestMultiClickAction3times,
//	}
//	EMultiClickAction eMultiClickAction = null;
	
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
		return vlEntriesList.size()-1;
//			+( ((int)lstbxEntriesToSelect.getSlider().getModel().getMaximum()) -1);
	}
	
	protected int getTopEntryIndex(){
		int iVisibleItems = lstbxEntriesToSelect.getVisibleItems();
		int iTotEntries = vlEntriesList.size();
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
			vlEntriesList.size()-lstbxEntriesToSelect.getVisibleItems()-iIndex);
		
	}
	
	@Override
	protected boolean initKeyMappings(){
		KeyActionListener actSimpleActions = new KeyActionListener() {
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
						selectRelativeEntry(-vlEntriesList.size()); //uses underflow protection
//						if(bControl)selectEntry(vlEntriesList.get(0));
						break;
					case KeyInput.KEY_END:
						selectRelativeEntry(vlEntriesList.size()); //uses overflow protection
//						if(bControl)selectEntry(vlEntriesList.get(vlEntriesList.size()-1));
						break;
					case KeyInput.KEY_NUMPADENTER:
					case KeyInput.KEY_RETURN:
						actionSubmit();
						break;
					case KeyInput.KEY_V: 
						if(bControl){
							getInputField().setText(getInputField().getText()
								+MiscI.i().retrieveClipboardString(true));
						}
						break;
				}
			}
	
		};
		
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_HOME,KeyAction.CONTROL_DOWN), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_END,KeyAction.CONTROL_DOWN), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_PGUP), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_PGDN), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_RETURN), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_NUMPADENTER), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_ESCAPE), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_UP), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_DOWN), actSimpleActions);
		
		return true;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,"showDialogKeyBinds"+getId(),null,"")){
			cc.dumpSubEntry("ESC - close");
			cc.dumpSubEntry("Up/Down/PgUp/PgDn/Ctrl+Home|End - nav. list entry");
			cc.dumpSubEntry("Enter - accept selected choice at config dialog");
			cc.dumpSubEntry("DoubleClick - open config/accept choice at config dialog");
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
	
	public static enum EModalDiagType{
		ListEntryConfig,
		ListEntryProperties,
		HintHelp, //generic
	}
	
	protected HashMap<EModalDiagType, LemurDialogGUIStateAbs> hmModals = new HashMap<EModalDiagType, LemurDialogGUIStateAbs>();
	protected Long	lClickActionMilis;
	private MouseCursorButtonData	buttonData;
	
//	public static class ModalDiag{
//		EModalDiagType e;
//		LemurDialogGUIStateAbs diagModal;
//		public ModalDiag(EModalDiagType e, LemurDialogGUIStateAbs diagModal) {
//			super();
//			this.e = e;
//			this.diagModal = diagModal;
//		}
//	}
	
	public void configModalDialog(EModalDiagType e, LemurDialogGUIStateAbs diagModal){
		diagModal.setModalParent(this);
		hmModals.put(e,diagModal);
	}
	
//	/**
//	 * this will override older request if set fast enough
//	 * @param e
//	 */
//	@Deprecated
//	protected void setMultiClickAction(MouseCursorButtonData buttonData, EMultiClickAction e){
//		/**
//		 * DO NOT CHECK if eClickAction != null
//		 * it is to be overriden!!!
//		 */
//		this.eMultiClickAction=e;
//		this.lClickActionMilis=System.currentTimeMillis();
//		this.buttonData=buttonData;
//	}
//	@Deprecated
//	protected void consumeAndResetMultiClickAction(){
//		eMultiClickAction=null;
//		
//		lClickActionMilis=null;
//		
//		buttonData.getClicks().clearClicks();
//		buttonData=null;
//	}
	
//	@Deprecated
//	public boolean actionMultiClick(MouseCursorButtonData buttonData, Spatial capture, int iClickCount) {
//		switch(iClickCount){
//			case 2:
//				if(isListBoxEntry(capture)){
//					if(bOptionSelectionMode){
//						setMultiClickAction(buttonData, EMultiClickAction.OptionModeSubmit);
//						return true;
//					}else{
//						setMultiClickAction(buttonData, EMultiClickAction.OpenConfigDialog);
////						LemurDialogGUIStateAbs diag = hmModals.get(EModalDiagType.ListEntryConfig);
////						if(diag!=null){
////							diag.requestEnable();
////							return true;
////						}
//					}
//					return true;
//				}
//				break;
//			case 3:
//				setMultiClickAction(buttonData, EMultiClickAction.DebugTestMultiClickAction3times);
//				break;
//		}
//		
//		return false;
//	}
	
	public boolean openPropertiesDialogFor(Spatial capture) {
		if(isListBoxEntry(capture)){
			LemurDialogGUIStateAbs diag = hmModals.get(EModalDiagType.ListEntryProperties);
			if(diag!=null){
				diag.requestEnable();
				return true;
			}else{
//				throw new PrerequisitesNotMetException("no properties dialog set");
			}
		}
		
		return false;
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

	public void selectEntry(DialogListEntryData data) {
		int i=vlEntriesList.indexOf(data);
		if(i>=0){
			selectionModel.setSelection(i);
			cd().dumpDebugEntry(getId()+",SelectIndex="+i+","+data.toString());
		}else{
			throw new PrerequisitesNotMetException("data not present on the list", data, lstbxEntriesToSelect);
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
		if(iMaxIndex==0)return null;
		
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
		
		selectionModel.setSelection(iSel);
		
//		iSel = selectionModel.getSelection();
		cd().dumpDebugEntry(getId()+":"
			+"SelectedEntry="+iSel+","
			+"SliderValue="+MiscI.i().fmtFloat(lstbxEntriesToSelect.getSlider().getModel().getValue()));
		return iSel;
//		return iSel==null?-1:iSel;
	}

}
