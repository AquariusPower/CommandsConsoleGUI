#!/bin/bash

#	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
#	
#	All rights reserved.
#
#	Redistribution and use in source and binary forms, with or without modification, are permitted 
#	provided that the following conditions are met:
#
#	1.	Redistributions of source code must retain the above copyright notice, this list of conditions 
#		and the following disclaimer.
#
#	2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
#		and the following disclaimer in the documentation and/or other materials provided with the distribution.
#	
#	3.	Neither the name of the copyright holder nor the names of its contributors may be used to endorse 
#		or promote products derived from this software without specific prior written permission.
#
#	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
#	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
#	PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
#	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
#	LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
#	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
#	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN 
#	IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

eval `secinit`

SECFUNCuniqueLock --waitbecomedaemon

function FUNCfind(){
	local strVarId=$1
	local strRegexp="$2"
	
	declare -ga $strVarId
	
	local strMatches="`grep -P "$strRegexp" ./src/* --include="*.java" -rn |grep -v commandsconsoleguitests`"
#	echo "$strMatches"
	IFS=$'\n' read -d '' -r -a $strVarId < <(echo "$strMatches" |cut -d: -f1 |sort)&&:
	
	return 0 #to ignore last command pseudo error
}

while true;do
	bInconsistencyFound=false
	
	astrSubPrjOrder=(spAppOs spCmd spJme spLemur spExtras)
	IFS=$'\n' read -d '' -r -a astrFileList < <(find -iname "*.java")&&:
	for((i=0;i<${#astrSubPrjOrder[@]}-1;i++));do #last one is always skipped as it can use any dependencies
		strPkgSP="${astrSubPrjOrder[i]}"
		SECFUNCdrawLine " sub-project $strPkgSP "
		for strFile in "${astrFileList[@]}";do
#			echo "$strPkgSP $strFile"
			if egrep -q "package .*$strPkgSP" "$strFile";then
				for((i2=i+1;i2<${#astrSubPrjOrder[@]};i2++));do
					strImpSP="${astrSubPrjOrder[i2]}"
#					echo "$strPkgSP $strImpSP $strFile"
					if egrep "import .*$strImpSP" "$strFile";then
						echoc --info " ABOVE!!! inconsistent: $strFile"
						bInconsistencyFound=true;
					fi
				done
			fi
		done
	done
	
	
#	SECFUNCdrawLine "Commands sub-project"
#	# files from Commands only package, no JME neither lemur (all lemur are also jme tho)
#	# Must NOT contain imports from JME/Lemur
#	FUNCfind astrCmdFileList "^package (?!.*[.]jme)(?!.*[.]lemur)"
#	#SECFUNCexecA -ce declare -p astrCmdFileList |tr '[' '\n';exit
#	for strFile in "${astrCmdFileList[@]}";do
#		if strInconsistency="`egrep "^import ((.*[.]jme)|(.*[.]lemur))" "$strFile"`";then
#			echoc --info ">>>>CMD.inconsistent>>>>> $strFile"
#			echo "$strInconsistency"
#			bInconsistencyFound=true;
#		fi
#	done

#	SECFUNCdrawLine "sub-project for JME" # Must NOT contain imports from Lemur
#	FUNCfind astrJmeFileList "^package .*[.]jme(?!.*[.]lemur)"
#	#SECFUNCexecA -ce declare -p astrJmeFileList |tr '[' '\n';exit
#	for strFile in "${astrJmeFileList[@]}";do
#		if strInconsistency="`egrep "^import .*[.]spLemur" "$strFile"`";then
#			echoc --info ">>>>JME.inconsistent>>>>> $strFile"
#			echo "$strInconsistency"
#			bInconsistencyFound=true;
#		fi
#	done
	
	# Console+JME+Lemur project can contain any imports
	
	if $bInconsistencyFound;then
		echoc -p --say "inconsistency found at sub-projects dependencies"
	fi
	
	echoc -w -t 1800
done

