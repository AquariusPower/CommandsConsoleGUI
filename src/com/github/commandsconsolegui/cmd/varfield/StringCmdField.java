/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower>
	
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

package com.github.commandsconsolegui.cmd.varfield;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.VarIdValueOwnerData;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * Represents a class field.
 * Mainly used to define commands user can issue on the console. 
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower>
 *
 */
public class StringCmdField extends VarCmdFieldAbs<String,StringCmdField>{
//	String str = "ERROR: NOT SET"; // hashcode depends on it not being null
//	private String strCmdId = null;
//	private int	iReflexFillCfgVariant;
//	private String	strReflexFillCfgCodePrefixVariant;
//	private String	strHelpComment;
	private static boolean bIgnoreCaseOnComparison = true;
//	public static final String strCodePrefix="scf"; //ex.: scfTestCommand
//	private static ArrayList<StringCmdField> ascfList = new ArrayList<StringCmdField>();
	
//	public static ArrayList<StringCmdField> getListCopy(){
//		return new ArrayList<StringCmdField>(ascfList);
//	}
	
	
	/**
	 * default is true, useful for easy lowercase user typed commands comparison
	 * @param b
	 */
	public static void setIgnoreCaseComparison(boolean b){
		bIgnoreCaseOnComparison=b;
	}
	
//	public StringField(IReflexFillCfg rfcfgOwner){
//		this(rfcfgOwner,0);
//	}
	
//	/**
//	 * 
//	 * @param strCmdId overrides auto-identifier thru reflection
//	 * @param strHelpComment
//	 */
//	public StringCmdField(String strCmdId, String strHelpComment){
////		super(true);
//		super(null);
//		super.setUniqueCmdId(new IdTmp(false, strCmdId, strCmdId));
//		setHelp(strHelpComment);
////		this.bReflexingIdentifier = false;
////		StringCmdField.ascfList.add(this);
//	}
	
	/**
	 * The value cannot be prepared at the constructor, 
	 * as it has not returned yet, so it's object owner will not have 
	 * a valid field (will still be null).
	 */
	public StringCmdField(IReflexFillCfg rfcfgOwner, String strReflexFillCfgCodePrefixVariant, String strHelpComment){ // int iReflexFillCfgVariant){
//		this((String)null,strHelpComment);
		super(rfcfgOwner);
		setHelp(strHelpComment);
		
		setCodePrefixVariant(strReflexFillCfgCodePrefixVariant);
////		this.iReflexFillCfgVariant=iReflexFillCfgVariant;
//		this.strReflexFillCfgCodePrefixVariant = strReflexFillCfgCodePrefixVariant;
//		if(this.strReflexFillCfgCodePrefixVariant==null){
//			this.strReflexFillCfgCodePrefixVariant=StringCmdField.getCodePrefixDefault();
//		}
		
//		ReflexFill.assertAndGetField(rfcfgOwner, this);
		
//		setOwner(rfcfgOwner);
		
		if(getOwner()==null){
			throw new NullPointerException("cant be null for: "+IReflexFillCfg.class.getName());
		}
	}
	public StringCmdField(IReflexFillCfg rfcfgOwner){
		this(rfcfgOwner, null, null);
	}
	public StringCmdField(IReflexFillCfg rfcfgOwner, String strReflexFillCfgCodePrefixVariant){
		this(rfcfgOwner, strReflexFillCfgCodePrefixVariant, null);
	}
	
//	/**
//	 * returns the string value stored on this field.
//	 */
//	@Override
//	public String toString() {
//		if(getUniqueCmdId()==null)chkAndInit();
//		return getUniqueCmdId();
//	}
	@Override
	public String getValueAsString() {
		if(getUniqueCmdId()==null)chkAndInit();
		return getUniqueCmdId();
	}
	@Override
	public String getValueAsString(int iIfFloatPrecision) {
		return getValueAsString();
	}
	
//	public String getHelpComment(){
//		return strHelp;
//	}
	
//	private void initialize(){
//		/**
//		 * This basically prevents recursive infinite loop,
//		 * if this is called at reflex fill method.
//		 */
////		super.strCmdId=errorMessage();
//		super.setUniqueId(ReflexFillI.i().createIdentifierWithFieldName(getOwner(), this, false));
////		throw new NullPointerException("not initialized properly: "+this);
//	}
	
//	
//	private String errorMessage(){
//		return "ERROR: "+StringCmdField.class.getName()+" not yet properly initialized!";
//	}
	
	@Override
	public boolean equals(Object obj) {
//		if(strCmdId==null)throw new NullPointerException(errorMessage());
		if(getUniqueCmdId()==null)chkAndInit();//initialize();
		if(bIgnoreCaseOnComparison){
			return getUniqueCmdId().equalsIgnoreCase(""+obj);
		}else{
			return getUniqueCmdId().equals(""+obj);
		}
	}
	
	@Override
	public int hashCode() {
//		if(strCmdId==null)throw new NullPointerException(errorMessage());
		if(getUniqueCmdId()==null)chkAndInit();//initialize();
		return getUniqueCmdId().hashCode();
	}
	
//	@Override
//	public int getReflexFillCfgVariant() {
//		return iReflexFillCfgVariant;
//	}

//	@Override
//	public String getCodePrefixVariant() {
//		return strReflexFillCfgCodePrefixVariant;
//	}

	@Override
//	public StringCmdField setObjectValue(CommandsDelegator.CompositeControl ccCD, Object objValue) {
	public StringCmdField setObjectRawValue(Object objValue) {
		throw new PrerequisitesNotMetException("TODO should this method do nothing here? should it be only set thru reflex?", this, objValue); //TODO
	}

	@Override
	public String getReport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValueRaw() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueVarId(boolean bRemoveType) {
		/**
		 * no var to console commands yet.
		 * TODO console commands could have a var representing their return value?
		 */
		return null;
	}
	@Override
	public String getUniqueVarId() {
		return getUniqueVarId(false); 
	}
	@Override
	public StringCmdField setConsoleVarLink(CommandsDelegator.CompositeControl ccCD, VarIdValueOwnerData vivo) {
//	public StringCmdField setConsoleVarLink(VarIdValueOwnerData vivo) {
		throw new PrerequisitesNotMetException("TODO: Each command could have a variable storing it's possible return value!", this, vivo); //TODO
	}

	@Override
	public String getHelp() {
		return null; //TODO mmm... check its uses, and let it use super getHelp()
	}

	@Override
	public String getVariablePrefix() {
		return "StringCmd";
	}
	
	@Override
	protected StringCmdField getThis() {
		return this;
	}

//	public static String getCodePrefixDefault() {
//		return "scf";
//	}
	private static String strCodePrefixDefault="scf";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
}
