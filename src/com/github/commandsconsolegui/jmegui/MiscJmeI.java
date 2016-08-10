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

package com.github.commandsconsolegui.jmegui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collections;

import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.globals.jmegui.GlobalRootNodeI;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.JmeSystem;
import com.simsilica.lemur.event.AbstractCursorEvent;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class MiscJmeI {
	private static MiscJmeI instance = new MiscJmeI();
	public static MiscJmeI i(){return instance;}

//	private SimpleApplication	sapp;
	private IHandleExceptions	ihe;
	private boolean	bConfigured;
	
	public void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		this.ihe=ihe;
//		this.sapp=GlobalSappRefI.i().get();
		bConfigured=true;
	}
	
	/**
	 * 
	 * @param sptStart
	 * @return parentest spatial, least top nodes like RootNode and GuiNode
	 */
	public Node getParentestFrom(Spatial sptStart){
		Spatial sptParentest = sptStart;
		while(sptParentest.getParent()!=null){
			if(GlobalGUINodeI.i().equals(sptParentest.getParent()))break;
			if(GlobalRootNodeI.i().equals(sptParentest.getParent()))break;
			sptParentest=sptParentest.getParent();
		}
		return (Node)sptParentest; //parent is always Node
	}
	
	/**
	 * first is the parentest
	 * 
	 * @param spt
	 * @param bIncludeSelf
	 * @return
	 */
	public ArrayList<Spatial> getParentListFrom(Spatial spt, boolean bIncludeSelf){
		ArrayList<Spatial> aspt = new ArrayList<Spatial>();
		Spatial sptParent = spt;
		if(bIncludeSelf)aspt.add(spt);
		while( (sptParent = sptParent.getParent()) != null ){
			aspt.add(sptParent);
		}
		Collections.reverse(aspt);
		return aspt;
	}
	
	public void saveImageToFile(com.jme3.texture.Image img, String strFileNameWithoutExt) {
		if(!img.getData(0).isReadOnly()){
			img = img.clone();
			img.setData(0, img.getData(0).asReadOnlyBuffer());
		}
		img.getData(0).rewind();
		
		File fl = new File(strFileNameWithoutExt+".png");
		OutputStream os=null;
		try {            
			os = new FileOutputStream(fl);
			JmeSystem.writeImageFile(
				os,
				"png", // to allow transparency
				img.getData(0),
				img.getWidth(),
				img.getHeight()); 
		} catch(IOException|BufferUnderflowException ex){
			ihe.handleExceptionThreaded(ex);
		} finally {
			if(os!=null)try {os.close();} catch (IOException ex) {ihe.handleExceptionThreaded(ex);}
		}             
	}

	public BitmapText retrieveBitmapTextFor(Node pnl){
		for(Spatial c : pnl.getChildren()){
			if(c instanceof BitmapText){
				return (BitmapText)c;
			}
		}
		return null;
	}
	
	public Vector3f eventToV3f(AbstractCursorEvent event){
		return new Vector3f(event.getX(),event.getY(),0);
	}

	public void updateColorFading(TimedDelayVarField td, ColorRGBA color, boolean bFadeInAndOut){
		color.a = td.getCurrentDelayPercentualDynamic();
		if(bFadeInAndOut){
			if(color.a>0.5f)color.a=1f-color.a; //to allow it fade in and out
			color.a *= 2f;
//				if(color.a<0.75f)color.a=0.75f;
		}
		if(color.a<0)color.a=0;
		if(color.a>1)color.a=1;
	}
	
//	
//	public boolean isMouseCursorButton(EMouseCursorButton emcb, int iIndex){
//		return emcb.getIndex()==iIndex;
//	}
	
	public static enum EUserData{
		matCursorBkp,
		;
	}
	
	public boolean updateBlink(TimedDelayVarField td, Spatial sptUserDataHolder, Geometry geom) {
		long lDelay = td.getCurrentDelayNano();
		
		Material matBkp = (Material)sptUserDataHolder.getUserData(EUserData.matCursorBkp.toString());
		
		boolean bVisible=false;
		
		if(matBkp!=null){
			geom.setMaterial(matBkp);
			sptUserDataHolder.setUserData(EUserData.matCursorBkp.toString(),null); //clear TODO why???
		}
		
		if(lDelay > td.getDelayLimitNano()){
			if(geom.getCullHint().compareTo(CullHint.Always)!=0){
				geom.setCullHint(CullHint.Always);
			}else{
//				bugFix(null,btgBugFixInvisibleCursor,tf);
//					bugFix(EBugFix.InvisibleCursor,tf);
//					fixInvisibleCursor(geomCursor);
				geom.setCullHint(CullHint.Inherit);
				bVisible=true;
			}
			
			td.updateTime();
		}
		
		return bVisible;
	}
}
