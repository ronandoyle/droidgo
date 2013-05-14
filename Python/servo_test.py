#!/usr/bin/python
import nxt.locator, time
from nxt.sensor import *

b = nxt.locator.find_one_brick()#find brick and connect
s = MSServo(b, PORT_1)          #which sensor, brick and port to talk to

def showServerInfo():
	print "Battery Level:", 37*(0x00FF & s.get_bat_level()), "mV"
	print "Speed:", s.get_speed(1)
	print "Position:", s.get_position(1)

def servoPositionTest():
	print "Looping through position settings:"
	for i in [1000, 1500, 2000]:
		s.set_position(1,i)
		time.sleep(0.5)
		print "Position:", s.get_position(1)
	
def servoSpeedTest():
	print "Looping through speed settings:"
	for i in [0, 0x10, 0x40, 0x80, 0xA0, 0xC0, 0xFF]:
		s.set_speed(1,i)
		time.sleep(0.5)
		print "Speed:", s.get_speed(1)

showServerInfo()
servoSpeedTest()
servoPositionTest()

