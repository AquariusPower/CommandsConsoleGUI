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
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;

/**
 * This interface exist to make it easy to track all bug fix implementations.
 * All bugfixes must be disablable dynamically, therefore, if some library dependency
 * is updated, that bug may have been fixed and the bugfix can be disabled.
 * 
 * "Bug fix" is some workaround (be hackish or not) that make things work 
 * as expected when they aren't.
 * 
 * Bug fixes should be a temporary code.
 * 
 * Implement bugfixes identifiers as {@link BoolTogglerCmdField}, so they become user console commands.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
@Deprecated
public interface IWorkAroundBugFix {
//	/**
//	 * Simple template. Self note: try to not change it...
//	 */
//	public static class WorkAroundCodeTemplate implements IWorkAroundBugFix, IReflexFillCfg{
//		BoolTogglerCmdField btgBugFixCustom = new BoolTogglerCmdField(this,false);
//		@Override
//		public <BFR> BFR bugFix(Class<BFR> clReturnType,
//				BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId,
//				Object... aobjCustomParams
//		) {
//			if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
//			
//			boolean bFixed = false;
//			Object objRet = null;
//			
//			if(btgBugFixCustom.isEqualToAndEnabled(btgBugFixId)){
//				Float f = MiscI.i().getParamFromArray(Float.class, aobjCustomParams, 0);
//				String str = MiscI.i().getParamFromArray(String.class, aobjCustomParams, 1);
//				
//				//DO SPECIFIC BUGFIX HERE
//				
//				bFixed=true;
//			}
//			
//			return MiscI.i().bugFixRet(clReturnType,bFixed, objRet, aobjCustomParams);
//		}
//		
//		@Override
//		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {return null;}
//		
//	}
	
	/**
	 * BFR is bugfix return (type)
	 * Collect param ex.: Float spt = MiscI.i().getParamFromArray(Float.class, aobjCustomParams, 0);
	 * 
	 * Template at {@link WorkAroundCodeTemplate#bugFix(Class, Object, BoolTogglerCmdField, Object...)}
	 * 
	 * @param clReturnType
	 * @param btgBugFixId
	 * @param aobjCustomParams
	 * @return
	 */
	@Deprecated
	public <BFR> BFR bugFix(Class<BFR> clReturnType, BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams);
}
