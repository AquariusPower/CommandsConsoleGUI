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

package misc;

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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class Misc {
//	public long lLastUniqueId = 0;
	private IHandleExceptions	ihe;
	private String	strLastUid = "0";
	
	public void initialize(IHandleExceptions ihe){
		this.ihe=ihe;
	}
	
	private static Misc instance = new Misc();
	public static Misc i(){return instance;}
	
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
		return new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
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
	
	public ArrayList<String> fileLoad(String strFile)  {
		return fileLoad(new File(strFile));
	}
	public ArrayList<String> fileLoad(File fl)  {
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
					ihe.handleException(e);
				}finally{
					if(br!=null)br.close();
				}
			} catch (IOException e) {
				ihe.handleException(e);
			}
		}else{
			ihe.handleException(new FileNotFoundException("File not found: "+fl.getAbsolutePath()));
//			dumpWarnEntry("File not found: "+fl.getAbsolutePath());
		}
		
		return astr;
	}
	public void fileAppendList(File fl, ArrayList<String> astr) {
		BufferedWriter bw = null;
		try{
			try {
				bw = new BufferedWriter(new FileWriter(fl, true));
				for(String str:astr){
					bw.write(str);
					bw.newLine();
				}
			} catch (IOException e) {
				ihe.handleException(e);
			}finally{
				if(bw!=null)bw.close();
			}
		} catch (IOException e) {
			ihe.handleException(e);
		}
	}
	
	public void fileAppendLine(File fl, String str) {
		ArrayList<String> astr = new ArrayList<String>();
		astr.add(str);
		fileAppendList(fl, astr);
	}
	
	/**
	 * This allows for unrelated things have same uid.
	 * @param strLastId
	 * @return
	 */
	public String getNextUniqueId(String strLastId){
		BigInteger bi = new BigInteger(strLastId);
		bi=bi.add(new BigInteger("1"));
		return bi.toString(36);
	}
	
	/**
	 * This uses a global uid, 
	 * unrelated things can have the same uid tho.
	 * @return
	 */
	public String getNextUniqueId(){
//		return ""+(++lLastUniqueId);
		return getNextUniqueId(strLastUid );
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
			ihe.handleException(e);
		}
		
		return "";
	}
}
