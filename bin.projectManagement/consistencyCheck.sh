#!/bin/bash

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
	
	SECFUNCdrawLine "Commands project"
	# files from Commands only package, no JME neither lemur (all lemur are also jme tho)
	# Must NOT contain imports from JME/Lemur
	FUNCfind astrCmdFileList "^package (?!.*[.]jme)(?!.*[.]lemur)"
	#SECFUNCexecA -ce declare -p astrCmdFileList |tr '[' '\n';exit
	for strFile in "${astrCmdFileList[@]}";do
		if strInconsistency="`egrep "^import ((.*[.]jme)|(.*[.]lemur))" "$strFile"`";then
			echoc --info ">>>>CMD.inconsistent>>>>> $strFile"
			echo "$strInconsistency"
			bInconsistencyFound=true;
		fi
	done

	SECFUNCdrawLine "Console+JME project"
	# files from JME only package, no lemur
	# Must NOT contain imports from Lemur
	FUNCfind astrJmeFileList "^package .*[.]jme(?!.*[.]lemur)"
	#SECFUNCexecA -ce declare -p astrJmeFileList |tr '[' '\n';exit
	for strFile in "${astrJmeFileList[@]}";do
		if strInconsistency="`egrep "^import .*[.]lemur" "$strFile"`";then
			echoc --info ">>>>JME.inconsistent>>>>> $strFile"
			echo "$strInconsistency"
			bInconsistencyFound=true;
		fi
	done
	
	# Console+JME+Lemur project can contain any imports
	
	if $bInconsistencyFound;then
		echoc -p --say "inconsistency found"
	fi
	
	echoc -w -t 1800
done
