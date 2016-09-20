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

package com.github.commandsconsolegui.jmegui.savablevalues;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;


/**
 * declare fields like ex.:
 * float fHeightDefault=10,fHeight=fHeightDefault; 
 * 
 * if default is not declared, current value will always be saved, or used as default when reading.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public abstract class CompositeSavableAbs<O,S extends CompositeSavableAbs<O,S>> implements Savable {
	public CompositeSavableAbs(){super();} //required by savable
	public abstract S getThis();
	
	public static interface ISaveSkipper{}
	
	private class SaveSkipper implements ISaveSkipper{
		private O owner=null;
		private boolean	bFailedToLoad;
	}
	private SaveSkipper ss = new SaveSkipper();
	
	public boolean isFailedToLoad(){return ss.bFailedToLoad;}

	public CompositeSavableAbs(O owner){
		this();
		setOwner(owner);
	}
	
	private S setOwner(O owner){
		if(ss.owner!=null)PrerequisitesNotMetException.assertNotAlreadySet("owner", ss.owner, owner, this);
		ss.owner=owner;
		return getThis();
	}
	
	public O getOwner() {
		return ss.owner;
	}
	
	private Object getDefaultValueFor(Field fld, boolean bIfDefaultMissingReturnCurrent) throws IllegalArgumentException, IllegalAccessException{
		/**
		 * if the default changes by the developer and it was not saved and the value is still the default,
		 * it will continue not being saved! 
		 */
		Field fldDefault = null;
		try {fldDefault = this.getClass().getDeclaredField(fld.getName()+"Default");} catch (NoSuchFieldException | SecurityException e1) {}
		Object valueDefault = null;
		if(fldDefault!=null)valueDefault=fldDefault.get(this);
		
		if(valueDefault==null && bIfDefaultMissingReturnCurrent){
			return fld.get(this);
		}
				
		return valueDefault;
	}
	
	protected boolean checkSkip(Field fld){
//		try {
//			if(fld.get(this)==this){
//				return true;
//			}
//		} catch (IllegalArgumentException | IllegalAccessException e) {}
		
		//this$0
		try {if(fld.get(this)==ss.owner)return true;} catch (IllegalArgumentException | IllegalAccessException e) {}
		
		if(ISaveSkipper.class.isAssignableFrom(fld.getType()))return true;
		
		return false;
	}
	
	protected boolean allowAccess(Field fld){
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
	
	protected void restoreAccessMode(Field fld, boolean bWasAccessible){
		if(!bWasAccessible)fld.setAccessible(false);
	}
	
	/**
	 * This happens on the object being directly used by the application.
	 */
	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		for(Class clOwner:MiscI.i().getSuperClassesOf(this)){
			for(Field fld:clOwner.getDeclaredFields()){
//				/**
//				 * if the default changes by the developer and it was not saved and the value is still the default,
//				 * it will continue not being saved! 
//				 */
//				Field fldDefault = null;
//				try {fldDefault = this.getClass().getDeclaredField(fld.getName()+"Default");} catch (NoSuchFieldException | SecurityException e1) {}
//				Object valueDefault = null;
				
				Object[] aobjDbg = null;
				
				boolean bAccessible = allowAccess(fld);
				try {
					if(checkSkip(fld))continue;
					
//					float fD=1,f=fD;
//					if(fldDefault!=null)valueDefault=fldDefault.get(this);
					Object valueDefault = getDefaultValueFor(fld,false);
					Object objValue = fld.get(this);
					String strName = fld.getName();
					
					Class clValue=objValue.getClass();
					
					aobjDbg=new Object[]{clOwner,clValue,fld,strName,objValue,valueDefault,oc};
					
					/**
					 * A missing default means to always save.
					 * If default value is not present, try to use an invalid value (like Float.Nan) to grant 
					 * value will always be saved in case there is no default.
					 * To grant it will be saved, the default just needs to differ from the actual valid value.
					 */
					if(clValue==Float.class || clValue==float.class){
						if(valueDefault==null)valueDefault=Float.NaN;
						oc.write((float)objValue, strName, (float)valueDefault);
					}else
					if(clValue==Integer.class || clValue==int.class){
						if(valueDefault==null)valueDefault=((int)objValue)+1;
						oc.write((int)objValue, strName, (int)valueDefault);
					}else
					{throw new PrerequisitesNotMetException("unsupported value class type "+clValue,aobjDbg);}
					
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new PrerequisitesNotMetException("failed to colled field/default value",aobjDbg)
						.initCauseAndReturnSelf(e);
				}finally{
					restoreAccessMode(fld, bAccessible);
				}
				
//				if(fld.getType()==Float.class){
//						try {
//							if(fldDefault!=null)fValueDefault=fldDefault.getFloat(this);
//							oc.write(fld.getFloat(this), fld.getName(), fValueDefault);  
//						} catch (IllegalArgumentException | IllegalAccessException e) {
//							throw new PrerequisitesNotMetException("failed to colled field/default value",fld,fldDefault,fValueDefault,ex,oc)
//								.initCauseAndReturnSelf(e);
//						}
//				}
			}
		}
	}
	
