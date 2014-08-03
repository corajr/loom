package org.chrisjr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.*;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.util.CallableNoop;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class CallableWithArgTest {

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	private class Incrementer implements Callable<Void> {
		final AtomicInteger[] counters;
		final int index;

		public Incrementer(final AtomicInteger[] counters, int index) {
			this.counters = counters;
			this.index = index;
		}

		@Override
		public Void call() throws Exception {
			counters[index].getAndIncrement();
			return null;
		}
	}

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
	}

	@Test
	public void onOnsetMultiple() {
		AtomicInteger[] counters = new AtomicInteger[4];
		Callable[] callables = new Callable[4];
		for (int i = 0; i < 4; i++) {
			counters[i] = new AtomicInteger();
			callables[i] = new Incrementer(counters, i);
		}

		Integer[] values = new Integer[] { 4, 3, 2, 1, 1, 2, 3, 4 };
		pattern.extend(values);

		pattern.onOnset(callables);

		for (int i = 0; i < 8; i++) {
			scheduler.setElapsedMillis(125 * i);
			for (int j = 0; j < counters.length; j++) {
				System.out.print(counters[j].get());
				System.out.print(" ");
			}
			System.out.println();
			// System.out.println(counters[values[i]].get());
			// assertThat(counters[values[i]].get(), is(equalTo(i < 4 ? 1 :
			// 2)));
		}

	}
}
