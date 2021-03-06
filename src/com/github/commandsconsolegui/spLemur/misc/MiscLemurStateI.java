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

package com.github.commandsconsolegui.spLemur.misc;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.globals.GlobalMainThreadI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MiscI.EStringMatchMode;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spCmd.CommandsHelperI;
import com.github.commandsconsolegui.spCmd.IConsoleCommandListener;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI;
import com.github.commandsconsolegui.spCmd.misc.WorkAroundI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spCmd.misc.WorkAroundI.BugFixBoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spCmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI;
import com.github.commandsconsolegui.spJme.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.spJme.globals.GlobalGUINodeI;
import com.github.commandsconsolegui.spJme.globals.GlobalManageDialogJmeI;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.github.commandsconsolegui.spLemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.spLemur.console.LemurDiagFocusHelperStateI;
import com.github.commandsconsolegui.spLemur.dialog.ManageLemurDialog.DialogStyleElementId;
import com.github.commandsconsolegui.spLemur.extras.CellRendererDialogEntry.CellDialogEntry;
import com.github.commandsconsolegui.spLemur.extras.DialogMainContainer;
import com.jme3.font.BitmapText;
import com.jme3.input.dummy.DummyKeyInput;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.DocumentModel;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.event.AbstractCursorEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.focus.FocusManagerState;
import com.simsilica.lemur.style.ElementId;

/**
 * Is an app state because of the text blinking cursor.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MiscLemurStateI extends CmdConditionalStateAbs<MiscLemurStateI> implements IConsoleCommandListener {
	public static final class CompositeControl extends CompositeControlAbs<MiscLemurStateI>{
		private CompositeControl(MiscLemurStateI casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	private static MiscLemurStateI instance = new MiscLemurStateI();
	public static MiscLemurStateI i(){return instance;}
	
	private final BoolTogglerCmdField	btgHoverHighlight = new BoolTogglerCmdField(this,true).setCallNothingOnChange();

	public final BoolTogglerCmdField	btgListBoxSelectorAsUnderline = 
		new BoolTogglerCmdField(this,true,
			" BUGFIX: this also work as a workaround/bugfix: "
			+" listbox.selectorArea is above listbox entry button and"
			+" below button's text, so mouse cursor over event only happens when over button text "
			+" but not over button area without text..."
		).setCallNothingOnChange();
	
	public final BoolTogglerCmdField	btgTextCursorOscilateFadeBlinkMode = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgTextCursorLarge = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	
	public final StringCmdField CMD_FIX_INVISIBLE_TEXT_CURSOR = new StringCmdField(this, CommandsHelperI.i().getCmdCodePrefix());

//	private SimpleApplication	sapp;
	
//	public void initialize(SimpleApplication sapp){
//		this.sapp = sapp;
//	}
	
	private final TimedDelayVarField tdTextCursorBlink = new TimedDelayVarField(this,1f,"blink delay");
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
	
	public MiscLemurStateI() {
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
			sptUserDataHolder.setUserData(MiscJmeI.EUserDataMiscJme.matCursorBkp.toString(), geomCursor.getMaterial());
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
//		if(!tf.equals(LemurFocusHelperStateI.i().getFocused()))return;
		if(!LemurDiagFocusHelperStateI.i().isDialogFocusedFor(tf))return;
		
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
				WorkAroundI.i().bugFix(btgBugFixInvisibleCursor, tf);
//				bugFix(null,null,btgBugFixInvisibleCursor,tf);
			}
		}
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
		
		TimedDelayVarField td = MiscJmeI.i().retrieveUserDataTimedDelay(lstbx, "tdListboxSelectorColorFade", 2f);
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
		GlobalMainThreadI.assertEqualsCurrentThread();
		
		// position carat properly
		DocumentModel dm = tf.getDocumentModel();
		dm.home(true);
		for(int i=0;i<iMoveCaratTo;i++){
			dm.right();
		}
		
		WorkAroundI.i().bugFix(btgBugFixUpdateTextFieldTextAndCaratVisibility, tf);
//		bugFix(null,null,btgBugFixUpdateTextFieldTextAndCaratVisibility,tf);
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
//	@Deprecated
//	public void resetCursorPositionHK(TextField tf){
//		TextEntryComponent tec = ((TextEntryComponent)ReflexHacks.i().getOrSetFieldValueHK(null, tf, "text", null, false, null));
//		ReflexHacks.i().callMethodHK(tec,"resetCursorPosition");
//	}

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
	
	BugFixBoolTogglerCmdField btgBugFixInvisibleCursor = new BugFixBoolTogglerCmdField(this,"in case text cursor is invisible")
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				if(!isEnabled())return true; //successful skipper
				
				boolean bFixed=false;
				
				/**
				 * To the point, but unnecessary.
				 * see {@link TextEntryComponent#resetCursorColor()}
				 */
				Geometry geomCursor = this.getParamsForMaintenance().getParam(Geometry.class,0);
				TextField tf = this.getParamsForMaintenance().getParam(TextField.class,0);
