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

package jmestates;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import misc.BoolTogglerCmd;
import misc.Debug;
import misc.Misc;
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
 * Locks have a short timeout.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class SingleInstanceState implements IReflexFillCfg, AppState{
	public final BoolTogglerCmd	btgSingleInstaceMode = new BoolTogglerCmd(this,true,BoolTogglerCmd.strTogglerCodePrefix,
		"better keep this enabled, other instances may conflict during files access.");
	private boolean bDevModeExitIfThereIsANewerInstance = true; //true if in debug mode
//	public final BoolToggler	btgSingleInstaceOverrideOlder = new BoolToggler(this,false,ConsoleCommands.strTogglerCodePrefix,
//		"If true, any older instance will exit and this will keep running."
//		+"If false, the oldest instance will keep running and this one will exit.");
	String strPrefix=SingleInstanceState.class.getSimpleName()+"-";
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
	private long lLockUpdateDelayMilis=3000;
	private long	lSelfLockCreationTime;
	private String	strExitReasonOtherInstance = "";
	
	private static SingleInstanceState instance = new SingleInstanceState();
	public static SingleInstanceState i(){return instance;}
	
	private File[] getAllLocksTD(){
		return flFolder.listFiles(fnf);
	}
	
//	/**
//	 * This will happen if newer instances are to exit promptly
//	 * allowing only the older instance to remain running.
//	 * 
//	 * So, broken locks will be cleaned.
//	 */
//	private void clearBrokenLocks(){
//		if(bDevModeExitIfThereIsANewerInstance)return;
//			
//		for(File fl:getAllLocks()){
//			if(cmpSelfWith(fl))continue;
//			System.err.println("Cleaning lock: "+fl.getName());
//			fl.delete();
//		}
//	}
	
	/**
	 * Clear locks that have not been updated lately.
	 */
	private void clearOldLocksTD(){
		for(File fl:getAllLocksTD()){
			if(cmpSelfWithTD(fl))continue;
			
			BasicFileAttributes attr = Misc.i().fileAttributesTS(fl);
			if(attr==null)continue;
			
			long lTimeLimit = System.currentTimeMillis() - (lLockUpdateDelayMilis*2);
			if(attr.lastModifiedTime().toMillis() < lTimeLimit){
				System.err.println("Cleaning old lock: "+fl.getName());
				fl.delete();
			}
		}
	}
	
	private boolean cmpSelfWithTD(File fl){
//		System.out.println(">>>"+flSelfLock.getName()()+">>>"+(fl.getAbsolutePath()));
		return flSelfLock.getName().equalsIgnoreCase(fl.getName());
	}
	
	private boolean checkExitTD(){
		if(!btgSingleInstaceMode.b())return false;
		
		boolean bExit=false;
		String strReport="";
		strReport+="-----------------SimultaneousLocks--------------------\n";
		strReport+="ThisLock:  "+flSelfLock.getName()+"\n";
		int iSimultaneousLocksCount=0;
		for(File flOtherLock:getAllLocksTD()){
			if(cmpSelfWithTD(flOtherLock))continue;
			
			Long lOtherCreationTime = getCreationTimeOfTD(flOtherLock);
			if(lOtherCreationTime==null)continue;
			
			strReport+="OtherLock: "+flOtherLock.getName()+"\n";
			
			boolean  bOtherIsNewer = lSelfLockCreationTime < lOtherCreationTime;
			
			if(bDevModeExitIfThereIsANewerInstance && bOtherIsNewer){
				strExitReasonOtherInstance="NEWER";
				bExit=true;
			}else
			if(!bDevModeExitIfThereIsANewerInstance && !bOtherIsNewer){
				/**
				 * exit if there is an older instance
				 */
				strExitReasonOtherInstance="OLDER";
				bExit=true;
			}
			
			if(!bExit){
				/**
				 * If the other instance is in release mode,
				 * this debug mode instance will exit.
				 * 
				 * To test, start a release mode, and AFTER that, start a debug mode one.
				 */
				if(bDebugMode){
					String strMode = getLockModeOfTD(flOtherLock);
					if(!strMode.equalsIgnoreCase(strDebugMode)){
						strExitReasonOtherInstance=strMode;
						bExit=true;
					}
				}
			}
			
			iSimultaneousLocksCount++;
		}
		
		if(bExit){
			System.err.println(strReport);
		}else{
			if(iSimultaneousLocksCount>0){
				System.err.println(strReport+"This instance will continue running.");
				clearOldLocksTD();
			}
		}
		
		return bExit;
	}
	
	class ThreadChecker implements Runnable{
		@Override
		public void run() {
			while(threadMain.isAlive()){
				try {
					if(!flSelfLock.exists()){
						System.err.println("Lock was deleted, recreating: "+flSelfLock.getName());
						createSelfLockFileTD();
					}
					
					if(checkExitTD()){
						System.err.println("Other "+strExitReasonOtherInstance+" instance is running, exiting this...");
						flSelfLock.delete();
						System.exit(0);
					}
					
					/**
					 * This will also update the file creation time...
					 */
					flSelfLock.setLastModified(System.currentTimeMillis());
					
					/**
					 * sleep after to help on avoiding allocating resources.
					 */
					Thread.sleep(lLockUpdateDelayMilis);
				} catch (InterruptedException e) {
					e.printStackTrace();
					flSelfLock.delete();
				}
			}
			System.err.println("Main thread ended.");
		}

	}
	
	String strDebugMode="DebugMode";
	String strReleaseMode="ReleaseMode";
	private boolean	bConfigured;
	private String	strErrorMissingValue="ERROR_MISSING_VALUE";
	private Boolean	bDebugMode;
	private Thread	threadMain;
	private Thread	threadChecker;
	
	private Long getCreationTimeOfTD(File fl){
		ArrayList<String> astr = Misc.i().fileLoad(fl);
		Long l = null;
		if(astr.size()>0){
			// line 1
			try{l = Long.parseLong(astr.get(0));}catch(NumberFormatException ex){};
		}
		return l;
	}
	
	private String getLockModeOfTD(File fl){
		ArrayList<String> astr = Misc.i().fileLoad(fl);
		if(astr.size()>0)return astr.get(1); // line 2
		return strErrorMissingValue;
	}
	
	private void createSelfLockFileTD() {
		try{
			flSelfLock.createNewFile();
			
			ArrayList<String> astr = new ArrayList<String>();
			// line 1
			astr.add(""+lSelfLockCreationTime);
			// line 2
			if(bDebugMode){
				astr.add(strDebugMode);
			}else{
				astr.add(strReleaseMode);
			}
			
			Misc.i().fileAppendListTS(flSelfLock, astr);
			
			attrSelfLock = Misc.i().fileAttributesTS(flSelfLock);
			
			System.err.println("Created lock: "+flSelfLock.getName());
			
			flSelfLock.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
			throw new NullPointerException("unable to create lock file "+flSelfLock.getAbsolutePath());
		}
	}
	
//	private String otherPrefixInfo(){
//		String strOther="Other ";
//		if(bDevModeExitIfThereIsANewerInstance){
//			strOther+="NEWER";
//		}else{
//			strOther+="OLDER";
//		}
//		if(Debug.i().isInIDEdebugMode())strOther+="(DebugMode)";
//		return strOther+" instance";
//	}
	
	public void configure(SimpleApplication sapp, ConsoleCommands cc, Thread threadMain){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		
		this.sapp=sapp;
		this.cc=cc;
		this.threadMain=threadMain;
		
		bDebugMode = Debug.i().isInIDEdebugMode();
		
//		if(Debug.i().isInIDEdebugMode())strPrefix="DebugMode-"+strPrefix;
		
		strId=strPrefix+Misc.i().getDateTimeForFilename(true)+strSuffix;
		
		flSelfLock = new File(strId);
		
		lSelfLockCreationTime = System.currentTimeMillis();
		
		flFolder = new File("./");
		fnf = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith(strPrefix) && name.endsWith(strSuffix))return true;
				return false;
			}
		};
		
		bDevModeExitIfThereIsANewerInstance = bDebugMode;
		
		if(bDebugMode){
			System.err.println("This instance is in DEBUG mode.");
		}
		
		if(!sapp.getStateManager().attach(this))throw new NullPointerException("already attached state "+this.getClass().getName());
		
		/**
		 * creating the new thread here will make the application ends faster if it can.
		 */
		clearOldLocksTD();
		createSelfLockFileTD();
		threadChecker = new Thread(new ThreadChecker());
		threadChecker.start();
		
		bConfigured=true;
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
