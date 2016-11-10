/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/><https://sourceforge.net/u/teike/profile/>
	
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

package com.github.commandsconsolegui.jme.lemur.console;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.PkgTopRef;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldManagerI;
import com.github.commandsconsolegui.cmd.varfield.VarCmdUId;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jme.AudioUII;
import com.github.commandsconsolegui.jme.ConditionalStateAbs;
import com.github.commandsconsolegui.jme.ConditionalStateManagerI;
import com.github.commandsconsolegui.jme.AudioUII.EAudio;
import com.github.commandsconsolegui.jme.DialogStateAbs;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData;
import com.github.commandsconsolegui.jme.lemur.dialog.MaintenanceListLemurDialogStateAbs;
import com.github.commandsconsolegui.misc.HoldRestartable;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/><https://sourceforge.net/u/teike/profile/>
 */
public class ConsoleVarsDialogStateI<T extends Command<Button>> extends MaintenanceListLemurDialogStateAbs<T,ConsoleVarsDialogStateI<T>> {
	private static ConsoleVarsDialogStateI<Command<Button>>	instance=new ConsoleVarsDialogStateI<Command<Button>>();
	public static ConsoleVarsDialogStateI<Command<Button>> i(){return instance;}

	@Override
	protected boolean modifyEntry(DialogStateAbs<T, ?> diagModal,	DialogListEntryData<T> dledAtModal,	ArrayList<DialogListEntryData<T>> adledAtThisToApplyResultsList) {
		String strUserTypedValue = diagModal.getInputTextAsUserTypedValue();
		if(strUserTypedValue==null)return false; //was a filter
		
		if(strUserTypedValue.equals(""+null))strUserTypedValue=null; //user requested value to become null
		
		// must be after "null" string check above
		if(strUserTypedValue!=null){
			strUserTypedValue=MiscI.i().removeQuotes(strUserTypedValue);
		}
		
		// can be null
		boolean bChangesMade=false;
		for(DialogListEntryData<T> dledToCfg:adledAtThisToApplyResultsList){
			VarCmdFieldAbs vcf = (VarCmdFieldAbs)dledToCfg.getUserObj();
			vcf.setObjectRawValue(strUserTypedValue);
			bChangesMade=true;
		}
		
		return bChangesMade;
	}
	
//	private HoldRestartable<ChoiceVarDialogState> hrdiagChoice = new HoldRestartable<ChoiceVarDialogState>(this);
	
	public static class CfgParm extends MaintenanceListLemurDialogStateAbs.CfgParm{
		public CfgParm(Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier) {
			super(fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, fEntryHeightMultiplier, null, null);
		}
	}
	private CfgParm	cfg;
	@Override
	public ConsoleVarsDialogStateI<T> configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		
//		if(hrdiagChoice.getRef()==null){ 
			//when restarting this, it's child may be already/still there TODO restart child too?
			ChoiceVarDialogState diagChoice = ConditionalStateManagerI.i().getConditionalState(
				ChoiceVarDialogState.class, null);
			if(diagChoice==null){
				diagChoice=new ChoiceVarDialogState();
				diagChoice.configure(new ChoiceVarDialogState.CfgParm(0.9f, 0.5f, 0.5f, null));//.setId(strId));
				diagChoice.setInputToUserEnterCustomValueMode(true);
			}
//			hrdiagChoice.setRef(diagChoice);
//		}
		
		cfg.setDiagChoice(diagChoice);
//		cfg.setDiagChoice(hrdiagChoice.getRef());
		
		cfg.setDiagQuestion(null);
		
		super.configure(cfg);
		
		storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	@Override
	protected ConsoleVarsDialogStateI<T> getThis() {
		return this;
	}
	
	protected class CmdBtnChangeValue implements Command<Button>{
		@Override
		public void execute(Button source) {
			ConsoleVarsDialogStateI.this.actionCustomAtEntry(
				ConsoleVarsDialogStateI.this.getDledFrom(source));
//			ConsoleVarsDialogStateI.this.changeValue(
//				ConsoleVarsDialogStateI.this.getDledFrom(source));
		}
	}
	private CmdBtnChangeValue cbcv = new CmdBtnChangeValue();
	
