package com.corajr.loom.transforms;

import java.util.Collection;
import java.util.Collections;

import com.corajr.loom.LEvent;

/**
 * @author corajr
 * 
 *         Keep only those events that match the specified value.
 * 
 */
public class MatchRewriter extends EventRewriter {
	public static class MatchRule extends Rule {
		static final double EPSILON = 1e-4;
		double value;

		public MatchRule(double value) {
			this.value = value;
		}

		@Override
		public boolean canApply(int index, LEvent event) {
			return true;
		}

		@Override
		public Collection<LEvent> apply(int index, LEvent event) {
			if (Math.abs(event.getValue() - value) < EPSILON)
				return Collections.singletonList(event);
			else
				return Collections.emptyList();
		}
	}

	public MatchRewriter(double value) {
		super(new MatchRewriter.MatchRule(value));
	}
}
