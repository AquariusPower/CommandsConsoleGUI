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

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
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
import com.jme3.scene.Spatial.CullHint;
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
	
	public final BoolTogglerCmdField	btgTextCursorPulseFadeBlinkMode = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgTextCursorLarge = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	
	public final StringCmdField CMD_FIX_INVISIBLE_TEXT_CURSOR = new StringCmdField(this, CommandsDelegator.strFinalCmdCodePrefix);

//	private SimpleApplication	sapp;
	
//	public void initialize(SimpleApplication sapp){
//		this.sapp = sapp;
//	}
	
	protected TimedDelayVarField tdTextCursorBlink = new TimedDelayVarField(this,1f,"blink delay");
	private boolean	bBlinkingTextCursor = true;
	private FocusManagerState	focusState;
	private TextField	tfToBlinkCursor;

//	private boolean	bInitialized;
//	private boolean	bEnabled = true;

//	private CommandsDelegatorI	cd;

//	private boolean	bFixInvisibleTextInputCursor=false;

	private boolean	bBlinkFadeInAndOut=true;

	private enum EKey{
		CursorHotLink,
		ExclusiveCursorMaterial,
		CursorMaterialBkp,
		ExclusiveCursorColor,
		CursorLargeMode,
		;
	}

//	private int	iMoveCaratTo;
	
	public LemurMiscHelpersStateI() {
		super.bPrefixCmdWithIdToo = true;
	}
	
	public Geometry getTextCursorFrom(TextField tf){
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
	
	public void setTextFieldInputToBlinkCursor(TextField tf){
		this.tfToBlinkCursor = tf;
	}
	
	private void checkAndPrepareExclusiveCursorMaterialFor(TextField tf, Geometry geomCursor){
		Material matCursorOnly = tf.getUserData(EKey.ExclusiveCursorMaterial.toString());
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
			tf.setUserData(EKey.CursorMaterialBkp.toString(), geomCursor.getMaterial());
			matCursorOnly = geomCursor.getMaterial().clone();
			MatParam param = matCursorOnly.getParam("Color");
			ColorRGBA colorClone = ((ColorRGBA)param.getValue()).clone();
			matCursorOnly.setColor("Color", colorClone);
//			matCursorOnly.setParam("Color", ColorRGBA.class, matCursorOnly.getParam("Color").clone());
			
			geomCursor.setMaterial(matCursorOnly);
//		fixInvisibleCursor(geomCursor);
			tf.setUserData(EKey.ExclusiveCursorMaterial.toString(), matCursorOnly);
			tf.setUserData(EKey.ExclusiveCursorColor.toString(), colorClone);
		}
	}
	
	private void updateBlinkInputFieldTextCursor(TextField tf) {
		if(!bBlinkingTextCursor)return;
		if(!tf.equals(LemurFocusHelperStateI.i().getFocused()))return;
		
//		tdTextCursorBlink.updateTime();
		
		Geometry geomCursor = tf.getUserData(EKey.CursorHotLink.toString());
		if(geomCursor==null){
			geomCursor = getTextCursorFrom(tf);
			tf.setUserData(EKey.CursorHotLink.toString(), geomCursor);
		}
		
//		BitmapText bmt = getBitmapTextFrom(tf);
//		((BitmapTextPage)bmt.getChild("BitmapFont")).
		
		long lDelay = tdTextCursorBlink.getCurrentDelayNano();
		
		if(btgTextCursorPulseFadeBlinkMode.b()){
			checkAndPrepareExclusiveCursorMaterialFor(tf,geomCursor);
			
			ColorRGBA color = tf.getUserData(EKey.ExclusiveCursorColor.toString());
			color.a = tdTextCursorBlink.getCurrentDelayPercentualDynamic();
			if(bBlinkFadeInAndOut){
				if(color.a>0.5f)color.a=1f-color.a; //to allow it fade in and out
				color.a*=2f;
//				if(color.a<0.75f)color.a=0.75f;
			}
			if(color.a<0)color.a=0;
			if(color.a>1)color.a=1;
		}else{
			Material matBkp = tf.getUserData(EKey.CursorMaterialBkp.toString());
			if(matBkp!=null){
				geomCursor.setMaterial(matBkp);
				tf.setUserData(EKey.CursorMaterialBkp.toString(),null); //clear
			}
			
			if(lDelay > tdTextCursorBlink.getDelayLimitNano()){
				if(geomCursor.getCullHint().compareTo(CullHint.Always)!=0){
					geomCursor.setCullHint(CullHint.Always);
				}else{
					bugFix(null,btgBugFixInvisibleCursor,tf);
//					bugFix(EBugFix.InvisibleCursor,tf);
//					fixInvisibleCursor(geomCursor);
					geomCursor.setCullHint(CullHint.Inherit);
				}
				
				tdTextCursorBlink.updateTime();
			}
		}
			
	}
	
	private void updateLargeTextCursorMode(TextField tf){
		if(btgTextCursorLarge.b()){
			if(tf.getUserData(EKey.CursorLargeMode.toString())==null){
				enableLargeCursor(tf,true);
			}
		}else{
			if(tf.getUserData(EKey.CursorLargeMode.toString())!=null){
				enableLargeCursor(tf,false);
			}
		}
	}
	
	private void enableLargeCursor(TextField tf, boolean b){
		Geometry geomCursor = getTextCursorFrom(tf);
		geomCursor.setLocalScale(b?3f:1f/3f,1f,1f);
		tf.setUserData(EKey.CursorLargeMode.toString(), b?true:null);
	}
	
	/**
	 * To show the cursor at the new carat position, 
	 * this required protected method: {@link TextEntryComponent#resetCursorPosition}
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
	
	BoolTogglerCmdField btgBugFixInvisibleCursor = 
		new BoolTogglerCmdField(this,true).setHelp("in case text cursor is invisible").setCallNothingOnChange();
	BoolTogglerCmdField btgBugFixUpdateTextFieldTextAndCaratVisibility = 
		new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	@Override
	public <T> T bugFix(Class<T> clReturnType, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams){
		boolean bFixed = false;
		
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
	protected boolean initOrUndo() {
		tdTextCursorBlink.updateTime();
		return super.initOrUndo();
	}

	@Override
	protected boolean updateOrUndo(float tpf) {
//		LemurFocusHelperStateI.i().update(tpf);
		
		if(tfToBlinkCursor!=null){
			updateBlinkInputFieldTextCursor(tfToBlinkCursor);
			updateLargeTextCursorMode(tfToBlinkCursor);
		}
		
		return super.updateOrUndo(tpf);
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
		
		if(cc.checkCmdValidity(this,scfCheckLemur,"[fNewZSize] go thru all lemur elemets active on the GUI node to check if they are all properly configured")){
			Float fNewZSize = cc.getCurrentCommandLine().paramFloat(1);
			
			checkElementsRecursive(GlobalGUINodeI.i(), fNewZSize);
			
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
		
		if(spt instanceof Panel){
			Panel panel = (Panel)spt;
			
			Vector3f v3f = panel.getSize();
			z=v3f.z;
			if(fNewZSize!=null){
				v3f.z=fNewZSize;
				panel.setSize(v3f);
			}
			
			Vector3f v3fP = panel.getPreferredSize();
			zP=v3fP.z;
			if(v3fP!=null){
				if(fNewZSize!=null){
					v3fP.z=fNewZSize;
					panel.setPreferredSize(v3fP);
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
				+""+spt.getClass().getSimpleName()+"(name)="+spt.getName()+";"
			);
		
		if(spt instanceof Node){
			Node node = (Node)spt;
			for(Spatial sptChild:node.getChildren()){
				checkElementsRecursive(sptChild,fNewZSize);
			}
		}
	}
	
}
