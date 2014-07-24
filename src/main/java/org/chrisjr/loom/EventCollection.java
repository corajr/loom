package org.chrisjr.loom;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;

/**
 * @author chrisjr
 * 
 *         Stores events as a sorted map indexed by start position. Events
 *         cannot overlap within a single collection; polyphony can be achieved
 *         by having multiple child patterns each with its own set of events.
 * 
 */

public class EventCollection extends ConcurrentSkipListMap<BigFraction, Event>
		implements EventQueryable {
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
		// TODO specify this format and define a real parser

		int n = string.length();

		List<Integer> intValues = new ArrayList<Integer>(n);
		for (int i = 0; i < n; i++) {
			intValues.add(Integer.parseInt(string.substring(i, i + 1), 16));
		}

		return fromInts(intValues);
	}

	public static EventCollection fromInts(Integer[] values) {
		List<Integer> intValues = Arrays.asList(values);
		return fromInts(intValues);
	}

	public static EventCollection fromInts(List<Integer> intValues) {
		int max = Collections.max(intValues);
		int n = intValues.size();

		List<Double> doubleValues = new ArrayList<Double>(n);

		for (int i = 0; i < n; i++) {
			double value = ((double) intValues.get(i)) / max;
			doubleValues.add(value);
		}
		return fromDoubles(doubleValues);
	}

	public static EventCollection fromDoubles(Double[] values) {
		List<Double> doubleValues = Arrays.asList(values);
		return fromDoubles(doubleValues);
	}

	public static EventCollection fromDoubles(List<Double> doubleValues)
			throws IllegalArgumentException {
		EventCollection events = new EventCollection();

		int n = doubleValues.size();
		for (int i = 0; i < n; i++) {
			double value = doubleValues.get(i);

			if (value < 0.0 || value > 1.0)
				throw new IllegalArgumentException("Values out of range.");

			BigFraction start = new BigFraction(i, n);
			BigFraction end = start.add(new BigFraction(1, n));
			Interval interval = new Interval(start, end);
			events.put(start, new Event(interval, value));
		}
		return events;
	}

	public void add(Event e) throws IllegalStateException {
		Collection<Event> existingEvents = getForInterval(e.getInterval());
		if (!existingEvents.isEmpty())
			throw new IllegalStateException(
					"Cannot add overlapping events! Create a new pattern instead.");
		put(e.getInterval().getStart(), e);
	}

	public void addAll(Collection<Event> events) throws IllegalStateException {
		for (Event e : events) {
			add(e);
		}
	}

	public void addAfterwards(Collection<Event> events)
			throws IllegalStateException {
		BigFraction end = getLatestEnd();
		for (Event e : events) {
			Interval newInterval = e.getInterval().add(end);
			add(new Event(newInterval, e.getValue()));
		}
	}

	private BigFraction getLatestEnd() {
		Event latest = null;
		if (this.size() > 0) {
			latest = this.descendingMap().firstEntry().getValue();
		}
		return latest != null ? latest.getInterval().getEnd()
				: new BigFraction(0);
	}

	public Interval getTotalInterval() {
		BigFraction start = this.firstKey();
		BigFraction end = getLatestEnd();
		return new Interval(start, end);
	}

	@Override
	public Collection<Event> getForInterval(Interval interval) {
		List<Event> events = new ArrayList<Event>();
		for (Event e : this.values()) {
			if (e.containedBy(interval))
				events.add(e);
		}
		return events;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("EventCollection(\n\t");

		boolean first = true;
		for (Event e : this.values()) {
			if (first)
				first = false;
			else
				sb.append(",\n\t");
			sb.append(e.toString());
		}
		sb.append("\n)");

		return sb.toString();
	}
}
