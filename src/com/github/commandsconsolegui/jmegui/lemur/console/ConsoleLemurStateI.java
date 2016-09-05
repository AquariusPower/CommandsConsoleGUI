/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower>
	
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

package com.github.commandsconsolegui.jmegui.lemur.console;

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.console.GlobalConsoleGUII;
import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.ConditionalStateManagerI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.console.ConsoleStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jmegui.lemur.console.MiscLemurHelpersStateI.BindKey;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.MiscI;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.LineWrapMode;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.RangedValueModel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;

/**
 * Here is the specific code that links the JME console state with Lemur GUI.
 * 
 * TODO complete specific code migration from ConsoleGuiStateAbs
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower>
 *
 */
public class ConsoleLemurStateI<T extends Command<Button>> extends ConsoleStateAbs<T,ConsoleLemurStateI<T>>{
	private static ConsoleLemurStateI instance=null;//new ConsoleLemurStateI();
	public static ConsoleLemurStateI i(){
		if(instance==null)instance=new ConsoleLemurStateI();
		return instance;
	}
	
	public static final class CompositeControl extends CompositeControlAbs<ConsoleLemurStateI>{
		private CompositeControl(ConsoleLemurStateI casm){super(casm);};
	}
	private CompositeControl ccSelf = new CompositeControl(this);
	
	StringVarField svfBackgroundHexaColorRGBA = new StringVarField(this,"","XXXXXXXX ex.: 'FF12BC4A' Red Green Blue Alpha");
//	private ConsoleMouseCursorListenerI consoleCursorListener;
	private Button	btnCopy;
	private Button	btnPaste;
	private Button	btnClipboardShow;
	private Button	btnCut;
	private Label	lblStats;
	private ArrayList<BindKey>	abkList = new ArrayList<BindKey>();
	
	public final StringCmdField CMD_SHOW_BINDS = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	
	private ColorRGBA	colorConsoleStyleBackground;
	private TextEntryComponent	tecInputField;
	private KeyActionListener	actSimpleActions;
	
	public ConsoleLemurStateI(){
		setDumpEntriesSlowedQueue(new VersionedList<String>());
		setDumpEntries(new VersionedList<String>());
		setPrefixCmdWithIdToo(true);
	}
	
//	@Override
//	public void initializePre() {
//		super.initializePre();
//		
//		GuiGlobals.initialize(sapp);
//		BaseStyles.loadGlassStyle(); //do not mess with default user styles: GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
//		
//		addStyle(BaseStyles.GLASS);
//		addStyle(Styles.ROOT_STYLE);
//	}

	@Override
	protected boolean initAttempt() {
		GuiGlobals.initialize(GlobalAppRefI.i());
		
		BaseStyles.loadGlassStyle(); //do not mess with default user styles: GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
		
		addStyle(BaseStyles.GLASS);
		addStyle(Styles.ROOT_STYLE);
		
//		initializationCompleted();
		
		return super.initAttempt();
	}
	
//	public static class CfgParm implements ICfgParm{
//		String strUIId;
//		boolean bIgnorePrefixAndSuffix;
//		int iToggleConsoleKey;
//		Node nodeGUI;
//		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix,
//				int iToggleConsoleKey, Node nodeGUI) {
//			super();
//			this.strUIId = strUIId;
//			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
//			this.iToggleConsoleKey = iToggleConsoleKey;
//			this.nodeGUI = nodeGUI;
//		}
//	}
	public static class CfgParm extends ConsoleStateAbs.CfgParm{
		public CfgParm(
			String strUIId, 
			int iToggleConsoleKey
		) {
			super(
				strUIId==null ? strUIId=ConsoleLemurStateI.class.getSimpleName() : strUIId, 
				iToggleConsoleKey);
			super.setInitiallyEnabled(true); // the console must be initially enabled to startup properly
		}
	}
	@Override
	public ConsoleLemurStateI<T> configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
		
		// for restarting functionality
//		GlobalConsoleGuiI.iGlobal().validate();
		GlobalConsoleGUII.iGlobal().set(this);
		
		super.configure(cfg);
		
//		GuiGlobals.initialize(GlobalAppRefI.i());
		
		// misc cfg
		if(!MiscLemurHelpersStateI.i().isConfigured()){ //in case of restarting the console
			MiscLemurHelpersStateI.i().configure(new MiscLemurHelpersStateI.CfgParm());
		}
		
//		LemurMiscHelpersStateI.i().initialize(app().getStateManager(), sapp);
//		if(!app().getStateManager().attach(LemurMiscHelpersStateI.i())){
//			throw new NullPointerException("already attached state "+LemurMiscHelpersStateI.class.getName());
//		}
		if(!LemurFocusHelperStateI.i().isConfigured()){
			LemurFocusHelperStateI.i().configure(new LemurFocusHelperStateI.CfgParm(null));
		}
		
