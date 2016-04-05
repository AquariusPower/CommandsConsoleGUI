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

package misc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import misc.ReflexFill.IReflexFillCfg;
import misc.ReflexFill.IReflexFillCfgVariant;
import misc.ReflexFill.ReflexFillCfg;

import com.jme3.app.SimpleApplication;

import console.ConsoleCommands;
import console.IConsoleCommandListener;

/**
 * Use this class sparingly!
 * 
 * A - When to use this? 
 *  If you need to do something very specific 
 *   and can't wait for your project class dependencies developers to implement it. 
 *  But, use it only until you find a proper way to implement without this class.
 * 
 * B - Why not use this? 
 *  Code using this class may break as soon incompatible 
 *   changes happen at the targeted classes it access.
 * 
 * C - What is a better alternative to this? 
 * C.1 - Ask such classes developers to provide proper/safer ways to access 
 *        such methods and fields, so basically they will maintain it.
 * C.2 - If possible, create a fork of such classes (but you will have to maintain it).
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 * 
 */
public class ReflexHacks implements IReflexFillCfg, IConsoleCommandListener{
	private static ReflexHacks instance = new ReflexHacks();
	public static ReflexHacks i(){return instance;}
	
	private ConsoleCommands cc;
	public final BoolTogglerCmd	btgAllowHacks = new BoolTogglerCmd(this,false,BoolTogglerCmd.strTogglerCodePrefix,
		"Hacks allows for otherwise impossible features, but they may break if targeted classes are updated.");

	private IHandleExceptions	ihe;
	private SimpleApplication	sapp;
//	public void setExceptionHandler(IHandleExceptions ihe){
//		this.ihe=ihe;
//	}
	private boolean	bConfigured;
	
	/**
	 * We shouldnt access private fields/methods as things may break.
	 * Any code depending on this must ALWAYS be optional!
	 * 
	 * @param clazzOfObjectFrom what superclass of the object from is to be used?
	 * @param objFrom
	 * @param strFieldName will break if changed..
	 * @return field value
	 */
	public Object reflexFieldHK(Class<?> clazzOfObjectFrom, Object objFrom, String strFieldName){
		if(!btgAllowHacks.b())return null;
		
		Object objFieldValue = null;
		try{
			Field field = clazzOfObjectFrom.getDeclaredField(strFieldName);
			field.setAccessible(true);
			objFieldValue = field.get(objFrom);
			field.setAccessible(false);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			ihe.handleExceptionThreaded(e);
		}
		
		return objFieldValue;
	}
	public Object getFieldValueHK(Object objFrom, String strFieldName){
		if(!btgAllowHacks.b())return null;
		
		return reflexFieldHK(objFrom.getClass(), objFrom, strFieldName);
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
	
	public void configure(SimpleApplication sapp, ConsoleCommands cc, IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		this.sapp = sapp;
		this.cc=cc;
		this.ihe=ihe;
		bConfigured=true;
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cc.getReflexFillCfg(rfcv);
	}
	
	/**
	 * For now, this is just a dummy method to allow this class {@link BoolToggler#} 
	 * fields to be indicated as owned by this class. 
	 */
	@Override
	public boolean executePreparedCommand(ConsoleCommands cc) {
		return false;
//		/**
//		 * remove this below if something else is actually implemented here...
//		 */
//		throw new NullPointerException("Do not call! Dummy method, just to allow the "
//				+BoolToggler.class.getName()+" be indicated as owned by this class "
//				+this.getClass().getName());
	}
}