package org.chrisjr.loom.transforms;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;

public class Transforms {
	public static class Noop extends Transform {
		@Override
		public Pattern call(Pattern original) {
			return original;
		}
	}

	public static class Reverse extends Transform {
		@Override
		public Pattern call(Pattern original) {
			return original.reverse();
		}
	}

	public class Rewrite extends Transform {
		EventRewriter eventRewriter;

		public Rewrite(EventRewriter eventRewriter) {
			this.eventRewriter = eventRewriter;
		}

		@Override
		public Pattern call(Pattern original) {
			return original.rewrite(eventRewriter);
		}
	}

	public static class Shift extends Transform {
		BigFraction shiftAmt;

		public Shift(int numerator, int denominator) {
			shiftAmt = new BigFraction(numerator, denominator);
		}

		@Override
		public Pattern call(Pattern original) {
			return original.shift(shiftAmt);
		}
	}
}
