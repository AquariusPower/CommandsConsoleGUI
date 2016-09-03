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

package com.github.commandsconsolegui.jmegui.lemur.console;

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.dialog.LemurBasicDialogStateAbs;
import com.github.commandsconsolegui.jmegui.lemur.dialog.MaintenanceListDialogState;
import com.github.commandsconsolegui.jmegui.lemur.dialog.MaintenanceListDialogState.CfgParm;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class ConsoleVarsDialogStateI<T extends Command<Button>> extends MaintenanceListDialogState<T> {
	private static ConsoleVarsDialogStateI<Command<Button>>	instance=new ConsoleVarsDialogStateI<Command<Button>>();
	public static ConsoleVarsDialogStateI<Command<Button>> i(){return instance;}

	public static class CfgParm<T> extends MaintenanceListDialogState.CfgParm{
		public CfgParm(Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Integer iEntryHeightPixels,
				LemurDialogGUIStateAbs diagChoice, LemurDialogGUIStateAbs diagQuestion) {
			super(fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, iEntryHeightPixels, diagChoice, diagQuestion);
		}
	}
	private CfgParm<T>	cfg;
	@Override
	public ConsoleVarsDialogStateI<T> configure(ICfgParm icfg) {
		cfg = (CfgParm<T>)icfg;
		
		super.configure(cfg);
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	private void prepareListData() {
		ArrayList<VarCmdFieldAbs> avcf = VarCmdFieldAbs.getListFullCopy();
		for(VarCmdFieldAbs vcf:avcf){
//			if(vcf instanceof StringCmdField)
			String str = vcf.getVarId();
			if(str!=null){
				addEntry(new DialogListEntryData<T>().setText(str, vcf));
			}
			
		}
	}
	
	@Override
	protected boolean initAttempt() {
		if(!super.initAttempt())return false;
		
		prepareListData();
		
		return true;
	}
}
