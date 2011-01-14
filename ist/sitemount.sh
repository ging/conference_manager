#!/bin/sh
IGW_BASE_URL=$2
IGW_OUTPUT_DIR=$3
IGW_TMP_FILE=/tmp/igwdavfs.tmp
MOUNT_OPTIONS=user,rw,noauto,dir_mode=777,file_mode=777

case "$1" in
mount)
	mount.davfs $IGW_BASE_URL $IGW_OUTPUT_DIR -o $MOUNT_OPTIONS
	echo $IGW_OUTPUT_DIR > $IGW_TMP_FILE
	;;

umount)
	IGW_OUTPUT_DIR=$(cat $IGW_TMP_FILE)
	umount $IGW_OUTPUT_DIR > /dev/null 2>&1
	rm -f $IGW_TMP_FILE
	;;

*)
	echo "Usage:"
	echo "	$0 mount <IGW_BASE_URL> <IGW_OUTPUT_DIR>"
	echo "	$0 umount"
	;;
esac

exit 0