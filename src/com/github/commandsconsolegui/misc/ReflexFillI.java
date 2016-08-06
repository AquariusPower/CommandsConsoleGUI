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

import java.lang.reflect.Field;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ReflexFillI{ //implements IConsoleCommandListener{
	private static ReflexFillI instance = new ReflexFillI();
	public static ReflexFillI i(){return instance;}
//	private static IHandleExceptions	ihe;
	
	private boolean bUseDefaultCfgIfMissing=false;
	private String	strCommandPartSeparator = "_";
	
	
	
	/**
	 * the owner class will have the configurations for each
	 * field class type.
	 *
	 */
	public static interface IReflexFillCfg{
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv);
	}
	
	/**
	 * for the same owner class, there may have user preferred prefix/suffix
	 * for each class type implementing this interface.
	 */
	public static interface IReflexFillCfgVariant{
//		public int getReflexFillCfgVariant();
		public String getCodePrefixVariant();
		public IReflexFillCfg getOwner();
		public boolean isReflexing();
		public String getVariablePrefix();
	}
	
	public static class ReflexFillCfg{
		/**
		 * these shall not be copied/cloned
		 */
		IReflexFillCfgVariant rfcv;
		private boolean bUsePrefixDeclaringClass=true;
		String	strPrefixDeclaringClass="";
		boolean bUsePrefixInstancedClass=true;
		String	strPrefixInstancedClass="";
		
		/**
		 * to validate and also be removed from the identifier string
		 * these can be copied/cloned
		 */
		String	strCodingStyleFieldNamePrefix=null;
		String	strPrefixCmd="cmd";
		String	strPrefixVar="var";
		String	strPrefixCustomId="";
		String	strSuffix="";
		boolean bFirstLetterUpperCase = false;
		boolean	bIsCommandToo = false;
		
		public String getCodingStyleFieldNamePrefix() {
			return strCodingStyleFieldNamePrefix;
		}

		public void setCodingStyleFieldNamePrefix(
				String strCodingStyleFieldNamePrefix) {
			this.strCodingStyleFieldNamePrefix = strCodingStyleFieldNamePrefix;
		}

		public String getPrefixVar() {
			return strPrefixVar;
		}
		public void setPrefixVar(String strVarPrefix) {
			this.strPrefixVar = strVarPrefix;
		}
		public void appendPrefixVar(String str) {
			this.strPrefixVar+=str;
		}
		
		public String getPrefixCmd() {
			return strPrefixCmd;
		}
		public void setPrefixCmd(String strCommandPrefix) {
			this.strPrefixCmd = strCommandPrefix;
		}
		public void appendPrefixCmd(String str) {
			this.strPrefixCmd+=str;
		}

		public String getSuffix() {
			return strSuffix;
		}

		public void setSuffix(String strCommandSuffix) {
			if(strCommandSuffix==null)throw new PrerequisitesNotMetException("cant be null");
			this.strSuffix = strCommandSuffix;
		}

		public boolean isFirstLetterUpperCase() {
			return bFirstLetterUpperCase;
		}

		public void setFirstLetterUpperCase(boolean bFirstLetterUpperCase) {
			this.bFirstLetterUpperCase = bFirstLetterUpperCase;
		}

		public ReflexFillCfg(IReflexFillCfgVariant rfcv) {
			super();
			this.rfcv = rfcv;
		}
		
		public ReflexFillCfg(ReflexFillCfg otherCopyBasicDataFrom, IReflexFillCfgVariant rfcv) {
			this(rfcv);
			
			this.strCodingStyleFieldNamePrefix = otherCopyBasicDataFrom.strCodingStyleFieldNamePrefix;
			this.strPrefixCmd = otherCopyBasicDataFrom.strPrefixCmd;
			this.strPrefixVar = otherCopyBasicDataFrom.strPrefixVar;
			this.strSuffix = otherCopyBasicDataFrom.strSuffix;
			this.bFirstLetterUpperCase = otherCopyBasicDataFrom.bFirstLetterUpperCase;
			this.bIsCommandToo = otherCopyBasicDataFrom.bIsCommandToo;
		}

		public void setAsCommandToo(boolean b) {
			this.bIsCommandToo = b;
		}
		
		public boolean isCommandToo() {
			return this.bIsCommandToo;
		}

		public String getPrefixDeclaringClass() {
			return strPrefixDeclaringClass;
		}

		public void setPrefixDeclaringClass(String strPrefixDeclaringClass) {
			this.strPrefixDeclaringClass = strPrefixDeclaringClass;
		}

		public String getPrefixInstancedClass() {
			return strPrefixInstancedClass;
		}

		public void setPrefixInstancedClass(String strPrefixInstancedClass) {
			this.strPrefixInstancedClass = strPrefixInstancedClass;
		}

		public String getPrefixCustomId() {
			return strPrefixCustomId;
		}

		public void setPrefixCustomId(String strPrefixCustomId) {
			if(strPrefixCustomId==null)throw new PrerequisitesNotMetException("cant be null");
			this.strPrefixCustomId = strPrefixCustomId;
		}

		public boolean isUsePrefixInstancedClass() {
			return bUsePrefixInstancedClass;
		}
		
		public void setUsePrefixInstancedClass(boolean bUsePrefixInstancedClass) {
			this.bUsePrefixInstancedClass = bUsePrefixInstancedClass;
		}

		public boolean isUsePrefixDeclaringClass() {
			return bUsePrefixDeclaringClass;
		}

		public void setUsePrefixDeclaringClass(boolean bUsePrefixDeclaringClass) {
			this.bUsePrefixDeclaringClass = bUsePrefixDeclaringClass;
		}
		
		
	}

	private static boolean	bAllowHK = true;
	
