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

import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
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
						new Vector3f(10,10,100),
						new Vector3f(500,500,100),
						ColorRGBA.Cyan, 
						GlobalSimpleAppRefI.i().getGuiNode()
					)
					.setFollowMouse(true)
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
		private Spatial	sptFollow;
//		private boolean	bDiscarding;
		private Vector3f	v3fFollowDisplacement;
		
		/**
		 * 
		 * @param v3fFrom
		 * @param v3fTo
		 * @param colorBase
		 * @param nodeParent
		 */
		public EffectElectricity(Object objOwner, Vector3f v3fFrom, Vector3f v3fTo, ColorRGBA colorBase, Node nodeParent){
			this.objOwner=objOwner;
			this.nodeParent=nodeParent;
			this.colorBase=colorBase;
			
			this.v3fFrom=v3fFrom; 
			this.v3fTo=v3fTo;
			
//			this.v3fDirectionNormalized = v3fTo.subtract(v3fFrom).normalize();
			
//			fDist = v3fFrom.distance(v3fTo);
//			iMaxAllowedParts = (int) (fDist/fPartMinPerc);
			
//			v3fRelativePartStepMaxPos = v3fDirectionNormalized.mult(iPartMaxDots);
		}
		public EffectElectricity setFollowMouse(boolean b){
			this.bToMouse=b;
			return this;
		}
		public EffectElectricity setFollowTarget(Spatial spt, Vector3f v3fDisplacement){
			sptFollow=spt;
			v3fFollowDisplacement = v3fDisplacement==null?new Vector3f():v3fDisplacement;
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
				createPath().toArray(), ColorRGBA.Cyan, FastMath.nextRandomFloat()*4+1);
			geomNew.setLocalTranslation(v3fFrom);
//			geomNew.getLocalTranslation().setZ(v3fFrom.z);
			
			if(geomLast!=null)geomLast.removeFromParent();
			
			nodeParent.attachChild(geomNew);
			
			geomLast=geomNew;
		}
		private Vector3f getLocationTo() {
			Vector3f v3fTargetSpot=v3fTo.clone();
			if(bToMouse){
				v3fTargetSpot=ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f();
				v3fTargetSpot.z=v3fTo.z;
			}else
			if(sptFollow!=null){
				//TODO if sptFollow is a node, add a node to it and apply displacement to let rotations etc apply
				v3fTargetSpot=sptFollow.getLocalTranslation().add(v3fFollowDisplacement);
			}
			return v3fTargetSpot;
		}
		public BfdArrayList<Vector3f> createPath() {
			Vector3f v3fTargetSpot=getLocationTo();
			
			Vector3f v3fDirectionNormalized = v3fTargetSpot.subtract(v3fFrom).normalize();
			Vector3f v3fRelativePartStepMaxPos = v3fDirectionNormalized.mult(iPartMaxDots);
			int iDotsMaxDist = (int) v3fFrom.distance(v3fTargetSpot);
			int iMinDotsLength = (int) (iPartMaxDots*fPartMinPerc);
			int iMaxAllowedParts = (iDotsMaxDist/iMinDotsLength);
			
			BfdArrayList<Vector3f> av3fList = new BfdArrayList<Vector3f>() {};
			Vector3f v3fPartStart = v3fFrom.clone();
			av3fList.add(v3fPartStart.clone());
//			for(int i=0;i<iMaxAllowedParts;i++){
			while(true){
//				Vector3f v3fPartEnd;
//				int iDotsCurrentDist = (int) v3fFrom.distance(v3fPartStart);
//				if( bMaxPartsReached || (v3fPartStart.distance(v3fTargetSpot) < iMinDotsLength) ){
//				if( bMaxPartsReached || (iDotsCurrentDist>=iDotsMaxDist) ){
//					av3fList.remove(av3fList.size()-1);
//					v3fPartEnd=v3fTargetSpot.clone();
//					if(bMaxPartsReached)MsgI.i().devInfo("max parts reached, is the code well implemented?",this,getUId());
//					break;
//				}
				
				// move a bit towards the end
				Vector3f v3fPartEnd = new Vector3f(v3fPartStart);
				float fPerc = fPartMinPerc + (FastMath.nextRandomFloat() * fDeltaPerc);
				v3fPartEnd.interpolateLocal(v3fPartEnd.add(v3fRelativePartStepMaxPos), fPerc);
				
				// random coolness missplacement
				float fDots=iPartMaxDots*fAmplitudePerc;
				v3fPartEnd.x+=MiscI.i().randomMinusOneToPlusOne()*fDots;
				v3fPartEnd.y+=MiscI.i().randomMinusOneToPlusOne()*fDots;
				v3fPartEnd.z+=MiscI.i().randomMinusOneToPlusOne()*fDots;
				
				int iDotsCurrentDist = (int) v3fFrom.distance(v3fPartEnd);
				boolean bBreak = false;
				if(!bBreak && av3fList.size()==iMaxAllowedParts-1){
					MsgI.i().devInfo("max parts reached, is the code well implemented?",this,getUId());
					bBreak=true; //max parts reached 
				}
				if(!bBreak && (iDotsCurrentDist>=iDotsMaxDist))bBreak=true; //max parts reached 
//				if( bBreak || (iDotsCurrentDist>=iDotsMaxDist) ){
//					av3fList.remove(av3fList.size()-1);
				if(bBreak){
					v3fPartEnd=v3fTargetSpot.clone();
//					if(bBreak)MsgI.i().devInfo("max parts reached, is the code well implemented?",this,getUId());
//					break;
				}
				
				av3fList.add(v3fPartEnd.clone());
				
				if(bBreak)break;
				
				v3fPartStart = v3fPartEnd.clone();
			}
			
			for(Vector3f v3f:av3fList){
				v3f.subtractLocal(v3fFrom); //the mesh is relative to the geometry
			}
			
			return av3fList;
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
		
	}
	
	HashMap<String,IEffect> hmEffects = new HashMap<String, IEffect>();
	
	public <T extends IEffect> T addEffect(T ie){
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
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		for(IEffect ie:hmEffects.values()){
			ie.play(tpf);
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
