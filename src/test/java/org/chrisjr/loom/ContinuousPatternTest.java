package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.atomic.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.continuous.*;
import org.chrisjr.loom.time.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContinuousPatternTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		scheduler = null;
		loom = null;
	}

	@Test
	public void sinePattern() {
		pattern = new Pattern(loom, new SineFunction());

		final double epsilon = 1e-4;

		scheduler.setElapsedMillis(250);
		assertThat(pattern.getValue(), is(closeTo(1.0, epsilon)));
		scheduler.setElapsedMillis(500);
		assertThat(pattern.getValue(), is(closeTo(0.5, epsilon)));
		scheduler.setElapsedMillis(750);
		assertThat(pattern.getValue(), is(closeTo(0.0, epsilon)));
		scheduler.setElapsedMillis(1000);
		assertThat(pattern.getValue(), is(closeTo(0.5, epsilon)));
	}

	@Test
	public void canBeCloned() throws CloneNotSupportedException {
		pattern = new Pattern(loom, new SineFunction());

		Pattern pattern2 = pattern.clone();

		// TODO this test makes no sense until patterns can be modified.

		final double epsilon = 1e-3;

		scheduler.setElapsedMillis(250);
		assertThat(pattern.getValue(), is(closeTo(1.0, epsilon)));

		assertThat(pattern2.getValue(), is(closeTo(1.0, epsilon)));
	}

	@Test
	public void followContinuousPattern() {
		pattern = new Pattern(loom, new SineFunction());

		Pattern pattern2 = new Pattern(loom, new FollowerFunction(pattern));

		final double epsilon = 1e-5;

		for (int i = 0; i < 10; i++) {
			scheduler.setElapsedMillis(i * 100);
			assertThat(pattern.getValue(),
					is(closeTo(pattern2.getValue(), epsilon)));
		}
	}

	@Test
	public void followDiscretePattern() {
		pattern = new Pattern(loom);
		pattern.extend("0101");

		Pattern pattern2 = new Pattern(loom, new FollowerFunction(pattern));

		final double epsilon = 1e-5;

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis(i * 250);
			assertThat(pattern.getValue(),
					is(closeTo(pattern2.getValue(), epsilon)));
		}
	}


	@Test
	public void delayDiscretePattern() {
		pattern = new Pattern(loom);
		pattern.extend("0101");
		
		int offset = 250;
		
		Pattern pattern2 = new Pattern(loom, new DelayFunction(pattern,
				new BigFraction(offset, 1000)));

		final double epsilon = 1e-5;

		double oldValue = pattern.getValue();

		for (int i = 1; i < 4; i++) {
			scheduler.setElapsedMillis(i * offset);
			assertThat(pattern2.getValue(), is(closeTo(oldValue, epsilon)));
			oldValue = pattern.getValue();
		}
	}

	@Test
	public void delayPattern() {
		pattern = new Pattern(loom, new SineFunction());
		
		int offset = 300;

		Pattern pattern2 = new Pattern(loom, new DelayFunction(pattern,
				new BigFraction(offset, 1000)));

		final double epsilon = 1e-2;

		double oldValue = pattern.getValue();

		for (int i = 1; i < 10; i++) {
			scheduler.setElapsedMillis(i * offset);
			assertThat(pattern2.getValue(), is(closeTo(oldValue, epsilon)));
			oldValue = pattern.getValue();
		}
	}
	
	@Test
	public void triggerPattern() {
		TriggerFunction trigger = new TriggerFunction();
		pattern = new Pattern(loom, trigger);
		
		assertThat(pattern.getValue(), is(equalTo(0.0)));
		trigger.fire();
		assertThat(pattern.getValue(), is(equalTo(1.0)));
		assertThat(pattern.getValue(), is(equalTo(0.0)));
	}
}
