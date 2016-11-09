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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.TreeMap;

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
	
	private TreeMap<String,Integer> tmIdCode = new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER);
	
	public AppOS(String strApplicationBaseSaveDataPath) {
		setApplicationBaseSaveDataPath(strApplicationBaseSaveDataPath);
		
		int iSpecialCodeStart=Integer.MAX_VALUE;
		addKeyIdCode("ctrl"	, iSpecialCodeStart--);
		addKeyIdCode("shift", iSpecialCodeStart--);
		addKeyIdCode("alt"	, iSpecialCodeStart--);
		
		addKeyIdCode("mouseWheelUp"	, iSpecialCodeStart--);
		addKeyIdCode("mouseWheelDown"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton0"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton1"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton2"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton3"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton4"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton5"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton6"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton7"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton8"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton9"	, iSpecialCodeStart--);
	}
	
	/**
	 * 
	 * @param strId
	 * @param iCode
	 * @return true if added or already set with same code, false if already set with different code
	 */
	public boolean addKeyIdCode(String strId, Integer iCode){
		Integer iCodeExisting = tmIdCode.get(strId);
		if(iCodeExisting!=null){
			if(iCodeExisting==iCode){
				MsgI.i().devWarn("already set", strId, iCode);
				return true;
			}else{
				MsgI.i().devWarn("cannot modify the code for", strId, iCodeExisting, iCode);
				return false;
			}
		}
		
		if(tmIdCode.values().contains(iCode)){
			String strExistingId=null;
			for(Entry<String, Integer> entry:tmIdCode.entrySet()){
				if(entry.getValue()==iCode){
					strExistingId=entry.getKey();
					MsgI.i().devWarn("(multiplicity) already contains code", strExistingId, iCode, strId);
				}
			}
			
//			throw new PrerequisitesNotMetException("already contains code", strExistingId, iCode, strKey);
		}
		
		tmIdCode.put(strId, iCode);
		
		return true;
	}
	
	public ArrayList<String> getAllKeyCodesReport(){
		ArrayList<String> astr = new ArrayList<String>();
		for(Entry<String, Integer> entry:tmIdCode.entrySet()){
			astr.add(entry.getKey()+"="+entry.getValue());
		}
		Collections.sort(astr);
		return astr;
	}
	
	/**
	 * 
	 * @param cl
	 * @param strKeyIdPrefixFilter can be null
	 * @return
	 */
	public boolean fillKeyIdCodeFrom(Class<?> cl, String strKeyIdPrefixFilter){
//		if(tmIdCode.size()>0){			return;		}
		try {
			int iMaxCode=-1;
			for(Field fld:cl.getFields()){
				if(strKeyIdPrefixFilter!=null && !strKeyIdPrefixFilter.isEmpty()){
					if(!fld.getName().startsWith(strKeyIdPrefixFilter))continue;
				}
				
				int iCode=(Integer)fld.get(null);
				if(iCode>iMaxCode)iMaxCode=iCode;
				
				String strId=fld.getName();//.substring(4); //removes the KEY_ prefix
				
				if(!addKeyIdCode(strId,iCode)){
					throw new PrerequisitesNotMetException("keycode filling failed",strId,iCode,cl,strKeyIdPrefixFilter);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new PrerequisitesNotMetException("unexpected").setCauseAndReturnSelf(e);
		}
		
		return true;
	}
	
	public int getKeyCode(String strId){
		return tmIdCode.get(strId);
	}
	
	public String getKeyId(int iCode){
		for(Entry<String, Integer> entry:tmIdCode.entrySet()){
			if(entry.getValue()==iCode)return entry.getKey();
		}
		return null;
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
}
