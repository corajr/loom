package com.corajr.loom.transforms;

import java.util.Collection;

import com.corajr.loom.LEvent;

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
	public abstract boolean canApply(int index, LEvent event);

	public boolean canApply(LEvent event) {
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
	public abstract Collection<LEvent> apply(int index, LEvent event);

	public Collection<LEvent> apply(LEvent event) {
		return apply(-1, event);
	}
}