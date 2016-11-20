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
package com.github.commandsconsolegui.spLemur.misc;

import java.lang.reflect.Field;
import java.util.TreeMap;

import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI.IConfigure;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.anim.SpatialTweens;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.TweenAnimation;
import com.simsilica.lemur.anim.Tweens;
import com.simsilica.lemur.effect.AbstractEffect;
import com.simsilica.lemur.effect.EffectInfo;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class LemurEffectsI implements IReflexFillCfg, IConfigure<LemurEffectsI> {
	private static LemurEffectsI instance = new LemurEffectsI();
	public static LemurEffectsI i(){return instance;}
	
	public static enum EEffState{
		Show,
		Hide,
		CallAttention,
		LoopLight,
		LoopStrong,
	}
	
	public static enum EEffChannel{
		ChnGrowShrink,
		;
		private TreeMap<EEffState,BfdEffect> tmEf = new TreeMap<EEffState,BfdEffect>();
		private Vector3f	v3fPlayPosStart;
		public void putEffect(EEffState es, BfdEffect ef){
			PrerequisitesNotMetException.assertNotAlreadySet("effect", tmEf.get(es), ef, this);
			tmEf.put(es,ef);
		}
		public BfdEffect getEffect(EEffState es){
			return tmEf.get(es);
		}
		public BfdEffect[] getAllEffects(){
			return tmEf.values().toArray(new BfdEffect[0]);
		}
		public void applyEffectsAt(Panel pnl){
			for(BfdEffect ef:getAllEffects()){
				pnl.addEffect(ef.getName(), ef);
			}
		}
		public void play(EEffState es, Panel pnl, Vector3f v3fPosStart){
			this.v3fPlayPosStart=v3fPosStart;
			pnl.runEffect(tmEf.get(es).getName());
		}
		public String s(){return toString();}
	}
	
	public abstract class BfdEffect extends AbstractEffect<Panel>{
		protected EEffChannel	echn;

		public BfdEffect(EEffChannel echn, EEffState es) {
			super(echn.s());
			echn.putEffect(es,this);
			this.echn = echn;
		}

		public String getName(){
			return ReflexFillI.i().assertAndGetField(LemurEffectsI.i(), this).getName();
		}
	}
	
	BfdEffect efGrow = new BfdEffect(EEffChannel.ChnGrowShrink, EEffState.Show) {
    public Animation create( Panel target, EffectInfo existing ) {
    	Vector3f v3fOrigin = echn.v3fPlayPosStart.clone();//ManageMouseCursorI.i().getMouseCursorPositionCopyAsV3f(); //Vector3f.ZERO.clone();
    	
    	/**
    	 * it is subtract because the start pos is the lower left corner
    	 */
    	Vector3f v3fFrom = v3fOrigin.subtract(MiscLemurStateI.i().getRelativeCenterXYposOf(target));
//    	Vector3f v3fHalfSize = target.getSize().mult(0.5f);
//    	v3fHalfSize.y*=-1f;
//    	Vector3f v3fFrom = v3fOrigin.add(v3fHalfSize);
    	
    	Vector3f v3fTo = v3fOrigin.clone();
    	
    	float fTime=0.250f;
      Tween twMove = SpatialTweens.move(target, v3fFrom, v3fTo, fTime);
      Tween twScale = SpatialTweens.scale(target, 0, 1, fTime);
      return new TweenAnimation(Tweens.smoothStep(Tweens.parallel(twMove, twScale)));
    }
	};
	BfdEffect efShrink = new BfdEffect(EEffChannel.ChnGrowShrink, EEffState.Hide) {
	    public Animation create( Panel target, EffectInfo existing ) {
	        Tween move = SpatialTweens.move(target, new Vector3f(200, 200, 0), Vector3f.ZERO, 0.250);
	        Tween scale = SpatialTweens.scale(target, 1, 0, 0.250);
	        return new TweenAnimation(Tweens.smoothStep(Tweens.parallel(move, scale)));
	    }
	};
	private boolean	bConfigured;

//	public final BfdEffect efGrow = new BfdEffect() {
//		
//		@Override
//		public String getChannel() {
//			return EEffChannel.ChnGrowShrink.s();
//		}
//		
//		@Override
//		public Animation create(final Panel target, EffectInfo existing) {
//			
//			Animation anim = new Animation() {
//				Long lStartMilis=null;
//				long lMaxMilis=250;
//				
//				@Override
//				public void cancel() {
//					target.setLocalScale(1);
//				}
//				
//				@Override
//				public boolean animate(double tpf) {
//					if(lStartMilis==null)lStartMilis=System.currentTimeMillis();
//					
//					long lDiff = System.currentTimeMillis()-lStartMilis;
//					
//					float fPerc=(float)lDiff/(float)lMaxMilis;
//					
//					boolean b=true;
//					if(fPerc>=1f){
//						fPerc=1f;
//						b=false;
//					}
//					
//					target.setLocalScale(fPerc);
//					
//					return b;
//				}
//			};
//			
//			return anim;
//		}
//	};
	
//	public void applyEffectsAt(Panel pnl, EEffChannel e){
//		e.applyEffectsAt(pnl);
//	}
//	
//	public void addEffectTo(Panel pnl, BfdEffect ef) {
//		pnl.addEffect(ef.getName(), ef);
//	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,IllegalAccessException {
		return fld.get(this);
	}

	@Override
	public void setFieldValue(Field fld, Object value)throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {return null;}

	@Override
	public boolean isConfigured() {
		return bConfigured;
	}

	@Override
	public LemurEffectsI configure(ICfgParm icfg) {
		bConfigured=true;
		return this;
	}
	
}
