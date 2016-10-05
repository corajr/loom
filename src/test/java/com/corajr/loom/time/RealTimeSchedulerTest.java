package com.corajr.loom.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.time.RealTimeScheduler;
import com.corajr.loom.time.Scheduler;
import com.corajr.loom.util.CallableOnChange;
import com.corajr.loom.util.StatefulCallable;

public class RealTimeSchedulerTest {

	private Loom loom;
	private Scheduler scheduler;
	private Pattern testPattern;

	final double epsilon = 1.0; // millisecond error

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		scheduler = new RealTimeScheduler();
		loom = new Loom(null, scheduler);
		testPattern = new Pattern(loom);
		testPattern.extend(0, 1, 0, 1, 0, 1, 0, 1, 0, 1);
		testPattern.loop();
	}

	@After
	public void tearDown() throws Exception {
	}

	public void preparePattern(final ConcurrentLinkedQueue<Long> queue,
			Pattern pattern) {

		/*
		 * try { Thread.sleep(15000); } catch (InterruptedException e) {
		 * e.printStackTrace(); } //
		 */

		StatefulCallable[] ops = CallableOnChange
				.fromCallables(new Callable<Void>() {
					@Override
					public Void call() {
						long now = System.nanoTime();
						queue.add(now);
						return null;
					}
				});

		pattern.asStatefulCallable(ops);
	}

	public long getTotalAbsoluteError(final ConcurrentLinkedQueue<Long> queue,
			long startNanos, long[] expectedTimesMillis) {
		long totalError = 0;
		Iterator<Long> it = queue.iterator();
		for (int i = 0; i < expectedTimesMillis.length; i++) {
			long nanos = (it.next()) - startNanos;
			totalError += nanos - (expectedTimesMillis[i] * 1000000);
		}

		return totalError;
	}

	public long getTotalRelativeError(final ConcurrentLinkedQueue<Long> queue,
			long startNanos, long[] expectedGapsMillis) {
		long totalError = 0;

		Iterator<Long> it = queue.iterator();
		List<Long> times = new ArrayList<Long>();
		while (it.hasNext()) {
			times.add(it.next() - startNanos);
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

		long totalError = getTotalAbsoluteError(queue, startNanos,
				expectedTimesMillis);
		double avgError = totalError / expectedTimesMillis.length;
		assertThat(avgError / 1e6, is(closeTo(0, epsilon)));
	}

	public void relativeTimings(int repeats) {
		final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();

		long[] expectedGapsMillis = new long[(repeats * 5) - 1];
		Arrays.fill(expectedGapsMillis, 200);

		preparePattern(queue, testPattern);

		long startNanos = System.nanoTime();
		loom.play();
		try {
			Thread.sleep(1000 * repeats + 10);
		} catch (InterruptedException e) {
		}

		assertThat(queue.size(), is(equalTo(expectedGapsMillis.length + 1)));

		long totalError = getTotalRelativeError(queue, startNanos,
				expectedGapsMillis);
		double avgError = totalError / expectedGapsMillis.length;
		assertThat(avgError / 1e6, is(closeTo(0, epsilon)));
	}

	@Test
	public void testRelativeTiming() {
		relativeTimings(1);
	}

	@Ignore
	public void longtermTiming() {
		relativeTimings(10);
	}

	@Test
	public void throwExceptionWhenStopped() {
		thrown.expect(IllegalStateException.class);
		loom.getCurrentInterval();
	}
}
