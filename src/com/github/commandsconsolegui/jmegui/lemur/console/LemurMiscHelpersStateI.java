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

package com.github.commandsconsolegui.jmegui.lemur.console;

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.SavableHolder;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.lemur.extras.CellRendererDialogEntry.Cell;
import com.github.commandsconsolegui.misc.IWorkAroundBugFix;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexHacks;
import com.jme3.font.BitmapText;
import com.jme3.input.dummy.DummyKeyInput;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.DocumentModel;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.focus.FocusManagerState;

/**
 * Is an app state because of the text blinking cursor.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class LemurMiscHelpersStateI extends CmdConditionalStateAbs implements IWorkAroundBugFix, IConsoleCommandListener {
	private static LemurMiscHelpersStateI instance = new LemurMiscHelpersStateI();
	public static LemurMiscHelpersStateI i(){return instance;}
	
	public final BoolTogglerCmdField	btgListBoxSelectorAsUnderline = 
		new BoolTogglerCmdField(this,true,null,
			" BUGFIX: this also work as a workaround/bugfix: "
			+" listbox.selectorArea is above listbox entry button and"
			+" below button's text, so mouse cursor over event only happens when over button text "
			+" but not over button area without text..."
		).setCallNothingOnChange();
	
	public final BoolTogglerCmdField	btgTextCursorOscilateFadeBlinkMode = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgTextCursorLarge = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	
	public final StringCmdField CMD_FIX_INVISIBLE_TEXT_CURSOR = new StringCmdField(this, CommandsDelegator.strFinalCmdCodePrefix);

//	private SimpleApplication	sapp;
	
//	public void initialize(SimpleApplication sapp){
//		this.sapp = sapp;
//	}
	
	private TimedDelayVarField tdTextCursorBlink = new TimedDelayVarField(this,1f,"blink delay");
	private boolean	bBlinkingTextCursor = true;
	private FocusManagerState	focusState;
	private TextField	tfToBlinkCursor;

//	private boolean	bInitialized;
//	private boolean	bEnabled = true;

//	private CommandsDelegatorI	cd;

//	private boolean	bFixInvisibleTextInputCursor=false;

//	private boolean	bBlinkFadeInAndOut=true;

	private enum EUserData{ //TODO EUserData
		geomCursorHotLink,
		bCursorLargeMode,
		
		matForBlinking,
		colorForBlinking,
		
//		tdListboxSelectorColorFade,
		geomListboxSelector,
		;
	}

//	private int	iMoveCaratTo;
	
	public LemurMiscHelpersStateI() {
		setPrefixCmdWithIdToo(true);
	}
	
	public Geometry getTextCursorFrom(TextField tf){
		//TODO is there some way to get the cursor geometry in a lemur way?
		return (Geometry) getBitmapTextFrom(tf).getChild("cursor");
//				
//		Geometry geomCursor = null;
//		for(Spatial spt:tf.getChildren()){
//			if(spt instanceof BitmapText){
//				geomCursor = (Geometry)((BitmapText)spt).getChild("cursor"); //as set at TextEntryComponent()
//				break;
//			}
//		}
//		
//		return geomCursor;
	}
	
	public BitmapText getBitmapTextFrom(TextField tf){
		for(Spatial spt:tf.getChildren()){
			if(spt instanceof BitmapText)return (BitmapText)spt;
		}
		
		return null;
	}
	
	/**
	 * There can only have one single input target at a time.
	 * @param tf
	 */
	public void setTextFieldInputToBlinkCursor(TextField tf){
		this.tfToBlinkCursor = tf;
	}
	
	private ColorRGBA retrieveExclusiveColorForBlinking(Spatial sptUserDataHolder, Geometry geomCursor){
		Material matCursorOnly = sptUserDataHolder.getUserData(EUserData.matForBlinking.toString());
//		matCursorOnly=null;
		/**
		 * check if cursor material was updated outside here
		 */
		if(matCursorOnly==null || !matCursorOnly.equals(geomCursor.getMaterial())){
			/**
			 * The material is shared with the cursor and the text.
			 * As the material may affect also the text, and a fading text is horrible
			 * using a cloned material to substitute it.
			 */
			sptUserDataHolder.setUserData(MiscJmeI.EUserData.matCursorBkp.toString(), geomCursor.getMaterial());
			matCursorOnly = geomCursor.getMaterial().clone();
			MatParam param = matCursorOnly.getParam("Color");
			ColorRGBA colorClone = ((ColorRGBA)param.getValue()).clone();
			matCursorOnly.setColor("Color", colorClone);
//			matCursorOnly.setParam("Color", ColorRGBA.class, matCursorOnly.getParam("Color").clone());
			
			geomCursor.setMaterial(matCursorOnly);
//		fixInvisibleCursor(geomCursor);
			sptUserDataHolder.setUserData(EUserData.matForBlinking.toString(), matCursorOnly);
			sptUserDataHolder.setUserData(EUserData.colorForBlinking.toString(), colorClone);
		}
		
		return (ColorRGBA)sptUserDataHolder.getUserData(EUserData.colorForBlinking.toString());
	}
	
