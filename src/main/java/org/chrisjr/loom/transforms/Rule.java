package org.chrisjr.loom.transforms;

import java.util.Collection;

import org.chrisjr.loom.Event;

public abstract class Rule {
	/**
	 * Defines which events this rule applies to.
	 * 
	 * @param index
	 *            the index of event (-1 if irrelevant)
	 * @param event
	 *            the input event
	 * @return true if this rule can apply to event
	 */
	public abstract boolean canApply(int index, Event event);

	public boolean canApply(Event event) {
		return canApply(-1, event);
	}

	/**
	 * Return transformed events according to this rule
	 * 
	 * @param index
	 *            the index of event (-1 if irrelevant)
	 * @param event
	 *            the input event
	 * @return zero or more transformed events
	 */
	public abstract Collection<Event> apply(int index, Event event);

	public Collection<Event> apply(Event event) {
		return apply(-1, event);
	}
}