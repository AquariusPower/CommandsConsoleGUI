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

package com.github.commandsconsolegui.jmegui;

import java.util.TreeMap;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.GlobalRootNodeI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class AudioUII implements IReflexFillCfg, IConsoleCommandListener {
	private static AudioUII instance = new AudioUII();
	public static AudioUII i(){return instance;}
	
	TreeMap<String,AudioNode> tmAudio = new TreeMap<String,AudioNode>();

	public final BoolTogglerCmdField btgMute = new BoolTogglerCmdField(this,false).setCallNothingOnChange();
	
	static String strBasePath="Sounds/Effects/UI/";
	public static enum EAudio{
		SubmitCfgChoice	(strBasePath+"220172__gameaudio__flourish-spacey-2.mono.ogg"),
		SubmitSelection	(strBasePath+"220183__gameaudio__click-casual.mono.ogg"),
		HoverActivator	(strBasePath+"220197__gameaudio__click-basic.mono.ogg" ),
		;
		
		private String	strFile;

		EAudio(String strFile){
			this.strFile=strFile;
		}
		
		public String getFile(){return strFile;} 
	}
	
	public void playAudio(EAudio ea) {
		playAudio(ea.toString());
	}
	
	public void playAudio(String strAudioId) {
		if(btgMute.b())return;
		
		AudioNode an = tmAudio.get(strAudioId);
		
		if(an==null){
			try{
				an = setAudio(strAudioId, EAudio.valueOf(strAudioId).getFile());
			}catch(IllegalArgumentException e){}
		}
		
		if(an!=null){
			an.playInstance();
		}else{
			GlobalCommandsDelegatorI.i().dumpWarnEntry("audio not set", strAudioId);
		}
	}
	
	public AudioNode setAudio(String strAudioId, String strFile){
		AudioNode an = tmAudio.get(strAudioId);
		
		if(an!=null){
			MsgI.i().warn("audio already set",strAudioId, an.getKey().getName(), strFile);
			GlobalRootNodeI.i().detachChild(an);
		}
		
		for(int i=1;i<=2;i++){ //1st is try
			try{
				an = new AudioNode(GlobalAppRefI.i().getAssetManager(), strFile,	DataType.Buffer);
				
				tmAudio.put(strAudioId,an);
				
				GlobalRootNodeI.i().attachChild(an);
			}catch(AssetNotFoundException ex){
				switch(i){
					case 1:
						GlobalAppRefI.i().getAssetManager().registerLocator("./assets/", FileLocator.class);
						break;
					case 2:
						GlobalCommandsDelegatorI.i().dumpExceptionEntry(ex, strAudioId, strFile);
						break;
				}
			}
		}
		
		return an;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator ccRequester) {
		return ECmdReturnStatus.NotFound;
	}

	public void configure() {
		// this will register the bool togglers commands too. 
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
	}

}
