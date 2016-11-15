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

package com.github.commandsconsolegui.spAppOs.misc;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spCmd.varfield.TimedDelayVarField;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class DebugI implements IReflexFillCfg {//, IConsoleCommandListener{
//	private ConsoleCommands	cc;
	
	public DebugI() {
		ManageSingleInstanceI.i().add(this);
		
		bDebugMode=ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
			.indexOf("-agentlib:jdwp") > 0;
	}
	
	/**
	 * when enabled, these keys are used to perform debug tests
	 */
	public static enum EDebugKey implements IReflexFillCfg{
		AddTextToConsStats,
		DumpFontImgFile, 
		ShowConsNewDayInfoOnce,
//		VarToStringDenied,
		FillKeyValueHashmap(true),
		;
		
		private boolean	bDelayedMode;

		EDebugKey(){}
		EDebugKey(boolean bDelayedMode){
			this.bDelayedMode=bDelayedMode;
			if(this.bDelayedMode)tdBetweenValueApply.setActive(true);
		}
		
		BoolTogglerCmdField btgEnabled = new BoolTogglerCmdField(this,false); //default is disabled
		TimedDelayVarField tdBetweenValueApply = new TimedDelayVarField(this, 1f, "delay between feeding values to debug (where appliable), mainly to lower CPU usage");
//		boolean b;

		@Override
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
			ReflexFillCfg rfcfg = GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
			rfcfg.setPrefixCustomId(this.toString());
			return rfcfg;
		}
		@Override
		public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
			return fld.get(this);
		}
		@Override
		public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
			fld.set(this,value);
		}
		
		@Override
		public String getUniqueId() {
			return MiscI.i().prepareUniqueId(this)+":"+this.toString();
		}
	}
	
	private static DebugI instance = new DebugI();
	public static DebugI i(){return instance;}
//	public static void init(Debug dbg){
//		Debug.instance=dbg;
//	}

	private Boolean	bDebugMode;
	private boolean	bConfigured;
	private HashMap<String,Object> hmDebugKeyValue = new HashMap<String,Object>();
	
	public void configure(CommandsDelegator cc){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		//		if(Debug.instance==null)Debug.instance=this;
//		this.cc=cc;
		if(cc==null)throw new NullPointerException("invalid instance for "+CommandsDelegator.class.getName()); // KEEP ON TOP
//		cc.addConsoleCommandListener(this);
		
		bConfigured=true;
	}
	
//	public void setConsoleCommand(ConsoleCommands cc){
//		this.cc=cc;
//	}
//	
	
	/**
	 * Use this to spend less time processing the parameters that would be called
	 * at ex.: {@link #putKeyValue(String, Object)}
	 * @param ek
	 * @return
	 */
	public boolean isKeyEnabled(EDebugKey ek){
		if(ek.btgEnabled.b()){
			if(ek.bDelayedMode){
				if(ek.tdBetweenValueApply.isReady(true)){
					return true;
				}
			}else{
				return true;
			}
		}
		
		return false;
	}
	
	public void disableKey(EDebugKey ek){
		ek.btgEnabled.setObjectRawValue(false);
	}
	
	public void putKeyValue(String strKey, Object val){
		hmDebugKeyValue.put(strKey, val);
	}
	
	public ArrayList<String> reportKeyValue(){
		ArrayList<String> astr = new ArrayList<String>();
		for(Entry<String, Object> entry:hmDebugKeyValue.entrySet()){
			astr.add(entry.getKey()+"="+entry.getValue());
		}
		Collections.sort(astr);
		return astr;
	}
	
	StringCmdField scfReportKeyValue = new StringCmdField(this)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				GlobalCommandsDelegatorI.i().dumpSubEntry(reportKeyValue());
				return true;
			}
		});
	
//	@Override
//	public ECmdReturnStatus execConsoleCommand(CommandsDelegator	cd) {
//		boolean bCmdWorked=false;
//		
//		if(cd.checkCmdValidity(this,"debug",null,"[optionToToggle] [force:true|false] empty for a list")){
//			String str = cd.getCurrentCommandLine().paramString(1);
//			Boolean bForce = cd.getCurrentCommandLine().paramBoolean(2);
//			if(str==null){
//				for(EDebugKey ek:EDebugKey.values()){
//					cd.dumpSubEntry(""+ek+" "+ek.btgEnabled.b());
//				}
//			}else{
//				try{
//					EDebugKey ek = EDebugKey.valueOf(str);
//					if(bForce!=null){
//						ek.btgEnabled.setObjectRawValue(bForce);
//					}else{
//						ek.btgEnabled.toggle();
//					}
//					bCmdWorked=true;
//				}catch(IllegalArgumentException ex){
//					cd.dumpExceptionEntry(ex);
//				}
//			}
//		}else
//		{
//			return ECmdReturnStatus.NotFound;
//		}
//		
//		return cd.cmdFoundReturnStatus(bCmdWorked);
//	}

	public boolean isInIDEdebugMode() {
//		if(bDebugMode==null){
//			bDebugMode=ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
//				.indexOf("-agentlib:jdwp") > 0;
//		}
		return bDebugMode;
	}
	
	/**
	 * put a breakpoint inside this method!
	 * @param b
	 */
	public void conditionalBreakpoint(boolean b){
		if(b){
			/**
			 * PUT A BREAKPOINT AT LINE BELOW!
			 * that trick with ints is to avoid eclipse warning marker...
			 */
			int i2,iPutABreakPointHere=0;
			i2=iPutABreakPointHere;
			iPutABreakPointHere=i2;
		}
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}

	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}
	
	public String dumpValue(Object objValue){
		String str = MiscI.i().asReport(objValue);
		System.err.println("DEBUG DUMP VALUE:\n"+str);
		return str;
	}
}
