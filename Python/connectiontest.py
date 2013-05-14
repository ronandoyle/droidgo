#!/usr/bin/env python

import time
import socket
import sys

soc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
count = 0
x = 0
print "Socket created"
host = '127.0.0.1'
port = int(5660)
soc.bind((host,port))
print "Socket ", soc, " bound to host ", host, " on port ", port
count = 0
while (count == 0):
        print "Preparing to listen..."
	soc.listen(1)
	print " Listening..."

	conn,addr = soc.accept()
	print "Connection accepted to address ", addr
	print (conn,addr)

	data = conn.recv(100000)
	data = data.decode("utf-8")
	print "COMMAND: ", data