//	@Deprecated
//	private void _updateBlinkInputFieldTextCursor(TextField tf) {
//		if(!bBlinkingTextCursor)return;
//		if(!tf.equals(LemurFocusHelperStateI.i().getFocused()))return;
//		
////		tdTextCursorBlink.updateTime();
//		
//		Geometry geomCursor = tf.getUserData(EUserData.CursorHotLink.toString());
//		if(geomCursor==null){
//			geomCursor = getTextCursorFrom(tf);
//			tf.setUserData(EUserData.CursorHotLink.toString(), geomCursor);
//		}
//		
////		BitmapText bmt = getBitmapTextFrom(tf);
////		((BitmapTextPage)bmt.getChild("BitmapFont")).
//		
//		long lDelay = tdTextCursorBlink.getCurrentDelayNano();
//		
//		if(btgTextCursorPulseFadeBlinkMode.b()){
//			checkAndPrepareExclusiveCursorMaterialFor(tf,geomCursor);
//			
//			ColorRGBA color = tf.getUserData(EUserData.ExclusiveCursorColor.toString());
//			color.a = tdTextCursorBlink.getCurrentDelayPercentualDynamic();
//			if(bBlinkFadeInAndOut){
//				if(color.a>0.5f)color.a=1f-color.a; //to allow it fade in and out
//				color.a*=2f;
////				if(color.a<0.75f)color.a=0.75f;
//			}
//			if(color.a<0)color.a=0;
//			if(color.a>1)color.a=1;
//		}else{
//			Material matBkp = tf.getUserData(MiscJmeI.EUserData.CursorMaterialBkp.toString());
//			if(matBkp!=null){
//				geomCursor.setMaterial(matBkp);
//				tf.setUserData(MiscJmeI.EUserData.CursorMaterialBkp.toString(),null); //clear
//			}
//			
//			if(lDelay > tdTextCursorBlink.getDelayLimitNano()){
//				if(geomCursor.getCullHint().compareTo(CullHint.Always)!=0){
//					geomCursor.setCullHint(CullHint.Always);
//				}else{
//					bugFix(null,btgBugFixInvisibleCursor,tf);
////					bugFix(EBugFix.InvisibleCursor,tf);
////					fixInvisibleCursor(geomCursor);
//					geomCursor.setCullHint(CullHint.Inherit);
//				}
//				
//				tdTextCursorBlink.updateTime();
//			}
//		}
//			
//	}
	
	private void updateBlinkInputFieldTextCursor(TextField tf) {
		if(!bBlinkingTextCursor)return;
		if(!tf.equals(LemurFocusHelperStateI.i().getFocused()))return;
		
		Geometry geomCursor = tf.getUserData(EUserData.geomCursorHotLink.toString());
		if(geomCursor==null){
			geomCursor = getTextCursorFrom(tf);
			tf.setUserData(EUserData.geomCursorHotLink.toString(), geomCursor);
		}
		
		if(btgTextCursorOscilateFadeBlinkMode.b()){
			ColorRGBA color = retrieveExclusiveColorForBlinking(tf,geomCursor);
		//(ColorRGBA)tf.getUserData(EUserData.colorExclusiveCursor.toString()),
			MiscJmeI.i().updateColorFading(tdTextCursorBlink, color, true);
		}else{
			if(MiscJmeI.i().updateBlink(tdTextCursorBlink, tf, geomCursor)){
				bugFix(null,btgBugFixInvisibleCursor,tf);
			}
		}
	}
	
	public TimedDelayVarField retrieveTimedDelayFrom(Spatial sptHolder, String strUserDataKey){
		SavableHolder sh = (SavableHolder)sptHolder.getUserData(strUserDataKey);
		TimedDelayVarField td = null;
		if(sh==null){
			sh = new SavableHolder(new TimedDelayVarField(2f,"").setActive(true));
			sptHolder.setUserData(strUserDataKey, sh);
		}
		td = (TimedDelayVarField)sh.getRef();
		
		return td;
	}
	
	public void updateBlinkListBoxSelector(ListBox<?> lstbx){//, boolean bShrinkExpandToo) {
		Geometry geomSelector = retrieveSelectorGeometryFromListbox(lstbx);
		if(geomSelector==null)return;
		
//		Geometry geomSelector = (Geometry)lstbx.getUserData(EUserData.geomListboxSelector.toString());
//		if(geomSelector==null){
//			geomSelector=getSelectorGeometryFromListbox(lstbx);
//			lstbx.setUserData(EUserData.geomListboxSelector.toString(), geomSelector);
//		}
		ColorRGBA color = retrieveExclusiveColorForBlinking(lstbx,geomSelector);
//		ColorRGBA color = (ColorRGBA)lstbx.getUserData(EUserData.colorExclusiveCursor.toString());
		
		TimedDelayVarField td = retrieveTimedDelayFrom(lstbx, "tdListboxSelectorColorFade");
		MiscJmeI.i().updateColorFading(
			td, 
			color, 
			true);
		
//		if(bShrinkExpandToo){
//			geomSelector.getLocalScale().x = 0.5f + (td.getCurrentDelayPercentualDynamic() - 0.5f);
////			geomSelector.update
//		}
	}	
	
	private void updateLargeTextCursorMode(TextField tf){
		if(btgTextCursorLarge.b()){
			if(tf.getUserData(EUserData.bCursorLargeMode.toString())==null){
				enableLargeCursor(tf,true);
			}
		}else{
			if(tf.getUserData(EUserData.bCursorLargeMode.toString())!=null){
				enableLargeCursor(tf,false);
			}
		}
	}
	
	private void enableLargeCursor(TextField tf, boolean b){
		Geometry geomCursor = getTextCursorFrom(tf);
		geomCursor.setLocalScale(b?3f:1f/3f,1f,1f);
		tf.setUserData(EUserData.bCursorLargeMode.toString(), b?true:null);
	}
	
	/**
	 * To show the cursor at the new carat position, 
	 * this required private method: {@link TextEntryComponent#resetCursorPosition}
	 * must be reached in some way...
	 * 
	 * @param tf
	 * @param iMoveCaratTo
	 * @param tec 
	 */
	public void setCaratPosition(TextField tf, int iMoveCaratTo) {
		// position carat properly
		DocumentModel dm = tf.getDocumentModel();
		dm.home(true);
		for(int i=0;i<iMoveCaratTo;i++){
			dm.right();
		}
		
		bugFix(null,btgBugFixUpdateTextFieldTextAndCaratVisibility,tf);
//		bugFix(EBugFix.UpdateTextFieldTextAndCaratVisibility,tf);
//		resetCursorPosition(tf);
	}
	
