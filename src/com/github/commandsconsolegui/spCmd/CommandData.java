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

package com.github.commandsconsolegui.spCmd;

import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.misc.IHasOwnerInstance;
import com.github.commandsconsolegui.spAppOs.misc.IDebugReport;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RefHolder;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.varfield.VarCmdFieldAbs;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class CommandData implements Comparable<CommandData>,IHasOwnerInstance<IReflexFillCfg>,IDebugReport{//,IDiscardableInstance{
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
	private RefHolder<StackTraceElement[]> rhasteInstancedAt = new RefHolder<StackTraceElement[]>(null);

	private ArrayList<CommandData>	acmddSimpleIdConflicts = new ArrayList<CommandData>();

	private VarCmdFieldAbs	vcf;
	
	public String getSimpleCmdId(){
		return strSimpleCmdId;
	}
	
	public String getUniqueCmdId() {
		return strUniqueCmdId;
	}
	public String getComment() {
		return strComment;
	}
	
	@Override
	public IReflexFillCfg getOwner() {
		return irfcOwner;
	}
	
	public VarCmdFieldAbs getVar() {
		return vcf;
	}
	
//	public CommandData(IReflexFillCfg irfcOwner, VarCmdFieldAbs vcf, String strBaseCmd, String strSimpleCmdId, String strComment) {
//	public CommandData(IReflexFillCfg irfcOwner, VarCmdFieldAbs vcf, String strBaseCmd, String strSimpleCmdId, String strComment) {
	public CommandData(IReflexFillCfg irfcOwner, String strBaseCmd, String strSimpleCmdId, String strComment) {
		super();
		this.irfcOwner = irfcOwner;
//		this.vcf=vcf;
		this.strUniqueCmdId = strBaseCmd;
		this.strSimpleCmdId=strSimpleCmdId;
		this.strComment = strComment;
		this.rhasteInstancedAt.setHolded(Thread.currentThread().getStackTrace());
//		setVar(vcf);
		
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
	
	public void addSimpleIdConflict(CommandData cmdd){
		if(!acmddSimpleIdConflicts.contains(cmdd)){
			acmddSimpleIdConflicts.add(cmdd);
		}
	}
	
	public boolean isSimpleCmdIdConflicting(){
		return acmddSimpleIdConflicts.size()>0;
	}
	
	public ArrayList<CommandData> getSimpleIdConflictListClone() {
		return new ArrayList<CommandData>(acmddSimpleIdConflicts);
	}

	@Override
	public int compareTo(CommandData o) {
		return this.getSimpleCmdId().toLowerCase().compareTo(o.getSimpleCmdId().toLowerCase());
	}

	public void applySimpleCmdIdConflictFixed(CommandsDelegator.CompositeControl cc, String strNewSimpleCmdId, boolean bForce){//, Collection<CommandData> cmdListToClearConflicts) {
		cc.assertSelfNotNull();
		
		if(!isSimpleCmdIdConflicting()){
			if(bForce){
				GlobalCommandsDelegatorI.i().dumpDebugEntry("forcing change of simple id even without conflicts: "
					+this.getSimpleCmdId()+", "+this.getUniqueCmdId());
			}else{
				throw new PrerequisitesNotMetException("can only be directly set if it has conflicts",this, this.getSimpleCmdId(), this.getUniqueCmdId());
			}
		}
		
		// clear conflicts
		for(CommandData cmddOther:acmddSimpleIdConflicts.toArray(new CommandData[0])){
			cmddOther.acmddSimpleIdConflicts.remove(this);
			this.acmddSimpleIdConflicts.remove(cmddOther);
		}
		
//		for(CommandData dataOther:cmdListToClearConflicts){
//			if(dataOther.getSimpleCmdId().equalsIgnoreCase(this.strSimpleCmdId)){
//				dataOther.acmddCoreIdConflicts.remove(this);
//			}
//		}
		
		this.strSimpleCmdId=strNewSimpleCmdId;
	}

	public CommandData setVar(VarCmdFieldAbs vcf) {
		PrerequisitesNotMetException.assertNotAlreadySet(this.vcf, vcf, "var link to this cmd data", this);
		if(vcf.getOwner()!=getOwner()){
			throw new PrerequisitesNotMetException("should be the same owner", this, getOwner(), vcf, vcf.getOwner());
		}
		this.vcf=vcf;
		return this;
	}

	@Override
	public String getFailSafeDebugReport() {
		return getUniqueCmdId();
	}
	
}
