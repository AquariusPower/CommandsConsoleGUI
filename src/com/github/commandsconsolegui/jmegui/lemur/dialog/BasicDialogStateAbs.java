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

package com.github.commandsconsolegui.jmegui.lemur.dialog;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.GuiGlobals;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public abstract class BasicDialogStateAbs<T,R extends BasicDialogStateAbs<T,R>> extends LemurDialogGUIStateAbs<T,R>{
	StringCmdField scfAddEntry = new StringCmdField(this,null,"[strEntryText]");
	private CfgParm	cfg;
	
	public BasicDialogStateAbs() {
		setPrefixCmdWithIdToo(true);
	}
	
	public static class CfgParm extends LemurDialogGUIStateAbs.CfgParm{
		boolean bPrepareTestData;
		public CfgParm doPrepareTestData(){
			bPrepareTestData=true;
			return this;
		}
		public CfgParm(
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Integer iEntryHeightPixels){//, BaseDialogStateAbs<T> modalParent) {
			super(null, null,
					fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, iEntryHeightPixels);//, modalParent);
		}
	}
	@Override
	public R configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg; //this also validates if icfg is the CfgParam of this class
		
		super.configure(cfg); //params are identical
		
		/**
		 * this is actually not really necessary here, 
		 * but is an example as state changes can be delayed,
		 * to save CPU or any other reason.
		 */
		super.setRetryDelay(100); //sets for all kinds
		super.setRetryDelay(EDelayMode.Init,1000); //must be after the generic one above.
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	protected String getTextInfo() {
		return super.getTextInfo()+"Custom Dialog: Test.";
	};
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(getDiagModalCurrent()!=null){
			if(getDiagModalCurrent().getDiagModal().isChoiceMade()){
				applyResultsFromModalDialog();
			}
		}
		
		return super.updateAttempt(tpf);
	}
	
	public abstract boolean prepareTestData();
	
	@SuppressWarnings("unchecked")
	public DialogListEntryData<T> getDataFrom(Spatial spt){
		String strKey = DialogListEntryData.class.getName();
		Object data = spt.getUserData(strKey);
		if(data==null)throw new PrerequisitesNotMetException("missing user object "+strKey);
		return (DialogListEntryData<T>) data;
	}
	
//	enum EAudio{
//		RemoveListEntry,
//		RemoveSubTreeEntry,
//		;
//	}
	
	public class CommandSel implements Command<Button>{
		@Override
		public void execute(Button btn) {
			BasicDialogStateAbs.this.selectAndChoseOption(getDataFrom(btn));
		}
	}
	CommandSel cmdSel = new CommandSel();
	
	/**
	 * TODO addTextEntry()
	 * @param strText
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DialogListEntryData<T> addEntryQuick(String strText){
		DialogListEntryData<T> dle = new DialogListEntryData<T>();
		if(strText==null){
			strText=this.getId()+": New test entry: "
//				+MiscI.i().getDateTimeForFilename(true)
//				+", "
				+System.nanoTime();
		}
//		dle.setText(strText, cmdCfg);
		dle.setText(strText, null);
		
		if(isOptionChoiceSelectionMode()){
			dle.addCustomButtonAction("<-",(T)cmdSel);
		}
		
		super.addEntry(dle);
		
//		if(adleFullList.size()>100)adleFullList.remove(0);
		
		requestRefreshList();
		
		return dle;
	}

//	@Override
//	private String getSelectedEntryKey() {
//		return hmKeyValueTmp.get(formatEntryKey(getSelectedEntryValue()));
//	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(GuiGlobals.getInstance()==null)return false;
		
		return super.initCheckPrerequisites();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,scfAddEntry,"[strText] [strParenUId]")){
			String strText = cd.getCurrentCommandLine().paramString(1);
			String strParentUId = cd.getCurrentCommandLine().paramString(2);
			
			DialogListEntryData<T> dleParent = null;
			for(DialogListEntryData<T> dle:getCompleteEntriesListCopy()){
				if(dle.getUId().equalsIgnoreCase(strParentUId)){
					dleParent=dle;
					break;
				}
			}
			
			DialogListEntryData<T> dleNew = addEntryQuick(strText);
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
	public boolean execTextDoubleClickActionFor(DialogListEntryData<T> data) {
		if(isOptionSelectionMode())throw new PrerequisitesNotMetException("Option mode should not reach this method.");
		
		actionCustomAtEntry(data);
//		openModalDialog(EDiag.Cfg.toString(), data, (T)cmdCfg);
//		data.getActionTextDoubleClick().execute(null);
		
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
		
		if(cfg.bPrepareTestData){
			if(!prepareTestData())return false;
		}
		
		return true;
	}
	
	@Override
	public Vector3f getMainSize() {
		return getContainerMain().getPreferredSize();
	}
}
