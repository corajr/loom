package org.chrisjr.loom.time;

/**
 * @author chrisjr
 *
 * This Scheduler plays back pattern events as close to real time as possible.
 */
public class RealTimeScheduler extends Scheduler {

	public long getElapsedMillis() {
		return state == State.PAUSED ? elapsedMillis : System.currentTimeMillis() - startMillis;
	}
		
	public void play() {
		long now = System.currentTimeMillis();

		if (state == State.PAUSED) {
			// resume counting where we left off
			startMillis = now - elapsedMillis;
		} else {
			startMillis = System.currentTimeMillis();			
		}

		super.play();
	}
}
