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

package com.github.commandsconsolegui.jmegui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jme3tools.savegame.SaveGame;
import truetypefont.TrueTypeBitmapGlyph;
import truetypefont.TrueTypeFont;
import truetypefont.TrueTypeKey;
import truetypefont.TrueTypeLoader;

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.GlobalOperationalSystemI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.globals.jmegui.GlobalRootNodeI;
import com.github.commandsconsolegui.jmegui.console.ConsoleStateAbs.TrueTypeFontFromSystem;
import com.github.commandsconsolegui.misc.CallQueueI.CallableWeak;
import com.github.commandsconsolegui.misc.DebugI;
import com.github.commandsconsolegui.misc.DebugI.EDebugKey;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.MiscI.EStringMatchMode;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.export.Savable;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.JmeSystem;
import com.jme3.system.JmeSystem.StorageFolderType;
import com.jme3.texture.Texture2D;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class MiscJmeI implements IReflexFillCfg{
	private static MiscJmeI instance = new MiscJmeI();
	public static MiscJmeI i(){return instance;}

//	private SimpleApplication	sapp;
	private IHandleExceptions	ihe;
	private boolean	bConfigured;
	
	public void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		this.ihe=ihe;
//		this.sapp=GlobalSappRefI.i().get();
		
		cfgRenderingNode(GlobalRootNodeI.i(), true);
		cfgRenderingNode(GlobalGUINodeI.i(), true);
		
		bConfigured=true;
	}
	
//	public Node getParentestFrom(Spatial sptStart){
//		return getParentestFrom(sptStart, true);
//	}
	public Spatial getParentestFrom(Spatial sptStart){
		return getParentestFrom(sptStart,true);
	}
	/**
	 * 
	 * @param sptStart
	 * @return parentest spatial
	 */
	public Spatial getParentestFrom(Spatial sptStart, boolean bExcludeRenderingNodes){
		/**
		 * For best compatibility, do not use Node, return Spatial.
		 * This way, the starting one can be returned also!
		 */
		
		Spatial sptParent = sptStart.getParent();
		if(sptParent==null)return sptStart;
		
		if(bExcludeRenderingNodes){
			if(isRenderingNode(sptStart))return null; //wow ... :P
			if(isRenderingNode(sptParent))return sptStart;
		}
		
		Spatial sptParentest = sptParent;
		while(sptParent!=null){ //1st loop will not be null
			if(bExcludeRenderingNodes){
				if(isRenderingNode(sptParent))break; //will return the previously set one
			}
			
			sptParentest = sptParent;
			sptParent = sptParent.getParent();
		}
		
		return sptParentest;
	}
//	/**
//	 * 
//	 * @param sptStart
//	 * @return parentest spatial, or self if attached directly to a rendering node
//	 */
//	public Node getParentestFrom(Spatial sptStart, boolean bLeastTopRenderingNodes){
//		if(sptStart.getParent()==null)return null;
//		
//		Node nodeParent = sptStart.getParent();
//		if(bLeastTopRenderingNodes){
//			if(isRenderingNode(nodeParent))return sptStart;
////			for(Node nodeRendering:anodeRendering){
////				if(nodeParent==nodeRendering)break;
////			}
//		}
//		
//		Node nodeParentest = nodeParent;
//		while(nodeParent!=null){ //1st loop will not be null
//			if(bLeastTopRenderingNodes){
//				for(Node nodeRendering:anodeRendering){
//					if(nodeParent==nodeRendering)break;
//				}
//			}
////			if(GlobalGUINodeI.i().equals(sptParentest.getParent()))break;
////			if(GlobalRootNodeI.i().equals(sptParentest.getParent()))break;
//			nodeParentest = nodeParent;
//			nodeParent = nodeParent.getParent();
//		}
//		
//		return nodeParentest;
//	}
	
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

	public BitmapText retrieveBitmapTextFor(Node node){
		for(Spatial c : node.getChildren()){
			if(c instanceof BitmapText){
				return (BitmapText)c;
			}
		}
		return null;
	}
	
	BoolTogglerCmdField btgFixBitmapTextLimits = new BoolTogglerCmdField(this,false);
	
	public void fixBitmapTextLimitsFor(Node node,Vector3f v3fSizeLimits){
		if(!btgFixBitmapTextLimits.b())return;
		
		BitmapText bmt = MiscJmeI.i().retrieveBitmapTextFor(node);
		
		Vector3f v3fPos = node.getLocalTranslation();
		Rectangle rectfont = new Rectangle(v3fPos.x,v3fPos.y,v3fSizeLimits.x,v3fSizeLimits.y);
		
		bmt.setBox(rectfont);
	}
	
	public void updateColorFading(TimedDelayVarField td, ColorRGBA color, boolean bFadeInAndOut){
		float fMinAlpha=0.25f;
		float fDeltaWorkAlpha = 1.0f - fMinAlpha;
		
		td.setOscilateMode(bFadeInAndOut);
		
		color.a = fMinAlpha + td.getCurrentDelayCalcDynamic(fDeltaWorkAlpha);
		
//		if(color.a<0)color.a=0;
		if(color.a>1f){
			color.a=1f;
		}else
		if(color.a < fMinAlpha){
			color.a=fMinAlpha;
		}
	}
	
