package org.chrisjr.loom.time;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.chrisjr.loom.Loom;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.util.CallableOnChange;
import org.chrisjr.loom.util.StatefulCallable;
import org.chrisjr.loom.util.StatefulNoop;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NonRealTimeSchedulerTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern testPattern;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		testPattern = new Pattern(loom);
		testPattern.extend(0, 1, 0, 1);

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUpdate() {
		testPattern.loop();

		final AtomicInteger totalCount = new AtomicInteger();

		StatefulCallable[] ops = CallableOnChange
				.fromCallable(new Callable<Void>() {
					public Void call() {
						totalCount.incrementAndGet();
						return null;
					}
				});

		testPattern.asStatefulCallable(ops);

		scheduler.setElapsedMillis(2001);

		assertThat(totalCount.get(), is(equalTo(4)));
	}

}
