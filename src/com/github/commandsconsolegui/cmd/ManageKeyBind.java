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
import java.util.HashMap;
import java.util.TreeMap;

import com.github.commandsconsolegui.cmd.varfield.KeyBoundVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.globals.GlobalManageKeyCodeI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.KeyBind;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;


/**
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
public abstract class ManageKeyBind {
	public static final class CompositeControl extends CompositeControlAbs<ManageKeyBind>{
		private CompositeControl(ManageKeyBind cc){super(cc);};
	};protected CompositeControl ccSelf = new CompositeControl(this);
	
	private TreeMap<String,KeyBoundVarField> tmbindList = new TreeMap<String, KeyBoundVarField>(String.CASE_INSENSITIVE_ORDER);
	
	public void configure(){}
	
	public String getMappingFrom(KeyBoundVarField bind){
		String strMapping=null;
		
		if(bind.isField()){
			strMapping=bind.getUniqueVarId(true);
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
	public void removeKeyBind(String strBindCfg){
//		tmbindList.remove(KeyBoundVarField.parseToBoundCfg(strBindCfg,false).getBindCfg());
		tmbindList.remove(new KeyBind().setFromKeyCfg(strBindCfg).getBindCfg());
	}
	
	public void addKeyBind(KeyBoundVarField bind){
		String strMapping=getMappingFrom(bind);
		
		tmbindList.put(strMapping,bind);
	}
	
//	/**
//	 * TODO rename to executeKeyBindCode
//	 * @param bRun
//	 * @param strId
//	 */
//	public void executeUserBinds(boolean bRun, String strId){
//		for(KeyBoundVarField bind:tmbindList.values()){
//			if(bind.checkRunCallerAssigned(bRun, strId))break;
//		}
//	}
	
	private HashMap<Integer,ArrayList<KeyBoundVarField>> hmKeyCodeVsActivatedBind = new HashMap<Integer, ArrayList<KeyBoundVarField>>();

	private KeyBoundVarField	bindCaptureToTarget;
	
	private KeyBind kbCaptured;
	
	public void update(float fTpf){
		/**
		 * this will hold the bound keys execution
		 */
		if(bindCaptureToTarget!=null){
//			if(kbCaptured==null)kbCaptured = new KeyBind();
			
			kbCaptured=GlobalManageKeyCodeI.i().getPressedKeysAsKeyBind();
			
//			if(kbCaptured.getActionKey()!=null){
			if(kbCaptured!=null){
				bindCaptureToTarget.setValue(kbCaptured);
				MsgI.i().info(
					"captured key bind "+kbCaptured.getBindCfg()
					+" for "+bindCaptureToTarget.getKeyBindRunCommand(),
					bindCaptureToTarget,kbCaptured,this);
				bindCaptureToTarget=null;
			}
			
			return;
		}
		
		
		/**
		 * fill all binds for each action keycode to check which will win.
		 */
		hmKeyCodeVsActivatedBind.clear();
		for(KeyBoundVarField bind:tmbindList.values()){
			if(bind.getValue().isActivated()){
				Integer iKeyCode = bind.getValue().getActionKey().getKeyCode();
				
				ArrayList<KeyBoundVarField> abindForActKeyCode = hmKeyCodeVsActivatedBind.get(iKeyCode);
				if(abindForActKeyCode==null){
					abindForActKeyCode=new ArrayList<KeyBoundVarField>();
					hmKeyCodeVsActivatedBind.put(iKeyCode, abindForActKeyCode);
				}
				
				abindForActKeyCode.add(bind);
			}else{
				if(bind.isWaitingDeactivation()){
					bind.runIfActivatedOrResetIfDeactivated();
				}
			}
		}
		
		/**
		 * only the activated bind with most modifiers will be run! 
		 */
		for(ArrayList<KeyBoundVarField> abindForActKeyCode:hmKeyCodeVsActivatedBind.values()){
			KeyBoundVarField bindWin=abindForActKeyCode.get(0);
			
			for(KeyBoundVarField bind:abindForActKeyCode){
				if(bindWin.getValue().getKeyModListSize() < bind.getValue().getKeyModListSize()){
					bindWin=bind;
				}
			}
			
			bindWin.runIfActivatedOrResetIfDeactivated();
		}
		
	}
	
//	public void refreshPressedState(int iKeyCode, boolean bPressed){
//		for(KeyBoundVarField bind:tmbindList.values()){
//			bind.getValue().applyPressedState(iKeyCode, bPressed);
//		}
//	}
	
	public ArrayList<KeyBoundVarField> getListCopy(){
		return new ArrayList<KeyBoundVarField>(tmbindList.values());
	}

	public ArrayList<String> getReportAsCommands(StringCmdField scfBindKey, boolean bOnlyUserCustomOnes) {
		ArrayList<String> astr = new ArrayList<String>();
		for(KeyBoundVarField bind:getListCopy()){
			if(bOnlyUserCustomOnes && bind.isField())continue;
			
			String strMapping = getMappingFrom(bind);
			if(strMapping.equals(bind.getBindCfg())){
				strMapping="";
			}else{
				strMapping="#ActionMappingId("+strMapping+")";
			}
			
			String strUserCmd = bind.getUserCommand();
			if(strUserCmd==null){
				strUserCmd="";
			}else{
				strUserCmd+=" ";
			}
			
			astr.add(GlobalCommandsDelegatorI.i().getCommandPrefixStr()+scfBindKey.getSimpleId()+" "
				+bind.getBindCfg()+" "
				+strUserCmd
				+strMapping);
		}
		return astr;
	}

	public void captureAndSetKeyBindAt(KeyBoundVarField bindTarget) {
		this.bindCaptureToTarget=bindTarget;
		MsgI.i().info("For unconventional (more complex) key bindings, use the console command.", bindTarget); 
//		bindTarget.setValue(captureKeyBind(bindTarget));
	}

//	protected abstract KeyBind captureKeyBind(KeyBoundVarField bindTarget);
}


