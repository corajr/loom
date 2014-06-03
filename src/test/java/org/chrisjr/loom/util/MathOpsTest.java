package org.chrisjr.loom.util;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class MathOpsTest {
	
	double epsilon = 1e-5;

	@Test
	public void testModInterval() {
		assertThat(MathOps.modInterval(-1.0), is(equalTo(0.0)));
		assertThat(MathOps.modInterval(-0.9), is(closeTo(0.1, epsilon)));

		assertThat(MathOps.modInterval(0.0), is(equalTo(0.0)));
		assertThat(MathOps.modInterval(0.5), is(equalTo(0.5)));
		assertThat(MathOps.modInterval(1.0), is(equalTo(1.0)));
		assertThat(MathOps.modInterval(1.5), is(equalTo(0.5)));
		assertThat(MathOps.modInterval(2.0), is(equalTo(1.0)));
	}

	@Test
	public void testModIntervalSpecifyingInterval() {
		assertThat(MathOps.modInterval(-1.0, -2.0, -1.0), is(equalTo(-1.0)));
	}

}
