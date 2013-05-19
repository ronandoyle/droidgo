package com.droidgo;


public class DriveClass {
	
	
	

	private String inArea;
	

	

	private enum DriveCommands{
		FORWARD, FORWARDx2, FORWARDx3, FORWARDx4, FORWARDx5, FORWARD_LEFT, FORWARD_RIGHT, BACK, BACKx2, CENTER, LEFT, RIGHT
	};
	private DriveCommands driveCommands;
	
	
	public DriveClass()
	{
		setDriveCommands(DriveCommands.CENTER);
		inArea = "";
	}
	
	public String getInArea() {
		return inArea;
	}

	public void setInArea(String inArea) {
		this.inArea = inArea;
	}
	
	public DriveCommands getDriveCommands() {
		return driveCommands;
	}

	public void setDriveCommands(DriveCommands driveCommands) {
		this.driveCommands = driveCommands;
	}
	
	public void driveRegion(int x, int y) {

		/**
		 * The following x,y values are hardcoded for a HTC One S screen
		 * dimensions. Tests have not been properly carried out on other
		 * devices.
		 * 
		 * Will more than likely need to change these values to be dynamic,
		 * based on the devices screen size.
		 */
		
		if (y >= 139 && y < 159 && (x >= 250 && x <= 290)) 
		{
//			driveCommands = DriveCommands.FORWARD;
			setDriveCommands(DriveCommands.FORWARD);
		} 
		else if (y >= 119 && y < 139 && (x >= 250 && x <= 290)) 
		{
//			driveCommands = DriveCommands.FORWARDx2;
			setDriveCommands(DriveCommands.FORWARDx2);
		}
		else if (y >= 99 && y < 119 && (x >= 250 && x <= 290))
		{
//			driveCommands = DriveCommands.FORWARDx3;
			setDriveCommands(DriveCommands.FORWARDx3);
		}
		else if (y >= 79 && y < 99 && (x >= 250 && x <= 290))
		{
//			driveCommands = DriveCommands.FORWARDx4;
			setDriveCommands(DriveCommands.FORWARDx4);
		}
		else if (y < 79 && (x >= 250 && x <= 290))
		{
//			driveCommands = DriveCommands.FORWARDx5;
			setDriveCommands(DriveCommands.FORWARDx5);
		}
		
		else if (x < 250 && (y >= 159 && y <= 199)) {
//			driveCommands = DriveCommands.LEFT;
			setDriveCommands(DriveCommands.LEFT);
		}
		// right
		//FLYER 325
		else if (x > 290 && (y >= 159 && y <= 199)) {
//			driveCommands = DriveCommands.RIGHT;
			setDriveCommands(DriveCommands.RIGHT);
		}
		
		// diagonal
		else if (y < 159 && x < 250)
		{
//			driveCommands = DriveCommands.FORWARD_LEFT;
			setDriveCommands(DriveCommands.FORWARD_LEFT);
		}
		else if (y < 159 && x > 250)
		{
//			driveCommands = DriveCommands.FORWARD_RIGHT;
			setDriveCommands(DriveCommands.FORWARD_RIGHT);
		}
		// reverse
		// FLYER 267
		else if (y > 199 && y < 280) 
		{
//			driveCommands = DriveCommands.BACK;
			setDriveCommands(DriveCommands.BACK);
		} 
		else if (y > 280) 
		{
//			driveCommands = DriveCommands.BACKx2;
			setDriveCommands(DriveCommands.BACKx2);
		}
		// center
		// FLYER 187 267
		else if (y >= 159 && y <= 199) 
		{
//			driveCommands = DriveCommands.CENTER;
			setDriveCommands(DriveCommands.CENTER);
		}
		

		driveSwitch();
	}
	
	public void driveSwitch() {
		switch (driveCommands) {
		case FORWARD:
			if (getInArea() != "FORWARD") {
				new SendToServer("FORWARD").execute();
				System.out.println("FORWARD");
				setInArea("FORWARD");
			}
			break;
		case FORWARDx2:
			if (getInArea() != "FORWARDx2") {
				new SendToServer("FORWARDx2").execute();
				System.out.println("FORWARDx2");
				setInArea("FORWARDx2");
			}
			break;
		case FORWARDx3:
			if (getInArea() != "FORWARDx3") {
				new SendToServer("FORWARDx3").execute();
				System.out.println("FORWARDx3");
				setInArea("FORWARDx3");
			}
			break;
		case FORWARDx4:
			if (getInArea() != "FORWARDx4") {
				new SendToServer("FORWARDx4").execute();
				System.out.println("FORWARDx4");
				setInArea("FORWARDx4");
			}
			break;
		case FORWARDx5:
			if (getInArea() != "FORWARDx5") {
				new SendToServer("FORWARDx5").execute();
				System.out.println("FORWARDx5");
				setInArea("FORWARDx5");
			}
			break;
		case FORWARD_LEFT:
			if (getInArea() != "FORWARD_LEFT") {
				new SendToServer("FORWARD_LEFT").execute();
				System.out.println("FORWARD_LEFT");
				setInArea("FORWARD_LEFT");
			}
			break;
		case FORWARD_RIGHT:
			if (getInArea() != "FORWARD_RIGHT") {
				new SendToServer("FORWARD_RIGHT").execute();
				System.out.println("FORWARD_RIGHT");
				setInArea("FORWARD_RIGHT");
			}
			break;
		case BACK:
			if (getInArea() != "BACK") {
				new SendToServer("BACK").execute();
				System.out.println("BACK");
				setInArea("BACK");
			}
			break;
		case BACKx2:
			if (getInArea() != "BACKx2") {
				new SendToServer("BACKx2").execute();
				System.out.println("BACKx2");
				setInArea("BACKx2");
			}
			break;
		case LEFT:
			if(getInArea() != "LEFT"){
				 new SendToServer("LEFT").execute();
				 System.out.println("LEFT");
				 setInArea("LEFT");
			}
			break;
		case RIGHT:
			if(getInArea() != "RIGHT"){
				 new SendToServer("RIGHT").execute();
				 System.out.println("RIGHT");
				 setInArea("RIGHT");
			}
			break;
		case CENTER:
			if (getInArea() != "CENTER") {
				new SendToServer("CENTER").execute();
				System.out.println("CENTER");
				setInArea("CENTER");
			}
			break;
		default:
		}
	}

}
