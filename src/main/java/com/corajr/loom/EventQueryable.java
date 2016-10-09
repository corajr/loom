package com.corajr.loom;

import java.util.Collection;

import com.corajr.loom.time.Interval;

/**
 * Allows the caller to ask for all events falling within a given query
 * interval.
 * 
 * @author corajr
 */
public interface EventQueryable {
	public Collection<LEvent> getForInterval(Interval interval);
}