//				Geometry geomCursor = MiscI.i().getParamFromArray(Geometry.class, aobjCustomParams, 0);
//				TextField tf = MiscI.i().getParamFromArray(TextField.class, aobjCustomParams, 0);
				if(geomCursor!=null){
//					if(!bFixInvisibleTextInputCursor)return null;
					
//				getBitmapTextFrom(tf).setAlpha(1f); //this is a fix to let text cursor be visible.
					geomCursor.getMaterial().setColor("Color",ColorRGBA.White.clone());
					bFixed=true;
				}else
				if(tf!=null){
//					if(!bFixInvisibleTextInputCursor)return null;
					
					String strInvisibleCursorFixed="InvisibleCursorFixed";
					if(tf.getUserData(strInvisibleCursorFixed)!=null)return true;
					
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
				
				return bFixed;
			}
		});
//		.setAsBugFixerMode();
	
	BugFixBoolTogglerCmdField btgBugFixUpdateTextFieldTextAndCaratVisibility = 
		new BugFixBoolTogglerCmdField(this)
			.setCallerAssigned(new CallableX(this) {
				@Override
				public Boolean call() {
					if(!isEnabled())return true; //successful skipper
					
					/**
					 * This updates the displayed text cursor position.
					 * 
					 * This below is actually a trick, 
					 * because this flow will finally call the required method.
					 * {@link TextEntryComponent#resetCursorPosition}
					 *  
					 * @param tf
					 */
					TextField tf = this.getParamsForMaintenance().getParam(TextField.class, 0);
					
					tf.setFontSize(tf.getFontSize());
					
					return true;
				}
			});
//			.setAsBugFixerMode();
	
