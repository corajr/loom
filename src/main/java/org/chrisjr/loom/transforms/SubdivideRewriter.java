package org.chrisjr.loom.transforms;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.EventCollection;

public class SubdivideRewriter extends EventRewriter {
	public InnerSubdivideRewriter[] internalRewriters;

	public class InnerSubdivideRewriter extends EventRewriter {
		public InnerSubdivideRewriter(BigFraction shortenBy, int divisions) {
			super(new Rule[] { new SubdivideRule(shortenBy, divisions) });
		}
	}

	public SubdivideRewriter(BigFraction shortenBy, int divisions) {
		super(new Rule[] {});
		internalRewriters = new InnerSubdivideRewriter[divisions];
		for (int i = 0; i < divisions; i++) {
			BigFraction s = shortenBy.divide(i + 1);
			internalRewriters[i] = new InnerSubdivideRewriter(s, i + 1);
		}
	}

	public EventCollection apply(EventCollection original) {
		EventCollection events = original;
		for (int i = 0; i < internalRewriters.length; i++) {
			events = internalRewriters[i].apply(events);
		}
		return events;
	}
}
