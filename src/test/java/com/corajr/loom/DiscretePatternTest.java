package com.corajr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.LEvent;
import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.transforms.MatchRewriter;

public class DiscretePatternTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler); // attachment to PApplet is not needed
											// here
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
	public void addedToLoom() {
		assertTrue(loom.patterns.contains(pattern));
	}

	@Test
	public void doesNotReturnIfEmpty() {
		thrown.expect(IllegalStateException.class);
		pattern.getValue();
	}

	private void checkValues(Pattern pattern, double... timeAndValue) {
		for (int i = 0; i < timeAndValue.length / 2; i++) {
			int millis = (int) timeAndValue[i * 2];
			double expectedValue = timeAndValue[i * 2 + 1];
			scheduler.setElapsedMillis(millis);
			assertThat(pattern.getValue(), is(equalTo(expectedValue)));
		}
	}

	@Test
	public void canBeExtendedWithString() {
		pattern.extend("0101");

		checkValues(pattern, 0, 0.0, 251, 1.0);
	}

	@Test
	public void canBeExtendedWithInts() {
		pattern.extend(0, 1, 0, 1);

		checkValues(pattern, 0, 0.0, 251, 1.0);
	}

	@Test
	public void canBeLooped() {
		pattern.extend("0101");
		pattern.loop();

		checkValues(pattern, 1251, 1.0);
	}

	@Test
	public void canStopLooping() {
		pattern.extend("0101");
		pattern.loop();

		checkValues(pattern, 1251, 1.0);

		pattern.once();

		checkValues(pattern, 1251, 0.0);
	}

	@Test
	public void canBeRepeatedAndExtended() {
		pattern.extend("0001");
		pattern.repeat(4).after(10, LEvent.evt(0.25, 0.5));

		checkValues(pattern, 751, 1.0, 1751, 1.0, 2751, 1.0, 3751, 1.0, 4001,
				0.0, 4751, 0.0, 10001, 0.5, 10251, 0.0);
	}

	@Test
	public void canBeOffset() {
		pattern.extend("0101");
		pattern.setTimeOffset(new BigFraction(1, 4));

		scheduler.setElapsedMillis(501);
		assertThat(pattern.getValue(), is(equalTo(1.0)));
	}

	@Test
	public void canRewrite() {
		pattern.extend("0101");
		pattern.rewrite(new MatchRewriter(1.0));
		assertThat(pattern.getEvents().size(), is(equalTo(2)));
	}

	@Test
	public void clonedPatternsAreDistinct() throws CloneNotSupportedException {
		pattern.extend("0101");
		Pattern pattern2 = pattern.clone();
		pattern2.extend("1010");
		assertThat(pattern.getEvents().size(), is(equalTo(4)));
		assertThat(pattern2.getEvents().size(), is(equalTo(8)));
	}
}
