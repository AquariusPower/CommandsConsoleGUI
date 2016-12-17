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
package com.github.commandsconsolegui.spCmd.varfield;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class VarCmdReflexFillI {
	private static VarCmdReflexFillI instance = new VarCmdReflexFillI();
	public static VarCmdReflexFillI i(){return instance;}
	
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
	public VarCmdUId createIdentifierWithFieldName(IReflexFillCfg rfcfgOwnerOfField, IReflexFillCfgVariant rfcvFieldAtTheOwner){//, boolean bIsVariable){
		if(rfcfgOwnerOfField==null){
			throw new PrerequisitesNotMetException("Invalid usage, "
				+IReflexFillCfg.class.getName()+" owner is null, is this a local (non field) variable?");
		}
		
		ReflexFillCfg rfcfg = rfcfgOwnerOfField.getReflexFillCfg(rfcvFieldAtTheOwner);
		if(rfcfg==null){
//			if(isUseDefaultCfgIfMissing()){
				rfcfg = new ReflexFillCfg(rfcvFieldAtTheOwner);
//			}else{
//				throw new PrerequisitesNotMetException("Configuration is missing for "
//					+rfcfgOwnerOfField.getClass().getName()
//					+" -> "
//					+rfcvFieldAtTheOwner.getClass().getName()
//					+":"
//					+rfcvFieldAtTheOwner.getCodePrefixVariant());
//			}
		}
		
		VarCmdUId vcid = new VarCmdUId();
		
		Field fld = ReflexFillI.i().assertAndGetField(rfcfgOwnerOfField, rfcvFieldAtTheOwner);
		Class<?> cl = fld.getDeclaringClass();
		
		String strFieldName=fld.getName();
		
		boolean bMakePretty=!strFieldName.matches(".*[a-z].*");
		
		String strCodeTypePrefix = rfcfg.getCodingStyleFieldNamePrefix();
		if(strCodeTypePrefix==null){
			strCodeTypePrefix=rfcvFieldAtTheOwner.getCodePrefixVariant();
		}
		
		String strCommandSimple = strFieldName;
		if(strCodeTypePrefix!=null){
			if(strCommandSimple.startsWith(strCodeTypePrefix)){
				//remove prefix
				strCommandSimple=strCommandSimple.substring(strCodeTypePrefix.length());
			}else{
				throw new PrerequisitesNotMetException(
					"code prefix was set but field doesnt begin with it",
					strCodeTypePrefix, strFieldName, strCommandSimple, rfcfgOwnerOfField, rfcvFieldAtTheOwner, rfcfg);
			}
		}
		
		if(bMakePretty){
			strCommandSimple=MiscI.i().makePretty(strCommandSimple, rfcfg.isFirstLetterUpperCase());
		}else{
			/** Already nice to read field name. */
			strCommandSimple=MiscI.i().firstLetter(strCommandSimple,rfcfg.isFirstLetterUpperCase());
		}
		
		vcid.setUniqueId(prepareFullCommand(vcid, strCommandSimple, rfcfg));//, bIsVariable));
//		id.strSimpleCmdId = rfcfg.getPrefixCustomId()+strCommandCore;
		vcid.setPrefixCustom(rfcfg.getPrefixCustomId());
		vcid.setSimpleId(strCommandSimple);
//		id.setAsVariable(bIsVariable);
		return vcid;
	}
	
	/**
	 * All these concatenated identifiers are good to make sure all commands are unique,
	 * but... command's size get huge!
	 * 
	 * Also, there is redundancy cleaner, avoiding identifiers duplicity mess.
	 * 
	 * @param strCommandSimple
	 * @param rfcfg
	 * @return
	 */
	private String prepareFullCommand(VarCmdUId vcuid, String strCommandSimple, ReflexFillCfg rfcfg){//, boolean bIsVariable){
//		DebugI.i().conditionalBreakpoint(rfcfg.getPrefixCustomId().equals("ConfigDialog"));
		
	//	strCommand=rfcfg.strPrefix+strCommand+rfcfg.strSuffix;
		vcuid.setPartSeparator(ReflexFillI.i().getCommandPartSeparator());
		vcuid.setVarType(rfcfg.getPrefixVar()+rfcfg.getRfcv().getVariablePrefix());
		vcuid.setPrefixCmd(rfcfg.getPrefixCmd());
//		String strFullCommand=preparePart(bIsVariable?
//			vcuid.getVarType():
//			rfcfg.getPrefixCmd());
		String strFullCommand="";
		
		boolean bUseCustomId=true;
		
		String strDeclaring="";
		if(rfcfg.isUsePrefixDeclaringClass()){
			if(rfcfg.getDeclaringClass()==null){
				Field field = ReflexFillI.i().assertAndGetField(rfcfg.getRfcv().getOwner(), rfcfg.getRfcv());
				rfcfg.setDeclaringClass(field.getDeclaringClass());
			}
			
			strDeclaring=preparePart(rfcfg.getPrefixDeclaringClass());
			
			if(rfcfg.getPrefixCustomId().equalsIgnoreCase(rfcfg.getPrefixDeclaringClass())){
				bUseCustomId=false;
			}
		}
		vcuid.setDeclaringClass(rfcfg.getDeclaringClass(), strDeclaring);
		
		String strInstancedConcrete="";
		if(rfcfg.isUsePrefixInstancedConcreteClass()){
			if(rfcfg.getInstancedConcreteClass()==null){
				rfcfg.setInstancedConcreteClass(rfcfg.getRfcv().getOwner().getClass());
			}
			
			vcuid.setConcreteClass(rfcfg.getInstancedConcreteClass(), preparePart(rfcfg.getPrefixInstancedConcreteClass()));
			if(!rfcfg.getPrefixDeclaringClass().equalsIgnoreCase(rfcfg.getPrefixInstancedConcreteClass())){
				strInstancedConcrete=vcuid.getConcreteClassSimpleName();
				
				if(rfcfg.getPrefixCustomId().equalsIgnoreCase(rfcfg.getPrefixInstancedConcreteClass())){
					bUseCustomId=false;
				}
			}
		}
		
		String strCustomId="";
		if(bUseCustomId)strCustomId=preparePart(rfcfg.getPrefixCustomId());
		vcuid.setPrefix(strCustomId);
		
		/**
		 * this order is good for sorting, from more specific to more generic
		 */
		strFullCommand+=strCustomId+strInstancedConcrete+strDeclaring;
				
		strFullCommand+=strCommandSimple;
//		id.setStrId(strCommandSimple);
		
		strFullCommand+=preparePart(rfcfg.getSuffix(),true);
		vcuid.setSuffix(rfcfg.getSuffix());
		
		return strFullCommand;
	}
	
	private String preparePart(String str){
		return preparePart(str, false);
	}
	private String preparePart(String str, boolean bPrependSeparator){
		if(str==null || str.isEmpty())return "";
		if(bPrependSeparator){
			return ReflexFillI.i().getCommandPartSeparator()+str;
		}else{
			return str+ReflexFillI.i().getCommandPartSeparator();
		}
	}
	
}