//	public static void assertReflexFillFields(IReflexFillCfg owner){
//		assertAndGetField(owner);
//	}
	
	/**
	 * Use this at the class constructor that instantiates fields of classes 
	 * that implements {@link IReflexFillCfgVariant#}, like {@link BoolToggler#} 
	 * and {@link StringField#}.
	 * 
	 * @param objClassOwningTheFields set simply to 'this' at the constructor.
	 */
	public void assertReflexFillFieldsForOwner(IReflexFillCfg objClassOwningTheFields){
		assertAndGetField(objClassOwningTheFields, null);
	}
	
	/**
	 * Cannot be used at field constructor because that object is not ready yet 
	 * and so its class owner does not have yet such field set to 'this'... 
	 * self is still null at constructor.
	 * 
	 * @param objClassOwningField
	 * @param objFieldValue if null, will validate if fields of type {@link IReflexFillCfgVariant#} are owned by the specified owner
	 * @return
	 */
	public Field assertAndGetField(Object objClassOwningField, Object objFieldValue){
//		Class<?> clFound = null;
		Field fldFound = null;
		Class<?> cl = objClassOwningField.getClass();
		String strExceptionLog="Field object not found at: ";
		while(true){
			strExceptionLog+=cl.getName();
			if(cl.getName().equals(Object.class.getName())){
				strExceptionLog+="";
				break;
			}else{
				strExceptionLog+=" <= ";
			}
			
			for(Field fld:cl.getDeclaredFields()){
				try{
					boolean bWasAccessible = fld.isAccessible();
					if(!bWasAccessible)fld.setAccessible(true);
					
					Object objExistingFieldValue = fld.get(objClassOwningField);
					if(objFieldValue!=null){
						if(objExistingFieldValue==objFieldValue)fldFound=fld; //clFound=cl;
					}else{
						/**
						 * validating all fields if parent is configured properly
						 */
						if(objExistingFieldValue instanceof IReflexFillCfgVariant){
							IReflexFillCfgVariant rfcv = (IReflexFillCfgVariant)objExistingFieldValue;
							if(rfcv.isReflexing()){
								IReflexFillCfg configuredOwner = rfcv.getOwner();
								if(configuredOwner != objClassOwningField){
									throwExceptionAboutMissConfiguration(cl, fld, configuredOwner, objClassOwningField);
								} 
							}
						}
					}
					
					if(!bWasAccessible)fld.setAccessible(false);
					if(fldFound!=null)return fldFound;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			cl=cl.getSuperclass();
		}
		
		if(objFieldValue!=null){
			throw new NullPointerException("Failed to automatically set command id. "
				+"Was "+objFieldValue.getClass()+" object owner properly set to the class where it is instantiated? "
				+strExceptionLog);
		}
		
		return null;
//		throw new NullPointerException("class "+cl.getName()+" doesnt owns field "+objFieldValue.getClass());
	}
	
	private void throwExceptionAboutMissConfiguration(Class<?> cl, Field fld, IReflexFillCfg configuredOwner, Object objClassOwningField){
		throw new PrerequisitesNotMetException(
			"The field "+cl.getName()+"."+fld.getName() + "("+cl.getSimpleName()+".java:0) "
			+" was configured with an invalid owner "
			+"'"+(configuredOwner!=null?configuredOwner.getClass().getName():"null")+"', "
			
			+"at an instance of "+objClassOwningField.getClass().getName()
				+"("+objClassOwningField.getClass().getSimpleName()+".java:0)" +". "
			
			+"Fix that field instance of "+fld.getType().getName()
				+"("+fld.getType().getSimpleName()+".java:0) "
				+"by setting "
				+"it's owner to 'this' at the configuration parameter type: "
			
			+IReflexFillCfg.class.getName()
				+"("+IReflexFillCfg.class.getSimpleName()+".java:0) "
				
			+" ");
	}
	
	public static class IdTmp{
		public String strUniqueCmdId;
		public String strSimpleCmdId;
		public boolean	bIsVariable;
		public IdTmp() {
		}
		public IdTmp(boolean bIsVariable, String strCore, String strFull) {
			this.strUniqueCmdId = strFull;
			this.strSimpleCmdId = strCore;
			this.bIsVariable = bIsVariable;
		}
	}
	
	/**
	 * Works on reflected variable name.
	 * If it is all uppercase, it will be prettyfied.
	 * 
	 * IMPORTANT:
	 * This cannot be used at constructors because it depends on the field value being set.
	 * At the constructor, it has not returned yet, therefore the class field is still null!!!
	 * 
	 * @param rfcfgOwnerOfField
	 * @param rfcvFieldAtTheOwner
	 * @param bIsVariable otherwise is a command
	 * @return
	 */
	public IdTmp createIdentifierWithFieldName(IReflexFillCfg rfcfgOwnerOfField, IReflexFillCfgVariant rfcvFieldAtTheOwner, boolean bIsVariable){
		if(rfcfgOwnerOfField==null){
			throw new NullPointerException("Invalid usage, "
				+IReflexFillCfg.class.getName()+" owner is null, is this a local (non field) variable?");
		}
		
		ReflexFillCfg rfcfg = rfcfgOwnerOfField.getReflexFillCfg(rfcvFieldAtTheOwner);
		if(rfcfg==null){
			if(bUseDefaultCfgIfMissing){
				rfcfg = new ReflexFillCfg(rfcvFieldAtTheOwner);
			}else{
				throw new NullPointerException("Configuration is missing for "
					+rfcfgOwnerOfField.getClass().getName()
					+" -> "
					+rfcvFieldAtTheOwner.getClass().getName()
					+":"
					+rfcvFieldAtTheOwner.getCodePrefixVariant());
			}
		}
		
		Field fld = assertAndGetField(rfcfgOwnerOfField, rfcvFieldAtTheOwner);
		Class<?> cl = fld.getDeclaringClass();
		
		String strFieldName=fld.getName();
		
		boolean bMakePretty=!strFieldName.matches(".*[a-z].*");
		
		String strCodeTypePrefix = rfcfg.strCodingStyleFieldNamePrefix;
		if(strCodeTypePrefix==null){
			strCodeTypePrefix=rfcvFieldAtTheOwner.getCodePrefixVariant();
		}
		
		String strCommandCore = strFieldName;
		if(strCodeTypePrefix==null || strFieldName.startsWith(strCodeTypePrefix)){
			if(strCodeTypePrefix!=null){
				//remove prefix
				strCommandCore=strCommandCore.substring(strCodeTypePrefix.length());
			}
			
			if(bMakePretty){
				strCommandCore=MiscI.i().makePretty(strCommandCore, rfcfg.bFirstLetterUpperCase);
//				/**
//				 * upper case with underscores
//				 */
//				String strCmdNew = null;
//				for(String strWord : strCommand.split("_")){
//					if(strCmdNew==null){
//						if(rfcfg.bFirstLetterUpperCase){
//							strCmdNew=firstLetter(strWord.toLowerCase(),true);
//						}else{
//							strCmdNew=strWord.toLowerCase();
//						}
//					}else{
//						strCmdNew+=firstLetter(strWord.toLowerCase(),true);
//					}
//				}
//				strCommand=strCmdNew;
			}else{
				/**
				 * Already nice to read field name.
				 */
				strCommandCore=MiscI.i().firstLetter(strCommandCore,rfcfg.bFirstLetterUpperCase);
			}
		}
		
		IdTmp id = new IdTmp();
		id.strUniqueCmdId = prepareFullCommand(strCommandCore, rfcfg, bIsVariable);
		id.strSimpleCmdId = strCommandCore;
		id.bIsVariable=bIsVariable;
		return id;
	}
	
	/**
	 * All these concatenated identifiers are good to make sure all commands are unique,
	 * but... command's size get huge!
	 * 
	 * Also, there is redundancy cleaner, avoiding identifiers duplicity mess.
	 * 
	 * @param strCommandCore
	 * @param rfcfg
	 * @return
	 */
	private String prepareFullCommand(String strCommandCore, ReflexFillCfg rfcfg, boolean bIsVariable){
//		DebugI.i().conditionalBreakpoint(rfcfg.getPrefixCustomId().equals("ConfigDialog"));
		
	//	strCommand=rfcfg.strPrefix+strCommand+rfcfg.strSuffix;
		String strFullCommand=preparePart(bIsVariable?
			rfcfg.getPrefixVar()+rfcfg.rfcv.getVariablePrefix():
			rfcfg.getPrefixCmd());
		
		boolean bUseCustomId=true;
		
		String strPrefixDeclaring="";
		if(rfcfg.isUsePrefixDeclaringClass()){
			if(rfcfg.getPrefixDeclaringClass().isEmpty()){
				Field field = assertAndGetField(rfcfg.rfcv.getOwner(), rfcfg.rfcv);
				rfcfg.setPrefixDeclaringClass(field.getDeclaringClass().getSimpleName());
			}
			
			strPrefixDeclaring=preparePart(rfcfg.getPrefixDeclaringClass());
			
			if(rfcfg.getPrefixCustomId().equalsIgnoreCase(rfcfg.getPrefixDeclaringClass())){
				bUseCustomId=false;
			}
		}
		
		String strPrefixInstanced="";
		if(rfcfg.isUsePrefixInstancedClass()){
			if(rfcfg.getPrefixInstancedClass().isEmpty()){
				rfcfg.setPrefixInstancedClass(rfcfg.rfcv.getOwner().getClass().getSimpleName());
			}
			
			if(!rfcfg.getPrefixDeclaringClass().equalsIgnoreCase(rfcfg.getPrefixInstancedClass())){
				strPrefixInstanced=preparePart(rfcfg.getPrefixInstancedClass());
				
				if(rfcfg.getPrefixCustomId().equalsIgnoreCase(rfcfg.getPrefixInstancedClass())){
					bUseCustomId=false;
				}
			}
		}
		
		String strPrefixCustomId="";
		if(bUseCustomId)strPrefixCustomId=preparePart(rfcfg.getPrefixCustomId());
		
		/**
		 * this order is good for sorting, from more specific to more generic
		 */
		strFullCommand+=strPrefixCustomId+strPrefixInstanced+strPrefixDeclaring;
				
		strFullCommand+=strCommandCore;
		
		strFullCommand+=preparePart(rfcfg.getSuffix(),true);
		
		return strFullCommand;
	}
	
	private String preparePart(String str){
		return preparePart(str, false);
	}
	private String preparePart(String str, boolean bPrependSeparator){
		if(str==null || str.isEmpty())return "";
		if(bPrependSeparator){
			return strCommandPartSeparator+str;
		}else{
			return str+strCommandPartSeparator;
		}
	}
	
	public boolean isbUseDefaultCfgIfMissing() {
		return bUseDefaultCfgIfMissing;
	}

	public void setUseDefaultCfgIfMissing(boolean bUseDefaultCfgIfMissing) {
		this.bUseDefaultCfgIfMissing = bUseDefaultCfgIfMissing;
	}
	
//	private static IHandleExceptions	ihe;
//	public static void setExceptionHandler(IHandleExceptions ihe){
//		ReflexFill.ihe=ihe;
//	}
	
//	@Override
//	public boolean executePreparedCommand(ConsoleCommands	cc) {
//		boolean bCommandWorked = false;
//		
//		if(cc.checkCmdValidity(this,btgHacks,"[iHowMany] users working")){
//			bAllowHK=cc.paramBoolean(1);
//			bCommandWorked = true;
//		}else
//		{}
//			
//		return bCommandWorked;
//	}
	
	/**
	 * 
	 * @param objClassOwningField
	 * @param objFieldValue
	 * @return the (super) class that contains a field storing the specified value 
	 */
	public Class<?> getDeclaringClass(Object objClassOwningField, Object objFieldValue){
		Field field = assertAndGetField(objClassOwningField,objFieldValue);
		Class<?> clWhereFieldIsActuallyDeclared = field.getDeclaringClass();
		return clWhereFieldIsActuallyDeclared;
	}

	public String getCommandPartSeparator() {
		return strCommandPartSeparator;
	}

	public void setCommandPartSeparator(String strCommandPartSeparator) {
		this.strCommandPartSeparator = strCommandPartSeparator;
		if(this.strCommandPartSeparator==null)throw new PrerequisitesNotMetException("use empty intead of null separator!");
	}
}
