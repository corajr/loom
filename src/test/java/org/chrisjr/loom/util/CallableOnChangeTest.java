package org.chrisjr.loom.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CallableOnChangeTest {

	@Test
	public void onlyOnce() throws Exception {
		final AtomicInteger lastValue = new AtomicInteger();
		final AtomicInteger totalCount = new AtomicInteger();

		StatefulCallable one = new StatefulNoop(lastValue);
		StatefulCallable add = new CallableOnChange(lastValue,
				new Callable<Void>() {
					public Void call() {
						totalCount.incrementAndGet();
						return null;
					}
				});

		one.call();
		add.call(); // +1 == 1
		add.call(); // -- == 1

		assertThat(totalCount.get(), is(equalTo(1)));
		
		one.call(); // reset
		add.call(); // +1 == 2
		add.call(); // -- == 2
		add.call(); // -- == 2

		assertThat(totalCount.get(), is(equalTo(2)));
	}

}
