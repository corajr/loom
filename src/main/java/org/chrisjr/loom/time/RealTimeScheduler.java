package org.chrisjr.loom.time;

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

			while (true) {
				try {
					update();
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

	private Timer timer;
	private Thread timingThread;

	public RealTimeScheduler() {
		timer = new Timer();
		timingThread = new Thread(timer);
	}

	public long getElapsedMillis() {
		return timer.getElapsedMillis();
	}

	public void play() {
		timingThread.start();

		super.play();
	}
}
