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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.naming.directory.InvalidAttributeValueException;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MiscI {
	private static MiscI instance = new MiscI();
	public static MiscI i(){return instance;}
	
//	public long lLastUniqueId = 0;
	private IHandleExceptions	ihe = SimpleHandleExceptionsI.i();
	private String	strLastUid = "0";
	private boolean	bConfigured;
//	private SimpleApplication	sapp;
	
	public void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		
		if(ihe==null)throw new NullPointerException("invalid instance for "+IHandleExceptions.class.getName());
		this.ihe=ihe;
		
//		this.sapp=sapp;
		bConfigured=true;
	}
	
	public Boolean parseBoolean(String strValue){
		if(strValue.equalsIgnoreCase("true"	))return new Boolean(true);
		if(strValue.equalsIgnoreCase("1"		))return new Boolean(true);
		if(strValue.equalsIgnoreCase("false"))return new Boolean(false);
		if(strValue.equalsIgnoreCase("0"		))return new Boolean(false);
		throw new NumberFormatException("invalid boolean value: "+strValue);
	}

	public String getSimpleTime(boolean bShowMilis){
		return getSimpleTime(null, bShowMilis);
	}
	public String getSimpleTime(Long lMilis, boolean bShowMilis){
		Date date = null;
		
		if(lMilis==null){
			date = Calendar.getInstance().getTime();
		}else{
			date = new Date(lMilis);
		}
		
		return "["+new SimpleDateFormat("HH:mm:ss"+(bShowMilis?".SSS":"")).format(date)+"]";
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
	public String getSimpleDate() {
		return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
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
	synchronized public boolean fileAppendListTS(File fl, ArrayList<String> astr) {
		BufferedWriter bw = null;
		boolean bWrote=false;
		try{
			try {
				if(!fl.exists()){
					File flParent = fl.getParentFile();
					if(flParent!=null){
						if(!flParent.exists()){
							flParent.mkdirs();
						}
					}
					fl.createNewFile();
				}
				bw = new BufferedWriter(new FileWriter(fl, true));
				for(String str:astr){
					bw.write(str);
					bw.newLine();
				}
				
				bWrote=true;
			} catch (IOException e) {
				ihe.handleExceptionThreaded(e);
			}finally{
				if(bw!=null)bw.close();
			}
		} catch (IOException e) {
			ihe.handleExceptionThreaded(e);
		}
		
		return bWrote;
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
	 * @param iOnlyFirstLettersCount 
	 * 	If null, will use the full words,
	 * 	otherwise, just the first count letters of each word.
	 * 	If -1, will use the default.
	 * 	If 0 will use nothing, is like ignoring this function.
	 * @return
	 */
	public String makePretty(Class<?> cl, boolean bFirstLetterUpperCase, Integer iOnlyFirstLettersCount){
		if(iOnlyFirstLettersCount!=null && iOnlyFirstLettersCount.intValue()==0)return "";
		if(iOnlyFirstLettersCount.intValue()==-1)iOnlyFirstLettersCount=1;
		String strPkgClass = cl.getName();
		String[] astr = strPkgClass.split("[.]");
		String strPretty="";
		for(String str:astr){
			String strWord=firstLetter(str, true);
			if(iOnlyFirstLettersCount!=null){
				if(strWord.length()>=iOnlyFirstLettersCount){
					strWord=strWord.substring(0,iOnlyFirstLettersCount);
				}
			}
			strPretty+=strWord;
		}
		return strPretty;
	}
	
	/**
	 * make all uppercase and underscored string into a pretty Id
	 * @param strCommand
	 * @param bFirstLetterUpperCase (of each part when words are separated by "_")
	 * @return
	 */
	public String makePretty(String strCommand, boolean bFirstLetterUpperCase){
		/**
		 * upper case with underscores
		 */
		String strCmdNew = null;
		for(String strWord : strCommand.split("_")){
			boolean bIsAllUpperCase = !strWord.matches(".*[a-z].*");
			if(bIsAllUpperCase){
				if(strCmdNew==null){
					if(bFirstLetterUpperCase){
						strCmdNew=firstLetter(strWord.toLowerCase(),true);
					}else{
						strCmdNew=strWord.toLowerCase();
					}
				}else{
					strCmdNew+=firstLetter(strWord.toLowerCase(),true);
				}
			}else{
				strCmdNew+=firstLetter(strWord,true);
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
//		if(strLastId==null)strLastId="0";
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
	
	synchronized public BasicFileAttributes fileReadAttributesTS(File fl){
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
	
//	/**
//	 * 
//	 * @param sptStart
//	 * @return parentest spatial, least top nodes
//	 */
//	public Spatial getParentestFrom(Spatial sptStart){
//		Spatial sptParentest = sptStart;
//		while(sptParentest.getParent()!=null){
//			if(sapp.getGuiNode().equals(sptParentest.getParent()))break;
//			if(sapp.getRootNode().equals(sptParentest.getParent()))break;
//			sptParentest=sptParentest.getParent();
//		}
//		return sptParentest;
//	}
	
	public static enum EStringMatchMode{
		Exact,
		Contains,
		
		StartsWith,
		EndsWith,
		
		Regex,
		
		/**
		 * like a regex for each letter ex.: "Test"
		 * regex = ".*[T].*[e].*[s].*[t].*"
		 */
		Fuzzy,
		;
	}
	public boolean containsFuzzyMatch(String strToCheck, String strMatch, EStringMatchMode eMode, boolean bIgnoreCase){
		if(bIgnoreCase){
			strToCheck=strToCheck.toLowerCase();
			strMatch=strMatch.toLowerCase();
		}
		
		switch (eMode) {
			case StartsWith:
				return strToCheck.startsWith(strMatch);
			case Contains:
				return strToCheck.contains(strMatch);
			case EndsWith:
				return strToCheck.endsWith(strMatch);
			case Exact:
				return strToCheck.equals(strMatch);
			case Fuzzy:
				int iFuzzyIndex = 0;
				for(char c : strToCheck.toCharArray()){
					if(c == strMatch.charAt(iFuzzyIndex)){
						iFuzzyIndex++;
						if(strMatch.length()==iFuzzyIndex){
							return true;
						}
					}
				}
				return false;
			case Regex:
				return strToCheck.matches(strMatch);
		}
		
		return false;
	}
	
	/**
	 * use Arrays.deepEquals(asteA, asteB);
	 * 
	 * @param asteA
	 * @param asteB
	 * @return
	 */
	@Deprecated
	public boolean compareStackTraces(StackTraceElement[] asteA, StackTraceElement[] asteB){
		for(int i=0;i<asteA.length;i++){
			StackTraceElement ste = asteA[i];
			if(!ste.equals(asteB[i])){
//			if(!ste.toString().equals(asteB[i].toString())){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This is a safe reflex.
	 * 
	 * @param clazzOfObjectFrom what superclass of the object from is to be used?
	 * @param objValueMatch the 1st field matching this value will return it's name
	 * @return field name
	 */
	public String getFieldNameForValue(Object objFieldDeclaredAt, Object objValueToMatch, String strAllowManySeparator, String strPrefixRemove,  boolean bMakePretty){
		String str="";
		boolean bAllowMany = strAllowManySeparator!=null;
		try{
			for(Field field:objFieldDeclaredAt.getClass().getFields()){
				if(objValueToMatch.equals(field.get(objFieldDeclaredAt))){
					if(!str.isEmpty())str+=strAllowManySeparator;
					
					String strName = field.getName();
					if(strPrefixRemove!=null && strName.startsWith(strPrefixRemove)){
						strName = strName.substring(strPrefixRemove.length());
					}
					
					if(bMakePretty)strName = MiscI.i().makePretty(strName, true);
					
					str += strName;
					
					if(!bAllowMany)break;
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			ihe.handleExceptionThreaded(e);
		}
		
		return str.isEmpty()?null:str;
	}

	public ArrayList<File> listFiles(String strPath) throws FileNotFoundException {
		ArrayList<File> aflList = new ArrayList<File>();
		
		if(!strPath.startsWith("./") && !strPath.startsWith("/")){
			strPath="./"+strPath;
		}
		
		File flFolder = new File(strPath);
		if(!flFolder.exists()){
			throw new FileNotFoundException(strPath);
		}
		
		aflList.addAll(Arrays.asList(flFolder.listFiles()));
		
//		File[] afl = flFolder.listFiles();
//		
//		for(File fl:afl){
//			if(fl.isDirectory())aflList.add(fl);
//		}
//		
//		for(File fl:afl){
//			if(fl.isFile())aflList.add(fl);
//		}
		
		Collections.sort(aflList,cmpFiles);
		
		return aflList;
	}
	
	Comparator<File> cmpFiles = new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			if(
				o1.isDirectory() && o2.isDirectory()
				||
				o1.isFile() && o2.isFile()
			){
				return o1.compareTo(o2);
			}else{
				if(o1.isDirectory() && o2.isFile())return -1;
				if(o1.isFile() && o2.isDirectory())return 1;
			}
			
			return 0; //TODO what reaches here?
		}
	};
	
	public void assertSameClass(Class<?> cl, Class<?> clOther){
		if(!cl.isAssignableFrom(clOther)){
			throw new PrerequisitesNotMetException("invalid class", cl, clOther);
		}
	}

	public String getTabAsSpaces() {
		return "  ";
	}

	private String	strValidCmdCharsRegex = "a-zA-Z0-9_"; // better not allow "-" as has other uses like negate number and commands functionalities
	public boolean isValidIdentifierCmdVarAliasFuncString(String strCmdPart) {
		if(strCmdPart==null)return false;
		//TODO match as "^["+strValidCmdCharsRegex+"]*$" (whole string)? necessary?
		return strCmdPart.matches("["+strValidCmdCharsRegex+"]*");
	}
	
	public String assertGetValidId(String strId, String strDefaultId){
		if(strId==null)strId=strDefaultId;
		if(!isValidIdentifierCmdVarAliasFuncString(strId)){
			throw new PrerequisitesNotMetException("invalid id", strId);
		}
		return strId;
	}
	
	public String getClassTreeReportFor(Object obj,boolean bSimpleName){
		ArrayList<Class<?>> ac = getSuperClassesOf(obj);
		String strClassTree="";
		for(Class<?> cl:ac){
			if(!strClassTree.isEmpty())strClassTree+="/";
			strClassTree+= bSimpleName ? cl.getSimpleName() : cl.getName();
		}
		return strClassTree;
	}
	/**
	 * different from: obj.getClass().getDeclaredClasses()
	 * @param obj
	 * @return
	 */
	public ArrayList<Class<?>> getSuperClassesOf(Object obj){
		ArrayList<Class<?>> ac = new ArrayList<Class<?>>();
		
		Class<?> cl = obj.getClass();
		while(!cl.toString().equals(Object.class.toString())){
			ac.add(cl);
			cl=cl.getSuperclass();
		}
		
		return ac;
	}
	
	public String getClassName(Object obj, boolean bSimple){
		String str =  bSimple ? obj.getClass().getSimpleName() : obj.getClass().getName();
		if(str.isEmpty()){
			throw new PrerequisitesNotMetException(
				"empty class name, do not use anonymous inner class if you want to use their name...",
				obj, getClassTreeReportFor(obj,true), bSimple);
		}
		return str;
	}

	public boolean isInnerClassOfConcrete(Object objInnerToCheck, Object objConcreteOwner){
		return (objInnerToCheck.getClass().getTypeName().startsWith(
			objConcreteOwner.getClass().getTypeName()+"$"));
	}
	
	public boolean isAnonymousClass(Object obj){
		/**
		 * TODO could something like this be less guessing?
		obj.getClass().getDeclaredClasses(); //[]
		obj.getClass().getDeclaringClass(); //null
		obj.getClass().getEnclosingClass(); //ex.: CommandsDelegator
		obj.getClass().getGenericSuperclass(); //ex.: CallQueueI$CallableX
		obj.getClass().getTypeName(); //ex.: CommandsDelegator$1
		Modifier.isStatic(obj.getClass().getModifiers()); //false
		Modifier.isTransient(obj.getClass().getModifiers()); //false
		Modifier.isVolatile(obj.getClass().getModifiers()); //false
		 */
		String str = obj.getClass().getTypeName();
		return (str.matches("^.*[$][0-9]*$"));
	}
	
	public String removeQuotes(String str) {
		str=str.trim();
		if(str.startsWith("\"") && str.endsWith("\"")){
			str=str.substring(1,str.length()-1);
		}
		return str;
	}
	
	/**
	 * 
	 * @param clReturnType
	 * @param aobj params are expected to be NOT null
	 * @param iIndex
	 * @return null if invalid class type to cast
	 */
	public <T> T getParamFromArray(Class<T> clReturnType, Object[] aobj, int iIndex){
		Object obj = aobj[iIndex];
		if(clReturnType.isInstance(obj))return (T)obj;
		return null;
	}
	public <BFR> BFR bugFixRet(Class<BFR> clReturnType, boolean bFixed,	Object objRet, Object[] aobjCustomParams) {
		if(!bFixed){
			throw new PrerequisitesNotMetException("cant bugfix this way...",aobjCustomParams);
		}
		
		return (BFR)objRet;
	}
	
	public <T> T assertCurrentIsNull(T currentValue, T newValue){
		PrerequisitesNotMetException.assertNotAlreadySet("Field", currentValue, newValue);
		return newValue;
	}
	
	HashMap<Class,Object> hmPrimitivesDefaultValues;
	/**
	 * Use on constructors to prevent initializations in the class body.
	 * Static fields will be ignored.
	 * @param irfsa
	 */
	public void assertFieldsHaveDefaultValue(IReflexFieldSafeAccess irfsa){
		if(!DebugI.i().isInIDEdebugMode()){
			return; //spend this time only in development mode
		}
		
		if(hmPrimitivesDefaultValues==null){
			hmPrimitivesDefaultValues = new HashMap<Class, Object>();
			hmPrimitivesDefaultValues.put(boolean.class, new Boolean(false));
			hmPrimitivesDefaultValues.put(byte.class, new Byte((byte)0));
			hmPrimitivesDefaultValues.put(char.class, new Character((char) 0));
			hmPrimitivesDefaultValues.put(short.class, new Short((short) 0));
			hmPrimitivesDefaultValues.put(int.class, new Integer(0));
			hmPrimitivesDefaultValues.put(long.class, new Long(0));
			hmPrimitivesDefaultValues.put(float.class, new Float(0));
			hmPrimitivesDefaultValues.put(double.class, new Double(0));
		}
		
		ArrayList<Class<?>> superClassesOf = MiscI.i().getSuperClassesOf(irfsa);
		for(Class cl:superClassesOf){
			@SuppressWarnings("unused") int iDbgBreakPoint=0;
			for(Field fld:cl.getDeclaredFields()){
				if(Modifier.isStatic(fld.getModifiers()))continue;
				
				try {
					Object objValue = irfsa.getFieldValue(fld);
					
					boolean bIsDefault=false;
					if(fld.getType().isPrimitive()){
						for(Entry<Class, Object> entry:hmPrimitivesDefaultValues.entrySet()){
							Class clPrimitive = entry.getKey();
							if(fld.getType().isAssignableFrom(clPrimitive)){
								if(objValue.equals(entry.getValue()))bIsDefault=true;
								break;
							}
						}
					}else{
						if(objValue==null)bIsDefault=true;
					}
//					if(fld.getType().isAssignableFrom(Boolean.class)){
//						if(((Boolean)objValue)!=Boolean.class.newInstance())bIsDefault=false;
//					}else
//					{} //just for easy coding
					
					if(!bIsDefault){
						throw new PrerequisitesNotMetException(
							"not default"+(fld.getType().isPrimitive() ? " (primitive)" : ""),
							irfsa, cl, fld, objValue);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new PrerequisitesNotMetException("check for default values failed", irfsa, cl, fld)
						.initCauseAndReturnSelf(e);
				}
			}
		}
	}
	
	public <T extends Number> T assertValidFloating(T n) throws IllegalArgumentException{
		if(n==null)throw new IllegalArgumentException("null invalid value");
		
		if (n instanceof Float) {
			Float v = (Float) n;
			if(v.isNaN() || v.isInfinite()){
				throw new IllegalArgumentException("invalid value");
			}
		}else
		if (n instanceof Double) {
			Double v = (Double) n;
			if(v.isNaN() || v.isInfinite()){
				throw new IllegalArgumentException("invalid value");
			}
		}else{
			throw new UnsupportedOperationException(""+n.getClass()+","+n);
		}
		
		return n;
	}
	
	public <T> T assertNotNull(Class<T> cl, T value) throws InvalidAttributeValueException{
		if(value==null)throw new InvalidAttributeValueException();
		return value;
	}
	
	public boolean stackTraceContainsClass(StackTraceElement[] aste,Class cl) {
		for(StackTraceElement ste:aste){
			if(ste.getClassName().equals(cl.getName()))return true;
		}
		return false;
	}

	public String getValidCmdCharsRegex() {
		return strValidCmdCharsRegex;
	}
	
	public <T extends IUniqueId> T findByUniqueId(ArrayList<T> aList, String strId){
		return findByUniqueId(aList, strId, true,true);
	}
	public <T extends IUniqueId> T findByUniqueId(ArrayList<T> aList, String strId, boolean bIgnoreCase, boolean bTrim){
		if(bTrim){
			strId=strId.trim();
		}		
		
		for(T objWithUId:aList){
			String strObjId = objWithUId.getUniqueId();
			if(bTrim){
				strObjId=strObjId.trim();
			}
			
			if(bIgnoreCase){
				if(strObjId.equalsIgnoreCase(strId))return objWithUId;
			}else{
				if(strObjId.equals(strId))return objWithUId;
//				if(objWithUId.isUniqueId(strId))return objWithUId;
			}
		}
		
		return null;
	}

	public String prepareUniqueId(ISingleInstance si) {
		return si.getClass().getName(); //TODO dots to underscores?
	}
}
