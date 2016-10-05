package com.corajr.loom.continuous;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.continuous.DelayFunction;
import com.corajr.loom.continuous.FollowerFunction;
import com.corajr.loom.continuous.MatchFunction;
import com.corajr.loom.continuous.SineFunction;
import com.corajr.loom.continuous.ThresholdFunction;
import com.corajr.loom.continuous.TriggerFunction;
import com.corajr.loom.time.*;
import com.corajr.loom.transforms.MatchRewriter;

public class ContinuousPatternTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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

	private void checkValues(double epsilon, Pattern pattern,
			double... timeAndValue) {
		for (int i = 0; i < timeAndValue.length / 2; i++) {
			int millis = (int) timeAndValue[i * 2];
			double expectedValue = timeAndValue[i * 2 + 1];
			scheduler.setElapsedMillis(millis);
			assertThat(pattern.getValue(), is(closeTo(expectedValue, epsilon)));
		}
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

		Pattern pattern2 = Pattern.following(pattern);

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

	@Test
	public void matchDiscretePattern() {
		pattern = new Pattern(loom);
		pattern.extend("0201");

		double toBeMatched = 0.5;
		Pattern pattern2 = new Pattern(loom, new MatchFunction(pattern,
				toBeMatched));

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis(i * 250);
			assertTrue((pattern.getValue() == toBeMatched) == (pattern2
					.getValue() == 1.0)); // pattern2 == 1.0 iff pattern matched
		}
	}

	@Test
	public void thresholdDiscretePattern() {
		pattern = new Pattern(loom);
		pattern.extend("0201");

		double threshold = 0.5;
		Pattern pattern2 = new Pattern(loom, new ThresholdFunction(pattern,
				threshold));

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis(i * 250);
			assertTrue((pattern.getValue() >= threshold) == (pattern2
					.getValue() == 1.0)); // pattern2 == 1.0 iff pattern >=
											// threshold
		}
	}

	@Test
	public void continuousThenDiscrete() {
		Pattern discrete = Pattern.fromString(loom, "1343");
		Pattern sinePattern = new Pattern(loom, new SineFunction());

		pattern = sinePattern.then(discrete);

		double epsilon = 1e-2;

		checkValues(epsilon, pattern, 250, 1.0, 500, 0.5, 750, 0.0, 999, 0.5,
				1001, 0.25, 1250, 0.75, 1500, 1.0, 1750, 0.75, 2001, 0.0, 2250,
				0.0);
	}

	@Test
	public void discreteThenContinuous() {
		Pattern discrete = Pattern.fromString(loom, "1343");
		Pattern sinePattern = new Pattern(loom, new SineFunction());

		pattern = discrete.then(sinePattern);

		double epsilon = 1e-2;

		checkValues(epsilon, pattern, 0, 0.25, 250, 0.75, 500, 1.0, 750, 0.75,
				1000, 0.5, 1250, 1.0, 1500, 0.5, 1750, 0.0, 1999, 0.5);
	}

	@Test
	public void cannotRewriteIfContinuous() {
		pattern = new Pattern(loom);

		thrown.expect(IllegalStateException.class);
		pattern.rewrite(new MatchRewriter(1.0));
	}

}
