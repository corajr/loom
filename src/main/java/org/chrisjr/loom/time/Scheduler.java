package org.chrisjr.loom.time;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * @author chrisjr
 * 
 *         The base class for schedulers in Loom.
 * 
 */
public abstract class Scheduler {
	
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
		// TODO if not playing, we should throw an informative exception
		assert(state != State.STOPPED);
		long elapsed = getElapsedMillis();
		
		return new BigFraction(elapsed, periodMillis);
	}

	public Interval getCurrentInterval() {
		BigFraction now = getNow();
		return new Interval(now, now.add(minimumResolution));
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
}
