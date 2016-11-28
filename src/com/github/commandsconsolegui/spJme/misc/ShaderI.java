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

import com.github.commandsconsolegui.spJme.globals.GlobalSimpleAppRefI;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class ShaderI {
	private static ShaderI instance = new ShaderI();
	public static ShaderI i(){return instance;}
	
	public static enum EShaderMatDef{
		Sky2$Sky2,
		SimpleSprite$SimpleSprite,
		Bubble$Bubble,
		Glass$Glass,
		Electricity$Electricity1,
		Electricity$Electricity1_2,
		Electricity$Electricity4,
		Electricity$Electricity2,
		Electricity$Electricity2_2,
		Electricity$Electricity5,
		Electricity$Electricity3,
		Electricity$Electricity5_2,
		Dissolve$Lighting,
		SimpleSpriteParticle$SimpleSpriteParticle,
		SkyDome$SkyDome,
		SimpleRefraction$SimpleRefraction,
		ForceShield$ForceShield,
		Filters$MotionBlur$MotionBlur,
		Filters$FrostedGlass$FrostedGlass,
		Filters$GrayScale$GrayScale,
		Filters$NightVision$NightVision,
		Filters$CircularFading$CircularFading,
		Filters$PredatorVision$PredatorVision,
		Filters$Pixelation$Pixelation,
		Filters$OldFilm$OldFilm,
		Filters$SimpleRefraction$SimpleRefractionFilter,
		Filters$SimpleRefraction$Refract,
		Filters$BasicSSAO$BasicSSAOBlur,
		Filters$BasicSSAO$BasicSSAO,
		Filters$ColorScale$ColorScale,
		MatCap$MatCap,
		FakeParticleBlow$FakeParticleBlow,
		LightBlow$LightBlow,
		GPUAnimationFactory$GPUAnimationFactory,
		TextureBombing$TextureBombing,
		;
		
		private Material	mat;

		public String s(){return toString();}

		public Material getRawMaterial() {
			if(mat==null){
				mat = new Material(GlobalSimpleAppRefI.i().getAssetManager(),
					"ShaderBlow/MatDefs/"+s().replace("$","/")+".j3md");
			}
			return mat;
		}
	}
	
	/**
	 * TODO not all are usable at geometries...
	 * TODO these are raw matdefs, the materials pre-configured are at tests: .j3m 
	 * 
	 * @param geom
	 * @param e
	 * @return material for further setup
	 */
	public Material applyShaderMatDefAt(Geometry geom, EShaderMatDef e){
		Material mat = e.getRawMaterial();
		
		Geometry geomShadered = new Geometry(geom.getName()+":"+EShaderMatDef.class.getSimpleName()+":"+e);
		geomShadered.setQueueBucket(Bucket.Transparent);
		
		geomShadered.setMesh(geom.getMesh());
		geomShadered.setMaterial(mat);
		
		geomShadered.setLocalTransform(geom.getLocalTransform());
		
		geom.getParent().attachChild(geomShadered);
		
		return mat;
	}
	
}
