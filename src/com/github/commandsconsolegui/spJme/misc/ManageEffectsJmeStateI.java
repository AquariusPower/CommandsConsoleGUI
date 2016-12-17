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
package com.github.commandsconsolegui.spJme.misc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalSimulationTimeI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.IInstance;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI;
import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.DebugData;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.EDbgStkOrigin;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RunMode;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spJme.ConditionalStateAbs;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageEffectsJmeStateI extends ConditionalStateAbs<ManageEffectsJmeStateI> implements IManager<ManageEffectsJmeStateI.IEffect>,IInstance{
	private static ManageEffectsJmeStateI instance = new ManageEffectsJmeStateI();
	public static ManageEffectsJmeStateI i(){return instance;}
	
	public static final class CompositeControl extends CompositeControlAbs<ManageEffectsJmeStateI>{
		private CompositeControl(ManageEffectsJmeStateI cc){super(cc);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	public ManageEffectsJmeStateI() {
		DelegateManagerI.i().addManager(this,IEffect.class);
	}
	
//	public final StringCmdField scfTest = new StringCmdField(this)
//		.setCallerAssigned(new CallableX(this) {
//			@Override
//			public Boolean call() {
////				addEffect(
//					new EffectElectricity(this)
//						.setFromTo(new Vector3f(10,10,100),null)
//						.setFollowToMouse(true)
//						.setPlay(true);
////				);
//				return true;
//			}
//		});
	
	public static class CfgParm extends ConditionalStateAbs.CfgParm{
		public CfgParm() {super(null);}
	}
	CfgParm cfg;
	@Override
	public ManageEffectsJmeStateI configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		super.configure(icfg);
		return getThis();
	}
	
	public static interface IEffect<THIS extends IEffect>{
		public boolean isPlaying();
		
		public Vector3f getLocationFrom();

		public Vector3f getLocationTo();

		void assertNotDiscarded();
		
		THIS setOwner(Object objOwner);
		
		THIS setColor(ColorRGBA colorRef);

		THIS setFromTo(Vector3f v3fFrom, Vector3f v3fTo);

		THIS setNodeParent(Node node);

		THIS setFollowToMouse(boolean b);

		THIS setFollowToTarget(Spatial spt, Vector3f v3fDisplacement);

		THIS setFollowFromTarget(Spatial spt, Vector3f v3fDisplacement);
		
		THIS getThis();
		
		THIS clone();
		
//		public String getUId();
		
		public void assertConfigIsValid();
		
		public void play(ManageEffectsJmeStateI.CompositeControl cc, float tpf);
		
		public Object getOwner();
		
		public THIS setSkipDiscardingByOwner();

		public THIS setPlay(boolean b);
		
		public THIS setAsDiscarded();

		public boolean isDiscardingByOwner();
	}
	
	private String strLastUId="0";
	
//	HashMap<String,IEffect> hmEffects = new HashMap<String, IEffect>();
	BfdArrayList<IEffect> aEffectList = new BfdArrayList<IEffect>(){};
	
	public void discardEffect(IEffect ie){
		aEffectList.remove(ie);//ie.getUId(),ie);
		ie.setAsDiscarded();
	}
	
	public void discardEffectsForOwner(Object objOwner){
		for(IEffect ie:aEffectList.toArray()){//.values().toArray(new IEffect[0])){
			if(!ie.isDiscardingByOwner())continue;
			
			if(ie.getOwner()==objOwner){
				discardEffect(ie);
			}
		}
	}
	
	long lLastUpdateMilis=GlobalSimulationTimeI.i().getMillis();
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		int iFPStarget=15;
		if(lLastUpdateMilis+(1000/iFPStarget) < GlobalSimulationTimeI.i().getMillis()){
			for(IEffect ie:aEffectList){//.values()){
				if(!ie.isPlaying())continue;
				ie.assertConfigIsValid(); //config may change during play
				ie.play(ccSelf,tpf);
			}
			lLastUpdateMilis=GlobalSimulationTimeI.i().getMillis();
		}
		
		return true;
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {return null;}

	@Override
	protected ManageEffectsJmeStateI getThis() {
		return this;
	}

	public boolean containsEffect(IEffect ieffAlert) {
		return aEffectList.contains(ieffAlert);
//		return hmEffects.get(ieffAlert.getUId())!=null;
	}

	@Override
	public boolean addHandled(IEffect ieff) {
//		boolean b = !hmEffects.containsKey(obj.getUId());
//		addEffect(obj);
//		return b;
		
		PrerequisitesNotMetException.assertNotAlreadyAdded(aEffectList, ieff, this);
//		PrerequisitesNotMetException.assertNotAlreadySet(hmEffects.get(ieff.getUId()), ieff, this);
//		return hmEffects.put(ieff.getUId(),ieff)==null;
		
		return aEffectList.add(ieff);
	}

	@Override
	public ArrayList<IEffect> getHandledListCopy() {
		return aEffectList.getCopy();
//		return new ArrayList<IEffect>(hmEffects.values());
	}

	@Override
	public boolean isInstanceReady() {
		return instance!=null;
	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,	IllegalAccessException {
		if(fld.getDeclaringClass()!=ManageEffectsJmeStateI.class)return super.getFieldValue(fld); //For subclasses uncomment this line
		return fld.get(this);
	}
	
	@Override
	public void setFieldValue(Field fld, Object value)throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=ManageEffectsJmeStateI.class){super.setFieldValue(fld,value);return;} //For subclasses uncomment this line
		fld.set(this,value);
	}
}
