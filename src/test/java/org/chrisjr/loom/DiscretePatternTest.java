package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscretePatternTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private DiscretePattern pattern;
	
	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler); // attachment to PApplet is not needed here
		pattern = new DiscretePattern(loom);
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
	public void returnsValue() {
		assertThat(pattern.getValue(), is(equalTo(0.0)));
	}

	@Test
	public void canBeExtended() {
		pattern.extend("0101");
		for (Event e : pattern.events.values()) {
			System.out.println(e.toString());
		}
		assertThat(pattern.getValue(), is(equalTo(0.0)));
		scheduler.setElapsedMillis(251);
		assertThat(pattern.getValue(), is(equalTo(1.0)));
	}
}
