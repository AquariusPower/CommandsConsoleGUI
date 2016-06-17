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
	}
	
	public static class ReflexFillCfg{
		/**
		 * to validate and also be removed from the identifier string
		 */
		String	strCodingStyleFieldNamePrefix=null;
		String	strPrefix="";
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

		public String getPrefix() {
			return strPrefix;
		}

		public void setPrefix(String strCommandPrefix) {
			this.strPrefix = strCommandPrefix;
		}

		public String getSuffix() {
			return strSuffix;
		}

		public void setSuffix(String strCommandSuffix) {
			this.strSuffix = strCommandSuffix;
		}

		public boolean isFirstLetterUpperCase() {
			return bFirstLetterUpperCase;
		}

		public void setFirstLetterUpperCase(boolean bFirstLetterUpperCase) {
			this.bFirstLetterUpperCase = bFirstLetterUpperCase;
		}

		public ReflexFillCfg() {
			super();
		}
		
		public ReflexFillCfg(ReflexFillCfg other) {
			super();
			this.strCodingStyleFieldNamePrefix = other.strCodingStyleFieldNamePrefix;
			this.strPrefix = other.strPrefix;
			this.strSuffix = other.strSuffix;
			this.bFirstLetterUpperCase = other.bFirstLetterUpperCase;
			this.bIsCommandToo = other.bIsCommandToo;
		}

		public void setAsCommandToo(boolean b) {
			this.bIsCommandToo = b;
		}
		
		public boolean isCommandToo() {
			return this.bIsCommandToo;
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
	 * @return
	 */
	public String createIdentifierWithFieldName(IReflexFillCfg rfcfgOwnerOfField, IReflexFillCfgVariant rfcvFieldAtTheOwner){
		ReflexFillCfg rfcfg = rfcfgOwnerOfField.getReflexFillCfg(rfcvFieldAtTheOwner);
		if(rfcfg==null){
			if(bUseDefaultCfgIfMissing){
				rfcfg = new ReflexFillCfg();
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
		
		String strCommand = strFieldName;
		if(strCodeTypePrefix==null || strFieldName.startsWith(strCodeTypePrefix)){
			if(strCodeTypePrefix!=null){
				//remove prefix
				strCommand=strCommand.substring(strCodeTypePrefix.length());
			}
			
			if(bMakePretty){
				strCommand=MiscI.i().makePretty(strCommand, rfcfg.bFirstLetterUpperCase);
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
				strCommand=MiscI.i().firstLetter(strCommand,rfcfg.bFirstLetterUpperCase);
			}
		}
		strCommand=rfcfg.strPrefix+strCommand+rfcfg.strSuffix;
		return strCommand;
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
	 * @param rfcfgOwningValue
	 * @param strCodePrefixVariant
	 * @param irfcvValue
	 * @param iOnlyFirstLettersCount see {@link MiscI#makePretty(Class, boolean, Integer)}
	 * @return
	 */
	public String getVarId(IReflexFillCfg rfcfgOwningValue, String strCodePrefixVariant, IReflexFillCfgVariant irfcvValue, Integer iOnlyFirstLettersCount) {
		if(rfcfgOwningValue==null){
			throw new NullPointerException("Invalid usage, "
				+IReflexFillCfg.class.getName()+" owner is null, is this a local (non field) variable?");
		}
		
//		Field field = assertAndGetField(rfcfgOwningValue,irfcvValue);
//		Class<?> clWhereFieldIsActuallyDeclared = field.getDeclaringClass();
		
		String strVarId = strCodePrefixVariant
//			+rfcfgOwner.getClass().getSimpleName()
//			+clWhereFieldIsActuallyDeclared.getSimpleName()
				+getDeclaringClass(rfcfgOwningValue,irfcvValue).getSimpleName()
//			+MiscI.i().makePretty(rfcfgOwner.getClass(), false, iOnlyFirstLettersCount)
			+MiscI.i().firstLetter(
				ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwningValue,irfcvValue),
				true);
		
		return strVarId;
	}
	
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
}
