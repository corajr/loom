package com.corajr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.corajr.loom.time.Interval;

/**
 * Abstract class for transforming the events of an EventQueryable. For each
 * event in the original collection, zero or more events may be returned.
 * 
 * @author corajr
 */

public abstract class EventTransformer implements EventQueryable {
	final private EventQueryable parentEvents;

	public EventTransformer(EventQueryable parentEvents) {
		this.parentEvents = parentEvents;
	}

	public abstract Collection<LEvent> apply(Interval interval, LEvent e);

	@Override
	public Collection<LEvent> getForInterval(Interval interval) {
		Collection<LEvent> realEvents = parentEvents.getForInterval(interval);
		List<LEvent> events = new ArrayList<LEvent>();

		for (LEvent e : realEvents) {
			events.addAll(apply(interval, e));
		}

		return events;
	}
}