//	/**
//	 * This updates the displayed text cursor position.
//	 * 
//	 * This below is actually a trick, 
//	 * because this flow will finally call the required method.
//	 *  
//	 * @param tf
//	 */
//	public void resetCursorPosition(TextField tf){
//		tf.setFontSize(tf.getFontSize()); //resetCursorPositionHK(tf);
//	}
	@Deprecated
	public void resetCursorPositionHK(TextField tf){
		TextEntryComponent tec = ((TextEntryComponent)ReflexHacks.i().getFieldValueHK(tf, "text"));
		ReflexHacks.i().callMethodHK(tec,"resetCursorPosition");
	}

	public Float guessEntryHeight(ListBox<?> listBox){
		if(listBox.getGridPanel().getChildren().isEmpty())return null;
		
		Button	btnFixVisibleRowsHelper = null;
		for(Spatial spt:listBox.getGridPanel().getChildren()){
			if(spt instanceof Button){
				btnFixVisibleRowsHelper = (Button)spt;
				break;
			}
		}
		if(btnFixVisibleRowsHelper==null)return null;
		
		return MiscJmeI.i().retrieveBitmapTextFor(btnFixVisibleRowsHelper).getLineHeight();
	}

//	@Override
//	public void initialize(AppStateManager stateManager, Application app) {
//		this.sapp = (SimpleApplication) app;
//		tdTextCursorBlink.updateTime();
//		
////		cc.addConsoleCommandListener(this);
////		ReflexFill.assertReflexFillFieldsForOwner(this);
//		
//		bInitialized=true;
//	}
	
