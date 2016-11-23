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
package com.github.commandsconsolegui.spLemur.dialog;

import java.io.File;
import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.globals.GlobalAppOSI;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spCmd.varfield.FileVarField;
import com.github.commandsconsolegui.spJme.DialogStateAbs;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;


/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class FileChoiceDialogStateI extends ChoiceLemurDialogStateAbs<Command<Button>,FileChoiceDialogStateI> {
	private static FileChoiceDialogStateI	instance=new FileChoiceDialogStateI();
	public static FileChoiceDialogStateI i(){return instance;}
	
	public static class CfgParm extends ChoiceLemurDialogStateAbs.CfgParm{
		public CfgParm() {
			super(null,null,null,null);
		}
	}
	
//	private File	flChosen;
	
	CfgParm cfg;
	
	@Override
	public FileChoiceDialogStateI configure(ICfgParm icfg) {
		this.cfg=(CfgParm)icfg;
		setParentDiagIsRequired(true);
		super.configure(icfg);
		return getThis();
	}
	
	@Override
	protected void updateList() {
		clearList();
		
		DialogListEntryData<Command<Button>,File> dledAtParent = getParentReferencedDledListCopy().get(0);
		Object obj = dledAtParent.getLinkedObj();
		File flAtParentTmp;
		if (obj instanceof FileVarField) {
			FileVarField flf = (FileVarField) obj;
			flAtParentTmp = flf.getFile();
		}else
		if (obj instanceof File) {
			flAtParentTmp = (File) obj;
		}else{
			throw new PrerequisitesNotMetException("unsupported type", obj.getClass(), obj);
		}
		
		if(!flAtParentTmp.exists()){ //try assets folder
			flAtParentTmp=new File(GlobalAppOSI.i().getAssetsFolder()+File.separator+flAtParentTmp.getPath());
			PrerequisitesNotMetException.assertIsTrue("path exist", flAtParentTmp.exists(), flAtParentTmp);
		}
		
		File flDir = flAtParentTmp.getParentFile();
		
		MsgI.i().debug("current path",(new File(".")).getAbsolutePath()); //TODO this dont work why?
		
		DialogListEntryData<Command<Button>,File> dledSelectTmp=null;
		
		//folders 
		for(File flTmp:flDir.listFiles()){ //flDir=new File("./Sounds/Effects/UI/13940__gameaudio__game-audio-ui-sfx/")
			if(!flTmp.isDirectory())continue;
			DialogListEntryData dled = new DialogListEntryData(this);
			dled.setText(">"+flTmp.getName()+"/", flTmp); // prepended a token just to help on sorting
			addEntry(dled);
//			if(flTmp.equals(flAtParent)){
//				dledSelectTmp=dled;
//			}
		}
		
		// files
		for(File flTmp:flDir.listFiles()){ //flDir=new File("./Sounds/Effects/UI/13940__gameaudio__game-audio-ui-sfx/")
			if(!flTmp.isFile())continue;
			DialogListEntryData dled = new DialogListEntryData(this);
			dled.setText(flTmp.getName(), flTmp);
			addEntry(dled);
//			if(flTmp.equals(flAtParent)){
//				dledSelectTmp=dled;
//			}
		}
		
//		for(DialogListEntryData<Command<Button>> dled:getCompleteEntriesListCopy()){
//			if(dled.getLinkedObj().getName().equals(dledAtParent.getLinkedObj()))
//		}
		
//		final DialogListEntryData<Command<Button>> dledSelect=dledSelectTmp;
		final File flAtParent = flAtParentTmp;
		ManageCallQueueI.i().addCall(new CallableX(this,100) {
			@Override
			public Boolean call() {
				for(DialogListEntryData dled:getCompleteEntriesListCopy()){
					File fl = (File)dled.getLinkedObj();
					if(fl.equals(flAtParent)){
						selectEntry(dled);
						return true;
					}
				}
				return false;
			}
		});
		
		super.updateList();
	}
	
	@Override
	protected boolean enableAttempt() {
		if(!super.enableAttempt())return false;
		
		updateList();
		
		return true;
	}
	
	@Override
	protected FileChoiceDialogStateI getThis() {
		return this;
	}
	
//	@Override
//	protected FileChoiceDialogStateI resetChoice() {
//		this.flChosen=null;
//		return super.resetChoice();
//	}
	
//	public File getChosenFileAndReset(){
//		File fl=this.flChosen;
//		this.flChosen=null;
//		return fl;
//	}
//	public void setInitiallyChosenFile(File fl) {
//		PrerequisitesNotMetException.assertNotAlreadySet("chosen file", this.flChosen, fl, this);
//		PrerequisitesNotMetException.assertNotNull("chosen file", fl, this);
//		this.flChosen=fl;
//	}
	
	@Override
	public void setFieldValue(Field fld, Object value)  throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=FileChoiceDialogStateI.class){super.setFieldValue(fld,value);return;} //For subclasses uncomment this line
		fld.set(this,value);
	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,IllegalAccessException {
		if(fld.getDeclaringClass()!=FileChoiceDialogStateI.class)return super.getFieldValue(fld); //For subclasses uncomment this line
		return fld.get(this);
	}

//	public boolean isChosenFileReset() {
//		return flChosen==null;
//	}
	
//	@Override
//	public void selectEntry(DialogListEntryData<Command<Button>> dledSelectRequested) {
//		super.selectEntry(dledSelectRequested);
//		File fl = (File)dledSelectRequested.getLinkedObj();
//		PrerequisitesNotMetException.assertNotNull("selected file",fl,this);
//		flChosen=fl;
//	}
	
	@Override
	public <T extends DialogStateAbs.SaveDiag> T load(Class<T> clCS) {
		return super.load(clCS);
	}
}
