package org.chrisjr.loom;

import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.math3.fraction.Fraction;
import org.chrisjr.loom.time.Interval;

/**
 * @author chrisjr
 *
 * Events are stored as a sorted map indexed by start position.
 *
 */

// TODO using a map means no overlapping events in a collection. Is this desirable?

public class EventCollection extends ConcurrentSkipListMap<Fraction, Event> {
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
			
			Fraction start = new Fraction(i, n);
			Fraction end = start.add(new Fraction(1, n));
			Interval interval = new Interval(start, end);
			events.put(start, new Event(interval, value));
		}
		
		return events;
	}
}
