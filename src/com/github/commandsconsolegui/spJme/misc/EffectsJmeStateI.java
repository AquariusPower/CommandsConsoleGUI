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
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.DebugData;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.EDbgStkOrigin;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.RunMode;
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
public class EffectsJmeStateI extends ConditionalStateAbs<EffectsJmeStateI>{
	private static EffectsJmeStateI instance = new EffectsJmeStateI();
	public static EffectsJmeStateI i(){return instance;}
	
	public final StringCmdField scfTest = new StringCmdField(this)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				addEffect(
					new EffectElectricity(this)
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
		public boolean isPlaying();
		
		public String getUId();
		
		public void assertConfigIsValid();
		
		public void play(float tpf);
		public Object getOwner();
		public void setSkipDiscardingByOwner();

		public void setPlay(boolean b);
		
		public void setAsDiscarded();

		public boolean isDiscardingByOwner();
	}
	
	private String strLastUId="0";
	
	/**
	 * TODO improve with aura particles shader, mesh, texture? 
	 * TODO create like 10 patterns and randomize thru them to be less CPU intensive? also rotate them in teh "direction axis" to look more randomized.
	 */
	public static class EffectElectricity implements IEffect{
		private DebugData dbg;
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
		private ColorRGBA	colorRefDefault=ColorRGBA.White.clone();
		private ColorRGBA	colorRefBase;
//		private int	iMaxAllowedParts;
		private Node	nodeParent;
		private Geometry	geom;
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
		private boolean	bDiscarded;
		private int	iMaxHoldMilis = 1000;
		private boolean	bDiscardingByOwner=true;
		
		/**
		 * 
		 * @param colorBase
		 * @param nodeParent
		 */
		public EffectElectricity(Object objOwner){
			if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg, EDbgStkOrigin.Constructed);
			this.objOwner=objOwner;
			
