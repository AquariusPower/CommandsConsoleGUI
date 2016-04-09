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

package com.github.commandsconsolegui.misc;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.naming.directory.BasicAttributes;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class MiscI {
//	public long lLastUniqueId = 0;
	private IHandleExceptions	ihe = HandleExceptionsRaw.i();
	private String	strLastUid = "0";
	private boolean	bConfigured;
	
	public void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		this.ihe=ihe;
		bConfigured=true;
	}
	
	private static MiscI instance = new MiscI();
	public static MiscI i(){return instance;}
	
	public Boolean parseBoolean(String strValue){
		if(strValue.equalsIgnoreCase("true"))return new Boolean(true);
		if(strValue.equalsIgnoreCase("1"))return new Boolean(true);
		if(strValue.equalsIgnoreCase("false"))return new Boolean(false);
		if(strValue.equalsIgnoreCase("0"))return new Boolean(false);
		throw new NumberFormatException("invalid boolean value: "+strValue);
	}

	public String getSimpleTime(boolean bShowMilis){
		return "["+new SimpleDateFormat("HH:mm:ss"+(bShowMilis?".SSS":"")).format(Calendar.getInstance().getTime())+"]";
	}
	
	public String getDateTimeForFilename(){
		return getDateTimeForFilename(false);
	}
	public String getDateTimeForFilename(boolean bShowMilis){
		return getDateTimeForFilename(System.currentTimeMillis(), bShowMilis);
	}
	public String getDateTimeForFilename(long lTimeMilis, boolean bShowMilis){
		String strMilis="";
		if(bShowMilis)strMilis="-SSS";
		
		return new SimpleDateFormat("yyyyMMdd-HHmmss"+strMilis)
			.format(new Date(lTimeMilis));
//			.format(Calendar.getInstance().getTime());
	}
	
	public String fmtFloat(double d){
		return fmtFloat(d,-1);
	}
	public String fmtFloat(Float f, int iDecimalPlaces){
		return fmtFloat(f==null?null:f.doubleValue(),iDecimalPlaces);
	}
	public String fmtFloat(Double d,int iDecimalPlaces){
		if(iDecimalPlaces==-1)iDecimalPlaces=2;
		return d==null?"null":String.format("%."+iDecimalPlaces+"f", d);
	}
	
	synchronized public ArrayList<String> fileLoad(String strFile)  {
		return fileLoad(new File(strFile));
	}
	synchronized public ArrayList<String> fileLoad(File fl)  {
		ArrayList<String> astr = new ArrayList<String>();
		if(fl.exists()){
			try{
				BufferedReader br=null;
		    try {
		    	br = new BufferedReader(new FileReader(fl));
		    	while(true){
						String strLine = br.readLine();
						if(strLine==null)break;
						astr.add(strLine);
		    	}
				} catch (IOException e) {
					ihe.handleExceptionThreaded(e);
				}finally{
					if(br!=null)br.close();
				}
			} catch (IOException e) {
				ihe.handleExceptionThreaded(e);
			}
		}else{
			ihe.handleExceptionThreaded(new FileNotFoundException(fl.getAbsolutePath()));
//			dumpWarnEntry("File not found: "+fl.getAbsolutePath());
		}
		
		return astr;
	}
	synchronized public void fileAppendListTS(File fl, ArrayList<String> astr) {
		BufferedWriter bw = null;
		try{
			try {
				if(!fl.exists())fl.createNewFile();
				bw = new BufferedWriter(new FileWriter(fl, true));
				for(String str:astr){
					bw.write(str);
					bw.newLine();
				}
			} catch (IOException e) {
				ihe.handleExceptionThreaded(e);
			}finally{
				if(bw!=null)bw.close();
			}
		} catch (IOException e) {
			ihe.handleExceptionThreaded(e);
		}
	}
	
	/**
	 * 
	 * @param str
	 * @param bCapitalize if false will lower case
	 * @return
	 */
	public String firstLetter(String str, boolean bCapitalize){
		if(bCapitalize){
			return Character.toUpperCase(str.charAt(0))
				+str.substring(1);
		}else{
			return Character.toLowerCase(str.charAt(0))
				+str.substring(1);
		}
	}
	
	/**
	 * full package and class name prettified to be an Id
	 * @param cl
	 * @param bFirstLetterUpperCase
	 * @return
	 */
	public String makePretty(Class<?> cl, boolean bFirstLetterUpperCase){
		String strPkgClass = cl.getName();
		String[] astr = strPkgClass.split("[.]");
		String strPretty="";
		for(String str:astr){
			strPretty+=firstLetter(str, true);
		}
		return strPretty;
	}
	
	/**
	 * make all uppercase and underscored string into a pretty Id
	 * @param strCommand
	 * @param bFirstLetterUpperCase
	 * @return
	 */
	public String makePretty(String strCommand, boolean bFirstLetterUpperCase){
		/**
		 * upper case with underscores
		 */
		String strCmdNew = null;
		for(String strWord : strCommand.split("_")){
			if(strCmdNew==null){
				if(bFirstLetterUpperCase){
					strCmdNew=firstLetter(strWord.toLowerCase(),true);
				}else{
					strCmdNew=strWord.toLowerCase();
				}
			}else{
				strCmdNew+=firstLetter(strWord.toLowerCase(),true);
			}
		}
		
		return strCmdNew;
	}
	
	synchronized public void fileAppendLine(File fl, String str) {
		ArrayList<String> astr = new ArrayList<String>();
		astr.add(str);
		fileAppendListTS(fl, astr);
	}
	
	/**
	 * "Unlimited?" uid!
	 * This allows for unrelated/non-conflicting things to have same uid.
	 * 
	 * @param strLastId
	 * @return
	 */
	public String getNextUniqueId(String strLastId){
		int iRadix=36;
		BigInteger bi = new BigInteger(strLastId,iRadix);
		bi=bi.add(new BigInteger("1"));
		return bi.toString(iRadix);
	}
	
	/**
	 * This uses a global uid.
	 * @return
	 */
	public String getNextUniqueId(){
		strLastUid=getNextUniqueId(strLastUid);
		return strLastUid;
	}

	public String retrieveClipboardString() {
		return retrieveClipboardString(false);
	}
	public void putStringToClipboard(String str){
		StringSelection ss = new StringSelection(str);
		Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(ss, ss);
	}

	/**
	 * this is heavy...
	 * @param bEscapeNL good to have single line result
	 * @return
	 */
	public String retrieveClipboardString(boolean bEscapeNL) {
		try{
			Transferable tfbl = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			String str = (String) tfbl.getTransferData(DataFlavor.stringFlavor);
			if(bEscapeNL){
				str=str.replace("\n", "\\n");
			}
			return str;
		} catch (UnsupportedFlavorException | IOException e) {
			ihe.handleExceptionThreaded(e);
		}
		
		return "";
	}
	
	synchronized public BasicFileAttributes fileAttributesTS(File fl){
//		if(fl.exists()){
			try {
				return Files.readAttributes(fl.toPath(), BasicFileAttributes.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}else{
//			ihe.handleException(new NullPointerException("file not found "+fl.getAbsolutePath()));
//		}
		
		return null;
	}
	
}