//	enum EBugFix{
//		InvisibleCursor,
//		UpdateTextFieldTextAndCaratVisibility,
//	}
	
	/**
	 * the geom may not have been created yet by lemur.
	 * 
	 * @param lstbx
	 * @return can be null for awhile...
	 */
	public Geometry retrieveSelectorGeometryFromListbox(ListBox<?> lstbx){
		Geometry geomSelector = (Geometry)lstbx.getUserData(EUserData.geomListboxSelector.toString());
		
		/**
		 * this is guess work...
		 * the geometry could have a name... :)
		 */
		if(geomSelector==null){
			Node nodeSelectorArea = (Node)lstbx.getChild("selectorArea");
			labelFound:for(Spatial sptPanel:nodeSelectorArea.getChildren()){
				if(sptPanel instanceof Panel){
					Panel pnlSelectorArea = (Panel)sptPanel;
					for(Spatial sptGeom:pnlSelectorArea.getChildren()){
						if(sptGeom instanceof Geometry){
							geomSelector=(Geometry)sptGeom;
							break labelFound;
						}
					}
				}
			}
			
			lstbx.setUserData(EUserData.geomListboxSelector.toString(), geomSelector);
		}
		
		return geomSelector;
	}
	
	public void listboxSelectorAsUnderline(ListBox<?> lstbx){
		if(!btgListBoxSelectorAsUnderline.b())return;
		
		Geometry geomSelectorArea = retrieveSelectorGeometryFromListbox(lstbx);
		if(geomSelectorArea!=null){
			geomSelectorArea.getLocalScale().y=0.1f;
		}
	}
	
	BoolTogglerCmdField btgBugFixInvisibleCursor = 
		new BoolTogglerCmdField(this,true).setHelp("in case text cursor is invisible").setCallNothingOnChange();
	BoolTogglerCmdField btgBugFixUpdateTextFieldTextAndCaratVisibility = 
		new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	@Override
	public <T> T bugFix(Class<T> clReturnType, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams){
		if(!btgBugFixId.b())return null;
		
		boolean bFixed = false;
		
//		if(btgBugFixListBoxSelectorArea.isEqualToAndEnabled(btgBugFixId)){
//			ListBox<?> lstbx = (ListBox<?>)aobjCustomParams[0];
//			
//			Geometry geomSelectorArea = getSelectorGeometryFromListbox(lstbx);
//			if(geomSelectorArea!=null){ 
//				geomSelectorArea.setLocalScale(1f, 0.1f, 1f); //this makes the selectorArea looks like an underline! quite cool!
//			}
//			
//			bFixed=true;
//		}else
		if(btgBugFixUpdateTextFieldTextAndCaratVisibility.isEqualToAndEnabled(btgBugFixId)){
			/**
			 * This updates the displayed text cursor position.
			 * 
			 * This below is actually a trick, 
			 * because this flow will finally call the required method.
			 * {@link TextEntryComponent#resetCursorPosition}
			 *  
			 * @param tf
			 */
			if(aobjCustomParams[0] instanceof TextField){
				TextField tf = (TextField)aobjCustomParams[0];
				tf.setFontSize(tf.getFontSize());
				bFixed=true;
			}				
		}else
		if(btgBugFixInvisibleCursor.isEqualToAndEnabled(btgBugFixId)){
			/**
			 * To the point, but unnecessary.
			 * see {@link TextEntryComponent#resetCursorColor()}
			 */
			if(aobjCustomParams[0] instanceof Geometry){
				Geometry geomCursor = (Geometry)aobjCustomParams[0];
//				if(!bFixInvisibleTextInputCursor)return null;
				
//			getBitmapTextFrom(tf).setAlpha(1f); //this is a fix to let text cursor be visible.
				geomCursor.getMaterial().setColor("Color",ColorRGBA.White.clone());
				bFixed=true;
			}else
			if(aobjCustomParams[0] instanceof TextField){
				TextField tf = (TextField)aobjCustomParams[0];
//				if(!bFixInvisibleTextInputCursor)return null;
				
				String strInvisibleCursorFixed="InvisibleCursorFixed";
				if(tf.getUserData(strInvisibleCursorFixed)!=null)return null;
				
				BitmapText bmt = getBitmapTextFrom(tf);
				
				/**
				 * The BitmapText base alpha is set to an invalid value -1.
				 * That value seems to be used as a marker/indicator of "invalidity?".
				 * But the problem is, it is used as a normal value and never verified/validadted 
				 * towards its invalidity of -1.
				 * Wouldnt have been better if it was used 'null' as indicator of invalidity?
				 * 
				 * This flow will fix that base alpha to fully visible.
				 * -> BitmapText.letters.setBaseAlpha(alpha);
				 */
				bmt.setAlpha(1f); // alpha need to be fixed, it was -1; -1 is an invalid value used as a merker/indicator, woulndt be better it be a null marker?
				
				/**
				 * This flow will apply the base alpha of BitmapText to the text cursor.
				 * -> TextField.text.setColor(color)->resetCursorColor()
				 */
				tf.setColor(tf.getColor());
				
				tf.setUserData(strInvisibleCursorFixed,true);
				bFixed=true;
			}				
		}
		
		if(!bFixed){
			throw new PrerequisitesNotMetException("cant bugfix this way...",aobjCustomParams);
		}
		
		return null;
	}
	
