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
package com.github.commandsconsolegui.spLemur;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI;
import com.github.commandsconsolegui.spJme.OSAppJme;
import com.github.commandsconsolegui.spJme.globals.GlobalGUINodeI;
import com.github.commandsconsolegui.spJme.globals.GlobalSimpleAppRefI;
import com.github.commandsconsolegui.spJme.misc.EffectsJmeStateI;
import com.github.commandsconsolegui.spJme.misc.ILinkedSpatial;
import com.github.commandsconsolegui.spJme.misc.EffectsJmeStateI.EffectElectricity;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.github.commandsconsolegui.spLemur.console.LemurDiagFocusHelperStateI;
import com.github.commandsconsolegui.spLemur.globals.GlobalLemurDialogHelperI;
import com.github.commandsconsolegui.spLemur.misc.EffectsLemurI.EEffChannel;
import com.github.commandsconsolegui.spLemur.misc.EffectsLemurI.EEffState;
import com.github.commandsconsolegui.spLemur.misc.MiscLemurStateI;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem.StorageFolderType;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class OSAppLemur extends OSAppJme {
//	private Label	lblAlert;
	
	private Container	cntrAlert;
	private Label	lblAlertMsg;
	private Panel	pnlBlocker;
	private final TimedDelayVarField	tdBlockerGlow = new TimedDelayVarField(this, 3f, "the blocker blocks all gui elements to let some special feature be performed");
	private ColorRGBA	colorBlockerBkg;
	BoolTogglerCmdField btgAlertStayOnCenter = new BoolTogglerCmdField(this, false, "if false, will follow mouse");
//	private boolean	bStayOnCenter = false;
	private boolean	bStartedShowEffect;
	private boolean	bAlertPanelIsReady;
	private EffectElectricity	ieffAlert;

	public Panel getAlertPanel() {
		return (Panel)super.getAlertSpatial();
	}
	
	public OSAppLemur(String strApplicationBaseSaveDataPath,StorageFolderType esft) {
		super(strApplicationBaseSaveDataPath, esft);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void update(float fTpf) {
		super.update(fTpf);
		
		updateMessage();
		
		updateIfPanelIsReady();
		
		if(pnlBlocker!=null){
			MiscJmeI.i().updateColorFading(tdBlockerGlow, colorBlockerBkg, true, 0.25f, 0.35f);
		}
	}
	
	@Override
	protected void dumpAlert() {
		super.dumpAlert();
		
		updateAlertPosSize();
	}

	private void updateIfPanelIsReady(){
		if(getAlertPanel()==null)return;
		
		Vector3f v3fLblSize = getAlertPanel().getSize();
		if(v3fLblSize.length()==0)return;
		
		bAlertPanelIsReady=true;
		
		if(!bStartedShowEffect){
			EEffChannel.ChnGrowShrink.play(EEffState.Show, cntrAlert, getPos(EElement.Alert,null));
			
			createAlertLinkEffect();
			
			bStartedShowEffect=true;
		}
		
//		if(getAlertUserInteractionIndicator()!=null){
		if(isDynamicInfoSet()){
			updateEdgesColor(ColorRGBA.Cyan.clone());
		}else{
			updateEdgesColor(null);
		}
	
//		Vector3f v3fPos = ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f();
//		v3fPos.addLocal(MiscLemurStateI.i().getCenterPosOf(getAlertPanel()));
////		v3fPos.x-=v3fLblSize.x/2f;
////		v3fPos.y+=v3fLblSize.y/2f;
		
//		MiscJmeI.i().setLocationXY(getAlertPanel(), v3fPos);
//		MiscJmeI.i().setLocationXY(getAlertPanel(), 
//			ManageMouseCursorI.i().getPosWithMouseOnCenter(
//				MiscLemurStateI.i().getValidSizeFrom(getAlertPanel())));
		MiscJmeI.i().setLocationXY(getAlertPanel(),getPos(EElement.Alert,v3fLblSize)); 
	}
	
//	private void updateAlertPosSize(){
////			MiscJmeI.i().setLocationXY(getAlertPanel(), ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f());
//		
//		Vector3f v3fWdwSize = MiscJmeI.i().getAppWindowSize();
//		MiscLemurStateI.i().setPreferredSizeSafely(pnlBlocker, v3fWdwSize, true);
//		
//		Vector3f v3fLblSize = v3fWdwSize.mult(0.5f);
//		MiscLemurStateI.i().setPreferredSizeSafely(getAlertPanel(), v3fLblSize, true);
//		
//		/**
//		 * as preferred size is not applied imediately
//		 */
//		if(getAlertPanel().getSize().length()!=0){
//			v3fLblSize = getAlertPanel().getSize();
//		}
//		
//		Vector3f v3fPos = new Vector3f(v3fWdwSize.x/2f,v3fWdwSize.y/2f,0);
//		v3fPos.x-=v3fLblSize.x/2f;
//		v3fPos.y+=v3fLblSize.y/2f;
//		
//		/**
//		 * the alert is ultra special and the Z can be dealt with here!
//		 */
//		float fZ=0;
//		for(Spatial spt:MiscJmeI.i().getAllChildrenRecursiveFrom(GlobalGUINodeI.i())){
//			if(spt.getLocalTranslation().z > fZ)fZ=spt.getLocalTranslation().z;
//		}
//		pnlBlocker.setLocalTranslation(new Vector3f(0, v3fWdwSize.y,
//			fZ + LemurDiagFocusHelperStateI.i().getDialogZDisplacement()*1));
//		getAlertPanel().setLocalTranslation(new Vector3f(v3fPos.x, v3fPos.y,
//			fZ + LemurDiagFocusHelperStateI.i().getDialogZDisplacement()*2));
//	}
	
	private void createAlertLinkEffect() {
		if(ieffAlert==null){
			ieffAlert = new EffectElectricity(this);
		}
		
		boolean bUseMouse = false;
		if(getActionSourceElement()!=null){
			Spatial spt=null;
			if(getActionSourceElement() instanceof ILinkedSpatial){
				spt=((ILinkedSpatial)getActionSourceElement()).getLinkedSpatial();
			}else
			if(getActionSourceElement() instanceof Spatial){
				spt=(Spatial)getActionSourceElement();
			}
			
			if(spt!=null){
				ieffAlert.setFollowFromTarget(spt, new Vector3f(0,0,1));
			}else{
				MsgI.i().devWarn("invalid type to follow", getActionSourceElement().getClass());
				bUseMouse=true;
			}
		}
		
		if(bUseMouse){
			ieffAlert.setFromTo( getPos(EElement.Effects, ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f()), null);
		}
		
		ieffAlert.setFollowToTarget(cntrAlert, null);
		
		if(!EffectsJmeStateI.i().containsEffect(ieffAlert)){
			EffectsJmeStateI.i().addEffect(ieffAlert);
		}
	}

	private void updateAlertPosSize(){
		Vector3f v3fWdwSize = MiscJmeI.i().getAppWindowSize();
		MiscLemurStateI.i().setPreferredSizeSafely(pnlBlocker, v3fWdwSize, true);
		
		Vector3f v3fLblSize = v3fWdwSize.mult(0.5f);
		MiscLemurStateI.i().setPreferredSizeSafely(getAlertPanel(), v3fLblSize, true);
		
		pnlBlocker.setLocalTranslation(getPos(EElement.Blocker,null));
		
		/**
		 * as preferred size is not applied imediately
		 */
		if(getAlertPanel().getSize().length()!=0){
			v3fLblSize = getAlertPanel().getSize();
		}
		getAlertPanel().setLocalTranslation(getPos(EElement.Alert,v3fLblSize));
	}
	
	private static enum EElement{
		Blocker,
		Alert,
		Effects,
	}
	
	private Vector3f getPos(EElement e, Vector3f v3fRef){
		if(v3fRef!=null)v3fRef=v3fRef.clone(); //safety
		Vector3f v3fWdwSize = MiscJmeI.i().getAppWindowSize();
		
		/**
		 * the alert is ultra special and the Z can be dealt with here!
		 */
		float fZ=0;
		for(Spatial spt:MiscJmeI.i().getAllChildrenRecursiveFrom(GlobalGUINodeI.i())){
			if(spt.getLocalTranslation().z > fZ)fZ=spt.getLocalTranslation().z;
		}
		
		Vector3f v3fPos = null;
		float fZDispl = LemurDiagFocusHelperStateI.i().getDialogZDisplacement();
		fZ += fZDispl*(e.ordinal()+1); // attention how the order/index is a multiplier of the displacement!
		switch(e){ 
			case Blocker:
				v3fPos = new Vector3f(0, v3fWdwSize.y,fZ);
//					fZ + fZDispl*1);
				break;
			case Effects:
				v3fPos = new Vector3f(v3fRef);
				v3fPos.z=fZ;
//					fZ + fZDispl*2;
				break;
			case Alert:
				Vector3f v3fAlertSize=v3fRef;
				if(v3fAlertSize==null){
					v3fAlertSize = v3fWdwSize.mult(0.5f);
				}
				
				if(btgAlertStayOnCenter.b()){
					v3fPos = new Vector3f(v3fWdwSize.x/2f,v3fWdwSize.y/2f,0);
					v3fPos.x-=v3fAlertSize.x/2f;
					v3fPos.y+=v3fAlertSize.y/2f;
				}else{
					v3fPos = ManageMouseCursorI.i().getPosWithMouseOnCenter(v3fAlertSize);
				}
				
				v3fPos.set(new Vector3f(v3fPos.x, v3fPos.y, fZ));
//					fZ + fZDispl*3));
				
				break;
		}
		
		return v3fPos;
	}
	
	private void createAlert(){
		if(pnlBlocker==null){
			// old trick to prevent access to other gui elements easily! :D
			pnlBlocker = new Button("");
			colorBlockerBkg = ColorRGBA.Red.clone(); //new ColorRGBA(1f,0,0,1);//0.25f);
			pnlBlocker.setBackground(new QuadBackgroundComponent(colorBlockerBkg));
			tdBlockerGlow.setActive(true);
			GlobalGUINodeI.i().attachChild(pnlBlocker);
		}
		
		// the alert container
		QuadBackgroundComponent qbc;
		
		cntrAlert = new Container(new BorderLayout());
		setAlertSpatial(cntrAlert);
		
		EEffChannel.ChnGrowShrink.applyEffectsAt(cntrAlert);
//		LemurEffectsI.i().addEffectTo(cntrAlert, LemurEffectsI.i().efGrow);
		
		//yellow background with border margin
		lblAlertMsg = new Label("",GlobalLemurDialogHelperI.i().STYLE_CONSOLE);
		lblAlertMsg.setColor(ColorRGBA.Blue.clone());
		lblAlertMsg.setShadowColor(ColorRGBA.Black.clone());
		lblAlertMsg.setShadowOffset(new Vector3f(1,1,0));
		lblAlertMsg.setBackground(null);
		qbc=new QuadBackgroundComponent(new ColorRGBA(1,1,0,0.75f));
		qbc.setMargin(10f, 10f);
		lblAlertMsg.setBorder(qbc);
		cntrAlert.addChild(lblAlertMsg, BorderLayout.Position.Center);
		
		//edges countour in red
		for(BorderLayout.Position eEdge:new Position[]{Position.East,Position.West,Position.North,Position.South}){
			addAlertEdge(eEdge, ColorRGBA.Red.clone(), new Vector3f(2f,2f,0.1f));
		}
		
	}
	
	@Override
	public StackTraceElement[] showSystemAlert(String strMsg, Object objActionSourceElement) {
		StackTraceElement[] aste = super.showSystemAlert(strMsg,objActionSourceElement);
		
		if(getAlertPanel()==null)createAlert();
		
		if(!GlobalGUINodeI.i().hasChild(cntrAlert)){
			GlobalGUINodeI.i().attachChild(cntrAlert);
//			EEffChannel.ChnGrowShrink.play(EEffState.Show, cntrAlert, getPos(EElement.Alert,null));
		}
		
//		if(!lblAlertMsg.getText().equals(strMsg)){
//			lblAlertMsg.setText(strMsg);
//		}
		
//		updateAlertPosSize();
		
		return aste;
	}
	
	/**
	 * @param bFullAlert not only the blocker, but also the main alert box message too
	 */
	@Override
	public boolean isShowingAlert(boolean bFullAlert) {
		if(super.isShowingAlert(bFullAlert))return true;
		
		if(bFullAlert){
			return pnlBlocker!=null && getAlertPanel()!=null;
		}else{
			return pnlBlocker!=null; // || getAlertPanel()!=null;
		}
	}
	
	private void updateMessage(){
		if(lblAlertMsg!=null)lblAlertMsg.setText(getFullMessage());
	}
	
	private ColorRGBA colorEdgesDefault=ColorRGBA.Red.clone();
	
	private BfdArrayList<Panel> aedgeList = new BfdArrayList<Panel>() {};
//	private boolean	bAllowNewEffectCreation = true;
	private void addAlertEdge(BorderLayout.Position eEdge, ColorRGBA color, Vector3f v3fSize){
		QuadBackgroundComponent qbc = new QuadBackgroundComponent(color);
		Label lbl=new Label("",GlobalLemurDialogHelperI.i().STYLE_CONSOLE);
		lbl.setBackground(qbc);
		MiscLemurStateI.i().setPreferredSizeSafely(lbl, v3fSize, true);
		
		aedgeList.add(lbl);
		
		cntrAlert.addChild(lbl, eEdge);
	}
	
	private void updateEdgesColor(ColorRGBA c){
		if(c==null)c=colorEdgesDefault;
		for(Panel pnl:aedgeList){
			((QuadBackgroundComponent)pnl.getBackground()).setColor(c);
		}
	}
	
	/**
	 * 
	 * @param asteFrom
	 * @param bKeepGuiBlockerOnce useful when going to retry having subsequent alerts
	 */
	@Override
	public void hideSystemAlert(StackTraceElement[] asteFrom, boolean bKeepGuiBlockerOnce) {
		super.hideSystemAlert(asteFrom,bKeepGuiBlockerOnce);
		cntrAlert.removeFromParent();
		cntrAlert=null;
		
//		lblAlertMsg.removeFromParent();
//		lblAlertMsg=null;
//		if(!bKeepGuiBlockerOnce){
//			pnlBlocker.removeFromParent();
//			pnlBlocker=null;
//		}
		
		bStartedShowEffect=false;
		
		bAlertPanelIsReady=false;
		
//		bAllowNewEffectCreation=false;
		if(!bKeepGuiBlockerOnce){
			pnlBlocker.removeFromParent();
			pnlBlocker=null;
			
			EffectsJmeStateI.i().discardEffectsForOwner(this);
			ieffAlert=null;
//			bAllowNewEffectCreation=true;
		}
		
		setAlertSpatial(null);
	}
	

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,IllegalAccessException {
		if(fld.getDeclaringClass()!=OSAppLemur.class)return super.getFieldValue(fld); //For subclasses uncomment this line
		return fld.get(this);
	}

	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=OSAppLemur.class){super.setFieldValue(fld,value);return;} //For subclasses uncomment this line
		fld.set(this,value);
	}

}
