package org.chrisjr.loom;

import java.util.Collection;

import org.chrisjr.loom.time.Interval;

public interface EventQueryable {
	public Collection<Event> getForInterval(Interval interval);
}
