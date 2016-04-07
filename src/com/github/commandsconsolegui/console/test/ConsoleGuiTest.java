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

package com.github.commandsconsolegui.console.test;

import com.github.commandsconsolegui.console.ConsoleCommands;
import com.github.commandsconsolegui.console.ConsoleCommands.ECmdReturnStatus;
import com.github.commandsconsolegui.console.IConsoleCommandListener;
import com.github.commandsconsolegui.jmestates.SingleInstance;
import com.github.commandsconsolegui.misc.ReflexFill;
import com.github.commandsconsolegui.misc.ReflexFill.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFill.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFill.ReflexFillCfg;
import com.github.commandsconsolegui.misc.StringFieldCmd;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleGuiTest extends SimpleApplication implements IConsoleCommandListener, IReflexFillCfg{
	private static ConsoleGuiTest instance = new ConsoleGuiTest();
	public static ConsoleGuiTest i(){return instance;}
	
	protected ConsoleCustomCommands	cc;	
	protected boolean bHideSettings=true; 
	
	//private final String strFinalFieldCodePrefix="CMD_";
	private final String strFieldCodePrefix="sf";
	private final String strFieldCodePrefixLess = "VariantAsPrefixLess";
	
	public final StringFieldCmd CMD_END_USER_COMMAND_TEST = new StringFieldCmd(this,cc.strFinalCmdCodePrefix);
	private StringFieldCmd sfTestCommandAutoFillVariant1 = new StringFieldCmd(this,strFieldCodePrefix);
	private StringFieldCmd testCommandAutoFillPrefixLessVariant2 = new StringFieldCmd(this,strFieldCodePrefixLess);
	private StringFieldCmd testCommandAutoFillPrefixLessVariantDefaulted3 = new StringFieldCmd(this,null);
	
	public boolean endUserCustomMethod(Integer i){
		cc.dumpSubEntry("Shhh.. "+i+" end user(s) working!");
		cc.dumpSubEntry("CommandTest1: "+sfTestCommandAutoFillVariant1);
		cc.dumpSubEntry("CommandTest2: "+testCommandAutoFillPrefixLessVariant2);
		if(ReflexFill.i().isbUseDefaultCfgIfMissing()){
			cc.dumpSubEntry("CommandTest3: "+testCommandAutoFillPrefixLessVariantDefaulted3);
		}
		return true;
	}
	
	@Override
	public void simpleInitApp() {
		cc = new ConsoleCustomCommands(this);
		cc.addConsoleCommandListener(this);
		
//		SingleInstanceState.i().configureBeforeInitializing(this,true);
		SingleInstance.i().configureRequiredAtApplicationInitialization();//cc);
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}
	
	public static void main( String... args ) {
		ConsoleGuiTest main = ConsoleGuiTest.i();
		main.configure();
		
		if(main.bHideSettings){
			AppSettings as = new AppSettings(true);
			as.setResizable(true);
			as.setWidth(1024);
			as.setHeight(768);
			//as.setFrameRate(30);
			main.setSettings(as);
			main.setShowSettings(false);
		}
		
		SingleInstance.i().configureOptionalAtMainMethod();
		
		main.start();
	}

	private void configure() {
		ReflexFill.i().assertReflexFillFieldsForOwner(this);
	}

	@Override
	public ECmdReturnStatus executePreparedCommand(ConsoleCommands	cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,CMD_END_USER_COMMAND_TEST,"[iHowMany] users working?")){
			bCommandWorked = endUserCustomMethod(cc.paramInt(1));
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
			
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(StringFieldCmd.class)){
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
//		if(rfcfg==null)rfcfg = getReflexFillCfg(rfcv);
		if(rfcfg==null)rfcfg = cc.getReflexFillCfg(rfcv);
		
		return rfcfg;
	}
}	
