#!/bin/bash

eval `secinit`

strRunClass="`egrep "public static void main[(]" ../src/* -rIc |grep -v :0 |sed -r 's".*/src/(.*)[.]java:1$"\1"' |tr "/" "."`"&&:
echoc --info "strRunClass='$strRunClass'"

nPid="`pgrep -f "java .* $strRunClass"`"&&:
ps -o pid,cmd -p $nPid

SECFUNCexecA -ce kill -SIGKILL $nPid


