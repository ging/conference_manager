#!/bin/bash

ping $1 -c 4 -n > /dev/null 2> /dev/null
if [ $? -eq 0 ]
then
	echo 200
else
	echo 500
fi