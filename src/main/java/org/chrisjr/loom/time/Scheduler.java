package org.chrisjr.loom.time;

import org.chrisjr.loom.PatternCollection;

/**
 * @author chrisjr
 * 
 *         The base class for schedulers in Loom.
 * 
 */
public abstract class Scheduler {
	public PatternCollection patterns;

	/**
	 * period of a complete cycle in milliseconds
	 */
	long periodMillis;

	abstract public void play();
	abstract public void pause();
	abstract public void stop();
}
