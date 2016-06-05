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

import java.util.ArrayList;

import com.github.commandsconsolegui.jmegui.ConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.MiscI;
import com.jme3.scene.Node;
import com.simsilica.lemur.GuiGlobals;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class CustomDialogGUIState extends LemurDialogGUIStateAbs<String>{
	ArrayList<String> astr;
	
	public static class CfgParm implements ICfgParm{
		String strUIId;
		boolean bIgnorePrefixAndSuffix;
		Node nodeGUI;
		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
			super();
			this.strUIId = strUIId;
			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
			this.nodeGUI =nodeGUI;
		}
	}
	@Override
	public CustomDialogGUIState configure(ICfgParm icfg) {
//	public void configure(String strUIId, boolean bIgnorePrefixAndSuffix) {
		CfgParm cfg = (CfgParm)icfg;
		astr = new ArrayList<String>();
		super.configure(new LemurDialogGUIStateAbs.CfgParm(
			cfg.strUIId, cfg.bIgnorePrefixAndSuffix, cfg.nodeGUI));
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	protected void updateTextInfo() {
		lblTextInfo.setText("Info: Type a list filter at input text area and hit Enter.");
//		super.updateTextInfo();
	}
	
	@Override
	protected void updateList() {
		astr.add("New test entry: "+MiscI.i().getDateTimeForFilename(true));
		if(astr.size()>100)astr.remove(0);
		updateList(astr);
	}

//	@Override
//	protected boolean cleanupValidating() {
//		astr.clear();
//		astr=null;
//		return super.cleanupValidating();
//	}

	@Override
	protected boolean initCheckPrerequisites() {
		if(GuiGlobals.getInstance()==null)return false;
		return super.initCheckPrerequisites();
	}
	
}
