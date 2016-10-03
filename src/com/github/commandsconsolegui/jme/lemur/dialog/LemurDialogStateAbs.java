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

package com.github.commandsconsolegui.jme.lemur.dialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jme.AudioUII;
import com.github.commandsconsolegui.jme.AudioUII.EAudio;
import com.github.commandsconsolegui.jme.ConditionalStateManagerI.CompositeControl;
import com.github.commandsconsolegui.jme.DialogStateAbs;
import com.github.commandsconsolegui.jme.MouseCursorCentralI;
import com.github.commandsconsolegui.jme.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData;
import com.github.commandsconsolegui.jme.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jme.lemur.console.LemurConsoleStateI;
import com.github.commandsconsolegui.jme.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogManagerI.DialogStyleElementId;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogManagerI.DummyEffect;
import com.github.commandsconsolegui.jme.lemur.extras.CellRendererDialogEntry;
import com.github.commandsconsolegui.jme.lemur.extras.CellRendererDialogEntry.CellDialogEntry;
import com.github.commandsconsolegui.jme.lemur.extras.CellRendererDialogEntry.CellDialogEntry.EUserData;
import com.github.commandsconsolegui.jme.lemur.extras.DialogMainContainer;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.DebugI;
import com.github.commandsconsolegui.misc.DebugI.EDebugKey;
import com.github.commandsconsolegui.misc.HoldRestartable;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.WorkAroundI;
import com.github.commandsconsolegui.misc.WorkAroundI.BugFixBoolTogglerCmdField;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI.BindKey;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
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
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.effect.AbstractEffect;
import com.simsilica.lemur.effect.Effect;
import com.simsilica.lemur.effect.EffectInfo;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.list.SelectionModel;
import com.simsilica.lemur.style.ElementId;

