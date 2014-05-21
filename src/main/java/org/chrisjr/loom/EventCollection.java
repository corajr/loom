package org.chrisjr.loom;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;

/**
 * @author chrisjr
 *
 * Events are stored as a sorted map indexed by start position.
 *
 */

// TODO using a map means no overlapping events in a collection. Is this desirable?

public class EventCollection extends ConcurrentSkipListMap<BigFraction, Event> {
	private static final long serialVersionUID = -4270420021705392093L;
	
	/**
	 * Creates a series of events from a string.
	 * 
	 * For now, only "0" and "1" will be accepted. The length of the string, N,
	 * is considered one cycle, so each value is 1/N in duration.
	 * 
	 * @param string
	 * @return a new EventCollection
	 */
	public static EventCollection fromString(String string) {
		EventCollection events = new EventCollection();
		
		// TODO specify this format and define a real parser
		
		int n = string.length();
		for (int i = 0; i < n; i++) {
			double value = (double) Integer.valueOf(string.substring(i, i+1));
			
			BigFraction start = new BigFraction(i, n);
			BigFraction end = start.add(new BigFraction(1, n));
			Interval interval = new Interval(start, end);
			events.put(start, new Event(interval, value));
		}
		
		return events;
	}
	
	public void add(Event e) throws IllegalStateException {		
		Collection<Event> existingEvents = getForInterval(e.getInterval());
		if (!existingEvents.isEmpty()) throw new IllegalStateException("Cannot add overlapping events! Create a new pattern instead.");
		put(e.getInterval().getStart(), e);
	}
	
	public Collection<Event> getForInterval(Interval interval) {
		List<Event> events = new ArrayList<Event>();
		for (Event e : this.values()) {
			int startsCompared = e.getInterval().getStart().compareTo(interval.getStart());
			int endsCompared = e.getInterval().getEnd().compareTo(interval.getEnd());
			if (startsCompared >= 0 && endsCompared <= 0) events.add(e);			
		}
		return events;
	}
}
