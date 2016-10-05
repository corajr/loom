package com.corajr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.awt.Color;

import org.junit.Test;

import com.corajr.loom.mappings.ColorMapping;
import com.corajr.loom.mappings.IntMapping;
import com.corajr.loom.mappings.Mapping;
import com.corajr.loom.mappings.ObjectMapping;

public class MappingTest {

	@Test
	public void intMapping() {
		int max = 100;

		Mapping<Integer> m = new IntMapping(0, max);
		for (int i = 0; i < max; i++) {
			double value = (double) i / max;
			assertThat(m.call(value), is(equalTo(i)));
		}
	}

	@Test
	public void colorMapping() {
		int black = Color.BLACK.getRGB();
		int white = Color.WHITE.getRGB();
		int gray60 = 0xff999999;

		Mapping<Integer> m = new ColorMapping(black, white);
		assertThat(m.call(0.60), is(equalTo(gray60)));
	}

	@Test
	public void objectMapping() {
		Mapping<Integer> m = new ObjectMapping<Integer>(1, 2, 3, 4, 5, 6, 7, 8,
				9, 10);
		assertThat(m.call(0.60), is(equalTo(6)));
	}
}
