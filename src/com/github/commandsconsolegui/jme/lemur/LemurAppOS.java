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
package com.github.commandsconsolegui.jme.lemur;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.jme.GlobalGUINodeI;
import com.github.commandsconsolegui.globals.jme.lemur.GlobalLemurDialogHelperI;
import com.github.commandsconsolegui.jme.JmeAppOS;
import com.github.commandsconsolegui.jme.ManageMouseCursorI;
import com.github.commandsconsolegui.jme.lemur.console.LemurDiagFocusHelperStateI;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI;
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
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class LemurAppOS extends JmeAppOS {
//	private Label	lblAlert;
	
	private Container	cntrAlert;
	private Label	lblAlertMsg;
	private Panel	pnlBlocker;
	private TimedDelayVarField	tdBlockerGlow = new TimedDelayVarField(this, 3f, "...");
	private ColorRGBA	colorBlockerBkg;

	public Panel getAlertPanel() {
		return (Panel)super.getAlertSpatial();
	}
	
	public LemurAppOS(String strApplicationBaseSaveDataPath,StorageFolderType esft) {
		super(strApplicationBaseSaveDataPath, esft);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void update(float fTpf) {
		super.update(fTpf);
		
		updateFollowMouse();
		
		if(pnlBlocker!=null){
			MiscJmeI.i().updateColorFading(tdBlockerGlow, colorBlockerBkg, true, 0.25f, 0.35f);
		}
	}
	
	@Override
	protected void dumpAlert() {
		super.dumpAlert();
		
		updateAlertPosSize();
	}

	private void updateFollowMouse(){
		if(getAlertPanel()==null)return;
		
		Vector3f v3fLblSize = getAlertPanel().getSize();
		if(getAlertPanel().getSize().length()==0)return;
		
		Vector3f v3fPos = ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f();
		v3fPos.addLocal(MiscLemurStateI.i().getCenterPositionOf(getAlertPanel()));
//		v3fPos.x-=v3fLblSize.x/2f;
//		v3fPos.y+=v3fLblSize.y/2f;
		
		MiscJmeI.i().setLocationXY(getAlertPanel(), v3fPos);
	}
	
	private void updateAlertPosSize(){
//			MiscJmeI.i().setLocationXY(getAlertPanel(), ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f());
		
		Vector3f v3fWdwSize = MiscJmeI.i().getAppWindowSize();
		MiscLemurStateI.i().setPreferredSizeSafely(pnlBlocker, v3fWdwSize, true);
		
		Vector3f v3fLblSize = v3fWdwSize.mult(0.5f);
		
		MiscLemurStateI.i().setPreferredSizeSafely(getAlertPanel(), v3fLblSize, true);
		
		/**
		 * as preferred size is not applied imediately
		 */
		if(getAlertPanel().getSize().length()!=0){
			v3fLblSize = getAlertPanel().getSize();
		}
		
		Vector3f v3fPos = new Vector3f(v3fWdwSize.x/2f,v3fWdwSize.y/2f,0);
		v3fPos.x-=v3fLblSize.x/2f;
		v3fPos.y+=v3fLblSize.y/2f;
		
		/**
		 * the alert is ultra special and the Z can be dealt with here!
		 */
		float fZ=0;
		for(Spatial spt:MiscJmeI.i().getAllChildrenRecursiveFrom(GlobalGUINodeI.i())){
			if(spt.getLocalTranslation().z > fZ)fZ=spt.getLocalTranslation().z;
		}
		pnlBlocker.setLocalTranslation(new Vector3f(0, v3fWdwSize.y,
			fZ + LemurDiagFocusHelperStateI.i().getDialogZDisplacement()*1));
		getAlertPanel().setLocalTranslation(new Vector3f(v3fPos.x, v3fPos.y,
			fZ + LemurDiagFocusHelperStateI.i().getDialogZDisplacement()*2));
	}
	
	@Override
	public StackTraceElement[] showSystemAlert(String strMsg) {
		StackTraceElement[] aste = super.showSystemAlert(strMsg);
		
		strMsg="ALERT!!!\n"+strMsg;
		
		if(getAlertPanel()==null){
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
			
			GlobalGUINodeI.i().attachChild(cntrAlert);//getAlertPanel());
		}
		
		if(!lblAlertMsg.getText().equals(strMsg)){
			lblAlertMsg.setText(strMsg);
		}
		
//		updateAlertPosSize();
		
		return aste;
	}
	
	private void addAlertEdge(BorderLayout.Position eEdge, ColorRGBA color, Vector3f v3fSize){
		QuadBackgroundComponent qbc = new QuadBackgroundComponent(color);
		Label lbl=new Label("",GlobalLemurDialogHelperI.i().STYLE_CONSOLE);
		lbl.setBackground(qbc);
		MiscLemurStateI.i().setPreferredSizeSafely(lbl, v3fSize, true);
		
		cntrAlert.addChild(lbl, eEdge);
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
		if(!bKeepGuiBlockerOnce){
			pnlBlocker.removeFromParent();
			pnlBlocker=null;
		}
		
		setAlertSpatial(null);
	}
	

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurAppOS.class)return super.getFieldValue(fld); //For subclasses uncomment this line
		return fld.get(this);
	}

	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurAppOS.class){super.setFieldValue(fld,value);return;} //For subclasses uncomment this line
		fld.set(this,value);
	}

}
