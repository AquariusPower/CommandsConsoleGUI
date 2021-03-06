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
package com.github.commandsconsolegui.spLemur.extras;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.WorkAroundI.BugFixBoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringVarField;
import com.github.commandsconsolegui.spLemur.extras.CellRendererDialogEntry.CellDialogEntry;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageCellRendererI implements IReflexFillCfg,IManager<CellDialogEntry>{
	private static ManageCellRendererI instance = new ManageCellRendererI();
	public static ManageCellRendererI i(){return instance;}
	
	public final StringVarField svfTreeDepthToken = new StringVarField(this, " ", null);
	public final BoolTogglerCmdField	btgShowTreeUId = new BoolTogglerCmdField(this,false).setCallNothingOnChange();
	public final BugFixBoolTogglerCmdField btgNOTWORKINGBugFixGapForListBoxSelectorArea = new BugFixBoolTogglerCmdField(this);
	
	public ManageCellRendererI() {
		DelegateManagerI.i().addManager(this,CellDialogEntry.class);
//		ManageSingleInstanceI.i().add(this);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,	IllegalAccessException {
		return fld.get(this);
	}

	@Override
	public void setFieldValue(Field fld, Object value)throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}
	
	private BfdArrayList<CellDialogEntry> a = new BfdArrayList<CellDialogEntry>(){};
	@Override
	public boolean addHandled(CellDialogEntry objNew) {
		return a.add(objNew);
	}
	
	@Override
	public ArrayList<CellDialogEntry> getHandledListCopy() {
		return a.getCopy();
	}

	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}

}