//	private <T> void write(OutputCapsule oc, Class<T> cl, Field fld, Field fldDefault) throws IOException{
//		if(fld.getType()==cl){
//			Object[] aobjDbg = null;
//			try {
//				T valueDefault = null;
//				if(fldDefault!=null)valueDefault=(T)fldDefault.get(this);
//				aobjDbg=new Object[]{cl,fld,fldDefault,valueDefault,oc};
//				
//				/**
//				 * try to use an invalid value (like Float.Nan) to grant value will always be saved in case there is no default
//				 */
//				if(cl==Float.class || cl==float.class){
//					oc.write((float)fld.get(this), fld.getName(), valueDefault==null ? Float.NaN : (float)valueDefault);
//				}else
//				if(cl==Double.class || cl==double.class){
//					oc.write((double)fld.get(this), fld.getName(), valueDefault==null ? Double.NaN : (double)valueDefault);
//				}else
//				if(cl==Integer.class || cl==int.class){
//					oc.write((int)fld.get(this), fld.getName(), valueDefault==null ? Double.NaN : (double)valueDefault);
//				}else
//				{throw new PrerequisitesNotMetException("unsupported class type ",aobjDbg);}
//			} catch (IllegalArgumentException | IllegalAccessException e) {
//				throw new PrerequisitesNotMetException("failed to colled field/default value",aobjDbg)
//					.initCauseAndReturnSelf(e);
//			}
//		}
//	}
	
	/**
	 * This happens on a new instance, requires {@link #applyValuesFrom(CompositeSavableAbs)} to be made useful.
	 */
	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		for(Class clOwner:MiscI.i().getSuperClassesOf(this)){
			for(Field fld:clOwner.getDeclaredFields()){
				Class clField=null;
				
				boolean bAccessible = allowAccess(fld);
				try {
					if(checkSkip(fld))continue;
					
					clField = fld.getType();
					if(clField==Float.class || clField==float.class){
						fld.set(this, ic.readFloat(fld.getName(), (float)getDefaultValueFor(fld,true)));
//						if(new Float(fld.getFloat(this)).isNaN())throw new IllegalArgumentException("could not load");
					}else
					if(clField==Integer.class || clField==int.class){
						fld.set(this, ic.readInt(fld.getName(), (int)getDefaultValueFor(fld,true)));
					}else
					{}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					GlobalCommandsDelegatorI.i().dumpExceptionEntry(e,this,im,ic,clOwner,fld,clField);
					ss.bFailedToLoad=true;
					break;
				}finally{
					restoreAccessMode(fld, bAccessible);
				}
			}
		}
	}
	
	public boolean applyValuesFrom(S svLoaded) {
		return applyValuesFrom(svLoaded,false);
	}
	private boolean applyValuesFrom(S svLoaded, boolean bRestoring) {
		if(svLoaded==null){
			GlobalCommandsDelegatorI.i().dumpWarnEntry("null, nothing loaded",this,svLoaded);
			return false;
		}
		
		S svBkpSelf = null;
		if(!bRestoring){
			if(svLoaded.isFailedToLoad()){
				GlobalCommandsDelegatorI.i().dumpWarnEntry("cannot apply values from a 'failed to load' object",this,svLoaded);
				return false;
			}
			
			try {
				svBkpSelf = (S)this.getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e1) {
				throw new PrerequisitesNotMetException("missing empty constructor",this).initCauseAndReturnSelf(e1);
			}
		}
		
		labelClassLoop:for(Class cl:MiscI.i().getSuperClassesOf(this)){
			if(!cl.isInstance(svLoaded))throw new PrerequisitesNotMetException("incompatible",cl,this,svLoaded);
			for(Field fld:cl.getDeclaredFields()){
				boolean bAccessible = allowAccess(fld);
				try {
					if(checkSkip(fld))continue;
					
//					fld.set(this, cl.getDeclaredField(fld.getName()).get(svLoaded));
					fld.set(this, fld.get(svLoaded));
				} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
					if(bRestoring){
						throw new PrerequisitesNotMetException("during restore values", this, svLoaded, cl, fld).initCauseAndReturnSelf(e);
					}else{
						GlobalCommandsDelegatorI.i().dumpExceptionEntry(e,this,cl,fld,svLoaded);
						this.applyValuesFrom(svBkpSelf,true); //restore values to remove any inconsistency
					}
					
					break labelClassLoop;
				}finally{
					restoreAccessMode(fld, bAccessible);
				}
			}
		}
		
		return true;
	}
}
