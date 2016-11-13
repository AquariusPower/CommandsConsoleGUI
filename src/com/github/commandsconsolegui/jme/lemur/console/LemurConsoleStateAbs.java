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

package com.github.commandsconsolegui.jme.lemur.console;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.lwjgl.opengl.Display;

import com.github.commandsconsolegui.cmd.CommandData;
import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.CommandsHelperI;
import com.github.commandsconsolegui.cmd.DumpEntryData;
import com.github.commandsconsolegui.cmd.EDataBaseOperations;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.KeyBoundVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.GlobalManageKeyBindI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jme.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jme.GlobalDialogHelperI;
import com.github.commandsconsolegui.globals.jme.console.GlobalJmeConsoleUII;
import com.github.commandsconsolegui.jme.AudioUII;
import com.github.commandsconsolegui.jme.AudioUII.EAudio;
import com.github.commandsconsolegui.jme.DialogStateAbs;
import com.github.commandsconsolegui.jme.IJmeConsoleUI;
import com.github.commandsconsolegui.jme.ManageConditionalStateI;
import com.github.commandsconsolegui.jme.ManageMouseCursorI.EMouseCursorButton;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData;
import com.github.commandsconsolegui.jme.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogStateAbs;
import com.github.commandsconsolegui.jme.lemur.extras.DialogMainContainer;
import com.github.commandsconsolegui.misc.AutoCompleteI;
import com.github.commandsconsolegui.misc.AutoCompleteI.AutoCompleteResult;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.DebugI;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.WorkAroundI;
import com.github.commandsconsolegui.misc.WorkAroundI.BugFixBoolTogglerCmdField;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI.BindKey;
import com.jme3.app.StatsAppState;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.RangedValueModel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;

/**
 * Here is the specific code that links the JME CommandsConsole State with Lemur GUI.
 * 
 * It is intentionally independent of {@link #LemurDialogStateAbs} to be more robust.
 * 
 * TODO below is from deprecated ConsoleStateAbs, its methods are now all private and begin with "_", may be do not merge as they mean things that are JME only too (not lemur), so in the future a console helper class could be more easily created? 
 * A graphical console where developers and users can issue application commands.
 * This class connects the console commands class with JMonkeyEngine.
 * It must contain the base for the GUI to work.
 * It must be more simple than normal dialogs, therefore less complex, faster and more robust.
 * More info at {@link DialogStateAbs}
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class LemurConsoleStateAbs<T extends Command<Button>, THIS extends LemurConsoleStateAbs<T,THIS>> extends LemurDialogStateAbs<T,THIS> implements IJmeConsoleUI {
	public static final class CompositeControl extends CompositeControlAbs<LemurConsoleStateAbs>{
		private CompositeControl(LemurConsoleStateAbs casm){super(casm);};
	}
	private CompositeControl ccSelf = new CompositeControl(this);
	
//	StringVarField svfBackgroundHexaColorRGBA = new StringVarField(this,"","XXXXXXXX ex.: 'FF12BC4A' Red Green Blue Alpha");
//	private ConsoleMouseCursorListenerI consoleCursorListener;
	private Button	btnCopy;
	private Button	btnPaste;
	private Button	btnClipboardShow;
	private Button	btnCut;
	private Label	lblStats;
	private ArrayList<BindKey>	abkList = new ArrayList<BindKey>();
	
	public final StringCmdField CMD_SHOW_BINDS = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	
	private TextEntryComponent	tecInputField;
	private KeyActionListener	actSimpleActions;

	private ButtonClick	btnclk;
	
	public LemurConsoleStateAbs(){
		super();
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
		
		addKnownStyle(BaseStyles.GLASS);
		addKnownStyle(Styles.ROOT_STYLE);
		
		bindToggleConsole.setObjectRawValue(cfg.iToggleConsoleKey);//bindToggleConsole.getUniqueCmdId()
//		GlobalManageKeyBindI.i().addKeyBind(bindToggleConsole);
		
//		initializationCompleted();
		
		return _initAttempt();
	}
	private boolean _initAttempt() {
		if(!super.initAttempt())return false;
		
		btgEffect.setObjectRawValue(false);
		
		setRetryDelayFor(1000L); //just to lower the output spam of possible problems
		
		return true;
	}
	
	private KeyBoundVarField bindToggleConsole = new KeyBoundVarField(this)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				if(!LemurConsoleStateAbs.this.isConfigured())return false;
				if(!LemurConsoleStateAbs.this.isInitializedProperly())return false;
				
				requestToggleEnabled();
				
				/**
				 * as it is initially invisible, from the 1st time user opens the console on, 
				 * it must be visible.
				 */
				setInitializationVisibility(true);
				
				return true;
			}
		});
	
	public static class CfgParm extends LemurDialogStateAbs.CfgParm{
		private int iToggleConsoleKey;
		public CfgParm(String strUIId, int iToggleConsoleKey) {
			super(strUIId==null ? strUIId=LemurConsoleStateAbs.class.getSimpleName() : strUIId,
				null,null,null,null);
			this.iToggleConsoleKey = iToggleConsoleKey;
			super.setInitiallyEnabled(true); // the console must be initially enabled to startup properly TODO explain better/precisely why?
		}
	}
	private CfgParm cfg;
	@Override
	public THIS configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		
		// for restarting functionality
//		GlobalConsoleGuiI.iGlobal().validate();
		GlobalJmeConsoleUII.iGlobal().set(this);
		
		/**
		 * The console is a special dialog.
		 * Many things depend on it.
		 * Even if initially enabled, for the looks it will be made invisible.
		 */
		
		super.configure(cfg);
		
		setSaveDialog(false);
		setOverrideNormalDialog(true);
		
//		GuiGlobals.initialize(GlobalAppRefI.i());
		
		// misc cfg
		if(!MiscLemurStateI.i().isConfigured()){ //in case of restarting the console
			MiscLemurStateI.i().configure(new MiscLemurStateI.CfgParm());
		}
		
//		LemurMiscHelpersStateI.i().initialize(app().getStateManager(), sapp);
//		if(!app().getStateManager().attach(LemurMiscHelpersStateI.i())){
//			throw new NullPointerException("already attached state "+LemurMiscHelpersStateI.class.getName());
//		}
		if(!LemurDiagFocusHelperStateI.i().isConfigured()){
			LemurDiagFocusHelperStateI.i().configure(new LemurDiagFocusHelperStateI.CfgParm(null));
		}
		
		if(!ManageConditionalStateI.i().isConfigured()){
			ManageConditionalStateI.i().configure(GlobalAppRefI.i());
		}
		
		if(!ConsoleMouseCursorListenerI.i().isConfigured()){
			ConsoleMouseCursorListenerI.i().configure();
		}
		
		if(!ConsoleVarsDialogStateI.i().isConfigured()){
			ConsoleVarsDialogStateI.i().configure(new ConsoleVarsDialogStateI.CfgParm(
				0.9f, 0.9f, 0.1f, null));
		}
		
		storeCfgAndReturnSelf(cfg);
		
		return getThis();
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
	
//	@Override
//	public void prepareStyle(){
//		super.prepareStyle();
//		
//		Styles styles = GuiGlobals.getInstance().getStyles();
//		
//		if(colorConsoleStyleBackground==null){
//			colorConsoleStyleBackground = ColorRGBA.Blue.clone();
//			colorConsoleStyleBackground.b=0.25f;
//			colorConsoleStyleBackground.a=1f; //0.75f;
//		}
//		
//		if(svfBackgroundHexaColorRGBA.getStringValue().isEmpty()){
//			String strHexa = Integer.toHexString(colorConsoleStyleBackground.asIntRGBA());
//			strHexa = String.format("%8s", strHexa).replace(" ", "0").toUpperCase();
//			svfBackgroundHexaColorRGBA.setObjectRawValue(strHexa);
//		}else{
//			try{
//				int i = Integer.parseInt(svfBackgroundHexaColorRGBA.getStringValue(),16);//hexa string
//				colorConsoleStyleBackground.fromIntRGBA(i);
//			}catch(IllegalArgumentException ex){
//				cd().dumpExceptionEntry(ex);
//			}
//		}
//		
//		ColorRGBA clBg;
//		
//		Attributes attrs;
//		attrs = styles.getSelector(STYLE_CONSOLE); // this also creates the style
//		attrs.set("fontSize", 16);
//		attrs.set("color", ColorRGBA.White.clone());
////		clBg = ColorRGBA.Blue.clone();clBg.b=0.25f;clBg.a=0.75f;
//		clBg = colorConsoleStyleBackground;
//		attrs.set("background", new QuadBackgroundComponent(clBg));
//		attrs.set("font", getFont());
//		
////			attrs = styles.getSelector("grid", STYLE_CONSOLE);
////			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,1,0,1)));
//		
//		attrs = styles.getSelector(Button.ELEMENT_ID, STYLE_CONSOLE);
////		attrs.set("color", new ColorRGBA(0,1,0.5f,1));
////		clBg = new ColorRGBA(0,0,0.125f,1);
//		attrs.set("color", ColorRGBA.Cyan.clone());
////		clBg = new ColorRGBA(0,0.25f,0,1);
//		clBg = new ColorRGBA(0,0.25f,0,0.75f);
//		attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
//		
//		attrs = styles.getSelector(ConsElementIds.buttonResizeBorder.s(), STYLE_CONSOLE);
//		clBg = ColorRGBA.Cyan.clone();
//		attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
//		
//		attrs = styles.getSelector(TextField.ELEMENT_ID, STYLE_CONSOLE);
//		attrs.set("color", new ColorRGBA(0.75f,1,1,1));
//		clBg = new ColorRGBA(0.15f, 0.25f, 0, 1);
//		attrs.set(TextField.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
//		
////		lstbx.getElementId().child(ListBox.SELECTOR_ID);
//		attrs = styles.getSelector(ListBox.ELEMENT_ID, ListBox.SELECTOR_ID, STYLE_CONSOLE);
////			attrs = styles.getSelector("list", "selector", STYLE_CONSOLE);
////			attrs.set("color", ColorRGBA.Red.clone());
//		clBg = ColorRGBA.Yellow.clone();clBg.a=0.25f;
//		attrs.set(ListBox.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
//		
////		attrs = styles.getSelector(ListBox.ELEMENT_ID, ListBox.ITEMS_ID, STYLE_CONSOLE);
////		clBg = new ColorRGBA(0,0,0,0);
////		attrs.set(ListBox.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
//
////			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
////
////			attrs = styles.getSelector("slider", "button", STYLE_CONSOLE);
////			attrs.set("color", ColorRGBA.Yellow.clone());
////			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
////			
////			attrs = styles.getSelector("grid", "button", STYLE_CONSOLE);
////			attrs.set("color", ColorRGBA.Yellow.clone());
////			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
//		
////		String strAllChars="W";
////		fMonofontCharWidth = fontWidth(strAllChars,STYLE_CONSOLE);
//		
////		bConsoleStyleCreated=true;
//		
////		updateFontStuff();
//	}
	
	public void initializeOnlyTheUI() {
//		prepareStyle();
		
//		consoleCursorListener = new ConsoleMouseCursorListenerI();
//		consoleCursorListener.configure();
		
		// auto complete hint
		setHintList(new VersionedList<String>());
		setHintBox(new ListBox<String>(new VersionedList<String>(),getDiagStyle()));
		getHintBox().setModel(getHintList());
		CursorEventControl.addListenersToSpatial(getHintBox(), ConsoleMouseCursorListenerI.i());
		
		// main container
//		setContainerMain(new ContainerMain(new BorderLayout(), getDiagStyle()).setDiagOwner(this));
		setDialogMainContainer(new DialogMainContainer(this, new BorderLayout(), getDiagStyle()));
		MiscLemurStateI.i().setPreferredSizeSafely(getDialogMainContainer(), getConsoleSizeCopy(), true);
		
		/**
		 * TOP ELEMENT =================================================================
		 */
		setStatsAndControls(new Container(getDiagStyle()));
//		getContainerStatsAndControls().setName("ConsoleStats");
		getDialogMainContainer().addChild(getContainerStatsAndControls(), BorderLayout.Position.North);
		
		// console stats
		lblStats = new Label("Console stats.",getDiagStyle());
		lblStats.setColor(new ColorRGBA(1,1,0.5f,1));
		MiscLemurStateI.i().setSizeSafely(lblStats, getConsoleSizeCopy().x*0.75f, 1f, true); //TODO y=1f so it will expand?
		getContainerStatsAndControls().addChild(lblStats,0,0);
		
		// buttons
		ArrayList<Button> abtn = new ArrayList<Button>();
		int iButtonIndex=0;
		btnClipboardShow = new Button("ShwClpbrd",getDiagStyle());
		abtn.add(btnClipboardShow);
		
		btnCopy = new Button("Copy",getDiagStyle());
		abtn.add(btnCopy);
		
		btnPaste = new Button("Paste",getDiagStyle());
		abtn.add(btnPaste);
		
		btnCut = new Button("Cut",getDiagStyle());
		abtn.add(btnCut);
		
		btnclk = new ButtonClick();
		for(Button btn:abtn){
			btn.setTextHAlignment(HAlignment.Center);
			//TODO why buttons do not obbey this preferred size 50,1,0?
			btn.addClickCommands(btnclk);
			DialogMouseCursorListenerI.i().addMouseCursorHighlightEffects(btn);
			getContainerStatsAndControls().addChild(btn,0,++iButtonIndex);
		}
		
		
		/**
		 * CENTER ELEMENT (dump entries area) ===========================================
		 */
		setLstbxDumpArea(new ListBox<String>(new VersionedList<String>(),getDiagStyle()));
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
		getDialogMainContainer().addChild(getDumpArea(), BorderLayout.Position.Center);
		
		setSliderDumpArea(getDumpArea().getSlider());
		
		/**
		 * BOTTOM ELEMENT =================================================================
		 */
		// input
		super.setInputField(new TextField(""+cd().getCommandPrefix(),getDiagStyle()));
    CursorEventControl.addListenersToSpatial(getInputField(), ConsoleMouseCursorListenerI.i());
		LemurDiagFocusHelperStateI.i().addFocusChangeListener(getInputField());
//		fInputHeight = MiscJmeI.i().retrieveBitmapTextFor(getInputField()).getLineHeight();
		getDialogMainContainer().addChild( getInputField(), BorderLayout.Position.South );
		
		_initializeOnlyTheUI();
	}
	
	public VersionedList<String> getHintList(){
		return (VersionedList<String>)_getHintList();
	}
	
	@Override
	protected boolean enableAttempt() {
		if(!_enableAttempt())return false;
		
		LemurDiagFocusHelperStateI.i().requestFocus(getInputField());
//	commonOnEnableDisable();
		
		if(isFullyInitialized()){
			AudioUII.i().play(EAudio.OpenConsole);
		}
		
		return true;
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!_disableAttempt())return false;
		
		if(isFullyInitialized()){ //super sets this..
			AudioUII.i().play(EAudio.CloseConsole);
		}
		
		closeHint();
		LemurDiagFocusHelperStateI.i().removeFocusableFromList(getInputField());
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
	
	@SuppressWarnings("unchecked")
	public ListBox<String> getHintBox() {
		return (ListBox<String>)_getHintBox();
	}
	
	@SuppressWarnings("unchecked")
	public ListBox<String> getDumpArea() {
		return (ListBox<String>)_getLstbxDumpArea();
	}
	
	@Override
	protected TextField getInputField(){
		return (TextField)super.getInputField();
	}
	
	public void clearHintSelection() {
		getHintBox().getSelectionModel().setSelection(-1);
	}

	public Integer getHintIndex() {
		return getHintBox().getSelectionModel().getSelection();
	}

	public LemurConsoleStateAbs setHintIndex(Integer i) {
		getHintBox().getSelectionModel().setSelection(i);
		return this;
	}
	
	public LemurConsoleStateAbs setHintBoxSize(Vector3f v3fBoxSizeXY, Integer iVisibleLines) {
		MiscLemurStateI.i().setPreferredSizeSafely(getHintBox(), v3fBoxSizeXY, true);
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
						actionSubmitDirectlyFromUserInput(getInputText());
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
					case KeyInput.KEY_UP:
						if(bControl)dAdd = -1;
						break;
					case KeyInput.KEY_DOWN:
						if(bControl)dAdd = 1;
						break;
				}
				double dSet = dCurrent + dAdd;
				if(dSet<0.0)dSet=0.0;
				scrollDumpArea(dSet);
				scrollToBottomRequestSuspend();
			}
		};
		bindKey(actDumpNavigate,"navigate dump area to previous line",KeyInput.KEY_UP, KeyAction.CONTROL_DOWN);
		bindKey(actDumpNavigate,"navigate dump area to next line",KeyInput.KEY_DOWN, KeyAction.CONTROL_DOWN);
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
		BindKey bk = MiscLemurStateI.i().bindKey(getInputField(), act,
			strActionPerformedHelp, iKeyCode, aiKeyModifiers);
		abkList.add(bk);
		return bk;
	}
	
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
	
	protected double getScrollDumpAreaFlindex(){
		return getDumpArea().getSlider().getModel().getMaximum()
				-getDumpArea().getSlider().getModel().getValue();
	}
	
	protected void updateVisibleRowsAmount(){
		if(getLstbxHeight() != getDumpArea().getSize().y){
			setVisibleRowsAdjustRequest(0); //dynamic
		}
		
		if(getVisibleRowsAdjustRequest()==null)return;
		
		Integer iForceAmount = getVisibleRowsAdjustRequest();
		if(iForceAmount>0){
			setShowRowsAmount(iForceAmount);
		}else{
			setLstbxEntryHeight(MiscLemurStateI.i().guessEntryHeight(getDumpArea()));
			if(getLstbxEntryHeight()==null)return;
			
			setLstbxHeight(getDumpArea().getSize().y);
			
			float fHeightAvailable = getLstbxHeight();
			setShowRowsAmount((int) (fHeightAvailable / getLstbxEntryHeight()));
		}
		
		getDumpArea().setVisibleItems(getShowRowsAmount());
		
		cd().varSet(cd().CMD_FIX_VISIBLE_ROWS_AMOUNT, ""+getShowRowsAmount(), true);
		
	//	lstbx.getGridPanel().setVisibleSize(getShowRowsAmount(),1);
		cd().dumpInfoEntry("fLstbxEntryHeight="+MiscI.i().fmtFloat(getLstbxEntryHeight())+", "+"iShowRows="+getShowRowsAmount());
		
		setVisibleRowsAdjustRequest(null);
		
		cmdLineWrapDisableDumpArea();
	}
	
	public int getVisibleRows(){
		return getDumpArea().getGridPanel().getVisibleRows();
	}
	
	public void setInputFieldText(String str){
		/**
		 * do NOT trim() the string, it may be being auto completed and 
		 * an space being appended to help on typing new parameters.
		 */
		getInputField().setText(fixStringToInputField(str));
//		LemurMiscHelpersStateI.i().bugFix(EBugFix.UpdateTextFieldTextAndCaratVisibility, getInputField());
	}
	
	protected boolean editInsertAtCaratPosition(String str) {
		MiscLemurStateI.i().insertTextAtCaratPosition(getInputField(), str);
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
		
		MiscLemurStateI.i().setCaratPosition(getInputField(),iNewPos);
	}
	
	protected void updateDumpAreaSelectedIndex(){
		Integer i = getDumpArea().getSelectionModel().getSelection();
		setDumpAreaSelectedIndex(i==null ? -1 : i);
	}
	public void clearDumpAreaSelection() {
		getDumpArea().getSelectionModel().setSelection(-1); //clear selection
	}
	protected Double getDumpAreaSliderPercent(){
		return getDumpArea().getSlider().getModel().getPercent();
	}
	protected Integer getInputFieldCaratPosition(){
		return getInputField().getDocumentModel().getCarat();
	}
	
	protected String autoCompleteInputFieldWithCmd(boolean bMatchContains) {
		String strCompletedCmd = _autoCompleteInputFieldWithCmd(bMatchContains);
		MiscLemurStateI.i().setCaratPosition(getInputField(), strCompletedCmd.length());
		return strCompletedCmd;
	}
