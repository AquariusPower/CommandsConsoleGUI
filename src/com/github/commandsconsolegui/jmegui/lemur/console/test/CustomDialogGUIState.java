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

package com.github.commandsconsolegui.jmegui.lemur.console.test;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.GuiGlobals;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class CustomDialogGUIState<T extends Command<Button>> extends LemurDialogGUIStateAbs<T>{
	StringCmdField scfAddEntry = new StringCmdField(this,null,"[strEntryText]");
//	ArrayList<String> astr;
//	HashMap<String,String> hmKeyValueTmp = new HashMap<String,String>();
//	private Object	answerFromModal;
	
//public static class CfgParm implements ICfgParm{
//String strUIId;
//boolean bIgnorePrefixAndSuffix;
//Node nodeGUI;
//public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
//	super();
//	this.strUIId = strUIId;
//	this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
//	this.nodeGUI =nodeGUI;
//}
//}
	
	public static enum EDiag{
		Cfg,
		List,
		;
	}
	EDiag ediag = null;
	
	public CustomDialogGUIState(EDiag ediag) {
		this.ediag=ediag;
		super.bPrefixCmdWithIdToo = true;
	}
	
	public static class CfgParm extends LemurDialogGUIStateAbs.CfgParm{
		public CfgParm(boolean	bOptionSelectionMode,boolean bIgnorePrefixAndSuffix,
				Node nodeGUI, Float fDialogHeightPercentOfAppWindow,
				Float fDialogWidthPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Integer iEntryHeightPixels, BaseDialogStateAbs modalParent) {
			super(bOptionSelectionMode,null, bIgnorePrefixAndSuffix, nodeGUI,
					fDialogHeightPercentOfAppWindow, fDialogWidthPercentOfAppWindow,
					fInfoHeightPercentOfDialog, iEntryHeightPixels, modalParent);
			// TODO Auto-generated constructor stub
		}
	}
	@Override
	public CustomDialogGUIState configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
		cfg.setUIId(ediag.toString());
		
		super.configure(cfg); //params are identical
		
		/**
		 * this is just an example as state changes can be delayed
		 */
		super.setRetryDelay(500);
		super.rInit.setRetryDelay(1000); //after the generic one
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	protected String getTextInfo() {
		return super.getTextInfo()+"Custom Dialog: Test.";
	};
	
	@Override
	protected void updateList() {
		DialogListEntryData<T> dataValue = getCfgDataValueAndClearIt();
		if(dataValue!=null){
			DialogListEntryData<T> dataRef = getCfgDataRefAndClearIt();
			if(dataRef!=null){
				dataRef.setCfg(dataValue);
			}else{
				cd().dumpWarnEntry("no entry selected at "+this.getId()+" to apply modal dialog option");
			}
		}
		
//		super.updateList(adleFullList);
		
		super.updateList();
	}
	
	public DialogListEntryData<T> getDataFrom(Spatial spt){
		String strKey = DialogListEntryData.class.getName();
		Object data = spt.getUserData(strKey);
		if(data==null)throw new PrerequisitesNotMetException("missing user object "+strKey);
		return (DialogListEntryData<T>) data;
	}
	
	public class CommandCfg implements Command<Button>{
		@Override
		public void execute(Button btn) {
//			EButtonAction.valueOf(btn.getUserData(EButtonAction.class.getSimpleName()));
//			DialogListEntryData<Command<Button>> data = btn.getUserData(DialogListEntryData.class.getName());
//			CustomDialogGUIState.this.openDialog(EDiag.Cfg.toString(),getDataFrom(btn));
			CustomDialogGUIState.this.openCfgDialog(getDataFrom(btn));
		}
	}
	CommandCfg cmdCfg = new CommandCfg();
	
	public class CommandDel implements Command<Button>{
		@Override
		public void execute(Button btn) {
//			DialogListEntryData<Command<Button>> data = btn.getUserData(DialogListEntryData.class.getName());
//			DialogListEntryData<T> data = getDataFrom(btn);
//			if(!CustomDialogGUIState.this.adleFullList.remove(data)){
//				throw new PrerequisitesNotMetException("missing data at list", getDataFrom(btn));
//			}
			CustomDialogGUIState.this.removeEntry(getDataFrom(btn));
		}
	}
	CommandDel cmdDel = new CommandDel();
	
	public void openCfgDialog(DialogListEntryData<T> data){
		CustomDialogGUIState.this.openDialog(EDiag.Cfg.toString(),data);
	}
	
	public class CommandSel implements Command<Button>{
		@Override
		public void execute(Button btn) {
			CustomDialogGUIState.this.selectAndChoseOption(getDataFrom(btn));
		}
	}
	CommandSel cmdSel = new CommandSel();
	
	public DialogListEntryData<T> addEntryQuick(String strText){
		DialogListEntryData<T> dle = new DialogListEntryData<T>();
		if(strText==null){
			strText=this.getId()+": New test entry: "
//				+MiscI.i().getDateTimeForFilename(true)
//				+", "
				+System.nanoTime();
		}
		dle.setText(strText,(T)cmdCfg);
		
		if(bOptionSelectionMode){
			dle.addCustomButtonAction("<-",(T)cmdSel);
		}else{
			/**
			 * this order matters
			 */
			dle.addCustomButtonAction("Cfg",(T)cmdCfg);
			dle.addCustomButtonAction("X",(T)cmdDel);
		}
		
		super.addEntry(dle);
		
//		if(adleFullList.size()>100)adleFullList.remove(0);
		
		requestRefreshList();
		
		return dle;
	}

//	@Override
//	protected String getSelectedEntryKey() {
//		return hmKeyValueTmp.get(formatEntryKey(getSelectedEntryValue()));
//	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(GuiGlobals.getInstance()==null)return false;
		
		return super.initCheckPrerequisites();
	}
	
	@Override
	protected boolean initOrUndo() {
		addEntryQuick(null);
		addEntryQuick(null);
		
		DialogListEntryData<T> dleS1 = addEntryQuick("section 1");
		addEntryQuick(null).setParent(dleS1);
		addEntryQuick(null).setParent(dleS1);
		addEntryQuick(null).setParent(dleS1);
		
		DialogListEntryData<T> dleS2 = addEntryQuick("section 2");
		addEntryQuick(null).setParent(dleS2);
		addEntryQuick(null).setParent(dleS2);
		DialogListEntryData<T> dleS21 = addEntryQuick("section 2.1").setParent(dleS2);
		addEntryQuick(null).setParent(dleS21);
		addEntryQuick(null).setParent(dleS21);
		addEntryQuick(null).setParent(dleS21);
		
		addEntryQuick("S2 child").setParent(dleS2); //ok, will be placed properly
		
		addEntryQuick("S1 child").setParent(dleS1); //out of order for test
		addEntryQuick("S21 child").setParent(dleS21); //out of order for test
		
//		for(int i=0;i<10;i++)addEntry(null); //some test data
		
		return super.initOrUndo();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,scfAddEntry,null)){
			String strText = cd.getCurrentCommandLine().paramString(1);
			addEntryQuick(strText);
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cd);
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}

	@Override
	public boolean execTextDoubleClickActionFor(DialogListEntryData<T> data) {
		if(isOptionSelectionMode())throw new PrerequisitesNotMetException("Option mode should not reach this method.");
		
		openCfgDialog(data);
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
}
