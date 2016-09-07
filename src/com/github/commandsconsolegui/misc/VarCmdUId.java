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

import java.lang.reflect.Field;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class VarCmdUId implements Cloneable{
	// parts of the unique id
	private String strPrefixCmd=null;
	private String strVarType=null;
	
	private String strConcreteClassSName=null;
	private String strDeclaringClassSName=null;
	private String strPrefix=null;
	private String strSimpleId=null;
	private String strSuffix=null;
	// the unique id
	private String strUniqueId=null;
	
//	private Boolean	bIsVariable;
	private String	strPrefixCustomToSolveConflicts;
	private Class	clDeclaring;
	private Class	clConcrete;
	private String	strPartSeparator;
	
	public VarCmdUId() {
	}
//	public IdTmp(boolean bIsVariable, String strSimpleId, String strUniqueId) {
//		this.setUniqueId(strUniqueId);
//		this.setSimpleId(strSimpleId);
//		this.setAsVariable(bIsVariable);
//	}
	/**
	 * 
	 * @param bAsVariable if false will be as Command, if null will be without prefix.
	 * @return
	 */
	public String getUniqueId(Boolean bAsVariable) {
		if(bAsVariable==null)return strUniqueId;
		
		if(bAsVariable){
			return strVarType+strPartSeparator+strUniqueId;
		}else{
			return strPrefixCmd+strPartSeparator+strUniqueId;
		}
	}
	public VarCmdUId setUniqueId(String strUniqueId) {
		PrerequisitesNotMetException.assertNotAlreadySet("strUniqueId", this.strUniqueId, strUniqueId, this);
		this.strUniqueId = strUniqueId;
		return this;
	}
	public String getSimpleId() {
		return strSimpleId;
	}
	public VarCmdUId setSimpleId(String strSimpleId) {
		PrerequisitesNotMetException.assertNotAlreadySet("simple id", this.strSimpleId, strSimpleId, this);
		this.strSimpleId = strSimpleId;
		return this;
	}
//	public boolean isVariable() {
//		return bIsVariable;
//	}
//	public VarCmdUId setAsVariable(boolean bIsVariable) {
//		PrerequisitesNotMetException.assertNotAlreadySet("bIsVariable", this.bIsVariable, bIsVariable, this);
//		this.bIsVariable = bIsVariable;
//		return this;
//	}
	public String getPrefixCustomToSolveConflicts() {
		return strPrefixCustomToSolveConflicts;
	}
	public VarCmdUId setPrefixCustomToSolveConflicts(String strPrefixCustomToSolveConflicts) {
		PrerequisitesNotMetException.assertNotAlreadySet("strPrefixCustomToSolveConflicts", this.strPrefixCustomToSolveConflicts, strPrefixCustomToSolveConflicts, this);
		this.strPrefixCustomToSolveConflicts = strPrefixCustomToSolveConflicts;
		return this;
	}
	public String getVarType() {
		return strVarType;
	}
	public VarCmdUId setVarType(String strType) {
		PrerequisitesNotMetException.assertNotAlreadySet("strType", this.strVarType, strType, this);
		this.strVarType = strType;
		return this;
	}
	
	public Class getConcreteClass(){
		return clConcrete;
	}
	public Class getDeclaringClass(){
		return clDeclaring;
	}
	
	public String getConcreteClassSName() {
		return strConcreteClassSName;
	}
	public VarCmdUId setConcreteClass(Class clConcrete, String strConcreteClass) {
		PrerequisitesNotMetException.assertNotAlreadySet("strConcreteClassSName", this.strConcreteClassSName, strConcreteClassSName, this);
		this.clConcrete=clConcrete;
		this.strConcreteClassSName = strConcreteClass;
		return this;
	}
	public String getDeclaringClassSName() {
		return strDeclaringClassSName;
	}
	public VarCmdUId setDeclaringClass(Class clDeclaring, String strDeclaringClass) {
		PrerequisitesNotMetException.assertNotAlreadySet("strDeclaringClassSName", this.strDeclaringClassSName, strDeclaringClassSName, this);
		this.clDeclaring=clDeclaring;
		this.strDeclaringClassSName = strDeclaringClass;
		return this;
	}
	public String getPrefix() {
		return strPrefix;
	}
	public VarCmdUId setPrefix(String strPrefix) {
		PrerequisitesNotMetException.assertNotAlreadySet("prefix", this.strPrefix, strPrefix, this);
		this.strPrefix = strPrefix;
		return this;
	}
	public String getSuffix() {
		return strSuffix;
	}
	public VarCmdUId setSuffix(String strSuffix) {
		PrerequisitesNotMetException.assertNotAlreadySet("suffix", this.strSuffix, strSuffix, this);
		this.strSuffix = strSuffix;
		return this;
	}
	
	@Override
	public VarCmdUId clone() {
		for(Field fld:VarCmdUId.class.getFields()){
			if(!fld.getType().isPrimitive())throw new PrerequisitesNotMetException("clone will fail!",this);
		}
		
		VarCmdUId idCopy = null;
		
		try {
			/**
			 * this will only work properly if all fields are primitives...
			 */
			idCopy = (VarCmdUId)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return idCopy;
	}
	
	public VarCmdUId setPrefixCmd(String strPrefixCmd) {
		this.strPrefixCmd=strPrefixCmd;
		return this;
	}
	
	public String getPrefixCmd() {
		return strPrefixCmd;
	}
	
	public VarCmdUId setPartSeparator(String strCommandPartSeparator) {
		this.strPartSeparator=strCommandPartSeparator;
		return this;
	}
}
