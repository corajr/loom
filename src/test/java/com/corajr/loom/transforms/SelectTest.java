package com.corajr.loom.transforms;

import static org.junit.Assert.*;
import static com.corajr.loom.LEvent.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.*;
import com.corajr.loom.time.NonRealTimeScheduler;

public class SelectTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
	}

	private void expectAt(Pattern pat, int... timesAndValues) {
		for (int i = 0; i < timesAndValues.length / 2; i++) {
			int time = timesAndValues[i * 2];
			int value = timesAndValues[i * 2 + 1];
			scheduler.setElapsedMillis(time);
			assertThat(pat.asInt(), is(equalTo(value)));
		}
	}

	@Test
	public void selectOnce() {
		pattern.extend(seq(evt(1, 0.5), evt(1, 1.0))).loop();

		Pattern one = Pattern.fromString(null, "0123").asInt(0, 3);
		Pattern two = Pattern.fromString(null, "9876543210").asInt(0, 9);

		Pattern pat = pattern.selectFrom(one, two).loop();

		expectAt(pat, 0, 0, 250, 1, 500, 2, 750, 3, 1000, 9, 1100, 8, 1200, 7,
				1300, 6, 1400, 5, 1500, 4, 1600, 3, 1700, 2, 1800, 1, 1900, 0,
				2000, 0, 2250, 1, 2500, 2, 2750, 3);
	}
}
