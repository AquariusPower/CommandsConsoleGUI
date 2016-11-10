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

package com.github.commandsconsolegui.cmd;

import java.util.ArrayList;
import java.util.TreeMap;

import com.github.commandsconsolegui.cmd.varfield.KeyBoundVarField;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;


/**
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
public class ManageKeyBind {
	public static final class CompositeControl extends CompositeControlAbs<ManageKeyBind>{
		private CompositeControl(ManageKeyBind cc){super(cc);};
	};protected CompositeControl ccSelf = new CompositeControl(this);
	
	private TreeMap<String,KeyBoundVarField> tmbindList = new TreeMap<String, KeyBoundVarField>(String.CASE_INSENSITIVE_ORDER);
	
	public void configure(){}
	
	public String getMappingFrom(KeyBoundVarField bind){
		String strMapping=null;
		
		if(bind.isField()){
			strMapping=bind.getUniqueCmdId();
			if(tmbindList.containsKey(strMapping)){
				KeyBoundVarField bindExisting = tmbindList.get(strMapping);
				if(bindExisting!=bind){
					throw new PrerequisitesNotMetException("conflicting key mapping ids", bindExisting, bind);
				}
			}
		}else{
			strMapping = bind.getBindCfg();
		}
		
		return strMapping;
	}
	
	public void removeKeyBind(KeyBoundVarField bind){
		String strMapping=getMappingFrom(bind);
		removeKeyBind(strMapping);
	}
	public void removeKeyBind(String strMapping){
		tmbindList.remove(strMapping);
	}
	
	public void addKeyBind(KeyBoundVarField bind){
		String strMapping=getMappingFrom(bind);
		
		tmbindList.put(strMapping,bind);
	}
	
	/**
	 * TODO rename to executeKeyBindCode
	 * @param bRun
	 * @param strId
	 */
	public void executeUserBinds(boolean bRun, String strId){
		for(KeyBoundVarField bind:tmbindList.values()){
			if(bind.checkRunCallerAssigned(bRun, strId))break;
		}
	}
	
	public ArrayList<KeyBoundVarField> getListCopy(){
		return new ArrayList<KeyBoundVarField>(tmbindList.values());
	}
}


