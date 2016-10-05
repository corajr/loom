package com.corajr.loom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.ConcretePattern;
import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.transforms.Transforms;
import com.corajr.loom.util.CallableOnChange;

public class PatternTransformationsTest {

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
		pattern.extend("0123");
		pattern.asInt(0, 3);
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
			assertThat(pattern.asInt(), is(equalTo(i)));
		}
	}

	@Test
	public void doubleSpeed() {
		pattern.speed(2);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((125 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i)));
		}
	}

	public void checkIfShifting(int measureLength, double beatLength,
			int shiftBy, int beatsTillShift) {
		for (int i = 0; i < beatsTillShift * 2; i++) {
			long time = (long) (beatLength * i) + 1;
			int shifts = beatsTillShift > 0 ? i / beatsTillShift : 1;
			scheduler.setElapsedMillis(time);
			int expecting = (measureLength + i + (shiftBy * shifts))
					% measureLength;
			// System.out.format("%d %d %s %d\n", pattern.asInt(), expecting,
			// pattern.getTimeOffset(), time);
			assertThat(pattern.asInt(), is(equalTo(expecting)));
		}
	}

	@Test
	public void shiftLeft() {
		pattern.loop();
		pattern.delay(0.25);

		checkIfShifting(4, 250, -1, 0);
	}

	@Test
	public void shiftRight() {
		pattern.loop();
		pattern.shift(0.25);

		checkIfShifting(4, 250, 1, 0);
	}

	@Test
	public void shiftRightEveryCycle() {
		pattern.loop();
		pattern.every(1, new Transforms.Shift(1, 4));

		checkIfShifting(4, 250, 1, 4);
	}

	@Test
	public void shiftLeftEveryCycle() {
		pattern.loop();
		pattern.every(1, new Transforms.Shift(-1, 4));

		checkIfShifting(4, 250, -1, 4);
	}

	@Test
	public void shiftRightEveryHalfCycle() {
		pattern.loop();
		pattern.every(0.5, new Transforms.Shift(1, 4));

		checkIfShifting(4, 250, 1, 2);
	}

	@Test
	public void shiftRightEveryOtherCycle() {
		pattern.loop();
		pattern.every(2, new Transforms.Shift(1, 4));

		checkIfShifting(4, 250, 1, 8);
	}

	@Test
	public void shiftLeftEveryHalfCycle() {
		pattern.loop();
		pattern.every(0.5, new Transforms.Shift(-1, 4));

		checkIfShifting(4, 250, -1, 2);
	}

	@Test
	public void shiftLeftEveryOtherCycle() {
		pattern.loop();
		pattern.every(2, new Transforms.Shift(-1, 4));

		checkIfShifting(4, 250, -1, 8);
	}

	@Test
	public void shiftLeftEveryTwelveCycles() {
		pattern.clear();
		pattern.extend("0123456789AB");
		pattern.asInt(0, 11);
		pattern.loop();
		pattern.every(12, new Transforms.Shift(-1, 12));

		checkIfShifting(12, 1000 / 12.0, -1, 144);
	}

	@Test
	public void shiftRightEveryTwelveCycles() {
		pattern.clear();
		pattern.extend("0123456789AB");
		pattern.asInt(0, 11);
		pattern.loop();
		pattern.every(12, new Transforms.Shift(1, 12));

		checkIfShifting(12, 1000 / 12.0, 1, 144);
	}

	@Test
	public void reverse() {
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo((3 - i) % 4)));
		}
	}

	@Test
	public void reverseTwiceIsUnchanged() {
		pattern.reverse();
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i)));
		}
	}

	public void checkIfReversing(int beatLength, int beatsTillReverse) {
		for (int i = 0; i < 16; i++) {
			long time = (beatLength * i) + 1;
			boolean reversed = (i / beatsTillReverse) % 2 == 1;
			scheduler.setElapsedMillis(time);
			int expecting = reversed ? (3 - (i % 4)) % 4 : (i % 4);
			// System.out.format("%d %d %s %d\n", pattern.asInt(), expecting,
			// pattern.getTimeScale(), time);
			assertThat(pattern.asInt(), is(equalTo(expecting)));
		}
	}

	@Test
	public void reverseEveryCycle() {
		pattern.every(1, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(250, 4);
	}

	@Test
	public void slowAndReverseEveryCycle() {
		pattern.speed(0.1);
		pattern.every(1, new Transforms.Reverse());
		pattern.loop();

		checkIfReversing(2500, 4);
	}

	@Test
	public void reverseEveryHalfCycle() {
		pattern.every(0.5, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(250, 2);
	}

	@Test
	public void reverseEveryOtherCycle() {
		pattern.every(2, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(250, 8);
	}

	@Test
	public void speedUpAndReverse() {
		pattern.speed(5);

		pattern.every(1, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(50, 4);
	}

	@Test
	public void slowAndReverse() {
		pattern.speed(0.5);

		pattern.every(1, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(500, 4);
	}

	@Test
	public void reverseThenSlow() {
		pattern.every(1, new Transforms.Reverse());
		pattern.speed(0.1);
		pattern.loop();

		checkIfReversing(2500, 4);
	}

	@Test
	public void invert() {
		pattern.invert();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(4 - (i + 1))));
		}
	}

	private void countBeats(Pattern pattern, Integer... expected) {
		final AtomicInteger counter = new AtomicInteger();
		pattern.onOnset(new Callable<Void>() {
			@Override
			public Void call() {
				counter.incrementAndGet();
				return null;
			}
		});

		for (int i = 0; i < expected.length / 2; i++) {
			int millis = expected[i * 2];
			int expectedCount = expected[i * 2 + 1];
			scheduler.setElapsedMillis(millis);
			assertThat(counter.get(), is(equalTo(expectedCount)));
		}
	}

	@Test
	public void repeat() {
		pattern.repeat(4);
		countBeats(pattern, 999, 4, 1999, 8, 2999, 12, 3999, 16, 5999, 16);
	}

	@Test
	public void repeatThenStop() {
		Pattern pat = pattern.repeat(4).then(
				Pattern.fromInts(loom, 3, 2, 1, 0).asInt(0, 3));

		for (int i = 0; i < 20; i++) {
			scheduler.setElapsedMillis(250 * i);
			int expected = i < 16 ? i % 4 : 4 - (i % 4) - 1;
			assertThat(pat.asInt(), is(equalTo(expected)));
		}
	}

	@Test
	public void after() {
		final AtomicInteger ai = new AtomicInteger();
		pattern.loop();
		pattern.after(10, new Callable<Void>() {
			@Override
			public Void call() {
				ai.incrementAndGet();
				return null;
			}
		});

		countBeats(pattern, 20000, 81);
		assertThat(ai.get(), is(equalTo(1)));
	}

	@Test
	public void slowAfter() {
		pattern.after(1, new Transforms.Speed(0.5));
		pattern.loop();
		countBeats(pattern, 999, 4, 1000, 5, 1500, 6, 2000, 7, 2500, 8, 3000,
				9, 3500, 10);
	}

	@Test
	public void slowEveryCycle() {
		pattern.every(1, new Transforms.Speed(0.5));
		pattern.loop();

		countBeats(pattern, 0, 1, 250, 2, 500, 3, 750, 4, 1000, 5, 1500, 6,
				2000, 7, 2500, 8, 3000, 9, 3500, 10, 4000, 11, 5000, 12, 6000,
				13, 7000, 14, 9000, 15, 11000, 16, 13000, 17, 15000, 18, 19000,
				19);
	}

	@Test
	public void slowEveryHalfCycle() {
		pattern.every(0.5, new Transforms.Speed(0.5));
		pattern.loop();

		countBeats(pattern, 0, 1, 250, 2, 500, 3, 1000, 4, 1500, 5, 2500, 6,
				3500, 7, 5500, 8, 7500, 9, 11500, 10, 15500, 11);
	}

	@Test
	public void speedUpEveryCycle() {
		pattern.every(1, new Transforms.Speed(5));
		pattern.loop();

		countBeats(pattern, 0, 1, 250, 2, 500, 3, 750, 4, 1000, 5, 1050, 6,
				1100, 7, 1150, 8, 1200, 9, 1210, 10, 1220, 11, 1230, 12, 1240,
				13, 1242, 14, 1244, 15, 1246, 16);
	}

	@Test
	public void speedUpEveryHalfCycle() {
		pattern.every(0.5, new Transforms.Speed(5));
		pattern.loop();

		countBeats(pattern, 0, 1, 250, 2, 500, 3, 550, 4, 600, 5, 610, 6, 620,
				7, 622, 8);

	}

	@Test
	public void onOnset() {
		pattern.clear();
		pattern.extend("1101");

		countBeats(pattern, 1001, 4);
	}

	@Test
	public void manyTinyOnsets() {
		int n = 2000;
		pattern.clear();

		Integer[] allOnes = new Integer[n];
		Arrays.fill(allOnes, 1);

		pattern.extend(allOnes);

		countBeats(pattern, 1001, n);
	}

	@Test
	public void onRelease() {
		pattern.clear();
		pattern.extend("1101");

		countBeats(pattern, 1001, 4);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void noteOnAndOff() {
		final AtomicInteger noteOffs = new AtomicInteger();
		final AtomicInteger noteOns = new AtomicInteger();

		Callable<Void> noteOffInc = new Callable<Void>() {
			@Override
			public Void call() {
				noteOffs.getAndIncrement();
				return null;
			}
		};

		Callable<Void> noteOnInc = new Callable<Void>() {
			@Override
			public Void call() {
				noteOns.getAndIncrement();
				return null;
			}
		};

		pattern.extend("1111");

		ConcretePattern trigger = ConcretePattern.forEach(pattern
				.getConcretePattern());
		trigger.asStatefulCallable(CallableOnChange.fromCallables(noteOffInc,
				noteOnInc));
		pattern.addChild(trigger);

		scheduler.setElapsedMillis(500);
		assertThat(noteOns.get(), is(equalTo(3)));
		assertThat(noteOffs.get(), is(equalTo(2)));
		scheduler.setElapsedMillis(999);
		assertThat(noteOns.get(), is(equalTo(4)));
		assertThat(noteOffs.get(), is(equalTo(4)));
		scheduler.setElapsedMillis(1000);
		assertThat(noteOns.get(), is(equalTo(5)));
		assertThat(noteOffs.get(), is(equalTo(4)));
	}
}
