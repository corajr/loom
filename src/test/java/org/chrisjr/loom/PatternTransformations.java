package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.transforms.Transform;
import org.chrisjr.loom.transforms.Transforms;
import org.chrisjr.loom.transforms.Transforms.Reverse;
import org.junit.*;
import org.junit.rules.ExpectedException;

public class PatternTransformations {

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		scheduler.play();
		pattern = new Pattern(loom);
	}

	@After
	public void tearDown() throws Exception {
		scheduler = null;
		loom = null;
		pattern = null;
	}

	@Test
	public void halfSpeed() {
		pattern.extend("0101");
		pattern.asInt(0, 1);
		pattern.speed(0.5);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((500 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}

	@Test
	public void doubleSpeed() {
		pattern.extend("0101");
		pattern.asInt(0, 1);
		pattern.speed(2);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((125 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}

	@Test
	public void shiftRight() {
		pattern.extend("0101");
		pattern.asInt(0, 1);
		pattern.loop();
		pattern.shift(0.25);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * (i + 1)) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}

	@Test
	public void shiftLeft() {
		pattern.extend("0101");
		pattern.asInt(0, 1);
		pattern.loop();
		pattern.shift(-0.25);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			System.out.println(pattern.asInt());
			assertThat(pattern.asInt(), is(equalTo((i + 1) % 2)));
		}
	}

	@Test
	public void reverse() {
		pattern.extend("0101");
		pattern.asInt(0, 1);
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo((i + 1) % 2)));
		}
	}

	@Test
	public void reverseTwiceIsUnchanged() {
		pattern.extend("0101");
		pattern.asInt(0, 1);
		pattern.reverse();
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}

	@Test
	public void reverseAfterCycleManually() {
		pattern.extend("0101");
		pattern.asInt(0, 1);
		pattern.loop();

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				long time = (j * 1000) + (250 * i) + 1;
				scheduler.setElapsedMillis(time);
				assertThat(pattern.asInt(), is(equalTo((i + j) % 2)));
			}
			pattern.reverse();
		}
	}

	@Test
	public void reverseEveryCycle() {
		pattern.extend("0101");
		pattern.asInt(0, 1);

		pattern.every(1, new Transforms.Reverse());

		pattern.loop();

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				long time = (j * 1000) + (250 * i) + 1;
				scheduler.setElapsedMillis(time);
				assertThat(pattern.asInt(), is(equalTo((i + j) % 2)));
			}
		}
	}

	@Test
	public void forEach() {
		pattern.extend("1101");

		final AtomicInteger counter = new AtomicInteger();
		pattern.forEach(new Callable<Void>() {
			public Void call() {
				counter.incrementAndGet();
				return null;
			}
		});

		scheduler.setElapsedMillis(1001);
		assertThat(counter.get(), is(equalTo(3)));

	}
}
