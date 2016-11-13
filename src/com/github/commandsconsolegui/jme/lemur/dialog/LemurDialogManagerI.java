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

import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jme.ManageDialogAbs;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.ManageSingleInstanceI;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.effect.AbstractEffect;
import com.simsilica.lemur.effect.Effect;
import com.simsilica.lemur.effect.EffectInfo;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.Styles;

/**
 * TODO rename to LemurBaseDialogHelperI
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class LemurDialogManagerI<T extends LemurDialogStateAbs> extends ManageDialogAbs<T>{
	private static LemurDialogManagerI instance = new LemurDialogManagerI();
	public static LemurDialogManagerI i(){return instance;}
	
	private ColorRGBA	colorConsoleStyleBackground;
	
	public final StringVarField svfBackgroundHexaColorRGBA = new StringVarField(this,"","XXXXXXXX ex.: 'FF12BC4A' Red Green Blue Alpha");
	public final IntLongVarField ilvBorderThickness = new IntLongVarField(this, 3, "")
		.setMinMax(1L, 20L)
		.setCallerAssigned(new CallableX(this,100) {
			@Override
			public Boolean call() {
				for(LemurDialogStateAbs diag:getDialogListCopy(LemurDialogStateAbs.class)){
					diag.setBordersThickness(ilvBorderThickness.getInt());
				}
				
				return true;
			}
		});

	public static enum DialogStyleElementId{
		
		ResizeBorder,
		
		/** TODO not working yet */
		SliderForValueChange,
		
		PopupHelp, 
		
		SystemAlert,
		
		;
		public String s(){return this.toString();}
		public String str(){return this.toString();}
	}
	
	@Override
	protected String getTextFromField(Spatial spt) {
		return ((TextField)spt).getText();
	}

	@Override
	protected Vector3f getSizeCopyFrom(Spatial spt) {
		return ((Container)spt).getPreferredSize().clone();
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
		
		if(svfBackgroundHexaColorRGBA.getValueAsString().isEmpty()){
			String strHexa = Integer.toHexString(colorConsoleStyleBackground.asIntRGBA());
			strHexa = String.format("%8s", strHexa).replace(" ", "0").toUpperCase();
			svfBackgroundHexaColorRGBA.setObjectRawValue(strHexa);
		}else{
			try{
				int i = Integer.parseInt(svfBackgroundHexaColorRGBA.getValueAsString(),16);//hexa string
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
		
		attrs = styles.getSelector(DialogStyleElementId.PopupHelp.s(), STYLE_CONSOLE);
		attrs.set("color", ColorRGBA.Blue.clone());
		clBg = ColorRGBA.Cyan.clone();
		attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
		attrs = styles.getSelector(DialogStyleElementId.SystemAlert.s(), STYLE_CONSOLE);
		attrs.set("color", ColorRGBA.Blue.clone());
		clBg = ColorRGBA.Yellow.clone();clBg.a=0.75f;
		attrs.set(Label.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		//TODO not working? 		attrs.set(Label.LAYER_INSETS, new Insets3f(5f, 5f, 5f, 5f));
		//TODO not working? 		attrs.set(Label.LAYER_BORDER, new QuadBackgroundComponent(ColorRGBA.Red.clone()));
		
		attrs = styles.getSelector(DialogStyleElementId.ResizeBorder.s(), STYLE_CONSOLE);
		clBg = ColorRGBA.Cyan.clone();
		attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
//		Effect<Button> efHighLightBkgNeg = new AbstractEffect<Button>("HighLight") {
//			@Override
//			public Animation create(final Button target, final EffectInfo existing) {
//				final QuadBackgroundComponent gcBg = (QuadBackgroundComponent)target.getBackground();
//				final ColorRGBA colorBkp = gcBg.getColor();
//				return new Animation() {
//					@Override	public void cancel() {
//						gcBg.setColor(colorBkp);
//					}
//					@Override	public boolean animate(double tpf) {
//						if(existing.getAnimation()==this)return true;
//						gcBg.setColor(MiscJmeI.i().negateColor(colorBkp));
//						return true;
//					}
//				};
//			}
//		};
//		attrs.set(Button.EFFECT_ACTIVATE, efHighLightBkgNeg);
////		attrs.set(Button.EFFECT_DEACTIVATE, efHighLightBkgNeg);
		
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
	
	public ArrayList<LemurDialogStateAbs> getDialogListCopy() {
		return super.getDialogListCopy(LemurDialogStateAbs.class);
	}
	
	public LemurDialogManagerI() {
		super();
		ManageSingleInstanceI.i().add(this);
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		for(LemurDialogStateAbs diag:getDialogListCopy(LemurDialogStateAbs.class)){
			fixDialogPosition(diag);
		}
	}

	public boolean fixDialogPosition(LemurDialogStateAbs diag) {
		Vector3f v3fPos = diag.getDialogMainContainer().getLocalTranslation();
		Vector3f v3fSize = diag.getDialogMainContainer().getSize();
		//TODO get current app window size
		return false;
	}
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurDialogManagerI.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurDialogManagerI.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
	
	/**
	 * this is just to let actual effects to end themselves with their own cancel()
	 */
	public static class DummyEffect extends AbstractEffect{
		private String	strId;
		public DummyEffect(String strId, String channel){
			super(channel);
			this.strId=strId;
		}
		
		public String getId(){
			return this.strId;
		}
		
		@Override
		public Animation create(Object target, EffectInfo existing) {
			return new Animation() {
				@Override	public void cancel() {}
				@Override	public boolean animate(double tpf) {return true;}
			};
		}
	}
	
	/**
	 * 
	 * @param pnl
	 * @param strEffectId
	 * @param ef
	 * @param efDummy can be null initially, use a field variable
	 * @return dummy effect for re-use
	 */
	public DummyEffect setupSimpleEffect(Panel pnl, String strEffectId, Effect ef, DummyEffect efDummy){
		String strDummyId="DummyEffectUniqueId";
		if(efDummy==null)efDummy=new DummyEffect(strDummyId,ef.getChannel());
		
		if(!efDummy.getChannel().equals(ef.getChannel())){
			throw new PrerequisitesNotMetException("both should be on the same channel", efDummy, strEffectId, ef, pnl, this);
		}
		
		if(strEffectId.equals(strDummyId)){
			throw new PrerequisitesNotMetException("ids should differ", strDummyId, efDummy, strEffectId, ef, pnl, this);
		}
		
		pnl.addEffect(strEffectId, (Effect)ef);
		pnl.addEffect(strDummyId, efDummy);
		
		return efDummy;
	}

	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}
	
//	public static class LmrDiagMgrCS extends DiagMgrCS<LemurDialogManagerI>{
//	}
}
