package com.corajr.loom.continuous;

import java.util.concurrent.atomic.*;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * A "trigger" pattern that can be fired by outside events.
 * 
 * @author corajr
 * 
 */
public class TriggerFunction extends ContinuousFunction {
	private final AtomicBoolean fired = new AtomicBoolean();
	private final AtomicInteger countdown = new AtomicInteger();

	public void fire() {
		fired.set(true);
	}

	@Override
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
