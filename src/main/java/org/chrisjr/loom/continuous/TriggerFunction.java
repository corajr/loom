package org.chrisjr.loom.continuous;

import java.util.concurrent.atomic.*;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * Accepts an AtomicInteger as input; when this integer is set to 2, the
 * function will return 1.0 the next time it is polled.
 * 
 * @author chrisjr
 * 
 */
public class TriggerFunction extends ContinuousFunction {
	final AtomicBoolean fired;
	private AtomicInteger countdown = new AtomicInteger();

	public TriggerFunction(final AtomicBoolean fired) {
		this.fired = fired;
	}

	public double call(BigFraction t) {
		boolean wasFired = fired.getAndSet(false);
		if (wasFired) {
			// will be called twice (once for start and end of interval)
			// so we must return 1.0 twice
			countdown.set(2);
		}

		int value = countdown.get();
		if (value > 0)
			countdown.decrementAndGet();

		return value > 0 ? 1.0 : 0.0;
	}

}
