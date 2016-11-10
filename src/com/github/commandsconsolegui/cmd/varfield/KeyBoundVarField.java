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

package com.github.commandsconsolegui.cmd.varfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.github.commandsconsolegui.globals.GlobalAppOSI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * This class is intended to be used only as class field variables.
 * It automatically creates console variables.
 * 
 * TODO set limit min and max, optinally throw exception or just fix the value to not over/underflow
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class KeyBoundVarField extends VarCmdFieldAbs<Integer[],KeyBoundVarField>{
	private String	strFullUserCommand;
	
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis, Integer[] aiBindCfg) {
		super(rfcfgOwnerUseThis, EVarCmdMode.VarCmd, aiBindCfg);
//		this.strFullCommand=strFullCommand;
		constructed();
	}
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis) {
		this(rfcfgOwnerUseThis, (Integer[])null);
	}
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis, String strBindCfg) {
		this(rfcfgOwnerUseThis, parseToBoundCfg(strBindCfg, true));
	}
	/**
	 * @param lInitialValue if null, the variable will be removed from console vars.
	 */
	public KeyBoundVarField(IReflexFillCfg rfcfgOwnerUseThis, int iKeyActionCode, int... aiKeyModifierCodeList) {
		this(rfcfgOwnerUseThis, join(iKeyActionCode,aiKeyModifierCodeList));
	}
	
	public KeyBoundVarField setUserCommand(String strFullUserCommand){
		this.strFullUserCommand=strFullUserCommand;
		
		setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				GlobalCommandsDelegatorI.i().addCmdToQueue(KeyBoundVarField.this.strFullUserCommand);
				return true;
			}
		});
		
		return getThis();
	}
	
	private static Integer[] join(int iAct, int... aiMod){
		Integer[] aiBoundCfg = new Integer[aiMod.length+1];
		aiBoundCfg[0]=iAct;
		for(int iIndex=0;iAct<aiMod.length;iAct++){
			aiBoundCfg[iIndex+1]=aiMod[iIndex];
		}
		return aiBoundCfg;
	}
	
//	private TreeMap<String,Integer> tmIdCode = new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER);
//	private static void fillIdCode(){
//		if(tmIdCode.size()>0)return;
//		try {
//			int iSpecialCodeStart=Integer.MAX_VALUE;
//			tmIdCode.put("ctrl"	, iSpecialCodeStart--);
//			tmIdCode.put("shift", iSpecialCodeStart--);
//			tmIdCode.put("alt"	, iSpecialCodeStart--);
//			
//			int iMaxCode=-1;
//			for(Field fld:KeyInput.class.getFields()){
//				/**
//				 * removes the KEY_ prefix
//				 */
//				int iCode=(Integer)fld.get(null);
//				if(iCode>iMaxCode)iMaxCode=iCode;
//				
//				String strId=fld.getName().substring(4);
//				
//				if(tmIdCode.values().contains(iCode)){
//					String strExistingId=null;
//					for(Entry<String, Integer> entry:tmIdCode.entrySet()){
//						if(entry.getValue()==iCode){
//							strExistingId=entry.getKey();
//						}
//					}
//					throw new PrerequisitesNotMetException("already contains code", strExistingId, iCode, strId);
//				}
//				
//				tmIdCode.put(strId, iCode);
//			}
//		} catch (IllegalArgumentException | IllegalAccessException e) {
//			throw new PrerequisitesNotMetException("unexpected").setCauseAndReturnSelf(e);
//		}
//	}
	
	/**
	 * expected syntax ex:
	 * LCONTROL+LSHIFT+LMENU+F1
	 * TODO let it parse ex. ctrl+shift+alt+f1, as there are two of each of these modifiers LCONTROL RCONTROL etc, create a new code to represent both of each
	 * @param strBindCfg
	 * @return the last key will be returned as the first, the activator
	 */
	public static Integer[] parseToBoundCfg(String strBindCfg,boolean bExceptionOnFail){
//		fillIdCode();
		
		String[] astr = strBindCfg.split("[+]");
		Integer[] ai = new Integer[astr.length];
		for(int i=0;i<astr.length;i++){
			Integer iCode = GlobalAppOSI.i().getKeyCode(astr[i]);
			if(iCode==null){
				String strMsg="parse fail for "+astr[i]+", "+strBindCfg;
				if(!bExceptionOnFail){
					MsgI.i().warn(strMsg);
				}else{
					throw new PrerequisitesNotMetException(strMsg);
				}
				return null;
			}
			ai[i]=iCode;
		}
		
		ArrayList<Integer> aiList = new ArrayList<Integer>(Arrays.asList(ai));
		Collections.rotate(aiList, 1);
		
		return aiList.toArray(new Integer[0]);
	}
	
	public String getBindCfg(){
		Integer[] ai = getValue();
		
		String str=GlobalAppOSI.i().getKeyId(ai[0]);
		for(int i=1;i<ai.length;i++){
			str+="+"+GlobalAppOSI.i().getKeyId(ai[i]);
		}
		
		return str;
	}
	
	@Override
	public KeyBoundVarField setObjectRawValue(Object objValue) {
		if(objValue == null){
			//keep this empty skipper nullifier
		}else
		if(objValue instanceof String){
			objValue = parseToBoundCfg((String)objValue, false); //coming from user action will just warn on failure
		}else
		if(objValue instanceof Integer[]){
			//expected value
		}else
		if(objValue instanceof Integer){
			objValue = new Integer[]{(Integer)objValue};
		}else{
			throw new PrerequisitesNotMetException("unsupported class type", objValue.getClass());
		}
		
		super.setObjectRawValue(objValue);
		
		return getThis();
	}
	
	@Override
	public String getVariablePrefix() {
		return "Bind";
	}
	
	@Override
	protected KeyBoundVarField getThis() {
		return this;
	}

	private String strCodePrefixDefault="bind";
	private boolean	bUseCallQueue;
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
	
	@Override
	protected String valueReport(Object val) {
		if(val==null)return ""+null;
		
		return getBindCfg();
	}
	
	@Override
	public String getValueAsString(int iFloatingPrecision) {
		return getBindCfg();
	}
	
//	/**
//	 * just to easy debugging
//	 */
//	@Override
//	public KeyBoundVarField setConsoleVarLink(CompositeControl cc,ConsoleVariable cvar) {
//		return super.setConsoleVarLink(cc, cvar);
//	}
	
	@Override
	public Integer[] getValue() {
		return super.getValue();
	}
	public void checkRunCallerAssigned(boolean bRun, String strId) {
		if(bRun && isUniqueCmdIdEqualTo(strId)){
			if(isUseCallQueue()){
				callerAssignedQueueNow();
			}else{
				callerAssignedRunNow();
			}
		}
	}
	private boolean isUseCallQueue() {
		return bUseCallQueue;
	}
	public KeyBoundVarField setUseCallQueue(boolean bUseCallQueue) {
		this.bUseCallQueue = bUseCallQueue;
		return getThis();
	}
}
