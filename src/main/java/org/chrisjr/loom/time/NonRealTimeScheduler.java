package org.chrisjr.loom.time;

/**
 * @author chrisjr
 * 
 *         This Scheduler records pattern events to a file for later rendering.
 * 
 */
public class NonRealTimeScheduler extends Scheduler {

	// NonRealTimeScheduler only progresses when explicitly updated via
	// <code>setElapsedMillis</code>.
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
				try {
					update();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			this.elapsedMillis = elapsedMillis;
		}
	}

	public long getElapsedMillis() {
		return elapsedMillis;
	}
}
