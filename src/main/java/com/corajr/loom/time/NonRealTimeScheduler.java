package com.corajr.loom.time;

/**
 * @author corajr
 * 
 *         This Scheduler records pattern events to a file for later rendering.
 * 
 */
public class NonRealTimeScheduler extends Scheduler {

	// NonRealTimeScheduler only progresses when explicitly updated via
	// <code>setElapsedMillis</code>.
	@Override
	public void play() {
		super.play();
	}

	/**
	 * Jump to a new time.
	 * 
	 * @param elapsedMillis
	 */
	public void setElapsedMillis(long elapsedMillis) {
		if (this.elapsedMillis < elapsedMillis) {
			while (this.elapsedMillis < elapsedMillis) {
				this.elapsedMillis++;
				update();
			}
		} else {
			this.elapsedMillis = elapsedMillis;
		}
	}

	@Override
	public long getElapsedMillis() {
		return elapsedMillis;
	}
}
