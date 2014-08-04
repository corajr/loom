package org.chrisjr.loom.time;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * @author chrisjr
 * 
 *         This Scheduler plays back pattern events as close to real time as
 *         possible.
 */
public class RealTimeScheduler extends Scheduler {
	class Timer implements Runnable {
		public long getElapsedMillis() {
			return state == State.PAUSED ? elapsedMillis : System
					.currentTimeMillis() - startMillis;
		}

		@Override
		public void run() {
			long now = System.currentTimeMillis();

			if (state == State.PAUSED) {
				// resume counting where we left off
				startMillis = now - elapsedMillis;
			} else {
				startMillis = System.currentTimeMillis();
			}

			int waitInNanos = (int) (periodMillis
					* minimumResolution.doubleValue() * 500000);
			if (waitInNanos > 999999)
				waitInNanos = 500000;

			BigFraction lastUpdated = getNow().subtract(halfMinimum);
			BigFraction nowFrac;
			while (true) {
				try {
					nowFrac = getNow().add(halfMinimum);
					updateFor(new Interval(lastUpdated, nowFrac));
					lastUpdated = nowFrac;
					Thread.sleep(0, waitInNanos);
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	private final Timer timer;
	private final Thread timingThread;

	public RealTimeScheduler() {
		timer = new Timer();
		timingThread = new Thread(timer);
	}

	@Override
	public long getElapsedMillis() {
		return timer.getElapsedMillis();
	}

	@Override
	public void play() {
		timingThread.start();

		super.play();
	}
}
