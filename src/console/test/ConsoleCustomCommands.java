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

package console.test;

import jmestates.FpsLimiterState;
import jmestates.SingleInstanceState;
import misc.Misc;
import misc.ReflexFill;

import com.jme3.input.KeyInput;

import console.ConsoleScriptCommands;
import console.gui.lemur.ConsoleGUILemurState;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleCustomCommands extends ConsoleScriptCommands{ //use ConsoleCommands to prevent scripts usage
//	public FpsLimiterState fpslState = new FpsLimiterState();
	
	public ConsoleCustomCommands(ConsoleGuiTest sapp){
		super();
		
//		ConsoleGUILemurState cs = new ConsoleGUILemurState(KeyInput.KEY_F10, this, sapp);
		ConsoleGUILemurState.i().configure(sapp, this, KeyInput.KEY_F10);
//		addConsoleCommandListener(cs);
//		setConsoleUI(cs);
//		csaTmp = cs;
		
//		sapp.getStateManager().attach(cs);
		
//		sapp.getStateManager().attach(fpslState);
		FpsLimiterState.i().configure(sapp, this);
		SingleInstanceState.i().configure(sapp, this, Thread.currentThread());
		
		/**
		 *  This allows test3 at endUserCustomMethod() to work.
		 */
		ReflexFill.i().setUseDefaultCfgIfMissing(true);
	}
	
	@Override
	public boolean executePreparedCommandRoot() {
		boolean bCommandWorked = false;
		
//		if(checkCmdValidity(CMD_END_USER_COMMAND_TEST,"[iHowMany] users working")){
//			bCommandWorked = sapp.endUserCustomMethod(paramInt(1));
//		}else
		if(checkCmdValidity(null,"fpsLimit","[iMaxFps]")){
			Integer iMaxFps = paramInt(1);
			if(iMaxFps!=null){
				FpsLimiterState.i().setMaxFps(iMaxFps);
				bCommandWorked=true;
			}
			dumpSubEntry("FpsLimit = "+FpsLimiterState.i().getFpsLimit());
		}else
		{
			return super.executePreparedCommandRoot();
		}
		
		return bCommandWorked;
	}
	
	@Override
	public void updateToggles() {
		if(btgFpsLimit.checkChangedAndUpdate())FpsLimiterState.i().setEnabled(btgFpsLimit.b());
		super.updateToggles();
	}
	
	@Override
	public String prepareStatsFieldText() {
		String strStatsLast = super.prepareStatsFieldText();
		
		if(EStats.TimePerFrame.b()){
			strStatsLast+=
					"Tpf"+(FpsLimiterState.i().isEnabled() ? (int)(fTPF*1000.0f) : Misc.i().fmtFloat(fTPF,6)+"s")
						+(FpsLimiterState.i().isEnabled()?
							"="+FpsLimiterState.i().getFrameDelayByCpuUsageMilis()+"+"+FpsLimiterState.i().getThreadSleepTimeMilis()+"ms"
							:"")
						+";";
		}
		
		return strStatsLast; 
	}
	
//	@Override
//	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
//		ReflexFillCfg rfcfg = null;
//		
//		if(rfcv.getClass().isAssignableFrom(StringField.class)){
//			if(strFieldCodePrefix.equals(rfcv.getCodePrefixVariant())){
//				rfcfg = new ReflexFillCfg();
//				rfcfg.strCommandPrefix = "Niceprefix";
//				rfcfg.strCommandSuffix = "Nicesuffix";
//				rfcfg.bFirstLetterUpperCase = true;
//			}else
//			if(strFieldCodePrefixLess.equals(rfcv.getCodePrefixVariant())){
//				rfcfg = new ReflexFillCfg();
//				rfcfg.strCodingStyleFieldNamePrefix=null;
//				rfcfg.bFirstLetterUpperCase = true;
//			}
//		}
//		
//		/**
//		 * If you are coding in the same style of another class,
//		 * just call it!
//		 * Remember to set the same variant!
//		 */
////		if(rfcfg==null)rfcfg = getReflexFillCfg(rfcv);
//		if(rfcfg==null)rfcfg = super.getReflexFillCfg(rfcv);
//		
//		return rfcfg;
//	}

}