//	@Override
//	public <BFR> BFR bugFix(Class<BFR> clReturnType, BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams){
//		if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
//		
//		boolean bFixed = false;
//		Object objRet = null;
//		
////		if(btgBugFixListBoxSelectorArea.isEqualToAndEnabled(btgBugFixId)){
////			ListBox<?> lstbx = (ListBox<?>)aobjCustomParams[0];
////			
////			Geometry geomSelectorArea = getSelectorGeometryFromListbox(lstbx);
////			if(geomSelectorArea!=null){ 
////				geomSelectorArea.setLocalScale(1f, 0.1f, 1f); //this makes the selectorArea looks like an underline! quite cool!
////			}
////			
////			bFixed=true;
////		}else
//		if(btgBugFixUpdateTextFieldTextAndCaratVisibility.isEqualToAndEnabled(btgBugFixId)){
//			/**
//			 * This updates the displayed text cursor position.
//			 * 
//			 * This below is actually a trick, 
//			 * because this flow will finally call the required method.
//			 * {@link TextEntryComponent#resetCursorPosition}
//			 *  
//			 * @param tf
//			 */
//			TextField tf = MiscI.i().getParamFromArray(TextField.class, aobjCustomParams, 0);
//			
//			tf.setFontSize(tf.getFontSize());
//			
//			bFixed=true;
//		}else{}
////		if(btgBugFixInvisibleCursor.isEqualToAndEnabled(btgBugFixId)){
////			/**
////			 * To the point, but unnecessary.
////			 * see {@link TextEntryComponent#resetCursorColor()}
////			 */
////			Geometry geomCursor = MiscI.i().getParamFromArray(Geometry.class, aobjCustomParams, 0);
////			TextField tf = MiscI.i().getParamFromArray(TextField.class, aobjCustomParams, 0);
////			if(geomCursor!=null){
//////				if(!bFixInvisibleTextInputCursor)return null;
////				
//////			getBitmapTextFrom(tf).setAlpha(1f); //this is a fix to let text cursor be visible.
////				geomCursor.getMaterial().setColor("Color",ColorRGBA.White.clone());
////				bFixed=true;
////			}else
////			if(tf!=null){
//////				if(!bFixInvisibleTextInputCursor)return null;
////				
////				String strInvisibleCursorFixed="InvisibleCursorFixed";
////				if(tf.getUserData(strInvisibleCursorFixed)!=null)return null;
////				
////				BitmapText bmt = getBitmapTextFrom(tf);
////				
////				/**
////				 * The BitmapText base alpha is set to an invalid value -1.
////				 * That value seems to be used as a marker/indicator of "invalidity?".
////				 * But the problem is, it is used as a normal value and never verified/validadted 
////				 * towards its invalidity of -1.
////				 * Wouldnt have been better if it was used 'null' as indicator of invalidity?
////				 * 
////				 * This flow will fix that base alpha to fully visible.
////				 * -> BitmapText.letters.setBaseAlpha(alpha);
////				 */
////				bmt.setAlpha(1f); // alpha need to be fixed, it was -1; -1 is an invalid value used as a merker/indicator, woulndt be better it be a null marker?
////				
////				/**
////				 * This flow will apply the base alpha of BitmapText to the text cursor.
////				 * -> TextField.text.setColor(color)->resetCursorColor()
////				 */
////				tf.setColor(tf.getColor());
////				
////				tf.setUserData(strInvisibleCursorFixed,true);
////				bFixed=true;
////			}				
////		}
//		
//		if(!bFixed){
//			throw new PrerequisitesNotMetException("cant bugfix this way...",aobjCustomParams);
//		}
//		
//		return MiscI.i().bugFixRet(clReturnType,bFixed, objRet, aobjCustomParams);
////		return (BFR)objRet;
//	}
//	
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
		bAllowMinSizeCheckAndFix=true;
//		LemurFocusHelperStateI.i().update(tpf);
		
		if(tfToBlinkCursor!=null){
			updateBlinkInputFieldTextCursor(tfToBlinkCursor);
			updateLargeTextCursorMode(tfToBlinkCursor);
		}
		
		if(strPopupHelp!=null){
			if(lblPopupHelp==null){
				lblPopupHelp = new Label("nothing yet...", new ElementId(DialogStyleElementId.PopupHelp.s()), GlobalManageDialogJmeI.i().STYLE_CONSOLE);
			}
			lblPopupHelp.setName("Popup Help/Hint Label");
			lblPopupHelp.setText("["+strPopupHelp+"]");
			Vector2f v2f = ManageMouseCursorI.i().getMouseCursorPositionCopy();
			
			Vector3f v3fSize = lblPopupHelp.getSize();
			
			float fDistFromCursor=10f;
			float fZAboveAll=1000;
			lblPopupHelp.setLocalTranslation(v2f.x-v3fSize.x/2, v2f.y+v3fSize.y+fDistFromCursor, fZAboveAll);
			
			if(lblPopupHelp.getParent()==null)GlobalGUINodeI.i().attachChild(lblPopupHelp);
		}else{
			if(lblPopupHelp!=null){
				if(lblPopupHelp.getParent()!=null){
					lblPopupHelp.removeFromParent();
				}
			}
		}
		
		return super.updateAttempt(tpf);
	}
	
	Label lblPopupHelp;
	
	public static class CfgParm extends CmdConditionalStateAbs.CfgParm{
		public CfgParm() {
			super(null);
		}
	}
	@Override
	public MiscLemurStateI configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
		
