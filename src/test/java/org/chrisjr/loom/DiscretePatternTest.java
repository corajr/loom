package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

	@Test
	public void canBeExtendedWithString() {
		pattern.extend("0101");

		assertThat(pattern.getValue(), is(equalTo(0.0)));
		scheduler.setElapsedMillis(251);
		assertThat(pattern.getValue(), is(equalTo(1.0)));
	}

	@Test
	public void canBeExtendedWithInts() {
		pattern.extend(0, 1, 0, 1);

		assertThat(pattern.getValue(), is(equalTo(0.0)));
		scheduler.setElapsedMillis(251);
		assertThat(pattern.getValue(), is(equalTo(1.0)));
	}

	@Test
	public void canBeLooped() {
		pattern.extend("0101");
		pattern.loop();

		scheduler.setElapsedMillis(1251);
		assertThat(pattern.getValue(), is(equalTo(1.0)));
	}

	@Test
	public void canStopLooping() {
		pattern.extend("0101");
		pattern.loop();

		scheduler.setElapsedMillis(1251);
		assertThat(pattern.getValue(), is(equalTo(1.0)));

		pattern.once();
		assertThat(pattern.getValue(), is(equalTo(0.0)));
	}

	@Test
	public void canBeOffset() {
		pattern.extend("0101");
		pattern.setTimeOffset(new BigFraction(1,4));

		scheduler.setElapsedMillis(501);
		assertThat(pattern.getValue(), is(equalTo(1.0)));
	}

	@Ignore
	public void clonedPatternsAreDistinct() throws CloneNotSupportedException {
		pattern.extend("0101");
		Pattern pattern2 = pattern.clone();
		pattern2.extend("1010");
		assertThat(pattern.getEvents().size(), is(equalTo(4)));
		assertThat(pattern2.getEvents().size(), is(equalTo(8)));
	}
}