		if(!ConditionalStateManagerI.i().isConfigured()){
			ConditionalStateManagerI.i().configure(GlobalAppRefI.i());
		}
		
		if(!ConsoleMouseCursorListenerI.i().isConfigured()){
			ConsoleMouseCursorListenerI.i().configure();
		}
		
		if(!ConsoleVarsDialogStateI.i().isConfigured()){
			ConsoleVarsDialogStateI.i().configure(new ConsoleVarsDialogStateI.CfgParm(
				0.9f, 0.9f, 0.1f, 2.0f));
		}
		
		return storeCfgAndReturnSelf(icfg);
	}
	
//	public void ConsoleGUILemurState(int iOpenConsoleHotKey, ConsoleCommands cc, Application app) {
//		super(iOpenConsoleHotKey, cc);
//		app.getStateManager().attach(LemurGuiMisc.i());
//		LemurGuiMisc.i().setConsoleCommands(cc);
//	}
	
//	@Override
//	public void initialize(AppStateManager stateManager, Application app) {
//		super.initialize(stateManager, app);
//		app.getStateManager().attach(LemurGuiMisc.i());
//	}
	
	@Override
	public void prepareStyle(){
		super.prepareStyle();
		
		Styles styles = GuiGlobals.getInstance().getStyles();
		
		if(colorConsoleStyleBackground==null){
			colorConsoleStyleBackground = ColorRGBA.Blue.clone();
			colorConsoleStyleBackground.b=0.25f;
			colorConsoleStyleBackground.a=1f; //0.75f;
		}
		
		if(svfBackgroundHexaColorRGBA.getStringValue().isEmpty()){
			String strHexa = Integer.toHexString(colorConsoleStyleBackground.asIntRGBA());
			strHexa = String.format("%8s", strHexa).replace(" ", "0").toUpperCase();
			svfBackgroundHexaColorRGBA.setObjectRawValue(strHexa);
		}else{
			try{
				int i = Integer.parseInt(svfBackgroundHexaColorRGBA.getStringValue(),16);//hexa string
				colorConsoleStyleBackground.fromIntRGBA(i);
			}catch(IllegalArgumentException ex){
				cd().dumpExceptionEntry(ex);
			}
		}
		
		ColorRGBA clBg;
		
		Attributes attrs;
		attrs = styles.getSelector(STYLE_CONSOLE); // this also creates the style
		attrs.set("fontSize", 16);
		attrs.set("color", ColorRGBA.White.clone());
//		clBg = ColorRGBA.Blue.clone();clBg.b=0.25f;clBg.a=0.75f;
		clBg = colorConsoleStyleBackground;
		attrs.set("background", new QuadBackgroundComponent(clBg));
		attrs.set("font", getFont());
		
//			attrs = styles.getSelector("grid", STYLE_CONSOLE);
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,1,0,1)));
		
		attrs = styles.getSelector(Button.ELEMENT_ID, STYLE_CONSOLE);
//		attrs.set("color", new ColorRGBA(0,1,0.5f,1));
//		clBg = new ColorRGBA(0,0,0.125f,1);
		attrs.set("color", ColorRGBA.Cyan.clone());
