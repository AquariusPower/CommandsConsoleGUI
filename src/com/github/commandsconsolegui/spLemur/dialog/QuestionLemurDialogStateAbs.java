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

import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class QuestionLemurDialogStateAbs<T extends Command<Button>, THIS extends QuestionLemurDialogStateAbs<T,THIS>> extends BasicLemurDialogStateAbs<T,THIS> {
	private DialogListEntryData	dledYes;
	private DialogListEntryData	dledNo;
	
	public static class CfgParm extends BasicLemurDialogStateAbs.CfgParm{
		public CfgParm(
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier) {
			super(fDialogWidthPercentOfAppWindow,
					fDialogHeightPercentOfAppWindow, fInfoHeightPercentOfDialog,
					fEntryHeightMultiplier);
//			super.setUIId(QuestionDialogStateAbs.class.getSimpleName());
		}
	}
	private CfgParm	cfg;
	@Override
	public THIS configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		super.configure(cfg);
		storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	@Override
	protected boolean initAttempt() {
		super.setOptionChoiceSelectionMode(true);
		if(!super.initAttempt())return false;
		
		btgSortListEntries.setObjectRawValue(false);
		
		dledYes = addEntryQuick("[ yes    ]");
		dledNo  = addEntryQuick("[     no ]");
		
		return true;
	}
	
	@Override
	protected boolean initGUI() {
		getCellRenderer().setCellHeightMult(2f); //before super call
		if(!super.initGUI())return false;
		
		return true;
	}
	
	public boolean isYes(DialogListEntryData dled){
		return dled==dledYes;
	}
	
	public boolean isNo(DialogListEntryData dled){
		return dled==dledNo;
	}

//	@Override
//	protected THIS getThis() {
//		return (THIS)this;
//	}
	
	@Override
	protected void enableSuccess() {
		super.enableSuccess();
		selectEntry(dledNo);
	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=QuestionLemurDialogStateAbs.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=QuestionLemurDialogStateAbs.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
}

