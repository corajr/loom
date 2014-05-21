package org.chrisjr.loom;

import java.util.*;

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
	
	public double getValue() {
		double value = 0.0;
		Collection<Event> activeEvents = events.getForInterval(myLoom
				.getCurrentInterval());
		for (Event e : activeEvents) {
			System.out.println(e.getValue());
			value = e.getValue();
		}
		return value;
	}

}
