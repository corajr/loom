/**
 * 
 */
package org.chrisjr.loom;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
	public void testStartsEmpty() {
		assertTrue(loom.patterns.size() == 0);
	}

}