//	@Override
//	public Vector3f getDumpAreaSliderSize(){
//		return getDumpArea().getSlider().getSize();
//	}
	
	@Override
	public DialogMainContainer getDialogMainContainer(){
		return (DialogMainContainer)_getDialogMainContainer();
	}
	
	public Container getContainerStatsAndControls(){
		return (Container)_getStatsAndControls();
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
				GlobalCommandsDelegatorI.i().dumpDevWarnEntry("unsupported "+source.getName(), source, this);
				AudioUII.i().play(EAudio.Failure);
				return; 
			}
			
			AudioUII.i().play(EAudio.ReturnChosen);
		}
	}
	
	public Vector3f getContainerConsolePreferredSize(){
		return getDialogMainContainer().getPreferredSize();
	}
	
	public void setContainerConsolePreferredSize(Vector3f v3f) {
		MiscLemurStateI.i().setPreferredSizeSafely(getDialogMainContainer(), v3f, true);
	}
	public void addRemoveContainerConsoleChild(boolean bAdd, Node pnlChild){
		if(bAdd){
			BorderLayout.Position p = null;
			if(pnlChild.equals(getContainerStatsAndControls()))p=BorderLayout.Position.North;
			getDialogMainContainer().addChild(pnlChild,p);
		}else{
			getDialogMainContainer().removeChild(pnlChild);
		}
	}
	
//	@Override
//	private Vector3f getStatsAndControlsSize() {
//		return getContainerStatsAndControls().getSize();
//	}
	
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
	
	protected void setStatsText(String str) {
		lblStats.setText(str);
	}
	protected String getStatsText() {
		return lblStats.getText();
	}
	
	@Override
	public boolean prepareToDiscard(ManageConditionalStateI.CompositeControl cc) {
		getDialogMainContainer().clearChildren();
		return _prepareToDiscard(cc);
	}

//	public boolean isInitializationCompleted() {
//		return super.isInitializedProperly();
//	}

	protected void updateOverrideInputFocus() {
		if( !LemurDiagFocusHelperStateI.i().isDialogFocusedFor(getInputField()) ){
//		if(!getInputField().equals(LemurFocusHelperStateI.i().getFocused())){
			LemurDiagFocusHelperStateI.i().requestFocus(getInputField(),true);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(CMD_SHOW_BINDS)){
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
			return _execConsoleCommand(cd);
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}

	protected Float getStatsHeight() {
		return MiscJmeI.i().retrieveBitmapTextFor(lblStats).getLineHeight();
	}

	@Override
	protected void updateSelected(DialogListEntryData<T> dledAbove, DialogListEntryData<T> dledParentTmp) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented yet");
	}

	@Override
	protected void setPositionSize(Vector3f v3fPos, Vector3f v3fSize) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented yet");
	}

	@Override
	protected <N extends Node> void lineWrapDisableForChildrenOf(N node) {
		MiscLemurStateI.i().lineWrapDisableForListboxEntries((ListBox<String>)node);
	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurConsoleStateAbs.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurConsoleStateAbs.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}

	/**********************************************************************
	 * TODO adapting from deprecated ConsoleStateAbs
	 **********************************************************************
	 */
	
	//private FpsLimiterState fpslState = new FpsLimiterState();
		
	//ReattachSafelyState rss;
	
	//	private final String strInputIMCPREFIX = "CONSOLEGUISTATE_";
	public final String strFinalFieldInputCodePrefix="INPUT_MAPPING_CONSOLE_";
//	public final StringCmdField INPUT_MAPPING_CONSOLE_TOGGLE = new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_SCROLL_UP = new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_SCROLL_DOWN = new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_SHIFT_PRESSED	= new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_CONTROL_PRESSED	= new StringCmdField(this,strFinalFieldInputCodePrefix);
	
	//public final String STYLE_CONSOLE="console";
	
	//private boolean bStartupCmdQueueDone = false; 
	
	/**
	 * commands user can type
	 */
	public final StringCmdField CMD_CLOSE_CONSOLE = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_CONSOLE_HEIGHT = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_CONSOLE_STYLE = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_DEFAULT = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_FONT_LIST = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	
	//private String strDefaultFont = "DroidSansMono";
	//private StringVarField	svfUserFontOption = new StringVarField(this, strDefaultFont, null);
	//private int iDefaultFontSize = 12;
	//private IntLongVarField ilvFontSize = new IntLongVarField(this, iDefaultFontSize,null);
	
	///**
	// * keep "initialized" vars together!
	// */
	//private boolean	bInitialized;
	
	/**
	 * keep delayers together!
	 */
	private TimedDelayVarField tdStatsRefresh = new TimedDelayVarField(this,0.5f,"delay between simple stats field refresh");
	private TimedDelayVarField tdScrollToBottomRequestAndSuspend = new TimedDelayVarField(this,0.5f,null);
	private TimedDelayVarField tdScrollToBottomRetry = new TimedDelayVarField(this,0.1f,null);
	
	//private TimedDelay tdLetCpuRest = new TimedDelay(0.1f);
	//private TimedDelay tdStatsRefresh = new TimedDelay(0.5f);
	//private TimedDelay tdDumpQueuedEntry = new TimedDelay(1f/5f); // per second
	//private TimedDelay tdSpareGpuFan = new TimedDelay(1.0f/60f); // like 60 FPS
	
	/**
	 * keep guesses together!
	 * guesses should not exist... 
	 * they are imprecise, they just "work" therefore they may "just break"... 
	 * TODO better algorithms/calculations are required...
	 */
	private int	iJumpBackGUESSED = 1;
	private int	iDotsMarginSafetyGUESSED = 0;
	private int	iSkipCharsSafetyGUESSED = 1;
	private float	fSafetyMarginGUESSED = 20f;
	
	///**
	// * some day this one may not be required
	// */
	//private LemurGuiExtraFunctionalitiesHK efHK = null;
	//public LemurGuiExtraFunctionalitiesHK getLemurHK(){return efHK;}
	//public void initLemurHK(){efHK=new LemurGuiExtraFunctionalitiesHK(this);};
	
	/**
	 * other vars!
	 */
	//private Node	ctnrMainTopSubWindow;
	private Node	lstbxDumpArea;
	//private Node	intputField;
	//private TextField tfAutoCompleteHint;
	//private SimpleApplication	sapp;
	//private boolean	bEnabled;
	private AbstractList<String> vlstrDumpEntriesSlowedQueue;
	private AbstractList<String> vlstrDumpEntries;
	private AbstractList<String> vlstrAutoCompleteHint;
	private Node lstbxAutoCompleteHint;
	private int	iShowRows = 1;
	//private Integer	iToggleConsoleKey = null;
	private Integer	iVisibleRowsAdjustRequest = 0; //0 means dynamic
	//private int	iCmdHistoryCurrentIndex = 0;
	//private String	strInfoEntryPrefix			=". ";
	//private String	strWarnEntryPrefix			="?Warn: ";
	//private String	strErrorEntryPrefix			="!ERROR: ";
	//private String	strExceptionEntryPrefix	="!EXCEPTION: ";
	//private String	strDevWarnEntryPrefix="?DevWarn: ";
	//private String	strDevInfoEntryPrefix=". DevInfo: ";
	//private String	strSubEntryPrefix="\t";
	private boolean	bInitiallyClosedOnce = true;
	//private ArrayList<String> astrCmdHistory = new ArrayList<String>();
	//private ArrayList<String> astrCmdWithCmtValidList = new ArrayList<String>();
	//private ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	private ArrayList<String> astrStyleList = new ArrayList<String>();
	//private int	iCmdHistoryCurrentIndex = 0;
	private	String	strNotSubmitedCmd=null; //null is a marker here
	//private Panel	pnlTest;
	//private String	strTypeCmd="Cmd";
	//private Label	lblStats;
	private Float	fLstbxEntryHeight;
	private Float	fStatsHeight;
	private Float	fInputHeight;
	private int	iScrollRetryAttemptsDBG;
	private int	iScrollRetryAttemptsMaxDBG;
	//private int iMaxCmdHistSize = 1000;
	//private int iMaxDumpEntriesAmount = 100000;
	//private String	strFilePrefix = "Console"; //ConsoleStateAbs.class.getSimpleName();
	//private String	strFileTypeLog = "log";
	//private String	strFileTypeConfig = "cfg";
	//private String	strFileCmdHistory = strFilePrefix+"-CmdHist";
	//private String	strFileLastDump = strFilePrefix+"-LastDump";
	//private String	strFileInitConsCmds = strFilePrefix+"-Init";
	//private String	strFileSetup = strFilePrefix+"-Setup";
	//private String	strFileDatabase = strFilePrefix+"-DB";
	private FloatDoubleVarField	fdvConsoleHeightPercDefault = new FloatDoubleVarField(this,0.5,"percentual of the application window");
	
	/**
	 * use the console height command to set this var, do not use a console variable for it,
	 * mainly because it is already implemented that way and working well...
	 */
	private float	fConsoleHeightPerc = fdvConsoleHeightPercDefault.getFloat();
	//private ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
	//private ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
	//private ArrayList<PreQueueCmdsBlockSubList>	astrExecConsoleCmdsPreQueue = new ArrayList<PreQueueCmdsBlockSubList>();
	//private File	flCmdHist;
	//private File	flLastDump;
	//private File	flInit;
	//private File	flDB;
	//private File	flSetup;
	private float	fLstbxHeight;
	private int	iSelectionIndex = -1;
	private int	iSelectionIndexPreviousForFill = -1;
	private Double	dMouseMaxScrollBy = null; //max scroll if set
	//private boolean bShowLineIndex = true;
	//private String strStyle = BaseStyles.GLASS;
	//private String strStyle = STYLE_CONSOLE;
	//private String strStyle = Styles.ROOT_STYLE;
	private String	strInputTextPrevious = "";
	private AnalogListener	alConsoleScroll;
