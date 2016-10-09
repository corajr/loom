package com.corajr.loom;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.time.Interval;
import com.corajr.loom.time.Scheduler;

/**
 * Transforms each event of a parent EventQueryable into separate onset and
 * release events.
 * 
 * @author corajr
 * 
 */
public class EventBoundaryProxy extends EventTransformer {
	final private Pattern timeScaler;

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
			return Scheduler.DEFAULT_RESOLUTION;
	}

	@Override
	public Collection<LEvent> apply(Interval interval, LEvent e) {
		Collection<LEvent> newEvents = new ArrayList<LEvent>();

		Interval eInterval = e.getInterval();
		BigFraction instant = eInterval.getSize().divide(2);
		if (instant.compareTo(getMinimumResolution()) > 0) {
			instant = getMinimumResolution();
		}

		BigFraction start = eInterval.getStart();
		BigFraction startPlus = start.add(instant);
		BigFraction end = eInterval.getEnd();
		BigFraction endMinus = end.subtract(instant);

		LEvent[] triggers = new LEvent[] {
				new LEvent(new Interval(start, startPlus), ONSET, e),
				new LEvent(new Interval(endMinus, end), RELEASE, e) };

		for (LEvent t : triggers) {
			if (t.containedBy(interval))
				newEvents.add(t);
		}

		return newEvents;
	}
}
