#!/bin/sh
var=`ps -ef |grep 'gcnetl' | grep -v grep | awk '{print $2}'`
echo "$var"
kill -9 $var