			this.geom = new Geometry("Geom:"+EffectElectricity.class.getSimpleName());
			Mesh mesh = new Mesh();
			mesh.setStreamed();
			this.geom.setMesh(mesh);
		}
		public EffectElectricity setColor(ColorRGBA colorRef){
			this.colorRefBase = colorRef!=null ? colorRef : colorRefDefault;
			this.geom.setMaterial(MiscJmeI.i().retrieveMaterialUnshadedColor(colorRef));
			return this;
		}
		public EffectElectricity setNodeParent(Node node){
			this.nodeParent=node;
			return this;
		}
		public EffectElectricity setFromTo(Vector3f v3fFrom, Vector3f v3fTo){
			assertNotDiscarded();
			this.v3fFrom=PrerequisitesNotMetException.assertNotNull(v3fFrom,this); 
			this.v3fTo=PrerequisitesNotMetException.assertNotNull(v3fTo,this);
			return this;
		}
		public EffectElectricity setFollowToMouse(boolean b){
			assertNotDiscarded();
			this.bToMouse=b;
			return this;
		}
		public EffectElectricity setFollowFromTarget(Spatial spt, Vector3f v3fDisplacement){
			assertNotDiscarded();
			sptFollowFrom=spt;
			v3fFollowFromDisplacement = v3fDisplacement==null?new Vector3f():v3fDisplacement;
			return this;
		}
		public EffectElectricity setFollowToTarget(Spatial spt, Vector3f v3fDisplacement){
			if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg);
			assertNotDiscarded();
			sptFollowTo=spt;
			v3fFollowToDisplacement = v3fDisplacement==null?new Vector3f():v3fDisplacement;
			return this;
		}
		
		@Override
		public String getUId(){
			assertNotDiscarded();
			return strUId;
		}
		
		@Override
		public void play(float tpf) {
			assertNotDiscarded();
			if(!bPlay)return;
			
			if(!nodeParent.hasChild(geom))nodeParent.attachChild(this.geom);

//			if(bDiscarding)return;
			
			MiscJmeI.i().updateMultiLineMesh(geom.getMesh(), recreatePath().toArray());
			geom.getMaterial().getAdditionalRenderState().setLineWidth(getThickNess());
//			Geometry geomNew = null;// MiscJmeI.i().createMultiLineGeom(recreatePath().toArray(), colorBase, FastMath.nextRandomFloat()*4+1);
//			geomNew.setLocalTranslation(getLocationFrom());
			geom.setLocalTranslation(getLocationFrom());
//			geomNew.getLocalTranslation().z+=0.03f;
//			geomNew.getLocalTranslation().setZ(v3fFrom.z); //getLocationTo()
			
//			if(geom!=null)geom.removeFromParent();
			
//			nodeParent.attachChild(geomNew);
			
//			geom=geomNew;
		}
		private Vector3f getLocationTo() {
			Vector3f v3fTargetSpot = v3fTo==null?null:v3fTo.clone();
			if(bToMouse){
				v3fTargetSpot=ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f();
				v3fTargetSpot.z=v3fTo.z;
			}else
			if(sptFollowTo!=null){
				//TODO if sptFollow is a node, add a node to it and apply displacement to let rotations etc apply
				v3fTargetSpot=sptFollowTo.getWorldTranslation().add(v3fFollowToDisplacement);
			}
			return v3fTargetSpot;
		}
		private Vector3f getLocationFrom() {
			Vector3f v3fTargetSpot = v3fFrom==null?null:v3fFrom.clone();
			if(sptFollowFrom!=null){
				//TODO if sptFollow is a node, add a node to it and apply displacement to let rotations etc apply
				v3fTargetSpot=sptFollowFrom.getWorldTranslation().add(v3fFollowFromDisplacement);
			}
			return v3fTargetSpot;
		}
		public long getThickNess(){
			long lRemainMilis = iHoldUntilMilis - GlobalSimulationTimeI.i().getMilis();
			long lMaxThickness = 8;
			long lThicknessStepMilis = iMaxHoldMilis/lMaxThickness;
			long lCurrentThickness = lRemainMilis/lThicknessStepMilis;
//			return FastMath.nextRandomFloat()*4+1;
			return lCurrentThickness>=1 ? lCurrentThickness : 1;
		}
		
		public BfdArrayList<Vector3f> recreatePath() {
			assertNotDiscarded();
			Vector3f v3fTargetSpot=getLocationTo();
			
			int iPartMaxDotsCurrent = iPartMaxDots;
			int iDotsMaxDist = (int) getLocationFrom().distance(v3fTargetSpot);
			if(iDotsMaxDist<iPartMaxDots)iPartMaxDotsCurrent=iDotsMaxDist;
			
			Vector3f v3fDirectionNormalized = v3fTargetSpot.subtract(getLocationFrom()).normalize();
			Vector3f v3fRelativePartStepMaxPos = v3fDirectionNormalized.mult(iPartMaxDotsCurrent);
			int iMinDotsLength = (int) (iPartMaxDotsCurrent*fPartMinPerc);
			int iMaxAllowedParts = (iDotsMaxDist/iMinDotsLength);
			
			boolean bUpdate=false;
			float fMaxMoveDetectDist=0.01f;
			if(v3fHoldPreviousFrom.distance(getLocationFrom()) > fMaxMoveDetectDist)bUpdate=true;
			if(v3fHoldPreviousTo.distance(getLocationTo()) > fMaxMoveDetectDist)bUpdate=true;
			if(iHoldUntilMilis < GlobalSimulationTimeI.i().getMilis()){
				iHoldUntilMilis = GlobalSimulationTimeI.i().getMilis() + FastMath.nextRandomInt(250, iMaxHoldMilis );
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
				float fDots=iPartMaxDotsCurrent*fAmplitudePerc;
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
			assertNotDiscarded();
			return objOwner;
		}
		@Override
		public void setPlay(boolean b) {
			assertNotDiscarded();
			this.bPlay=b;
			if(!this.bPlay){
				if(geom!=null)geom.removeFromParent();
			}
		}
		@Override
		public void setAsDiscarded() {
			if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg, EDbgStkOrigin.Discarded);
			setPlay(false);
			if(geom!=null)geom.removeFromParent();
			bDiscarded=true;
		}
		private void assertNotDiscarded(){
			if(bDiscarded){
				throw new PrerequisitesNotMetException("cant work with a discarded effect");
			}
		}
		@Override
		public void assertConfigIsValid() {
			assertNotDiscarded();
			if(!isPlaying())return; //not redundant when called directly after adding the effect
			
			if(colorRefBase==null){
				setColor(colorRefDefault);
			}
			
			if(v3fFrom==null && sptFollowFrom==null){
				throw new PrerequisitesNotMetException("playing and 'from' not set",this);
			}
			
			if(v3fTo==null && sptFollowTo==null && !bToMouse){
				throw new PrerequisitesNotMetException("playing and 'to' not set",this);
			}
			
			if(nodeParent==null){
				if (objOwner instanceof ILinkedSpatial) {
					ILinkedSpatial ils = (ILinkedSpatial) objOwner;
					// the top node
					nodeParent=(Node)MiscJmeI.i().getParentestFrom(ils.getLinkedSpatial(),false); 
				}else{
					if (objOwner instanceof Spatial) {
						Spatial spt = (Spatial) objOwner;
						nodeParent=(Node)MiscJmeI.i().getParentestFrom(spt,false);
					}
				}
				
				PrerequisitesNotMetException.assertNotNull(nodeParent, "parent", objOwner, this);
			}
		}
		@Override
		public boolean isPlaying() {
			return bPlay;
		}
		public void setAmplitudePerc(float f) {
			this.fAmplitudePerc=f;
		}
		@Override
		public void setSkipDiscardingByOwner() {
			bDiscardingByOwner=false;
		}
		@Override
		public boolean isDiscardingByOwner() {
			return bDiscardingByOwner;
		}
		
	}
	
	HashMap<String,IEffect> hmEffects = new HashMap<String, IEffect>();
	
	public <T extends IEffect> T addEffect(T ieff){
		if(ieff.isPlaying())ieff.assertConfigIsValid();
		hmEffects.put(ieff.getUId(),ieff);
		return ieff;
	}
	
	public void discardEffect(IEffect ie){
		hmEffects.remove(ie.getUId(),ie);
		ie.setAsDiscarded();
	}
	
	public void discardEffectsForOwner(Object objOwner){
		for(IEffect ie:hmEffects.values().toArray(new IEffect[0])){
			if(!ie.isDiscardingByOwner())continue;
			
			if(ie.getOwner()==objOwner){
				discardEffect(ie);
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
				ie.assertConfigIsValid(); //config may change during play
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

	public boolean containsEffect(EffectElectricity ieffAlert) {
		return hmEffects.get(ieffAlert.getUId())!=null;
	}
}
