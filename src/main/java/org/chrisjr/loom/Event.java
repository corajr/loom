package org.chrisjr.loom;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;

/**
 * @author chrisjr An Event has a start, an end, and a value. The start and end
 *         are expressed as fractions of a cycle, and the value is constant
 *         during that time.
 * 
 *         (For "events" that vary over time, use a ContinuousPattern.)
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

	public boolean containedBy(Interval queryInterval) {
		BigFraction queryStart = queryInterval.getStart();
		BigFraction queryEnd = queryInterval.getEnd();

		BigFraction start = interval.getStart();
		BigFraction end = interval.getEnd();

		boolean startsBeforeOrAtQueryEnd = start.compareTo(queryEnd) <= 0;
		boolean endsAfterQueryStart = end.compareTo(queryStart) > 0;

		return startsBeforeOrAtQueryEnd && endsAfterQueryStart;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((interval == null) ? 0 : interval.hashCode());
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		if (interval == null) {
			if (other.interval != null)
				return false;
		} else if (!interval.equals(other.interval))
			return false;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		return true;
	}

	public String toString() {
		return "Event(" + interval.toString() + " == " + String.valueOf(value)
				+ ")";
	}
}
