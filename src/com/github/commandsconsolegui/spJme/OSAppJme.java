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

package com.github.commandsconsolegui.spJme;

import java.io.File;
import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.OSApp;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spJme.globals.GlobalAppSettingsI;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem;
import com.jme3.system.JmeSystem.StorageFolderType;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class OSAppJme extends OSApp {
	public OSAppJme(String strApplicationBaseSaveDataPath,StorageFolderType esft) {
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
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,IllegalAccessException {
		if(fld.getDeclaringClass()!=OSAppJme.class)return super.getFieldValue(fld); //For subclasses uncomment this line
		return fld.get(this);	}


	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=OSAppJme.class){super.setFieldValue(fld,value);return;} //For subclasses uncomment this line
		fld.set(this,value);
	}
}
