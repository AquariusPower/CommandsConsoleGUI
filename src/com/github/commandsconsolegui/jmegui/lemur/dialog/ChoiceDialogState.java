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

import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * TODO CfgChoiceDialogState
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ChoiceDialogState<T extends Command<Button>> extends LemurBasicDialogStateAbs<T,ChoiceDialogState<T>> {
	public static class CfgParm extends LemurBasicDialogStateAbs.CfgParm{
		public CfgParm(
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Integer iEntryHeightPixels) {
			super(fDialogWidthPercentOfAppWindow,
					fDialogHeightPercentOfAppWindow, fInfoHeightPercentOfDialog,
					iEntryHeightPixels);
			super.setUIId(ChoiceDialogState.class.getSimpleName());
		}
	}
	private CfgParm	cfg;
	@Override
	public ChoiceDialogState<T> configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		super.configure(cfg);
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	protected boolean initAttempt() {
		super.setOptionChoiceSelectionMode(true);
		
		return super.initAttempt();
	}
	
	@Override
	public boolean prepareTestData(){
		for(int i=0;i<10;i++)addEntryQuick(null); //some test data
		return true;
	}

	@Override
	protected ChoiceDialogState<T> getThis() {
		return this;
	}
}