/**
* 
* More info at {@link DialogStateAbs}
*	TODO implement docking dialogs, a small icon will be created at app window edges
* 
* TODO migrate from {@link DialogStateAbs} to here, everything that is not usable at {@link LemurConsoleStateI}
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
//public abstract class LemurDialogGUIStateAbs<T,CS extends LemurDialogGUIStateAbs.CompositeSavableLemur,R extends LemurDialogGUIStateAbs<T,CS,R>> extends BaseDialogStateAbs<T,CS,R> {//implements IWorkAroundBugFix{
public abstract class LemurDialogStateAbs<T,THIS extends LemurDialogStateAbs<T,THIS>> extends DialogStateAbs<T,THIS> {//implements IWorkAroundBugFix{
	private Label	lblTitle;
	private Label	lblTextInfo;
//	private ListBox<DialogListEntryData<T>>	lstbxEntriesToSelect;
	private VersionedList<DialogListEntryData<T>>	vlVisibleEntriesList = new VersionedList<DialogListEntryData<T>>();
	private int	iVisibleRows;
//	private Integer	iEntryHeightPixels; //TODO this is init is failing why? = 20; 
	private Vector3f	v3fEntryListSizeIni;
	private Container	cntrEntryCfg;
	private SelectionModel	selectionModel;
	private BoolTogglerCmdField btgAutoScroll = new BoolTogglerCmdField(this, true).setCallNothingOnChange();
//	private ButtonCommand	bc;
	private boolean	bRefreshScroll;
	private HashMap<String, HoldRestartable<LemurDialogStateAbs<T,?>>> hmhrChildDiagModals = new HashMap<String, HoldRestartable<LemurDialogStateAbs<T,?>>>();
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
	public DialogMainContainer getDialogMainContainer(){
		return (DialogMainContainer)super.getDialogMainContainer();
	}
	
	private FloatDoubleVarField fdvEntryHeightMultiplier = new FloatDoubleVarField(this,1f,"");
	
	public static class CfgParm extends DialogStateAbs.CfgParm{
		private Float fDialogHeightPercentOfAppWindow;
		private Float fDialogWidthPercentOfAppWindow;
		private Float fNorthHeightPercentOfDialog;
		
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
			this.fNorthHeightPercentOfDialog = fInfoHeightPercentOfDialog;
			if(fEntryHeightMultiplier!=null)this.fEntryHeightMultiplier = fEntryHeightMultiplier;
		}
	}
	private CfgParm	cfg;
	private boolean	bRunningEffectAtAllListEntries;
	private float	fMinScale = 0.01f;
	private boolean	bPreparedForListEntriesEffects;
	private Integer	iFinalEntryHeightPixels;
	private Quaternion	quaBkpMain;
	private Container	cntrCenterMain;
	private Button	btnResizeNorth;
	private Button	btnResizeSouth;
	private Button	btnResizeEast;
	private Button	btnResizeWest;
	private ArrayList<Button>	abtnBorderList = new ArrayList<Button>();
	private Vector3f	v3fDiagSize;
	private Vector3f	v3fPosCentered;
	private Button	btnResizeInfoAndList;
	private Container	cntrTitleBox;
	private Container	cntrTitleButtons;
	private Button	btnMinimize;
	private Button	btnMaximize;
	private Button	btnClose;
//	public Vector3f	v3fUnmaximizedSize;
//	public Long	lUnmaximizedBorderThickness;
	private ButtonClick	btnclk;
private Button	btnRestart;
//	private boolean	bMaximized;
	
	@Override
	public THIS configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;//this also validates if icfg is the CfgParam of this class
		
		fdvEntryHeightMultiplier.setObjectRawValue(cfg.fEntryHeightMultiplier);
//		DialogMouseCursorListenerI.i().configure(null);
		
		if(cfg.fDialogHeightPercentOfAppWindow==null){
			cfg.fDialogHeightPercentOfAppWindow=0.75f;
		}
		
		if(cfg.fDialogWidthPercentOfAppWindow==null){
			cfg.fDialogWidthPercentOfAppWindow=0.75f;
		}
		
		if(cfg.fNorthHeightPercentOfDialog==null){
			cfg.fNorthHeightPercentOfDialog=0.25f;
		}
		
//		if(isCompositeSavableSet())setCompositeSavable(new LemurDialogCS(this));
		
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
		return getDialogMainContainer().hasChild(spt); //this is actually recursive!!!
	}
	
	private float sizePercOrPixels(float fSizeBase, float fPercOrPixels){
		if(Float.compare(fPercOrPixels, 1.0f)<=0){ //percent
			fSizeBase *= fPercOrPixels;
		}else{ // >1.0f is in pixels
			fSizeBase = fPercOrPixels;
		}
		
		return fSizeBase;
	}
	
	protected LmrDiagCS getCS() {
		return super.getCompositeSavable(LmrDiagCS.class);
	}
	
	@Override
	protected boolean initAttempt() {
		if(!isCompositeSavableSet())setCompositeSavable(new LmrDiagCS(this));
		if(!super.initAttempt())return false;
		
//		setRetryDelayFor(300L, EDelayMode.Update.s()); //mainly useful when resizing
		
		return true;
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
		
		//main top container
//		setContainerMain(new ContainerMain(new BorderLayout(), getDiagStyle()).setDiagOwner(this));
		setDialogMainContainer(new DialogMainContainer(this, new BorderLayout(), getDiagStyle()));
		getDialogMainContainer().setName(getUniqueId()+"_Dialog");
		
		Vector3f v3fAppWindowSize = MiscJmeI.i().getAppWindowSize();
		v3fDiagSize = new Vector3f(v3fAppWindowSize);
		v3fDiagSize.y = sizePercOrPixels(v3fDiagSize.y,cfg.fDialogHeightPercentOfAppWindow);
		v3fDiagSize.x = sizePercOrPixels(v3fDiagSize.x,cfg.fDialogWidthPercentOfAppWindow);
		
		v3fPosCentered = new Vector3f(
			(v3fAppWindowSize.x-v3fDiagSize.x)/2f,
			(v3fAppWindowSize.y-v3fDiagSize.y)/2f+v3fDiagSize.y,
			0
		);
		
		cfg.setIniPos(v3fPosCentered.clone());
		cfg.setIniSize(v3fDiagSize.clone());
		
		MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fDiagSize, true);
		getDialogMainContainer().setLocalTranslation(v3fPosCentered);
		
		// resizing borders
//		CallQueueI.i().addCall(new CallableX() {
//			@Override
//			public Boolean call() {
				reinitBorders();//true);
//				return true;
//			}
//		});
//		if(false){
			v3fDiagSize.x -= btnResizeEast.getSize().x + btnResizeWest.getSize().x;
			v3fDiagSize.y -= btnResizeNorth.getSize().y + btnResizeSouth.getSize().y;
//		}
		
		// main center container
		cntrCenterMain = new Container(new BorderLayout(), getDiagStyle());
		MiscJmeI.i().setUserDataPSH(cntrCenterMain, this);
		quaBkpMain = cntrCenterMain.getLocalRotation().clone();
		cntrCenterMain.setName(getUniqueId()+"_CenterMain");
		getDialogMainContainer().addChild(cntrCenterMain, BorderLayout.Position.Center);
		
		// impossible layout indicator
//		Label lbl = new Label("[X] impossible layout",getDiagStyle());
//		lbl.setFontSize(0.5f);
		getDialogMainContainer().setImpossibleLayoutIndicatorAndCenterMain(null, cntrCenterMain);
		
		// regions
		initNorthRegion();
		initCenterRegion();
		initSouthRegion();
		
		// finalize
		LemurFocusHelperStateI.i().prepareDialogToBeFocused(this);
		CursorEventControl.addListenersToSpatial(getDialogMainContainer(), DialogMouseCursorListenerI.i());
		
		getNodeGUI().attachChild(getDialogMainContainer());
		
		return true;
	}
	
	private void initSouthRegion() {
		//////////////////////////////// SOUTH (typing/config)
		setCntrSouth(new Container(new BorderLayout(), getDiagStyle()));
		getSouthContainer().setName(getUniqueId()+"_SouthContainer");
		
//		// configure an entry from the list
//		cntrEntryCfg = new Container(new BorderLayout(), getStyle());
//		cntrEntryCfg.setName(getId()+"_EntryConfig");
//		getSouthContainer().addChild(cntrEntryCfg, Bor)
		
		// status line, about the currently selected entry on the list
		lblSelectedEntryStatus = new Label("Selected Entry Status",getDiagStyle());
		MiscJmeI.i().lineWrapDisableFor(lblSelectedEntryStatus);
		getSouthContainer().addChild(lblSelectedEntryStatus, BorderLayout.Position.North);
		
		// mainly used as a list filter
		setInputField(new TextField("",getDiagStyle()));
		getInputField().setName(getUniqueId()+"_InputField");
		LemurFocusHelperStateI.i().addFocusChangeListener(getInputField());
		getSouthContainer().addChild(getInputField(),BorderLayout.Position.South);
		
		cntrCenterMain.addChild(getSouthContainer(), BorderLayout.Position.South);
	}

	private void initCenterRegion() {
		//////////////////////////// CENTER (list)
		// list
		v3fEntryListSizeIni = v3fDiagSize.clone();
//		float fListPerc = 1.0f - cfg.fInfoHeightPercentOfDialog;
//		v3fEntryListSize.y *= fListPerc;
		v3fEntryListSizeIni.y -= getNorthContainer().getPreferredSize().y;//fInfoHeightPixels;
		setMainList(new ListBox<DialogListEntryData<T>>(
			new VersionedList<DialogListEntryData<T>>(), 
			getCellRenderer(), 
			getDiagStyle()));
		selectionModel = getMainList().getSelectionModel();
		getMainList().setName(getUniqueId()+"_EntriesList");
		getMainList().setSize(v3fEntryListSizeIni); //not preferred, so the input field can fit properly
		//TODO multi was not implemented yet... lstbxVoucherListBox.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		cntrCenterMain.addChild(getMainList(), BorderLayout.Position.Center);
		
//		vlstrEntriesList.add("(Empty list)");
		getMainList().setModel((VersionedList<DialogListEntryData<T>>)vlVisibleEntriesList);
		
//		LemurMiscHelpersStateI.i().bugFix(null, LemurMiscHelpersStateI.i().btgBugFixListBoxSelectorArea, getListEntries());
		
//		/**
//		 * TODO entry height should be automatic... may be each entry could have its own height.
//		 */
//		iEntryHeightPixels = cfg.iEntryHeightPixels;
	}

	private void initNorthRegion() {
		///////////////////////// NORTH (title + info/help)
		setContainerNorth(new Container(new BorderLayout(), getDiagStyle()));
		getNorthContainer().setName(getUniqueId()+"_NorthContainer");
		
		setNorthHeight(v3fDiagSize.y, true);
//		/**
//		 * TODO info height should be automatic. Or Info should be a list with vertical scroll bar, and constrainted to >= 1 lines.
//		 */
//		Vector3f v3fNorthSize = v3fDiagSize.clone();
//		float fNorthHeightPixels = sizePercOrPixels(v3fDiagSize.y, cfg.fInfoHeightPercentOfDialog);
//		v3fNorthSize.y = fNorthHeightPixels;
//		MiscLemurStateI.i().setSizeSafely(getNorthContainer(), v3fNorthSize, true);
		
		cntrTitleBox = new Container(new BorderLayout(), getDiagStyle());
		cntrTitleBox.setName(getUniqueId()+"_TitleBox");
		
		//title 
		lblTitle = new Label(getTitle(),getDiagStyle());
		lblTitle.setName(getUniqueId()+"_Title");
		ColorRGBA cLightGreen = new ColorRGBA(0.35f,1f,0.35f,1f);
		lblTitle.setColor(cLightGreen); //TODO make it custom
		
		cntrTitleBox.addChild(lblTitle, BorderLayout.Position.Center);
		
		cntrTitleButtons = new Container(new SpringGridLayout(), getDiagStyle());
		cntrTitleButtons.setName(getUniqueId()+"_TitleButtons");
		
		//buttons 
		btnRestart = new Button("[Restart]",getDiagStyle());
		btnMinimize = new Button("[m]",getDiagStyle());
		btnMaximize = new Button("[M]",getDiagStyle());
		btnClose = new Button("[X]",getDiagStyle());
		
		btnclk = new ButtonClick();
		btnRestart.addClickCommands(btnclk);
		btnMinimize.addClickCommands(btnclk);
		btnClose.addClickCommands(btnclk);
		btnMaximize.addClickCommands(btnclk);
		
		int i=0;
		cntrTitleButtons.addChild(btnRestart,i++);
		cntrTitleButtons.addChild(btnMinimize,i++);
		cntrTitleButtons.addChild(btnMaximize,i++);
		cntrTitleButtons.addChild(btnClose,i++);
		
		cntrTitleBox.addChild(cntrTitleButtons, BorderLayout.Position.East);
		
		getNorthContainer().addChild(cntrTitleBox, BorderLayout.Position.North);
		
//		CursorEventControl.addListenersToSpatial(lblTitle, DialogMouseCursorListenerI.i());
		
		// simple info/help box
		lblTextInfo = new Label("",getDiagStyle());
		lblTextInfo.setName(getUniqueId()+"_TxtInfo");
		MiscJmeI.i().lineWrapDisableFor(lblTextInfo);
		getNorthContainer().addChild(lblTextInfo, BorderLayout.Position.Center);
		
		// info (and list) resizer
		btnResizeInfoAndList=prepareResizeBorder(btnResizeInfoAndList,null);
		MiscLemurStateI.i().setSizeSafely(btnResizeInfoAndList, 1, 1, true);
		getNorthContainer().addChild(btnResizeInfoAndList, BorderLayout.Position.South);
//		setBordersThickness(getCompositeSavable(LmrDiagCS.class).ilvBorderThickness.intValue());
		setBordersThickness(LemurDialogManagerI.i().ilvBorderThickness.intValue());
		
		cntrCenterMain.addChild(getNorthContainer(), BorderLayout.Position.North);
	}
	
