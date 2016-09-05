/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower>
	
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.GlobalRootNodeI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.Application;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower>
 *
 */
public class AudioUII extends ConditionalStateAbs implements IReflexFillCfg, IConsoleCommandListener {
	private static AudioUII instance = new AudioUII();
	public static AudioUII i(){return instance;}
	
	TreeMap<String,AudioNode> tmAudio = new TreeMap<String,AudioNode>();

	public final BoolTogglerCmdField btgMute = new BoolTogglerCmdField(this,false).setCallNothingOnChange();
	public final FloatDoubleVarField fdvMasterVolumeGain = new FloatDoubleVarField(this,1.0,"").setMin(0.0).setMax(1.0);

	private ArrayList<Class<?>>	aclassUserActionStackList = new ArrayList<Class<?>>(); 
	
	public enum EUserData{
		strAudioId,
		strFileName,
		bMute,
		;
	}
	
	static String strBasePath="Sounds/Effects/UI/13940__gameaudio__game-audio-ui-sfx/";
	public static enum EAudio{
		SubmitSelection			(strBasePath+"220183__gameaudio__click-casual.mono.ogg"),
		
		ReturnChosen				(strBasePath+"220200__gameaudio__basic-click-wooden.mono.ogg"),
//		ReturnChosen				(strBasePath+"220172__gameaudio__flourish-spacey-2.mono.ogg"),
		ReturnNothing				(strBasePath+"220210__gameaudio__bonk-click-w-deny-feel.mono.ogg"),
		
		Question						(strBasePath+"220187__gameaudio__loose-deny-casual-1.mono.ogg"),
		
		HoverOverActivators	(strBasePath+"220189__gameaudio__blip-squeak.cut.mono.ogg" ),
		
		SelectEntry					(strBasePath+"220197__gameaudio__click-basic.mono.ogg" ),
		DisplayEntryEffect	(strBasePath+"220168__gameaudio__button-spacey-confirm.mono.ogg"),
		
		RemoveEntry					(strBasePath+"220177__gameaudio__quick-ui-or-event-deep.mono.ogg"),
		RemoveSubTreeEntry	(strBasePath+"220205__gameaudio__teleport-darker.mono.ogg"),
		
		ExpandSubTree				(strBasePath+"220195__gameaudio__click-wooden-1.mono.ogg"), 
		ShrinkSubTree				(strBasePath+"220194__gameaudio__click-heavy.mono.ogg"), 
		
		OpenConsole					(strBasePath+"220202__gameaudio__teleport-casual.mono.ogg"),
		CloseConsole				(strBasePath+"220203__gameaudio__casual-death-loose.mono.ogg"),
		
		Failure							(strBasePath+"220167__gameaudio__button-deny-spacey.mono.ogg"),
		;
		
		CfgAudio cfga;
		
		EAudio(String strFile){
			cfga = new CfgAudio(this.toString());
			cfga.setFile(strFile);
		}
		
		/**
		 * cfg() not getCfg() in a sense that the cfg can be directly modified, is not a safe copy.
		 * @return
		 */
		public CfgAudio cfg(){
			return cfga;
		}
		
	}
	
	public static class CfgAudio implements IReflexFillCfg{
		private String	strUId;
		private String	strFile;
		private FloatDoubleVarField fdvVolumeGain = new FloatDoubleVarField(this,1.0,"").setMin(0.0).setMax(1.0);
		
		public CfgAudio(String strUId) {
			this.strUId=strUId;
		}
		public String getFile(){return strFile;}
		public void setFile(String strFile){this.strFile=strFile;}
		
