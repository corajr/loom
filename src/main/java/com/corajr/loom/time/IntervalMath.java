package com.corajr.loom.time;

import org.apache.commons.math3.fraction.BigFraction;

public class IntervalMath {
	public static final double EPSILON = 1E-5;

	public static double modInterval(double value) {
		return modInterval(value, 0.0, 1.0);
	}

	public static double modInterval(double value, double intervalStart,
			double intervalEnd) {
		if (value >= intervalStart && value <= intervalEnd)
			return value;

		double length = intervalEnd - intervalStart;

		while (value < intervalStart)
			value += length;

		while (value > intervalEnd)
			value -= length;

		return value;
	}

	public static BigFraction toFraction(double value) {
		return new BigFraction(value, EPSILON, 100);
	}
}
