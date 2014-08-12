package org.chrisjr.loom.time;

public class IntervalMath {
	public static double modInterval(double value) {
		return modInterval(value, 0.0, 1.0);
	}
	
	public static double modInterval(double value, double intervalStart, double intervalEnd) {
		if (value >= intervalStart && value <= intervalEnd)
			return value;
		
		double length = intervalEnd - intervalStart;
		
		while (value < intervalStart)
			value += length;			

		while (value > intervalEnd)
			value -= length;
		
		return value;			
	}
}
