#!/usr/bin/env python
### Example of Servo Functions ###
### User guide at http://www.mindsensors.com/index.php?module=documents&JAS_DocumentManager_op=list

import nxt.locator
import time
import socket
import sys
from nxt.sensor import *

print "Initializing Python Script"
#print "The passed arguments are ", sys.argv

#message = sys.argv[1]
#print "Message created"
#if message is not None:
#        print "Message is not none"
#        ifile = open( "/home/wit/nxt-python-2.2.2/my_programs/output.txt", "w+")
#        print "File created"
#        ifile.write(message + '\n')
#        print "Written to file"
#        ifile.close()
#        print "File closed"
#
#print message, " has been added to output.txt"
print "Locating brick and servos..."
b = nxt.locator.find_one_brick()#find brick and connect
s = MSServo(b, PORT_1)          #which sensor, brick and port to talk to
print "Brick and servos found!"
def reset():
        print "Resetting the positions to 1500..."
	s.set_position(1,1493)
	s.set_position(2,1493)
	print "Positions reset"

reset()



