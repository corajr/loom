package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.chrisjr.loom.time.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RealTimeSchedulerTest {

	private Loom loom;
	private Scheduler scheduler;
	private DiscretePattern testPattern;
	
	final double epsilon = 1.0; // 1 millisecond error at most


	@Before
	public void setUp() throws Exception {
		scheduler = new RealTimeScheduler();
		loom = new Loom(null, scheduler);
		testPattern = new DiscretePattern(loom);
		testPattern.extend(0, 1, 0, 1, 0, 1, 0, 1, 0, 1);
	}

	@After
	public void tearDown() throws Exception {
	}

	public void preparePattern(final ConcurrentLinkedQueue<Long> queue,
			Pattern pattern) {
		
		/*
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // */

		final AtomicInteger lastValue = new AtomicInteger();

		Callable<Void> notYet = new Callable<Void>() {
			public Void call() {
				lastValue.set(0);
				return null;
			}
		};

		Callable<Void> addNowToQueue = new Callable<Void>() {
			public Void call() {
				int value = lastValue.getAndSet(1);
				if (value == 0) {
					long now = System.nanoTime();
					queue.add(now);
				}
				return null;
			}
		};

		pattern.asCallable(notYet, addNowToQueue);
	}

	public long getTotalAbsoluteError(final ConcurrentLinkedQueue<Long> queue, long startNanos, long[] expectedTimesMillis) {
		long totalError = 0;
		Iterator it = queue.iterator();
		for (int i = 0; i < expectedTimesMillis.length; i++) {
			long nanos = ((Long) it.next()) - startNanos;
			totalError += nanos - (expectedTimesMillis[i] * 1000000);
		}
		
		return totalError;
	}

	public long getTotalRelativeError(final ConcurrentLinkedQueue<Long> queue, long startNanos, long[] expectedGapsMillis) {
		long totalError = 0;

		Iterator it = queue.iterator();
		List<Long> times = new ArrayList<Long>();
		while (it.hasNext()) {
			times.add((Long) it.next() - startNanos);
		}

		for (int i = 0; i < expectedGapsMillis.length; i++) {
			long gap = times.get(i + 1) - times.get(i);
			totalError += gap - (expectedGapsMillis[i] * 1000000);
		}
		
		return totalError;
	}
	
	@Test
	public void testAbsoluteTiming() {
		final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();

		long[] expectedTimesMillis = new long[] { 100, 300, 500, 700, 900 };

		preparePattern(queue, testPattern);
		
		long startNanos = System.nanoTime();
		loom.play();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		assertThat(queue.size(), is(equalTo(expectedTimesMillis.length)));

		long totalError = getTotalAbsoluteError(queue, startNanos, expectedTimesMillis);
		double avgError = totalError / expectedTimesMillis.length;
		assertThat(avgError / 1e6, is(closeTo(0, epsilon)));
	}

	@Test
	public void testRelativeTiming() {
		final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();

		long[] expectedGapsMillis = new long[] { 200, 200, 200, 200 };

		preparePattern(queue, testPattern);
		
		long startNanos = System.nanoTime();
		loom.play();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		assertThat(queue.size(), is(equalTo(expectedGapsMillis.length + 1)));

		long totalError = getTotalRelativeError(queue, startNanos, expectedGapsMillis);
		double avgError = totalError / expectedGapsMillis.length;
		assertThat(avgError / 1e6, is(closeTo(0, epsilon)));
	}

}
