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

package com.github.commandsconsolegui.jme;

import java.io.File;

import com.github.commandsconsolegui.AppOS;
import com.github.commandsconsolegui.globals.jme.GlobalAppSettingsI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem;
import com.jme3.system.JmeSystem.StorageFolderType;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class JmeAppOS extends AppOS {
	public JmeAppOS(String strApplicationBaseSaveDataPath,StorageFolderType esft) {
		super(strApplicationBaseSaveDataPath);
		
		setStorageFolderType(esft);
		
		if(GlobalAppSettingsI.iGlobal().isSet()){
			setApplicationTitle(GlobalAppSettingsI.i().getTitle());
		}
	}

	@Override
	protected void verifyBaseSaveDataPath() {
		File fl = JmeSystem.getStorageFolder(getStorageFolderType());
		verifyBaseSaveDataPath(fl.getAbsolutePath()+File.separator+getApplicationBaseFolderName());
	}

	private StorageFolderType	esft;
	private Spatial	sptAlert;
	public StorageFolderType getStorageFolderType() {
		if(esft==null)throw new PrerequisitesNotMetException("strStorageFolderType is null",StorageFolderType.class);
		return esft;
	}
	public void setStorageFolderType(StorageFolderType esft) {
		PrerequisitesNotMetException.assertNotAlreadySet("storage folder type", this.esft, esft, this);
		this.esft = esft;
	}

	public Spatial getAlertSpatial() {
		return sptAlert;
	}
	
	protected void setAlertSpatial(Spatial spt){
		this.sptAlert=spt;
	}
	
//	@Override
//	protected void dumpAlert() {
//		super.dumpAlert();
//		
//		float fZ=0;
//		for(Spatial spt:MiscJmeI.i().getAllChildrenRecursiveFrom(GlobalGUINodeI.i())){
//			if(spt.getLocalTranslation().z > fZ)fZ=spt.getLocalTranslation().z;
//		}
//		
//		Vector3f v3fAppWindowSize = MiscJmeI.i().getAppWindowSize();
//		v3fAppWindowSize.x*=0.5f;
//		v3fAppWindowSize.y*=0.5f;
//		v3fAppWindowSize.z=fZ+10;
//		
//		btAlert.setLocalTranslation(v3fAppWindowSize);//new Vector3f(Display.getWidth()/2f, Display.getHeight()/2f, fZ+10)); //above any other gui elements  
//	}
//	
//	private BitmapText	btAlert;
//	@Override
//	public StackTraceElement[] showSystemAlert(String strMsg) {
//		StackTraceElement[] aste = super.showSystemAlert(strMsg);
//		
//		if(btAlert==null){
//			btAlert = new BitmapText(GlobalAppRefI.i().getAssetManager().loadFont("Interface/Fonts/Console.fnt"));
//			GlobalGUINodeI.i().attachChild(btAlert);
//		}
//		
//		if(!btAlert.getText().equals(strMsg)){
//			btAlert.setText(strMsg);
//		}
//		
//		return aste;
//	}
//	
//	@Override
//	public void hideSystemAlert(StackTraceElement[] asteFrom) {
//		super.hideSystemAlert(asteFrom);
//		btAlert.removeFromParent();
//		btAlert=null;
//	}
}
