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

package com.github.commandsconsolegui.spAppOs;

import java.io.File;
import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.scene.Spatial;

/**
 * Very basic configurations related to the OS goes here.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class OSApp implements IReflexFillCfg{
//	private static OperationalSystem instance = new OperationalSystem();
//	public static OperationalSystem i(){return instance;}
	
	private File	flBaseSaveDataPath;
	
	public OSApp(String strApplicationBaseSaveDataPath) {
		setApplicationBaseSaveDataPath(strApplicationBaseSaveDataPath);
	}
	
	
	public File getBaseSaveDataPath(){
		verifyBaseSaveDataPath();
		return flBaseSaveDataPath;
	}
	
	protected void verifyBaseSaveDataPath(){
		String strRelativeToRunPath="."+File.separator;
		verifyBaseSaveDataPath(strRelativeToRunPath+getApplicationBaseFolderName());
	}
	
	protected void verifyBaseSaveDataPath(String strPath){
		if(flBaseSaveDataPath!=null)return;
		flBaseSaveDataPath = new File(strPath+File.separator);
		if(!flBaseSaveDataPath.exists()){
			if(flBaseSaveDataPath.mkdirs()){
				MsgI.i().debug("created path: "+flBaseSaveDataPath.getAbsolutePath(), true, this);
			}
		}
	}
	
	private String strApplicationBaseSaveDataPath = null;
	public void setApplicationBaseSaveDataPath(String str) {
//		if(this.strApplicationBaseSaveDataPath!=null)PrerequisitesNotMetException.assertNotAlreadySet("base application folder already set", this.strApplicationBaseSaveDataPath, str);
		PrerequisitesNotMetException.assertNotAlreadySet(this.strApplicationBaseSaveDataPath, str, "base application folder already set");
		this.strApplicationBaseSaveDataPath = str;
	}
	public String getApplicationBaseFolderName(){
		if(strApplicationBaseSaveDataPath==null)throw new PrerequisitesNotMetException("app folder name not set");
		return strApplicationBaseSaveDataPath;
	}

	private String strApplicationTitle = null;
	public void setApplicationTitle(String strApplicationTitle) {
		PrerequisitesNotMetException.assertNotAlreadySet(this.strApplicationTitle, strApplicationTitle, "app title", this);
		this.strApplicationTitle=strApplicationTitle;
	}
	
	public String getApplicationTitle() {
		return strApplicationTitle;
	}

	private boolean	bExiting=false;
	private String	strAlertMsg;
	private long	lLastAlertMilis;
	private StackTraceElement[]	asteStackKeyRequestOrigin;
	private boolean	bFirstTimeQuickUpdate;
	private String	strDynamicInfo="";
	private StackTraceElement[]	asteLastValidHideRequestOrigin;
	private Object	objActionSourceElement;
	/**
	 * this is important to let some other threads know the application is exiting and behave properly
	 */
	public void setAppExiting(){
		String str="["+OSApp.class.getSimpleName()+"]";
		System.out.println(str+"Exit requested from stack:");
		for(StackTraceElement ste:Thread.currentThread().getStackTrace()){
			System.out.println(str+ste);
		}
		System.out.println(str+"Exiting... ");
		this.bExiting=true;
	}
	public boolean isApplicationExiting(){
		//TODO is there some way to test if JME is exiting???
		return bExiting;
	}
	
	public void update(float fTpf){
		if(strAlertMsg!=null){
			if( bFirstTimeQuickUpdate || ((lLastAlertMilis+1500) < System.currentTimeMillis()) ){
				dumpAlert();
				lLastAlertMilis=System.currentTimeMillis();
				bFirstTimeQuickUpdate=false;
			}
		}
	}
	
	protected void dumpAlert(){
		System.err.println(getFullMessage().replace("\n", ";"));
	}
	
	public String getFullMessage(){
		return "ALERT!!!\n"
				+getAlertMessage()+"\n"
				+"DynamicInfo: "+getDynamicInfo()+"\n";
	}
	
	public String getAlertMessage(){
		return strAlertMsg;
	}
	
	public boolean isShowingAlert(boolean bFullAlert){
		return strAlertMsg!=null;
	}
	
	protected Object getActionSourceElement(){
		return objActionSourceElement;
	}
	
	public StackTraceElement[] showSystemAlert(String strMsg, Object objActionSourceElement){
		PrerequisitesNotMetException.assertNotAlreadySet(this.strAlertMsg, strMsg, "system alert message", asteStackKeyRequestOrigin, this);
		this.asteStackKeyRequestOrigin=Thread.currentThread().getStackTrace();
		this.strAlertMsg=strMsg;
		bFirstTimeQuickUpdate=true;
		this.objActionSourceElement=objActionSourceElement;
//		dumpAlert(); //just in case another one happens before the update...
		return this.asteStackKeyRequestOrigin;
	}
	
	/**
	 * 
	 * @param asteStackKeyRequestOrigina
	 * @param bKeepGuiBlockerOnce useful when going to retry having subsequent alerts (DevNote: will only work if overriden by GUI)
	 */
	public void hideSystemAlert(StackTraceElement[] asteStackKeyRequestOrigin, boolean bKeepGuiBlockerOnce) {
		if(this.strAlertMsg!=null){
			this.asteLastValidHideRequestOrigin=Thread.currentThread().getStackTrace();
		}else{
			MsgI.i().warn("already hidden", this, this.asteLastValidHideRequestOrigin);
		}
		
		PrerequisitesNotMetException.assertIsTrue("alert origin matches", asteStackKeyRequestOrigin==this.asteStackKeyRequestOrigin, asteStackKeyRequestOrigin, this.asteStackKeyRequestOrigin, this);
		this.strAlertMsg=null;
		this.asteStackKeyRequestOrigin=null;
	}
	public void hideSystemAlert(StackTraceElement[] asteFrom){
		hideSystemAlert(asteFrom, false);
	}


	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,IllegalAccessException {
		 return fld.get(this);
	}


	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}


//	@Override
//	public String getUniqueId() {
//		throw new UnsupportedOperationException("method not implemented yet");
//	}


	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcvField);
	}


	public void setDynamicInfo(String str) {
		this.strDynamicInfo=str;
	}
	
	public String getDynamicInfo(){
		return strDynamicInfo;
	}
	
	public boolean isDynamicInfoSet(){
		return !strDynamicInfo.isEmpty();
	}


	public String getAssetsFolder() {
		return "./assets/";
	}
}