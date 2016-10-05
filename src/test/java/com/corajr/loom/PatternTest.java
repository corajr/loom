package com.corajr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.awt.Color;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.mappings.TestMockPApplet;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.util.StatefulNoop;

public class PatternTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);

		pattern = new Pattern(loom, 0.6);
		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		pattern = null;
		loom = null;
	}

	@Test
	public void hasExternalMappings() {
		assertThat(pattern.hasActiveMappings(), is(equalTo(false)));

		pattern.asCallable(new StatefulNoop(null));
		assertThat(pattern.hasActiveMappings(), is(equalTo(true)));
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
		int gray60 = 0xff999999;
		pattern.asColor(black, white);
		assertThat(pattern.asColor(), is(equalTo(gray60)));
	}

	@Test
	public void asColorDiscrete() {
		pattern = Pattern.fromInts(loom, 0, 1, 0, 1);

		int black = Color.BLACK.getRGB();
		int white = Color.WHITE.getRGB();
		pattern.asColor(black, white);

		assertThat(pattern.asColor(), is(equalTo(black)));

		scheduler.setElapsedMillis(251);
		assertThat(pattern.asColor(), is(equalTo(white)));
	}

	@Test
	public void asObject() {
		pattern.asObject(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		assertThat((Integer) pattern.asObject(), is(equalTo(6)));
	}

	@Test
	public void rect() {
		TestMockPApplet testApp = new TestMockPApplet();
		loom = new Loom(testApp, scheduler);
		pattern = Pattern.fromString(loom, "01");

		pattern.asColor(0xFF000000, 0xFFFFFFFF);
		pattern.loop();

		pattern.rect(0, 0, 3, 1);

		String[] expected1 = new String[] { "stroke(ff000000);",
				"line(0, 0, 0, 1);", "stroke(ffffffff);", "line(1, 0, 1, 1);",
				"stroke(ffffffff);", "line(2, 0, 2, 1);" };

		String[] expected2 = new String[] { "stroke(ffffffff);",
				"line(0, 0, 0, 1);", "stroke(ff000000);", "line(1, 0, 1, 1);",
				"stroke(ffffffff);", "line(2, 0, 2, 1);" };
		assertThat(testApp.commands, hasItems(expected1));
		testApp.commands.clear();
		scheduler.setElapsedMillis(500);
		pattern.rect(0, 0, 3, 1);
		assertThat(testApp.commands, hasItems(expected2));
	}

	@Test
	public void cannotAddSelfToSelf() {
		thrown.expect(IllegalArgumentException.class);
		pattern.addChild(pattern);
	}

	@Test
	public void whenNoMappingIsSet() {
		thrown.expect(IllegalStateException.class);
		pattern.asColor();
	}

	@Test
	public void noMappingsWhenPatternLacksEventsAndFunction() {
		thrown.expect(IllegalStateException.class);
		pattern = new Pattern(loom);
		pattern.asColor(0);
	}

	@Test
	public void cannotAddChildToConcretePattern() {
		Pattern other = new Pattern(null);
		thrown.expect(IllegalStateException.class);
		pattern.getConcretePattern().addChild(other);
	}
}
