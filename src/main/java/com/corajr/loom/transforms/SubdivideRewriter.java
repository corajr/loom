package com.corajr.loom.transforms;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.EventCollection;
import com.corajr.loom.LEvent;
import com.corajr.loom.time.Interval;

public class SubdivideRewriter extends EventRewriter {
	public static class SubdivideRule extends Rule {
		BigFraction shortenAmt;
		int levels;

		public SubdivideRule(BigFraction shortenAmt, int levels) {
			this.shortenAmt = shortenAmt;
			this.levels = levels;
			if (levels < 1)
				throw new IllegalArgumentException(
						"Must have at least 1 level!");
		}

		@Override
		public boolean canApply(int index, LEvent event) {
			return true;
		}

		@Override
		public Collection<LEvent> apply(int index, LEvent event) {
			ArrayList<LEvent> newEvents = new ArrayList<LEvent>();

			Interval length = event.getInterval();
			int oldLevel = (int) (event.getValue() * (levels - 1));
			double newValue = (oldLevel + 1.0) / levels;
			Interval[] longShort = Interval.shortenBy(length, shortenAmt);

			newEvents.add(new LEvent(longShort[0], newValue, event));
			newEvents.add(new LEvent(longShort[1], 0.0, event));
			return newEvents;
		}
	}

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

	@Override
	public EventCollection apply(EventCollection original) {
		EventCollection events = original;
		for (int i = 0; i < internalRewriters.length; i++) {
			events = internalRewriters[i].apply(events);
		}
		return events;
	}
}
