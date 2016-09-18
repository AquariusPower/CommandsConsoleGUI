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

package com.github.commandsconsolegui.jmegui.lemur.dialog;

import java.io.IOException;
import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.BaseDialogHelper;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs.DialogSavable;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.Styles;

/**
 * TODO rename to LemurBaseDialogHelperI
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class LemurBaseDialogHelperI extends BaseDialogHelper{
	private static LemurBaseDialogHelperI instance = new LemurBaseDialogHelperI();
	public static LemurBaseDialogHelperI i(){return instance;}
	
	private ColorRGBA	colorConsoleStyleBackground;
	StringVarField svfBackgroundHexaColorRGBA = new StringVarField(this,"","XXXXXXXX ex.: 'FF12BC4A' Red Green Blue Alpha");

	public static enum DialogStyleElementId{
		
		buttonResizeBorder,
		
		/** TODO not working yet */
		SliderForValueChange,
		
		;
		public String s(){return this.toString();}
		public String str(){return this.toString();}
	}
	
	@Override
	protected String getTextFromField(Spatial spt) {
		return ((TextField)spt).getText();
	}

	@Override
	protected Vector3f getSizeFrom(Spatial spt) {
		return ((Container)spt).getPreferredSize();
	}

	@Override
	protected void setTextAt(Spatial spt,String str) {
		((TextField)spt).setText(str);
	}

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
				GlobalCommandsDelegatorI.i().dumpExceptionEntry(ex);
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
		
		attrs = styles.getSelector(DialogStyleElementId.buttonResizeBorder.s(), STYLE_CONSOLE);
		clBg = ColorRGBA.Cyan.clone();
		attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
		//TODO the slider style is not working yet... copy from another style temporarily?
		attrs = styles.getSelector(DialogStyleElementId.SliderForValueChange.s(), STYLE_CONSOLE);
		clBg = colorConsoleStyleBackground;
		attrs.set(Slider.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
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
	
//	Savable svAllDiags = new Savable(){
//		@Override
//		public void write(JmeExporter ex) throws IOException {
//			OutputCapsule oc = ex.getCapsule(this);
//			for(BaseDialogStateAbs diag:getDialogListCopy()){
//				oc.write(diag, diag.getId(), null);
//			}
//		}
//		@Override
//		public void read(JmeImporter im) throws IOException {
//			InputCapsule ic = im.getCapsule(this);
//			for(BaseDialogStateAbs diag:getDialogListCopy()){
//				Savable svDiag = ic.readSavable(diag.getId(), diag);
//				diag.read(im);
//			}
//		}
//	};
//	
//	@Override
//	public void saveAllDialogs() {
//		SaveGame.saveGame("ConsoleConfig", "Dialogs", svAllDiags);
////		BinaryExporter ex; 
////    ex.save(data, new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile))););
//	}
	
	public ArrayList<LemurDialogGUIStateAbs> getDialogListCopy() {
		return super.getDialogListCopy(LemurDialogGUIStateAbs.class);
	}
	
	DialogsStorage svAllDiags = new DialogsStorage();
	
	public static class DialogsStorage implements Savable{
		enum E{
			DialogList,
			;
			public String s(){return this.toString();}
		}
		
		public DialogsStorage(){} //required when loading
		
		@Override
		public void write(JmeExporter ex) throws IOException {
			OutputCapsule oc = ex.getCapsule(this);
			oc.writeSavableArrayList(LemurBaseDialogHelperI.i().getListOfSavableForDialogCopy(LemurBasicDialogStateAbs.class), 
				E.DialogList.s(), null);
//			for(LemurDialogGUIStateAbs diag:LemurBaseDialogHelperI.i().getDialogListCopy()){
//				oc.write(diag, diag.getId(), null);
//			}
		}
		@Override
		public void read(JmeImporter im) throws IOException {
//			try {
//				DialogSavable.class.newInstance();
//			} catch (InstantiationException | IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			InputCapsule ic = im.getCapsule(this);
			ArrayList asvList = ic.readSavableArrayList(E.DialogList.s(),null); //this will instance new objects with empty constructors
			boolean bHasSomething=false;for(Object obj:asvList){if(obj!=null){bHasSomething=true;break;}}
			if(!bHasSomething){ 
				GlobalCommandsDelegatorI.i().dumpWarnEntry("failed to load dialogs", this, im, ic);
			}
//			for(LemurDialogGUIStateAbs diag:LemurBaseDialogHelperI.i().getDialogListCopy()){
////				Savable svDiag = ic.readSavable(diag.getId(), diag);
//				diag.read(im);
//			}
		}
		
	};
	
	private String strFileDialogs="Dialogs.jmesave";
	
	private StringCmdField scfSaveDialogs = new StringCmdField(this)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				MiscJmeI.i().saveWriteConsoleData(strFileDialogs,svAllDiags);
				return true;
			}
		});
	
	private StringCmdField scfLoadDialogs = new StringCmdField(this)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				MiscJmeI.i().loadReadConsoleData(strFileDialogs);
				return true;
			}
		});
	
	public LemurBaseDialogHelperI() {
		CallQueueI.i().addCall(new CallableX(this,1000) {
			@Override
			public Boolean call() {
				GlobalCommandsDelegatorI.i().addCmdToQueue(scfLoadDialogs);
				return true;
			}
		});
	}
	
	@Override
	public void update(float tpf){
		super.update(tpf);
		
		if(isDialogSaveRequested()){
			for(LemurBasicDialogStateAbs diag:LemurBaseDialogHelperI.i().getDialogListCopy(LemurBasicDialogStateAbs.class)){
				if(super.isDialogSaveRequestedAndReset(diag)){
					GlobalCommandsDelegatorI.i().addCmdToQueue(scfSaveDialogs);
					resetDialogSaveRequest();
					break;
				}
			}
		}
	}
}
