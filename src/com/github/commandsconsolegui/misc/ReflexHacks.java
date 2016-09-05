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

package com.github.commandsconsolegui.misc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.lwjgl.opengl.XRandR;
import org.lwjgl.opengl.XRandR.Screen;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;

/**
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
public class ReflexHacks implements IConsoleCommandListener, IReflexFillCfg {
	private static ReflexHacks instance = new ReflexHacks();
	public static ReflexHacks i(){return instance;}
	
//	private CommandsDelegator cc;
	public final BoolTogglerCmdField	btgAllowHacks = new BoolTogglerCmdField(this,false,
		"Hacks allows for otherwise impossible features, but they may break if targeted classes are updated.").setCallNothingOnChange();

	private IHandleExceptions	ihe = HandleExceptionsRaw.i();
//	private SimpleApplication	sapp;
//	public void setExceptionHandler(IHandleExceptions ihe){
//		this.ihe=ihe;
//	}
	private boolean	bConfigured;
	
//	public Object getFieldValueHK(Object objFrom, String strFieldName){
//		if(!btgAllowHacks.b())return null;
//		
//		return getFieldValueHK(objFrom.getClass(), objFrom, strFieldName);
//	}
	
//	/**
//	 * We shouldnt access private fields/methods as things may break.
//	 * Any code depending on this must ALWAYS be optional!
//	 * 
//	 * @param clazzOfObjectFrom what superclass of the object from is to be used?
//	 * @param objFieldOwner
//	 * @param strFieldName will break if changed..
//	 * @return field value
//	 */
//	public Object getFieldValueHK(Object objFieldOwner, String strFieldName){
//		if(clazz==null)clazz=objFieldOwner.getClass();
//		return fieldWorkHK(objFieldOwner.getClass(), objFieldOwner, strFieldName, false, null);
//////	private Object getFieldValueHK(Class<?> clazzOfObjectFrom, Object objFrom, String strFieldName){
////		if(!btgAllowHacks.b())return null;
////		
////		Class<?> clazzOfObjectFrom = objFieldOwner.getClass();
////		Object objFieldValue = null;
////		try{
////			Field field = clazzOfObjectFrom.getDeclaredField(strFieldName);
////			field.setAccessible(true);
////			objFieldValue = field.get(objFieldOwner);
////			field.setAccessible(false);
////		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
////			ihe.handleExceptionThreaded(e);
////		}
////		
////		return objFieldValue;
//	}
	
//	/**
//	 * 
//	 * @param clazz if null, will use owner.class()
//	 * @param objFieldOwner if null, clazz must be set (will be refering to a static field then)
//	 * @param strFieldName
//	 * @param objSetNewValue
//	 * @return
//	 */
//	public Object setFieldValueHK(Class<?> clazz, Object objFieldOwner, String strFieldName, Object objSetNewValue){
//		if(clazz==null)clazz=objFieldOwner.getClass();
//		return fieldWorkHK(clazz, objFieldOwner, strFieldName, true, objSetNewValue);
//	}
	
	/**
	 * 
	 * @param clazzOfObjectFrom what superclass of the object from is to be used? if null, will use owner.class()
	 * @param objFieldOwner if null, clazz must be set (will be refering to a static field then)
	 * @param strFieldName this method will break if it gets changed by lib developers...
	 * @param bSetValue
	 * @param objSetNewValue
	 * @return
	 */
	public Object getOrSetFieldValueHK(Class<?> clazzOfObjectFrom, Object objFieldOwner, String strFieldName, boolean bSetValue, Object objSetNewValue){
		if(!btgAllowHacks.b())return null;
		
		if(clazzOfObjectFrom==null)clazzOfObjectFrom=objFieldOwner.getClass();
		
		Object objFieldValue = null;
		try{
			Field field = clazzOfObjectFrom.getDeclaredField(strFieldName);
			
			boolean b = field.isAccessible();
			if(!b)field.setAccessible(true);
			
			objFieldValue = field.get(objFieldOwner);
			
			if(bSetValue)field.set(objFieldOwner, objSetNewValue);
			
			if(!b)field.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			ihe.handleExceptionThreaded(e);
		}
		
		return objFieldValue;
	}
	
//	/**
//	 * 
//	 * @param clazzOfObjectFrom what superclass of the object from is to be used?
//	 * @param objFrom
//	 * @param strFieldName will break if changed..
//	 * @param bAccessPrivateField We shouldnt access private fields/methods as things may break. Any code depending on this must ALWAYS be optional!
//	 * @param objValueMatch if not null, the 1st field matching this value will return it's name
//	 * @return field value or name
//	 */
//	public Object reflexFieldHK(Class<?> clazzOfObjectFrom, Object objFrom, String strFieldName, boolean bAccessPrivateField, Object objValueMatch){
//		if(!btgAllowHacks.b())return null;
//		
//		Object objFieldData = null;
//		try{
//			Field field = clazzOfObjectFrom.getDeclaredField(strFieldName);
//			
//			boolean bPrivate = !field.isAccessible();
//			
//			if(bAccessPrivateField && bPrivate)field.setAccessible(true);
//			
//			objFieldData = field.get(objFrom); // will throw if private, ok!
//			
//			if(bPrivate)field.setAccessible(false);
//		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
//			ihe.handleExceptionThreaded(e);
//		}
//		
//		return objFieldData;
//	}
	
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
	
//	public void configure(SimpleApplication sapp, CommandsDelegatorI cc, IHandleExceptions ihe){
//	public void configure(CommandsDelegator cc, IHandleExceptions ihe){
	public void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
//		this.sapp = sapp;
		
//		if(cc==null)throw  new NullPointerException("invalid instance for "+CommandsDelegator.class.getName());
//		this.cc=cc;
		
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
				cd.checkCmdValidity(this, scfHkFixXRandR, 
					"In linux XRandR, even if application is windowed, on exiting it may try to "
					+"restore the resolution but may do it wrongly, without considering the viewport."
					+strHacksWarning
				)
		){
			// fix as upon restoring may ignore X configuration viewportin/viewportout
			Object o = ReflexHacks.i().getOrSetFieldValueHK(XRandR.class, null, "savedConfiguration", true, null);
			String str = scfHkFixXRandR.getUniqueCmdId()+", erased value: "+Arrays.toString((Screen[])o);
			cd.dumpInfoEntry(str, o);
			
			bCommandWorked = true;
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}
	
}