//	private ActionListener	alGeneralJmeListener;
	//private String	strValidCmdCharsRegex = "A-Za-z0-9_-"+"\\"+strCommandPrefixChar;
	//private String	strValidCmdCharsRegex = "a-zA-Z0-9_"; // better not allow "-" as has other uses like negate number and commands functionalities
	//private String	strStatsLast = "";
	private Node	ctnrStatsAndControls;
	private Vector3f	v3fStatsAndControlsSize;
	//private Button	btnClipboardShow;
	private boolean	bConsoleStyleCreated;
	//private boolean	bUseDumbWrap = true;
	//private Integer	iConsoleMaxWidthInCharsForLineWrap = 0;
	private BitmapFont	fntMakeFixedWidth;
	private StatsAppState	stateStatsOptional;
	//private boolean	bEngineStatsFps;
	//private float	fMonofontCharWidth;
	//private GridPanel	gpListboxDumpArea;
	//private int	iCopyFrom = -1;
	//private int	iCopyTo = -1;
	//private Button	btnCopy;
	//private Button	btnPaste;
	//private boolean	bAddEmptyLineAfterCommand = true;
	//private String	strLineEncloseChar = "'";
	//private String	strCmdLinePrepared = "";
	//private CharSequence	strReplaceTAB = "  ";
	private Float	fWidestCharForCurrentStyleFont = null;
	private boolean	bKeyShiftIsPressed;
	private boolean	bKeyControlIsPressed;
	private Vector3f	v3fConsoleSize;
	private Vector3f	v3fApplicationWindowSize;
	//private String	strPreviousCmdHistoryKey;
	private String	strPreviousInputValue;
	private int	iCmdHistoryPreviousIndex;
	//private boolean	bShowExecQueuedInfo = false;
	//private CommandsDelegatorI	cd;
	//private Hashtable<String,Object> htUserVariables = new Hashtable<String,Object>();
	//protected Hashtable<String,Object> htRestrictedVariables = new Hashtable<String,Object>();
	//private TreeMap<String,Object> tmUserVariables = 
	//	new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	//private TreeMap<String,Object> tmRestrictedVariables =
	//	new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	//private TreeMap<String,ArrayList<String>> tmFunctions = 
	//		new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
	//private ArrayList<Alias> aAliasList = new ArrayList<Alias>();
	//private String	strCmdLineOriginal;
	//private boolean	bLastAliasCreatedSuccessfuly;
	//private float	fTPF;
	//private long	lNanoFrameTime;
	//private long	lNanoFpsLimiterTime;
	//private Boolean	bIfConditionIsValid;
	//private ArrayList<DumpEntry> adeDumpEntryFastQueue = new ArrayList<DumpEntry>();
	//private Button	btnCut;
	//private ArrayList<ConditionalNested> aIfConditionNestedList = new ArrayList<ConditionalNested>();
	//private Boolean	bIfConditionExecCommands;
	//private String	strPrepareFunctionBlockForId;
	//private boolean	bFuncCmdLineRunning;
	//private boolean	bFuncCmdLineSkipTilEnd;
	//private long lLastUniqueId = 0;
	
	//private ConsoleCursorListener consoleCursorListener;
	
	private Spatial	sptScrollTarget;
	private Integer	iStatsTextSafeLength = null;
	private boolean	bExceptionOnce = true;
	private boolean	bKeepInitiallyInvisibleUntilFirstClosed = false;
	private boolean	bFullyInitialized = false;
	
	//private FocusManagerState	focusState;
	//private Spatial	sptPreviousFocus;
	//private boolean	bRestorePreviousFocus;
	private boolean	bInitializeOnlyTheUI;
	//private boolean	bConfigured;
	//private BitmapFont	font;
	//private BitmapFont	fontConsoleDefault;
	//private String	strConsoleDefaultFontName = "Console";
	private int	iMargin;
	private Node	sliderDumpArea;
	//private BitmapFont	fontConsoleExtraDefault;
	//private boolean	bConfigureSimpleCompleted;
	
	BugFixBoolTogglerCmdField btgBugFixStatsLabelTextSize = new BugFixBoolTogglerCmdField(this,false,"FIXED: By fixating the size of the label, this crash preventer is not that necesary anymore.")
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				if(!isEnabled())return true; //simple skipper
				
				/**
				 * BugFix: because the related crash should/could be prevented/preventable.
				 * TODO this bugfix is slow right?
				 * 
				 * Buttons get smashed, shrinking to less than 0, they seem to not accept preferred size constraint.
				 */
				
				boolean bFailed=false;
				while(true){
					String str=getStatsText();
					if(str.isEmpty())break;
					
					boolean bCheckAlways=true; //safer
					if(!bCheckAlways){
						if(iStatsTextSafeLength!=null){
							if(str.length()>iStatsTextSafeLength){
								str=str.substring(0,iStatsTextSafeLength);
								setStatsText(str);
							}
							break;
						}
					}
					
					boolean bRetry=false;
					try{
						/**
						 * this is when crashes outside of here
						 */
						getDialogMainContainer().updateLogicalState(0.05f);
						
						if(bFailed){ //had failed, so look for a safe length
							if(iStatsTextSafeLength==null || !iStatsTextSafeLength.equals(str.length())){
								iStatsTextSafeLength=str.length();
								cd().dumpDebugEntry("StatsTextSafeLength="+str.length());
							}
						}
					}catch(Exception ex){
						if(bExceptionOnce){
							cd().dumpExceptionEntry(ex);
							bExceptionOnce=false;
						}
						
						str=str.substring(0,str.length()-1);
						setStatsText(str);
						
						bRetry = true;
						bFailed = true;
					}
					
					if(!bRetry)break;
				}
				
				return !bFailed;
			}
		});
	//	.setAsBugFixerMode();
		
	private Object	ccTrustedManipulator;
	//private boolean	bUsePreQueue = false; 
	
//	/**********************************************
//	 * USER MUST IMPLEMENT THESE METHODS,
//	 * keep them together for easy review
//	 */
//	
//	///** This method is GUI independent */
//	//protected abstract Object getFocus();
//	///**
//	// * This method is GUI independent
//	// * @param obj if null, is to remove focus from everything
//	// */
//	//protected abstract boolean setFocus(Object obj);
//	///** This method is GUI independent */
//	//protected abstract void removeFocus(Object obj);
//	protected abstract float fontWidth(String strChars, String strStyle, boolean bAveraged);
//	protected abstract void setStatsText(String str);
//	protected abstract String getStatsText();
//	protected abstract void updateVisibleRowsAmount(); 
//	protected abstract void clearHintSelection();
//	protected abstract Integer getHintIndex();
//	protected abstract ConsoleStateAbs setHintIndex(Integer i);
//	protected abstract ConsoleStateAbs setHintBoxSize(Vector3f v3fBoxSizeXY, Integer iVisibleLines);
//	protected abstract void scrollHintToIndex(int i);
//	//protected abstract void lineWrapDisableForChildrenOf(Node node);
//	protected abstract boolean mapKeysForInputField();
//	protected abstract int getVisibleRows();
//	protected abstract Vector3f getSizeOf(Spatial spt);
//	//public abstract Vector3f getDumpAreaSliderSize();
//	protected abstract Vector3f getContainerConsolePreferredSize();
//	protected abstract void setContainerConsolePreferredSize(Vector3f v3f);
//	protected abstract void updateDumpAreaSelectedIndex();
//	protected abstract Double getDumpAreaSliderPercent();
//	protected abstract Integer getInputFieldCaratPosition();
//	//protected abstract Vector3f getDumpAreaSize();
//	//protected abstract Vector3f getStatsAndControlsSize();
//	//protected abstract Vector3f getInputFieldSize();
//	/**
//	 * @param dIndex if -1, means max index (bottom)
//	 */
//	protected abstract void scrollDumpArea(double dIndex);
//	/**
//	 * @return a "floating point index" (Dlindex)
//	 */
//	protected abstract double getScrollDumpAreaFlindex();
//	/**
//	 * 
//	 * @param bAdd if false will remove
//	 * @param pnlChild
//	 * @param p
//	 */
//	public abstract void addRemoveContainerConsoleChild(boolean bAdd, Node pnlChild);
	
	//private static ConsoleGuiStateAbs instance;
	//private static ConsoleGuiStateAbs i(){
	//	return instance;
	//}
	//
	
	//public void configureSimple(int iToggleConsoleKey){
	//	this.iToggleConsoleKey=iToggleConsoleKey;
	//	
	//	bConfigureSimpleCompleted = true;
	//}
	
	//@Deprecated
	//@Override
	//private void configure(String strCmdIdentifier,	boolean bIgnorePrefixAndSuffix) {
	//	throw new NullPointerException("deprecated!!!");
	//}
	
	//public static class CfgParm implements ICfgParm{
	//	String strUIId;
	//	boolean bIgnorePrefixAndSuffix;
	//	int iToggleConsoleKey;
	//	Node nodeGUI;
	//	public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix,
	//			int iToggleConsoleKey, Node nodeGUI) {
	//		super();
	//		this.strUIId = strUIId;
	//		this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
	//		this.iToggleConsoleKey = iToggleConsoleKey;
	//		this.nodeGUI = nodeGUI;
	//	}
	//}
//	public static class CfgParm extends DialogStateAbs.CfgParm{
//		private int iToggleConsoleKey;
//		public CfgParm(String strUIId, int iToggleConsoleKey) {
//			super(strUIId);//, nodeGUI);//, null);
//			this.iToggleConsoleKey = iToggleConsoleKey;
//		}
//	}	
//	private CfgParm cfg;
//	public THIS configure(ICfgParm icfg) {
//		cfg = (CfgParm)icfg;
//		
//		/**
//		 * The console is a special dialog.
//		 * Many things depend on it.
//		 * Even if initially enabled, for the looks it will be made invisible.
//		 */
//		
//		setSaveDialog(false);
//		super.configure(cfg);
//		
//	//	this.iToggleConsoleKey=cfg.iToggleConsoleKey;
//		
//		return storeCfgAndReturnSelf(cfg);
//	}
	
	/**
	 * @param strStyleId
	 */
	protected void addKnownStyle(String strStyleId){
		if(!astrStyleList.contains(strStyleId)){
			astrStyleList.add(strStyleId);
		}
	}
	
	@Override
	protected boolean initGUI() {
		if(!super.initGUI())return false;
		
	//	initializePre();
		addKnownStyle(GlobalDialogHelperI.i().STYLE_CONSOLE);
		
	//	sapp = (SimpleApplication)app;
	//	cc.sapp = sapp;
		
	//	app().getStateManager().attach(fpslState);
		tdStatsRefresh.updateTime();
		
	//	GuiGlobals.initialize(sapp);
	//	BaseStyles.loadGlassStyle(); //do not mess with default user styles: GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
		
	//	fontConsoleDefault = app().getAssetManager().loadFont("Interface/Fonts/Console.fnt");
	//	fontConsoleExtraDefault = app().getAssetManager().loadFont("Interface/Fonts/DroidSansMono.fnt");
	//	app().getAssetManager().registerLoader(TrueTypeLoader.class, "ttf");
		
	//	cc.configure(this,sapp);
	//	cd().initialize();
		
		// other inits
		cd().addCmdToQueue(cd().CMD_FIX_LINE_WRAP);
		cd().addCmdToQueue(cd().CMD_CONSOLE_SCROLL_BOTTOM);
		
		/**
		 * KEEP AS LAST queued cmds below!!!
		 */
		// must be the last queued command after all init ones!
		// end of initialization
		cd().addCmdToQueue(cd().RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE);
	//	addExecConsoleCommandToQueue(cc.btgPreQueue.getCmdIdAsCommand(true)); //		 TODO temporary workaround, pre-queue cannot be enable from start yet...
		if(bInitiallyClosedOnce){
			// after all, close the console
			bKeepInitiallyInvisibleUntilFirstClosed=true;
			cd().addCmdToQueue(CMD_CLOSE_CONSOLE);
		}
		
	//	astrStyleList.add(BaseStyles.GLASS);
	//	astrStyleList.add(Styles.ROOT_STYLE);
	//	astrStyleList.add(STYLE_CONSOLE);
		
		stateStatsOptional = app().getStateManager().getState(StatsAppState.class);
		updateEngineStats();
		
		// instantiations initializer
		initializeOnlyTheUIpreInit();
		initializeOnlyTheUIallSteps();
		
		bInitiallyClosedOnce=false; // to not interfere on reinitializing after a cleanup
		
	//	ConsoleCommandsBackgroundState ccbs = new ConsoleCommandsBackgroundState(this, cc);
	//	ConsoleCommandsBackgroundState.i().configure(sapp,this,cc);
	//	if(!app().getStateManager().attach(ccbs))throw new NullPointerException("already attached state "+ccbs.getClass().getName());
		
	//	bInitialized=true;
		return true;
	}
	
	//@Override
	//public boolean isInitialized() {
	//	return bInitialized;
	//}
	
	private boolean _enableAttempt() {
		if(!super.enableAttempt())return false;
		
		if(!bInitializeOnlyTheUI){
			initializeOnlyTheUIallSteps();
		}
		
		return true;
	}
	
	//@Override
	//public void onEnable() {
	//	super.onEnable();
	//	
	//	if(!bInitializeOnlyTheUI){
	//		initializeOnlyTheUI();
	//	}
	//}
	//
	//@Override
	//public void onDisable() {
	//	super.onDisable();
	//}
	//
	//@Override
	//public void setEnabled(boolean bEnabled) {
	//	if(!bInitializeOnlyTheUI){
	//		initializeOnlyTheUI();
	//	}
	//	
	//	this.bEnabled=bEnabled;
	//}
	//
	//@Override
	//public boolean isEnabled() {
	//	return bEnabled;
	//}
	//
	//@Override
	//public void stateAttached(AppStateManager stateManager) {
	//}
	//
	//@Override
	//public void stateDetached(AppStateManager stateManager) {
	//}
	
	/**
	 * to be used only during initialization to "look good"
	 * @param b
	 */
	private void setInitializationVisibility(boolean b){
		if(b){
			getDialogMainContainer().setCullHint(CullHint.Inherit); //TODO use bkp with: Never ?
			lstbxAutoCompleteHint.setCullHint(CullHint.Inherit); //TODO use bkp with: Dynamic ?
		}else{
			getDialogMainContainer().setCullHint(CullHint.Always); 
			lstbxAutoCompleteHint.setCullHint(CullHint.Always);
		}
	}
	
	private void initializeOnlyTheUIpreInit(){
		if(bInitializeOnlyTheUI)throw new NullPointerException("already configured!");
		
		v3fApplicationWindowSize = new Vector3f(
				Display.getWidth(), //app().getContext().getSettings().getWidth(),
				Display.getHeight(), //app().getContext().getSettings().getHeight(),
				MiscLemurStateI.i().getPreferredThickness());
		
		iMargin=2;
		v3fConsoleSize = new Vector3f(
			v3fApplicationWindowSize.x -(iMargin*2),
			(v3fApplicationWindowSize.y * fConsoleHeightPerc) -iMargin,
			MiscLemurStateI.i().getPreferredThickness());
	}
	
	//private float fPreferredThickness=10.0f;
	
	private void initializeOnlyTheUIallSteps(){
		GlobalDialogHelperI.i().prepareStyle();
		initializeOnlyTheUI();
		updateFontStuff();
	}
	
	/**
	 * this can be used after a cleanup() too
	 */
	private void _initializeOnlyTheUI(){
	//	consoleCursorListener = new ConsoleCursorListener();
	//	consoleCursorListener.configure(sapp, cc, this);
	//	CursorEventControl.addListenersToSpatial(lstbxAutoCompleteHint, consoleCursorListener);
		lstbxAutoCompleteHint.setName("ConsoleHints");
		lstbxDumpArea.setName("ConsoleDumpArea");
		getInputField().setName("ConsoleInput");
		getDialogMainContainer().setName("ConsoleContainer");
		ctnrStatsAndControls.setName("ConsoleStats");
		
	//	tdLetCpuRest.updateTime();
	//	tdStatsRefresh.updateTime();
	//	tdDumpQueuedEntry.updateTime();
		
	//	createMonoSpaceFixedFontStyle();
		
		// main container
		getNodeGUI().attachChild(getDialogMainContainer());
		getDialogMainContainer().setLocalTranslation(
			iMargin, 
			app().getContext().getSettings().getHeight()-iMargin, 
			0); // Z translation controls what will be shown above+/below- others, will be automatically set by FocusChangeListenerI
		
	//	initKeyMappings(); //		mapKeys();
		
	//	// focus
	//	GuiGlobals.getInstance().requestFocus(intputField);
		
		// help (last thing)
	//	dumpInfoEntry("ListBox height = "+fLstbxHeight);
	//	dumpAllStats();
	//	dumpInfoEntry("Hit F10 to toggle console.");
		
		if(bInitiallyClosedOnce){
			setInitializationVisibility(false);
		}
		
		/**
		 * =======================================================================
		 * =========================== LAST THING ================================
		 * =======================================================================
		 */
		bInitializeOnlyTheUI=true;
	}
	
	public boolean cmdEditCopyOrCut(boolean bCut) {
		String strParam1 = cd().getCurrentCommandLine().paramString(1);
		boolean bUseCommandDelimiterInsteadOfNewLine=false;
		if(strParam1!=null){
			switch(strParam1){
				case "-d":bUseCommandDelimiterInsteadOfNewLine=true;break;
			}
		}
		
		/**
		 * the string is sent to clipboard, no need to collect here...
		 * TODO log it may be?
		 */
		String str = cd().editCopyOrCut(false, bCut, bUseCommandDelimiterInsteadOfNewLine);
		
		return true;
	}
	
	//private String prepareToPaste(String strPasted, String strCurrent){
	//	if(isInputTextFieldEmpty() && strPasted.trim().startsWith(""+cd().getCommandPrefix())){
	//		/**
	//		 * replaces currently input field "empty" line 
	//		 * with the command (can be invalid in this case, user may complete it properly)
	//		 */
	//		strCurrent = strPasted.trim(); 
	//	}else{
	//		strCurrent+=strPasted; //simple append if there is no carat position reference
	//	}
	//	
	//	return strCurrent;
	//}
	
	//private void editPasteAppending(String strPasted) {
	////	String strPasted = MiscI.i().retrieveClipboardString(true);
	////	if(strPasted.endsWith("\\n"))strPasted=strPasted.substring(0, strPasted.length()-2);
	//	
	//	String strCurrent = getInputText();
	////	strCurrent = prepareToPaste(strPasted, strCurrent);
	//	
	//////	if(isInputTextFieldEmpty() && strPasted.trim().startsWith(""+cd().getCommandPrefix())){
	////	if(isInputTextFieldEmpty() && cd().isCommandString(strPasted)){
	////		/**
	////		 * replaces currently input field "empty" line 
	////		 * with the command (can be invalid in this case, user may complete it properly)
	////		 */
	////		strCurrent = strPasted.trim(); 
	////	}else{
	//		strCurrent+=strPasted; //simple append if there is no carat position reference
	////	}
	//	
	//	setInputFieldText(strCurrent); 
	//}
	
	protected void editPasteFromClipBoard() {
		String str = MiscI.i().retrieveClipboardString(true);
		if(str.endsWith("\\n"))str=str.substring(0, str.length()-2);
		
		if(isInputTextFieldEmpty() && cd().isCommandString(str)){
			/**
			 * replaces currently input field "empty" line 
			 * with the command (can be invalid in this case, user may complete it properly)
			 */
			setInputFieldText(str.trim());
		}else{
			if(!editInsertAtCaratPosition(str)){
				/**
				 * just append the text
				 */
				setInputFieldText(getInputText()+str); 
	//			editPasteAppending(str);
			}
		}
	}	
	
