package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.chrisjr.loom.time.Interval;

public abstract class EventTransformer implements EventQueryable {
	final private EventQueryable parentEvents;

	public EventTransformer(EventQueryable parentEvents) {
		this.parentEvents = parentEvents;
	}

	public abstract Collection<Event> apply(Interval interval, Event e);

	@Override
	public Collection<Event> getForInterval(Interval interval) {
		Collection<Event> realEvents = parentEvents.getForInterval(interval);
		List<Event> events = new ArrayList<Event>();

		for (Event e : realEvents) {
			events.addAll(apply(interval, e));
		}

		return events;
	}
}
