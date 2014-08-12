package org.chrisjr.loom.time;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.PatternCollection;

/**
 * The base class for schedulers in Loom.
 * 
 * @author chrisjr
 */
public abstract class Scheduler {

	private PatternCollection patterns;

	/**
	 * The state of the scheduler.
	 */
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

	/**
	 * One millisecond at standard playback speed.
	 */
	public static final BigFraction minimumResolution = new BigFraction(1, 1000);
	public static final BigFraction halfMinimum = minimumResolution.divide(2);

	/**
	 * Implementations of the Scheduler class must provide the present time when
	 * queried.
	 * 
	 * @return the number of milliseconds elapsed
	 */
	public abstract long getElapsedMillis();

	/**
	 * Returns the current time in milliseconds divided by the period.
	 * 
	 * @return the present moment as a fraction of a cycle
	 */
	public BigFraction getNow() {
		if (state == State.STOPPED)
			throw new IllegalStateException(
					"Tried to retrieve the time while stopped! "
							+ "Please call loom.play() or loom.record().");

		long elapsed = getElapsedMillis();

		return new BigFraction(elapsed, periodMillis);
	}

	/**
	 * Retrieves an {@link Interval} that extends before and after the current
	 * instant by half the minimum resolution.
	 * 
	 * @return an interval
	 */
	public Interval getCurrentInterval() {
		BigFraction now = getNow();
		return new Interval(now.subtract(halfMinimum), now.add(halfMinimum));
	}

	/**
	 * Start playback. Must be called before querying the scheduler!
	 */
	public void play() {
		state = State.PLAYING;
	}

	/**
	 * Pause playback.
	 */
	public void pause() {
		state = State.PAUSED;
	}

	/**
	 * Stop playback. Additional queries to the scheduler will throw an
	 * exception until {@link #play} is called again.
	 */
	public void stop() {
		state = State.STOPPED;
	}

	public long getPeriod() {
		return periodMillis;
	}

	/**
	 * Sets the period of the scheduler.
	 * 
	 * @param periodMillis
	 *            the new period
	 */
	public void setPeriod(long periodMillis) {
		this.periodMillis = periodMillis;
	}

	/**
	 * Runs all callbacks associated with the current interval.
	 */
	public void update() {
		updateFor(getCurrentInterval());
	}

	/**
	 * Runs all callbacks for a given interval. If a pattern is made of discrete
	 * events, the callback for each event will be called individually. For a
	 * continuous pattern, only one callback will be called.
	 * 
	 * @param interval
	 *            the interval over which to run callbacks
	 */
	public void updateFor(Interval interval) {
		PatternCollection actives = getPatternsWithActiveMappings();
		for (Pattern pattern : actives) {
			Collection<Callable<?>> callbacks = pattern
					.getActiveMappingsFor(interval);
			for (Callable<?> callback : callbacks) {
				if (callback != null)
					try {
						callback.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}
	}

	/**
	 * Retrieve the patterns that we must query for callbacks.
	 * 
	 * @return the patterns that have external mappings
	 */
	public PatternCollection getPatternsWithActiveMappings() {
		return patterns.getPatternsWithActiveMappings();
	}

	/**
	 * Sets the {@link PatternCollection} associated with this scheduler, needs
	 * for active mappings.
	 * 
	 * @param patterns
	 *            the patterns to manage with this scheduler
	 */
	public void setPatterns(PatternCollection patterns) {
		this.patterns = patterns;
	}
}