//	protected abstract boolean editInsertAtCaratPosition(String str);
	
	//private void cmdHistSave(String strCmd) {
	//	fileAppendLine(flCmdHist,strCmd);
	//}
	
	//private ArrayList<String> fileLoad(String strFile) {
	//	return fileLoad(new File(strFile));
	//}
	//private ArrayList<String> fileLoad(File fl) {
	//	ArrayList<String> astr = new ArrayList<String>();
	//	if(fl.exists()){
	//		try{
	//			BufferedReader br=null;
	//	    try {
	//	    	br = new BufferedReader(new FileReader(fl));
	//	    	while(true){
	//					String strLine = br.readLine();
	//					if(strLine==null)break;
	//					astr.add(strLine);
	//	    	}
	//			} catch (IOException e) {
	//				dumpExceptionEntry(e);
	//			}finally{
	//				if(br!=null)br.close();
	//			}
	//		} catch (IOException e) {
	//			dumpExceptionEntry(e);
	//		}
	//	}else{
	//		dumpWarnEntry("File not found: "+fl.getAbsolutePath());
	//	}
	//	
	//	return astr;
	//}
	
	//private void cmdHistLoad() {
	//	astrCmdHistory.addAll(fileLoad(flCmdHist));
	//}
	//
	//private void dumpSave(DumpEntry de) {
	////	if(de.isSavedToLogFile())return;
	//	fileAppendLine(flLastDump,de.getLineOriginal());
	//}
	
	//private void fileAppendList(File fl, ArrayList<String> astr) {
	//	BufferedWriter bw = null;
	//	try{
	//		try {
	//			bw = new BufferedWriter(new FileWriter(fl, true));
	//			for(String str:astr){
	//				bw.write(str);
	//				bw.newLine();
	//			}
	//		} catch (IOException e) {
	//			dumpExceptionEntry(e);
	//		}finally{
	//			if(bw!=null)bw.close();
	//		}
	//	} catch (IOException e) {
	//		dumpExceptionEntry(e);
	//	}
	//}
	//
	//private void fileAppendLine(File fl, String str) {
	//	ArrayList<String> astr = new ArrayList<String>();
	//	astr.add(str);
	//	fileAppendList(fl, astr);
	//}
	
	//private void cmdTest(){
	//	dumpInfoEntry("testing...");
	//	String strOption = paramString(1);
	//	
	//	if(strOption.equalsIgnoreCase("fps")){
	////		app().setSettings(settings);
	//	}else
	//	if(strOption.equalsIgnoreCase("allchars")){
	//		for(char ch=0;ch<256;ch++){
	//			dumpSubEntry(""+(int)ch+"='"+Character.toString(ch)+"'");
	//		}
	//	}else{
	////	dumpSubEntry("["+(char)Integer.parseInt(strParam1, 16)+"]");
	////	if(getDumpAreaSelectedIndex()>=0){
	////		dumpSubEntry("Selection:"+getDumpAreaSelectedIndex()+": '"+vlstrDumpEntries.get(getDumpAreaSelectedIndex())+"'");
	////	}
	//	}
	//	
	//}	
	
	private float fontWidth(String strChars){
		return fontWidth(strChars, getDiagStyle(), true);
	}
	
	private boolean isHintActive(){
		return lstbxAutoCompleteHint.getParent()!=null;
	}
	
	private String getSelectedHint(){
		if(!isHintActive())return null;
		Integer i = getHintIndex();
		if(i==null)return null;
		return vlstrAutoCompleteHint.get(i);
	}
	
//	@Override
//	public void addKeyBind(KeyBoundVarField bind){
//		if(!app().getInputManager().hasMapping(bind.getBindCfg())){
////		  app().getInputManager().deleteMapping(bind.getUserCommand());
////		}
////		
//			app().getInputManager().addMapping(bind.getBindCfg(),
//				MiscJmeI.i().asTriggerArray(bind));
//			
//			app().getInputManager().addListener(alGeneralJmeListener, bind.getBindCfg());
//		}
//	}
	
	@Override
	protected boolean initKeyMappings() {
		if(!mapKeysForInputField())return false;
		
		// console toggle
	//	if(iToggleConsoleKey!=null){
//			if(!app().getInputManager().hasMapping(bindToggleConsole.getUniqueCmdId())){
//				app().getInputManager().addMapping(bindToggleConsole.getUniqueCmdId(),
//					MiscJmeI.i().asTriggerArray(bindToggleConsole));
////					new KeyTrigger(bindToggleConsole.getValue()[0]));
//					
//				alGeneralJmeListener = new ActionListener() {
//					@Override
//					public void onAction(String name, boolean isPressed, float tpf) {
//						if(!isPressed)return;
//						
//						// all field JME binds go here
//						if(bindToggleConsole.checkRunCallerAssigned(isPressed,name))return;
//						
//						GlobalCommandsDelegatorI.i().executeUserBinds(isPressed,name);
//					}
//				};
//				app().getInputManager().addListener(alGeneralJmeListener, bindToggleConsole.getUniqueCmdId());            
//			}
	//	}
		
		if(!app().getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.getUniqueCmdId())){
			app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.getUniqueCmdId(), 
					new KeyTrigger(KeyInput.KEY_LCONTROL),
					new KeyTrigger(KeyInput.KEY_RCONTROL));
			
			ActionListener al = new ActionListener() {
				@Override
				public void onAction(String name, boolean isPressed, float tpf) {
					bKeyControlIsPressed  = isPressed;
				}
			};
			app().getInputManager().addListener(al, INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.getUniqueCmdId());            
		}
		
		if(!app().getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.getUniqueCmdId())){
			app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.getUniqueCmdId(), 
				new KeyTrigger(KeyInput.KEY_LSHIFT),
				new KeyTrigger(KeyInput.KEY_RSHIFT));
				
			ActionListener al = new ActionListener() {
				@Override
				public void onAction(String name, boolean isPressed, float tpf) {
					bKeyShiftIsPressed  = isPressed;
				}
			};
			app().getInputManager().addListener(al, INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.getUniqueCmdId());            
		}
		
		// mouse scroll
	  Trigger[] tggScrollUp = {new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false)};
	  Trigger[] tggScrollDown = {new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true)};
	  
	  alConsoleScroll = new AnalogListener() {
	  	@Override
	  	public void onAnalog(String name, float value, float tpf) {
	      if (isEnabled()) {
	      	boolean bUp = INPUT_MAPPING_CONSOLE_SCROLL_UP.isUniqueCmdIdEqualTo(name);
	      	
	      	if(sptScrollTarget==null)return;
	      	
	      	if(sptScrollTarget.equals(lstbxDumpArea)){
		      	double dScrollCurrentFlindex = getScrollDumpAreaFlindex();
		      	double dScrollBy = iShowRows/5; //20% of the visible rows
		      	if(dMouseMaxScrollBy!=null){
			      	if(dScrollBy > dMouseMaxScrollBy)dScrollBy = dMouseMaxScrollBy;
		      	}
		      	if(dScrollBy < 1)dScrollBy = 1;
		      	
						if(bUp){
							scrollDumpArea(dScrollCurrentFlindex - dScrollBy);
						}else{
	//					if(INPUT_MAPPING_CONSOLE_SCROLL_DOWN.equals(name)){
							scrollDumpArea(dScrollCurrentFlindex + dScrollBy);
						}
	      	}else
	      	if(sptScrollTarget.equals(getInputField())){
	      		navigateCmdHistOrHintBox(getInputField(), bUp?ENav.Up:ENav.Down);
	      	}else
	      	if(sptScrollTarget.equals(lstbxAutoCompleteHint)){
	      		navigateCmdHistOrHintBox(lstbxAutoCompleteHint, bUp?ENav.Up:ENav.Down);
	      	}
	      }
			}
		};
	  
		app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_UP.getUniqueCmdId(), tggScrollUp);
		app().getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_UP.getUniqueCmdId());
	  
		app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_DOWN.getUniqueCmdId(), tggScrollDown);
		app().getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_DOWN.getUniqueCmdId());
		
		return true;
	}
	
	private void fillInputFieldWithHistoryDataAtIndex(int iIndex){
		String str = cd().getCmdHistoryAtIndex(iIndex);
		if(str==null)return;
		
		setInputFieldText(str);
	}
	
	//class ThreadBackgrounCommands implements Runnable{
	//	@Override
	//	private void run() {
	//		TimedDelay td = new TimedDelay(1000.0f/60f)
	//		while(true){
	//			if(!cc.btgExecCommandsInBackground.b())return;
	//			
	//		}
	//	}
	//}
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		if(bKeepInitiallyInvisibleUntilFirstClosed){
			setInitializationVisibility(false);
		}else{
			setInitializationVisibility(isEnabled());
		}
		
		if(!isEnabled())return true; //one update may actually happen after being disabled...
		
		/**
		 *	Will update only if enabled... //if(!isEnabled())return;
		 */
		
		cd().update(tpf);
		GlobalDialogHelperI.i().update(tpf);
		
		updateInputFieldFillWithSelectedEntry();
		updateCurrentCmdHistoryEntryReset();
		updateStats();
		updateAutoCompleteHint();
		updateDumpAreaSelectedIndex();
		updateVisibleRowsAmount();
		updateScrollToBottom();
	//	updateBlinkInputFieldTextCursor(intputField);
	//	if(efHK!=null)efHK.updateHK();
		
		updateOverrideInputFocus();
		
		return true; 
	}
	
	
	@Override
	protected void initSuccess() {
		super.initSuccess();
		updateVisibleRowsAmount();
	}
	