	private void changeValue(DialogListEntryData<T> dled){
		AudioUII.i().playOnUserAction(EAudio.SelectEntry);
		
		VarCmdFieldAbs vcf = (VarCmdFieldAbs)dled.getUserObj();
		boolean bChanged=false;
		if(vcf instanceof BoolTogglerCmdField){
			((BoolTogglerCmdField)vcf).toggle();
			bChanged=true;
		}else{
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("not yet supported "+vcf.getClass());
			AudioUII.i().playOnUserAction(EAudio.Failure);				
		}
		
		if(bChanged)requestRefreshUpdateList();
	}
	
	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dledSelected) {
		Object objUser = dledSelected.getUserObj();
		if(objUser instanceof BoolTogglerCmdField){
			changeValue(dledSelected);
		}else
		{
			if(objUser instanceof VarCmdFieldAbs){
				super.actionCustomAtEntry(dledSelected);
			}else
			if(objUser instanceof EGroupKeys){
				// skipper, this is actually an empty group (no tree expanded/shrinked)
			}else{
				throw new PrerequisitesNotMetException("user object not a var", objUser, dledSelected, this);
			}
		}
	}
	
	enum EGroupKeys{
		ParentDeclaringClass,
		ParentConcreteClass,
		DialogsCons,
		Dialogs,
		PkgTopRef,
		;
	}
	private DialogListEntryData<T>	dledConsProjPackage;
	private DialogListEntryData<T>	dledDiagsCons;
	private DialogListEntryData<T>	dledDiags;
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void updateList() {
		//clearList()
		ArrayList<VarCmdFieldAbs> avcf = VarCmdFieldManagerI.i().getListCopy();
		
		for(VarCmdFieldAbs vcfVarEntry:avcf){
			if(vcfVarEntry.getUniqueVarId()==null)continue;
			
			// check if already at the list
			DialogListEntryData<T> dledVarEntry = null;
			for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
				Object obj = dled.getUserObj();
				if (obj instanceof EGroupKeys)continue;
//				{
//					EGroupKeys ek = (EGroupKeys) obj;
//					switch(ek){
//						case DialogsCons:
//						case ParentConcreteClass:
//						case ParentDeclaringClass:
//						case PkgTopRef:
//							continue;
//					}
//				}
//				if(dled.getUserObj()==PkgTopRef.class)continue;
				VarCmdFieldAbs vcfAtListEntry = ((VarCmdFieldAbs)obj);
				if(vcfVarEntry.getUniqueVarId().equals(vcfAtListEntry.getUniqueVarId())){
					dledVarEntry = dled;
				}
			}
			
			// create new if not at list
			if(dledVarEntry==null){
				dledVarEntry=createNewVarEntry(vcfVarEntry); //will return with parent already set
				
				// will set the package entry as the rootEntry's parent
				String strVarConcrPkg = vcfVarEntry.getIdTmpCopy().getConcreteClass().getPackage().getName();
				boolean bConsProj = strVarConcrPkg.startsWith(PkgTopRef.class.getPackage().getName());
				if (vcfVarEntry.getOwner() instanceof DialogStateAbs) {
					setParentestParent(dledVarEntry, bConsProj ? dledDiagsCons : dledDiags);
				}else{
					if(bConsProj)setParentestParent(dledVarEntry, dledConsProjPackage);
				}
			}
			
			// truncate value string
			String strVal=null;
			if(vcfVarEntry.getRawValue()==null){
				strVal=""+null;
			}else{
				strVal=vcfVarEntry.getValueAsString(3);
	//			if(strVal==null)strVal="";
				if(vcfVarEntry instanceof StringVarField){
					strVal='"'+strVal+'"';
					if(strVal.length()>10){
						String strEtc="+"; //TODO use 3 dots single character if it exists or some other symbol?
						strVal=strVal.substring(0, 10-strEtc.length())+strEtc;
					}
				}
			}
			
			// update custom buttons as values may have changed
			//TODO compare if values changed?
			dledVarEntry.clearCustomButtonActions();
			dledVarEntry.addCustomButtonAction(strVal, (T)cbcv);
		}
		
		
		super.updateList(); //MiscI.i().getClassTreeFor(this)
	}
	
	private void setParentestParent(DialogListEntryData<T> dledEntry, DialogListEntryData<T> dledParent){
		DialogListEntryData<T> dledParentest = dledEntry.getParentest();
		
		if (dledParentest.getUserObj() instanceof EGroupKeys) {
			EGroupKeys egk = (EGroupKeys) dledParentest.getUserObj();
			switch(egk){
				case PkgTopRef:
				case DialogsCons:
				case Dialogs:
					// constant top groups are already setup at initialization
					return; 
			}
		}
		
		if(dledParentest!=dledParent){
			dledParentest.setParent(dledParent);
		}
	}
	
	/**
	 * For each var (field) there is only one declaring class (the super or concrete one).
	 * But, each concrete class can have many super classes, the declaring ones.
	 * So the top parent (root list entries) must be the concrete class!
	 * @param vcf
	 * @return
	 */
	private DialogListEntryData<T> createNewVarEntry(VarCmdFieldAbs vcf){
		VarCmdUId vidCopy = vcf.getIdTmpCopy();
		
		DialogListEntryData<T> dledVarEntryParent = null;
		
		if(vidCopy.getConcreteClassSimpleName().equals(vidCopy.getDeclaringClassSimpleName())){
			dledVarEntryParent = createParentEntry(
					vidCopy.getDeclaringClassSimpleName(),
					EGroupKeys.ParentDeclaringClass);
		}else{
			DialogListEntryData<T> dledDeclaringClassParent = createParentEntry(
				vidCopy.getConcreteClassSimpleName()+vidCopy.getDeclaringClassSimpleName(),
				EGroupKeys.ParentDeclaringClass);
			
			DialogListEntryData<T> dledConcreteClassParent = createParentEntry(
				vidCopy.getConcreteClassSimpleName(),
				EGroupKeys.ParentConcreteClass);
			
			if(dledDeclaringClassParent.getParent()==null){
				dledDeclaringClassParent.setParent(dledConcreteClassParent);
			}else{
				if(dledDeclaringClassParent.getParent()!=dledConcreteClassParent){
					throw new PrerequisitesNotMetException("Cannot re-parent a declaring class list entry", dledDeclaringClassParent, dledDeclaringClassParent.getParent(), dledConcreteClassParent);
				}
			}
			
			dledVarEntryParent = dledDeclaringClassParent;
		}
		
		// prepare var linked one
		String strVarId = vcf.getUniqueVarId(true);
		strVarId=strVarId.replaceFirst(vidCopy.getDeclaringClassSimpleName(), "");
		strVarId=strVarId.replaceFirst(vidCopy.getConcreteClassSimpleName(), "");
		
		DialogListEntryData<T> dledVar = new DialogListEntryData<T>(this);
		dledVar.setText(strVarId, vcf);
		dledVar.setParent(dledVarEntryParent);
		addEntry(dledVar);
		
		return dledVar;
	}
	
	private DialogListEntryData<T> createParentEntry(String strParentTextKey, EGroupKeys egk){
		// prepare declaring class as tree parent  
		DialogListEntryData<T> dledParent=null;
		for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
			if(dled.getTextValue().equals(strParentTextKey)){
				dledParent=dled;
				break;
			}
		}
		if(dledParent==null){
			dledParent=new DialogListEntryData<T>(this);
			dledParent.setText(strParentTextKey,egk);
			addEntry(dledParent);
		}
		
		return dledParent;
	}
	
	@Override
	protected boolean initAttempt() {
		if(!super.initAttempt())return false;
		
		// the top package as parent:
		dledConsProjPackage = new DialogListEntryData<T>(this);
		dledConsProjPackage.setText("(Package: "+PkgTopRef.class.getPackage().getName()+")", EGroupKeys.PkgTopRef);//PkgTopRef.class);
		
		dledDiagsCons = new DialogListEntryData<T>(this);
		dledDiagsCons.setText("(DialogsCons)",EGroupKeys.DialogsCons);
		dledDiagsCons.setParent(dledConsProjPackage);
		
		dledDiags = new DialogListEntryData<T>(this);
		dledDiags.setText("(Dialogs)",EGroupKeys.Dialogs);
		
		return true;
	}
	
	@Override
	public ConsoleVarsDialogStateI<T> copyToSelfValuesFrom(ConsoleVarsDialogStateI<T> casDiscarding) {
		cfg.setRestartCopyToSelfAllEntries(false);
		
		super.copyToSelfValuesFrom(casDiscarding);
		
		return getThis();
	}
	
	@Override
	protected String getTextInfo() {
		String str="";
		
		str+="Help("+ConsoleVarsDialogStateI.class.getSimpleName()+"):\n";
		str+="\tAll console variables are accessible thru this dialog.\n";
		
		return str+super.getTextInfo();
	}
	
	@Override
	protected void initSuccess() {
		super.initSuccess();
		addEntry(dledConsProjPackage);
		addEntry(dledDiagsCons);
		addEntry(dledDiags);
	}
	
//	@Override
//	public void focusGained() {
//		super.focusGained();
//		requestRefreshList();
//	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=ConsoleVarsDialogStateI.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=ConsoleVarsDialogStateI.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
	
}
