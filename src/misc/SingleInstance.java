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

package misc;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import misc.ReflexFill.IReflexFillCfg;
import misc.ReflexFill.IReflexFillCfgVariant;
import misc.ReflexFill.ReflexFillCfg;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;

import console.ConsoleCommands;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class SingleInstance implements IReflexFillCfg, AppState{
	public final BoolToggler	btgAllowSingleInstace = new BoolToggler(this,true,ConsoleCommands.strTogglerCodePrefix);
	private boolean bDevModeExitIfThereIsANewerInstance = true; //true if in debug mode
//	public final BoolToggler	btgSingleInstaceOverrideOlder = new BoolToggler(this,false,ConsoleCommands.strTogglerCodePrefix,
//		"If true, any older instance will exit and this will keep running."
//		+"If false, the oldest instance will keep running and this one will exit.");
	String strPrefix=SingleInstance.class.getSimpleName()+"-";
	String strSuffix=".lock";
	String strId;
	private File	flSelfLock;
	private SimpleApplication	sapp;
	private ConsoleCommands	cc;
	private BasicFileAttributes	attrSelfLock;
	private boolean	bInitialized;
	private boolean	bEnabled = true;
	private FilenameFilter	fnf;
	private File	flFolder;
	
	private static SingleInstance instance = new SingleInstance();
	public static SingleInstance i(){return instance;}
	
	private File[] getAllLocks(){
		return flFolder.listFiles(fnf);
	}
	
	/**
	 * This will happen if newer instances are to exit promptly
	 * allowing only the older instance to remain running.
	 * 
	 * So, broken locks will be cleaned.
	 */
	private void clearBrokenLocks(){
		if(bDevModeExitIfThereIsANewerInstance)return;
			
		for(File fl:getAllLocks()){
			if(cmpSelfWith(fl))continue;
			System.err.println("Cleaning lock: "+fl.getName());
			fl.delete();
		}
	}
	
	private boolean cmpSelfWith(File fl){
//		System.out.println(">>>"+flSelfLock.getName()()+">>>"+(fl.getAbsolutePath()));
		return flSelfLock.getName().equalsIgnoreCase(fl.getName());
	}
	
	private boolean checkExit(){
		if(!btgAllowSingleInstace.b())return false;
		
		boolean bExit=false;
		for(File fl:getAllLocks()){
			if(cmpSelfWith(fl))continue;
			
			try {
//				String strOther="";
				BasicFileAttributes attr = Files.readAttributes(fl.toPath(), BasicFileAttributes.class);
				
				boolean  bOtherIsNewer = attrSelfLock.creationTime().compareTo(attr.creationTime())<0;
				
				if(bDevModeExitIfThereIsANewerInstance && bOtherIsNewer){
//					strOther="NEWER";
					bExit=true;
				}else
				if(!bDevModeExitIfThereIsANewerInstance && !bOtherIsNewer){
					/**
					 * exit if there is an older instance
					 */
//					strOther="OLDER";
					bExit=true;
				}
				
//				if(btgSingleInstaceOverrideOlder.b() && bOtherIsNewer)continue;
//				
//				if(btgSingleInstaceOverrideOlder.b()){
//					if(attrSelfLock.creationTime().compareTo(attr.creationTime())<0){
//						strOther="NEWER";
//						bExit=true;
//					}
//				}else{
//					if(attrSelfLock.creationTime().compareTo(attr.creationTime())>0){
//						strOther="OLDER";
//						bExit=true;
//					}
//				}
				
				if(bExit){
					System.err.println(other()+" file lock: "+fl.getName());
					break;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return bExit;
	}
	
	class ThreadChecker implements Runnable{
		@Override
		public void run() {
			boolean bCleanOnce=true;
			while(true){
				try {
					Thread.sleep(3000);
					
					if(checkExit()){
//						System.err.println(btgSingleInstaceExitSelfIfOlder.getReport());
//						String strOther="OLDER";
//						if(bDevModeExitIfThereIsANewerInstance){
//							strOther="NEWER";
//						}
						System.err.println(other()+" is running, exiting...");
						flSelfLock.delete();
						System.exit(0);
					}else
					if(bCleanOnce){
						clearBrokenLocks();
						bCleanOnce=false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					flSelfLock.delete();
				}
			}
		}
	}
	
	private String other(){
		String strOther="Another ";
		if(bDevModeExitIfThereIsANewerInstance){
			strOther+="NEWER";
		}else{
			strOther+="OLDER";
		}
		if(Debug.i().isInIDEdebugMode())strOther+="(DebugMode)";
		return strOther+" instance";
	}
	
	public void initialize(SimpleApplication sapp, ConsoleCommands cc){
		this.sapp=sapp;
		this.cc=cc;
		
		if(Debug.i().isInIDEdebugMode())strPrefix="DebugMode-"+strPrefix;
		
		strId=strPrefix+Misc.i().getDateTimeForFilename()+strSuffix;
		
		flSelfLock = new File(strId);
		
		try {
			flSelfLock.createNewFile();
			System.err.println("Created lock: "+flSelfLock.getName());
			attrSelfLock = Files.readAttributes(flSelfLock.toPath(), BasicFileAttributes.class);
			flSelfLock.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
			throw new NullPointerException("unable to create lock file "+flSelfLock.getAbsolutePath());
		}
		
		flFolder = new File("./");
		fnf = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith(strPrefix) && name.endsWith(strSuffix))return true;
				return false;
			}
		};
		
		bDevModeExitIfThereIsANewerInstance = Debug.i().isInIDEdebugMode();
		
		new Thread(new ThreadChecker()).start();
		
		sapp.getStateManager().attach(this);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cc.getReflexFillCfg(rfcv);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		bInitialized=true;
	}

	@Override
	public boolean isInitialized() {
		return bInitialized;
	}

	@Override
	public void setEnabled(boolean active) {
		this.bEnabled=active;
	}

	@Override
	public boolean isEnabled() {
		return bEnabled;
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(float tpf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(RenderManager rm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRender() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() {
		flSelfLock.delete();
	}
	
}
