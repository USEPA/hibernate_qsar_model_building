package gov.epa.util;

/**
* @author TMARTI02
*/

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

public class SleepStopper {
	// Mouse Movement Simulation
	public static void main(String[] args) throws Exception {
		Robot energy = new Robot();
		
		int delay=1000*60;
//		int delay=10;
		int move=10;
		System.out.println("Sleep stopper engaged!");
		
		while (true) {
			energy.delay(delay);
			Point pObj = MouseInfo.getPointerInfo().getLocation();
			Point pObj2 = pObj;
//			System.out.println(pObj.toString() + "x>>" + pObj.x + "  y>>" + pObj.y);
			energy.mouseMove(pObj.x + move, pObj.y + move);
			energy.mouseMove(pObj.x - move, pObj.y - move);
			energy.mouseMove(pObj2.x, pObj.y);
			pObj = MouseInfo.getPointerInfo().getLocation();
//			System.out.println(pObj.toString() + "x>>" + pObj.x + "  y>>" + pObj.y);
		}
	}

}