//	@Override
//	public Object bugFix(Object... aobj) {
//		boolean bFixed = false;
//		EBugFix e = (EBugFix)aobj[0];
//		switch(e){
//			case InvisibleCursor:{
//				/**
//				 * To the point, but unnecessary.
//				 * see {@link TextEntryComponent#resetCursorColor()}
//				 */
//				if(aobj[1] instanceof Geometry){
//					Geometry geomCursor = (Geometry) aobj[1];
//					if(!bFixInvisibleTextInputCursor)return null;
////				getBitmapTextFrom(tf).setAlpha(1f); //this is a fix to let text cursor be visible.
//					geomCursor.getMaterial().setColor("Color",ColorRGBA.White.clone());
//					bFixed=true;
//				}else
//				if(aobj[1] instanceof TextField){
//					TextField tf = (TextField) aobj[1];
//					
//					if(!bFixInvisibleTextInputCursor)return null;
//					
//					String strInvisibleCursorFixed="InvisibleCursorFixed";
//					if(tf.getUserData(strInvisibleCursorFixed)!=null)return null;
//					
//					BitmapText bmt = getBitmapTextFrom(tf);
//					
//					/**
//					 * The BitmapText base alpha is set to an invalid value -1.
//					 * That value seems to be used as a marker/indicator of "invalidity?".
//					 * But the problem is, it is used as a normal value and never verified/validadted 
//					 * towards its invalidity of -1.
//					 * Wouldnt have been better if it was used 'null' as indicator of invalidity?
//					 * 
//					 * This flow will fix that base alpha to fully visible.
//					 * -> BitmapText.letters.setBaseAlpha(alpha);
//					 */
//					bmt.setAlpha(1f); // alpha need to be fixed, it was -1; -1 is an invalid value used as a merker/indicator, woulndt be better it be a null marker?
//					
//					/**
//					 * This flow will apply the base alpha of BitmapText to the text cursor.
//					 * -> TextField.text.setColor(color)->resetCursorColor()
//					 */
//					tf.setColor(tf.getColor());
//					
//					tf.setUserData(strInvisibleCursorFixed,true);
//					bFixed=true;
//				}				
//			}break;
//			case UpdateTextFieldTextAndCaratVisibility:{
//				/**
//				 * This updates the displayed text cursor position.
//				 * 
//				 * This below is actually a trick, 
//				 * because this flow will finally call the required method.
//				 * {@link TextEntryComponent#resetCursorPosition}
//				 *  
//				 * @param tf
//				 */
//				if(aobj[1] instanceof TextField){
//					TextField tf = (TextField) aobj[1];
//					tf.setFontSize(tf.getFontSize());
//					bFixed=true;
//				}				
//			}break;
//		}
//		
//		if(!bFixed){
//			throw new PrerequisitesNotMetException("cant bugfix this way...",aobj);
//		}
//		
//		return null;
//	}

	@Override
	protected boolean initAttempt() {
		tdTextCursorBlink.updateTime();
		return super.initAttempt();
	}

	@Override
	protected boolean updateAttempt(float tpf) {
//		LemurFocusHelperStateI.i().update(tpf);
		
		if(tfToBlinkCursor!=null){
			updateBlinkInputFieldTextCursor(tfToBlinkCursor);
			updateLargeTextCursorMode(tfToBlinkCursor);
		}
		
		return super.updateAttempt(tpf);
	}
	
	/**
	 * Keep this as implementation methodology. Look at super classes also.
	 */
	public static class CfgParm implements ICfgParm{}
	@Override
	public LemurMiscHelpersStateI configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
		
