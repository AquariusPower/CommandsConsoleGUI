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

package com.github.commandsconsolegui;

import java.io.File;

import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;

/**
 * Very basic configurations related to the OS goes here.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class AppOS {
//	private static OperationalSystem instance = new OperationalSystem();
//	public static OperationalSystem i(){return instance;}
	
	private File	flBaseSaveDataPath;
	
	public AppOS(String strApplicationBaseSaveDataPath) {
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
		PrerequisitesNotMetException.assertNotAlreadySet("base application folder already set", this.strApplicationBaseSaveDataPath, str);
		this.strApplicationBaseSaveDataPath = str;
	}
	public String getApplicationBaseFolderName(){
		if(strApplicationBaseSaveDataPath==null)throw new PrerequisitesNotMetException("app folder name not set");
		return strApplicationBaseSaveDataPath;
	}

	private String strApplicationTitle = null;
	public void setApplicationTitle(String strApplicationTitle) {
		PrerequisitesNotMetException.assertNotAlreadySet("app title", this.strApplicationTitle, strApplicationTitle, this);
		this.strApplicationTitle=strApplicationTitle;
	}
	
	public String getApplicationTitle() {
		return strApplicationTitle;
	}

	private boolean	bExiting=false;
	private String	strAlertMsg;
	private long	lLastAlertMilis;
	private StackTraceElement[]	asteDebugAlertFrom;
	/**
	 * this is important to let some other threads know the application is exiting and behave properly
	 */
	public void setAppExiting(){
		System.err.println("Exiting... ");
//		Thread.dumpStack();
		this.bExiting=true;
	}
	public boolean isApplicationExiting(){
		//TODO is there some way to test if JME is exiting???
		return bExiting;
	}
	
	public void update(float fTpf){
		if(strAlertMsg!=null){
			if( (lLastAlertMilis+1500) < System.currentTimeMillis()){
				dumpAlert();
				lLastAlertMilis=System.currentTimeMillis();
			}
		}
	}
	
	protected void dumpAlert(){
		System.err.println("Alert!!! "+strAlertMsg);
	}
	
	public String getAlertMessage(){
		return strAlertMsg;
	}
	
	public boolean isShowingAlert(){
		return strAlertMsg!=null;
	}
	
	public StackTraceElement[] showSystemAlert(String strMsg){
		PrerequisitesNotMetException.assertNotAlreadySet("system alert message", this.strAlertMsg, strMsg, asteDebugAlertFrom, this);
		this.asteDebugAlertFrom=Thread.currentThread().getStackTrace();
		this.strAlertMsg=strMsg;
//		dumpAlert(); //just in case another one happens before the update...
		return this.asteDebugAlertFrom;
	}
	public void hideSystemAlert(StackTraceElement[] asteFrom){
		PrerequisitesNotMetException.assertIsTrue("alert from matches", asteFrom==asteDebugAlertFrom, asteFrom, asteDebugAlertFrom, this);
		this.strAlertMsg=null;
	}
}
