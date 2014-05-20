package org.chrisjr.loom;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PatternTest {
	private Loom loom;
	private Pattern pattern;
	
	@Before
	public void setUp() throws Exception {
		loom = new Loom(null); // attachment to PApplet is not needed here
		pattern = new Pattern(loom);
	}

	@After
	public void tearDown() throws Exception {
		loom = null;
		pattern = null;
	}

	@Test
	public void testAddedToLoom() {
		assertTrue(loom.patterns.contains(pattern));
	}	

	@Test
	public void testCanBeExtended() {
		String testStr = "101010";
		pattern.extend(testStr);
		assertTrue(pattern.getPatternString().equals(testStr));
	}
}