//		super.icfgOfInstance=icfg;
		
//		super.configure(new CmdConditionalStateAbs.CfgParm(LemurMiscHelpersStateI.class.getSimpleName()));
		super.configure(new CmdConditionalStateAbs.CfgParm(null));
		
		return storeCfgAndReturnSelf(icfg);
	}

//	public void initializeSpecialKeyListeners(TextEntryComponent source) {
//		source.
//	}

	public void insertTextAtCaratPosition(TextField tf, String str) {
		DocumentModel dm = tf.getDocumentModel();
		for(int i=0;i<str.length();i++)dm.insert(str.charAt(i));
		bugFix(null,btgBugFixUpdateTextFieldTextAndCaratVisibility,tf);
//		bugFix(EBugFix.UpdateTextFieldTextAndCaratVisibility, tf);
	}

	/**
	 * 
	 * @param strActionPerformedHelp the key code's field name will be reflexed
	 * @param kal
	 * @param iKeyCode
	 * @param aiKeyModifiers
	 * @return better if stored into some kind of array for further line formatting 
	 */
	public BindKey bindKey(TextField tf, KeyActionListener kal, String strActionPerformedHelp, int iKeyCode, int... aiKeyModifiers){
		KeyAction ka = new KeyAction(iKeyCode, aiKeyModifiers);
		tf.getActionMap().put(ka, kal);
		
		BindKey bk = new BindKey();
		
		bk.tfInputTarget = tf;
		
		bk.kalAction = kal;
		
		bk.iCode = iKeyCode;
		bk.strName = MiscI.i().getFieldNameForValue(new DummyKeyInput(), iKeyCode, "/", "KEY_", true);
//		bk.strName = MiscI.i().makePretty(bk.strName, true);
		
		bk.strHelp = strActionPerformedHelp;
		
		bk.aiModifiers = aiKeyModifiers;
		
		for(int i:aiKeyModifiers){
			if(i==KeyAction.CONTROL_DOWN){
				bk.strModifiers += "Ctrl+";
			}else{
				bk.strModifiers += MiscI.i().getFieldNameForValue(ka, i, "/", null, true)+"+";
			}
		}
		
		return bk;
	}
