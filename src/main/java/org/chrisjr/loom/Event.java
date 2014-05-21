package org.chrisjr.loom;

import org.chrisjr.loom.time.Interval;

/**
 * @author chrisjr
 * An Event has a start, an end, and a value. The start and end are expressed
 * as fractions of a cycle, and the value is constant during that time.
 * 
 * (For "events" that vary over time, use a ContinuousPattern.)
 */
public class Event {
	final private Interval interval;
	final private double value;

	public Event(Interval _interval, double _value) {
		interval = _interval;
		value = _value;
	}

	public Interval getInterval() {
		return interval;
	}

	public double getValue() {
		return value;
	}
}