//	public void updateColorFading(TimedDelayVarField td, ColorRGBA color, boolean bFadeInAndOut){
//		float fMinAlpha=0.25f;
//		float fDeltaWorkAlpha = 1.0f - fMinAlpha;
//		
//		float fPerc = td.getCurrentDelayPercentualDynamic();
//		
//		if(bFadeInAndOut){
//			if(fPerc<0.5f){
//				fPerc*=2f;
//				color.a = fMinAlpha + (fDeltaWorkAlpha*fPerc);
//			}else{
//				fPerc = (1.0f - fPerc)*2f; // 0.5 to 1.0, will become 1.0 to 0.0
//				color.a = fMinAlpha + (fDeltaWorkAlpha*fPerc);
//			}
//		}else{
//			color.a = fMinAlpha + (fDeltaWorkAlpha*fPerc);
//		}
//		
//		if(color.a < fMinAlpha){
//			color.a=fMinAlpha;
//		}
//		
////		if(bFadeInAndOut){
////			color.a=fPerc;
////			if(fPerc>0.5f){
////				color.a=1f-fPerc; //to allow it fade in and out
////			}
////			color.a *= 2f;
//////				if(color.a<0.75f)color.a=0.75f;
////		}
//		
////		color.a = td.getCurrentDelayPercentualDynamic();
////		if(bFadeInAndOut){
////			if(color.a>0.5f)color.a=1f-color.a; //to allow it fade in and out
////			color.a *= 2f;
//////				if(color.a<0.75f)color.a=0.75f;
////		}
//		if(color.a<0){
//			color.a=0;
//		}
//		if(color.a>1){
//			color.a=1;
//		}
//	}
	
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
	
	Comparator<Spatial> cmpNodesLast = new Comparator<Spatial>() {
		@Override
		public int compare(Spatial o1, Spatial o2) {
			if(o1 instanceof Node && o2 instanceof Spatial)return 1;
			return 0;
		}
	};
	private boolean	bTTFloaderRegistered;
	private ArrayList<Node>	anodeRendering = new ArrayList<Node>();
	
//	public ArrayList<Spatial> getAllChildrenFrom(Node nodeParent, String strChildName) {
//		return getAllChildrenFrom(nodeParent, strChildName, false);
//	}
	
	public ArrayList<Spatial> getAllChildrenRecursiveFrom(Node nodeParent, String strMatchChildName, EStringMatchMode eMode, boolean bIgnoreCase) {
		ArrayList<Spatial> asptList = new ArrayList<Spatial>();
		
		// add direct children
		for(Spatial sptChild:nodeParent.getChildren()){
			if(sptChild.getName()==null)continue;
			
			Spatial sptMatch = null;
//			if(bFuzzyMatch){
				if(MiscI.i().containsFuzzyMatch(sptChild.getName(), strMatchChildName, eMode, bIgnoreCase)){
					sptMatch=sptChild;
				}
//			}else{
//				if(bIgnoreCase){
//					if(sptChild.getName().equalsIgnoreCase(strChildName)){
//						sptMatch=(sptChild);
//					}
//				}else{
//					if(sptChild.getName().equals(strChildName)){
//						sptMatch=(sptChild);
//					}
//				}
//			}
			
			if(sptMatch!=null)asptList.add(sptMatch);
		}
		
		// deep search (even if parent doesnt match name will look at its children)
		for(Spatial sptChild:nodeParent.getChildren()){
			if(sptChild instanceof Node){
				asptList.addAll(getAllChildrenRecursiveFrom((Node)sptChild, strMatchChildName, eMode, bIgnoreCase));
			}
		}
		
		return asptList;
	}

	public TimedDelayVarField retrieveUserDataTimedDelay(Spatial sptHolder, String strKey, final float fDelay){
		TimedDelayVarField td = retrieveUserData(TimedDelayVarField.class, sptHolder, strKey, null, new CallableWeak<TimedDelayVarField>() {
			@Override
			public TimedDelayVarField call() {
				return new TimedDelayVarField(fDelay,"").setActive(true);
			}
		});
		
		if(Float.compare(td.getDelayLimitSeconds(), fDelay)!=0){
			td.setObjectRawValue(fDelay);
		}
		
		return td;
	}
