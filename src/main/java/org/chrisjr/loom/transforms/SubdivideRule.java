package org.chrisjr.loom.transforms;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Event;
import org.chrisjr.loom.time.Interval;

public class SubdivideRule extends Rule {
	BigFraction shortenAmt;
	int levels;

	public SubdivideRule(BigFraction shortenAmt, int levels) {
		this.shortenAmt = shortenAmt;
		this.levels = levels;
		if (levels < 1)
			throw new IllegalArgumentException("Must have at least 1 level!");
	}

	@Override
	public boolean canApply(int index, Event event) {
		return true;
	}

	@Override
	public Collection<Event> apply(int index, Event event) {
		ArrayList<Event> newEvents = new ArrayList<Event>();

		Interval length = event.getInterval();
		int oldLevel = (int) (event.getValue() * (levels - 1));
		double newValue = (oldLevel + 1.0) / levels;
		Interval[] longShort = Interval.shortenBy(length, shortenAmt);

		newEvents.add(new Event(longShort[0], newValue));
		newEvents.add(new Event(longShort[1], 0.0));
		return newEvents;
	}
}
