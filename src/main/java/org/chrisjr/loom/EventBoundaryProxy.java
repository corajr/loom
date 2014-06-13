package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;

public class EventBoundaryProxy implements EventQueryable {
	final private Pattern parent;
	final private EventQueryable parentEvents;
	static final BigFraction DEFAULT_RESOLUTION = new BigFraction(1, 1000);

	public EventBoundaryProxy(Pattern parent) {
		this.parent = parent;
		this.parentEvents = parent.getEvents();
	}

	public EventBoundaryProxy(EventQueryable parentEvents) {
		this.parent = null;
		this.parentEvents = parentEvents;
	}

	public BigFraction getMinimumResolution() {
		if (parent != null)
			return parent.getMinimumResolution().divide(2);
		else
			return DEFAULT_RESOLUTION.divide(2);
	}

	@Override
	public Collection<Event> getForInterval(Interval interval) {
		Collection<Event> realEvents = parentEvents.getForInterval(interval);
		List<Event> events = new ArrayList<Event>();

		for (Event e : realEvents) {
			Interval eInterval = e.getInterval();
			BigFraction start = eInterval.getStart();
			BigFraction end = eInterval.getEnd();
			BigFraction instant = getMinimumResolution();

			Event noteOn = new Event(new Interval(start.subtract(instant),
					start.add(instant)), 1.0);
			Event noteOff = new Event(new Interval(end.subtract(instant),
					end.add(instant)), 0.5);

			if (noteOn.containedBy(interval))
				events.add(noteOn);
			if (noteOff.containedBy(interval))
				events.add(noteOff);
		}
		return events;
	}

}
