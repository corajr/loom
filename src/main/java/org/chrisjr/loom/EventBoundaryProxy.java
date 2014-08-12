package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;

/**
 * Transforms each event of a parent EventQueryable into separate onset and
 * release events.
 * 
 * @author chrisjr
 * 
 */
public class EventBoundaryProxy extends EventTransformer {
	final private Pattern timeScaler;
	static final BigFraction DEFAULT_RESOLUTION = new BigFraction(1, 1000);

	public static final double ONSET = 1.0;
	public static final double RELEASE = 0.5;

	public EventBoundaryProxy(Pattern timeScaler, EventQueryable parentEvents) {
		super(parentEvents);
		this.timeScaler = timeScaler;
	}

	public BigFraction getMinimumResolution() {
		if (timeScaler != null)
			return timeScaler.getMinimumResolution();
		else
			return DEFAULT_RESOLUTION;
	}

	@Override
	public Collection<Event> apply(Interval interval, Event e) {
		Collection<Event> newEvents = new ArrayList<Event>();

		Interval eInterval = e.getInterval();
		BigFraction instant = eInterval.getSize().divide(10);
		if (instant.compareTo(getMinimumResolution()) > 0) {
			instant = getMinimumResolution();
		}

		BigFraction start = eInterval.getStart();
		BigFraction startPlus = start.add(instant);
		BigFraction end = eInterval.getEnd();
		BigFraction endMinus = end.subtract(instant);

		Event[] triggers = new Event[] {
				new Event(new Interval(start, startPlus), ONSET, e),
				new Event(new Interval(endMinus, end), RELEASE, e) };

		for (Event t : triggers) {
			if (t.containedBy(interval))
				newEvents.add(t);
		}

		return newEvents;
	}
}
