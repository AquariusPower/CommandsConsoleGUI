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

package com.github.commandsconsolegui.misc;

import java.util.HashMap;

import javax.xml.bind.ValidationEvent;

import com.google.common.primitives.Primitives;

/**
 * TODO funny unnecessarily complex class, may be slow to do all these accesses? better not use? :)
 * 
 * @param <G>
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 * 
 */
@Deprecated
public class GlobalsI <G extends GlobalsI<G>> {
	@SuppressWarnings("rawtypes")	private static GlobalsI instance = new GlobalsI();
	@SuppressWarnings("rawtypes")	public static GlobalsI i(){return instance;}
	@SuppressWarnings("rawtypes")
	public static void setInstance(GlobalsI i){
		GlobalsI.instance=i;
	}
	
	HashMap<Class<?>,Object> hmSingleInstanceGlobalList = new HashMap<Class<?>,Object>();
	HashMap<Object,ValueAccess> hmPrimitiveGlobalList = new HashMap<Object,ValueAccess>();
	
	public void addSingleInstanceFor(Object objGloballyAccessible){
		if(objGloballyAccessible==null){
			throw new NullPointerException("invalid null global");
		}
		
		if(objGloballyAccessible.getClass().isPrimitive() || Primitives.isWrapperType(objGloballyAccessible.getClass())){
			throw new NullPointerException("use specific methods for primitives: "+objGloballyAccessible);
		}
		
		PrerequisitesNotMetException.assertNotAlreadySet(
			"SingleInstance", 
			getSingleInstance(objGloballyAccessible.getClass()), 
			objGloballyAccessible, 
			objGloballyAccessible.getClass().getName());
//		if(getSingleInstance(objGloballyAccessible.getClass())!=null){
//			throw new NullPointerException("single instance already set for: "+objGloballyAccessible.getClass().getName());
//		}
		
		hmSingleInstanceGlobalList.put(objGloballyAccessible.getClass(),objGloballyAccessible);
	}
	
	public <T> T getSingleInstance(Class<T> clazz){
		@SuppressWarnings("unchecked") T obj = (T)hmSingleInstanceGlobalList.get(clazz);
		if(obj==null)throw new NullPointerException("asked class global instance was not set yet: "+clazz.getName());
		return obj;
	}
	
	private class ValueAccess{
		Object objVal;
		Object objRestrictAccess;
		
		public ValueAccess(Object objVal, Object objRestrictAccess) {
			super();
			this.objVal = objVal;
			this.objRestrictAccess = objRestrictAccess;
		}
	}
	
	/**
	 * 
	 * @param objKey
	 * @param objVal
	 * @param objRestrictAccess if not null, only this object will be allowed to update this global variable
	 */
	public void setPrimitiveKeyValue(Object objKey, Object objVal, Object objRestrictAccess){
		if(objKey==null){
			throw new NullPointerException("invalid null primitive key");
		}
		
		if(!objVal.getClass().isPrimitive() || !Primitives.isWrapperType(objVal.getClass())){
			throw new NullPointerException("use specific methods for non-primitives: "+objVal);
		}
		
		ValueAccess va = new ValueAccess(objVal,objRestrictAccess);
		if(hmPrimitiveGlobalList.containsKey(objKey)){
//		if(getPrimitiveValue(objKey, objVal.getClass())!=null){
//			throw new NullPointerException("primitive already set for key: "+objKey);
			PrerequisitesNotMetException.assertNotAlreadySet("primitive", hmPrimitiveGlobalList.get(objKey), va, objKey);
		}
		
		hmPrimitiveGlobalList.put(objKey,va);
	}
	
	public void updatePrimitiveValue(Object objKey, Object objVal, Object objRestrictAccess){
		ValueAccess va = hmPrimitiveGlobalList.get(objKey);
		
		if(va==null)throw new NullPointerException("asked primitive was not set yet for key: "+objKey);
		
		if(va.objRestrictAccess==null || va.objRestrictAccess.equals(objRestrictAccess)){
			va.objVal=objVal;
		}else{
			throw new NullPointerException("access denied for: "+objRestrictAccess+"; requires: "+va.objRestrictAccess);
		}
	}	
	
	/**
	 * 
	 * @param objKey
	 * @param clazzToAssert instead of casting, this will grant the value type returned
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPrimitiveValue(Object objKey, Class<T> clazzToAssert){
		ValueAccess va = hmPrimitiveGlobalList.get(objKey);
		
		if(va==null)throw new NullPointerException("asked primitive was not set yet for key: "+objKey);
		
		if(va.objVal==null)return null;
		
		if(!va.objVal.getClass().isAssignableFrom(clazzToAssert)){
			throw new NullPointerException("invalid object type: "+clazzToAssert.getName()+" != "+va.objVal.getClass()+": "+objKey);
		}
		
		return (T)va.objVal;
	}
	
}
