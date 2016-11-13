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

import com.github.commandsconsolegui.globals.jme.GlobalGUINodeI;
import com.github.commandsconsolegui.globals.jme.lemur.GlobalLemurDialogHelperI;
import com.github.commandsconsolegui.jme.JmeAppOS;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogManagerI.DialogStyleElementId;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem.StorageFolderType;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.ElementId;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class LemurAppOS extends JmeAppOS{
//	private Label	lblAlert;
	
	public Label getAlertLabel() {
		return (Label)super.getAlertSpatial();
	}
	
	public LemurAppOS(String strApplicationBaseSaveDataPath,StorageFolderType esft) {
		super(strApplicationBaseSaveDataPath, esft);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void dumpAlert() {
		super.dumpAlert();
		
		updateAlertPosSize();
	}
	
	private void updateAlertPosSize(){
		Vector3f v3fWdwSize = MiscJmeI.i().getAppWindowSize();
		
		Vector3f v3fLblSize = v3fWdwSize.mult(0.5f);
		
		MiscLemurStateI.i().setPreferredSizeSafely(getAlertLabel(), v3fLblSize, true);
		
		/**
		 * as preferred size is not applied imediately
		 */
		if(getAlertLabel().getSize().length()!=0){
			v3fLblSize = getAlertLabel().getSize();
		}
		
		Vector3f v3fPos = new Vector3f();
		v3fPos.x=v3fWdwSize.x/2f - v3fLblSize.x/2f;
		v3fPos.y=v3fWdwSize.y/2f + v3fLblSize.y/2f;
		
		float fZ=0;
		for(Spatial spt:MiscJmeI.i().getAllChildrenRecursiveFrom(GlobalGUINodeI.i())){
			if(spt.getLocalTranslation().z > fZ)fZ=spt.getLocalTranslation().z;
		}
		v3fPos.z=fZ+10;
		
		getAlertLabel().setLocalTranslation(v3fPos);//new Vector3f(Display.getWidth()/2f, Display.getHeight()/2f, fZ+10)); //above any other gui elements  
	}
	
	@Override
	public StackTraceElement[] showSystemAlert(String strMsg) {
		StackTraceElement[] aste = super.showSystemAlert(strMsg);
		
		strMsg="ALERT!!! "+strMsg;
		
		if(getAlertLabel()==null){
			setAlertSpatial(new Label("nothing yet...", new ElementId(DialogStyleElementId.SystemAlert.s()), GlobalLemurDialogHelperI.i().STYLE_CONSOLE));
//			getAlertLabel().setInsets(new Insets3f(5f, 5f, 5f, 5f));
//			getAlertLabel().setBorder(new QuadBackgroundComponent(ColorRGBA.Red.clone()));
			
//			setAlertSpatial(new Button("nothing yet...", new ElementId(DialogStyleElementId.SystemAlert.s()), GlobalLemurDialogHelperI.i().STYLE_CONSOLE));
			GlobalGUINodeI.i().attachChild(getAlertLabel());
		}
		
		if(!getAlertLabel().getText().equals(strMsg)){
			getAlertLabel().setText(strMsg);
		}
		
//		updateAlertPosSize();
		
		return aste;
	}
	
	@Override
	public void hideSystemAlert(StackTraceElement[] asteFrom) {
		super.hideSystemAlert(asteFrom);
		getAlertLabel().removeFromParent();
		setAlertSpatial(null);
	}
	
}
