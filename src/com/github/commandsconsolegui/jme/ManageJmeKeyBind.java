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

package com.github.commandsconsolegui.jme;

import java.util.ArrayList;
import java.util.TreeMap;

import com.github.commandsconsolegui.cmd.ManageKeyBind;
import com.github.commandsconsolegui.cmd.varfield.KeyBoundVarField;
import com.github.commandsconsolegui.globals.jme.GlobalAppRefI;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.jme3.input.controls.ActionListener;


/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageJmeKeyBind extends ManageKeyBind {
	private ActionListener	alGeneralJmeListener;
	
	@Override
	public void configure(){
		alGeneralJmeListener = new ActionListener() {
			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				if(!isPressed)return;
				
//				// all field JME binds go here
//				if(bindToggleConsole.checkRunCallerAssigned(isPressed,name))return;
				
				executeUserBinds(isPressed,name);
			}
		};
		
	}
	
	@Override
	public void removeKeyBind(String strMapping){
		if(GlobalAppRefI.i().getInputManager().hasMapping(strMapping)){
			GlobalAppRefI.i().getInputManager().deleteMapping(strMapping);
		}
		
		super.removeKeyBind(strMapping);
	}
	
	@Override
	public void addKeyBind(KeyBoundVarField bind){
		super.addKeyBind(bind);
			
		String strMapping=getMappingFrom(bind);
		
		if(GlobalAppRefI.i().getInputManager().hasMapping(strMapping)){
			GlobalAppRefI.i().getInputManager().deleteMapping(strMapping);
		}
		
		GlobalAppRefI.i().getInputManager().addMapping(strMapping,
			MiscJmeI.i().asTriggerArray(bind));
		
		GlobalAppRefI.i().getInputManager().addListener(alGeneralJmeListener, strMapping);
	}
	
}