//		clBg = new ColorRGBA(0,0.25f,0,1);
		clBg = new ColorRGBA(0,0.25f,0,0.75f);
		attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
		attrs = styles.getSelector(TextField.ELEMENT_ID, STYLE_CONSOLE);
		attrs.set("color", new ColorRGBA(0.75f,1,1,1));
		clBg = new ColorRGBA(0.15f, 0.25f, 0, 1);
		attrs.set(TextField.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
//		lstbx.getElementId().child(ListBox.SELECTOR_ID);
		attrs = styles.getSelector(ListBox.ELEMENT_ID, ListBox.SELECTOR_ID, STYLE_CONSOLE);
//			attrs = styles.getSelector("list", "selector", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Red.clone());
		clBg = ColorRGBA.Yellow.clone();clBg.a=0.25f;
		attrs.set(ListBox.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
//		attrs = styles.getSelector(ListBox.ELEMENT_ID, ListBox.ITEMS_ID, STYLE_CONSOLE);
//		clBg = new ColorRGBA(0,0,0,0);
//		attrs.set(ListBox.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));

//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
//
//			attrs = styles.getSelector("slider", "button", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Yellow.clone());
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
//			
//			attrs = styles.getSelector("grid", "button", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Yellow.clone());
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
		
//		String strAllChars="W";
//		fMonofontCharWidth = fontWidth(strAllChars,STYLE_CONSOLE);
		
//		bConsoleStyleCreated=true;
		
//		updateFontStuff();
	}
	
	@Override
	public void initializeOnlyTheUI() {
//		prepareStyle();
		
//		consoleCursorListener = new ConsoleMouseCursorListenerI();
//		consoleCursorListener.configure();
		
		// auto complete hint
		setHintList(new VersionedList<String>());
		super.setHintBox(new ListBox<String>(new VersionedList<String>(),getStyle()));
		getHintBox().setModel(getHintList());
		CursorEventControl.addListenersToSpatial(getHintBox(), ConsoleMouseCursorListenerI.i());
		
		// main container
		setContainerMain(new Container(new BorderLayout(), getStyle()));
		MiscLemurHelpersStateI.i().setGrantedSize(getContainerConsole(), getConsoleSizeCopy(), true);
		
		/**
		 * TOP ELEMENT =================================================================
		 */
		super.setStatsAndControls(new Container(getStyle()));
//		getContainerStatsAndControls().setName("ConsoleStats");
		getContainerConsole().addChild(getContainerStatsAndControls(), BorderLayout.Position.North);
		
		// console stats
		lblStats = new Label("Console stats.",getStyle());
		lblStats.setColor(new ColorRGBA(1,1,0.5f,1));
		MiscLemurHelpersStateI.i().setGrantedSize(lblStats, getConsoleSizeCopy().x*0.75f, 1f, false); //TODO y=1f so it will expand?
		getContainerStatsAndControls().addChild(lblStats,0,0);
		
		// buttons
		ArrayList<Button> abu = new ArrayList<Button>();
		int iButtonIndex=0;
		btnClipboardShow = new Button("ShwClpbrd",getStyle());
		abu.add(btnClipboardShow);
		
		btnCopy = new Button("Copy",getStyle());
		abu.add(btnCopy);
		
		btnPaste = new Button("Paste",getStyle());
		abu.add(btnPaste);
		
		btnCut = new Button("Cut",getStyle());
		abu.add(btnCut);
		
		for(Button btn:abu){
			btn.setTextHAlignment(HAlignment.Center);
			//TODO why buttons do not obbey this preferred size 50,1,0?
			btn.addClickCommands(new ButtonClick());
			DialogMouseCursorListenerI.i().addDefaultCommands(btn);
			getContainerStatsAndControls().addChild(btn,0,++iButtonIndex);
		}
		
		
		/**
		 * CENTER ELEMENT (dump entries area) ===========================================
		 */
		setLstbxDumpArea(new ListBox<String>(new VersionedList<String>(),getStyle()));
    CursorEventControl.addListenersToSpatial(getDumpArea(), ConsoleMouseCursorListenerI.i());
		Vector3f v3fLstbxSize = getConsoleSizeCopy();
//		v3fLstbxSize.x/=2;
//		v3fLstbxSize.y/=2;
		getDumpArea().setSize(v3fLstbxSize); // no need to update fLstbxHeight, will be automatic
		//TODO not working? lstbx.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		
		/**
		 * The existance of at least one entry is very important to help on initialization.
		 * Actually to determine the listbox entry height.
		 */
		if(getDumpEntries().isEmpty())getDumpEntries().add(""+cd().getCommentPrefix()+" Initializing console.");
		
		getDumpArea().setModel((VersionedList<String>)getDumpEntries());
		getDumpArea().setVisibleItems(getShowRowsAmount());
//		lstbx.getGridPanel().setVisibleSize(getShowRowsAmount(),1);
		getContainerConsole().addChild(getDumpArea(), BorderLayout.Position.Center);
		
		setSliderDumpArea(getDumpArea().getSlider());
		
		/**
		 * BOTTOM ELEMENT =================================================================
		 */
		// input
		super.setInputField(new TextField(""+cd().getCommandPrefix(),getStyle()));
    CursorEventControl.addListenersToSpatial(getInputField(), ConsoleMouseCursorListenerI.i());
		LemurFocusHelperStateI.i().addFocusChangeListener(getInputField());
//		fInputHeight = MiscJmeI.i().retrieveBitmapTextFor(getInputField()).getLineHeight();
		getContainerConsole().addChild( getInputField(), BorderLayout.Position.South );
		
		super.initializeOnlyTheUI();
	}
	
	@Override
	public VersionedList<String> getHintList(){
		return (VersionedList<String>)super.getHintList();
	}
	
	@Override
	protected boolean enableAttempt() {
		if(!super.enableAttempt())return false;
		
		LemurFocusHelperStateI.i().requestFocus(getInputField());
//	commonOnEnableDisable();
		
		if(isFullyInitialized()){
			AudioUII.i().play(EAudio.OpenConsole);
		}
		
		return true;
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!super.disableAttempt())return false;
		
		if(isFullyInitialized()){ //super sets this..
			AudioUII.i().play(EAudio.CloseConsole);
		}
		
		closeHint();
		LemurFocusHelperStateI.i().removeFocusableFromList(getInputField());
//		commonOnEnableDisable();
		
		return true;
	};
	
