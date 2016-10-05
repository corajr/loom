package com.corajr.loom.transforms;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.Pattern;
import com.corajr.loom.time.IntervalMath;

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

	public static class Speed extends Transform {
		private final BigFraction amt;

		public Speed(double amt) {
			this(IntervalMath.toFraction(amt));
		}

		public Speed(BigFraction amt) {
			this.amt = amt;
		}

		@Override
		public Pattern call(Pattern original) {
			return original.speed(amt);
		}
	}

	public static class Rewrite extends Transform {
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
