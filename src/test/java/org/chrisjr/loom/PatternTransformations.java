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
		pattern.extend("0101");
		pattern.asInt(0, 1);
	}

	@After
	public void tearDown() throws Exception {
		scheduler = null;
		loom = null;
		pattern = null;
	}

	@Test
	public void halfSpeed() {
		pattern.speed(0.5);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((500 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}

	@Test
	public void doubleSpeed() {
		pattern.speed(2);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((125 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}

	@Test
	public void shiftRight() {
		pattern.loop();
		pattern.shift(0.25);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * (i + 1)) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}

	@Test
	public void shiftLeft() {
		pattern.loop();
		pattern.shift(-0.25);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo((i + 1) % 2)));
		}
	}

	@Test
	public void reverse() {
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo((i + 1) % 2)));
		}
	}

	@Test
	public void reverseTwiceIsUnchanged() {
		pattern.reverse();
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i % 2)));
		}
	}
	
	public void checkIfReversing(int beatLength) {
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				long time = (j * beatLength * 4) + (beatLength * i) + 1;
				scheduler.setElapsedMillis(time);
				assertThat(pattern.asInt(), is(equalTo((i + j) % 2)));
			}
		}
	}

	@Test
	public void reverseEveryCycle() {
		Transform reverse = new Transforms.Reverse();
		pattern.every(1, reverse);

		pattern.loop();
		
		checkIfReversing(250);
	}


	@Test
	public void speedUpAndReverse() {
		pattern.speed(5);

		pattern.every(1, new Transforms.Reverse());

		pattern.loop();
		System.out.println(pattern.getChild(1));

		checkIfReversing(50);
	}

	@Test
	public void slowAndReverse() {
		pattern.speed(0.1);

		pattern.every(10, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(2500);
	}

	@Test
	public void invert() {
		pattern.invert();

		scheduler.setElapsedMillis(251);
		assertThat(pattern.asInt(), is(equalTo(0)));
	}

	@Test
	public void forEach() {
		pattern.clear();
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
