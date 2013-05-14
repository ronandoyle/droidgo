#!/usr/bin/env python

import nxt.locator
import time
import socket
import sys
from nxt.sensor import *

print "Initializing Python Script"

print "Locating brick and servos..."

b = nxt.locator.find_one_brick()# Find brick and connect

s = MSServo(b, PORT_1)          # Which sensor, brick and port to talk to. If there is an error pointing to this line, then make sure that the cable connecting the NXT to the breakout board is connected to Port 1.
print "Brick and servos found!"

print "Creating switching class"
class switch(object):
    def __init__(self, value):
        self.value = value
        self.fall = False

    def __iter__(self):
        """Return the match method once, then stop"""
        yield self.match
        raise StopIteration

    def match(self, *args):
        """Indicate whether or not to enter a case suite"""
        if self.fall or not args:
            return True
        elif self.value in args:
            self.fall = True
            return True
        else:
            return False

print "Switching class created"

soc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
count = 0

print "Socket created"

host = '127.0.0.1'
port = int(5660)
soc.bind((host,port))

print "Socket ", soc, " bound to host ", host, " on port ", port

print "Setting speed of motors to 150" #Changes the speed at whichthe motors can change position.
s.set_speed(1,150)
s.set_speed(2,150)

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
	for case in switch(str(data)):
                if case("FORWARD"):
                        s.set_position(1,1400)
                        s.set_position(2,1400)
                        break
		if case("FORWARDx2"):
			s.set_position(1,1350)
			s.set_position(2,1350)
			break;
		if case("FORWARDx3"):
			s.set_position(1,1300)
			s.set_position(1,1300)
			break;
		if case("FORWARDx4"):
			s.set_position(1,1200)
			s.set_position(2,1200)
			break;
		if case("FORWARDx5"):
			s.set_position(1,1100)
			s.set_position(2,1100)
			break;
		if case("FORWARD_LEFT"):
			s.set_position(1,1350)
			s.set_position(2,1400)
			break;
		if case("FORWARD_RIGHT"):
			s.set_position(2,1350)
			s.set_position(1,1400)
			break;
                if case("BACK"):
                        s.set_position(1,1700)
                        s.set_position(2,1700)
                        break;
		if case("BACKx2"):
			s.set_position(1,1900)
			s.set_position(2,1900)
			break;
                if case("LEFT"):
                        s.set_position(1,1300)
			s.set_position(2,1493)
			break;
                if case("RIGHT"):
                        s.set_position(2,1300)
			s.set_position(1,1493)
			break;
		if case("CENTER"):
			s.set_position(1,1493)
			s.set_position(2,1493)		

soc.close

