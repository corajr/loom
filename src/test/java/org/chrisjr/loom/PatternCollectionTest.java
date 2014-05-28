package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PatternCollectionTest {
	private PatternCollection patterns;

	@Before
	public void setUp() throws Exception {
		patterns = new PatternCollection();
	}

	@After
	public void tearDown() throws Exception {
		patterns = null;
	}

	@Test
	public void getExternalMappings() {
		Loom loom = new Loom(null);
		Pattern pattern = new DiscretePattern(loom);
		Pattern pattern2 = new DiscretePattern(loom);

		pattern2.asMIDI("clap");

		patterns.add(pattern);
		patterns.add(pattern2);

		PatternCollection activePatterns = patterns
				.getPatternsWithExternalMappings();
		assertThat(activePatterns.size(), is(equalTo(1)));
	}

}
