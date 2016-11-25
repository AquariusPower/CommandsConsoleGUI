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

package com.github.commandsconsolegui.spLemur.dialog;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI.EMouseCursorButton;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.GuiGlobals;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public abstract class BasicLemurDialogStateAbs<ACT,THIS extends BasicLemurDialogStateAbs<ACT,THIS>> extends LemurDialogStateAbs<ACT,THIS>{
	private final StringCmdField scfAddEntry = new StringCmdField(this);
	private CfgParm	cfg;
	
	public BasicLemurDialogStateAbs() {
		setPrefixCmdWithIdToo(true);
	}
	
	public static class CfgParm extends LemurDialogStateAbs.CfgParm{
		public CfgParm(
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Float fEntryHeightMultiplier){//, BaseDialogStateAbs<T> modalParent) {
			super(null,
					fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, fEntryHeightMultiplier);//, modalParent);
		}
	}
	@Override
	public THIS configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg; //this also validates if icfg is the CfgParam of this class
		
		super.configure(cfg); //params are identical
		
		/**
		 * this is actually not really necessary here, 
		 * but is an example as state changes can be delayed,
		 * to save CPU or any other reason.
		 */
		setRetryDelayFor(100L, ERetryDelayMode.Init.s());
		
//		storeCfgAndReturnSelf(cfg);
		
		return getThis();
	}
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		if(getChildDiagModalInfoCurrent()!=null){
			if(getChildDiagModalInfoCurrent().getDiagModal().isChoiceMade()){
				applyResultsFromModalDialog();
			}else{
//				if(!getChildDiagModalInfoCurrent().getDiagModal().isEnabled()){
				if(!isHasEnabledChildDialog()){
					setChildDiagModalInfoCurrent(null); //TODO not working!
				}
			}
		}
		
//		if(isHasEnabledChildDialog()){
//			if(getChildDiagModalInfoCurrent().getDiagModal().isChoiceMade()){
//				applyResultsFromModalDialog();
//			}else{
//				setChildDiagModalInfoCurrent(null);
//			}
//		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public DialogListEntryData getDledFrom(Spatial spt){
		return MiscJmeI.i().getUserDataPSH(spt, DialogListEntryData.class);
	}
	
	protected class CommandSel implements Command<Button>{
		@Override
		public void execute(Button btn) {
			BasicLemurDialogStateAbs.this.selectAndChoseOption(getDledFrom(btn));
		}
	}
	CommandSel cmdSel = new CommandSel();
	
	/**
	 * TODO addTextEntry()
	 * @param strText
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DialogListEntryData addEntryQuick(String strText){
		DialogListEntryData dle = new DialogListEntryData(this);
		if(strText==null){
			strText=this.getUniqueId()+": Quick filler temp entry: "
				+System.nanoTime(); //can be real time as is just a dummy text
		}
//		dle.setText(strText, cmdCfg);
		dle.setText(strText, null);
		
		if(isOptionChoiceSelectionMode()){
			dle.addCustomButtonAction("<-",(ACT)cmdSel);
		}
		
		super.addEntry(dle);
		
//		if(adleFullList.size()>100)adleFullList.remove(0);
		
		requestRefreshUpdateList();
		
		return dle;
	}

	@Override
	protected boolean initCheckPrerequisites() {
		if(GuiGlobals.getInstance()==null)return false;
		
		return super.initCheckPrerequisites();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(scfAddEntry,"[strText] [strParenUId]")){
			String strText = cd.getCurrentCommandLine().paramString(1);
			String strParentUId = cd.getCurrentCommandLine().paramString(2);
			
			DialogListEntryData dleParent = null;
			for(DialogListEntryData dle:getCompleteEntriesListCopy()){
				if(dle.getUId().equalsIgnoreCase(strParentUId)){
					dleParent=dle;
					break;
				}
			}
			
			DialogListEntryData dleNew = addEntryQuick(strText);
			if(dleParent!=null){
				dleNew.setParent(dleParent);
			}
			
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cd);
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execTextDoubleClickActionFor(DialogListEntryData dled,Spatial sptActionSourceElement) {
		if(isOptionSelectionMode())throw new PrerequisitesNotMetException("Option mode should not reach this method.");
		
		actionMainAtEntry(dled, sptActionSourceElement);
		
		return true;
	}

	@Override
	public boolean execActionFor(EMouseCursorButton e, Spatial sptSource) {
		switch(e){
			default:
				cd().dumpDevInfoEntry("no action for "+e+" "+sptSource.getName());
				break;
		}
		
		return true;
	}

	@Override
	protected boolean initAttempt() {
		if(!super.initAttempt())return false;
		
		return true;
	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=BasicLemurDialogStateAbs.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=BasicLemurDialogStateAbs.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
}
