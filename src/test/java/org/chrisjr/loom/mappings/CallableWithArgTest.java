package org.chrisjr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.*;
import org.chrisjr.loom.time.NonRealTimeScheduler;
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
	public void onOnsetWithValue() {
		AtomicInteger[] counters = new AtomicInteger[4];
		Callable[] callables = new Callable[4];
		for (int i = 0; i < 4; i++) {
			counters[i] = new AtomicInteger();
			callables[i] = new Incrementer(counters, i);
		}

		Mapping<Callable<Void>> callableMap = new ObjectMapping<Callable<Void>>(
				callables);

		Integer[] values = new Integer[] { 3, 2, 1, 0, 0, 1, 2, 3 };
		pattern.extend(values);

		pattern.onOnsetWithValue(callableMap);

		for (int i = 0; i < 8; i++) {
			scheduler.setElapsedMillis(125 * i);
			System.out.println(counters[values[i]].get());
			assertThat(counters[values[i]].get(), is(equalTo(i < 4 ? 1 : 2)));
		}

	}
}
