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

package gui.console;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class BoolToggler{
	protected static String	strCodingStyleFieldNamePrefix="";
	protected static String	strCommandTogglerPrefix="";
	protected static String	strCommandTogglerSuffix="";
	protected static ArrayList<BoolToggler> abtList = new ArrayList<BoolToggler>();
	
	protected boolean bPrevious;
	protected boolean bCurrent;
	protected String strCommand;
	protected Object	objOwner;
	
	public static void setCfg(String strCodingStyleFieldNamePrefix, String strCommandTogglerPrefix, String strCommandTogglerSuffix){
		BoolToggler.strCodingStyleFieldNamePrefix=strCodingStyleFieldNamePrefix;
		BoolToggler.strCommandTogglerPrefix=strCommandTogglerPrefix;
		BoolToggler.strCommandTogglerSuffix=strCommandTogglerSuffix;
	}
	public static ArrayList<BoolToggler> getBoolTogglerListCopy(){
		return new ArrayList<BoolToggler>(abtList);
	}
	
	private BoolToggler(){
		abtList.add(this);
	}
	public BoolToggler(Object objOwner, boolean bInitValue){
		this();
		this.objOwner=objOwner;
		set(bInitValue);
	}
	public BoolToggler(boolean bInitValue, String strCustomCmdId){
		this();
		set(bInitValue);
		setCustomCmdId(strCustomCmdId);
	}
	
	/**
	 * sets the command identifier that user will type in the console
	 * @param strCmd
	 */
	protected void setCustomCmdId(String strCmd) {
		this.strCommand=strCmd;
	}
	public String getCmdId(){
		if(strCommand!=null)return strCommand;
		return createCommandIdentifier();
	}
	/**
	 * based on reflection variable name
	 * @return
	 */
	protected String createCommandIdentifier(){
		for(Field fld:objOwner.getClass().getDeclaredFields()){
			try {
				boolean bWasAccessible = fld.isAccessible();
				if(!bWasAccessible)fld.setAccessible(true);
				if(fld.get(objOwner)==this){
					strCommand=fld.getName();
					if(strCommand.startsWith(strCodingStyleFieldNamePrefix)){
						//remove prefix
						strCommand=strCommand.substring(strCodingStyleFieldNamePrefix.length());
						//lower case 1st char
						strCommand=""
							+(""+strCommand.charAt(0)).toLowerCase() 
							+strCommand.substring(1);
					}
					strCommand=strCommandTogglerPrefix+strCommand+strCommandTogglerSuffix;
					return strCommand;
				}
				fld.setAccessible(bWasAccessible);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		throw new NullPointerException("failed to automatically set command id for: "+this);
	}
	
	public boolean get(){return bCurrent;}
	public void set(boolean b){this.bCurrent=b;}
	
	/**
	 * @return true if value changed
	 */
	public boolean checkChangedAndUpdate(){
		if(bCurrent != bPrevious){
			bPrevious=bCurrent;
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ""+bCurrent;
	}
}