//	public TimedDelayVarField retrieveTimedDelayFrom(Spatial sptHolder, String strUserDataKey){
//		@SuppressWarnings("unchecked")
//		SavableHolder<TimedDelayVarField> sh = (SavableHolder<TimedDelayVarField>)sptHolder.getUserData(strUserDataKey);
//		
//		TimedDelayVarField td = null;
//		if(sh==null){
//			sh = new SavableHolder<TimedDelayVarField>(new TimedDelayVarField(2f,"").setActive(true));
//			sptHolder.setUserData(strUserDataKey, sh);
//		}
//		td = sh.getRef();
//		
//		return td;
//	}
	
	/**
	 * Uses {@link PseudoSavableHolder}
	 * 
	 * @param clReturn
	 * @param sptHolder
	 * @param strKey
	 * @param callCreateInstance
	 * @return
	 */
	public <R> R retrieveUserData(Class<R> clReturn, Spatial sptHolder, String strKey, R objExisting, CallableWeak<R> callCreateInstance){
		@SuppressWarnings("unchecked")
		PseudoSavableHolder<R> sh = (PseudoSavableHolder<R>)sptHolder.getUserData(strKey);
		
		R objUser = null;
		if(sh==null){
			if(objExisting==null)objExisting=callCreateInstance.call();
			sh = new PseudoSavableHolder<R>(objExisting);
//				throw new PrerequisitesNotMetException("object instance creation failed", clReturn, sptHolder, strKey);
			sptHolder.setUserData(strKey, sh);
		}
		objUser = sh.getRef();
		
		return (R)objUser;
	}
	
	public ColorRGBA negateColor(ColorRGBA clr){
		return new ColorRGBA(
				FastMath.abs(1f-clr.r),
				FastMath.abs(1f-clr.g),
				FastMath.abs(1f-clr.b),
				clr.a
			);
	}

	/**
	 * 
	 * @param spt
	 * @param obj each key will be one super class of it
	 */
	public void setUserDataPSH(Spatial spt, Object obj) {
		for(Class<?> cl:MiscI.i().getSuperClassesOf(obj)){
			setUserDataPSH(spt, cl.getName(), obj);
		}
	}
	public void setUserDataPSH(Spatial spt, String strKey, Object obj) {
		spt.setUserData(strKey, new PseudoSavableHolder(obj));
	}
	
	public <R> R getUserDataPSH(Spatial spt, Class<R> cl){
		return getUserDataPSH(spt, cl.getName());
	}
	public <R> R getUserDataPSH(Spatial spt, String strKey){
		PseudoSavableHolder<R> sh = (PseudoSavableHolder<R>)spt.getUserData(strKey);
		if(sh==null)return null;
		return sh.getRef();
	}

	/**
	 * TODO this is not working
	 * @param strFontID
	 * @param iFontSize
	 * @return
	 */
	public BitmapFont fontFromTTF(String strFontID, int iFontSize){
		if(true)return null; //TODO dummified
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font fntFound=null;
		for(Font fnt:ge.getAllFonts()){
			if(fnt.getFontName().toLowerCase().equalsIgnoreCase(strFontID)){
				fntFound=fnt;
				break;
			}
		}
		
		if(fntFound==null)return null;
		
		GlobalCommandsDelegatorI.i().dumpInfoEntry("System font: "+strFontID);
		
		//TODO this is probably wrong...
		TrueTypeKey ttk = new TrueTypeKey(strFontID,0,iFontSize,1);
		fntFound = fntFound.deriveFont(ttk.getStyle(), ttk.getPointSize());
		
		/**
		 * TODO how to directly get a system Font and create a TrueTypeFont without loading it with the file? 
		 */
		return convertTTFtoBitmapFont(
			new TrueTypeFontFromSystem(
				GlobalAppRefI.i().getAssetManager(), 
				fntFound,
				ttk.getPointSize(),
				ttk.getOutline()
			));
	}
	
	public BitmapFont fontFromTTFFile(String strFilePath, int iFontSize){
		File fontFile = new File(strFilePath);
		
		if(fontFile.getParent()==null)return null; //not a file with path
		
		GlobalAppRefI.i().getAssetManager().registerLocator(fontFile.getParent(), FileLocator.class);
		
		TrueTypeKey ttk = new TrueTypeKey(strFilePath, java.awt.Font.PLAIN, iFontSize);
		
		TrueTypeFont ttf=null;
		
		if(bTTFloaderRegistered){
			//TODO check if asset loader already registered, how???
			GlobalAppRefI.i().getAssetManager().registerLoader(TrueTypeLoader.class, "ttf");
			bTTFloaderRegistered=true;
		}
		
		try{
			ttf = (TrueTypeFont)GlobalAppRefI.i().getAssetManager().loadAsset(ttk);
		}catch(AssetNotFoundException|IllegalArgumentException ex){
			// missing file
			GlobalCommandsDelegatorI.i().dumpExceptionEntry(ex);
		}
		
		GlobalAppRefI.i().getAssetManager().unregisterLocator(fontFile.getParent(), FileLocator.class);
		
		if(ttf==null)return null;
		
		GlobalCommandsDelegatorI.i().dumpInfoEntry("Font from file: "+strFilePath);
		
		return convertTTFtoBitmapFont(ttf);
	}

	/**
	 * TODO WIP, not working yet... may be it is not possible to convert at all yet?
	 * @param ttf
	 * @return
	 */
	private BitmapFont convertTTFtoBitmapFont(TrueTypeFont ttf){
		String strGlyphs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789./*-+?\\;\"!@#$*()&^%";
		TrueTypeBitmapGlyph attbg[] = ttf.getBitmapGlyphs(strGlyphs);
		BitmapFont font = new BitmapFont();
		BitmapCharacterSet bcs = new BitmapCharacterSet();
		int iMaxHeight = -1;
		int iMaxWidth = -1;
		for(TrueTypeBitmapGlyph ttbg : attbg){
			BitmapCharacter bc = new BitmapCharacter();
			char ch = ttbg.getCharacter().charAt(0);
			bc.setChar(ttbg.getCharacter().charAt(0));
			
			bc.setWidth(ttbg.w);
			bc.setHeight(ttbg.h);
			
			bc.setX(ttbg.x);
			bc.setY(ttbg.y);
			
			bc.setXAdvance(ttbg.xAdvance);
			bc.setXOffset(ttbg.getHeightOffset());
			bc.setYOffset(ttbg.y);
			
			bcs.addCharacter(ch, bc);
			
			if(bc.getHeight()>iMaxHeight)iMaxHeight=bc.getHeight();
			if(bc.getWidth()>iMaxWidth)iMaxWidth=bc.getWidth();
		}
		font.setCharSet(bcs);
		
		Texture2D t2d = ttf.getAtlas();
//		Image imgAtlas = t2d.getImage();
//		Image imgTmp = imgAtlas.clone();
//		imgTmp.getData(0).rewind();
//		imgTmp.setData(0, imgTmp.getData(0).asReadOnlyBuffer());
//		MiscI.i().saveImageToFile(imgTmp,"temp"+ttf.getFont().getName().replace(" ",""));
		if(DebugI.i().isKeyEnabled(EDebugKey.DumpFontImgFile)){ //EDbgKey.values()
			//TODO why image file ends empty??
			MiscJmeI.i().saveImageToFile(t2d.getImage(),
				EDebugKey.DumpFontImgFile.toString()+ttf.getFont().getName().replace(" ",""));
		}
		
		bcs.setBase(iMaxHeight); //TODO what is this!?
//		bcs.setBase(ttf.getFont().getSize()); 
		bcs.setHeight(t2d.getImage().getHeight());
		bcs.setLineHeight(iMaxHeight);
		bcs.setWidth(t2d.getImage().getWidth());
		bcs.setRenderedSize(iMaxHeight);
//		bcs.setStyle(style);
		
		/**
		 * TODO why this fails? missing material's "colorMap" ...
		font.setPages(new Material[]{ttf.getBitmapGeom("A", ColorRGBA.White).getMaterial()});
		 */
		/*
//		font.setPages(new Material[]{fontConsoleDefault.getPage(0)});
//		Material mat = ttf.getBitmapGeom(strGlyphs, ColorRGBA.White).getMaterial();
//		Material mat = fontConsoleDefault.getPage(0).clone();
		Material mat = fontConsoleExtraDefault.getPage(0).clone();
		mat.setTexture("ColorMap", t2d); //TODO wow, weird results from this...
//		mat.setTexture("ColorMap", ttf.getAtlas());
//		mat.setParam("ColorMap", VarType.Texture2D, ttf.getAtlas());
		font.setPages(new Material[]{mat});
		*/
		
//		Material m = new Material();
//		m.setp
		
//		font.getCharSet().getCharacter(33);
//		fontConsoleDefault.getCharSet().getCharacter(35).getChar();
		
//		Material[] amat = new Material[fontConsoleDefault.getPageSize()];
		
//	ttf.getAtlas();
		
		/**
		 * 
		 * check for missing glyphs?
		private boolean hasContours(String character) {
	    GlyphVector gv = font.createGlyphVector(frc, character);
	    GeneralPath path = (GeneralPath)gv.getOutline();
	    PathIterator pi = path.getPathIterator(null);
	    if (pi.isDone())
	        return false;
	    
	    return true;
		}
		 */
		
		//app().getAssetManager().unregisterLocator(fontFile.getParent(), FileLocator.class);
		return font;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
	
	/**
	 * TODO check for all enabled viewport's scene node
	 * @param pnl
	 * @return
	 */
	public boolean isGoingToBeRenderedNow(Spatial spt) {
		Spatial sptParentest = getParentestFrom(spt,false);
		
		return isRenderingNode(sptParentest);
//		for(Node node:anodeRendering){
//			if(p==node)return true;
//		}
//		
//		return false;
	}
	
	/**
	 * 
	 * @param spt
	 * @return
	 */
	public boolean isRenderingNode(Spatial spt){
		/**
		 * keep the checking as Spatial for best compatibility with other methods. 
		 */
		for(Node node:anodeRendering){
			if(spt==node)return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param node
	 * @param bAdd will remove the node if false
	 */
	public void cfgRenderingNode(Node node, boolean bAdd){
		if(bAdd){
			anodeRendering.add(node);
		}else{
			anodeRendering.remove(node);
		}
	}

	/**
	 * 
	 * @param strFileName
	 * @param svMain must gather all other required Savables
	 */
	private Savable consoleDataStoring(String strFileName, Savable svMain, boolean bSave) {
		StorageFolderType esft = GlobalOperationalSystemI.i().getStorageFolderType();
		
		String strPathFull = GlobalCommandsDelegatorI.i().getConsoleSaveDataPath();
		String strPathRelative = null;
		String strSFT = JmeSystem.getStorageFolder(esft).getAbsolutePath();
		if(strPathFull.startsWith(strSFT)){
			strPathRelative = strPathFull.substring(strSFT.length());
		}else{
			throw new PrerequisitesNotMetException("console save data path value not expected", strPathFull, strSFT);
		}
		
		/**
		 * it does not check if the path is absolute and uses it as relative...
		 */
		String str=null;
		Savable svLoaded = null;
		if(bSave){
			str="saving";
			SaveGame.saveGame(
				strPathRelative,
				strFileName,
				svMain,
				esft);
		}else{
			str="loading";
			svLoaded = SaveGame.loadGame(
				strPathRelative,
				strFileName,
				null,//GlobalAppRefI.i().getAssetManager(),
				esft);
			
			if(svLoaded==null){
				GlobalCommandsDelegatorI.i().dumpWarnEntry("failed to load", strPathFull, strFileName);
			}
		}
		
		GlobalCommandsDelegatorI.i().dumpInfoEntry(str+" "+strFileName+" at "+strPathFull);
		
		return svLoaded;
	}

	public void saveWriteConsoleData(String strFileName, Savable svMain) {
		consoleDataStoring(strFileName, svMain, true);
	}

	public Savable loadReadConsoleData(String strFileName) {
		return consoleDataStoring(strFileName, null, false);
	}
	
}
