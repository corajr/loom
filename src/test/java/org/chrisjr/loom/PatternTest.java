package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PatternTest {
	private Loom loom;
	private Pattern pattern;

	@Before
	public void setUp() throws Exception {
		loom = new Loom(null); // PApplet is not needed here
		pattern = new ContinuousPattern(loom, 0.5);
	}

	@After
	public void tearDown() throws Exception {
		pattern = null;
		loom = null;
	}

	@Test
	public void asInt() {
		pattern.asInt(0, 100);
		assertThat(pattern.asInt(), is(equalTo(50)));
	}

	@Test
	public void asColor() {
		fail("Not yet implemented");
	}

	@Test
	public void asColorBlended() {
		fail("Not yet implemented");
	}

	@Test
	public void asObject() {
		fail("Not yet implemented");
	}

}
