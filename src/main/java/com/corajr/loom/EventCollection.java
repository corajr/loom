package com.corajr.loom;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.time.Interval;

/**
 * Stores events as a sorted map indexed by start position.
 * 
 * Note: Events cannot overlap within a single collection. Overlapping events
 * (polyphony) can be achieved by creating multiple child patterns, each with
 * its own set of events.
 * 
 * @author corajr
 */

public class EventCollection extends ConcurrentSkipListMap<BigFraction, LEvent>
		implements EventQueryable {
	private static final long serialVersionUID = -4270420021705392093L;

	/**
	 * Creates a series of events from a string.
	 * 
	 * The syntax is as follows: digits from "0" through "F" (hexadecimal) will
	 * be accepted, with the values being scaled so that the maximum value
	 * present is equal to 1.0. The length of the string, N, is considered one
	 * cycle, so each value is 1/N cycles in duration.
	 * 
	 * @param string
	 * @return a new EventCollection
	 * @see fromInts
	 */
	public static EventCollection fromString(String string) {
		int n = string.length();

		List<Integer> intValues = new ArrayList<Integer>(n);
		for (int i = 0; i < n; i++) {
			intValues.add(Integer.parseInt(string.substring(i, i + 1), 16));
		}

		return fromInts(intValues);
	}

	/**
	 * Creates a series of events from integers. Each argument will be divided
	 * by the maximum value of any argument and turned into an event of 1/N
	 * cycles in duration (where N is the total number of arguments).
	 * 
	 * Example: fromInts(0, 2, 4) will create a collection with 3 events, each
	 * 1/3 a cycle long, with values 0.0, 0.5, and 1.0.
	 * 
	 * @param values
	 *            the integer values of each event
	 * @return a new EventCollection containing these events
	 */
	public static EventCollection fromInts(Integer... values) {
		List<Integer> intValues = Arrays.asList(values);
		return fromInts(intValues);
	}

	/**
	 * Creates a series of events from integers. Each argument will be divided
	 * by the maximum value of any argument and turned into an event of 1/N
	 * cycles in duration (where N is the total number of arguments).
	 * 
	 * Example: Let <code>list</code> be a list containing 0, 2, and 4. Then
	 * fromInts(list) will create a collection with 3 events, each 1/3 a cycle
	 * long, with values 0.0, 0.5, and 1.0.
	 * 
	 * @param values
	 *            the integer values of each event
	 * @return a new EventCollection containing these events
	 */
	public static EventCollection fromInts(List<Integer> intValues) {
		int max = Collections.max(intValues);
		if (max == 0)
			max = 1;
		int n = intValues.size();

		List<Double> doubleValues = new ArrayList<Double>(n);

		for (int i = 0; i < n; i++) {
			double value = ((double) intValues.get(i)) / max;
			doubleValues.add(value);
		}
		return fromDoubles(doubleValues);
	}

	/**
	 * Creates an event collection where each event is 1/N cycles long (where N
	 * is the length of the list) and each has the specified value between 0.0
	 * and 1.0 inclusive. Values out of range will result in an exception.
	 * 
	 * @param values
	 *            the input values
	 * @return a new EventCollection
	 * @throws IllegalArgumentException
	 */
	public static EventCollection fromDoubles(Double... values) {
		List<Double> doubleValues = Arrays.asList(values);
		return fromDoubles(doubleValues);
	}

	/**
	 * Creates an event collection where each event is 1/N cycles long (where N
	 * is the length of the list) and each has the specified value between 0.0
	 * and 1.0 inclusive. Values out of range will result in an exception.
	 * 
	 * @param doubleValues
	 *            the list of input values
	 * @return a new EventCollection
	 * @throws IllegalArgumentException
	 */
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
			events.put(start, new LEvent(interval, value));
		}
		return events;
	}

	/**
	 * Turn a variable number of Events into an EventCollection.
	 * 
	 * @param events
	 *            the events to be added
	 * @return a new EventCollection
	 */

	public static EventCollection fromEvents(LEvent... events) {
		return fromEvents(Arrays.asList(events));
	}

	/**
	 * Turn a generic Collection of Events into an EventCollection.
	 * 
	 * @param collection
	 *            the collection of events to be added
	 * @return a new EventCollection
	 */
	public static EventCollection fromEvents(Collection<LEvent> collection) {
		EventCollection events = new EventCollection();
		events.addAll(collection);
		return events;
	}

	/**
	 * Add an event to the collection, unless it overlaps with an existing
	 * event.
	 * 
	 * @param e
	 *            the event to add
	 * @throws IllegalStateException
	 */
	public void add(LEvent e) throws IllegalStateException {
		Collection<LEvent> existingEvents = getForInterval(e.getInterval());
		if (!existingEvents.isEmpty())
			throw new IllegalStateException(
					"Cannot add overlapping events! Create a new pattern instead.");
		put(e.getInterval().getStart(), e);
	}

	public void addAll(Collection<LEvent> events) throws IllegalStateException {
		for (LEvent e : events) {
			add(e);
		}
	}

	/**
	 * Add events after the end of the last event currently in the collection.
	 * 
	 * @param events
	 *            the events to add
	 * @throws IllegalStateException
	 */
	public void addAfterwards(Collection<LEvent> events)
			throws IllegalStateException {
		BigFraction end = getLatestEnd();
		addWithOffset(end, events);
	}

	/**
	 * Add events with a specified offset.
	 * 
	 * @param offset
	 *            the amount by which to offset events
	 * @param events
	 *            the events to add
	 */
	public void addWithOffset(BigFraction offset, Collection<LEvent> events) {
		for (LEvent e : events) {
			Interval newInterval = e.getInterval().add(offset);
			add(new LEvent(newInterval, e.getValue()));
		}
	}

	/**
	 * Find the last event in the collection and return its end time.
	 * 
	 * @return the end of the last event
	 */
	private BigFraction getLatestEnd() {
		LEvent latest = null;
		if (this.size() > 0) {
			latest = this.descendingMap().firstEntry().getValue();
		}
		return latest != null ? latest.getInterval().getEnd()
				: BigFraction.ZERO;
	}

	/**
	 * Find the span of time taken up by all of the events in this collection.
	 * 
	 * @return the total duration of this collection (null if it has no events)
	 */
	public Interval getTotalInterval() {
		if (this.size() > 0) {
			BigFraction start = this.firstKey();
			BigFraction end = getLatestEnd();
			return new Interval(start, end);
		} else {
			return null;
		}
	}

	@Override
	public Collection<LEvent> getForInterval(Interval interval) {
		List<LEvent> events = new ArrayList<LEvent>();
		for (LEvent e : this.values()) {
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
		for (LEvent e : this.values()) {
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
