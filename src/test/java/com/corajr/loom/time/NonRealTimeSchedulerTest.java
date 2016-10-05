package com.corajr.loom.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.util.CallableOnChange;
import com.corajr.loom.util.StatefulCallable;

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
				.fromCallables(new Callable<Void>() {
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