//	protected abstract void updateOverrideInputFocus();
	//{
	//	if(isEnabled()){
	//		LemurFocusHelperStateI.i().requestFocus(getInputField());
	//		setFocus(getInputField());
	//	}else{
	//		removeFocus(getInputField());
	//	}
	//}
	
	//private void updateOverrideInputFocus(){
	//	Spatial sptWithFocus = (Spatial) getFocus();
	//	
	//	if(isEnabled()){
	//		if(intputField!=sptWithFocus){
	//			if(sptPreviousFocus==null){
	//				sptPreviousFocus = sptWithFocus;
	//				bRestorePreviousFocus=true;
	//			}
	//			
	//			setFocus(intputField);
	//		}
	//	}else{
	//		/**
	//		 * this shall happen only once on console closing...
	//		 */
	//		if(bRestorePreviousFocus){
	//			if(sptPreviousFocus!=null){
	//				if(
	//						!sptPreviousFocus.hasAncestor(app().getGuiNode())
	//						&&
	//						!sptPreviousFocus.hasAncestor(app().getRootNode()) //TODO can it be at root node?
	//				){
	//					/**
	//					 * if it is not in one of the rendering nodes,
	//					 * simply removes the focus, to give it back to 
	//					 * other parts of the application.
	//					 */
	//					sptPreviousFocus = null;
	//				}
	//			}
	//			
	//			setFocus(sptPreviousFocus);
	////			GuiGlobals.getInstance().requestFocus(sptPreviousFocus);
	//			sptPreviousFocus = null;
	//			bRestorePreviousFocus=false;
	//		}
	//	}
	//}
	
	///**
	// * TODO use base 36 (max alphanum chars amount)
	// * @return
	// */
	//private String getNextUniqueId(){
	//	return ""+(++lLastUniqueId);
	//}
	
	//private void updateToggles() {
	//	if(cc.btgEngineStatsView.checkChangedAndUpdate())updateEngineStats();
	//	if(cc.btgEngineStatsFps.checkChangedAndUpdate())updateEngineStats();
	//	if(cc.btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(cc.btgFpsLimit.b());
	//	if(cc.btgConsoleCpuRest.checkChangedAndUpdate())tdLetCpuRest.setActive(cc.btgConsoleCpuRest.b());
	////	if(cc.btgPreQueue.checkChangedAndUpdate())bUsePreQueue=cc.btgPreQueue.b();
	//}
	//private void resetCmdHistoryCursor(){
	//	cc.iCmdHistoryCurrentIndex = cc.getCmdHistorySize();
	//}
	
	private void updateCurrentCmdHistoryEntryReset() {
		String strNewInputValue = getInputText();
		if((cd().getCmdHistoryCurrentIndex()-iCmdHistoryPreviousIndex)==0){
			if(!strNewInputValue.equals(strPreviousInputValue)){
				/**
				 * user has deleted or typed some character
				 */
				cd().resetCmdHistoryCursor();
			}
		}
		
		strPreviousInputValue=strNewInputValue;
		iCmdHistoryPreviousIndex=cd().getCmdHistoryCurrentIndex();
	}
	
	protected void closeHint(){
		vlstrAutoCompleteHint.clear();
		lstbxAutoCompleteHint.removeFromParent();
	}
	
	///**
	// * Validates if the first extracted word is a valid command.
	// * 
	// * @param strCmdFullChk can be the full command line here
	// * @return
	// */
	//private boolean validateBaseCommand(String strCmdFullChk){
	//	strCmdFullChk = extractCommandPart(strCmdFullChk,0);
	////	if(strCmdFullChk.startsWith(strCommandPrefixChar)){
	////		strCmdFullChk=strCmdFullChk.substring(strCommandPrefixChar.length());
	////	}
	//	return astrBaseCmdValidList.contains(strCmdFullChk);
	//}
	
	///**
	// * 
	// * @param strCmdFull
	// * @param iPart 0 is base command, 1.. are params
	// * @return
	// */
	//private String extractCommandPart(String strCmdFull, int iPart){
	//	if(strCmdFull.startsWith(""+cc.getCommandPrefix())){
	//		strCmdFull=strCmdFull.substring(1); //1 cc.getCommandPrefix()Char
	//	}
	//	
	////	String[] astr = strCmdFull.split("[^$"+strValidCmdCharsRegex+"]");
	////	if(astr.length>iPart){
	////		return astr[iPart];
	////	}
	//	ArrayList<String> astr = convertToCmdParamsList(strCmdFull);
	//	if(iPart>=0 && astr.size()>iPart){
	//		return astr.get(iPart);
	//	}
	//	
	//	return null;
	//}
	
	//private String extractFirstCommandPart(String strCmdFull){
	//	if(strCmdFull.startsWith(strCommandPrefixChar)){
	//		strCmdFull=strCmdFull.substring(strCommandPrefixChar.length());
	//	}
	//	return strCmdFull.split("[^"+strValidCmdCharsRegex+"]")[0];
	//}
	
	//private boolean checkInputEmpty(){
	//	return checkInputEmptyDumpIfNot(false);
	//}
	/**
	 * after trim(), if empty or have only the command prefix char (pseudo empty), 
	 * will return true.
	 * @return
	 */
	private boolean isInputTextFieldEmpty(){
	//private boolean checkInputEmptyDumpIfNot(boolean bDumpContentsIfNotEmpty){
		String strCurrentInputText = getInputText().trim();
		
		if(strCurrentInputText.isEmpty())return true;
		
		if(strCurrentInputText.equals(""+cd().getCommandPrefix()))return true;
		
	//	if(bDumpContentsIfNotEmpty){
	//		dumpInfoEntry("Not issued command below:");
	//		dumpEntry(strCurrentInputText); //so user will not lose what was typing...
	//		/**
	//		 * Do not scroll in this case. No command was issued...
	//		 */
	//	}
		
		return false;
	}
	
	private String dumpAndClearInputField(){
		if(!isInputTextFieldEmpty()){
			cd().dumpInfoEntry("Not issued command below:");
			String str = getInputText();
			cd().dumpEntry(str); //so user will not lose what was typing...
			clearInputTextField();
			return str;
		}
		return null;
	}
	
	private void updateInputFieldFillWithSelectedEntry() {
		// auto-fill with selected command
		if(getDumpAreaSelectedIndex()>=0){// && isInputTextFieldEmpty()){
			if(iSelectionIndexPreviousForFill!=getDumpAreaSelectedIndex()){ //to let user type things...
				updateCopyFrom();
				
				if(bKeyControlIsPressed){
					String strCmdChk = cd().editCopyOrCut(true,false,true); //vlstrDumpEntries.get(getDumpAreaSelectedIndex()).trim();
					strCmdChk=strCmdChk.trim();
					if(cd().validateCommand(strCmdChk,false)){
						if(!strCmdChk.startsWith(""+cd().getCommandPrefix()))strCmdChk=cd().getCommandPrefix()+strCmdChk;
						dumpAndClearInputField();
	//					int iCommentBegin = strCmdChk.indexOf(cc.getCommentPrefix());
	//					if(iCommentBegin>=0)strCmdChk=strCmdChk.substring(0,iCommentBegin);
	//					strCmdChk=clearCommentsFromMultiline(strCmdChk);
	//					if(strCmdChk.endsWith("\\n"))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-2);
	//					if(strCmdChk.endsWith("\n" ))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-1);
						setInputFieldText(strCmdChk);
					}
				}
				
				updateSelectionIndexForAutoFillInputFieldText();
	//			iSelectionIndexPreviousForFill = getDumpAreaSelectedIndex();
			}
		}
	}
	private void updateSelectionIndexForAutoFillInputFieldText() {
		iSelectionIndexPreviousForFill = getDumpAreaSelectedIndex();
	}
	
	private String clearCommentsFromMultiline(String str){
		/**
		 * this will remove any in-between comments
		 */
	//	str=str.replace("\\n", "\n");
		String strCommentRegion=Pattern.quote(cd().getCommentPrefixStr())+"[^\n]*\n";
		String strJoinToken="\n";
		if(str.matches(".*"+strCommentRegion+".*")){
			str=String.join(strJoinToken, str.split(strCommentRegion));
		}
		
		strCommentRegion=Pattern.quote(cd().getCommentPrefixStr())+"[^"+cd().getCommandDelimiterStr()+"]*"+cd().getCommandDelimiterStr();
		strJoinToken=cd().getCommandDelimiterStr();
		if(str.matches(".*"+strCommentRegion+".*")){
			str=String.join(strJoinToken, str.split(strCommentRegion));
		}
		
		return str;
	}
	
	protected String fixStringToInputField(String str){
		str=clearCommentsFromMultiline(str);
		
		/**
		 * removes ending NL
		 */
		if(str.endsWith("\\n"))str=str.substring(0,str.length()-2);
		if(str.endsWith("\n" ))str=str.substring(0,str.length()-1);
		
		/**
		 * Lines ending with command delimiter, do not require newline.
		 */
		return cd().convertNewLineToCmdDelimiter(str)
			/**
			 * convert special chars to escaped ones
			 */
			.replace("\n", "\\n")
			.replace("\t", "\\t") //			.replace("\t", strReplaceTAB)
			.replace("\r", "");
	}
	
	//public CommandsDelegatorI getConsoleCommands(){
	//	return this.cd;
	//}
	
	protected int getDumpAreaSelectedIndex(){
		return iSelectionIndex;
	}
	protected THIS setDumpAreaSelectedIndex(int i){
		this.iSelectionIndex = i;
		return getThis();
	}
	
	private void updateCopyFrom(){
		cd().updateCopyFrom(getDumpAreaSelectedIndex(), bKeyShiftIsPressed);
	}
	
	/**
	 * this is to fix the dump entry by disallowing automatic line wrapping
	 */
	public void cmdLineWrapDisableDumpArea(){
		lineWrapDisableForChildrenOf(lstbxDumpArea);
	}
	
	//private void updateVisibleRowsAmount(){
	//	if(fLstbxHeight != getDumpAreaSize().y){
	//		iVisibleRowsAdjustRequest = 0; //dynamic
	//	}
	//	
	//	if(iVisibleRowsAdjustRequest==null)return;
	//	
	//	Integer iForceAmount = iVisibleRowsAdjustRequest;
	//	if(iForceAmount>0){
	//		iShowRows=iForceAmount;
	////		lstbx.setVisibleItems(iShowRows);
	////		lstbx.getGridPanel().setVisibleSize(iShowRows,1);
	//	}else{
	//		if(lstbxDumpArea.getGridPanel().getChildren().isEmpty())return;
	//		
	//		Button	btnFixVisibleRowsHelper = null;
	//		for(Spatial spt:lstbxDumpArea.getGridPanel().getChildren()){
	//			if(spt instanceof Button){
	//				btnFixVisibleRowsHelper = (Button)spt;
	//				break;
	//			}
	//		}
	//		if(btnFixVisibleRowsHelper==null)return;
	//		
	//		fLstbxEntryHeight = retrieveBitmapTextFor(btnFixVisibleRowsHelper).getLineHeight();
	//		if(fLstbxEntryHeight==null)return;
	//		
	//		fLstbxHeight = getDumpAreaSize().y;
	//		
	//		float fHeightAvailable = fLstbxHeight;
	////			float fHeightAvailable = fLstbxHeight -fInputHeight;
	////			if(ctnrMainTopSubWindow.hasChild(lblStats)){
	////				fHeightAvailable-=fStatsHeight;
	////			}
	//		iShowRows = (int) (fHeightAvailable / fLstbxEntryHeight);
	//	}
	//	
	//	lstbxDumpArea.setVisibleItems(iShowRows);
	//	
	//	cc.varSet(cc.CMD_FIX_VISIBLE_ROWS_AMOUNT, ""+iShowRows, true);
	//	
	////	lstbx.getGridPanel().setVisibleSize(iShowRows,1);
	//	cc.dumpInfoEntry("fLstbxEntryHeight="+MiscI.i().fmtFloat(fLstbxEntryHeight)+", "+"iShowRows="+iShowRows);
	//	
	//	iVisibleRowsAdjustRequest=null;
	//	
	//	cmdLineWrapDisableDumpArea();
	//}
	
	//private BitmapText retrieveBitmapTextFor(Node pnl){
	//	for(Spatial c : pnl.getChildren()){
	//		if(c instanceof BitmapText){
	//			return (BitmapText)c;
	//		}
	//	}
	//	return null;
	//}
	
	/**
	 * This is what happens when Enter key is pressed.
	 * @param strCmd
	 * @return false if was a comment, empty or invalid
	 */
	protected boolean actionSubmitDirectlyFromUserInput(final String strCmd){
		if(checkAndApplyHintAtInputField())return true;
		
		return cd().actionSubmitDirectlyFromUserInput(strCmd);
	}
	
	@Override
	public boolean checkAndApplyHintAtInputField(){
		/**
		 * if hint area is active and has a selected entry, 
		 * it will override default command submit.
		 */
		if(isHintActive()){
			String strHintCmd = getSelectedHint();
			if(strHintCmd!=null){
				strHintCmd=cd().getCommandPrefix()
					+cd().extractCommandPart(strHintCmd,0)+" "
					+String.join(" ", cd().convertToCmdParamsList(getInputText(),1));
	//				+cd().extractCommandPart(getInputText(),1)
				setInputFieldText(strHintCmd);
				return true;
	//			if(!getInputText().equals(strHintCmd)){
	//				editInsertAtCaratPosition(strHintCmd);
	////				setInputFieldText(strHintCmd);
	//				return true;
	//			}
			}
		}
		
		return false;
	}
	
	
	public void clearInputTextField() {
		setInputFieldText(""+cd().getCommandPrefix());
	}
	
	//private boolean actionSubmitCommand(final String strCmd){
	//	if(strCmd.isEmpty() || strCmd.trim().equals(""+cc.getCommandPrefix())){
	//		clearInputTextField(); 
	//		return false;
	//	}
	//	
	//	String strType=strTypeCmd;
	//	boolean bIsCmd=true;
	//	boolean bShowInfo=true;
	//	if(strCmd.trim().startsWith(""+cc.getCommentPrefix())){
	//		strType="Cmt";
	//		bIsCmd=false;
	//	}else
	//	if(!strCmd.trim().startsWith(""+cc.getCommandPrefix())){
	//		strType="Inv";
	//		bIsCmd=false;
	//	}
	//	
	//	if(bIsCmd){
	//		if(strCmd.trim().endsWith(""+cc.getCommentPrefix())){
	//			bShowInfo=false;
	//		}
	//	}
	//	
	////	String strTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+": ";
	//	if(bShowInfo)dumpInfoEntry(strType+": "+strCmd);
	//	
	//	clearInputTextField(); 
	//	
	//	// history
	//	boolean bAdd=true;
	//	if(!astrCmdHistory.isEmpty()){
	//		if(astrCmdHistory.get(getCmdHistorySize()-1).equals(strCmd)){
	//			bAdd=false; //prevent sequential dups
	//		}
	//	}
	//	
	//	if(bAdd){
	//		cc.astrCmdHistory.add(strCmd);
	//		
	//		cc.cmdHistSave(strCmd);
	//		while(cc.getCmdHistorySize()>cc.iMaxCmdHistSize){
	//			cc.astrCmdHistory.remove(0);
	//		}
	//	}
	//	
	//	resetCmdHistoryCursor();
	//	
	//	if(strType.equals(strTypeCmd)){
	//		if(!cc.executeCommand(strCmd)){
	//			cc.dumpWarnEntry(strType+": FAIL: "+strCmd);
	//			cc.showHelpForFailedCommand(strCmd);
	//		}
	//		
	//		if(cc.bAddEmptyLineAfterCommand ){
	//			cc.dumpEntry("");
	//		}
	//	}
	//	
	//	scrollToBottomRequest();
	//	
	//	return bIsCmd;
	//}
	
	public void scrollToBottomRequest(){
		if(!cd().btgAutoScroll.b())return;
		
		tdScrollToBottomRequestAndSuspend.updateTime();
		tdScrollToBottomRetry.updateTime();
	}
	
	protected void scrollToBottomRequestSuspend(){
		tdScrollToBottomRequestAndSuspend.reset();
		tdScrollToBottomRetry.reset();
		
		// update retry attempts for debug info
		if(iScrollRetryAttemptsDBG > iScrollRetryAttemptsMaxDBG){
			iScrollRetryAttemptsMaxDBG = iScrollRetryAttemptsDBG;
		}
		iScrollRetryAttemptsDBG=0;
	}
	
	private void updateScrollToBottom(){
		if(!tdScrollToBottomRequestAndSuspend.isActive())return;
		
	//	if(tdScrollToBottomRetry.getCurrentDelay() < tdScrollToBottomRetry.lDelayLimit){
		if(!tdScrollToBottomRetry.isReady()){
			return;
		}
		
		/**
		 * being after the retry delay check (lets say 0.1s),
		 * this will actually work by checking like at: 1s, 1.1s, 1.2s, ...
		 * like a cooldown time to make sure the slider accepted the command.
		 */
		if(tdScrollToBottomRequestAndSuspend.isReady()){
			if(Double.compare(getDumpAreaSliderPercent(), 0.0)==0){
				scrollToBottomRequestSuspend();
				return;
			}
		}
		
		scrollDumpArea(-1);
		tdScrollToBottomRetry.updateTime();
		iScrollRetryAttemptsDBG++;
	}	
	
	///**
	// * 
	// * @param dIndex if -1, means max index (bottom)
	// */
	//private void scrollDumpArea(double dIndex){
	//	/**
	//	 * the index is actually inverted
	//	 */
	//	double dMax = lstbxDumpArea.getSlider().getModel().getMaximum();
	//	if(dIndex==-1)dIndex=dMax;
	//	dIndex = dMax-dIndex;
	//	double dPerc = dIndex/dMax;
	//	
	//	lstbxDumpArea.getSlider().getModel().setPercent(dPerc);
	//	lstbxDumpArea.getSlider().getModel().setValue(dIndex);
	//}
	
	///**
	// * 
	// * @return a "floating point index" (Dlindex)
	// */
	//private double getScrollDumpAreaFlindex(){
	//	return lstbxDumpArea.getSlider().getModel().getMaximum()
	//			-lstbxDumpArea.getSlider().getModel().getValue();
	//}
	
	//private String autoCompleteInputField(){
	//	return autoCompleteInputField(false);
	//}
	private String _autoCompleteInputFieldWithCmd(boolean bMatchContains){
		String strCmdPart = getInputText();
		String strCmdAfterCarat="";
		
	//	Integer iCaratPositionHK = efHK==null?null:efHK.getInputFieldCaratPosition();
		Integer iCaratPosition = getInputFieldCaratPosition();
		if(iCaratPosition!=null){
			strCmdAfterCarat = strCmdPart.substring(iCaratPosition);
			strCmdPart = strCmdPart.substring(0, iCaratPosition);
		}
		
		String strCompletedCmd = autoCompleteCmdMoreWork(strCmdPart,bMatchContains);
		
		/**
		 * parameters completion!
		 */
		if(strCompletedCmd.equals(strCmdPart)){
			String strBaseCmd = cd().extractCommandPart(strCmdPart,0);
			String strParam1 = cd().extractCommandPart(strCmdPart,1);
			
			String strCmd=null;
			String strPartToComplete=null;
			ArrayList<String> astrOptList = null;
			boolean bIsVarCompletion=false;
			if(CMD_CONSOLE_STYLE.equals(strBaseCmd)){
				strCmd=CMD_CONSOLE_STYLE+" ";
				astrOptList=getStyleListClone();
				strPartToComplete=strParam1;
			}else
			if(cd().CMD_DB.equals(strBaseCmd)){
				strCmd=cd().CMD_DB+" ";
				astrOptList=EDataBaseOperations.getValuesAsArrayList();
				strPartToComplete=strParam1;
			}else
			if(cd().CMD_VAR_SET.equals(strBaseCmd)){
				strCmd=cd().CMD_VAR_SET+" ";
				astrOptList=cd().getVariablesIdentifiers(false);
				strPartToComplete=strParam1;
				bIsVarCompletion=true;
			}else{
				/**
				 * complete for variables ids when retrieving variable value 
				 */
				String strRegexVarOpen=Pattern.quote(""+cd().getVariableExpandPrefix()+"{");
				String strRegex=".*"+strRegexVarOpen+"["+MiscI.i().getValidCmdCharsRegex()+CommandsHelperI.i().getRestrictedToken()+"]*$";
				if(strCompletedCmd.matches(strRegex)){
					strCmd=strCompletedCmd.trim().substring(1); //removes command prefix
					astrOptList=cd().getVariablesIdentifiers(true);
					
					/**
					 * here the variable is being filtered
					 */
					String[] astrToVar = strCompletedCmd.split(".*"+strRegexVarOpen);
					if(astrToVar.length>1){
						strPartToComplete=astrToVar[astrToVar.length-1];
						strCmd=strCmd.substring(0, strCmd.length()-strPartToComplete.length());
						bIsVarCompletion=true;
					}
				}
			}
			
	//		if(strParam1!=null){
			if(astrOptList!=null){
				strCompletedCmd=""+cd().getCommandPrefix()+strCmd;
				
				ArrayList<String> astr = strPartToComplete==null ? astrOptList :
					AutoCompleteI.i().autoComplete(strPartToComplete, astrOptList, bMatchContains);
				if(astr.size()==1){
					strCompletedCmd+=astr.get(0);
				}else{
					cd().dumpInfoEntry("Param autocomplete:");
					String strFirst = null;
					if(strPartToComplete!=null)strFirst = astr.remove(0);
					for(String str:astr){
	//					if(strPartToComplete!=null && str.equals(astr.get(0)))continue;
						if(bIsVarCompletion)str=cd().varReportPrepare(str);
						cd().dumpSubEntry(str);
					}
					
					if(strPartToComplete!=null){
						strCompletedCmd+=strFirst; //best partial param match
					}
				}
			}
	//		}
		}
		
		if(strCompletedCmd.trim().isEmpty())strCompletedCmd=""+cd().getCommandPrefix();
		setInputFieldText(strCompletedCmd+strCmdAfterCarat);
	//	LemurMiscHelpersState.i().setCaratPosition(intputField, strCompletedCmd.length());
	//	if(efHK!=null)efHK.setCaratPosition(strCompletedCmd.length());
		
		scrollToBottomRequest();
		
		return strCompletedCmd;
	}
	
	private ArrayList<String> getStyleListClone() {
		return new ArrayList<String>(astrStyleList);
	}
	private String autoCompleteCmdMoreWork(String strCmdPart, boolean bMatchContains){
		String strCmdPartOriginal = strCmdPart;
		strCmdPart=strCmdPart.trim();
		
		// no command typed
		if(strCmdPart.equalsIgnoreCase(""+cd().getCommandPrefix()) || strCmdPart.isEmpty()){
			return strCmdPartOriginal;
		}
		
		strCmdPart=strCmdPart.replaceFirst("^"+cd().getCommandPrefix(), "");
		
		// do not allow invalid chars
		if(!MiscI.i().isValidIdentifierCmdVarAliasFuncString(strCmdPart)){
			return strCmdPartOriginal;
		}
		
		ArrayList<String> astr = AutoCompleteI.i().autoComplete(
			strCmdPart, cd().getAllPossibleCommands(), bMatchContains);
		/**
		 * Adjust the autocomplete result based on core and unique cmds:
		 * 0 - is partially typed
		 * 1 - is core or unique cmd
		 * 2 - is core or unique cmd
		 * In this case, there is no core cmds conflicts.
		 */
		if(astr.size()==3){
			CommandData cmdd = cd().getCmdDataIfSame(astr.get(1), astr.get(2));
			if(cmdd!=null){
	//		CommandData cmdd = cd().getCmdDataFor(astr.get(1));
	////		if(cmdd==null)cmdd=cd().getCmdDataFor(astr.get(1));
	//		if(cmdd.equals(cd().getCmdDataFor(astr.get(2)))){
				astr.clear();
				astr.add(cmdd.getSimpleCmdId());
			}
		}
		
		String strFirst=astr.get(0); //the actual stored command may come with comments appended
		
		String strAppendSpace = "";
		if(astr.size()==1 && cd().validateCommand(strFirst,true)){
			strAppendSpace=" "; //found an exact command valid match, so add space
		}
		
		// many possible matches
		if(astr.size()>1){
			cd().dumpInfoEntry("AutoComplete: ");
			for(String str:astr){
				if(str.equals(strFirst))continue; //skip the partial improved match, 1st entry
				cd().dumpSubEntry(cd().getCommandPrefix()+str);
			}
		}
		
		return cd().getCommandPrefix()+strFirst.split(" ")[0]+strAppendSpace;
	}
	
	public boolean statsFieldToggle() {
		if(cd().getCurrentCommandLine().paramBooleanCheckForToggle(1)){
			Boolean bEnable = cd().getCurrentCommandLine().paramBoolean(1);
			
			boolean bIsVisible = ctnrStatsAndControls.getParent()!=null;
			boolean bSetVisible = !bIsVisible; //toggle
			
			if(bEnable!=null)bSetVisible = bEnable; //override
			
			if(bSetVisible){
				if(!bIsVisible){
					addRemoveContainerConsoleChild(true,ctnrStatsAndControls);
	//				ctnrMainTopSubWindow.addChild(ctnrStatsAndControls,BorderLayout.Position.North);
				}
			}else{
				if(bIsVisible){
					addRemoveContainerConsoleChild(false,ctnrStatsAndControls);
	//				ctnrMainTopSubWindow.removeChild(ctnrStatsAndControls);
				}
			}
			
			updateVisibleRowsAmount();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param fNewHeightPercent null to use the default
	 */
	private void modifyConsoleHeight(Float fNewHeightPercent) {
		Vector3f v3fNew = getContainerConsolePreferredSize(); //getSize() does not work well..
	//	if(!v3fNew.equals(v3fConsoleSize)){
		if(Float.compare(v3fNew.x,v3fConsoleSize.x)!=0 && Float.compare(v3fNew.y,v3fConsoleSize.y)!=0){ // Z doesnt matter, is to be controlled by ex.: lemur gui
			cd().dumpDevWarnEntry("sizes should be equal: "+v3fNew+v3fConsoleSize);
		}
		
		if(fNewHeightPercent==null)fNewHeightPercent=fdvConsoleHeightPercDefault.getFloat();
		
		if(fNewHeightPercent>0.95f)fNewHeightPercent=0.95f;
		
		v3fNew.y = fNewHeightPercent * app().getContext().getSettings().getHeight();
		
		if(fLstbxEntryHeight!=null){
			float fMin = getInputFieldHeight() +getStatsHeight() +fLstbxEntryHeight*3; //will show only 2 rows, the 3 value is a safety margin
			
			if(v3fNew.y<fMin)v3fNew.y=fMin;
		}else{
			cd().dumpDevWarnEntry("console listbox entry height is null");
		}
		
		setContainerConsolePreferredSize(v3fNew); //setSize() does not work well..
	//	ctnrMainTopSubWindow.setSize(v3fNew); //setSize() does not work well..
		v3fConsoleSize.set(v3fNew);
		
		fConsoleHeightPerc = (fNewHeightPercent);
		
		cd().varSet(CMD_CONSOLE_HEIGHT, ""+fConsoleHeightPerc, true);
		
		iVisibleRowsAdjustRequest = 0; //dynamic
	}
	
	public boolean isVisibleRowsAdjustRequested(){
		return iVisibleRowsAdjustRequest!=null;
	}
	
	public void updateEngineStats() {
		if(stateStatsOptional==null){
			MsgI.i().warn("the default state for engine stats was not set", this);
			return;
		}
		
		stateStatsOptional.setDisplayStatView(cd().btgEngineStatsView.get());
		stateStatsOptional.setDisplayFps(cd().btgEngineStatsFps.get());
	}
	private void styleHelp(){
		cd().dumpInfoEntry("Available styles:");
		//There is still no way to collect styles from: GuiGlobals.getInstance().getStyles()
		for(String str:astrStyleList){
			cd().dumpSubEntry(str);
		}
	}
	private boolean styleCheck(String strStyle) {
		return astrStyleList.contains(strStyle);
	}
	
	private boolean cmdStyleApply(String strStyleNew) {
		boolean bOk = styleCheck(strStyleNew);
		if(bOk){
			setStyle(strStyleNew);
			
			cd().varSet(CMD_CONSOLE_STYLE, getDiagStyle(), true);
			
	//		updateFontStuff();
			
			cd().cmdResetConsole();
		}else{
			cd().dumpWarnEntry("invalid style: "+strStyleNew);
			styleHelp();
		}
		
		return bOk;
	}
	
	private float widthForListbox(){
		return getSizeOf(lstbxDumpArea).x;
	}
	
	/**
	 * TODO review this method, may be sliderDumpArea variable is unnecessary...
	 * @return
	 */
	private float widthForDumpEntryField(){
		//TODO does slider and the safety margin are necessary?
		return widthForListbox() -getSizeOf(sliderDumpArea).x -fSafetyMarginGUESSED;
	//	return widthForListbox();
	}
	
	private Node _getDialogMainContainer(){
		return (Node)super.getDialogMainContainer();
	}
	
	/**
	 * This method can be overriden but this is not necessary,
	 * because this is not a cleanup procedure...
	 * 
	 * This class object will be discarded/trashed/gc.
	 * 
	 * see how flow starting with {@link #requestRestart()} works
	 */
	private boolean _prepareToDiscard(ManageConditionalStateI.CompositeControl cc) {
	//	tdLetCpuRest.reset();
	//	tdScrollToBottomRequestAndSuspend.reset();
	//	tdScrollToBottomRetry.reset();
	//	tdTextCursorBlinkHK.reset();
		
		if(getDialogMainContainer().getChildren().size()>0){
			System.err.println("WARN: console container should have been properly/safely cleaned using specific gui methods by overriding this method.");
			getDialogMainContainer().detachAllChildren();
		}
	//	ctnrMainTopSubWindow.clearChildren();
	//	tfAutoCompleteHint.removeFromParent();
		lstbxAutoCompleteHint.removeFromParent();
		getDialogMainContainer().removeFromParent();
		
		//TODO should keymappings be at setEnabled() ?
	//  /**
	//   * IMPORTANT!!!
	//   * Toggle console must be kept! Re-initialization depends on it!
	//   * 
		GlobalManageKeyBindI.i().removeKeyBind(bindToggleConsole);
//		app().getInputManager().deleteMapping(bindToggleConsole.toString());
//	  app().getInputManager().removeListener(alGeneralJmeListener);
	//   */
	  app().getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_SCROLL_UP+"");
	  app().getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"");
	  app().getInputManager().removeListener(alConsoleScroll);
	  
	//  if(efHK!=null)efHK.cleanupHK();
	  bInitializeOnlyTheUI=false;
	//  bInitialized=false;
	  
		return super.prepareToDiscard(cc);
	}
	
	//private boolean isInitiallyClosed() {
	//	return bInitiallyClosedOnce;
	//}
	//private void setCfgInitiallyClosed(boolean bInitiallyClosed) {
	//	this.bInitiallyClosedOnce = bInitiallyClosed;
	//}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(StringCmdField.class)){
			if(strFinalFieldInputCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcv);
				rfcfg.setPrefixCmd("CONSOLEGUISTATE_");
				rfcfg.setFirstLetterUpperCase(true);
			}else
			if(CommandsHelperI.i().getRestrictedCmdCodePrefix().equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcv);
				rfcfg.setPrefixCmd(""+CommandsHelperI.i().getRestrictedToken());
				rfcfg.setFirstLetterUpperCase(true);
			}
			
			
	//		switch(rfcv.getReflexFillCfgVariant()){
	//			case 0:
	//				rfcfg = new ReflexFillCfg();
	//				rfcfg.strCodingStyleFieldNamePrefix = "INPUT_MAPPING_CONSOLE_";
	//				rfcfg.strCommandPrefix = IMCPREFIX;
	//				rfcfg.bFirstLetterUpperCase = true;
	//				break;
	//		}
		}
		
	//	if(rfcfg==null)rfcfg = cd().getReflexFillCfg(rfcv);
		if(rfcfg==null)rfcfg = super.getReflexFillCfg(rfcv);
		
		return rfcfg;
	}
	
	private ECmdReturnStatus _execConsoleCommand(CommandsDelegator	cd){
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(CMD_CLOSE_CONSOLE,"like the bound key to do it")){
			setEnabledRequest(false);
	//		bKeepInitiallyInvisibleUntilFirstClosed=false; //"first close" is the hint
	//		bFullyInitialized=true; //it will only be completely initialized after the 1st close...
			bCommandWorked=true;
		}else
		if(cd.checkCmdValidity(CMD_CONSOLE_HEIGHT,"[fPercent] of the application window")){
			Float f = cd.getCurrentCommandLine().paramFloat(1);
			modifyConsoleHeight(f);
			bCommandWorked=true;
		}else
	//	if(cc.checkCmdValidity(this,cc.CMD_CONSOLE_SCROLL_BOTTOM,"")){
	//		scrollToBottomRequest();
	//		bCommandWorked=true;
	//	}else
		if(cd.checkCmdValidity(CMD_CONSOLE_STYLE,"[strStyleName] changes the style of the console on the fly, empty for a list")){
			String strStyle = cd.getCurrentCommandLine().paramString(1);
			if(strStyle==null)strStyle="";
			bCommandWorked=cmdStyleApply(strStyle);
		}else
		if(cd.checkCmdValidity(CMD_DEFAULT,"will revert to default values/config/setup (use if something goes wrong)")){
			//TODO apply all basic settings here, like font size etc
			GlobalDialogHelperI.i().setDefault();
			bCommandWorked=cmdStyleApply(GlobalDialogHelperI.i().STYLE_CONSOLE);
		}else
		if(cd.checkCmdValidity(CMD_FONT_LIST,"[strFilter] use 'all' as filter to show all, otherwise only monospaced will be the default filter")){
			String strFilter = cd.getCurrentCommandLine().paramString(1);
			if(strFilter==null){
				strFilter="mono";
			}else{
				if(strFilter.equalsIgnoreCase("all")){
					strFilter=null;
				}
			}
			for(String str:getSystemFontList(strFilter))cd.dumpSubEntry(str);
			bCommandWorked=true;
		}else
		{
			return super.execConsoleCommand(cd);
	//		return ECmdReturnStatus.NotFound;
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}
	
	public void dumpAllStats() {
		cd().dumpEntry(true, cd().btgShowDeveloperInfo.get(), false, false,	
			MiscI.i().getSimpleTime(cd().btgShowMiliseconds.get())
				+cd().getDevInfoEntryPrefix()+"Console stats (Dev): "+"\n"
			
			+"Console Height = "+MiscI.i().fmtFloat(getSizeOf(getDialogMainContainer()).y)+"\n"
			+"Visible Rows = "+getVisibleRows()+"\n"
			+"Line Wrap At = "+cd().getCurrentFixedLineWrapAtColumn()+"\n"
			+"ListBox Height = "+MiscI.i().fmtFloat(getSizeOf(lstbxDumpArea).y)+"\n"
			+"ListBox Entry Height = "+MiscI.i().fmtFloat(fLstbxEntryHeight)+"\n"
			
			+"Stats Text Field Height = "+MiscI.i().fmtFloat(getStatsHeight())+"\n"
			+"Stats Container Height = "+MiscI.i().fmtFloat(getSizeOf(ctnrStatsAndControls).y)+"\n"
			
			+"Input Field Height = "+MiscI.i().fmtFloat(getInputFieldHeight())+"\n"
			+"Input Field Final Height = "+MiscI.i().fmtFloat(getSizeOf(getInputField()).y)+"\n"
			
			+"Slider Value = "+MiscI.i().fmtFloat(getScrollDumpAreaFlindex())+"\n"
			
			+"Slider Scroll request max retry attempts = "+iScrollRetryAttemptsMaxDBG);
	}
	
	//@Override
	//private void setConsoleMaxWidthInCharsForLineWrap(Integer paramInt) {
	//	iConsoleMaxWidthInCharsForLineWrap=paramInt;
	//}
	//
	//@Override
	//private Integer getConsoleMaxWidthInCharsForLineWrap() {
	//	return iConsoleMaxWidthInCharsForLineWrap;
	//}
	
	
	private boolean navigateHint(int iAdd){
		if(!isHintActive())return false;
		
		if(
				getSelectedHint()!=null
				||
				(cd().getCmdHistoryCurrentIndex()+1) >= cd().getCmdHistorySize() // end of cmd history
		){
			int iMaxIndex = vlstrAutoCompleteHint.size()-1;
			if(iMaxIndex<0)return false;
			
			Integer iCurrentIndex = getHintIndex();
			if(iCurrentIndex==null)iCurrentIndex=0;
			
			iCurrentIndex+=iAdd;
			
			if(iCurrentIndex<-1)iCurrentIndex=-1; //will clear the listbox selection
			if(iCurrentIndex>iMaxIndex)iCurrentIndex=iMaxIndex;
			
			setHintIndex(iCurrentIndex);
			
			scrollHintToIndex(iCurrentIndex);
			
			return iCurrentIndex>-1;
		}
		
		return false;
	}
	
	enum ENav{
		Up,
		Down,
	}
	
	protected void navigateCmdHistOrHintBox(Object source, int iKeyCode) {
		ENav enav = null;
		switch(iKeyCode){
			case KeyInput.KEY_UP	:
				enav=ENav.Up;
				break;
			case KeyInput.KEY_DOWN:
				enav=ENav.Down;
				break;
		}		
		
		navigateCmdHistOrHintBox(source, enav);
	}
	
	private void navigateCmdHistOrHintBox(Object objSource, ENav enav) {
		if(cd().getCmdHistoryCurrentIndex()<0)cd().setCmdHistoryCurrentIndex(0); //cant underflow
		if(cd().getCmdHistoryCurrentIndex()>cd().getCmdHistorySize())cd().resetCmdHistoryCursor(); //iCmdHistoryCurrentIndex=getCmdHistorySize(); //can overflow by 1
		
		if(!objSource.equals(getInputField()) && !objSource.equals(lstbxAutoCompleteHint)){
			objSource=null;
		}
		
		boolean bOk=false;
		switch(enav){
			case Up:
				if(objSource==null || objSource==lstbxAutoCompleteHint)bOk=navigateHint(-1);
				
				if((objSource==null && !bOk) || (objSource!=null && objSource.equals(getInputField()))){
					cd().addCmdHistoryCurrentIndex(-1);
					/**
					 * to not lose last possibly typed (but not issued) cmd
					 */
					if(cd().getCmdHistoryCurrentIndex()==(cd().getCmdHistorySize()-1)){ //requested last entry
						strNotSubmitedCmd = dumpAndClearInputField();
					}
					fillInputFieldWithHistoryDataAtIndex(cd().getCmdHistoryCurrentIndex());
				}
				
				break;
			case Down:
				if(objSource==null || objSource==lstbxAutoCompleteHint)bOk=navigateHint(+1);
				
				if((objSource==null && !bOk) || objSource==getInputField()){
					cd().addCmdHistoryCurrentIndex(1);
					if(cd().getCmdHistoryCurrentIndex()>=cd().getCmdHistorySize()){
						if(strNotSubmitedCmd!=null){
							setInputFieldText(strNotSubmitedCmd);
						}
					}
					fillInputFieldWithHistoryDataAtIndex(cd().getCmdHistoryCurrentIndex());
				}
				
				break;
		}
	}
	
	//@Override
	//public <BFR> BFR bugFix(Class<BFR> clReturnType, BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams) {
	//	if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
	//	
	//	boolean bFixed = false;
	//	Object objRet = null;
	//	
	//	if(btgBugFixStatsLabelTextSize.isEqualToAndEnabled(btgBugFixId)){
	//		/**
	//		 * BugFix: because the related crash should/could be prevented/preventable.
	//		 * TODO this bugfix is slow right?
	//		 * 
	//		 * Buttons get smashed, shrinking to less than 0, they seem to not accept preferred size constraint.
	//		 */
	//		
	//		boolean bFailed=false;
	//		while(true){
	//			String str=getStatsText();
	//			if(str.isEmpty())break;
	//			
	//			boolean bCheckAlways=true; //safer
	//			if(!bCheckAlways){
	//				if(iStatsTextSafeLength!=null){
	//					if(str.length()>iStatsTextSafeLength){
	//						str=str.substring(0,iStatsTextSafeLength);
	//						setStatsText(str);
	//					}
	//					break;
	//				}
	//			}
	//			
	//			boolean bRetry=false;
	//			try{
	//				/**
	//				 * this is when crashes outside of here
	//				 */
	//				getDialogMainContainer().updateLogicalState(0.05f);
	//				
	//				if(bFailed){ //had failed, so look for a safe length
	//					if(iStatsTextSafeLength==null || !iStatsTextSafeLength.equals(str.length())){
	//						iStatsTextSafeLength=str.length();
	//						cd().dumpDebugEntry("StatsTextSafeLength="+str.length());
	//					}
	//				}
	//			}catch(Exception ex){
	//				if(bExceptionOnce){
	//					cd().dumpExceptionEntry(ex);
	//					bExceptionOnce=false;
	//				}
	//				
	//				str=str.substring(0,str.length()-1);
	//				setStatsText(str);
	//				
	//				bRetry = true;
	//				bFailed = true;
	//			}
	//			
	//			if(!bRetry)break;
	//		}
	//		
	//		bFixed=true;
	//	}
	//	
	//	return MiscI.i().bugFixRet(clReturnType,bFixed, objRet, aobjCustomParams);
	//}
	
	//enum EBugFix{
	//	StatsLabelTextSize,
	//}
	//@Override
	//public Object bugFix(Object... aobj) {
	//	switch((EBugFix)aobj[0]){
	//		case StatsLabelTextSize:{
	//			/**
	//			 * BugFix: because the related crash should/could be prevented/preventable.
	//			 * 
	//			 * Buttons get smashed, shrinking to less than 0, they seem to not accept preferred size constraint.
	//			 * 
	//			 * By fixating the size of the label, this crash preventer is not that necesary anymore.
	//			 */
	//			boolean bEnableThisCrashPreventer=false;if(!bEnableThisCrashPreventer)return null;
	//			
	//			boolean bFailed=false;
	//			while(true){
	//				String str=getStatsText();
	//				if(str.isEmpty())break;
	//				
	//				boolean bCheckAlways=true; //safer
	//				if(!bCheckAlways){
	//					if(iStatsTextSafeLength!=null){
	//						if(str.length()>iStatsTextSafeLength){
	//							str=str.substring(0,iStatsTextSafeLength);
	//							setStatsText(str);
	//						}
	//						break;
	//					}
	//				}
	//				
	//				boolean bRetry=false;
	//				try{
	//					/**
	//					 * this is when crashes outside of here
	//					 */
	//					getContainerMain().updateLogicalState(0.05f);
	//					
	//					if(bFailed){ //had failed, so look for a safe length
	//						if(iStatsTextSafeLength==null || !iStatsTextSafeLength.equals(str.length())){
	//							iStatsTextSafeLength=str.length();
	//							cd().dumpDebugEntry("StatsTextSafeLength="+str.length());
	//						}
	//					}
	//				}catch(Exception ex){
	//					if(bExceptionOnce){
	//						cd().dumpExceptionEntry(ex);
	//						bExceptionOnce=false;
	//					}
	//					
	//					str=str.substring(0,str.length()-1);
	//					setStatsText(str);
	//					
	//					bRetry = true;
	//					bFailed = true;
	//				}
	//				
	//				if(!bRetry)break;
	//			}
	//		}break;
	//	}
	//	
	//	return null;
	//}
	
	private void updateStats(){
		if(!tdStatsRefresh.isReady(true))return;
		
		String str = cd().prepareStatsFieldText();
		
		if(DebugI.i().isKeyEnabled(DebugI.EDebugKey.AddTextToConsStats))str+=cd().strDebugTest;
		
		setStatsText(str);
		
		WorkAroundI.i().bugFix(btgBugFixStatsLabelTextSize);
	//	bugFix(null,null,btgBugFixStatsLabelTextSize);
	//	bugFix(EBugFix.StatsLabelTextSize);
	}	
	
	private void updateAutoCompleteHint() {
		String strInputText = getInputText();
		if(strInputText.isEmpty())return;
		strInputText=cd().extractCommandPart(strInputText,0);
		if(strInputText==null)return; //something invalid was typed...
		if(bKeyControlIsPressed && bKeyShiftIsPressed){
			/**
			 * user asked for 'contains' override even if there are hints already.
			 */
		}else
		if(lstbxAutoCompleteHint.getParent()==null && bKeyControlIsPressed){
			/**
			 * in this case, the hint list is empty,
			 * and match mode: "contains" was requested by user.
			 */
		}else{
			if(strInputTextPrevious.equals(strInputText))return;
		}
		
		AutoCompleteResult acr = AutoCompleteI.i().autoComplete(
			strInputText, cd().getAllPossibleCommands(), bKeyControlIsPressed, true);
		
	//	if(acr.bUsingFuzzy){
	//		cd().dumpWarnEntry("using FUZZY match for: "+strInputText);
	//	}
		
		boolean bShowHint = false;
		
		if(acr.astr.size()==0){
			bShowHint=false; // empty string, or simply no matches
		}else
		if(acr.astr.size()==1 && strInputText.equals(cd().extractCommandPart(acr.astr.get(0),0))){
			// no extra matches found, only what was already typed was returned
			bShowHint=false;
		}else{
			bShowHint=true; // show all extra matches
		}
		
		if(bShowHint){
			for(int i=0;i<acr.astr.size();i++){
				String str=acr.astr.get(i);
				int iNL = str.indexOf("\n");
				if(iNL>=0){
					acr.astr.set(i,str.substring(0,iNL));
				}
			}
			
			if(acr.bUsingFuzzy){
				for(int i=0;i<acr.astr.size();i++){
					acr.astr.set(i, 
						acr.astr.get(i)+" "+cd().getCommentPrefixStr()+cd().getFuzzyFilterModeToken());
				}
			}
			
			vlstrAutoCompleteHint.clear();
			vlstrAutoCompleteHint.addAll(acr.astr);
			clearHintSelection();
			
			Node nodeParent = getNodeGUI();
			if(!nodeParent.hasChild(lstbxAutoCompleteHint)){
				nodeParent.attachChild(lstbxAutoCompleteHint);
			}
			
			//lstbxAutoCompleteHint.setLocalTranslation(new Vector3f(0, -fInputHeight, 0));
			Vector3f v3f = getInputField().getWorldTranslation().clone();
			v3f.y -= getSizeOf(getInputField()).y;
			lstbxAutoCompleteHint.setLocalTranslation(v3f);
			
			float fEntryHeightGUESSED = getInputFieldHeight(); //TODO should be the listbox entry height
			float fAvailableHeight = v3fApplicationWindowSize.y -v3fConsoleSize.y -fEntryHeightGUESSED;
			int iVisibleItems = (int) (fAvailableHeight/fEntryHeightGUESSED);
			if(iVisibleItems==0)iVisibleItems=1;
			if(iVisibleItems>vlstrAutoCompleteHint.size())iVisibleItems=vlstrAutoCompleteHint.size();
			float fHintHeight = fEntryHeightGUESSED*iVisibleItems;
			if(fHintHeight>fAvailableHeight){
				cd().dumpDevWarnEntry("fHintHeight="+fHintHeight+",fAvailableHeight="+fAvailableHeight);
				fHintHeight=fAvailableHeight;
			}
			int iMinLinesGUESSED = 3; //seems to be required because the slider counts as 3 (up arrow, thumb, down arrow)
			float fMinimumHeightGUESSED = fEntryHeightGUESSED*iMinLinesGUESSED;
			if(fHintHeight<fMinimumHeightGUESSED)fHintHeight=fMinimumHeightGUESSED;
			setHintBoxSize(new Vector3f(widthForListbox(),fHintHeight,0), iVisibleItems);
			
			lineWrapDisableForChildrenOf(lstbxAutoCompleteHint);
			
			scrollHintToIndex(0);
		}else{
			closeHint();
		}
		
		strInputTextPrevious = strInputText;
	}
	
	public String getDumpAreaSliderStatInfo() {
		// this value is the top entry index
		int iMaxSliderIndex=vlstrDumpEntries.size()-getVisibleRows();
		
		return "Sl"
				+MiscI.i().fmtFloat(getScrollDumpAreaFlindex(),0)+"/"+iMaxSliderIndex+"+"+getVisibleRows()
				+"("+MiscI.i().fmtFloat(100.0f -getDumpAreaSliderPercent()*100f,0)+"%)"
				+";";
	
	}
	
	//@Override
	//private int getCmdHistoryCurrentIndex() {
	//	return iCmdHistoryCurrentIndex;
	//}
	
	public int getLineWrapAt() {
	//	updateFontStuff();
	
		boolean bUseFixedWrapColumn = cd().btgUseFixedLineWrapModeForAllFonts.b();
		
		/**
		 * Mono spaced fonts can always have a fixed linewrap column!
		 */
		if(!bUseFixedWrapColumn)bUseFixedWrapColumn=GlobalDialogHelperI.i().STYLE_CONSOLE.equals(getDiagStyle());
		
		if(bUseFixedWrapColumn){
			return (int)
				(widthForDumpEntryField() / fWidestCharForCurrentStyleFont)
				-iSkipCharsSafetyGUESSED;
		}
		
		return 0; //wrap will be dynamic
	}
	
	private void updateFontStuff(){
	//	if(true)return; //dummified
	//if(cc.iConsoleMaxWidthInCharsForLineWrap!=null){
		
		/**
		 * W seems to be the widest in most/all chars sets
		 * so, when using a fixed wrap column, this is the safest char width reference!
		 */
		fWidestCharForCurrentStyleFont = fontWidth("W");
		
	//	if(cc.iConsoleMaxWidthInCharsForLineWrap>0){
	//		cc.iConsoleMaxWidthInCharsForLineWrap = (int) //like trunc
	//			((widthForDumpEntryField()/fWidestCharForCurrentStyleFont)
	//			-iSkipCharsSafetyGUESSED);
	//	}
	//}
	}
	
	/**
	 * Auto wrap.
	 *  
	 * This is MUCH slower than using a mono spaced font and having a fixed linewrap column...
	 */
	public ArrayList<String> wrapLineDynamically(DumpEntryData de) {
		ArrayList<String> astrToDump = new ArrayList<String>();
		
		String[] astr = de.getLineBaking().split("\n");
		
		for(String strLine:astr){
			/**
			 * Dynamically adjust each line length based on the pixels size it will have
			 * after rendered.
			 * It removes characters from the end until the line fits on the limits.
			 */
			String strAfter = "";
			float fMaxWidth = widthForDumpEntryField() - iDotsMarginSafetyGUESSED;
			while(strLine.length()>0){
				while(fontWidth(strLine, getDiagStyle(), false) > fMaxWidth){
					int iLimit = strLine.length()-iJumpBackGUESSED;
					strAfter = strLine.substring(iLimit) + strAfter;
					strLine = strLine.substring(0, iLimit);
				}
				astrToDump.add(strLine);
				strLine = strAfter;
				strAfter="";
			}
		}
		
		return astrToDump;
	}
	
	public boolean isHintBox(Spatial target) {
		return target==lstbxAutoCompleteHint;
	}
	public boolean isScrollRequestTarget(Spatial target) {
		return target == sptScrollTarget;
	}
	public void setScrollRequestTarget(Spatial target) {
		this.sptScrollTarget = target;
	}
	
	///**
	// * TODO WIP, not working yet... may be it is not possible to convert at all yet?
	// * @param ttf
	// * @return
	// */
	//private BitmapFont convertTTFtoBitmapFont(TrueTypeFont ttf){
	//	String strGlyphs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789./*-+?\\;\"!@#$*()&^%";
	//	TrueTypeBitmapGlyph attbg[] = ttf.getBitmapGlyphs(strGlyphs);
	//	BitmapFont font = new BitmapFont();
	//	BitmapCharacterSet bcs = new BitmapCharacterSet();
	//	int iMaxHeight = -1;
	//	int iMaxWidth = -1;
	//	for(TrueTypeBitmapGlyph ttbg : attbg){
	//		BitmapCharacter bc = new BitmapCharacter();
	//		char ch = ttbg.getCharacter().charAt(0);
	//		bc.setChar(ttbg.getCharacter().charAt(0));
	//		
	//		bc.setWidth(ttbg.w);
	//		bc.setHeight(ttbg.h);
	//		
	//		bc.setX(ttbg.x);
	//		bc.setY(ttbg.y);
	//		
	//		bc.setXAdvance(ttbg.xAdvance);
	//		bc.setXOffset(ttbg.getHeightOffset());
	//		bc.setYOffset(ttbg.y);
	//		
	//		bcs.addCharacter(ch, bc);
	//		
	//		if(bc.getHeight()>iMaxHeight)iMaxHeight=bc.getHeight();
	//		if(bc.getWidth()>iMaxWidth)iMaxWidth=bc.getWidth();
	//	}
	//	font.setCharSet(bcs);
	//	
	//	Texture2D t2d = ttf.getAtlas();
	////	Image imgAtlas = t2d.getImage();
	////	Image imgTmp = imgAtlas.clone();
	////	imgTmp.getData(0).rewind();
	////	imgTmp.setData(0, imgTmp.getData(0).asReadOnlyBuffer());
	////	MiscI.i().saveImageToFile(imgTmp,"temp"+ttf.getFont().getName().replace(" ",""));
	//	if(DebugI.i().isKeyEnabled(EDebugKey.DumpFontImgFile)){ //EDbgKey.values()
	//		//TODO why image file ends empty??
	//		MiscJmeI.i().saveImageToFile(t2d.getImage(),
	//			EDebugKey.DumpFontImgFile.toString()+ttf.getFont().getName().replace(" ",""));
	//	}
	//	
	//	bcs.setBase(iMaxHeight); //TODO what is this!?
	////	bcs.setBase(ttf.getFont().getSize()); 
	//	bcs.setHeight(t2d.getImage().getHeight());
	//	bcs.setLineHeight(iMaxHeight);
	//	bcs.setWidth(t2d.getImage().getWidth());
	//	bcs.setRenderedSize(iMaxHeight);
	////	bcs.setStyle(style);
	//	
	//	/**
	//	 * TODO why this fails? missing material's "colorMap" ...
	//	font.setPages(new Material[]{ttf.getBitmapGeom("A", ColorRGBA.White).getMaterial()});
	//	 */
	////	font.setPages(new Material[]{fontConsoleDefault.getPage(0)});
	////	Material mat = ttf.getBitmapGeom(strGlyphs, ColorRGBA.White).getMaterial();
	////	Material mat = fontConsoleDefault.getPage(0).clone();
	//	Material mat = fontConsoleExtraDefault.getPage(0).clone();
	//	mat.setTexture("ColorMap", t2d); //TODO wow, weird results from this...
	////	mat.setTexture("ColorMap", ttf.getAtlas());
	////	mat.setParam("ColorMap", VarType.Texture2D, ttf.getAtlas());
	//	font.setPages(new Material[]{mat});
	//	
	////	Material m = new Material();
	////	m.setp
	//	
	////	font.getCharSet().getCharacter(33);
	////	fontConsoleDefault.getCharSet().getCharacter(35).getChar();
	//	
	////	Material[] amat = new Material[fontConsoleDefault.getPageSize()];
	//	
	////ttf.getAtlas();
	//	
	//	/**
	//	 * 
	//	 * check for missing glyphs?
	//	private boolean hasContours(String character) {
	//    GlyphVector gv = font.createGlyphVector(frc, character);
	//    GeneralPath path = (GeneralPath)gv.getOutline();
	//    PathIterator pi = path.getPathIterator(null);
	//    if (pi.isDone())
	//        return false;
	//    
	//    return true;
	//	}
	//	 */
	//	
	//	//app().getAssetManager().unregisterLocator(fontFile.getParent(), FileLocator.class);
	//	return font;
	//}
	
	///**
	// * TODO this is not working
	// * @param strFontID
	// * @param iFontSize
	// * @return
	// */
	//private BitmapFont fontFromTTF(String strFontID, int iFontSize){
	//	if(true)return null; //TODO dummified
	//	
	//	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	//	Font fntFound=null;
	//	for(Font fnt:ge.getAllFonts()){
	//		if(fnt.getFontName().toLowerCase().equalsIgnoreCase(strFontID)){
	//			fntFound=fnt;
	//			break;
	//		}
	//	}
	//	
	//	if(fntFound==null)return null;
	//	
	//	cd().dumpInfoEntry("System font: "+strFontID);
	//	
	//	//TODO this is probably wrong...
	//	TrueTypeKey ttk = new TrueTypeKey(strFontID,0,iFontSize,1);
	//	fntFound = fntFound.deriveFont(ttk.getStyle(), ttk.getPointSize());
	//	
	//	/**
	//	 * TODO how to directly get a system Font and create a TrueTypeFont without loading it with the file? 
	//	 */
	//	return convertTTFtoBitmapFont(
	//		new TrueTypeFontFromSystem(
	//			app().getAssetManager(), 
	//			fntFound,
	//			ttk.getPointSize(),
	//			ttk.getOutline()
	//		));
	//}
	//
	//private BitmapFont fontFromTTFFile(String strFilePath, int iFontSize){
	//	File fontFile = new File(strFilePath);
	//	
	//	if(fontFile.getParent()==null)return null; //not a file with path
	//	
	//	app().getAssetManager().registerLocator(fontFile.getParent(), FileLocator.class);
	//	
	//	TrueTypeKey ttk = new TrueTypeKey(strFilePath, java.awt.Font.PLAIN, iFontSize);
	//	
	//	TrueTypeFont ttf=null;
	//	try{
	//		ttf = (TrueTypeFont)app().getAssetManager().loadAsset(ttk);
	//	}catch(AssetNotFoundException|IllegalArgumentException ex){
	//		// missing file
	//		cd().dumpExceptionEntry(ex);
	//	}
	//	
	//	app().getAssetManager().unregisterLocator(fontFile.getParent(), FileLocator.class);
	//	
	//	if(ttf==null)return null;
	//	
	//	cd().dumpInfoEntry("Font from file: "+strFilePath);
	//	
	//	return convertTTFtoBitmapFont(ttf);
	//}
	
	/**
	 * 
	 * @param strFilter can be null
	 * @return
	 */
	public ArrayList<String> getSystemFontList(String strFilter){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ArrayList<String> astr = new ArrayList<String>();
		for(Font fnt:ge.getAllFonts()){
			if(strFilter==null || fnt.getFontName().toLowerCase().contains(strFilter)){
				astr.add(fnt.getFontName());
			}
		}
		return astr;
	}
	
	//public void prepareStyle() {
	//	String strFontName=svfUserFontOption.getStringValue();
	////	if(bConsoleStyleCreated)return;
	//	
	//	font=null;
	//	try{
	//		if(font==null){ //system font object
	//			font = fontFromTTF(strFontName,ilvFontSize.intValue());
	//		}
	//		
	//		if(font==null){ //custom font file
	//			font = fontFromTTFFile(strFontName,ilvFontSize.intValue());
	//		}
	//		
	//		if(font==null){ //bundled fonts
	//			strFontName=strFontName.replace(" ","");
	//			String strFile = "Interface/Fonts/"+strFontName+".fnt";
	//			font = app().getAssetManager().loadFont(strFile);
	//			if(font!=null)cd().dumpInfoEntry("Bundled font: "+strFile);
	//		}
	//	}catch(AssetNotFoundException ex){
	//		cd().dumpExceptionEntry(ex);
	//		font = fontConsoleDefault; //fontConsoleExtraDefault
	////		strFontName="Console";
	////		font = app().getAssetManager().loadFont("Interface/Fonts/"+strFontName+".fnt");
	////	svUserFontOption.setObjectValue(strFontName);
	//		svfUserFontOption.setObjectRawValue(strConsoleDefaultFontName );
	//	}
	////	BitmapFont font = app().getAssetManager().loadFont("Interface/Fonts/Console512x.fnt");
	//	
	////	updateFontStuff();
	//}
	
	protected void setDumpEntriesSlowedQueue(AbstractList<String> vlstrDumpEntriesSlowedQueue) {
		this.vlstrDumpEntriesSlowedQueue = vlstrDumpEntriesSlowedQueue;
	}
	
	public boolean isFullyInitialized(){
		return bFullyInitialized;
	}
	
	public void setHintBox(Node listBox) {
		this.lstbxAutoCompleteHint=listBox;
	}
	
	private Node _getHintBox() {
		return lstbxAutoCompleteHint;
	}
	
	private boolean _disableAttempt() {
		bKeepInitiallyInvisibleUntilFirstClosed=false; //"first close" is the hint
		bFullyInitialized=true; //it will only be completely initialized after the 1st close...
		
		return super.disableAttempt();
	}
	
	public void setStatsAndControls(Node container) {
		this.ctnrStatsAndControls = container;
	}
	
	public Node _getStatsAndControls() {
		return ctnrStatsAndControls;
	}
	
	@Override
	public void clearSelection() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void updateInputField() {
		// dummified
	}
	
	@Override
	public DialogListEntryData<T> getSelectedEntryData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void updateList() {
		//dummified
	}
	
	@Override
	protected void updateTextInfo() {
		// TODO Auto-generated method stub
	}
	
	//public String getStyle() {
	//	return strStyle;
	//}
	
	public int getShowRowsAmount() {
		return iShowRows;
	}
	
	protected THIS setShowRowsAmount(Integer i) {
		this.iShowRows=i;
		return getThis();
	}
	public Integer getVisibleRowsAdjustRequest() {
		return iVisibleRowsAdjustRequest;
	}
	public void setVisibleRowsAdjustRequest(Integer i){
		iVisibleRowsAdjustRequest=i;
	}
	public Float getLstbxEntryHeight() {
		return fLstbxEntryHeight;
	}
	protected THIS setLstbxEntryHeight(Float fLstbxEntryHeight) {
		this.fLstbxEntryHeight = fLstbxEntryHeight;
		return getThis();
	}
	public AbstractList<String> getDumpEntriesForManagement(CompositeControlAbs<?> ccTrustedManipulator) {
		chkCfgTrustedManipulator(ccTrustedManipulator);
		return vlstrDumpEntries;
	}
	protected AbstractList<String> getDumpEntries() {
		return vlstrDumpEntries;
	}
	protected THIS setDumpEntries(AbstractList<String> vlstrDumpEntries) {
		this.vlstrDumpEntries = vlstrDumpEntries;
		return getThis();
	}
	
	public AbstractList<String> getDumpEntriesSlowedQueueForManagement(CompositeControlAbs<?> ccTrustedManipulator) {
		chkCfgTrustedManipulator(ccTrustedManipulator);
		return vlstrDumpEntriesSlowedQueue;
	}
	
	//@Override
	//public AbstractList<String> getAutoCompleteHintList() {
	//	return vlstrAutoCompleteHint;
	//}
	
	private void chkCfgTrustedManipulator(CompositeControlAbs<?> ccTrustedManipulator) {
		ccTrustedManipulator.assertSelfNotNull();
		
		if(this.ccTrustedManipulator==null){
			this.ccTrustedManipulator=ccTrustedManipulator; //configure
		}else{
			if(this.ccTrustedManipulator!=ccTrustedManipulator){
				throw new PrerequisitesNotMetException("Only the first manipulator is allowed to have further access", this.ccTrustedManipulator, ccTrustedManipulator);
			}
		}
	}
	public float getLstbxHeight() {
		return fLstbxHeight;
	}
	protected void setLstbxHeight(float fLstbxHeight) {
		this.fLstbxHeight = fLstbxHeight;
	}
	private Node _getLstbxDumpArea() {
		return lstbxDumpArea;
	}
	protected void setLstbxDumpArea(Node lstbxDumpArea) {
		this.lstbxDumpArea = lstbxDumpArea;
	}
	
	public boolean isKeyShiftIsPressed() {
		return bKeyShiftIsPressed;
	}
	
	protected void setKeyShiftIsPressed(boolean bKeyShiftIsPressed) {
		this.bKeyShiftIsPressed = bKeyShiftIsPressed;
	}
	//protected BitmapFont getFont() {
	//	return font;
	//}
	//protected THIS setFont(BitmapFont font) {
	//	this.font = font;
	//	return getThis();
	//}
	protected BitmapFont getFntMakeFixedWidth() {
		return fntMakeFixedWidth;
	}
	protected void setFntMakeFixedWidth(BitmapFont fntMakeFixedWidth) {
		this.fntMakeFixedWidth = fntMakeFixedWidth;
	}
	private AbstractList<String> _getHintList() {
		return vlstrAutoCompleteHint;
	}
	protected void setHintList(AbstractList<String> vlstrAutoCompleteHint) {
		this.vlstrAutoCompleteHint = vlstrAutoCompleteHint;
	}
	public Vector3f getConsoleSizeCopy() {
		return v3fConsoleSize.clone();
	}
	protected void setConsoleSize(Vector3f v3fConsoleSize) {
		this.v3fConsoleSize = v3fConsoleSize;
	}
	protected Node getSliderDumpArea() {
		return sliderDumpArea;
	}
	protected void setSliderDumpArea(Node sliderDumpArea) {
		this.sliderDumpArea = sliderDumpArea;
	}
//	protected abstract Float getStatsHeight();
	
	@Override
	public boolean execTextDoubleClickActionFor(DialogListEntryData<T> dled) {
		throw new UnsupportedOperationException("method not implemented yet");
	}

	@Override
	public boolean execActionFor(EMouseCursorButton e, Spatial capture) {
		throw new UnsupportedOperationException("method not implemented yet");
	}
	
//	@Override
//	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
//		if(fld.getDeclaringClass()!=ConsoleStateAbs.class)return super.getFieldValue(fld);
//		return fld.get(this);
//	}
//	@Override
//	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
//		if(fld.getDeclaringClass()!=ConsoleStateAbs.class){super.setFieldValue(fld,value);return;}
//		fld.set(this,value);
//	}
	
//	@Override
//	public void infoSystemTopOverride(String str) {
//		LemurDiagFocusHelperStateI.i().requestFocus(spt);
//	}
}
