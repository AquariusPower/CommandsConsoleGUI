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

import java.util.HashMap;

import com.github.commandsconsolegui.spAppOs.globals.GlobalSimulationTimeI;
import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spJme.ConditionalStateAbs;
import com.github.commandsconsolegui.spJme.ManageMouseCursorI;
import com.github.commandsconsolegui.spJme.globals.GlobalSimpleAppRefI;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class EffectsJmeStateI extends ConditionalStateAbs<EffectsJmeStateI>{
	private static EffectsJmeStateI instance = new EffectsJmeStateI();
	public static EffectsJmeStateI i(){return instance;}
	
	public final StringCmdField scfTest = new StringCmdField(this)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				addEffect(
					new EffectElectricity(
						this,
						ColorRGBA.Cyan, 
						GlobalSimpleAppRefI.i().getGuiNode()
					)
					.setFromTo(new Vector3f(10,10,100),null)
					.setFollowToMouse(true)
				);
				return true;
			}
		});
	
	public static class CfgParm extends ConditionalStateAbs.CfgParm{
		public CfgParm() {super(null);}
	}
	CfgParm cfg;
	@Override
	public EffectsJmeStateI configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		super.configure(icfg);
		return getThis();
	}
	
	public static interface IEffect{
		public String getUId();
		
		public void assertConfigIsValid();
		
		public void play(float tpf);
		public Object getOwner();

		public void pause();
		
		public void prepareToBeDiscarded();
	}
	
	private String strLastUId="0";
	
	/**
	 * TODO improve with aura particles shader, mesh, texture? 
	 * TODO create like 10 patterns and randomize thru them to be less CPU intensive? also rotate them in teh "direction axis" to look more randomized.
	 */
	public static class EffectElectricity implements IEffect{
		private String	strUId = EffectsJmeStateI.i().strLastUId = MiscI.i().getNextUniqueId(EffectsJmeStateI.i().strLastUId);
		private Vector3f	v3fFrom;
		private Vector3f	v3fTo;
//		private Vector3f	v3fDirectionNormalized;
//		private float	fDist;
		private float	fPartMinPerc=0.20f;
		private float	fPartMaxPerc=1.00f;
		private float fDeltaPerc = fPartMaxPerc-fPartMinPerc;
		private float fAmplitudePerc = 0.15f;
		private int	iParts;
		private ColorRGBA	colorBase;
//		private int	iMaxAllowedParts;
		private Node	nodeParent;
		private Geometry	geomLast;
		private int	iPartMaxDots = 100;
//		private Vector3f v3fRelativePartStepMaxPos;
		private boolean	bToMouse;
		private Object	objOwner;
		private boolean	bPlay = true;
		private Spatial	sptFollowTo;
//		private boolean	bDiscarding;
		private Vector3f	v3fFollowToDisplacement;
		private Spatial	sptFollowFrom;
		private Vector3f	v3fFollowFromDisplacement;
		private BfdArrayList<Vector3f>	av3fList  = new BfdArrayList<Vector3f>() {};
		private long	iHoldUntilMilis;
		private Vector3f	v3fHoldPreviousFrom=new Vector3f();
		private Vector3f	v3fHoldPreviousTo=new Vector3f();
		
		/**
		 * 
		 * @param colorBase
		 * @param nodeParent
		 */
		public EffectElectricity(Object objOwner, ColorRGBA colorBase, Node nodeParent){
			this.objOwner=objOwner;
			this.nodeParent=nodeParent;
			this.colorBase=colorBase;
			
//			this.v3fDirectionNormalized = v3fTo.subtract(v3fFrom).normalize();
			
//			fDist = v3fFrom.distance(v3fTo);
//			iMaxAllowedParts = (int) (fDist/fPartMinPerc);
			
//			v3fRelativePartStepMaxPos = v3fDirectionNormalized.mult(iPartMaxDots);
		}
		public EffectElectricity setFromTo(Vector3f v3fFrom, Vector3f v3fTo){
			this.v3fFrom=v3fFrom; 
			this.v3fTo=v3fTo;
			return this;
		}
		public EffectElectricity setFollowToMouse(boolean b){
			this.bToMouse=b;
			return this;
		}
		public EffectElectricity setFollowFromTarget(Spatial spt, Vector3f v3fDisplacement){
			sptFollowFrom=spt;
			v3fFollowFromDisplacement = v3fDisplacement==null?new Vector3f():v3fDisplacement;
			return this;
		}
		public EffectElectricity setFollowToTarget(Spatial spt, Vector3f v3fDisplacement){
			sptFollowTo=spt;
			v3fFollowToDisplacement = v3fDisplacement==null?new Vector3f():v3fDisplacement;
			return this;
		}
		
		@Override
		public String getUId(){
			return strUId;
		}
		
		@Override
		public void play(float tpf) {
			if(!bPlay)return;
//			if(bDiscarding)return;
			
			Geometry geomNew = MiscJmeI.i().createMultiLineGeom(
				recreatePath().toArray(), ColorRGBA.Cyan, FastMath.nextRandomFloat()*4+1);
			geomNew.setLocalTranslation(getLocationFrom());
//			geomNew.getLocalTranslation().z+=0.03f;
//			geomNew.getLocalTranslation().setZ(v3fFrom.z); //getLocationTo()
			
			if(geomLast!=null)geomLast.removeFromParent();
			
			nodeParent.attachChild(geomNew);
			
			geomLast=geomNew;
		}
		private Vector3f getLocationTo() {
			Vector3f v3fTargetSpot = v3fTo==null?null:v3fTo.clone();
			if(bToMouse){
				v3fTargetSpot=ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f();
				v3fTargetSpot.z=v3fTo.z;
			}else
			if(sptFollowTo!=null){
				//TODO if sptFollow is a node, add a node to it and apply displacement to let rotations etc apply
				v3fTargetSpot=sptFollowTo.getLocalTranslation().add(v3fFollowToDisplacement);
			}
			return v3fTargetSpot;
		}
		private Vector3f getLocationFrom() {
			Vector3f v3fTargetSpot = v3fFrom==null?null:v3fFrom.clone();
			if(sptFollowFrom!=null){
				//TODO if sptFollow is a node, add a node to it and apply displacement to let rotations etc apply
				v3fTargetSpot=sptFollowFrom.getLocalTranslation().add(v3fFollowFromDisplacement);
			}
			return v3fTargetSpot;
		}
		public BfdArrayList<Vector3f> recreatePath() {
			Vector3f v3fTargetSpot=getLocationTo();
			
			Vector3f v3fDirectionNormalized = v3fTargetSpot.subtract(getLocationFrom()).normalize();
			Vector3f v3fRelativePartStepMaxPos = v3fDirectionNormalized.mult(iPartMaxDots);
			int iDotsMaxDist = (int) getLocationFrom().distance(v3fTargetSpot);
			int iMinDotsLength = (int) (iPartMaxDots*fPartMinPerc);
			int iMaxAllowedParts = (iDotsMaxDist/iMinDotsLength);
			
			boolean bUpdate=false;
			float fMaxMoveDetectDist=0.01f;
			if(v3fHoldPreviousFrom.distance(getLocationFrom()) > fMaxMoveDetectDist)bUpdate=true;
			if(v3fHoldPreviousTo.distance(getLocationTo()) > fMaxMoveDetectDist)bUpdate=true;
			if(iHoldUntilMilis < GlobalSimulationTimeI.i().getMilis()){
				iHoldUntilMilis = GlobalSimulationTimeI.i().getMilis() + FastMath.nextRandomInt(250, 3000);
				bUpdate=true;
			}
			if(bUpdate){
				av3fList.clear();
				// updating
				v3fHoldPreviousFrom.set(getLocationFrom());
				v3fHoldPreviousTo.set(getLocationTo());
			}else{
				// holding
				spreadInnersABit(av3fList);
				return av3fList;
			}
			
			Vector3f v3fPartStart = getLocationFrom();
			av3fList.add(v3fPartStart.clone());
			while(true){
				// move a bit towards the end
				Vector3f v3fPartEnd = new Vector3f(v3fPartStart);
				float fPerc = fPartMinPerc + (FastMath.nextRandomFloat() * fDeltaPerc);
				v3fPartEnd.interpolateLocal(v3fPartEnd.add(v3fRelativePartStepMaxPos), fPerc);
				
				// random coolness missplacement
				float fDots=iPartMaxDots*fAmplitudePerc;
				v3fPartEnd.x+=MiscI.i().randomMinusOneToPlusOne()*fDots;
				v3fPartEnd.y+=MiscI.i().randomMinusOneToPlusOne()*fDots;
				v3fPartEnd.z+=MiscI.i().randomMinusOneToPlusOne()*fDots;
				
				int iDotsCurrentDist = (int) getLocationFrom().distance(v3fPartEnd);
				boolean bBreak = false;
				if(!bBreak && av3fList.size()==iMaxAllowedParts-1){
					MsgI.i().devInfo("max parts reached, is the code well implemented?",this,getUId());
					bBreak=true; //max parts reached 
				}
				if(!bBreak && (iDotsCurrentDist>=iDotsMaxDist))bBreak=true; //max parts reached 
				if(bBreak){
					v3fPartEnd=v3fTargetSpot.clone();
				}
				
				av3fList.add(v3fPartEnd.clone());
				
				if(bBreak)break;
				
				v3fPartStart = v3fPartEnd.clone();
			}
			
			for(Vector3f v3f:av3fList){
				v3f.subtractLocal(getLocationFrom()); //the mesh is relative to the geometry
			}
			
			return av3fList;
		}
		private void spreadInnersABit(BfdArrayList<Vector3f> av3fList) {
			Vector3f v3fFromTmp=av3fList.get(0);
			Vector3f v3fToTmp=av3fList.get(av3fList.size()-1);
			float fDistMax=v3fFromTmp.distance(v3fToTmp);
			for(int i=1;i<av3fList.size()-1;i++){
				Vector3f v3f=av3fList.get(i);
//			for(Vector3f v3f:av3fList){
				/**
				 * throw a line from <---> to
				 * get the nearest point at it relative to the inner part vertex
				 * not need to be precise tho :(
				 */
				float fDist=v3fFromTmp.distance(v3f);
				Vector3f v3fNearest = v3fFromTmp.clone().interpolateLocal(v3fToTmp, fDist/fDistMax);
				Vector3f v3fNew = v3fNearest.clone().interpolateLocal(v3f, 1.02f);
				v3f.set(v3fNew);
			}
		}
		@Override
		public Object getOwner() {
			return objOwner;
		}
		@Override
		public void pause() {
			bPlay=false;
		}
		@Override
		public void prepareToBeDiscarded() {
			pause();
			if(geomLast!=null)geomLast.removeFromParent();
//			bDiscarding=true;
		}
		@Override
		public void assertConfigIsValid() {
			if(v3fFrom==null && sptFollowFrom==null){
				throw new PrerequisitesNotMetException("'from' not set",this);
			}
			
			if(v3fTo==null && sptFollowTo==null && !bToMouse){
				throw new PrerequisitesNotMetException("'to' not set",this);
			}
		}
		
	}
	
	HashMap<String,IEffect> hmEffects = new HashMap<String, IEffect>();
	
	public <T extends IEffect> T addEffect(T ie){
		ie.assertConfigIsValid();
		hmEffects.put(ie.getUId(),ie);
		return ie;
	}
	
	public void removeEffect(IEffect ie){
		ie.prepareToBeDiscarded();
		hmEffects.remove(ie.getUId(),ie);
	}
	
	public void removeEffectsForOwner(Object objOwner){
		for(IEffect ie:hmEffects.values().toArray(new IEffect[0])){
			if(ie.getOwner()==objOwner){
				removeEffect(ie);
			}
		}
	}
	
	long lLastUpdateMilis=GlobalSimulationTimeI.i().getMilis();
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		int iFPStarget=15;
		if(lLastUpdateMilis+(1000/iFPStarget) < GlobalSimulationTimeI.i().getMilis()){
			for(IEffect ie:hmEffects.values()){
				ie.play(tpf);
			}
			lLastUpdateMilis=GlobalSimulationTimeI.i().getMilis();
		}
		
		return true;
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {return null;}

	@Override
	protected EffectsJmeStateI getThis() {
		return this;
	}
}
