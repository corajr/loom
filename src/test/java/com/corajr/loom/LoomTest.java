/**
 * 
 */
package com.corajr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.Loom;

public class LoomTest {
	private Loom loom;

	@Before
	public void setUp() throws Exception {
		loom = new Loom(null); // attachment to PApplet is not needed here
	}

	@After
	public void tearDown() throws Exception {
		loom = null;
	}

	@Test
	public void startsEmpty() {
		assertTrue(loom.patterns.size() == 0);
	}
	
	@Test
	public void canIntializeWithBpm() {
		loom = new Loom(null, 120);
		assertThat(loom.getPeriod(), is(equalTo(2000L)));
	}

}
