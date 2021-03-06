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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ReflexFillI{ //implements IConsoleCommandListener{
	private static ReflexFillI instance = new ReflexFillI();
	public static ReflexFillI i(){return instance;}
	
//	private final boolean bUseDefaultCfgIfMissing = true; //changing this may cause a lot of unnecesary trouble...
	private String	strCommandPartSeparator = "_";
	
	/**
	 * the owner class will have the configurations for each
	 * field class type.
	 */
	public static interface IReflexFillCfg extends IReflexFieldSafeAccess{
		/**
		 * 
		 * @param rfcvField
		 * @return if null, will use default one.
		 */
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField);
	}
	
	/**
	 * for the same owner class, there may have user preferred prefix/suffix
	 * for each class type implementing this interface.
	 */
	public static interface IReflexFillCfgVariant extends IHasOwnerInstance<IReflexFillCfg>{
		/** used at the specific owner field type coding name prefix style */
		public String getCodePrefixVariant();
		
		/** for the class used as the field instance */
		public String getCodePrefixDefault();
		
		@Override
		public IReflexFillCfg getOwner();
		
		/** TODO remove? use owner!=null */
		public boolean isReflexing();
		
		/** 
		 * for creation of console variables
		 * TODO rename to getConsoleVariablePrefix  
		 */
		public String getVariablePrefix();
		
//		/** TODO remove: collect and test against default */
//		public boolean isCodePrefixVariantEqualDefault();
	}
	
	private String strPrefixCmdDefault="cmd";
	private String strPrefixVarDefault="var";
	
	public String getPrefixCmdDefault() {
		return strPrefixCmdDefault;
	}
	
	public String getPrefixVarDefault() {
		return strPrefixVarDefault;
	}
	
	public static class ReflexFillCfg{
		/**
		 * these shall not be copied/cloned
		 */
		private IReflexFillCfgVariant rfcv;
		private boolean bUsePrefixDeclaringClass=true;
		private Class clDeclaringClass;
//		private String	strPrefixDeclaringClass="";
		private boolean bUsePrefixInstancedClass=true;
//		private String	strPrefixInstancedClass="";
		private Class clConcreteClass;
		private Class clConcreteClassOverride;
		
		/**
		 * to validate and also be removed from the identifier string
		 * these can be copied/cloned
		 */
		private String	strCodingStyleFieldNamePrefix=null;
		private String	strPrefixCmd=ReflexFillI.i().getPrefixCmdDefault();
		private String	strPrefixVar=ReflexFillI.i().getPrefixVarDefault();
		private String	strPrefixCustomId="";
		private String	strSuffix="";
		private boolean bFirstLetterUpperCase = false;
		private boolean	bIsCommandToo = false;
		
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
			this.setRfcv(rfcv);
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
			return clDeclaringClass.getSimpleName();
		}

		public Class getDeclaringClass(){
			return clDeclaringClass;
		}
		
		public void setDeclaringClass(Class clDeclaringClass) {
			this.clDeclaringClass = clDeclaringClass;
		}

		public String getPrefixInstancedConcreteClass() {
			if(clConcreteClassOverride!=null){
				return clConcreteClassOverride.getSimpleName();
			}
			
			return clConcreteClass.getSimpleName();
		}
		
		public void setConcreteClassOverride(Class clConcreteClassOverride) {
			this.clConcreteClassOverride = clConcreteClassOverride;
		}
		
		public Class getInstancedConcreteClass(){
			return clConcreteClass;
		}
		
		public void setInstancedConcreteClass(Class clInstancedClass) {
			this.clConcreteClass = clInstancedClass;
		}

		public String getPrefixCustomId() {
			return strPrefixCustomId;
		}

		public void setPrefixCustomId(String strPrefixCustomId) {
			if(strPrefixCustomId==null)throw new PrerequisitesNotMetException("cant be null");
			this.strPrefixCustomId = strPrefixCustomId;
		}

		public boolean isUsePrefixInstancedConcreteClass() {
			return bUsePrefixInstancedClass;
		}
		
		public void setUsePrefixInstancedConcreteClass(boolean bUsePrefixInstancedClass) {
			this.bUsePrefixInstancedClass = bUsePrefixInstancedClass;
		}

		public boolean isUsePrefixDeclaringClass() {
			return bUsePrefixDeclaringClass;
		}

		public void setUsePrefixDeclaringClass(boolean bUsePrefixDeclaringClass) {
			this.bUsePrefixDeclaringClass = bUsePrefixDeclaringClass;
		}

		public IReflexFillCfgVariant getRfcv() {
			return rfcv;
		}

		public void setRfcv(IReflexFillCfgVariant rfcv) {
			this.rfcv = rfcv;
		}
		
		
	}

	private boolean	bAllowHK = true;
	
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
	 * Static fields will be skipped/ignored.
	 * 
	 * TODO check for more than one field pointing to the same value?
	 * 
	 * @param irfcfgInstanceOwningField only scans (sub)class implementing {@link IReflexFillCfg}
	 * @param objFieldValue if null, will validate if fields of type {@link IReflexFillCfgVariant#} are owned by the specified owner
	 * @return
	 */
//	public Field assertAndGetField(IReflexFillCfg irfcfgInstanceOwningField, IReflexFillCfgVariant irfcfgvFieldValue){
	public Field assertAndGetField(IReflexFillCfg irfcfgInstanceOwningField, Object objFieldValue){
//		Class<?> clFound = null;
		Field fldFound = null;
		Class<?> cl = irfcfgInstanceOwningField.getClass();
		String strExceptionLog="Field object not found at: ";
		String strClassStack="";
		/**
		 * will show the Object class name too
		 */
		labelWhile:while(true){ //!cl.equals(Object.class)){
			strClassStack+=cl.getName(); 
			if(cl.getName().equals(Object.class.getName())){
				strClassStack+="";
				break;
			}else{
				strClassStack+=" <= ";
			}
			
			for(Field fld:cl.getDeclaredFields()){
				if(Modifier.isStatic(fld.getModifiers()))continue;
//					MsgI.i().devInfo("skipping static field", fld);
				
				try{
					Object objExistingFieldValue = irfcfgInstanceOwningField.getFieldValue(fld);
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
								if(configuredOwner != irfcfgInstanceOwningField){
									throwExceptionAboutMissConfiguration(cl, fld, configuredOwner, irfcfgInstanceOwningField);
								} 
							}
						}
					}
					
				} catch (IllegalArgumentException | IllegalAccessException e) {
					if(e instanceof IllegalAccessException){
						if(e.getMessage().contains("private")){
							throw new PrerequisitesNotMetException("did you miss some subclass implementation of "+IReflexFieldSafeAccess.class+"?",
								fld,
								strClassStack
							).setCauseAndReturnSelf(e);
						}
					}
					
					throw new PrerequisitesNotMetException("problem with",fld,strClassStack).setCauseAndReturnSelf(e);
//					e.printStackTrace();
				}
				
				if(fldFound!=null)break labelWhile;
			}
			
			cl=cl.getSuperclass();
			
			if(!IReflexFillCfg.class.isAssignableFrom(cl))break;
		}
		
		if(fldFound!=null){
			/**
			 * Field is at owner instance.
			 */
			return fldFound;
		}
		
		/**
		 * Inconsistency:
		 * field is not at specified owner
		 */
		if(objFieldValue!=null){
//			throw new NullPointerException("Failed to automatically set command id? "
			throw new PrerequisitesNotMetException("Inconsistency found: "
				+"(obs.: Concrete Instanced class is "+irfcfgInstanceOwningField.getClass().getName()+")"
				+"1st check: Was "+objFieldValue.getClass()+"'s owner properly set to the class where it is "
				+"instantiated (using 'this' as its "+IReflexFillCfgVariant.class.getSimpleName()+" parameter)? "
				+"2nd check: Was methods of "+IReflexFieldSafeAccess.class.getSimpleName()+" properly coded in ALL subclasses?"
				+strExceptionLog+", "+strClassStack);
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
	
	
//	/**
//	 * Works on reflected variable name.
//	 * If it is all uppercase, it will be prettyfied.
//	 * 
//	 * IMPORTANT:
//	 * This cannot be used at constructors because it depends on the field value being set.
//	 * At the constructor, it has not returned yet, therefore the class field is still null!!!
//	 * 
//	 * @param rfcfgOwnerOfField
//	 * @param rfcvFieldAtTheOwner
//	 * @param bIsVariable otherwise is a command
//	 * @return
//	 */
//	public VarCmdUId createIdentifierWithFieldName(IReflexFillCfg rfcfgOwnerOfField, IReflexFillCfgVariant rfcvFieldAtTheOwner){//, boolean bIsVariable){
//		if(rfcfgOwnerOfField==null){
//			throw new PrerequisitesNotMetException("Invalid usage, "
//				+IReflexFillCfg.class.getName()+" owner is null, is this a local (non field) variable?");
//		}
//		
//		ReflexFillCfg rfcfg = rfcfgOwnerOfField.getReflexFillCfg(rfcvFieldAtTheOwner);
//		if(rfcfg==null){
////			if(isUseDefaultCfgIfMissing()){
//				rfcfg = new ReflexFillCfg(rfcvFieldAtTheOwner);
////			}else{
////				throw new PrerequisitesNotMetException("Configuration is missing for "
////					+rfcfgOwnerOfField.getClass().getName()
////					+" -> "
////					+rfcvFieldAtTheOwner.getClass().getName()
////					+":"
////					+rfcvFieldAtTheOwner.getCodePrefixVariant());
////			}
//		}
//		
//		VarCmdUId vcid = new VarCmdUId();
//		
//		Field fld = assertAndGetField(rfcfgOwnerOfField, rfcvFieldAtTheOwner);
//		Class<?> cl = fld.getDeclaringClass();
//		
//		String strFieldName=fld.getName();
//		
//		boolean bMakePretty=!strFieldName.matches(".*[a-z].*");
//		
//		String strCodeTypePrefix = rfcfg.strCodingStyleFieldNamePrefix;
//		if(strCodeTypePrefix==null){
//			strCodeTypePrefix=rfcvFieldAtTheOwner.getCodePrefixVariant();
//		}
//		
//		String strCommandSimple = strFieldName;
//		if(strCodeTypePrefix!=null){
//			if(strCommandSimple.startsWith(strCodeTypePrefix)){
//				//remove prefix
//				strCommandSimple=strCommandSimple.substring(strCodeTypePrefix.length());
//			}else{
//				throw new PrerequisitesNotMetException(
//					"code prefix was set but field doesnt begin with it",
//					strCodeTypePrefix, strFieldName, strCommandSimple, rfcfgOwnerOfField, rfcvFieldAtTheOwner, rfcfg);
//			}
//		}
//		
//		if(bMakePretty){
//			strCommandSimple=MiscI.i().makePretty(strCommandSimple, rfcfg.bFirstLetterUpperCase);
//		}else{
//			/** Already nice to read field name. */
//			strCommandSimple=MiscI.i().firstLetter(strCommandSimple,rfcfg.bFirstLetterUpperCase);
//		}
//		
//		vcid.setUniqueId(prepareFullCommand(vcid, strCommandSimple, rfcfg));//, bIsVariable));
////		id.strSimpleCmdId = rfcfg.getPrefixCustomId()+strCommandCore;
//		vcid.setPrefixCustom(rfcfg.getPrefixCustomId());
//		vcid.setSimpleId(strCommandSimple);
////		id.setAsVariable(bIsVariable);
//		return vcid;
//	}
	
//	/**
//	 * All these concatenated identifiers are good to make sure all commands are unique,
//	 * but... command's size get huge!
//	 * 
//	 * Also, there is redundancy cleaner, avoiding identifiers duplicity mess.
//	 * 
//	 * @param strCommandSimple
//	 * @param rfcfg
//	 * @return
//	 */
//	private String prepareFullCommand(VarCmdUId vcuid, String strCommandSimple, ReflexFillCfg rfcfg){//, boolean bIsVariable){
////		DebugI.i().conditionalBreakpoint(rfcfg.getPrefixCustomId().equals("ConfigDialog"));
//		
//	//	strCommand=rfcfg.strPrefix+strCommand+rfcfg.strSuffix;
//		vcuid.setPartSeparator(strCommandPartSeparator);
//		vcuid.setVarType(rfcfg.getPrefixVar()+rfcfg.rfcv.getVariablePrefix());
//		vcuid.setPrefixCmd(rfcfg.getPrefixCmd());
////		String strFullCommand=preparePart(bIsVariable?
////			vcuid.getVarType():
////			rfcfg.getPrefixCmd());
//		String strFullCommand="";
//		
//		boolean bUseCustomId=true;
//		
//		String strDeclaring="";
//		if(rfcfg.isUsePrefixDeclaringClass()){
//			if(rfcfg.getDeclaringClass()==null){
//				Field field = assertAndGetField(rfcfg.rfcv.getOwner(), rfcfg.rfcv);
//				rfcfg.setDeclaringClass(field.getDeclaringClass());
//			}
//			
//			strDeclaring=preparePart(rfcfg.getPrefixDeclaringClass());
//			
//			if(rfcfg.getPrefixCustomId().equalsIgnoreCase(rfcfg.getPrefixDeclaringClass())){
//				bUseCustomId=false;
//			}
//		}
//		vcuid.setDeclaringClass(rfcfg.getDeclaringClass(), strDeclaring);
//		
//		String strInstancedConcrete="";
//		if(rfcfg.isUsePrefixInstancedConcreteClass()){
//			if(rfcfg.getInstancedConcreteClass()==null){
//				rfcfg.setInstancedConcreteClass(rfcfg.rfcv.getOwner().getClass());
//			}
//			
//			vcuid.setConcreteClass(rfcfg.getInstancedConcreteClass(), preparePart(rfcfg.getPrefixInstancedConcreteClass()));
//			if(!rfcfg.getPrefixDeclaringClass().equalsIgnoreCase(rfcfg.getPrefixInstancedConcreteClass())){
//				strInstancedConcrete=vcuid.getConcreteClassSimpleName();
//				
//				if(rfcfg.getPrefixCustomId().equalsIgnoreCase(rfcfg.getPrefixInstancedConcreteClass())){
//					bUseCustomId=false;
//				}
//			}
//		}
//		
//		String strCustomId="";
//		if(bUseCustomId)strCustomId=preparePart(rfcfg.getPrefixCustomId());
//		vcuid.setPrefix(strCustomId);
//		
//		/**
//		 * this order is good for sorting, from more specific to more generic
//		 */
//		strFullCommand+=strCustomId+strInstancedConcrete+strDeclaring;
//				
//		strFullCommand+=strCommandSimple;
////		id.setStrId(strCommandSimple);
//		
//		strFullCommand+=preparePart(rfcfg.getSuffix(),true);
//		vcuid.setSuffix(rfcfg.getSuffix());
//		
//		return strFullCommand;
//	}
//	
//	private String preparePart(String str){
//		return preparePart(str, false);
//	}
//	private String preparePart(String str, boolean bPrependSeparator){
//		if(str==null || str.isEmpty())return "";
//		if(bPrependSeparator){
//			return strCommandPartSeparator+str;
//		}else{
//			return str+strCommandPartSeparator;
//		}
//	}
	
//	public boolean isUseDefaultCfgIfMissing() {
//		return bUseDefaultCfgIfMissing;
//	}
//
//	public void setUseDefaultCfgIfMissing(boolean bUseDefaultCfgIfMissing) {
//		this.bUseDefaultCfgIfMissing = bUseDefaultCfgIfMissing;
//	}
	
	/**
	 * 
	 * @param objClassOwningField
	 * @param objFieldValue
	 * @return the (super) class that contains a field storing the specified value 
	 */
	public Class<?> getDeclaringClass(IReflexFillCfg objClassOwningField, IReflexFillCfgVariant objFieldValue){
		Field field = assertAndGetField(objClassOwningField, objFieldValue);
		Class<?> clWhereFieldIsActuallyDeclared = field.getDeclaringClass();
		return clWhereFieldIsActuallyDeclared;
	}

	public String getCommandPartSeparator() {
		return strCommandPartSeparator;
	}

//	public void setCommandPartSeparator(String strCommandPartSeparator) {
//		this.strCommandPartSeparator = strCommandPartSeparator;
//		if(this.strCommandPartSeparator==null)throw new PrerequisitesNotMetException("use empty intead of null separator!");
//	}
	
	/**
	 * @param objClassToInspect surely works with anonymous (inner) classes, just like new ArrayList<String>(){}
	 * @param iGenericParamIndex
	 * @return
	 * @throws ClassNotFoundException 
	 */
  public Class getGenericParamAsClassTypeFrom(Object objClassToInspect, int iGenericParamIndex) throws ClassNotFoundException{
		try {
			Class cl = objClassToInspect.getClass();
	    ParameterizedType pt = (ParameterizedType)cl.getGenericSuperclass();
	    String strParamClassName = pt.getActualTypeArguments()[iGenericParamIndex].getTypeName();
	  	return Class.forName(strParamClassName);
		} catch (ClassNotFoundException e) {
	  	if(!objClassToInspect.getClass().isAnonymousClass()){
	  		throw new PrerequisitesNotMetException("will surely work with anonymous (inner) classes tho")
	  			.setCauseAndReturnSelf(e);
	  	}else{
	  		throw e;//new PrerequisitesNotMetException(e);
	  	}
		}
  }
	
}
