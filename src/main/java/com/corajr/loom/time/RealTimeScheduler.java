package com.corajr.loom.time;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * @author corajr
 * 
 *         This Scheduler plays back pattern events as close to real time as
 *         possible.
 */
public class RealTimeScheduler extends Scheduler {
	class Timer implements Runnable {
		@Override
		public void run() {
			if (state == State.PAUSED) {
				// resume counting where we left off
				startMillis = System.currentTimeMillis() - elapsedMillis;
			} else {
				startMillis = System.currentTimeMillis();
			}

			int waitInNanos = (int) (periodMillis
					* getMinimumResolution().doubleValue() * 500000);
			if (waitInNanos > 999999)
				waitInNanos = 500000;

			BigFraction lastUpdated = getNow().subtract(getHalfMinimum());
			BigFraction nowFrac;
			while (true) {
				try {
					elapsedMillis = System.currentTimeMillis() - startMillis;

					nowFrac = getNow().add(getHalfMinimum());
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
		return elapsedMillis;
	}

	@Override
	public void play() {
		timingThread.start();
		super.play();
	}

	@Override
	public void stop() {
		timingThread.interrupt();
		super.stop();
	}
}