//private Vector3f	v3fUnmaximizedPos;
	private class ButtonClick implements Command<Button>{

		@Override
		public void execute(Button source) {
			if(source.equals(btnRestart)){
				requestRestart();
			}else
			if(source.equals(btnClose)){
				requestDisable();
			}else
			if(source.equals(btnMinimize)){
				//TODO this requires some kind of window management/lister/docking etc...
			}else
			if(source.equals(btnMaximize)){
				applyCurrentSettings(true);
			}else
			{
				AudioUII.i().play(EAudio.Failure);
				GlobalCommandsDelegatorI.i().dumpDevWarnEntry("unsupported "+source.getName(), source, this);
				return;
			}
			
			AudioUII.i().play(EAudio.ReturnChosen);
		}
	}
	
	//	private void reinitBorders(boolean bAppyBorderSize) {
	private void reinitBorders() {
//		abtnResizeBorderList.clear();
		
		btnResizeNorth	= prepareResizeBorder(btnResizeNorth, BorderLayout.Position.North);
		btnResizeSouth	= prepareResizeBorder(btnResizeSouth, BorderLayout.Position.South);
		btnResizeEast		= prepareResizeBorder(btnResizeEast	, BorderLayout.Position.East);
		btnResizeWest		= prepareResizeBorder(btnResizeWest	, BorderLayout.Position.West);
		
//		setBordersThickness(getCompositeSavable(LmrDiagCS.class).ilvBorderThickness.intValue());
		setBordersThickness(LemurDialogManagerI.i().ilvBorderThickness.intValue());
//		getCompositeSavable(LemurDialogCS.class).ilvBorderThickness.callerAssignedRunNow();
		
//		if(bAppyBorderSize){
//			CallQueueI.i().addCall(new CallableX(this) {
//				@Override
//				public Boolean call() {
//					if(getCompositeSavable(LemurDialogCS.class)==null)return false;
//					getCompositeSavable(LemurDialogCS.class).ilvBorderThickness.callerAssignedQueueNow();
//					return true;
//				}
//			});
//		}
//		setBordersSize(ilvBorderSize.getInt());
	}

	@Override
	public void applyCurrentSettings(boolean bToggleMaximized) {
		LmrDiagCS lsv = getCompositeSavable(LmrDiagCS.class);
	//	IntLongVarField ilv = lsv.ilvBorderThickness;
		boolean b = bToggleMaximized ? lsv.toggleMaximized() : lsv.isMaximized();
		if(b){ //maximize
			Vector3f v3fAppW = MiscJmeI.i().getAppWindowSize();
			MiscLemurStateI.i().setPositionSafely(getDialogMainContainer(), new Vector3f(0,v3fAppW.y,0));
			MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fAppW, true);
		}else{ //restore
			MiscLemurStateI.i().setPositionSafely(getDialogMainContainer(),
				new Vector3f(lsv.getPosX(), lsv.getPosY(), 0));
			MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), 
				new Vector3f(lsv.getWidth(), lsv.getHeight(), 0), true);
		}
		
		requestRefreshUpdateList();
	}

	private BoolTogglerCmdField btgResizeBordersNorthAndSouthAlsoAffectEastBorder = new BoolTogglerCmdField(this,true);
	private BoolTogglerCmdField btgResizeBordersWestAndEastAlsoAffectSouthBorder = new BoolTogglerCmdField(this,true);
	
	@Override
	public void move(Spatial sptDraggedElement, Vector3f v3fDisplacement) {
		if(getCS().isMaximized())return;
		
		if(sptDraggedElement==btnResizeInfoAndList){
			if(isMouseCursorWithinReach(btnResizeInfoAndList)){
				resizeInfoAndList(v3fDisplacement);
			}
			return;
		}
		
		// will be resize
		Vector3f v3fSizeAdd = new Vector3f();
		Vector3f v3fPosAdd = new Vector3f();
		
		boolean bWorkOnEastBorder = false;
		boolean bWorkOnSouthBorder = false;
		if(sptDraggedElement==btnResizeNorth){
			if(isMouseCursorWithinReach(btnResizeNorth)){
				v3fSizeAdd.y += v3fDisplacement.y;
				v3fPosAdd.y += v3fDisplacement.y;
				if(btgResizeBordersNorthAndSouthAlsoAffectEastBorder.b())bWorkOnEastBorder=true;
			}
		}else
		if(sptDraggedElement==btnResizeSouth){
			if(isMouseCursorWithinReach(btnResizeSouth)){
				bWorkOnSouthBorder = true;
				if(btgResizeBordersNorthAndSouthAlsoAffectEastBorder.b())bWorkOnEastBorder=true;
			}
		}else
		if(sptDraggedElement==btnResizeWest){
			if(isMouseCursorWithinReach(btnResizeWest)){
				v3fSizeAdd.x += -v3fDisplacement.x;
				v3fPosAdd.x += v3fDisplacement.x;
				if(btgResizeBordersWestAndEastAlsoAffectSouthBorder.b())bWorkOnSouthBorder=true;
			}
		}else
		if(sptDraggedElement==btnResizeEast){
			if(isMouseCursorWithinReach(btnResizeEast)){
				bWorkOnEastBorder=true;
				if(btgResizeBordersWestAndEastAlsoAffectSouthBorder.b())bWorkOnSouthBorder=true;
			}
		}else
		{
			/**
			 * simple move by dragging at any other unrecognized element
			 * TODO allow dragging only the title?
			 */
			super.move(sptDraggedElement, v3fDisplacement);
			return;
		}
		
		if(bWorkOnEastBorder)v3fSizeAdd.x += v3fDisplacement.x;
		if(bWorkOnSouthBorder)v3fSizeAdd.y += -v3fDisplacement.y;
		
		Vector3f v3fSizeNew = getDialogMainContainer().getPreferredSize().add(v3fSizeAdd);
		MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fSizeNew, true);
