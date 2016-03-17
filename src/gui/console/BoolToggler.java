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

import gui.console.ReflexFill.IReflexFillCfg;
import gui.console.ReflexFill.IReflexFillCfgVariant;

import java.util.ArrayList;

/**
 * This class can provide automatic boolean console options toggle.
 * You just need to create the variable properly and it will be automatically recognized.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class BoolToggler implements IReflexFillCfgVariant{
	protected static ArrayList<BoolToggler> abtgList = new ArrayList<BoolToggler>();
	
	protected boolean bPrevious;
	protected boolean bCurrent;
	protected String strCommand;
	protected IReflexFillCfg	rfcfgOwner;

	private int	iReflexFillCfgVariant;
	
	public static ArrayList<BoolToggler> getBoolTogglerListCopy(){
		return new ArrayList<BoolToggler>(abtgList);
	}
	
	private BoolToggler(){
		abtgList.add(this);
	}
	public BoolToggler(IReflexFillCfg rfcfgOwner, boolean bInitValue){
		this(rfcfgOwner, bInitValue, 0);
	}
	public BoolToggler(IReflexFillCfg rfcfgOwner, boolean bInitValue, int iReflexFillCfgVariant){
		this();
		this.iReflexFillCfgVariant=iReflexFillCfgVariant;
		this.rfcfgOwner=rfcfgOwner;
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
		strCommand=ReflexFill.i().createIdentifierWithFieldName(rfcfgOwner,this);
		return strCommand;
	}
	
	public boolean get(){return bCurrent;}
	public boolean getBoolean(){return bCurrent;}
	public boolean getBool(){return bCurrent;}
	public boolean is(){return bCurrent;}
	public boolean b(){return bCurrent;}
	
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

	@Override
	public int getReflexFillCfgVariant() {
		return iReflexFillCfgVariant;
	}
}
