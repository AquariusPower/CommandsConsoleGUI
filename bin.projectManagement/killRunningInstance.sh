#!/bin/bash

eval `secinit`

strRunClass="`egrep "public static void main[(]" ./src/* -rIc |grep -v :0 |sed -r 's".*/src/(.*)[.]java:1$"\1"' |tr "/" "."`"&&:
echoc --info "strRunClass='$strRunClass'"

if [[ -z "$strRunClass" ]];then echoc -p "strRunClass='$strRunClass'";exit 1;fi

nPid="`pgrep -f "java .* $strRunClass"`"&&:
SECFUNCexecA -ce ps -o pid,cmd -p $nPid

if echoc -q "$nPid";then
	SECFUNCexecA -ce kill -SIGKILL $nPid
fi