		@Override
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
			ReflexFillCfg rfcfg = GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
			if(rfcfg==null)rfcfg=new ReflexFillCfg(rfcv);
			rfcfg.setPrefixCustomId(strUId);
			return rfcfg;
		} 
	}
	
	public boolean isUserActionStack(){
		for(StackTraceElement ste:Thread.currentThread().getStackTrace()){
			for(Class<?> cl:aclassUserActionStackList){
				if(
						ste.getClassName().equals(cl.getName())
						||
						ste.getClassName().startsWith(cl.getName()+"$")
				){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean playOnUserAction(EAudio ea) {
		return playOnUserAction(ea.toString());
	}
	public boolean playOnUserAction(String strAudioId) {
		if(isUserActionStack()){
			return play(strAudioId);
		}
		return false;
	}
	
	public boolean play(EAudio ea) {
		return play(ea.toString());
	}
	public boolean play(String strAudioId) {
		if(btgMute.b())return false;
		
		AudioNode an = tmAudio.get(strAudioId);
		
		CfgAudio cfg = EAudio.valueOf(strAudioId).cfg();
		
		if(an==null){
			try{
				an = setAudio(strAudioId, cfg.getFile());
			}catch(IllegalArgumentException e){}
		}
		
		if(an!=null){
			if(isMute(an))return false;
			
			an.setVolume(cfg.fdvVolumeGain.f() * fdvMasterVolumeGain.f());
			an.playInstance();
			return true;
		}else{
			GlobalCommandsDelegatorI.i().dumpWarnEntry("audio not set", strAudioId);
		}
		
		return false;
	}
	
	private boolean isMute(AudioNode an) {
		return (Boolean)an.getUserData(EUserData.bMute.toString());
	}

	public AudioNode setAudio(String strAudioId, String strFile){
		if(strAudioId.isEmpty()){
			throw new PrerequisitesNotMetException("invalid id", strAudioId);
		}
		
		AudioNode an = tmAudio.get(strAudioId);
		
		if(!strFile.contains("/"))strFile=strBasePath+strFile;
		
		if(an!=null){
			MsgI.i().warn("audio already set",strAudioId, an, strFile);
			GlobalRootNodeI.i().detachChild(an);
		}
		
		labelRetry:for(int i=1;i<=2;i++){ //1st is try
			try{
				an = new AudioNode(GlobalAppRefI.i().getAssetManager(), strFile,	DataType.Buffer);
				an.setUserData(EUserData.strAudioId.toString(), strAudioId);
				an.setUserData(EUserData.strFileName.toString(), strFile);
				an.setUserData(EUserData.bMute.toString(), false);
				
				for(AudioNode anChkDupSndSrc:tmAudio.values()){
					if(anChkDupSndSrc.getUserData(EUserData.strFileName.toString()).equals(strFile)){
						GlobalCommandsDelegatorI.i().dumpDevWarnEntry(
							"same sound file ["+strFile+"] being used with more than one event: "
								+strAudioId+", "
								+anChkDupSndSrc.getUserData(EUserData.strAudioId.toString())
						);
					}
				}
				
				tmAudio.put(strAudioId, an);
				
				GlobalRootNodeI.i().attachChild(an);
			}catch(AssetNotFoundException ex){
				switch(i){
					case 1:
						GlobalAppRefI.i().getAssetManager().registerLocator("./assets/", FileLocator.class);
						continue labelRetry;
					case 2:
						GlobalCommandsDelegatorI.i().dumpExceptionEntry(ex, strAudioId, strFile);
						break labelRetry;
				}
			}
			
			break;
		}
		
		return an;
	}
	
	public String getFileNameFrom(AudioNode an){
		return an.getUserData(EUserData.strFileName.toString());
	}
	
	public boolean muteAudioToggle(String strAudioId) {
		AudioNode an = tmAudio.get(strAudioId);
		if(an==null)return false;
		
		Boolean bMute = an.getUserData(EUserData.bMute.toString());
		bMute=!bMute;
		an.setUserData(EUserData.bMute.toString(), bMute);
		GlobalCommandsDelegatorI.i().dumpInfoEntry("SounteMuted: "+strAudioId+" "+bMute);
		
		return true;
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
	
	public final StringCmdField scfSoundSet = new StringCmdField(this);
	public final StringCmdField scfSoundMuteToggle = new StringCmdField(this);
	public final StringCmdField scfSoundList = new StringCmdField(this);
	public final StringCmdField scfSoundPlayFile = new StringCmdField(this);
	public final StringCmdField scfSoundRefreshCache = new StringCmdField(this);
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,scfSoundRefreshCache,"mainly to let developer dinamically update sound files")){
			/**
			 * TODO this is not working because of the jme assets cache right?
			 */
			cd.dumpWarnEntry("TODO: not working yet...");
			
			tmAudio.clear(); //useless without cleaning jme sound cache
			
			bCommandWorked=true;
		}else
		if(cd.checkCmdValidity(this,scfSoundList,"")){
			for(String strKey:tmAudio.keySet().toArray(new String[]{})){
				AudioNode an = tmAudio.get(strKey);
				String strMute=isMute(an)?"(Mute)":"";
				cd.dumpSubEntry(strKey+strMute+":\n\t"+getFileNameFrom(an));
			}
			
			bCommandWorked=true;
		}else
		if(cd.checkCmdValidity(this,scfSoundSet,"<strAudioId> <strFile>")){
			String strAudioId = cd.getCurrentCommandLine().paramString(1);
			String strFile 		= cd.getCurrentCommandLine().paramString(2);
			if(strAudioId!=null && strFile!=null){
				bCommandWorked=setAudio(strAudioId, strFile)!=null;
			}
		}else
		if(cd.checkCmdValidity(this,scfSoundMuteToggle,"<strAudioId>")){
			String strAudioId = cd.getCurrentCommandLine().paramString(1);
			if(strAudioId!=null){
				bCommandWorked=muteAudioToggle(strAudioId);
			}
		}else
		if(cd.checkCmdValidity(this,scfSoundPlayFile,"<strSoundFile>")){
			String strSoundFile = cd.getCurrentCommandLine().paramString(1);
			if(strSoundFile!=null){
				setAudio("temp", strSoundFile);
				play("temp");
				bCommandWorked=true;
			}
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}
	
	public static class CfgParm extends ConditionalStateAbs.CfgParm{
		Class<?>[] aclassUserActionStack;
		public CfgParm(Class<?>... aclassUserActionStack) {
			super(null);
			this.aclassUserActionStack=aclassUserActionStack;
		}
	}
	private CfgParm cfg = null;
	@Override
	public ConditionalStateAbs configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		
		super.configure(icfg);
		
		this.aclassUserActionStackList.addAll(Arrays.asList(cfg.aclassUserActionStack));
		
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
		
		return this;
	}
	
	@Override
	protected boolean initAttempt() {
		if(!super.initAttempt())return false;
		
		for(EAudio ea:EAudio.values()){
			setAudio(ea.toString(), ea.cfg().getFile());
		}
		
		return true;
	}
}
