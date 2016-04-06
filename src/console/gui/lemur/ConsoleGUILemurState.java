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

package console.gui.lemur;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.RangedValueModel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.Styles;

import console.ConsoleCommands;
import console.gui.ConsoleGuiStateAbs;

/**
 * Here is the specific code that links the JME console state with Lemur GUI.
 * 
 * TODO complete specific code migration from ConsoleGuiStateAbs
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleGUILemurState extends ConsoleGuiStateAbs{
	private static ConsoleGUILemurState instance = new ConsoleGUILemurState();
	public static ConsoleGUILemurState i(){return instance;}
	
	@Override
	public void configureBeforeInitializing(SimpleApplication sapp, ConsoleCommands cc, int iToggleConsoleKey) {
		super.configureBeforeInitializing(sapp, cc, iToggleConsoleKey);
		
		// misc cfg
		LemurMiscHelpers.i().configure(sapp, cc);
		if(!sapp.getStateManager().attach(LemurMiscHelpers.i()))throw new NullPointerException("already attached state "+LemurMiscHelpers.class.getName());
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
	
	public void createMonoSpaceFixedFontStyle(){
		if(bConsoleStyleCreated)return;
		
		BitmapFont font = sapp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
//		BitmapFont font = sapp.getAssetManager().loadFont("Interface/Fonts/Console512x.fnt");
		//TODO improve the font quality to be more readable, how???
		
		Styles styles = GuiGlobals.getInstance().getStyles();
		
		ColorRGBA clBg;
		
		Attributes attrs;
		attrs = styles.getSelector(STYLE_CONSOLE); // this also creates the style
		attrs.set("fontSize", 16);
		attrs.set("color", ColorRGBA.White.clone());
		clBg = ColorRGBA.Blue.clone();clBg.b=0.25f;clBg.a=0.75f;
		attrs.set("background", new QuadBackgroundComponent(clBg));
		attrs.set("font", font);
		
//			attrs = styles.getSelector("grid", STYLE_CONSOLE);
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,1,0,1)));
		
		attrs = styles.getSelector(Button.ELEMENT_ID, STYLE_CONSOLE);
		attrs.set("color", new ColorRGBA(0,1,0.5f,1));
		clBg = new ColorRGBA(0,0,0.125f,1);
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
		
		bConsoleStyleCreated=true;
	}
	
	@Override
	public void initializeOnlyTheUI() {
		createMonoSpaceFixedFontStyle();
		
		// auto complete hint
		super.vlstrAutoCompleteHint = new VersionedList<String>();
		super.lstbxAutoCompleteHint = new ListBox<String>(new VersionedList<String>(),strStyle);
		getHintBox().setModel(getHintList());
		
		super.initializeOnlyTheUI();
	}
	
	public VersionedList<String> getHintList(){
		return (VersionedList<String>)vlstrAutoCompleteHint;
	}
	
	@Override
	public void setEnabled(boolean bEnabled) {
		super.setEnabled(bEnabled);
		
		if(this.bEnabled){
			sapp.getGuiNode().attachChild(ctnrConsole);
//			GuiGlobals.getInstance().requestFocus(tfInput);
		}else{
			ctnrConsole.removeFromParent();
			closeHint();
//			GuiGlobals.getInstance().requestFocus(null);
		}
		
		updateOverrideInputFocus();
		
		GuiGlobals.getInstance().setCursorEventsEnabled(this.bEnabled);
	}
	
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
	
	@SuppressWarnings("unchecked")
	public ListBox<String> getHintBox() {
		return (ListBox<String>)super.lstbxAutoCompleteHint;
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
	public ConsoleGUILemurState setHintIndex(Integer i) {
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
				retrieveBitmapTextFor((Button)spt).setLineWrapMode(LineWrapMode.NoWrap);
			}
		}
	}

	@Override
	public ConsoleGUILemurState setHintBoxSize(Vector3f v3fBoxSizeXY, Integer iVisibleLines) {
		getHintBox().setPreferredSize(v3fBoxSizeXY);
		getHintBox().setVisibleItems(iVisibleLines);
		return this;
	}

}