//	public String prettyKeyName(String strPrefixRemove, String strFullName){
//		if(strFullName.startsWith(strPrefixRemove)){
//			strFullName = strFullName.substring(strPrefixRemove.length());
//		}
//		return MiscI.i().makePretty(strFullName, true);
//	}
	public static class BindKey{
		TextField tfInputTarget = null;
		KeyActionListener	kalAction = null;
		
		Integer iCode = null;
		int[] aiModifiers = null;
		
		String strName = null;
		String strModifiers = "";
		
		String strHelpContext = null;
		String strHelp = null;
		
		public String getHelp() {
			return strModifiers+strName+": "+strHelp;
		}
	}
	
	StringCmdField scfCheckLemur = new StringCmdField(this);
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(
			cc.checkCmdValidity(this,scfCheckLemur, 
				"[strName] if name is provided, check only the spatials matching it"
//				"[fNewZSize] go thru all lemur elemets active on the GUI node to check if they are "
//				+"all properly configured."
//				+"The option will only modify Z thickness if it is 0.0f."
			)
		){
//			Float fNewZSize = cc.getCurrentCommandLine().paramFloat(1);
			String strName = cc.getCurrentCommandLine().paramString(1);
			
			Spatial spt = GlobalGUINodeI.i();
			
			if(strName!=null){
				spt=GlobalGUINodeI.i().getChild(strName);
				if(spt==null)cc.dumpWarnEntry("spatial not found with name: "+strName);
			}
			
			if(spt!=null){
				checkElementsRecursive(spt, null);//fNewZSize);
			}
			
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cc);
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}

	public void checkElementsRecursive(Spatial spt, Float fNewZSize) {
		float zL=spt.getLocalTranslation().z;
		float zW=spt.getWorldTranslation().z;
		Float z=null;
		Float zP=null;
		
		Panel panel = (spt instanceof Panel) ? (Panel)spt : null;
		
		if(panel!=null){
			Vector3f v3fSize = panel.getSize();
			Vector3f v3fPrefSize = panel.getPreferredSize();
			
			if(Float.compare(v3fSize.z,0.0f)==0 && Float.compare(v3fPrefSize.z,0.0f)==0){
				GlobalCommandsDelegatorI.i().dumpProblemEntry(
//					new InvalidAttributesException(
					"lemur element "+panel.getClass().getSimpleName()+"(name)="+panel.getName()
						+" thickness is squashed (z=0.0)",
					panel.getClass().getName(),
					panel,
					"style:"+panel.getStyle(),
					"size:"+panel.getSize(),
					"prefSize:"+panel.getPreferredSize(),
					panel.getElementId(),
					panel.getName(),
					panel.getParent());
			}
			
			z=v3fSize.z;
			if(fNewZSize!=null && Float.compare(v3fSize.z,0.0f)==0 ){
				v3fSize.z=fNewZSize;
				panel.setSize(v3fSize);
			}
			
			zP=v3fPrefSize.z;
			if(v3fPrefSize!=null){
				if(fNewZSize!=null && Float.compare(v3fPrefSize.z,0.0f)==0 ){
					v3fPrefSize.z=fNewZSize;
					setGrantedSize(panel,v3fPrefSize,false);
				}
			}
			
		}
		
		String strIndent="";
		Spatial sptTmp=spt;
		while(sptTmp.getParent()!=null){
			strIndent+=" ";
			sptTmp=sptTmp.getParent();
		}
		
		GlobalCommandsDelegatorI.i().dumpSubEntry(strIndent
				+"z="+(z==null?z:MiscI.i().fmtFloat(z))+";"
				+"zP="+(zP==null?zP:MiscI.i().fmtFloat(zP))+";"
				+"zL="+MiscI.i().fmtFloat(zL)+";"
				+"zW="+MiscI.i().fmtFloat(zW)+";"
				+elementInfo(spt,false)+";"
			);
		
		if(spt instanceof Node){
			Node node = (Node)spt;
			for(Spatial sptChild:node.getChildren()){
				checkElementsRecursive(sptChild,fNewZSize);
			}
		}
	}
	
	public String elementInfo(Spatial spt, boolean bShowParentTreeOnNullName){
		if(spt.getName()==null && bShowParentTreeOnNullName){
			ArrayList<Spatial> aspt = MiscJmeI.i().getParentListFrom(spt,true);
			String str="";
			for(Spatial sptParent:aspt){
				if(!str.isEmpty())str+=" <- ";
				str+=sptParent.getClass().getSimpleName()+"(name)='"+sptParent.getName()+"'";
			}
			
			return str;
		}
		
		return spt.getClass().getSimpleName()+"(name)='"+spt.getName()+"'";
	}
	
