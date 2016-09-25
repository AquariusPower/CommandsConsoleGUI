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

package com.github.commandsconsolegui.jme.savablevalues;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.github.commandsconsolegui.cmd.varfield.EType;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.jme.SavableHelperI;
import com.github.commandsconsolegui.misc.jme.SavableHelperI.ISavableFieldAccess;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;


/**
 * declare fields like ex.:
 * float fHeightDefault=10,fHeight=fHeightDefault; 
 * 
 * if default is not declared, current value will always be saved, or used as default when reading.
 * 
 * TODO create a SavableHelperI class and a ISavableHelper.getSkipper() interface to use instead of Savable.
 * TODO ISavableHelper shall contain .getFieldValue() and .setFieldValue() so .isAccessible() and .setAcessible() are not required.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public abstract class CompositeSavableAbs<O,S extends CompositeSavableAbs<O,S>> implements ISavableFieldAccess {
	public CompositeSavableAbs(){
		ss.bThisInstanceIsALoadedTmp=true;
		initAllSteps();
	} //required by savable
	public CompositeSavableAbs(O owner){
		setOwner(owner);
		initAllSteps();
	}
	public abstract S getThis();
	
//	public static interface ISavableFieldAccess{
//		public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException;
//		public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException;
//	}
	
	public static interface ISaveSkipper{}
	
	public static class FieldExtraInfo{
		Boolean bAccessible;
		Object valDefault;
	}
	public static class SaveSkipper<O> implements ISaveSkipper{
		private boolean bThisInstanceIsALoadedTmp = false;
		private O owner=null;
		private boolean	bFailedToLoad;
//		private HashMap<Field,Boolean>	hmFieldAccessible;
		private HashMap<Field,FieldExtraInfo> hmFieldExtraInfo;// = new HashMap<Field,FieldExtraInfo>();
	}
	private SaveSkipper<O> ss = new SaveSkipper<O>();
	
	private void applyFieldExtraInfo(Field fld,Boolean bAccessible,Object valDef){
		FieldExtraInfo fei = ss.hmFieldExtraInfo.get(fld);
		
		if(fei==null){
			fei=new FieldExtraInfo();
			ss.hmFieldExtraInfo.put(fld,fei);
		}
		
		if(valDef!=null){
			fei.valDefault=valDef;
		}
		
		if(bAccessible!=null){
			fei.bAccessible=bAccessible;
		}
		
//		return fei;
	}
	
	public boolean isFailedToLoad(){return ss.bFailedToLoad;}
	
	/**
	 * this way, super methods can come on the beggining of the current class method
	 */
	private void initAllSteps(){
		initPre();
		initialize();
		initPos();
	}
	
	public boolean isThisInstanceALoadedTmp(){
		return ss.bThisInstanceIsALoadedTmp; //ss.owner==null;
	}
	
	/** currently this is just to give flexibility */
	protected void initPre() {}
	
	/** 
	 * this is where field's values can be instantiated,
	 * avoid instantiating field's values outside here or of constructors, as their order/placement
	 * in the class source may cause trouble ex.: if an instantiation is coded after the declaration of 
	 * the constructor, such field will still be null at the constructor!!!
	 */
	protected void initialize(){}
	
	/** this is after everything is instantiated */
	private void initPos() {
		prepareFields();
	}
	
	private void prepareFields() {
		for(Entry<Field,FieldExtraInfo> entry:getAllFields()){
			Field fld = entry.getKey();
			try {
				allowFieldAccess(fld);
				switch(EType.forClass(fld.getType())){
					case Boolean:
					case Double:
					case Float:
					case Int:
					case Long:
					case String:
						/**
						 * set DEFAULT value
						 */
						Object val = SavableHelperI.i().getFieldVal(this,fld);
						if(val==null){
							throw new PrerequisitesNotMetException("default (initial) value, cannot be null!", fld); 
						}
						applyFieldExtraInfo(fld, null, val);
						break;
					case Var:
						/**
						 * it has its own internal default.
						 * ensure consistency, Savable does not accept nulls...
						 */
						((VarCmdFieldAbs)SavableHelperI.i().getFieldVal(this,fld)).setDenyNullValue();
						break;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new PrerequisitesNotMetException("failed to retrieve default value",this,fld.getDeclaringClass(),fld.getName(),fld)
					.initCauseAndReturnSelf(e);
			}finally{
				restoreFieldAccessModeFor(fld, entry.getValue().bAccessible);
			}
		}
	}
	
	private S setOwner(O owner){
		if(ss.owner!=null)PrerequisitesNotMetException.assertNotAlreadySet("owner", ss.owner, owner, this);
		if(owner==null)throw new PrerequisitesNotMetException("cannot erase (nullify) the owner", ss.owner, this);
		ss.owner=owner;
		return getThis();
	}
	
	public O getOwner() {
		return ss.owner;
	}
	
	protected boolean checkSkip(Field fld){
		if(ISaveSkipper.class.isAssignableFrom(fld.getType()))return true;
		
		/**
		 * table switch for enum, dynamically generated for each switch(){} you code... 
		 * switch( ($SWITCH_TABLE$com$...())[this.ENUM_ID.ordinal] )
		 */
		if(fld.isSynthetic())return true; //this$0, $SWITCH_TABLE$, ...
		if(fld.getName().startsWith("$SWITCH_TABLE$")){
			return true; //should just be a synthetic
		}
		try {if(SavableHelperI.i().getFieldVal(this,fld)==ss.owner){ //this$0
				return true; //should just be a synthetic
		}} catch (IllegalArgumentException | IllegalAccessException e) {/*wont happen as skipper is inner*/}
		
		return false;
	}
	
	/**
	 * requires {@link #restoreFieldAccessModeFor(Field, boolean)} for each field at `...finally{}`
	 * @param fld
	 * @return
	 */
	protected boolean allowFieldAccess(Field fld){
		boolean bWasAccessible = true;
		if(!fld.isAccessible()){
			/**
			 * this is important to let the fields be private or protected, 
			 * otherwise would create flaws in such code by preventing encapsulation.
			 */
			bWasAccessible=false;
			fld.setAccessible(true);
		}
		
		return bWasAccessible;
	}
	
	protected void restoreFieldAccessModeFor(Field fld, boolean bWasAccessible){
		if(!bWasAccessible)fld.setAccessible(false);
	}
	
	private Set<Entry<Field,FieldExtraInfo>> getAllFields(){
		if(ss.hmFieldExtraInfo==null){
			ss.hmFieldExtraInfo = new HashMap<Field,FieldExtraInfo>();
			for(Class clOwner:MiscI.i().getSuperClassesOf(this)){
				for(Field fld:clOwner.getDeclaredFields()){
					boolean bAccessible = allowFieldAccess(fld);
					if(checkSkip(fld))continue;
					applyFieldExtraInfo(fld, bAccessible, null);
//					ss.hmFieldAccessible.put(fld,bAccessible);
				}
			}
		}
		
		return ss.hmFieldExtraInfo.entrySet();
	}
	
	/**
	 * This happens on the object being directly used by the application.
	 */
	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		for(Entry<Field,FieldExtraInfo> entry:getAllFields()){
			Field fld=entry.getKey();
//		for(Class clOwner:MiscI.i().getSuperClassesOf(this)){
//			for(Field fld:clOwner.getDeclaredFields()){
				ArrayList<Object> aobjDbg = new ArrayList<Object>();
				
				allowFieldAccess(fld);
//				boolean bAccessible = allowAccess(fld);
				try {
//					if(checkSkip(fld))continue;
					
					String strName = fld.getName();
					Object val = SavableHelperI.i().getFieldVal(this,fld);
					Object valDef = ss.hmFieldExtraInfo.get(fld).valDefault;
//					Object valDef = getDefaultValueFor(fld,false);
					
					addDbgInfo(aobjDbg, oc, fld.getDeclaringClass(), fld, strName, val.getClass().getName(), val, valDef);
					
					write(oc,strName,val,valDef,aobjDbg);
				} catch (IllegalArgumentException | IllegalAccessException | UnsupportedOperationException e) {
					throw new PrerequisitesNotMetException("failed to retrieve field/default value",aobjDbg)
						.initCauseAndReturnSelf(e);
				}finally{
					restoreFieldAccessModeFor(fld, entry.getValue().bAccessible);
				}
//			}
		}
	}
	
	private void write(OutputCapsule oc, String strName, Object val, Object valDef, ArrayList<Object> aobjDbg) throws IOException {
		Class<?> clValue = val.getClass();
		
		switch(EType.forClass(clValue)){
			case Boolean:	oc.write((boolean)val,	strName, changeVal(boolean.class,	val, valDef));break;
			case Double:	oc.write((double)val,		strName, changeVal(double.class,	val, valDef));break;
			case Float:		oc.write((float)val,		strName, changeVal(float.class,		val, valDef));break;
			case Int:			oc.write((int)val,			strName, changeVal(int.class,			val, valDef));break;
			case Long:		oc.write((long)val,			strName, changeVal(long.class,		val, valDef));break;
			case String:	oc.write((String)val,		strName, changeVal(String.class,	val, valDef));break;
			case Var:
				VarCmdFieldAbs<?,?> var = (VarCmdFieldAbs<?,?>)val;
				addDbgInfo(aobjDbg, var.getReport());
//				if(var.getRawValue() != var.getRawValueDefault()){
					write(oc, strName, var.getRawValue(), var.getRawValueDefault(), aobjDbg);
//				}
				return;
		}
	}

