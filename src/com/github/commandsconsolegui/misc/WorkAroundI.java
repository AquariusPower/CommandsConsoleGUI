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

package com.github.commandsconsolegui.misc;

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * This exists to make it easy to track all bug fix implementations.
 * All bugfixes must be disablable dynamically, therefore, if some library dependency
 * is updated, that bug may have been fixed and the bugfix can be disabled.
 * Every bugfix must also be initialized as disabled, only the end user can enable them.
 * 
 * "Bug fix" is some workaround (be hackish or not) that make things work 
 * as expected when they aren't (for any reason, even developer lack of knowledge :)).
 * 
 * Bug fixes should be a temporary code.
 * 
 * Implement bugfixes identifiers as {@link BugFixBoolTogglerCmdField}, 
 * so they become toggable user console commands.
 * The fixer code is a caller set at it.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class WorkAroundI {
	public static final class CompositeControl extends CompositeControlAbs<WorkAroundI>{
		private CompositeControl(WorkAroundI casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	private static WorkAroundI	instance=new WorkAroundI();
	public static WorkAroundI i(){return instance;}
	
	public void bugFix(BugFixBoolTogglerCmdField btgBugFixId, Object... aobjCustomParams) {
		bugFix(btgBugFixId,null,null,aobjCustomParams);
	}
	
	/**
	 * BFR is bugfix return (type)
	 * 
	 * @param clReturnType
	 * @param objRetIfBugFixBoolDisabled
	 * @param btgBugFixId
	 * @param aobjCustomParams
	 * @return
	 */
	public <BFR> BFR bugFix(BugFixBoolTogglerCmdField btgBugFixId, Class<BFR> clReturnType,
			BFR objRetIfBugFixBoolDisabled, Object... aobjCustomParams
	) {
		if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
		
		if(btgBugFixId.getValueDefault()){
			throw new PrerequisitesNotMetException("default bugfix value must be 'false', let end user decide to enable it!", btgBugFixId);
		}
		
		if(btgBugFixId.getCallerAssignedForMaintenance(ccSelf).isAllowQueue()){
			throw new PrerequisitesNotMetException("bugfixer's main caller cannot be queued directly!", btgBugFixId);
		}
		
		boolean bFixed = false;
		Object objRet = null;
		
		if(btgBugFixId.b()){
//		Float f = MiscI.i().getParamFromArray(Float.class, aobjCustomParams, 0);
//		String str = MiscI.i().getParamFromArray(String.class, aobjCustomParams, 1);
			bFixed=btgBugFixId.callerAssignedRunNow(aobjCustomParams);
			
			if(bFixed){
				objRet=btgBugFixId.getCallerAssignedForMaintenance(ccSelf).getReturnValue();
			}
		}
		
		if(!bFixed){
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("cant bugfix this way...", 
				clReturnType, objRetIfBugFixBoolDisabled, btgBugFixId, aobjCustomParams);
//			throw new PrerequisitesNotMetException("cant bugfix this way...",aobjCustomParams);
		}
		
		return (BFR)objRet;
	}
	
//	public void prepareBugFix(BoolTogglerCmdField btgBugFix){
//		btgBugFix.getCallerAssignedForMaintenance(ccSelf).setQueueDenied();
//	}
	
	public static class BugFixBoolTogglerCmdField extends BoolTogglerCmdField<BugFixBoolTogglerCmdField>{

		public BugFixBoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis,
				boolean bInitialValue, String strHelp) {
			super(rfcfgOwnerUseThis, bInitialValue, strHelp);
			// TODO Auto-generated constructor stub
		}

		public BugFixBoolTogglerCmdField(IReflexFillCfg rfcfgOwnerUseThis,
				boolean bInitValue) {
			super(rfcfgOwnerUseThis, bInitValue);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public BugFixBoolTogglerCmdField setCallerAssigned(CallableX caller) {
			caller.setQueueDenied();
//			WorkAroundI.i().prepareBugFix(this);
			
			super.setCallerAssigned(caller);
			
			return getThis();
		}
		
//		@Override
//		public BoolTogglerCmdField setHelp(String strHelp) {
//			super.setHelp(strHelp);
//			return getThis();
//		}
		
		@Override
		protected BugFixBoolTogglerCmdField getThis() {
			return this;
		}
	}
}
