#!/usr/bin/env python

# This file was used to test positions on 90deg servo motors.

import nxt.locator
import time
import sys
from nxt.sensor import *

b = nxt.locator.find_one_brick()
s = MSServo(b, PORT_1)
s.set_quick(1, 150)
def runMotor():
	x = 0
	s.command('S')
	s.set_speed(1,50)
	s.set_speed(2,50)
	while ( x == 0):
		for i in [1000, 1500, 2000]:
			print "Battery Level:", 37*(0x00FF & s.get_bat_level()), "mV"
			s.set_position(1,i)
			s.set_position(2,i)
			time.sleep(1)
			s.command('S')
			print "Position: ", s.get_position(1)
			print "Speed: ", s.get_speed(1)
	
runMotor()
