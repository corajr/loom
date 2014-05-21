package org.chrisjr.loom;

import org.apache.commons.math3.fraction.Fraction;

/**
 * @author chrisjr
 * An Event has a start, an end, and a value. The start and end are expressed
 * as fractions of a cycle, and the value is constant during that time.
 * 
 * (For "events" that vary over time, use a ContinuousPattern.)
 */
public class Event {
	Fraction start;
	Fraction end;
	double value;

	public Event(Fraction theStart, Fraction theEnd, double theValue) {
		start = theStart;
		end = theEnd;
		value = theValue;
	}
}
