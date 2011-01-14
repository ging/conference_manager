#!/bin/bash

HOST=$1
USER=isabel
VAR_TMP=`ssh -o "StrictHostKeyChecking no" $USER@$HOST "ps -aef | grep -v grep | grep -e java.*red5 | wc -l"`
if [ $? -eq 0 ]
then
	if [ $VAR_TMP -eq 1 ]
	then 
		# Todos los servicios se estan ejecutando
		echo 200
	else 
		# El Red5 no se esta ejecutando
		echo 500
	fi
else
	# No se ha podido conectar
	echo 400
fi