//	@Override
//	public void onDisable() {
//		super.onDisable();
//		
////		getContainerConsole().removeFromParent();
//		closeHint();
//		
//		commonOnEnableDisable();
//	}
	
//	private void commonOnEnableDisable(){
//		updateOverrideInputFocus();
////		if(LemurFocusHelperStateI.i().isFocusRequesterListEmpty()){
////			GuiGlobals.getInstance().setCursorEventsEnabled(this.bEnabled);
////		}
//	}
	
//	@Override
//	public void setEnabled(boolean bEnabled) {
//		super.setEnabled(bEnabled);
//		
//		if(this.bEnabled){
//			app().getGuiNode().attachChild(getContainerConsole());
////			GuiGlobals.getInstance().requestFocus(getInputField());
//		}else{
//			getContainerConsole().removeFromParent();
//			closeHint();
////			GuiGlobals.getInstance().requestFocus(null);
//		}
//		
//		updateOverrideInputFocus();
//		
//		GuiGlobals.getInstance().setCursorEventsEnabled(this.bEnabled);
//	}
	
	@Override
	public void scrollHintToIndex(int i){
		int iVisibleCount = getHintBox().getVisibleItems();
		
		RangedValueModel model = getHintBox().getSlider().getModel();
		
		int iVisibleMinIndex = (int)(model.getMaximum() -model.getValue());
		
		int iVisibleMaxIndex = iVisibleMinIndex + iVisibleCount;
		Integer iScrollMinIndexTo = null;
		if(i < iVisibleMinIndex){
			iScrollMinIndexTo = i;
		}else
		if(i >= iVisibleMaxIndex){
			iScrollMinIndexTo = i -iVisibleCount +1;
		}
		
		if(iScrollMinIndexTo!=null){
			double d = model.getMaximum();
			d -= iScrollMinIndexTo;
			if(d<0)d=0;
			model.setValue(d);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ListBox<String> getHintBox() {
		return (ListBox<String>)super.getHintBox();
	}
	
	@SuppressWarnings("unchecked")
	public ListBox<String> getDumpArea() {
		return (ListBox<String>)super.getLstbxDumpArea();
	}
	
	@Override
	protected TextField getInputField(){
		return (TextField)super.getInputField();
	}
	
	@Override
	public void clearHintSelection() {
		getHintBox().getSelectionModel().setSelection(-1);
	}

	@Override
	public Integer getHintIndex() {
		return getHintBox().getSelectionModel().getSelection();
	}

	@Override
	public ConsoleLemurStateI setHintIndex(Integer i) {
		getHintBox().getSelectionModel().setSelection(i);
		return this;
	}
	
	@Override
	public void lineWrapDisableForChildrenOf(Node node){
		@SuppressWarnings("unchecked")
		ListBox<String> lstbx = (ListBox<String>)node;
		
		GridPanel gp = lstbx.getGridPanel();
		for(Spatial spt:gp.getChildren()){
			if(spt instanceof Button){
				MiscJmeI.i().retrieveBitmapTextFor((Button)spt).setLineWrapMode(LineWrapMode.NoWrap);
			}
		}
	}

	@Override
	public ConsoleLemurStateI setHintBoxSize(Vector3f v3fBoxSizeXY, Integer iVisibleLines) {
		MiscLemurHelpersStateI.i().setGrantedSize(getHintBox(), v3fBoxSizeXY, false);
		getHintBox().setVisibleItems(iVisibleLines);
		return this;
	}
	
	private void setInputFieldTextEntryComponent(TextEntryComponent source){
		if(this.tecInputField!=null){
			if(!this.tecInputField.equals(source)){
				throw new NullPointerException("input field changed? support is required for this...");
			}
		}
		this.tecInputField = source;
	}
	
	@Override
	protected boolean mapKeysForInputField(){
		// simple actions
		actSimpleActions = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				setInputFieldTextEntryComponent(source);
//				LemurMiscHelpersStateI.i().initializeSpecialKeyListeners(source);
				
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
//				boolean bShift = key.hasModifier(0x01);
//				boolean bAlt = key.hasModifier(0x001);
//				case KeyInput.KEY_INSERT: //shift+ins paste
					//TODO ? case KeyInput.KEY_INSERT: //ctrl+ins copy
					//TODO ? case KeyInput.KEY_DELETE: //shift+del cut
				
				switch(key.getKeyCode()){
//					case KeyInput.KEY_B: 
//						if(bControl)cc.iCopyFrom = getDumpAreaSelectedIndex();
//						break;
					case KeyInput.KEY_C: 
						if(bControl)cd().editCopyOrCut(false,false,false);
						break;
					case KeyInput.KEY_ESCAPE: 
						setEnabledRequest(false);
						break;
					case KeyInput.KEY_V: 
						if(isKeyShiftIsPressed()){
							if(bControl)cd().showClipboard();
						}else{
							if(bControl)editPasteFromClipBoard();
						}
						break;
					case KeyInput.KEY_X: 
						if(bControl)cd().editCopyOrCut(false,true,false);
						break;
					case KeyInput.KEY_NUMPADENTER:
					case KeyInput.KEY_RETURN:
						actionSubmit(getInputText());
						break;
					case KeyInput.KEY_TAB:
						autoCompleteInputFieldWithCmd(bControl);
						break;
					case KeyInput.KEY_DELETE:
						if(bControl){
							clearInputTextField();
//							updateSelectionIndexForAutoFillInputFieldText();
						}
						break;
					case KeyInput.KEY_SLASH:
						if(bControl)cd().toggleLineCommentOrCommand();
						break;
					case KeyInput.KEY_LEFT:
						if(bControl)navigateWord(false);
						break;
					case KeyInput.KEY_RIGHT:
						if(bControl)navigateWord(true);
						break;
				}
			}
		};
		
		bindKey("Close", KeyInput.KEY_ESCAPE);
		
		bindKey("copy", KeyInput.KEY_C,KeyAction.CONTROL_DOWN);
		bindKey("cut", KeyInput.KEY_X,KeyAction.CONTROL_DOWN);
		bindKey("paste", KeyInput.KEY_V,KeyAction.CONTROL_DOWN);
		
		bindKey("autocomplete \"starts with\"", KeyInput.KEY_TAB);
		bindKey("autocomplete \"contains\"", KeyInput.KEY_TAB, KeyAction.CONTROL_DOWN);
		bindKey("submit command", KeyInput.KEY_RETURN);
		bindKey("submit command", KeyInput.KEY_NUMPADENTER);
		bindKey("", KeyInput.KEY_B,KeyAction.CONTROL_DOWN);
		bindKey("clear input field", KeyInput.KEY_DELETE,KeyAction.CONTROL_DOWN);
		bindKey("\"/\" toggle input field comment", KeyInput.KEY_SLASH,KeyAction.CONTROL_DOWN);
		
		bindKey("navigate to previous word", KeyInput.KEY_LEFT,KeyAction.CONTROL_DOWN);
		bindKey("navigate to next word", KeyInput.KEY_RIGHT,KeyAction.CONTROL_DOWN);
		
		// cmd history select action
		KeyActionListener actCmdHistoryEntrySelectAction = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				navigateCmdHistOrHintBox(source,key.getKeyCode());
			}
		};
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_UP), actCmdHistoryEntrySelectAction);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_DOWN), actCmdHistoryEntrySelectAction);
		
		// scroll actions
		KeyActionListener actDumpNavigate = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
				double dCurrent = getScrollDumpAreaFlindex();
				double dAdd = 0;
				switch(key.getKeyCode()){
					case KeyInput.KEY_PGUP:
						dAdd = -getShowRowsAmount();
						break;
					case KeyInput.KEY_PGDN:
						dAdd = +getShowRowsAmount();
						break;
					case KeyInput.KEY_HOME:
						if(bControl)dAdd = -dCurrent;
						break;
					case KeyInput.KEY_END:
						if(bControl)dAdd = getDumpEntries().size();
						break;
				}
				scrollDumpArea(dCurrent + dAdd);
				scrollToBottomRequestSuspend();
			}
		};
		bindKey(actDumpNavigate,"navigate dump area to previous page",KeyInput.KEY_PGUP);
		bindKey(actDumpNavigate,"navigate dump area to next page",KeyInput.KEY_PGDN);
		bindKey(actDumpNavigate,"navigate dump area to first entry",KeyInput.KEY_HOME, KeyAction.CONTROL_DOWN);
		bindKey(actDumpNavigate,"navigate dump area to last/current entry",KeyInput.KEY_END, KeyAction.CONTROL_DOWN);
		
		return true;
	}
	
	private BindKey bindKey(String strActionPerformedHelp, int iKeyCode, int... aiKeyModifiers){
		return bindKey(actSimpleActions,strActionPerformedHelp, iKeyCode, aiKeyModifiers);
	}
	private BindKey bindKey(KeyActionListener act, String strActionPerformedHelp, int iKeyCode, int... aiKeyModifiers){
		BindKey bk = MiscLemurHelpersStateI.i().bindKey(getInputField(), act,
			strActionPerformedHelp, iKeyCode, aiKeyModifiers);
		abkList.add(bk);
		return bk;
	}
	
	@Override
	protected void scrollDumpArea(double dIndex){
		/**
		 * the index is actually inverted
		 */
		double dMax = getDumpArea().getSlider().getModel().getMaximum();
		if(dIndex==-1)dIndex=dMax;
		dIndex = dMax-dIndex;
		double dPerc = dIndex/dMax;
		
		getDumpArea().getSlider().getModel().setPercent(dPerc);
		getDumpArea().getSlider().getModel().setValue(dIndex);
	}
	
	@Override
	protected double getScrollDumpAreaFlindex(){
		return getDumpArea().getSlider().getModel().getMaximum()
				-getDumpArea().getSlider().getModel().getValue();
	}
	
	@Override
	protected void updateVisibleRowsAmount(){
		if(getLstbxHeight() != getDumpArea().getSize().y){
			setVisibleRowsAdjustRequest(0); //dynamic
		}
		
		if(getVisibleRowsAdjustRequest()==null)return;
		
		Integer iForceAmount = getVisibleRowsAdjustRequest();
		if(iForceAmount>0){
			super.setShowRowsAmount(iForceAmount);
//			lstbx.setVisibleItems(iShowRows);
//			lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		}else{
//			if(getDumpArea().getGridPanel().getChildren().isEmpty())return;
//			
//			Button	btnFixVisibleRowsHelper = null;
//			for(Spatial spt:getDumpArea().getGridPanel().getChildren()){
//				if(spt instanceof Button){
//					btnFixVisibleRowsHelper = (Button)spt;
//					break;
//				}
//			}
//			if(btnFixVisibleRowsHelper==null)return;
//			
//			fLstbxEntryHeight = retrieveBitmapTextFor(btnFixVisibleRowsHelper).getLineHeight();
			setLstbxEntryHeight(MiscLemurHelpersStateI.i().guessEntryHeight(getDumpArea()));
			if(getLstbxEntryHeight()==null)return;
			
			setLstbxHeight(getDumpArea().getSize().y);
			
			float fHeightAvailable = getLstbxHeight();
//				float fHeightAvailable = fLstbxHeight -fInputHeight;
//				if(getContainerConsole().hasChild(lblStats)){
//					fHeightAvailable-=fStatsHeight;
//				}
			super.setShowRowsAmount((int) (fHeightAvailable / getLstbxEntryHeight()));
		}
		
		getDumpArea().setVisibleItems(super.getShowRowsAmount());
		
		cd().varSet(cd().CMD_FIX_VISIBLE_ROWS_AMOUNT, ""+getShowRowsAmount(), true);
		
	//	lstbx.getGridPanel().setVisibleSize(getShowRowsAmount(),1);
		cd().dumpInfoEntry("fLstbxEntryHeight="+MiscI.i().fmtFloat(getLstbxEntryHeight())+", "+"iShowRows="+getShowRowsAmount());
		
		setVisibleRowsAdjustRequest(null);
		
		cmdLineWrapDisableDumpArea();
	}
	
	@Override
	public int getVisibleRows(){
		return getDumpArea().getGridPanel().getVisibleRows();
	}
