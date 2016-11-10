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
package com.github.commandsconsolegui.misc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageSingleInstanceI implements IManager<ISingleInstance>{
	private static ManageSingleInstanceI instance = new ManageSingleInstanceI();
	public static ManageSingleInstanceI i(){return instance;}
	
	HashMap<String,InstanceInfo> hm = new HashMap<String,InstanceInfo>();
	ArrayList<ISingleInstance> aList = new ArrayList<ISingleInstance>();
	
	public static class InstanceInfo{
		private StackTraceElement[] asteInstancedAt;
//		private String strConcreteClassName;
		private ISingleInstance siObject;
		public String getConcreteClassName(){
			return siObject.getClass().getName();
		}
	}
	
	/**
	 * Put on single instance constructors.
	 * If some sub-class can have multiple instances, make it implement {@link IMultiInstanceOverride}
	 * 
	 * @param objNew
	 * @return
	 */
	@Override
	public boolean add(ISingleInstance objNew) {
		if(objNew instanceof IMultiInstanceOverride)return false; //a simple skipper is enough
		
		InstanceInfo ii = new InstanceInfo();
//		ii.strConcreteClassName=objNew.getClass().getName();
		ii.siObject=objNew;
		ii.asteInstancedAt=Thread.currentThread().getStackTrace();
		
//		PrerequisitesNotMetException.assertNotAlreadyAdded(asiList, objNew);
		InstanceInfo iiExisting = hm.get(ii.getConcreteClassName());
		if(iiExisting!=null){
			boolean bDiscarded=false;
			if(iiExisting.siObject instanceof IDiscardableInstance){
				IDiscardableInstance di = ((IDiscardableInstance)iiExisting.siObject);
				if(di.isBeingDiscarded()){
					hm.remove(ii.getConcreteClassName());
					bDiscarded=true;
				}
			}
			
			if(!bDiscarded){
				throw new PrerequisitesNotMetException(
					"class already instanced, should be using "+IMultiInstanceOverride.class+"?", 
					ii, iiExisting, iiExisting.getConcreteClassName(), iiExisting.asteInstancedAt);
	//			return false;
			}
		}
		
		hm.put(ii.getConcreteClassName(), ii);
		
		return aList.add(objNew);
	}
	
	@Override
	public ArrayList<ISingleInstance> getListCopy() {
		return new ArrayList<ISingleInstance>(aList);
	}
	
}
