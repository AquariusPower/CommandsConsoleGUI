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

import gui.console.ReflexFill.IReflexFillCfg;
import gui.console.ReflexFill.IReflexFillCfgVariant;
import gui.console.ReflexFill.ReflexFillCfg;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleCommands implements IReflexFillCfg{
	/**
	 * togglers
	 */
	protected BoolToggler	btgShowWarn = new BoolToggler(this,true);
	protected BoolToggler	btgShowInfo = new BoolToggler(this,true);
	protected BoolToggler	btgShowException = new BoolToggler(this,true);
	protected BoolToggler	btgEngineStatsView = new BoolToggler(this,false);
	protected BoolToggler	btgEngineStatsFps = new BoolToggler(this,false);
	// developer vars, keep together!
	protected BoolToggler	btgShowDeveloperInfo=new BoolToggler(this,false);
	protected BoolToggler	btgShowDeveloperWarn=new BoolToggler(this,false);
	protected BoolToggler	btgShowExecQueuedInfo=new BoolToggler(this,false);
	protected BoolToggler	btgShowMiliseconds=new BoolToggler(this,false);
	
	/**
	 * user can type these below at console (the actual commands are prepared by reflex)
	 */
	public final StringField CMD_ECHO = new StringField(this);
	public final StringField CMD_CLOSE_CONSOLE = new StringField(this);
	public final StringField CMD_CONSOLE_STYLE = new StringField(this);
	public final StringField CMD_HELP = new StringField(this);
	public final StringField CMD_HISTORY = new StringField(this);
	public final StringField CMD_CONSOLE_HEIGHT = new StringField(this);
	public final StringField CMD_CONSOLE_SCROLL_BOTTOM = new StringField(this);
	public final StringField CMD_FIX_LINE_WRAP = new StringField(this);
	public final StringField CMD_LINE_WRAP_AT = new StringField(this);
	public final StringField CMD_HK_TOGGLE = new StringField(this);
	public final StringField CMD_FIX_CURSOR = new StringField(this);
	
	private static ConsoleCommands i;
	public static ConsoleCommands i(){return i;}
	public ConsoleCommands(){
		i=this;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = new ReflexFillCfg();
		if(rfcv.getClass().isAssignableFrom(BoolToggler.class)){
			switch(rfcv.getReflexFillCfgVariant()){
				case 0:
					rfcfg.strCodingStyleFieldNamePrefix="btg";
					rfcfg.strCommandSuffix="Toggle";
					break;
			}
		}else
		if(rfcv.getClass().isAssignableFrom(StringField.class)){
			switch(rfcv.getReflexFillCfgVariant()){
				case 0:
					rfcfg.strCodingStyleFieldNamePrefix="CMD_";
					break;
			}
		}
		
		return rfcfg;
	}
}
