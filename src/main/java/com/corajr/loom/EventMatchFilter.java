package com.corajr.loom;

import java.util.Collection;
import java.util.Collections;

import com.corajr.loom.time.Interval;

/**
 * Match events from the parent EventQueryable according to a specified value.
 * 
 * @author corajr
 */
public class EventMatchFilter extends EventTransformer {
	double matchValue;
	static final double EPSILON = 1e-4;

	public EventMatchFilter(EventQueryable parentEvents, double matchValue) {
		super(parentEvents);
		this.matchValue = matchValue;
	}

	@Override
	public Collection<LEvent> apply(Interval interval, LEvent e) {
		if (Math.abs(e.getValue() - matchValue) < EPSILON)
			return Collections.singletonList(new LEvent(e.getInterval(), 1.0, e
					.getParentEvent()));
		else
			return Collections.singletonList(new LEvent(e.getInterval(), 0.0, e
					.getParentEvent()));
	}
}
