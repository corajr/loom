package org.chrisjr.loom.transforms;

import java.util.*;

import org.chrisjr.loom.LEvent;
import org.chrisjr.loom.EventCollection;

/**
 * Takes in an EventCollection and outputs a new EventCollection.
 * 
 * @author chrisjr
 * 
 */
public abstract class EventRewriter {
	ArrayList<Rule> rules;

	public EventRewriter(Rule... rules) {
		this(Arrays.asList(rules));
	}

	public EventRewriter(Collection<Rule> rules) {
		this.rules = new ArrayList<Rule>();
		this.rules.addAll(rules);
	}

	public EventCollection apply(EventCollection originalEvents) {
		EventCollection newEvents = new EventCollection();

		int i = 0;
		for (LEvent event : originalEvents.values()) {
			for (Rule rule : rules) {
				if (rule.canApply(i, event)) {
					newEvents.addAll(rule.apply(i, event));
					break;
				}
			}
			i++;
		}
		return newEvents;
	}
}
