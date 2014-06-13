package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;

public class EventBoundaryProxy implements EventQueryable {
	final private Pattern timeScaler;
	final private EventQueryable parentEvents;
	static final BigFraction DEFAULT_RESOLUTION = new BigFraction(1, 1000);

	public static final double ONSET = 1.0;
	public static final double RELEASE = 0.5;
	public static final double SUSTAIN = 0.0;

	public EventBoundaryProxy(Pattern timeScaler, EventQueryable parentEvents) {
		this.timeScaler = timeScaler;
		this.parentEvents = parentEvents;
	}

	public BigFraction getMinimumResolution() {
		if (timeScaler != null)
			return timeScaler.getMinimumResolution();
		else
			return DEFAULT_RESOLUTION;
	}

	@Override
	public Collection<Event> getForInterval(Interval interval) {
		Collection<Event> realEvents = parentEvents.getForInterval(interval);
		List<Event> events = new ArrayList<Event>();

		BigFraction instant = getMinimumResolution();

		for (Event e : realEvents) {
			Interval eInterval = e.getInterval();

			BigFraction start = eInterval.getStart();
			BigFraction startPlus = start.add(instant);
			BigFraction end = eInterval.getEnd();
			BigFraction endMinus = end.subtract(instant);

			Event[] triggers = new Event[] {
					new Event(new Interval(start, startPlus), ONSET),
					new Event(new Interval(endMinus, end), RELEASE) };
			// new Event(new Interval(startPlus.add(instant),
			// endMinus.subtract(instant)), SUSTAIN) };

			for (Event t : triggers) {
				if (t.containedBy(interval))
					events.add(t);
			}
		}

		return events;
	}
}
