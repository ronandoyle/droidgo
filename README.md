# The Android and Lego Mindstorms Telepresence Robot

Welcome to DROID-GO
---------------------------

For information regarding the installation and setup of the Android Development Environment, as well as a general introduction to Android development, please navigate to http://developer.android.com/training/index.html.

For information on the setup of the library files, necessary for the communication between Linux and the Lego NXT via USB please see the setup guide that I have written at http://telepresence-robot.blogspot.ie/2013/03/configuring-usb-comms-between-ubuntu.html.

Once this has been setup the Python files included in this repo should work without the need for any configuration, but please feel free to alter or improve these as you see fit.

In order to have the Android app stream commands to your Linux box I suggest installing the Lighttpd web server. This is a lightweight web server that is perfectly suited to resource limited environments. http://www.lighttpd.net/

Once a webserver has been setup and configured, the PHP files included in this repository should work without the need for configurations, but again please adjust as needed.

The automation bash script I have included is very much designed for the Linux machine that I have created for. This script is designed to run at startup, in order to run the webserver, camera streaming server, Java audio server and Python drive command script. This is done in the aim of providing a "Power on go" feature to the Linux machine. This script will need to be modified to suit the structure of each individual Linux machine separately.

I suggest calling this bash script from the end of the Linux machines .bashrc / .bash_profile. Then set the Terminal to be a startup application. This way, each time the Linux machine is switched on, the script will take care of the setup.

I have provided a test Python script which can be used to simply test that the commands sent from the Android device are being received by the Python script. I recommend running this script for initial tests, as it does not require the Lego Mindstorms brick to be attached to work.

If you have any questions regarding the setup above then please feel free to contact me.

Happy Development :)
