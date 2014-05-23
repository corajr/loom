package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Iterator;
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

	@Before
	public void setUp() throws Exception {
		scheduler = new RealTimeScheduler();
		loom = new Loom(null, scheduler);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTiming() {
		final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();
		
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
		
		DiscretePattern pattern = new DiscretePattern(loom);
		
		pattern.extend(0, 1, 0, 1, 0, 1, 0, 1, 0, 1);
		
		long[] expectedTimesMillis = new long[]{100, 300, 500, 700, 900};
		pattern.asCallable(notYet, addNowToQueue);
		
		long startNanos = System.nanoTime();
		loom.play();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertThat(queue.size(), is(equalTo(expectedTimesMillis.length)));

		long totalError = 0;
		Iterator<Long> it = queue.iterator();
		for (int i = 0; i < expectedTimesMillis.length; i++) {
			long trueNanos = ((Long) it.next()) - startNanos;
			long trueMillis = trueNanos / 1000000;
			long error = trueMillis - expectedTimesMillis[i];
			totalError += error;
		}
		System.out.println(totalError);
		double avgError = totalError / expectedTimesMillis.length;
		assertThat(avgError, is(closeTo(0, 0.1)));
	}

}