//	public static final float fPreferredThickness = 5f; //thickness z=5f to distinguish what was set by this library
	public static final float fPreferredThickness = 1f; //thickness z=1f to match lemur one and do not mess with its calculations, TODO try to collect it from lemur...
	Vector3f v3fMinSize=new Vector3f(10f,10f,fPreferredThickness); 
	
	/**
	 * see {@link #setGrantedSize(Panel, Vector3f, boolean)}
	 * 
	 * @param pnl
	 * @param fX
	 * @param fY
	 * @param bEnsureSizeNow
	 * @return
	 */
	public Vector3f setGrantedSize(Panel pnl, float fX, float fY, boolean bEnsureSizeNow){
		return setGrantedSize(pnl, new Vector3f(fX,fY,-1), bEnsureSizeNow); //z=-1 will be fixed
	}
	/**
	 * Sets size properly, acurately, precisely,
	 * without pitfalls.
	 * Make it sure the thickness is correct (not 0.0f).
	 * 
	 * @param pnl
	 * @param v3fSize x,y,z use -1 to let it be automatic = preferred
	 * @param bEnsureSizeNow this means that the Preferred size will be used now!
	 * @return
	 */
	public Vector3f setGrantedSize(final Panel pnl, final Vector3f v3fSize, final boolean bEnsureSizeNow){
		if(v3fSize.x<v3fMinSize.x)v3fSize.x=v3fMinSize.x;
		if(v3fSize.y<v3fMinSize.y)v3fSize.y=v3fMinSize.y;
		if(v3fSize.z<v3fMinSize.z)v3fSize.z=v3fMinSize.z;
		
		Vector3f v3fP = pnl.getPreferredSize();
		if(v3fSize.x<v3fP.x)v3fSize.x=v3fP.x;
		if(v3fSize.y<v3fP.y)v3fSize.y=v3fP.y;
		v3fSize.z=v3fP.z; // do not mess with Z !!!
		//if(v3fSize.z<v3fP.z)v3fSize.z=v3fP.z;
		
		pnl.setPreferredSize(v3fSize); 
		
//		pnl.setSize(v3fSize); //pnl.getPreferredSize(); pnl.getSize();
//		
//		// check on the next frame, so lemur possibly had time to make its calculations. TODO how to be sure lemur did it?
//		CallableX caller = new CallableX() {
//			@Override
//			public Boolean call() throws Exception {
//				boolean bUsePreferred = bEnsureSizeNow;
//				
//				if(!bUsePreferred && pnl.getSize().distance(v3fSize)>0.01f){
//					GlobalCommandsDelegatorI.i().dumpDevWarnEntry(
//						"setSize() failed, using setPreferredSize() for "+elementInfo(pnl,true), 
//						pnl,
//						pnl.getClass().getName(),
//						pnl.getName(),
//						"size="+pnl.getSize(),
//						"requestedSize="+v3fSize);
//					
//					bUsePreferred=true;
//				}
//				
//				if(bUsePreferred){
//					//TODO double/tripple try setSize() before preferred?
//					pnl.setPreferredSize(v3fSize); 
//					//TODO check after preferred?
//					
//					return true; //cuz could also return false here for some reason
//				}
//				
//				return true;
//			}
//		};
//		CallQueueI.i().addCall(caller.setAsPrepend(),bEnsureSizeNow);
		
//		if(bForcedSizeNow){
//			try {
//				caller.call();
//			} catch (Exception e) {
//				
//				e.printStackTrace();
//			}
//		}else{
//			CallQueueI.i().addCall(caller);
//		}
		
		return v3fSize;
	}

	public Cell<?> getCellFor(Spatial source) {
		Cell<?> cell=null;
		for(Spatial sptParent:MiscJmeI.i().getParentListFrom(source, false)){
			if(sptParent instanceof Cell<?>){
				cell = (Cell<?>)sptParent;
				break;
			}
		}
		
		return cell;
	}

	public LemurMiscHelpersStateI setLocationXY(Spatial spt, Vector3f v3f) {
		spt.setLocalTranslation(v3f.x, v3f.y, spt.getLocalTranslation().z); // to not mess with Z order
		return this;
	}

	public LemurMiscHelpersStateI setScaleXY(Spatial spt, Float fScaleX, Float fScaleY) {
		Vector3f v3fCurrentScale = spt.getLocalScale();
		spt.setLocalScale(
			fScaleX==null?v3fCurrentScale.x:fScaleX,
			fScaleY==null?v3fCurrentScale.y:fScaleY,
			v3fCurrentScale.z); // to not mess with Z order
		return this;
	}
}
