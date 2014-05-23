package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.awt.Color;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PatternTest {
	private Loom loom;
	private Pattern pattern;

	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		loom = new Loom(null); // PApplet is not needed here
		pattern = new ContinuousPattern(loom, 0.6);
	}

	@After
	public void tearDown() throws Exception {
		pattern = null;
		loom = null;
	}

	@Test
	public void hasExternalMappings() {
		assertThat(pattern.hasExternalMappings(), is(equalTo(false)));

		pattern.asMIDI("clap");
		assertThat(pattern.hasExternalMappings(), is(equalTo(true)));
	}

	
	@Test
	public void asInt() {
		pattern.asInt(0, 100);
		assertThat(pattern.asInt(), is(equalTo(60)));
	}

	@Test
	public void asColor() {
		int black = Color.BLACK.getRGB();
		int white = Color.WHITE.getRGB();
		pattern.asColor(black, white);
		assertThat(pattern.asColor(), is(equalTo(black)));

		pattern.asColor(white, black);
		assertThat(pattern.asColor(), is(equalTo(white)));
	}

	@Test
	public void asColorBlended() {
		int black = Color.BLACK.getRGB();
		int white = Color.WHITE.getRGB();
		int gray60 = 0xff999999;
		pattern.asColorBlended(black, white);
		assertThat(pattern.asColorBlended(), is(equalTo(gray60)));
	}

	@Test
	public void asObject() {
		pattern.asObject(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		assertThat((Integer) pattern.asObject(), is(equalTo(6)));
	}

	@Test
	public void whenNoMappingIsSet() {
		thrown.expect(IllegalStateException.class);
		pattern.asColor();
	}
}
