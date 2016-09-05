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

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class SimulationTime {
	private static SimulationTime instance = new SimulationTime();
	public static SimulationTime i(){return instance;}
	
	/**
	 * This is a developer coding restriction helper.
	 * 
	 * Instantiate with private/protected constructor on a private/protected field to 
	 * forbid other classes from updating the time.
	 */
	public static interface ISimulationTimeKey{}
	
	private ISimulationTimeKey	istk;
	
	long lUnsignedTime;
	
	double dTimeResolution;
	
	public SimulationTime() {
		lUnsignedTime = Long.parseUnsignedLong("0");
		dTimeResolution = 1000.0;
	}
	
	/**
	 * @param dTimeResolution cannot be changed after set, used for adding floating seconds properly
	 */
	public SimulationTime(double dTimeResolution){
		this();
		
		this.dTimeResolution=dTimeResolution;
		
		if(Double.compare(this.dTimeResolution,1000.0)==0){ //milis
		}else
		if(Double.compare(this.dTimeResolution,1000000.0)==0){ //micro
		}else
		if(Double.compare(this.dTimeResolution,1000000000.0)==0){ //nano
		}else{
			System.err.println("Warning: "+SimulationTime.class.getName()
				+"Time resolution should be milis, micro or nano. ");
			Thread.dumpStack();
		}
	}
	
	public long get(){
		return lUnsignedTime;
	}
	
//	public Double getSeconds(){
//		return lUnsignedTime / dTimeResolution;
//	}
	
	public SimulationTime add(double dSeconds, ISimulationTimeKey istk){
		return add((long)(dSeconds*dTimeResolution), istk);
	}
	
	public SimulationTime add(float fSeconds, ISimulationTimeKey istk){
		return add((long)(fSeconds*dTimeResolution), istk);
	}
	
	/**
	 * forever deprecated :)
	 * @return
	 */
	@Deprecated
	public double getSeconds(){
		throw new NullPointerException(
			"Do not use full time references in float/double variables!"+
			"Imprecision and lower limit than Long may torment you!");
	}
	
	/**
	 * When loading a simulation, just add the full time as a start value.
	 * 
	 * @param lTime normal long (not unsigned)
	 * @param istk the first time it is not null will ensure the restrictive access in a normal code
	 * @return
	 */
	public SimulationTime add(long lTime, ISimulationTimeKey istk){
		if(this.istk==null){
			if(istk!=null)this.istk=istk;
		}else{
			/**
			 * key access restriction
			 */
			if(!this.istk.equals(istk)){
				throw new NullPointerException("invalid key "+istk+", expected "+this.istk);
			}
		}
		
		this.lUnsignedTime+=lTime;
		
		return this;
	}
	
}
