package org.chrisjr.loom.time;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.PatternCollection;

/**
 * @author chrisjr
 * 
 *         The base class for schedulers in Loom.
 * 
 */
public abstract class Scheduler {

	private PatternCollection patterns;

	public enum State {
		PLAYING, PAUSED, STOPPED
	}

	/**
	 * System time in milliseconds upon first calling <code>play()</code>.
	 */
	long startMillis = -1;

	/**
	 * Milliseconds since playing began.
	 */
	long elapsedMillis = -1;

	/**
	 * period of a complete cycle in milliseconds
	 */
	long periodMillis = 1000;

	State state = State.STOPPED;

	BigFraction minimumResolution = new BigFraction(1, 1000);

	public abstract long getElapsedMillis();

	protected BigFraction getNow() {
		if (state == State.STOPPED)
			throw new IllegalStateException(
					"Tried to retrieve a new time while the scheduler was stopped!");

		long elapsed = getElapsedMillis();

		return new BigFraction(elapsed, periodMillis);
	}

	public Interval getCurrentInterval() {
		BigFraction now = getNow();
		return new Interval(now.subtract(minimumResolution), now);
	}

	public void play() {
		state = State.PLAYING;
	}

	public void pause() {
		state = State.PAUSED;
	}

	public void stop() {
		state = State.STOPPED;
	}

	public long getPeriod() {
		return periodMillis;
	}

	public void setPeriod(long periodMillis) {
		this.periodMillis = periodMillis;
	}

	public void update() throws Exception {
		PatternCollection actives = getPatternsWithExternalMappings();
		for (Pattern pattern : actives) {
			Collection<Callable<?>> callbacks = pattern.getExternalMappings();
			for (Callable<?> callback : callbacks) {
				callback.call();
			}
		}
	}

	/**
	 * @return the patterns that have external mappings
	 */
	public PatternCollection getPatternsWithExternalMappings() {
		return patterns.getPatternsWithExternalMappings();
	}

	/**
	 * @param patterns
	 *            the patterns to manage with this scheduler
	 */
	public void setPatterns(PatternCollection patterns) {
		this.patterns = patterns;
	}
}