//	@Override
//	public Vector3f getDumpAreaSize(){
//		return getDumpArea().getSize();
//	}
//	@Override
//	public Vector3f getInputFieldSize(){
//		return getInputField().getSize();
//	}
	
	@Override
	public String getInputText() {
		return getInputField().getText();
	}
	
	@Override
	public void setInputFieldText(String str){
		/**
		 * do NOT trim() the string, it may be being auto completed and 
		 * an space being appended to help on typing new parameters.
		 */
		getInputField().setText(fixStringToInputField(str));
//		LemurMiscHelpersStateI.i().bugFix(EBugFix.UpdateTextFieldTextAndCaratVisibility, getInputField());
	}
	
//	@Override
//	private void editPasteFromClipBoard() {
//		super.editPasteFromClipBoard();
//		LemurMiscHelpersStateI.i().positionCaratProperly(getInputField());
//	}
	@Override
	protected boolean editInsertAtCaratPosition(String str) {
		MiscLemurHelpersStateI.i().insertTextAtCaratPosition(getInputField(), str);
//		DocumentModel dm = getInputField().getDocumentModel();
//		for(int i=0;i<str.length();i++)dm.insert(str.charAt(i));
//		LemurMiscHelpersStateI.i().bugFix(EBugFix.UpdateTextFieldTextAndCaratVisibility, getInputField());
		return true;
	}
	