//		if(MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fSizeNew, true)!=null){
		Vector3f v3fPosNew = getDialogMainContainer().getLocalTranslation().add(v3fPosAdd);
		getDialogMainContainer().setLocalTranslation(v3fPosNew);
//		}
		requestRefreshUpdateList();
	}
	
//	private Vector3f overrideDragDisplacement(Panel pnl){
//		Vector2f v2fPosMCursor = MouseCursorCentralI.i().getMouseCursorPositionCopy();
//		Vector3f v3fPosPnl = pnl.getWorldTranslation().clone();
//		Vector3f v3fSizePnl = pnl.getSize().clone();
//		return new Vector3f();
//	}
	private boolean bMouseCursorWasInsideDragMargin = false;
	private IntLongVarField ilvDragMagnecticMarginPixels = new IntLongVarField(this,10,null).setMinMax(0L,20L);
	private Button	btnCurrentlyActiveDraggedReziseBorder;
//	private Vector3f adaptDragDisplacement(Panel pnl, Vector3f v3fDragDisplacement){
//		v3fDragDisplacement=v3fDragDisplacement.clone();
//		
//		int iMarginPixels = ilvDragMagnecticMarginPixels.intValue();
//		Vector2f v2fPosMCursor = MouseCursorCentralI.i().getMouseCursorPositionCopy();
//		Vector3f v3fPosPnl = pnl.getWorldTranslation().clone();
//		Vector3f v3fSizePnl = pnl.getSize().clone();
//		
//		v3fPosPnl.x-=iMarginPixels;
//		v3fPosPnl.y-=iMarginPixels;
//		
//		v3fSizePnl.x+=iMarginPixels*2;
//		v3fSizePnl.y+=iMarginPixels*2;
//		
//		if(v2fPosMCursor.y > v3fPosPnl.y && v2fPosMCursor.y < (v3fPosPnl.y + v3fSizePnl.y)){ 
//			if(v2fPosMCursor.x > v3fPosPnl.x && v2fPosMCursor.x < (v3fPosPnl.x + v3fSizePnl.x)){
//				return v3fDragDisplacement;
//			}
//		}
//		
//		return v3fDragDisplacement;
//	}
	private boolean isMouseCursorWithinReach(Button btn){
		if(!isRequestHitBorderToContinueDragging())return true;
		
		Vector2f v2fPosMCursor = MouseCursorCentralI.i().getMouseCursorPositionCopy();
		Vector3f v3fPosPnl = btn.getWorldTranslation().clone();
		Vector3f v3fSizePnl = btn.getSize().clone();
		
		int iDMMP = ilvDragMagnecticMarginPixels.intValue();
		if(bMouseCursorWasInsideDragMargin)iDMMP=0;
		
		v3fPosPnl.x-=iDMMP;
		v3fPosPnl.y-=iDMMP;
		
		v3fSizePnl.x+=iDMMP*2;
		v3fSizePnl.y+=iDMMP*2;
		
		if(v2fPosMCursor.y > v3fPosPnl.y && v2fPosMCursor.y < (v3fPosPnl.y + v3fSizePnl.y)){ 
			if(v2fPosMCursor.x > v3fPosPnl.x && v2fPosMCursor.x < (v3fPosPnl.x + v3fSizePnl.x)){
				setRequestHitBorderToContinueDragging(false); //it hits the border!
				bMouseCursorWasInsideDragMargin=true;
				btnCurrentlyActiveDraggedReziseBorder = btn;
				return true;
			}
		}
		
		bMouseCursorWasInsideDragMargin=false;
		return false;
	}
	
	@Override
	public void setBeingDragged(Object objDragManagerKey, boolean b) {
		super.setBeingDragged(objDragManagerKey, b);
		
		if(!b){
			bMouseCursorWasInsideDragMargin=false;
			btnCurrentlyActiveDraggedReziseBorder=null;
		}
	}
	
	@Override
	public void setNorthHeight(float fHeight, boolean bUseAsDiagPerc){
		/**
		 * TODO info height should be automatic. Or Info should be a list with vertical scroll bar, and constrainted to >= 1 lines.
		 */
		Vector3f v3fNorthSize = getNorthContainerSizeCopy();
//		if(v3fDialogSizeOverride==null)v3fDialogSizeOverride=getDialogMainContainer().getSize();
		
		float fNorthHeightPixels = fHeight;
		if(bUseAsDiagPerc) fNorthHeightPixels = sizePercOrPixels(fHeight, cfg.fNorthHeightPercentOfDialog);
		v3fNorthSize.y = fNorthHeightPixels;
		MiscLemurStateI.i().setSizeSafely(getNorthContainer(), v3fNorthSize, false);
	}
	
	@Override
	public Vector3f getNorthContainerSizeCopy(){
		return getNorthContainer().getSize().clone();
	}
	
	private void resizeInfoAndList(Vector3f v3fDisplacement) {
		Vector3f v3fNorth = getNorthContainerSizeCopy();
		Vector3f v3fCenter = cntrCenterMain.getSize().clone();
		
		v3fNorth.y  += -v3fDisplacement.y;
		v3fCenter.y +=  v3fDisplacement.y;
		
//		//forbid too little
//		float fMinHeight=30; 
//		if(v3fNorth.y<fMinHeight)return;
//		if(v3fCenter.y<fMinHeight)return;
		//TODO check to prevent impossible layout? use resetNorthHeight() here too?
		
		MiscLemurStateI.i().setSizeSafely(getNorthContainer(), v3fNorth, true);
		MiscLemurStateI.i().setSizeSafely(cntrCenterMain, v3fCenter, true); //TODO can be ignored this one
		
		requestRefreshUpdateList();
	}

	private Button prepareResizeBorder(final Button btnExisting, final Position edge) {
		if(btnExisting!=null){
			if(!btgBugFixReinitBordersByRecreatingThem.b()){
				CursorEventControl.removeListenersFromSpatial(btnExisting, DialogMouseCursorListenerI.i());
				CursorEventControl.addListenersToSpatial(btnExisting, DialogMouseCursorListenerI.i());
				/**
				 * this does not work very well...
				 */
				Boolean b=btnExisting.getUserData(EUserData.bHoverOverIsWorking.s());
				/**
				 * will recreate anyway while hover over hasnt worked yet with this button instance.
				 */
				if(b!=null && b){
					return btnExisting;
				}
			}
			
//			if(edge!=null)
			abtnBorderList.remove(btnExisting);
		}
			
//			CursorEventControl.removeListenersFromSpatial(btnExisting, DialogMouseCursorListenerI.i());
////			getDialogMainContainer().addChild(new Panel(), edge);
//			getDialogMainContainer().removeChild(btnExisting);
//			CallQueueI.i().addCall(new CallableX(this,100) {
//				@Override
//				public Boolean call() {
//					getDialogMainContainer().addChild(btnExisting, edge);
//					CursorEventControl.addListenersToSpatial(btnExisting, DialogMouseCursorListenerI.i());
//					return true;
//				}
//			}.updateTimeMilisNow());
//			return btnExisting;
//		}else{
			Button btnBorder=new Button("", new ElementId(DialogStyleElementId.ResizeBorder.s()), getDiagStyle());
			//Font TRICK(seems not necessary?): 
//			btnBorder.setFontSize(0.1f); //this trick will let us set it with any dot size!
			//btnBorder.getFontSize()
			
			MiscJmeI.i().setUserDataPSH(btnBorder, this); //if a border is clicked, the bugfixer that recreates it will make the old border object have no parent. Its parentest would have a reference to the dialog, but now it is alone, so such reference must be on it.
			
			EffectFocusData efd=new EffectFocusData();
			efd.colorOriginal=((QuadBackgroundComponent)btnBorder.getBackground()).getColor().clone();
			efd.colorDim = efd.colorOriginal.clone().mult(0.5f);
			efd.colorDim.a=efd.colorOriginal.a;
			MiscJmeI.i().setUserDataPSH(btnBorder, efd);
//			btnBorder.setUserData(EffectFocusData.class.getName(), efd);
			
			CursorEventControl.addListenersToSpatial(btnBorder, DialogMouseCursorListenerI.i());
			DialogMouseCursorListenerI.i().addMouseCursorHighlightEffects(btnBorder);
			
			abtnBorderList.add(btnBorder);
			String strName="Dialog_Resizer_";
			if(edge==null){
				strName+=btnBorder.getName();
			}else{
				strName+="Border"+edge.toString();
				getDialogMainContainer().addChild(btnBorder, edge); //this actually replaces the current at that border
			}
			btnBorder.setName(strName);
			
			efDummy = LemurDialogManagerI.i().setupSimpleEffect(btnBorder, EEffectId.FocusLost.s(), efFocusLost, efDummy);
//			if(!efDummy.getChannel().equals(efFocusLost.getChannel())){
//				throw new PrerequisitesNotMetException("both should be on the same channel", efDummy, efFocusLost, btnBorder, this);
//			}
//			btnBorder.addEffect(EEffectId.FocusLost.s(), (Effect)efFocusLost);
//			btnBorder.addEffect(EEffectId.Dummy.s(), efDummy);
			
			return btnBorder;
//		}
	}
	
	public static class EffectFocusData{
		ColorRGBA	colorDim;
		ColorRGBA colorOriginal;
	}
	
	StringCmdField scfFixReinitDialogBorders = new StringCmdField(this,null,"it may require a few tries actually...")
		.setCallerAssigned(new CallableX(this){ //,1000) {
			@Override
			public Boolean call() {
				if(!isEnabled()){
					GlobalCommandsDelegatorI.i().dumpWarnEntry("not enabled: "+LemurDialogStateAbs.this.getUniqueId(), this, LemurDialogStateAbs.this);
					return true; //simple skipper
				}
				
//				reinitBorders(false);
				reinitBorders();
				return true;
			}
		});
