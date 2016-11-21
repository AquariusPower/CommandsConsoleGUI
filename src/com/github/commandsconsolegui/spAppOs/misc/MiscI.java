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

package com.github.commandsconsolegui.spAppOs.misc;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.naming.directory.InvalidAttributeValueException;

import com.github.commandsconsolegui.spCmd.CommandData;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MiscI {
	private static MiscI instance = new MiscI();
	public static MiscI i(){return instance;}
	
//	public long lLastUniqueId = 0;
	private IHandleExceptions	ihe = SipmleHandleExceptionsI.i();
	private String	strLastUid = "0";
	private boolean	bConfigured;
//	private SimpleApplication	sapp;
	
	public void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		
		if(ihe==null)throw new NullPointerException("invalid instance for "+IHandleExceptions.class.getName());
		this.ihe=ihe;
		
		configureDefaultPrimitivesValues();
		
//		this.sapp=sapp;
		bConfigured=true;
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
	
	synchronized public void fileAppendLineTS(File fl, String str) {
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
		int iRadix=Character.MAX_RADIX;
		/**
		 * Do not fix if null like in `if(strLastId==null)strLastId="0";`
		 * because the last id must be controlled by a manager String field,
		 * or be a static String field of the class...
		 * 
		 * fixing the null would just be prone to developer coding bugs...
		 * 
		 * TODO better not fix if empty either!?
		 */
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
		
//		 allow multiline check
//		strToCheck=strToCheck.replace("\n"," "); //space to prevent joining words
		
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
				/**
				 * about "(?s)" see the javadoc of Pattern.DOTALL, 
				 * it will let dots match '\n' so if the string is a multiline, it will work!
				 */
				return strToCheck.matches("(?s)"+strMatch); 
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
	
	
	
	/**
	 * TODO add enclosings
	 * @param obj
	 * @param bSimpleName
	 * @return
	 */
	public String getClassTreeReportFor(Object obj,boolean bSimpleName){
		ArrayList<Class<?>> ac = getSuperClassesOf(obj,true);
		String strClassTree="";
		for(Class<?> cl:ac){
			if(!strClassTree.isEmpty())strClassTree+="/";
			strClassTree+= bSimpleName ? cl.getSimpleName() : cl.getName();
		}
		return strClassTree;
	}
	/**
	 * Differs from: obj.getClass().getDeclaredClasses()
	 * 
	 * Will include the concrete/instanced one too.
	 * 
	 * @param obj
	 * @return
	 */
	public ArrayList<Class<?>> getSuperClassesOf(Object obj,boolean bAddConcreteToo){
		ArrayList<Class<?>> ac = new ArrayList<Class<?>>();
		
		Class<?> cl = obj.getClass();
		while(cl!=null){
			boolean bAdd=true;
			
			if(!bAddConcreteToo && cl.toString().equals(obj.getClass().toString()))bAdd=false;
			if(cl.toString().equals(Object.class.toString()))bAdd=false; //avoid unnecessary base class
			
			if(bAdd)ac.add(cl);
			
			cl=cl.getSuperclass();
		}
		
		return ac;
	}
	
	public ArrayList<Class<?>> getEnclosingClassesOf(Object obj){
		ArrayList<Class<?>> ac = new ArrayList<Class<?>>();
		
		Class cl = obj.getClass().getEnclosingClass();
		while(cl!=null){
			ac.add(cl);
			cl = cl.getEnclosingClass();
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
	
	public Object parseToPrimitivesWithPriority(String strValue) throws NumberFormatException{
		// 1st as double would parse a long value
		try{return Long  .parseLong      (strValue);}catch(NumberFormatException e){}// accepted exception!
		// 2nd 
		try{return Double.parseDouble    (strValue);}catch(NumberFormatException e){}// accepted exception!
		// 3rd before String as some strings are accepted as boolean value
		try{return MiscI.i().parseBoolean(strValue);}catch(NumberFormatException e){}// accepted exception!
		// default and last one
		return strValue;
	}
	
	public Boolean parseBoolean(String strValue) throws NumberFormatException{
		if(strValue.equalsIgnoreCase("true"	))return new Boolean(true);
		if(strValue.equalsIgnoreCase("1"		))return new Boolean(true);
		if(strValue.equalsIgnoreCase("false"))return new Boolean(false);
		if(strValue.equalsIgnoreCase("0"		))return new Boolean(false);
		throw new NumberFormatException("invalid boolean value: "+strValue);
	}
	
	public boolean isPrimitive(Object objRawValue){
		if(objRawValue instanceof String){}else //as it is passed by value
			
		if(objRawValue instanceof Byte){}else
		if(objRawValue instanceof Character){}else
			
		if(objRawValue instanceof Short){}else
		if(objRawValue instanceof Integer){}else
		if(objRawValue instanceof Long){}else
			
		if(objRawValue instanceof Float){}else
		if(objRawValue instanceof Double){}else
			
		if(objRawValue instanceof Boolean){}else
		
		{
			return false;
		}
		
		return true;
	}
	
	public boolean isPrimitiveDefaultValue(Object objValue){
		Object objDefaultValue = hmPrimitivesDefaultValues.get(objValue.getClass());
		
		if(objDefaultValue==null){
			throw new PrerequisitesNotMetException("not a primitive",objValue.getClass(),objValue);
		}
		
		if(objValue.equals(objDefaultValue))return true;
		
		return false;
	}
	private void configureDefaultPrimitivesValues() {
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
	
	public String asReport(String strid,Object objValue, boolean bSingle){
		String str="";
		
		if(!bSingle)str+="\t";
		
		str += strid+"("+asReport(objValue)+")";
		
		if(!bSingle)str+=",\n";
		
		return str;
	}
	public String asReportLine(Object objKey,Object obj,boolean bSingle){
		String str="";
		
		if(!bSingle)str+="\t";
		
		if(bSingle){
			str+=obj.getClass().getName();
		}else{
			str+=obj.getClass().getSimpleName();
		}
		
		if(objKey!=null){
			str+="["+objKey+"]";
		}
		
		str+="='"+obj+"'";
		
		if(!bSingle)str+=",\n";
		
		return str;
	}
	public String asReport(Object objValue){
		String str="(";
		
		if(objValue==null){
			str+=""+null;
		}else{
			if(objValue instanceof IDebugReport){
				return ((IDebugReport)objValue).getFailSafeDebugReport();
			}
			
			Object[] aobjKey=null;
			Object[] aobjVal=null;
			if(objValue instanceof Map) { //HashMap TreeMap etc
				Set<Map.Entry> es = ((Map)objValue).entrySet();
				aobjVal=new Object[es.size()];
				aobjKey=new Object[es.size()];
				int i=0;
				for(Entry entry:es){
					aobjKey[i]=entry.getKey();
					aobjVal[i]=entry.getValue();
					i++;
				}
			}else
			if(objValue instanceof ArrayList) {
				ArrayList<?> aobjList = (ArrayList<?>) objValue;
				aobjVal=aobjList.toArray();
			}else
			if(objValue.getClass().isArray()){
				aobjVal = (Object[])objValue;
			}
			
			if(aobjVal!=null){ //array
				// this is just guessing all elements are of the same type
				str+=""+objValue.getClass().getName()+"["+aobjVal.length+"]{"; //TODO check if all elements are same type? 
				for(int i=0;i<aobjVal.length;i++){//Object obj:aobjVal){
					Object objKey=null;if(aobjKey!=null)objKey=aobjKey[i];
					str+=asReportLine(objKey,aobjVal[i],false);
				}
				str+="}";
			}else{
				str+=asReportLine(null,objValue,true);
			}
		}
		
		return str+")";
	}
	
	/**
	 * TODO mix this with {@link #asReport(Object)}
	 * @param bPrependCurrentTime
	 * @param strMessage
	 * @param aobjCustom
	 * @return
	 */
	public String joinMessageWithObjects(boolean bPrependCurrentTime, String strMessage, Object... aobjCustom){
		String strTime=new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
		if(bPrependCurrentTime){
			strMessage=strTime+strMessage;
		}
		return joinMessageWithObjects(strMessage,aobjCustom);
	}
	/**
	 * TODO mix this with {@link #asReport(Object)}
	 * @param strMessage
	 * @param aobjCustom
	 * @return
	 */
	public String joinMessageWithObjects(String strMessage, Object... aobjCustom){
		if(aobjCustom!=null){
			for(int i=0;i<aobjCustom.length;i++){
				Object obj = aobjCustom[i];
				strMessage+=multilineIfArray("\n\t["+i+"]",obj);
			}
		}
		
		return strMessage;
	}
	/**
	 * TODO mix this with {@link #asReport(Object)}
	 * @param strIndexPrefix
	 * @param obj
	 * @return
	 */
	private String multilineIfArray(String strIndexPrefix, Object obj){
		String strOut="";
		
		Object[] aobj = null; //obj instanceof Object[]
		Exception ex = null;
		if(obj instanceof Exception){
			ex = ((Exception)obj);
		}
		
		if(obj!=null){
			if (obj instanceof ArrayList) {
				ArrayList aobjList = (ArrayList) obj;
				aobj=aobjList.toArray();
			}else
			if(obj.getClass().isArray()){
				aobj = (Object[])obj;
	//			for(Object objInner:aobj){
			}
		}
		
		if(ex!=null){
			strOut+=strIndexPrefix+" "+ex.getClass().getSimpleName()+": "+ex.getMessage();
			aobj = ex.getStackTrace();
		}else
		if(aobj==null){
			return strIndexPrefix+fmtObj(obj,true);
		}
		
		String str1stObjClass=null;
		for(int i=0;i<aobj.length;i++){
			Object objInner=aobj[i]; //objInner.getClass().getName()
			
			String strFmtObj=null;
			if(objInner==null){
				strFmtObj = fmtObj(objInner,false);
			}else{
				if(str1stObjClass==null){
					str1stObjClass=objInner.getClass().getName();
					strOut+=strIndexPrefix+"ArrayOf: "+aobj.getClass().getTypeName();
//					strOut+=strIndexPrefix+"ArrayOf?(1stNotNullObjType):"+str1stObjClass;
				}
				
				strFmtObj = fmtObj(objInner, !str1stObjClass.equals(objInner.getClass().getName()));
			}
			
			strOut+=strIndexPrefix+"["+i+"] "+strFmtObj;
		}
		
		if(ex!=null){
			Throwable exCause = ex.getCause();
			if(exCause!=null){
				strOut+=multilineIfArray(strIndexPrefix+"[CAUSE]", exCause);
			}
		}
		
		return strOut;
	}
	/**
	 * TODO mix this with {@link #asReport(Object)}
	 * @param obj
	 * @param bShowClassName
	 * @return
	 */
	private String fmtObj(Object obj,boolean bShowClassName){
		String strCl = "";
		if(obj!=null && bShowClassName)strCl=obj.getClass().getName();
		
		String strObj = "";
		if(strObj.isEmpty()){
			if(obj instanceof IConstructed){
				IConstructed ic = (IConstructed) obj;
				if(!ic.isConstructed()){
					strObj="(not constructed yet)";
				}
			}
		}
		
		if(strObj.isEmpty()){
			if(obj instanceof IDebugReport){
				IDebugReport ir = (IDebugReport) obj;
				strObj=ir.getFailSafeDebugReport();
			}
		}
		
//		if(strObj.isEmpty()){
//			strObj=""+(obj==null?null:obj.toString());
//		}
		
		return ""+(obj==null ? null : strCl+": "+strObj); //this is better when dumping a sub-stacktrace
	}
	
	
	HashMap<Class,Object> hmPrimitivesDefaultValues;
	/**
	 * Use on constructors to prevent initializations in the class body.
	 * Static fields will be ignored.
	 * @param irfsa
	 */
	public void assertFieldsHaveDefaultValue(IReflexFieldSafeAccess irfsa){
		if(!RunMode.bDebugIDE){
			return; //spend this time only in development mode
		}
		
		ArrayList<Class<?>> superClassesOf = MiscI.i().getSuperClassesOf(irfsa,true);
		for(Class cl:superClassesOf){
			@SuppressWarnings("unused") int iDbgBreakPoint=0;
			for(Field fld:cl.getDeclaredFields()){
				if(Modifier.isStatic(fld.getModifiers()))continue;
				
				try {
					Object objValue = irfsa.getFieldValue(fld);
					
					boolean bIsDefault=false;
					if(fld.getType().isPrimitive()){
						bIsDefault=isPrimitiveDefaultValue(objValue);
//						for(Entry<Class, Object> entry:hmPrimitivesDefaultValues.entrySet()){
//							Class clPrimitive = entry.getKey();
//							if(fld.getType().isAssignableFrom(clPrimitive)){
//								if(objValue.equals(entry.getValue()))bIsDefault=true;
//								break;
//							}
//						}
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

	public String prepareUniqueId(IUniqueId si) {
		return si.getClass().getName(); //TODO dots to underscores? could shadow existing underscores tho in case of backwards conversion...
	}
	
	public boolean isGetThisTrickImplementation(Object obj){
		if(!ISimpleGetThisTrickIndicator.class.isInstance(obj)) return false;
			
		ISimpleGetThisTrickIndicator gtt = (ISimpleGetThisTrickIndicator)obj;
		
		String strErr=null;
		
		if(strErr==null && !Modifier.isFinal(gtt.getClass().getModifiers()))strErr="not final class";
		
//		Method[] adm = gtt.getClass().getDeclaredMethods();
//		if(strErr==null && adm.length!=1)strErr="more than one method present";
		
//		if(strErr==null && !adm[0].getName().equals("getThis"))strErr="single method is not the getThis() method";
		
		if(strErr!=null)throw new PrerequisitesNotMetException("invalid simple implementation related to getThis() trick", strErr, gtt);
		
		return true;
	}

	public boolean isRecursiveLoopOnMethod(String strMethodName, Class clOwner){
		StackTraceElement[] aste = Thread.currentThread().getStackTrace();
		ArrayList<StackTraceElement> asteList = new ArrayList<StackTraceElement>(Arrays.asList(aste));
		int iCountConstructor=0;
		int iCountGap=0;
		for(StackTraceElement ste:asteList){
			if(ste.getMethodName().equals(strMethodName) && ste.getClassName().equals(clOwner.getName())){
				iCountConstructor++;
				if(iCountGap>0){ // a gap between this previous and current exception
					return true;
				}
			}else{
				if(iCountConstructor>0){
					iCountGap++;
				}
			}
		}
		
		return false;
	}
	
	public String getEnclosingIfAnonymous(String strClassType){
		int iAnon=strClassType.indexOf("$");
		if(iAnon!=-1)strClassType=strClassType.substring(0,iAnon);
		return strClassType;
	}
}
