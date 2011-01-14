#!/bin/bash

HOST=$1
USER=isabel
MAX_FIN_WAIT=20
VAR_TMP=`ssh -o "StrictHostKeyChecking no" $USER@$HOST " ps -aef | grep -v grep | grep Venus_REST_Gateway.jar | wc -l"`
if [ $? -eq 0 ]
then
	if [ $VAR_TMP -eq 1 ]
	then 
		# Se ha encontrado el servicio.
		VAR_TMP=`ssh -o "StrictHostKeyChecking no" $USER@$HOST "ps -aef | grep -v grep | grep fms | wc -l"`
		if [ $VAR_TMP -gt 0 ]
		then
			VAR_TMP=`ssh -o "StrictHostKeyChecking no" $USER@$HOST "netstat -unta | grep FIN_WAIT | wc -l"`
			if [ $VAR_TMP -lt $MAX_FIN_WAIT ]
			then
				# Todos los servicios se estan ejecutando
				VAR_TMP=`ssh -o "StrictHostKeyChecking no" $USER@$HOST "ps -aef | grep -v grep | grep MP4Processor | wc -l"`
				if [ $VAR_TMP -gt 0 ]
				then
					echo 200
				else
					echo 503
				fi
			else
				# El FMS se ha quedado atascado (parece que es por los tuneles).
				echo 502
			fi
		else
			# El FMS no se esta ejecutando.
			echo 501
		fi
	else 
		# El VenusREST no se esta ejecutando
		echo 500
	fi
else
	# No se ha podido conectar
	echo 400
fi
