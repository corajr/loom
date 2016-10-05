package com.corajr.loom.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.corajr.loom.transforms.Transform;
import com.corajr.loom.transforms.Transforms;
import com.corajr.loom.util.CallableNoop;
import com.corajr.loom.util.CallableOnChange;
import com.corajr.loom.util.StatefulCallable;

public class CallableOnChangeTest {

	@Test
	public void transformToCallable() {
		Transform reverse = new Transforms.Reverse();
		StatefulCallable[] callables = CallableOnChange.fromTransform(reverse,
				null);
		assertThat(callables.length, is(equalTo(2)));
	}

	@Test
	public void fromNoop() throws Exception {
		AtomicInteger ai = new AtomicInteger();
		CallableOnChange wrappedNoop = new CallableOnChange(ai,
				new CallableNoop(), 1);

		wrappedNoop.call(); // for coverage

		assertThat(wrappedNoop.toString().startsWith("CallableOnChange"),
				is(equalTo(true)));

	}

	@Test
	public void onlyOnce() throws Exception {
		final AtomicInteger totalCount = new AtomicInteger();

		StatefulCallable[] callables = CallableOnChange
				.fromCallables(new Callable<Void>() {
					@Override
					public Void call() {
						totalCount.incrementAndGet();
						return null;
					}
				});

		StatefulCallable one = callables[0];
		StatefulCallable add = callables[1];

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

	@Test
	public void onlyOnceWithTwoCallables() throws Exception {
		final AtomicInteger count1 = new AtomicInteger();
		final AtomicInteger count2 = new AtomicInteger();

		Callable<Void> one = new Callable<Void>() {
			@Override
			public Void call() {
				count1.incrementAndGet();
				return null;
			}
		};

		Callable<Void> two = new Callable<Void>() {
			@Override
			public Void call() {
				count2.incrementAndGet();
				return null;
			}
		};

		StatefulCallable[] callables = CallableOnChange.fromCallables(one, two);

		callables[1].call();
		callables[1].call();
		assertThat(count1.get(), is(equalTo(1)));
		assertThat(count2.get(), is(equalTo(0)));

		callables[1].call();
		callables[2].call();
		assertThat(count1.get(), is(equalTo(1)));
		assertThat(count2.get(), is(equalTo(1)));

		callables[0].call();
		callables[2].call();

		assertThat(count1.get(), is(equalTo(1)));
		assertThat(count2.get(), is(equalTo(2)));
	}

}