//	public boolean isBlank(char c){
//		String str="";str.contains(""+c);
//		return c==' ' || c=='\t';
//	}

	public void navigateWord(boolean bForward){
		Integer iCurPos = getInputFieldCaratPosition();
		String strText = getInputText(); //strText.length()
//		String strBefore = str.substring(0,iCurPos);
//		String strAfter = str.substring(iCurPos);
		
		Integer iNewPos = null;
//		boolean bFoundBlank=false;
		int i=iCurPos;
		
		if(bForward){
			if(i==strText.length())return;
		}else{
			if(i==0)return;
			
//			if(i==strText.length()){
				i--;
//			}
		}
		
		boolean bLetter = Character.isLetter(strText.charAt(i));
		while(true){
			i+=bForward?1:-1;
			if(i==0 || i==strText.length())break;
			
			if(bForward){
				if(!bLetter){
					if(!Character.isLetter(strText.charAt(i))){
						continue;
					}
					break; //found letter
				}else{
					if(!Character.isLetter(strText.charAt(i))){
						bLetter=false;
					}
					continue;
				}
			}else{
				if(!bLetter){
					if(Character.isLetter(strText.charAt(i))){
						bLetter=true;
					}
					continue;
				}else{
					if(Character.isLetter(strText.charAt(i))){
						continue;
					}
					i++; //this will skip the blank to the next char.
					break;
				}
			}
		}
		iNewPos=i;
		
		
//		String strWork=new StringBuffer(bForward?strAfter:strBefore).reverse().toString();
//		for(int i=0; i<strWork.length(); i++){
//			char c = strWork.charAt(i);
//			if(iNewPos!=null){
//				if(bForward){
//					
//				}else{
//					
//				}
//				
//				if(isBlank(c)){
//					iNewPos++;
//					continue;
//				}else{
//					break; //the begin of next/previous word
//				}
//			}
//			
//			if(isBlank(c)){
//				bFoundBlank=true;
//			}else{
//				if(bFoundBlank){
//					iNewPos=bForward?i:-i;
//				}
//			}
//		}
//		
//		if(iNewPos==null){
//			iNewPos=bForward ? iCurPos+strAfter.length() : 0;
//		}else{
//			iNewPos+=iCurPos;
//		}
//		
//		if(bForward){
//			iNewPos++;
//		}else{
//		}
		
		MiscLemurHelpersStateI.i().setCaratPosition(getInputField(),iNewPos);
	}
	
