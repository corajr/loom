package com.corajr.loom.time;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.time.Interval;

public class IntervalTest {
	private Interval interval;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		interval = new Interval(0, 1);
	}

	@After
	public void tearDown() throws Exception {
		interval = null;
	}

	@Test
	public void mustStartBeforeEnd() {
		thrown.expect(IllegalArgumentException.class);
		new Interval(0.5, 0.25);
	}

	@Test
	public void equivalentIntervalsAreEqual() {
		Interval decimal = new Interval(0.25, 0.5);
		Interval decimal2 = new Interval(0.25, 0.5);
		assertThat(decimal, is(equalTo(decimal2)));
	}

	@Test
	public void canAddToInterval() {
		Interval newInterval = interval.add(1);
		assertThat(newInterval.getStart().intValue(), is(equalTo(1)));
	}

	@Test
	public void zeroTo() {
		Interval newInterval = Interval.zeroTo(2);
		assertThat(newInterval.getSize().doubleValue(), is(equalTo(2.0)));
	}

	@Test
	public void moduloOtherInterval() {
		Interval zeroToQuarter = new Interval(0, 0.25);
		Interval quarterToHalf = new Interval(0.25, 0.5);
		Interval halfToThreeQuarters = new Interval(0.5, 0.75);
		Interval threeQuartersToOne = new Interval(0.75, 1.0);
		Interval oneToOneAndAQuarter = zeroToQuarter.add(1);
		Interval oneAndAQuarterToOneAndAHalf = quarterToHalf.add(1);

		assertThat(zeroToQuarter.modulo(interval), is(equalTo(zeroToQuarter)));
		assertThat(quarterToHalf.modulo(interval), is(equalTo(quarterToHalf)));
		assertThat(halfToThreeQuarters.modulo(interval),
				is(equalTo(halfToThreeQuarters)));
		assertThat(threeQuartersToOne.modulo(interval),
				is(equalTo(threeQuartersToOne)));
		assertThat(oneToOneAndAQuarter.modulo(interval),
				is(equalTo(zeroToQuarter)));
		assertThat(oneAndAQuarterToOneAndAHalf.modulo(interval),
				is(equalTo(quarterToHalf)));

	}

	@Test
	public void negativeModuloOtherInterval() {
		Interval zeroToQuarter = new Interval(0.0, 0.25);
		zeroToQuarter = zeroToQuarter.add(-0.25);
		Interval threeQuartersToOne = new Interval(0.75, 1.0);

		assertThat(zeroToQuarter.modulo(interval),
				is(equalTo(threeQuartersToOne)));
	}

	@Test
	public void multiplyMod() {
		Interval quarterToHalf = new Interval(0.25, 0.5);
		Interval halfToThreeQuarters = new Interval(0.5, 0.75);

		assertThat(quarterToHalf.multiplyMod(-1, interval),
				is(equalTo(halfToThreeQuarters)));
	}

	@Test
	public void moduloOtherSmallerInterval() {
		Interval zeroToOneAndAHalf = new Interval(0, 1.5);

		thrown.expect(IllegalArgumentException.class);
		zeroToOneAndAHalf.modulo(interval);
	}

}