//	public static enum EType{
//		Int,
//		Long,
//		Float,
//		Double,
//		String,
//		Boolean,
//		Var,
//		;
//		
//		public static EType forClass(Class clValue) throws UnsupportedOperationException{
//			EType e = null;
//			if(clValue==Float.class		|| clValue==float.class		){e=EType.Float;}else
//			if(clValue==Double.class	|| clValue==double.class	){e=EType.Double;}else
//			if(clValue==Integer.class	|| clValue==int.class			){e=EType.Int;}else
//			if(clValue==Long.class		|| clValue==long.class		){e=EType.Long;}else
//			if(clValue==Boolean.class	|| clValue==boolean.class	){e=EType.Boolean;}else
//			if(clValue==String.class														){e=EType.String;}else
//			if(VarCmdFieldAbs.class.isAssignableFrom(clValue)		){e=EType.Var;}else
//			{
//				throw new UnsupportedOperationException("unsupported value class type "+clValue.getName());
//			}
//			
//			return e;
//		}
//		
//	}
	
	/**
	 * A missing default means to always save.
	 * If default value is not present, try to use an invalid value (like Float.Nan) to grant 
	 * value will always be saved in case there is no default.
	 * To grant it will be saved, the default just needs to differ from the actual valid value.
	 */
	private <T> T changeVal(Class<T> clValue, Object objValue, Object valueDefault){
		switch(EType.forClass(clValue)){
			case Boolean:	if(valueDefault==null)valueDefault=!((boolean)objValue);	break;
			case Double:	if(valueDefault==null)valueDefault=Double.NaN;						break;
			case Float:		if(valueDefault==null)valueDefault=Float.NaN;							break;
			case Int:
				if(valueDefault==null){
					if(((int)objValue)==Integer.MAX_VALUE){
						valueDefault=((int)objValue)-1;
					}else{
						valueDefault=((int)objValue)+1;
					}
				}
				break;
			case Long:
				if(valueDefault==null){
					if(((long)objValue)==Long.MAX_VALUE){
						valueDefault=((long)objValue)-1;
					}else{
						valueDefault=((long)objValue)+1;
					}
				}
				break;
			case String:	if(valueDefault==null)valueDefault=((String)objValue)+"_Different";	break;
			case Var:
				// put nothing here!
				break;
		}
//		{throw new PrerequisitesNotMetException("unsupported value class type "+clValue);}
		
		return (T)valueDefault;
	}
	
	private void addDbgInfo(ArrayList<Object> aobjDbg, Object... aobj){
		for(Object obj:aobj){
			aobjDbg.add(obj);
		}
	}
	
	/**
	 * This happens on a new instance, requires {@link #applyValuesFrom(CompositeSavableAbs)} to be made useful.
	 */
	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		for(Entry<Field,FieldExtraInfo> entry:getAllFields()){
			Field fld=entry.getKey();
//		for(Class clOwner:MiscI.i().getSuperClassesOf(this)){
//			for(Field fld:clOwner.getDeclaredFields()){
				Class clField=null;
				
				ArrayList<Object> aobjDbg = new ArrayList<Object>();
				addDbgInfo(aobjDbg,this,im,ic,fld.getDeclaringClass(),fld,clField);
				
				allowFieldAccess(fld);
//				boolean bAccessible = allowAccess(fld);
				try {
//					if(checkSkip(fld))continue;
					
					clField = fld.getType();
//					Object objValDef = getDefaultValueFor(fld,true);
					Object valDef = ss.hmFieldExtraInfo.get(fld).valDefault;
					String strName=fld.getName();
					
					read(ic,clField,strName,fld,valDef,aobjDbg);
				} catch (IllegalArgumentException | IllegalAccessException | NullPointerException e) {
					GlobalCommandsDelegatorI.i().dumpExceptionEntry(e,aobjDbg);
					ss.bFailedToLoad=true;
					break;
				}finally{
					restoreFieldAccessModeFor(fld, entry.getValue().bAccessible);
				}
//			}
		}
	}
	
	private Object read(InputCapsule ic, Class clField, String strName, Field fld, Object objValDef, ArrayList<Object> aobjDbg) throws IllegalArgumentException, IllegalAccessException, IOException {
		Object valRead = null;
		switch(EType.forClass(clField)){
			case Boolean:	valRead=ic.readBoolean(strName, (boolean)objValDef);break;
			case Double:	valRead=ic.readDouble	(strName, (double)objValDef);	break;
			case Float:		valRead=ic.readFloat	(strName, (float)objValDef);	break;
			case Int:			valRead=ic.readInt		(strName, (int)objValDef);		break;
			case Long:		valRead=ic.readLong		(strName, (long)objValDef);		break;
			case String:	valRead=ic.readString	(strName, (String)objValDef);	break;
			case Var:
				VarCmdFieldAbs var = (VarCmdFieldAbs)SavableHelperI.i().getFieldVal(this,fld);
				addDbgInfo(aobjDbg,var.getReport());
				var.setObjectRawValue(
					read(ic, var.getRawValue().getClass(), strName, null, var.getRawValueDefault(), aobjDbg));
				return null;
		}
		
		if(fld==null){ //called from var
			return valRead;
		}
		
		SavableHelperI.i().setFieldVal(this,fld,valRead);
		
		return null;
	}
	
	/**
	 * 
	 * @param svLoaded
	 * @return if succeeded to apply to self
	 */
	public boolean applyValuesFrom(S svLoaded) {
		return applyValuesFrom(svLoaded,false);
	}
	private boolean applyValuesFrom(S svLoaded, boolean bRestoringSelfBackup) {
		ArrayList<Object> aobjDbg = new ArrayList<Object>();
		aobjDbg.add(this.getClass().getName());
		aobjDbg.add(this);
		
		if(svLoaded==null){
			GlobalCommandsDelegatorI.i().dumpWarnEntry("null, nothing loaded",aobjDbg);
			return false;
		}
		aobjDbg.add(svLoaded.getClass().getName());
		aobjDbg.add(svLoaded);
		
		S svBkpSelf = null;
		if(!bRestoringSelfBackup){
			if(svLoaded.isFailedToLoad()){
				GlobalCommandsDelegatorI.i().dumpWarnEntry("cannot apply values from a 'failed to load' object",aobjDbg);
				return false;
			}
			
			try {
				svBkpSelf = (S)this.getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				throw new PrerequisitesNotMetException("missing empty constructor",aobjDbg).initCauseAndReturnSelf(e1);
			}
		}
		
		if(!this.getClass().isInstance(svLoaded))throw new PrerequisitesNotMetException("incompatible", aobjDbg);
		for(Entry<Field,FieldExtraInfo> entry:getAllFields()){
			Field fld=entry.getKey();
			aobjDbg.add(fld.getDeclaringClass());
			aobjDbg.add(fld.getType());
			aobjDbg.add(fld);
			
//		labelClassLoop:for(Class cl:MiscI.i().getSuperClassesOf(this)){
//			if(!cl.isInstance(svLoaded))throw new PrerequisitesNotMetException("incompatible",cl,this,svLoaded);
//			for(Field fld:cl.getDeclaredFields()){
//			boolean bAccessible = allowAccess(fld);
				allowFieldAccess(fld);
//				if(checkSkip(fld))continue;
				try {
					if(VarCmdFieldAbs.class.isAssignableFrom(fld.getType())){
						VarCmdFieldAbs<?,?> var=(VarCmdFieldAbs<?,?>)SavableHelperI.i().getFieldVal(this,fld);
						VarCmdFieldAbs<?,?> varLoaded=(VarCmdFieldAbs<?,?>)SavableHelperI.i().getFieldVal(svLoaded,fld);
						if(var.getClass()!=varLoaded.getClass()){
							aobjDbg.add(var.getClass());
							aobjDbg.add(varLoaded.getClass());
							throw new PrerequisitesNotMetException("should be the exactly same concrete instanced class, they differ:", aobjDbg);
						}
						var.setObjectRawValue(varLoaded.getRawValue());
					}else
					{	
						if(fld.getType().isPrimitive()){}else
						if(String.class.isAssignableFrom(fld.getType())){}else
						{
							throw new PrerequisitesNotMetException("direct field value overwrite is only allowed for primitives and String!"
								+" other classes must use their specific getters and setters",aobjDbg); 
						}
						
						Object valLoaded = SavableHelperI.i().getFieldVal(svLoaded,fld);
						if(valLoaded==null){
							throw new PrerequisitesNotMetException("how can the loaded value be null?", aobjDbg);
						}
						
						SavableHelperI.i().setFieldVal(this,fld,valLoaded);
					}
				} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
					if(bRestoringSelfBackup){
						throw new PrerequisitesNotMetException("during restore values", aobjDbg).initCauseAndReturnSelf(e);
					}else{
						GlobalCommandsDelegatorI.i().dumpExceptionEntry(e,aobjDbg);
						this.applyValuesFrom(svBkpSelf,true); //restore values to remove any inconsistency
					}
					
					break;
				}finally{
					restoreFieldAccessModeFor(fld, entry.getValue().bAccessible);
				}
		}
		
		return true;
	}
	
//	private void setFieldVal(Object objHoldingField, Field fld, Object val) throws IllegalArgumentException, IllegalAccessException {
//		if(objHoldingField instanceof ISavableFieldAccess){
//			((ISavableFieldAccess)objHoldingField).setFieldValue(fld, val);
//		}else{
//			fld.set(objHoldingField, val);
//		}
//	}
//	private Object getFieldVal(Object objHoldingField, Field fld) throws IllegalArgumentException, IllegalAccessException {
//		if(objHoldingField instanceof ISavableFieldAccess){
//			return ((ISavableFieldAccess)objHoldingField).getFieldValue(fld);
//		}else{
//			return fld.get(objHoldingField);
//		}
//	}
}