//	@Override
//	private String prepareToPaste(String strPasted, String strCurrent) {
//		if(!isInputTextFieldEmpty()){
//			strCurrent = LemurMiscHelpersStateI.i().prepareStringToPasteAtCaratPosition(
//				getInputField(), strCurrent, strPasted);
//		}else{
//			return super.prepareToPaste(strPasted, strCurrent);
//		}
//		
//		return strCurrent;
//	}
	@Override
	protected void updateDumpAreaSelectedIndex(){
		Integer i = getDumpArea().getSelectionModel().getSelection();
		setDumpAreaSelectedIndex(i==null ? -1 : i);
	}
	@Override
	public void clearDumpAreaSelection() {
		getDumpArea().getSelectionModel().setSelection(-1); //clear selection
	}
	@Override
	protected Double getDumpAreaSliderPercent(){
		return getDumpArea().getSlider().getModel().getPercent();
	}
	@Override
	protected Integer getInputFieldCaratPosition(){
		return getInputField().getDocumentModel().getCarat();
	}
	
	@Override
	protected String autoCompleteInputFieldWithCmd(boolean bMatchContains) {
		String strCompletedCmd = super.autoCompleteInputFieldWithCmd(bMatchContains);
		MiscLemurHelpersStateI.i().setCaratPosition(getInputField(), strCompletedCmd.length());
		return strCompletedCmd;
	}
//	@Override
//	public Vector3f getDumpAreaSliderSize(){
//		return getDumpArea().getSlider().getSize();
//	}
	
	public Container getContainerConsole(){
		return (Container)getContainerMain();
	}
	
	public Container getContainerStatsAndControls(){
		return (Container)super.getStatsAndControls();
	}
	
	private class ButtonClick implements Command<Button>{
		@Override
		public void execute(Button source) {
			if(source.equals(btnClipboardShow)){
				cd().showClipboard();
			}else
			if(source.equals(btnCopy)){
				cd().editCopyOrCut(false,false,false);
			}else
			if(source.equals(btnCut)){
				cd().editCopyOrCut(false,true,false);
			}else
			if(source.equals(btnPaste)){
				editPasteFromClipBoard();
			}else{
				return; //to not make sound mainly 
			}
			
			AudioUII.i().play(EAudio.ReturnChosen);
		}
	}
	
	@Override
	public Vector3f getContainerConsolePreferredSize(){
		return getContainerConsole().getPreferredSize();
	}
	
	@Override
	public void setContainerConsolePreferredSize(Vector3f v3f) {
		MiscLemurHelpersStateI.i().setGrantedSize(getContainerConsole(), v3f, true);
	}
	@Override
	public void addRemoveContainerConsoleChild(boolean bAdd, Node pnlChild){
		if(bAdd){
			BorderLayout.Position p = null;
			if(pnlChild.equals(getContainerStatsAndControls()))p=BorderLayout.Position.North;
			getContainerConsole().addChild(pnlChild,p);
		}else{
			getContainerConsole().removeChild(pnlChild);
		}
	}
	
