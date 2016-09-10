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

import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.BaseDialogHelper;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.Styles;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class LemurBaseDialogHelper extends BaseDialogHelper{
	private ColorRGBA	colorConsoleStyleBackground;
	StringVarField svfBackgroundHexaColorRGBA = new StringVarField(this,"","XXXXXXXX ex.: 'FF12BC4A' Red Green Blue Alpha");

	public static enum ConsElementIds{
		buttonResizeBorder,
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
		
		attrs = styles.getSelector(ConsElementIds.buttonResizeBorder.s(), STYLE_CONSOLE);
		clBg = ColorRGBA.Cyan.clone();
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

}