//	private boolean	bReinitBordersAfterThicknessChange;
	
//	IntLongVarField ilvBorderThickness = new IntLongVarField(this, 3, "").setMinMax(1L, 20L)
//		.setCallerAssigned(new CallableX(this) {
//			@Override
//			public Boolean call() {
//				setBordersThickness(ilvBorderThickness.getInt());
//				return true;
//			}
//		});
//	private boolean	bAllowUpdateLogicalState;
	
//	private static IntLongVarField ilvGlobalBorderThickness = new IntLongVarField(LemurDialogStateAbs.class, 3, "")
//		.setMinMax(1L, 20L)
//		.setCallerAssigned(new CallableX(LemurDialogStateAbs.class,100) {
//			@Override
//			public Boolean call() {
//				LemurDialogStateAbs diag = LemurDialogCS.this.getOwner();
//				if(diag==null)return false; //to retry until the dialog is found
//				
//				diag.setBordersThickness(ilvGlobalBorderThickness.getInt());
//				return true;
//			}
//		});
	
	public static class LmrDiagCS extends DiagCS<LemurDialogStateAbs> {
		public LmrDiagCS() {super();}//required by savable
		public LmrDiagCS(LemurDialogStateAbs owner) {super(owner);}
		
//		/**
//		 * This console variable will be saved at console cfg file and also with the dialog JME savable. 
//		 */
//		private IntLongVarField ilvBorderThickness;
		
		@Override
		protected void initialize(){
			super.initialize();
			
//			IReflexFillCfg irfcfgOwner = isThisInstanceALoadedTmp() ? null : this;
//			ilvBorderThickness = new IntLongVarField(irfcfgOwner, 3, "")
//				.setMinMax(1L, 20L)
//				.setCallerAssigned(new CallableX(this,100) {
//					@Override
//					public Boolean call() {
//						if(LmrDiagCS.this.isThisInstanceALoadedTmp())return true; //skipper
//						
//						LemurDialogStateAbs diag = LmrDiagCS.this.getOwner();
//						if(diag==null)return false; //to retry until the dialog is found
//						
//						diag.setBordersThickness(ilvBorderThickness.getInt());
//						return true;
//					}
//				});
			
		}
		
		@Override
		public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
			if(fld.getDeclaringClass()!=LmrDiagCS.class)return super.getFieldValue(fld);
			return fld.get(this);
		}
		@Override
		public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
			if(fld.getDeclaringClass()!=LmrDiagCS.class){super.setFieldValue(fld,value);return;}
			fld.set(this,value);
		}
	}
	
	public void setBordersThickness(int iPixels){
		for(Button btn:abtnBorderList){
			MiscLemurStateI.i().setSizeSafely(btn, iPixels, iPixels, true);
		}
//		CallQueueI.i().addCall(callerReinitBordersAfterThicknessChange.updateTimeMilisNow()); 
//		bReinitBordersAfterThicknessChange=true;
	}

