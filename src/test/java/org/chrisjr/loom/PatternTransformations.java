package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
	public void reverse() {
		pattern.extend("0101");

		Pattern pattern2 = new Pattern(loom);
		pattern2.extend("1010");

		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern2.getValue(), is(equalTo(pattern.getValue())));
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
				System.out.print(time);
				System.out.print(" ");
				System.out.println(pattern.asInt());
				assertThat(pattern.asInt(), is(equalTo((i + j) % 2)));
			}
		}
	}
}
