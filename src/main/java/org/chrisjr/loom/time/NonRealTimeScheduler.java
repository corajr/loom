package org.chrisjr.loom.time;

/**
 * @author chrisjr
 * 
 * This Scheduler records pattern events to a file for later rendering.
 *
 */
public class NonRealTimeScheduler extends Scheduler {

	// NonRealTimeScheduler only progresses when explicitly updated via <code>setNow</code>.
	public void play() {
		super.play();		
	}

	void setNow(long _elapsedMillis) {
		elapsedMillis = _elapsedMillis;		
	}
	
	public long getElapsedMillis() {
		return elapsedMillis;
	}
}
