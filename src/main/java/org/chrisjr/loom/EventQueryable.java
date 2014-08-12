package org.chrisjr.loom;

import java.util.Collection;

import org.chrisjr.loom.time.Interval;

/**
 * Allows the caller to ask for all events falling within a given query
 * interval.
 * 
 * @author chrisjr
 */
public interface EventQueryable {
	public Collection<Event> getForInterval(Interval interval);
}
