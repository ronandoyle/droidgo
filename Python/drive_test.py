#!/usr/bin/env python

# This class can be used to verify that he commands are properly being send from the Android app the the Python script. Do not run this app while the "drive.py" script is running, as they currently use the same ports. If you wish to run along side th other script then you will need to change this port below, and change the port in the php script, or add in a second port in the php script.

import nxt.locator
import time
import socket
import sys
from nxt.sensor import *
print " "
print "Initializing Python Script"

soc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
count = 0

print "Socket created"

host = '127.0.0.1'
port = int(5660)
soc.bind((host,port))

print "Socket ", soc, " bound to host ", host, " on port ", port

while (count == 0):
	print "Preparing to listen..."
        soc.listen(1)
	print " Listening..."

        conn,addr = soc.accept()
        print "A connection ", conn, " was established to address ", addr
	print (conn,addr)

        data = conn.recv(100000)
        data = data.decode("utf-8")
	print " "
	print "Command: ", data
	print " "
soc.close

