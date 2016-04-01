package console.test;

import misc.Misc;
import misc.ReflexFill;

import com.jme3.input.KeyInput;

import console.ConsoleScriptCommands;
import console.IConsoleUI;
import console.gui.ConsoleGUILemurState;
import extras.FpsLimiterState;

class ConsoleCustomCommands extends ConsoleScriptCommands{ //use ConsoleCommands to prevent scripts usage
	public FpsLimiterState fpslState = new FpsLimiterState();
	
	public ConsoleCustomCommands(ConsoleGuiTest sapp){
		super();
		
		ConsoleGUILemurState cs = new ConsoleGUILemurState(KeyInput.KEY_F10, this);
//		addConsoleCommandListener(cs);
//		setConsoleUI(cs);
//		csaTmp = cs;
		
		sapp.getStateManager().attach(cs);
		
		sapp.getStateManager().attach(fpslState);
		
		/**
		 *  This allows test3 at endUserCustomMethod() to work.
		 */
		ReflexFill.setUseDefaultCfgIfMissing(true);
	}
	
	@Override
	public boolean executePreparedCommandRoot() {
		boolean bCommandWorked = false;
		
//		if(checkCmdValidity(CMD_END_USER_COMMAND_TEST,"[iHowMany] users working")){
//			bCommandWorked = sapp.endUserCustomMethod(paramInt(1));
//		}else
		if(checkCmdValidity("fpsLimit","[iMaxFps]")){
			Integer iMaxFps = paramInt(1);
			if(iMaxFps!=null){
				fpslState.setMaxFps(iMaxFps);
				bCommandWorked=true;
			}
			dumpSubEntry("FpsLimit = "+fpslState.getFpsLimit());
		}else
		{
			return super.executePreparedCommandRoot();
		}
		
		return bCommandWorked;
	}
	
	@Override
	public void updateToggles() {
		if(btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(btgFpsLimit.b());
		super.updateToggles();
	}
	
	@Override
	public String prepareStatsFieldText() {
		String strStatsLast = super.prepareStatsFieldText();
		
		if(EStats.TimePerFrame.b){
			strStatsLast+=
					"Tpf"+(fpslState.isEnabled() ? (int)(fTPF*1000.0f) : Misc.i().fmtFloat(fTPF,6)+"s")
						+(fpslState.isEnabled()?
							"="+fpslState.getFrameDelayByCpuUsageMilis()+"+"+fpslState.getThreadSleepTimeMilis()+"ms"
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
