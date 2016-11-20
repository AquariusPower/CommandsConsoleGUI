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

import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.effect.Effect;
import com.simsilica.lemur.effect.EffectInfo;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class LemurEffectsI implements IReflexFillCfg {
	private static LemurEffectsI instance = new LemurEffectsI();
	public static LemurEffectsI i(){return instance;}
	
	public static enum EEffChannel{
		ChnGrowShrink,
		;
		public String s(){return toString();}
	}
	
	public abstract class BfdEffect implements Effect<Panel>{
		public String getName(){
			return ReflexFillI.i().assertAndGetField(i(),this).getName();
		}
	}
	
	public final BfdEffect efGrow = new BfdEffect() {
		
		@Override
		public String getChannel() {
			return EEffChannel.ChnGrowShrink.s();
		}
		
		@Override
		public Animation create(final Panel target, EffectInfo existing) {
			
			Animation anim = new Animation() {
				Long lStartMilis=null;
				long lMaxMilis=250;
				
				@Override
				public void cancel() {
					target.setLocalScale(1);
				}
				
				@Override
				public boolean animate(double tpf) {
					if(lStartMilis==null)lStartMilis=System.currentTimeMillis();
					
					long lDiff = System.currentTimeMillis()-lStartMilis;
					
					float fPerc=(float)lDiff/(float)lMaxMilis;
					
					boolean b=true;
					if(fPerc>=1f){
						fPerc=1f;
						b=false;
					}
					
					target.setLocalScale(fPerc);
					
					return b;
				}
			};
			
			return anim;
		}
	};
	
	public void addEffectTo(Panel pnl, BfdEffect ef) {
		pnl.addEffect(ef.getName(), ef);
	}

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
	
}
