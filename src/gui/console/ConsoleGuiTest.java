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

package gui.console;

import gui.console.ReflexFill.IReflexFillCfgVariant;
import gui.console.ReflexFill.ReflexFillCfg;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleGuiTest extends SimpleApplication {
	protected ConsoleGuiCustomState	cgsCustomizedState;	
	protected boolean bHideSettings=true; 
	
	public boolean endUserCustomMethod(Integer i){
		cgsCustomizedState.dumpSubEntry("Shhh.. "+i+" end user(s) working!");
		cgsCustomizedState.dumpSubEntry("CommandTest1: "+cgsCustomizedState.sfTestCommandAutoFillVariant1);
		cgsCustomizedState.dumpSubEntry("CommandTest2: "+cgsCustomizedState.testCommandAutoFillPrefixLessVariant2);
		if(ReflexFill.isbUseDefaultCfgIfMissing()){
			cgsCustomizedState.dumpSubEntry("CommandTest3: "+cgsCustomizedState.testCommandAutoFillPrefixLessVariantDefaulted3);
		}
		return true;
	}
	
	class ConsoleGuiCustomState extends ConsoleGuiLemurState{
//		private final String strFinalFieldCodePrefix="CMD_";
		private final String strFieldCodePrefix="sf";
		private final String strFieldCodePrefixLess = "VariantAsPrefixLess";
		
		public final StringField CMD_END_USER_COMMAND_TEST = new StringField(this,cc.strFinalCmdCodePrefix);
		private StringField sfTestCommandAutoFillVariant1 = new StringField(this,strFieldCodePrefix);
		private StringField testCommandAutoFillPrefixLessVariant2 = new StringField(this,strFieldCodePrefixLess);
		private StringField testCommandAutoFillPrefixLessVariantDefaulted3 = new StringField(this,null);
		
		public ConsoleGuiCustomState() {
			super(KeyInput.KEY_F10, null);
			
			/**
			 *  This allows test3 at endUserCustomMethod() to work.
			 */
			ReflexFill.setbUseDefaultCfgIfMissing(true);
		}
		
		@Override
		protected boolean executePreparedCommand() {
			boolean bOk = false;
			
			if(cc.checkCmdValidity(CMD_END_USER_COMMAND_TEST,"[iHowMany] users working")){
				bOk = endUserCustomMethod(paramInt(1));
			}else{
				return super.executePreparedCommand();
			}
			
			return bOk;
		}
		
		@Override
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
			ReflexFillCfg rfcfg = null;
			
			if(rfcv.getClass().isAssignableFrom(StringField.class)){
				if(strFieldCodePrefix.equals(rfcv.getCodePrefixVariant())){
					rfcfg = new ReflexFillCfg();
					rfcfg.strCommandPrefix = "Niceprefix";
					rfcfg.strCommandSuffix = "Nicesuffix";
					rfcfg.bFirstLetterUpperCase = true;
				}else
				if(strFieldCodePrefixLess.equals(rfcv.getCodePrefixVariant())){
					rfcfg = new ReflexFillCfg();
					rfcfg.strCodingStyleFieldNamePrefix=null;
					rfcfg.bFirstLetterUpperCase = true;
				}
			}
			
			/**
			 * If you are coding in the same style of another class,
			 * just call it!
			 * Remember to set the same variant!
			 */
//			if(rfcfg==null)rfcfg = cc.getReflexFillCfg(rfcv);
			if(rfcfg==null)rfcfg = super.getReflexFillCfg(rfcv);
			
			return rfcfg;
		}
	}
	
	@Override
	public void simpleInitApp() {
		cgsCustomizedState = new ConsoleGuiCustomState();
		getStateManager().attach(cgsCustomizedState);
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}
	
	public static void main( String... args ) {
		ConsoleGuiTest main = new ConsoleGuiTest();
		if(main.bHideSettings){
			AppSettings as = new AppSettings(true);
			as.setResizable(true);
			as.setWidth(1024);
			as.setHeight(768);
			//as.setFrameRate(30);
			main.setSettings(as);
			main.setShowSettings(false);
		}
		main.start();
	}
	
}	
