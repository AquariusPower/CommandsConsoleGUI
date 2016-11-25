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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.lwjgl.opengl.XRandR;
import org.lwjgl.opengl.XRandR.Screen;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spCmd.IConsoleCommandListener;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;

/**
 * This class commands may contain some otherwise impossible fixes or workarounds to annoying bugs
 *  or limitations.
 * 
 * Use this class sparingly!
 * 
 * A - When to use this? 
 *  If you need to do something very specific 
 *   and can't wait for your project dependencies' developers to implement it,
 *   or do knot know yet how it can be properly coded and you are in a hurry. 
 * 
 * B - Why not use this? 
 *  Code using this class may break as soon incompatible changes happen at the targeted 
 *   classes it access. Or you may do something that breaks and was not foreseen (of course)
 *   by the dependencies' developers.
 * 
 * C - What is a better alternative to this? 
 * C.1 - Ask such classes developers to provide proper/safer ways to access 
 *        such methods and fields, so basically they will maintain it.
 * C.2 - If possible, create a fork of such classes (but you will have to maintain it).
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public class ReflexHacksPluginI implements IConsoleCommandListener, IReflexFillCfg, ISingleInstance{
	private static ReflexHacksPluginI instance = new ReflexHacksPluginI();
	public static ReflexHacksPluginI i(){return instance;}
	
	public ReflexHacksPluginI() {
		DelegateManagerI.i().addHandled(this);
//		ManageSingleInstanceI.i().add(this);
	}
	
	public final BoolTogglerCmdField	btgAllowHacks = new BoolTogglerCmdField(this,false,
		"Hacks allows for otherwise impossible features, but they may break if targeted classes are updated.").setCallNothingOnChange();

	private IHandleExceptions	ihe = SipmleHandleExceptionsI.i();
	private boolean	bConfigured;
	
	/**
	 * 
	 * @param clazzOfObjectFrom what superclass of the object from is to be used? if null, will use owner.class()
	 * @param objFieldOwner if null, clazz must be set (will be refering to a static field then)
	 * @param strFieldName this method will break if it gets changed by lib developers...
	 * @param bSetValue
	 * @param objSetNewValue
	 * @return
	 */
	public Object getOrSetFieldValueHK(Class<?> clazzOfObjectFrom, Object objFieldOwner, String strFieldName, Field fldOverride, boolean bSetValue, Object objSetNewValue){
		if(!btgAllowHacks.b())return null;
		
		if(clazzOfObjectFrom==null)clazzOfObjectFrom=objFieldOwner.getClass();
		
		Object objFieldValue = null;
		try{
			Field fld = fldOverride==null ? clazzOfObjectFrom.getDeclaredField(strFieldName) : fldOverride;
			
			boolean b = fld.isAccessible();
			if(!b)fld.setAccessible(true);
			
			objFieldValue = fld.get(objFieldOwner);
			
			if(bSetValue)fld.set(objFieldOwner, objSetNewValue);
			
			if(!b)fld.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			ihe.handleExceptionThreaded(e);
		}
		
		return objFieldValue;
	}
	
	/**
	 * We shouldnt access private fields/methods as things may break.
	 * Any code depending on this must ALWAYS be optional!
	 * 
	 * @param objInvoker
	 * @param strMethodName
	 * @param aobjParams
	 */
	public Object callMethodHK(Object objInvoker, String strMethodName, Object... aobjParams) {
		if(!btgAllowHacks.b())return null;
		
		Object objReturn = null;
		try {
			Method m = objInvoker.getClass().getDeclaredMethod(strMethodName);
			boolean bWasAccessible=m.isAccessible();
			if(!bWasAccessible)m.setAccessible(true);
			objReturn = m.invoke(objInvoker);
			if(!bWasAccessible)m.setAccessible(false);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			ihe.handleExceptionThreaded(e);
		}
		return objReturn;
	}
	
	public void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		
		if(ihe==null)throw  new NullPointerException("invalid instance for "+IHandleExceptions.class.getName());
		this.ihe=ihe;
		
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
		
		bConfigured=true;
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
	
	String strHacksWarning="WARNING: Hacks may break when libraries get updated.";
	StringCmdField scfHkFixXRandR = new StringCmdField(this);
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
//		if(cd.checkCmdValidity(this,"linuxXrandRScreenRestoreWhileWindowedBugFix", "fixXRandR", "in linux XRandR, even if application is windowed, on exiting it may try to restore the resolution but may do it wrongly, without considering the viewport")){
		if(
				cd.checkCmdValidity(scfHkFixXRandR, 
					"In Linux, even if application is windowed, on exiting "+XRandR.class.getName()+" may try to "
					+"restore the resolution but may do it wrongly, without considering the X viewport."
					+strHacksWarning
				)
		){
			// fix as upon restoring may ignore X configuration viewportin/viewportout
			Object o = ReflexHacksPluginI.i().getOrSetFieldValueHK(XRandR.class, null, "savedConfiguration", null, true, null);
			String str = scfHkFixXRandR.getUniqueCmdId()+", erased value: "+Arrays.toString((Screen[])o);
			cd.dumpInfoEntry(str, o);
			
			bCommandWorked = true;
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
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

}