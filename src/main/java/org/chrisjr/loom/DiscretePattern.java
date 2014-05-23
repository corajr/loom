package org.chrisjr.loom;

import java.util.*;

import org.chrisjr.loom.time.Interval;

public class DiscretePattern extends Pattern {
	public EventCollection events = new EventCollection();

	public DiscretePattern(Loom loom) {
		super(loom);
	}

	/**
	 * @param string
	 *            a string such as "10010010" describing a pattern to be tacked
	 *            on at the end
	 * @return the updated pattern
	 */
	public Pattern extend(String string) {
		EventCollection newEvents = EventCollection.fromString(string);
		this.events.addAfterwards(newEvents.values());
		return this;
	}

	public Pattern extend(Integer... values) {
		EventCollection newEvents = EventCollection.fromInts(values);
		this.events.addAfterwards(newEvents.values());
		return this;
	}
	
	public double getValue() {
		double value = 0.0;
		Interval interval = myLoom.getCurrentInterval();
		if (isLooping) {
			interval = interval.modulo(loopInterval);
		}
		Collection<Event> activeEvents = events.getForInterval(interval);
		for (Event e : activeEvents) {
			value = e.getValue();
		}
		return value;
	}

}