//	public boolean toggleMaximized() {
//		
//	}
//	public void setMaximized(boolean maximized) {
//		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException("method not implemented yet");
//	}

	@Override
	protected ListBox<DialogListEntryData<T>> getMainList() {
		return (ListBox<DialogListEntryData<T>>)super.getMainList();
	}
	
	protected CellRendererDialogEntry<T> getCellRenderer(){
		if(cr==null)cr = new CellRendererDialogEntry<T>(getDiagStyle(),this);// bOptionSelectionMode),
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
		this.iFinalEntryHeightPixels = (int)FastMath.ceil(
			getEntryHeightPixels() * fdvEntryHeightMultiplier.f());
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
//		MiscLemurStateI.i().setSizeSafely(lblTextInfo,-1,100)
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
			
//			iVisibleRows = (int) (v3fEntryListSizeIni.y/getFinalEntryHeightPixels());
			iVisibleRows = (int) (getMainList().getSize().y/getFinalEntryHeightPixels());
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
	
	protected ArrayList<CellDialogEntry<T>> getVisibleCellEntries(){
		ArrayList<CellDialogEntry<T>> acell = new ArrayList<CellDialogEntry<T>>();
		
		GridPanel gp = getMainList().getGridPanel();
		for(int iC=0;iC<gp.getVisibleColumns();iC++){
			for(int iR=0;iR<gp.getVisibleRows();iR++){
				Panel pnl = gp.getCell(gp.getRow()+iR, gp.getColumn()+iC);
				if (pnl instanceof CellDialogEntry) {
					CellDialogEntry<T> cell = (CellDialogEntry<T>) pnl;
					acell.add(cell);
				}
			}
		}
		
		return acell;
	}
	
	protected boolean simpleUpdateVisibleCells(float tpf){
		for(CellDialogEntry<T> cell:getVisibleCellEntries()){
			cell.simpleUpdateThisCell(tpf);
		}
		return true;
	}
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		// abtnBorderList.get(0).setPreferredSize(new Vector3f(3,3,1))
		// MiscLemurStateI.i().setSizeSafely(abtnBorderList.get(2), 3, 3)
		
		if(DebugI.i().isKeyEnabled(EDebugKey.FillKeyValueHashmap)){
			for(Button btn:abtnBorderList){
				DebugI.i().putKeyValue(getUniqueId()+":"+btn.getName()+":size", btn.getSize());
				DebugI.i().putKeyValue(getUniqueId()+":"+btn.getName()+":psize", btn.getPreferredSize());
			}
		}
		
		if(!simpleUpdateVisibleCells(tpf))return false;
		
		if(bRefreshScroll)autoScroll();
		
//		updateSelectEntryRequested();
		
		if(!getInputText().startsWith(getUserEnterCustomValueToken())){
			if(!getInputText().equalsIgnoreCase(getLastFilter())){
	//			setLastFilter(getInputText());
				applyListKeyFilter();
				requestRefreshUpdateList();	//updateList();				
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
		
		MiscLemurStateI.i().updateBlinkListBoxSelector(getMainList());//,true);
		
//		bAllowUpdateLogicalState=MiscLemurHelpersStateI.i().validatePanelUpdate(getContainerMain());
//		if(bReinitBordersAfterThicknessChange){
//			reinitBorders(false); //false to avoid call queue recursion
//			bReinitBordersAfterThicknessChange=false;
//		}
		
		return true;
	}
	
	/**
	 * default is the class name, will look like the dialog title
	 */
	@Override
	protected void updateTextInfo(){
//		lblTextInfo.setText("DIALOG for "+this.getClass().getSimpleName());
		lblTextInfo.setText(getTextInfo());
		
		MiscLemurStateI.i().fixBitmapTextLimitsFor(lblTextInfo);
	}
	
	@Override
	public THIS setTitle(String str) {
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
							MiscLemurStateI.i().insertTextAtCaratPosition(getInputField(),
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
		BindKey bk = MiscLemurStateI.i().bindKey(getInputField(), actSimpleActions,
			strActionPerformedHelp, iKeyCode, aiKeyModifiers);
		abkList.add(bk);
		return bk;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,"showDialogKeyBinds"+getUniqueId(),null,"")){
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
	
//	@Override
//	public String getInputText() {
//		return getInputField().getText();
//	}
	
//	@Override
//	protected R setInputText(String str) {
//		getInputField().setText(str);
//		return getThis();
//	}
	
	public THIS addModalDialog(LemurDialogStateAbs<T,?> diagModal){
		diagModal.setDiagParent(this);
		hmhrChildDiagModals.put(diagModal.getUniqueId(), new HoldRestartable(this,diagModal));
		return getThis();
	}
	
	@Override
	public boolean prepareToDiscard(CompositeControl cc) {
		if(!super.prepareToDiscard(cc))return false;
		
//		for(Entry<String, LemurDialogStateAbs<T, ?>> entry:hmChildDiagModals.entrySet()){
//			entry.getValue().applyDiscardingParent();
//		}
		
		return true;
	}
	
//	public DiagModalInfo<T> getDiagModalCurrent(){
//		return dmi;
//	}

	//	public void openModalDialog(String strDialogId, DialogListEntryData<T> dataToAssignModalTo, T cmd){
	public void openModalDialog(String strDialogId, DialogListEntryData<T> dledToAssignModalTo, T cmd){
		LemurDialogStateAbs<T,?> diagModalCurrent = hmhrChildDiagModals.get(strDialogId).getRef();
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
		MiscLemurStateI.i().listboxSelectorAsUnderline(getMainList());
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
			cd().dumpDebugEntry(getUniqueId()+",SelectIndex="+i+","+dledSelectRequested.toString());
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
		
//		DialogMouseCursorListenerI.i().clearLastButtonHoverIn();
		
//		iSel = selectionModel.getSelection();
		cd().dumpDebugEntry(getUniqueId()+":"
			+"SelectedEntry="+iSel+","
			+"SliderValue="+MiscI.i().fmtFloat(getMainList().getSlider().getModel().getValue()));
		return iSel;
//		return iSel==null?-1:iSel;
	}

	public abstract boolean execTextDoubleClickActionFor(DialogListEntryData<T> dled);

	public abstract boolean execActionFor(EMouseCursorButton e, Spatial capture);

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
		CallQueueI.i().addCall(new CallableX(this) {
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
		
		for(CellDialogEntry<T> cell:getVisibleCellEntries()){
			MiscLemurStateI.i().setScaleXY(cell, fMinScale, 1f);
		}
//		GridPanel gp = getMainList().getGridPanel();
//		for(int iC=0;iC<gp.getVisibleColumns();iC++){
//			for(int iR=0;iR<gp.getVisibleRows();iR++){
//				Panel pnl = gp.getCell(iR, iC);
//				if(pnl!=null)MiscLemurHelpersStateI.i().setScaleXY(pnl, fMinScale, 1f);
//			}
//		}
		
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
		for(CellDialogEntry<T> cell:getVisibleCellEntries()){
//		for(int iC=0;iC<gp.getVisibleColumns();iC++){
//			for(int iR=0;iR<gp.getVisibleRows();iR++){
//				Panel pnl = gp.getCell(iR, iC);
				iCount++;
				
				if(cell!=null){
					if(Float.compare(cell.getLocalScale().x,1f)==0){
						continue;
					}
					
					fLastCalculatedEntryScale=updateEffectOnEntry(cell,bGrow);
					if(fLastCalculatedEntryScale<0.33f){
						break;
					}
//					iCountConcurrent++;
					
//					fScale-=0.33f;
//					if(fScale<0f)fScale=fMinScale;
					
//					if(iCountConcurrent==iMaxConcurrent)break; //to update one entry step per frame
//				}
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
		
		MiscLemurStateI.i().setScaleXY(spt, fScale, 1f);
		
		return fScale;
	}

	public Integer getFinalEntryHeightPixels() {
		return iFinalEntryHeightPixels;
	}
	
	@Override
	protected <N extends Node> void lineWrapDisableForChildrenOf(N node) {
		ListBox<String> lstbx = (ListBox<String>)node;
		MiscLemurStateI.i().lineWrapDisableForListboxEntries(lstbx);
	}
	
	public T getCmdDummy() {
		return (T) MiscLemurStateI.i().getCmdDummy();
	}
	
	enum EEffectId{
		FocusLost,
		Dummy,
		;
		public String s(){return this.toString();}
	}
	enum EEffectChannel{
		ChannelFocusIndicator,
		;
		public String s(){return this.toString();}
	}
	
	Effect<Button> efFocusLost = new AbstractEffect<Button>(EEffectChannel.ChannelFocusIndicator.s()) {
		@Override
		public Animation create(final Button target, final EffectInfo existing) {
			final GuiComponent gcBgChk = target.getBackground();
			if(!QuadBackgroundComponent.class.isInstance(gcBgChk)){
				MsgI.i().devWarn("background type not supported for this effect", gcBgChk, target, existing, this);
				return null;
			}
			
			return new Animation() {
				QuadBackgroundComponent gcBg = (QuadBackgroundComponent)gcBgChk;
				EffectFocusData efd = MiscJmeI.i().getUserDataPSH(target, EffectFocusData.class);
//				ColorRGBA colorBkp = gcBg.getColor();
				boolean bApplied=false;
				@Override	public void cancel() {
//					gcBg.setColor(colorBkp);
					gcBg.setColor(efd.colorOriginal);
				}
				@Override	public boolean animate(double tpf) {
					if(!bApplied){
	//					if(existing!=null && existing.getAnimation()==this)return true;
//						ColorRGBA colorDim = colorBkp.clone();
//						float fA=colorDim.a;
//						colorDim.multLocal(0.5f);
//						colorDim.a=fA; //keep alpha
//						
//						gcBg.setColor(colorDim);
						gcBg.setColor(efd.colorDim);
						bApplied=true;
					}
					return true;
				}
			};
		}
	};
	DummyEffect efDummy; // = new DummyEffect(efFocusLost.getChannel());

	@Override
	public void focusGained() {
		requestRefreshUpdateList();
//		if(quaBkpMain!=null){
//			getContainerMain().setLocalRotation(quaBkpMain);
//		}
//		bugFix(null, null, btgBugFixAutoReinitBorderOnFocusGained);
		WorkAroundI.i().bugFix(btgBugFixOnFocusAutoReinitBorder);
		applyBorderFocusEffect(true);
//		changeResizeBorderColor(ColorRGBA.Cyan);
	}
	
//	CallableX callerReinitBordersAfterThicknessChange = new CallableX(this,1000) {
//		@Override
//		public Boolean call() {
//			reinitBorders(true);
//			return true;
//		}
//	};
//	private void changeResizeBorderColor(ColorRGBA c){
////		if(true)return; //TODO temporary, trying lemur effects!
//		for(Button btn:abtnBorderList){
//			if(btn==btnCurrentlyActiveDraggedReziseBorder)continue;
//			MiscLemurStateI.i().setOverrideBackgroundColor(btn, c);
//		}
//	}
	
	private void applyBorderFocusEffect(boolean bGained){
		for(Button btn:abtnBorderList){
			if(btn==btnCurrentlyActiveDraggedReziseBorder)continue;
			if(bGained){
//				if(btn.hasEffect("FocusGained"))
//				btn.runEffect(EEffectId.Dummy.s()); //to just cancel the lost and restore original
				btn.runEffect(efDummy.getId()); //to just cancel the lost and restore original
			}else{
				btn.runEffect(EEffectId.FocusLost.s());
			}
		}
	}
	
	@Override
	public void focusLost() {
		applyBorderFocusEffect(false);
//		changeResizeBorderColor(ColorRGBA.Blue);
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1,1), new Vector3f(1,1,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(1,0,1), new Vector3f(0,1,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1f,1f), new Vector3f(1,0,0));
//		getContainerMain().getLocalRotation().lookAt(new Vector3f(0,1,1), new Vector3f(1,0,0));
	}
	
	@Override
	protected void restoreDefaultPositionSize() {
		super.restoreDefaultPositionSize();
		
		if(getDialogMainContainer()!=null){
			setPositionSize(cfg.getIniPos(), cfg.getIniSize());
//			getDialogMainContainer().setLocalTranslation(cfg.getIniPos());
//			getDialogMainContainer().setPreferredSize(cfg.getIniSize());
		}
	}
	
	@Override
	protected void setPositionSize(Vector3f v3fPos, Vector3f v3fSize) {
		MiscLemurStateI.i().setPositionSafely(getDialogMainContainer(), v3fPos);
//		v3fPos.setZ(getDialogMainContainer().getLocalTranslation().getZ());
//		getDialogMainContainer().setLocalTranslation(v3fPos);
		
//		getDialogMainContainer().setPreferredSize(v3fSize);
		MiscLemurStateI.i().setSizeSafely(getDialogMainContainer(), v3fSize, true);
		
		requestRefreshUpdateList();
	}

//	public ArrayList<LemurDialogGUIStateAbs<T,?>> getParentsDialogList(LemurFocusHelperStateI.CompositeControl cc) {
//	cc.assertSelfNotNull();
	/**
	 * 
	 * @return a new list of parent's dialogs
	 */
	public ArrayList<LemurDialogStateAbs<T,?>> getParentsDialogList() {
		ArrayList<LemurDialogStateAbs<T,?>> adiag = new ArrayList<LemurDialogStateAbs<T,?>>();
		
		LemurDialogStateAbs<T,?> diag = (LemurDialogStateAbs<T,?>)super.getParentDialog();
		while(diag!=null){
			adiag.add(diag);
			diag = (LemurDialogStateAbs<T,?>)diag.getParentDialog();
		}
		
		return adiag;
	}
	
	BugFixBoolTogglerCmdField btgBugFixReinitBordersByRecreatingThem = new BugFixBoolTogglerCmdField(this,false)
		.setHelp("this can cause minor problems too concerning mouse cursor clicks on borders, but it is more granted to make the borders work better overall");
	
	BugFixBoolTogglerCmdField btgBugFixOnFocusAutoReinitBorder = new BugFixBoolTogglerCmdField(this,false)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				if(!isEnabled())return true;//this is just a successful skipper
//				if(!isEnabled()){
//					this.setQuietOnFail(true);
//					return false;
//				}
				
//				this.setQuietOnFail(false);
				reinitBorders();//false);
				return true;
			}
		});
//	@Override
//	public <BFR> BFR bugFix(Class<BFR> clReturnType,
//			BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId,
//			Object... aobjCustomParams
//	) {
//		if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
//		
//		boolean bFixed = false;
//		Object objRet = null;
//		
//		if(btgBugFixAutoReinitBorderOnFocusGained.isEqualToAndEnabled(btgBugFixId)){
////			Spatial spt = MiscI.i().getParamFromArray(Spatial.class, aobjCustomParams, 0);
////			Float fZ = MiscI.i().getParamFromArray(Float.class, aobjCustomParams, 1);
//			
//			reinitBorders();
//			
//			bFixed=true;
//		}
//		
//		return MiscI.i().bugFixRet(clReturnType,bFixed, objRet, aobjCustomParams);
//	}
//	@Override		public String s(){return this.toString();}

	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurDialogStateAbs.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurDialogStateAbs.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
	
	@Override
	public void reLoad() {
		load(LmrDiagCS.class);
	}
	
	@Override
	public void requestRestart() {
		// do not allow restart with childs enabled, this also grants many consistencies
		for(HoldRestartable<LemurDialogStateAbs<T, ?>> hr:hmhrChildDiagModals.values()){
			if(hr.getRef().isEnabled()){
				AudioUII.i().play(EAudio.Failure);
				return;
			}
		}
		
		super.requestRestart();
	}
}