//	@Override
//	private Vector3f getStatsAndControlsSize() {
//		return getContainerStatsAndControls().getSize();
//	}
	
	@Override
	protected Vector3f getSizeOf(Spatial spt) {
		return ((Panel)spt).getSize();
	}

	/**
	 * DO NOT USE!
	 * overlapping problem, doesnt work well...
	 * keep this method as reference! 
	 */
	@Deprecated
	private void tweakDefaultFontToBecomeFixedSize(){
		setFntMakeFixedWidth(app().getAssetManager().loadFont("Interface/Fonts/Default.fnt"));
		BitmapCharacterSet cs = getFntMakeFixedWidth().getCharSet();
		for(int i=0;i<256;i++){ //is there more than 256?
			BitmapCharacter bc = cs.getCharacter(i);
			if(bc!=null){
				bc.setXAdvance(15); 
			}
		}
		GuiGlobals.getInstance().getStyles().setDefault(getFntMakeFixedWidth());
	}
	
	@Override
	protected float fontWidth(String strChars, String strStyle, boolean bAveraged){
		/**
		 * This is the unquestionable width value.
		 * 
		 * TODO find a better, more direct, way to get the width?
		 */
		float f = MiscJmeI.i().retrieveBitmapTextFor(new Label(strChars,strStyle)).getLineWidth();
		if(bAveraged)f/=strChars.length();
		return f;
	}
	
	@Override
	protected void setStatsText(String str) {
		lblStats.setText(str);
	}
	@Override
	protected String getStatsText() {
		return lblStats.getText();
	}
	
	@Override
	public boolean prepareAndCheckIfReadyToDiscard(ConditionalStateManagerI.CompositeControl cc) {
		getContainerConsole().clearChildren();
		return super.prepareAndCheckIfReadyToDiscard(cc);
	}
	
//	@Override
//	public void initializationCompleted() {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public boolean isInitializationCompleted() {
		return super.isInitializedProperly();
	}

//	@Override
//	public void requestFocus(Spatial spt) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	protected void actionSubmit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateOverrideInputFocus() {
		if(!getInputField().equals(LemurFocusHelperStateI.i().getFocused())){
			LemurFocusHelperStateI.i().requestFocus(getInputField(),true);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,CMD_SHOW_BINDS,"")){
			cd.dumpSubEntry("Shift+Ctrl+V - show clipboard");
			cd.dumpSubEntry("Shift+Click - marks dump area CopyTo selection marker for copy/cut");
			
			/**
			 * see: {@link ConsoleStateAbs#updateInputFieldFillWithSelectedEntry()}
			 */
			cd.dumpSubEntry("Ctrl+Click - if dump area entry is a command, it will overwrite the input field");
			
			cd.dumpSubEntry("HintListFill: Ctrl (contains mode) or Ctrl+Shift (overrides existing hint list with contains mode)");
			cd.dumpSubEntry("Filter: for any command that accepts a filter, if such filter starts with '"+cd().getFuzzyFilterModeToken()+"', the filtering will be fuzzy.");
			
			for(BindKey bk:abkList){
				cd.dumpSubEntry(bk.getHelp());
			}
			
			bCommandWorked=true;
		}else
		{
			return super.execConsoleCommand(cd);
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}

	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dataSelected) {
		super.actionCustomAtEntry(dataSelected);
	}

	@Override
	public <BFR> BFR bugFix(Class<BFR> clReturnType, BoolTogglerCmdField btgBugFixId,	Object... aobjCustomParams) {
		return null;
	}

	@Override
	protected void updateSelected(DialogListEntryData<T> dledPreviouslySelected) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateSelected(DialogListEntryData<T> dledAbove,
			DialogListEntryData<T> dledParentTmp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ConsoleLemurStateI<T> getThis() {
		return this;
	}

	@Override
	protected Float getStatsHeight() {
		return MiscJmeI.i().retrieveBitmapTextFor(lblStats).getLineHeight();
	}
	
	@Override
	public Container getContainerMain(){
		return (Container)super.getContainerMain();
	}
	
	@Override
	public Vector3f getMainSize() {
		return getContainerMain().getPreferredSize();
	}

	@Override
	protected ConsoleLemurStateI<T> setInputText(String str) {
		getInputField().setText(str);
		return getThis();
	}
}
