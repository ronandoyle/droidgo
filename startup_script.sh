#!/bin/bash
if [ "$(pidof lighttpd)" ]
then
	kill $(pgrep lighttpd)
	echo "Lighttpd process killed"
else
	echo "Lighttpd process not running"
fi

cd ../../../etc/lighttpd
echo "Changed to Lighttpd directory"
lighttpd -D -f lighttpd.conf &
echo " "
echo "Lighttpd web server started"
echo " "
echo "**************"
ifconfig | grep 'inet addr:' | grep -v '127.0.0.1'| cut -d: -f2 | awk '{print $1}'
echo "**************"
echo " "
echo "Opening camera stream"
cd /home/wit/mjpg-streamer
./mjpg_streamer -i "./input_uvc.so -d /dev/video0 -f 25 -r 640x480" -o "./output_http.so -w ./www" &
echo " "
cd /home/wit/Desktop
java Server &
echo " "
cd /home/wit/nxt-python-2.2.2/my_programs/
echo " "
echo "Changed to drive command directory"
echo " " 
python drive.py
