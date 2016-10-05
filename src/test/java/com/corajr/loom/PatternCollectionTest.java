package com.corajr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.PatternCollection;

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
		Pattern pattern = new Pattern(loom);
		Pattern pattern2 = new Pattern(loom);
		
		pattern.extend("1111");
		pattern2.extend("1111");
		pattern.asMidiMessage(pattern2);
		
		patterns.add(pattern);
		patterns.add(pattern2);

		PatternCollection activePatterns = patterns
				.getPatternsWithActiveMappings();
		assertThat(activePatterns.size(), is(equalTo(1)));
	}

}
