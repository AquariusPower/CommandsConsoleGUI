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

package com.github.commandsconsolegui.cmd;

import java.util.ArrayList;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CommandData implements Comparable<CommandData>{
	/** 
	 * Core IDs are simple short commands.
	 * They may conflict, but it is not critical as the full command is what matters. 
	 */
	private String strSimpleCmdId;
	
	/**
	 * Fully qualified, unique, using classes names etc.
	 * Must NOT conflict!
	 */
	private String strUniqueCmdId;
	
	private String strComment;
	private IReflexFillCfg irfcOwner;
	private StackTraceElement[] asteCodeTrackUniqueId;

	private ArrayList<CommandData>	acmddCoreIdConflicts = new ArrayList<CommandData>();
	
	public String getSimpleCmdId(){
		return strSimpleCmdId;
	}
	
	public String getUniqueCmdId() {
		return strUniqueCmdId;
	}
	public String getComment() {
		return strComment;
	}
	public IReflexFillCfg getOwner() {
		return irfcOwner;
	}
	
	public CommandData(IReflexFillCfg irfcOwner, String strBaseCmd, String strSimpleCmdId, String strComment) {
		super();
		this.irfcOwner = irfcOwner;
		this.strUniqueCmdId = strBaseCmd;
		this.strSimpleCmdId=strSimpleCmdId;
		this.strComment = strComment;
		this.asteCodeTrackUniqueId = Thread.currentThread().getStackTrace();
		
		if(this.irfcOwner==null)throw new PrerequisitesNotMetException("listener cannot be null");
	}
	
	public String asHelp() {
		String strCommentOk = strComment;
		if(!strCommentOk.trim().startsWith(GlobalCommandsDelegatorI.i().getCommentPrefixStr())){
			strCommentOk=GlobalCommandsDelegatorI.i().getCommentPrefixStr()+strCommentOk;
		}
		
		String strCmdToShow = getSimpleCmdId();
		String strInfoShow = " ("+strUniqueCmdId+")";
		if(isSimpleCmdIdConflicting()){
			strCmdToShow=strUniqueCmdId;
			strInfoShow=" (Conflict:"+getSimpleCmdId()+")";
		}
		
		return GlobalCommandsDelegatorI.i().getCommandPrefixStr()
			+strCmdToShow+" "
			+strCommentOk
			+strInfoShow;
	}
	
	public boolean identicalTo(CommandData cmddNew) {
		return 
			this.getOwner()==cmddNew.getOwner()
			&&
			this.getUniqueCmdId().equalsIgnoreCase(cmddNew.getUniqueCmdId())
			&&
			this.getComment().equalsIgnoreCase(cmddNew.getComment()) //TODO may be a stack of the code spot where the command was created, would be preferable than this "comment difference guessed uniqueness"?
			;
	}
	
	public void addCoreIdConflict(CommandData cmdd){
		if(!acmddCoreIdConflicts.contains(cmdd)){
			acmddCoreIdConflicts.add(cmdd);
		}
	}
	
	public boolean isSimpleCmdIdConflicting(){
		return acmddCoreIdConflicts.size()>0;
	}
	
	public ArrayList<CommandData> getCoreIdConflictListClone() {
		return new ArrayList<CommandData>(acmddCoreIdConflicts);
	}

	@Override
	public int compareTo(CommandData o) {
		return this.getSimpleCmdId().toLowerCase().compareTo(o.getSimpleCmdId().toLowerCase());
	}

	public void fixSimpleCmdIdConflict(CommandsDelegator.CompositeControl ccCd, String strNewSimpleCmdId){//, Collection<CommandData> cmdListToClearConflicts) {
		ccCd.assertSelfNotNull();
		
		if(!isSimpleCmdIdConflicting())throw new PrerequisitesNotMetException("can only be directly set if it has conflicts");
		
		// clear conflicts
		for(CommandData dataOther:acmddCoreIdConflicts.toArray(new CommandData[0])){
			dataOther.acmddCoreIdConflicts.remove(this);
			this.acmddCoreIdConflicts.remove(dataOther);
		}
		
//		for(CommandData dataOther:cmdListToClearConflicts){
//			if(dataOther.getSimpleCmdId().equalsIgnoreCase(this.strSimpleCmdId)){
//				dataOther.acmddCoreIdConflicts.remove(this);
//			}
//		}
		
		this.strSimpleCmdId=strNewSimpleCmdId;
	}
	
}