//		super.icfgOfInstance=icfg;
		
//		super.configure(new CmdConditionalStateAbs.CfgParm(LemurMiscHelpersStateI.class.getSimpleName()));
		super.configure(icfg);
		
//		return storeCfgAndReturnSelf(cfg);
		return getThis();
	}

//	public void initializeSpecialKeyListeners(TextEntryComponent source) {
//		source.
//	}

	public void insertTextAtCaratPosition(TextField tf, String str) {
		DocumentModel dm = tf.getDocumentModel();
		for(int i=0;i<str.length();i++)dm.insert(str.charAt(i));
		WorkAroundI.i().bugFix(btgBugFixUpdateTextFieldTextAndCaratVisibility, tf);
//		bugFix(null,null,btgBugFixUpdateTextFieldTextAndCaratVisibility,tf);
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
	
	/**
	 * TODO use this for dialogs too...
	 */
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
	
	StringCmdField scfDebugCheckLemur = new StringCmdField(this);
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(
			cc.checkCmdValidity(scfDebugCheckLemur, 
				"[strName] if name is provided, check only the spatials matching it. Only works for active/visible/attached GUI elements."
//				"[fNewZSize] go thru all lemur elemets active on the GUI node to check if they are "
//				+"all properly configured."
//				+"The option will only modify Z thickness if it is 0.0f."
			)
		){
//			Float fNewZSize = cc.getCurrentCommandLine().paramFloat(1);
			String strName = cc.getCurrentCommandLine().paramString(1);
			
			Spatial spt = GlobalGUINodeI.i();
			
			if(strName!=null){
				spt=GlobalGUINodeI.i().getChild(strName); //it is recursive exact name match
				if(spt==null){
					cc.dumpWarnEntry("spatial not found with name: "+strName);
					
					ArrayList<Spatial> aspt = MiscJmeI.i().getAllChildrenRecursiveFrom(
						GlobalGUINodeI.i(), strName, EStringMatchMode.Fuzzy, true);
					GlobalCommandsDelegatorI.i().dumpInfoEntry("fuzzy search matches: "+aspt.size());
					for(Spatial sptSearch:aspt){
						GlobalCommandsDelegatorI.i().dumpSubEntry(sptSearch.getName());
					}
				}
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
					setPreferredSizeSafely(panel,v3fPrefSize,true);
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
	public final float fPreferredThickness = 1f; //thickness z=1f to match lemur one and do not mess with its calculations, TODO try to collect it from lemur...
//	Vector3f v3fMinSize=new Vector3f(10f,10f,fPreferredThickness); 
	Vector3f v3fMinSize=new Vector3f(1f,1f,fPreferredThickness); //minimum is 1x1 dot!!!! 
	
	public float getPreferredThickness() {
		return fPreferredThickness;
	}
	
	private boolean	bAllowMinSizeCheckAndFix = false; //MUST BE INITIALLY FALSE!
	
	/**
	 * see {@link #setPreferredSizeSafely(Panel, Vector3f, boolean)}
	 * 
	 * @param pnl
	 * @param fX
	 * @param fY
	 * @param bEnsureSizeNow
	 * @return
	 */
	public Vector3f setSizeSafely(Panel pnl, float fX, float fY, boolean bForce){
		return setPreferredSizeSafely(pnl, new Vector3f(fX,fY,-1), bForce); //z=-1 will be fixed
	}
	
	/**
	 * this only works without crashing because of {@link DialogMainContainer#updateLogicalState()}
	 * 
	 * this only applies on next frame.
	 * 
	 * @param pnl
	 * @param v3fSizeNew x,y,z use -1 to let it be automatic = preferred
	 * @param bForceSpecificSize otherwise the preferred values will be used as minimum
	 * @return the possibly valid size after fixed (including z)
	 */
	public Vector3f setPreferredSizeSafely(
		final Panel pnl, 
		final Vector3f v3fSizeNew, 
		final boolean bForceSpecificSize 
	){
		GlobalMainThreadI.assertEqualsCurrentThread();
		
		if(v3fSizeNew.x<v3fMinSize.x)v3fSizeNew.x=v3fMinSize.x;
		if(v3fSizeNew.y<v3fMinSize.y)v3fSizeNew.y=v3fMinSize.y;
		if(v3fSizeNew.z<v3fMinSize.z)v3fSizeNew.z=v3fMinSize.z; //actually gets overriden
		
		Vector3f v3fPreferredBkp = pnl.getPreferredSize().clone();
		
		if(!bForceSpecificSize){
			if(v3fSizeNew.x<v3fPreferredBkp.x)v3fSizeNew.x=v3fPreferredBkp.x;
			if(v3fSizeNew.y<v3fPreferredBkp.y)v3fSizeNew.y=v3fPreferredBkp.y;
		}
		
		v3fSizeNew.z=v3fPreferredBkp.z; // do not mess with Z !!! //if(v3fSize.z<v3fP.z)v3fSize.z=v3fP.z;
		
		Vector3f v3fSizeBkp = pnl.getSize().clone();
		/**
		 * setSize() will not work... the panel will always be smaller than requested? why?
		 */
		pnl.setPreferredSize(v3fSizeNew);
		
//		try{
//			pnl.setSize(v3fSizeNew);
//		}catch(IllegalArgumentException e){
//			pnl.setSize(v3fSizeBkp);
//		}
		
		return v3fSizeNew;
	}
	
	/**
	 * IMPORTANT!! This is not strongly crash safe, prefer using {@link #setPreferredSizeSafely(Panel, Vector3f, boolean)}
	 * 
	 * Sets custom sizes with crash prevention.
	 * And make it sure the thickness is correct (not 0.0f).
	 * 
	 * @param pnl
	 * @param v3fSizeF x,y,z use -1 to let it be automatic = preferred
	 * @param bForceSpecificSize
	 * @param bMustDoItNow only works if it is going to be rendered
	 * @return null if size setup fails, like being too tiny. Or a valid size.
	 */
	@Deprecated
	public Vector3f setGrantedSizeValidateAndDelayed(
		final Panel pnl, 
		final Vector3f v3fSizeF, 
		final boolean bForceSpecificSize, 
		final boolean bMustDoItNow
	){
		GlobalMainThreadI.assertEqualsCurrentThread();

		final String strSizeKey="SizeKey";
		
		CallableX caller = new CallableX(this) {
			@Override
			public Boolean call() {
				if(!MiscJmeI.i().isGoingToBeRenderedNow(pnl))return false; //re-add to queue
				
				Vector3f v3fSize = v3fSizeF;
				
				Vector3f v3fPreferredBkp = pnl.getPreferredSize().clone();
				setPreferredSizeSafely(pnl, v3fSizeF, bForceSpecificSize);
				
				// the check
				if(!validatePanelUpdate(pnl)){
//				try{
//					pnl.getControl(GuiControl.class).update(1f/30f); //TODO use real?
////					pnl.setPreferredSize(v3fSize);
////					pnl.updateLogicalState(1f/30f);
//				}catch(IllegalArgumentException ex){
					GlobalCommandsDelegatorI.i().dumpWarnEntry("resize failed, restoring", pnl, v3fSize, bForceSpecificSize);
					pnl.setPreferredSize(v3fPreferredBkp);
					v3fSize = null;
				}
				
				putCustomValue(strSizeKey, v3fSize);
				
				return true; //must always discard the queue.
			}
		};
		
		/**
		 * only allow any changes if it can be properly priorly checked/tested 
		 */
		if(MiscJmeI.i().isGoingToBeRenderedNow(pnl)){
			caller.call(); //it will do it now if the check is granted to not crash.
		}else{
			if(bMustDoItNow){
				throw new PrerequisitesNotMetException("not going to be rendered", pnl, v3fSizeF, bForceSpecificSize);
			}else{
				ManageCallQueueI.i().addCall(caller);
				MsgI.i().debug("postponed resize "+pnl+", "+v3fSizeF, true, this);
			}
		}
		
		return (Vector3f)caller.getCustomValue(strSizeKey);
	}
	
	public boolean validatePanelUpdate(Panel pnl){
		try{
			//TODO prefer updateLogicalState()?
			pnl.getControl(GuiControl.class).update(1f/30f); //TODO use real?
//		}catch(IllegalArgumentException ex){
		}catch(Exception ex){
			return false;
		}
		
		return true;
	}

	public CellDialogEntry<?> getCellFor(Spatial source) {
		CellDialogEntry<?> cell=null;
		for(Spatial sptParent:MiscJmeI.i().getParentListFrom(source, false)){
			if(sptParent instanceof CellDialogEntry<?>){
				cell = (CellDialogEntry<?>)sptParent;
				break;
			}
		}
		
		return cell;
	}

//	public MiscLemurStateI setLocationXY(Spatial spt, Vector3f v3f) {
//		GlobalMainThreadI.assertEqualsCurrentThread();
//		spt.setLocalTranslation(v3f.x, v3f.y, spt.getLocalTranslation().z); // to not mess with Z order
//		return this;
//	}
//
//	public MiscLemurStateI setScaleXY(Spatial spt, Float fScaleX, Float fScaleY) {
//		GlobalMainThreadI.assertEqualsCurrentThread();
//		Vector3f v3fCurrentScale = spt.getLocalScale();
//		spt.setLocalScale(
//			fScaleX==null?v3fCurrentScale.x:fScaleX,
//			fScaleY==null?v3fCurrentScale.y:fScaleY,
//			v3fCurrentScale.z); // to not mess with Z order
//		return this;
//	}

	public void lineWrapDisableForListboxEntries(ListBox<String> lstbx){
		GridPanel gp = lstbx.getGridPanel();
		for(Spatial spt:MiscJmeI.i().getAllChildrenRecursiveFrom(gp)){//gp.getChildren()){
			/**
			 * must be button because lemur ListBox uses Button for entries!
			 * this means that other Panels are not actual entries!
			 */
			if(spt instanceof Button){ // 
				MiscJmeI.i().lineWrapDisableFor((Node)spt);
			}
		}
	}

	/**
	 * to let the use of buttons as labels
	 */
	public static class CmdDummy implements Command<Button>{
		@Override
		public void execute(Button source) {
		}
	}
	private final CmdDummy cmdDummy = new CmdDummy();

	private String	strPopupHelp;
	
	public CmdDummy getCmdDummy() {
		return cmdDummy;
	}


//	public void setOverrideBackgroundColorNegatingCurrent(Panel pnl) {
//		overrideBackgroundColor(pnl,null,false,true);
//	}
//	public void resetOverrideBackgroundColor(Panel pnl){
//		overrideBackgroundColor(pnl,null,true,false);
//	}
//	public void setOverrideBackgroundColor(Panel pnl, ColorRGBA colorApply) {
//		overrideBackgroundColor(pnl,colorApply,false,false);
//	}
//	/**
//	 * TODO use the lemur style instead?
//	 * @param colorOverride if null, will reset (restore current normal bkg color)
//	 * @param bNegateCurrentColor overrides color param (least if it is null)
//	 */
//	private void overrideBackgroundColor(Panel pnl, ColorRGBA colorOverride, boolean bResetToBackup, boolean bNegateCurrentColor) {
//		GlobalMainThreadI.assertEqualsCurrentThread();
//		if(!btgHoverHighlight.b())return;
//		
//		ColoredComponent cc = (ColoredComponent)pnl.getBackground();
//		
//		String strKey="BkgColorBkp";
//		ColorRGBA colorBkp=pnl.getUserData(strKey);
//		if(colorBkp==null){
//			colorBkp = cc.getColor();
//			pnl.setUserData(strKey, colorBkp);
//		}
//		
//		if(bResetToBackup){
//			cc.setColor(colorBkp);
//			pnl.setUserData(strKey, null); //clear to not leave useless value there
//		}else{
//			if(bNegateCurrentColor){
//				colorOverride = MiscJmeI.i().negateColor(colorBkp);
//			}else{
//				if(colorOverride==null)throw new PrerequisitesNotMetException("invalid null color override", this);
//			}
//			
//			cc.setColor(colorOverride.clone());
//		}
//	}

	public void fixBitmapTextLimitsFor(Panel pnl) {
		MiscJmeI.i().fixBitmapTextLimitsFor(pnl, pnl.getSize());
	}

	public Vector3f eventToV3f(AbstractCursorEvent event){
		return new Vector3f(event.getX(),event.getY(),0);
	}
	
//	enum E{
//		panel,
//		pos,
//		;
//		public String s(){return this.toString();}
//	}
	
//	BugFixBoolTogglerCmdField btgBugFixConfirmSetPosition = new BugFixBoolTogglerCmdField(this, false)
//		.setCallerAssigned(new CallableX(this,100) {
//			@Override
//			public Boolean call() {
//				Vector3f v3fPos = (Vector3f) getCustomValue(E.pos.s());
//				Panel pnl = (Panel) getCustomValue(E.panel.s());
//				
//				if(applyPositionSafely(pnl, v3fPos))return false; //had to change it, so check again
//				
//				return true;
//			}
//		});
	private boolean applyPositionSafely(Panel pnl,Vector3f v3fPos) {
		Vector3f v3fCurrentPos = pnl.getLocalTranslation();
		
		// DO NOT MESS WITH Z!!!
		v3fPos.setZ(v3fCurrentPos.getZ());
		
		if(v3fCurrentPos.equals(v3fPos)){
			return false; //nothing applied
		}
		
		pnl.setLocalTranslation(v3fPos);
		return true;
	}
	public void setPositionSafely(Panel pnl,Vector3f v3fPos) {
		if(applyPositionSafely(pnl, v3fPos)){
//			btgBugFixConfirmSetPosition.getCallerAssignedForMaintenance(ccSelf)
//				.putCustomValue(E.pos.s(), v3fPos.clone())
//				.putCustomValue(E.panel.s(), pnl);
//			btgBugFixConfirmSetPosition.callerAssignedQueueNow();
		}
	}
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=MiscLemurStateI.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=MiscLemurStateI.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}

	@Override
	protected MiscLemurStateI getThis() {
		return this;
	}

	public <T extends Panel> T setPopupHelp(T pnl, String strHelp){
		MiscJmeI.i().setPopupHelp(pnl,strHelp);
		CursorEventControl.addListenersToSpatial(pnl, DialogMouseCursorListenerI.i());
		return pnl;
	}

	public void clearPopupHelpString() {
		strPopupHelp=null;
	}

	public void setPopupHelpString(String str) {
		this.strPopupHelp=str;
	}
	
	public Vector3f getValidSizeFrom(Panel pnl) {
		Vector3f v3fPos = pnl.getSize().clone();
		if(v3fPos.length()<2){ //TODO why??
			v3fPos = pnl.getPreferredSize().clone();
		}
		return v3fPos;
	}
	
	/**
	 * 
	 * @param pnl
	 * @return z will be of the panel
	 */
	public Vector3f getRelativeCenterXYposOf(Panel pnl) {
//		Vector3f v3fPos = pnl.getSize().clone();
//		if(v3fPos.length()<2){ //TODO why??
//			v3fPos = pnl.getPreferredSize().clone();
//		}
		Vector3f v3fPos = MiscJmeI.i().getCenterXYposOf(getValidSizeFrom(pnl)).negate();
		v3fPos.z=pnl.getLocalTranslation().z;
		return v3fPos;
//		return MiscJmeI.i().getCenterPosOf(v3fPos);
//		v3fPos.multLocal(0.5f);
//		v3fPos.x*=-1;
//		return v3fPos;
	}

}
