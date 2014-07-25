package org.chrisjr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.chrisjr.loom.Loom;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import processing.core.PApplet;
import processing.core.PConstants;

public class DrawCommandsTest {
	private MockPApplet testApp;
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	public class MockPApplet extends PApplet {
		ArrayList<String> commands = new ArrayList<String>();

		@Override
		public void setup() {
			size(400, 400);

			// prevent thread from starving everything else
			noLoop();
		}

		@Override
		public void stroke(int rgb) {
			commands.add(String.format("stroke(%s);", Integer.toHexString(rgb)));
		}

		@Override
		public void fill(int rgb) {
			commands.add(String.format("fill(%s);", Integer.toHexString(rgb)));
		}

		@Override
		public void translate(float x, float y) {
			commands.add(String.format("translate(%1.0f, %1.0f);", x, y));
		}

		@Override
		public void rotate(float theta) {
			commands.add(String.format("rotate(%1.3f);", theta));
		}

		@Override
		public void pushMatrix() {
			commands.add("pushMatrix();");
		}

		@Override
		public void popMatrix() {
			commands.add("popMatrix();");
		}

		@Override
		public void line(float x1, float y1, float x2, float y2) {
			commands.add(String.format("line(%1.0f, %1.0f, %1.0f, %1.0f);", x1,
					y1, x2, y2));
		}

		@Override
		public void rect(float x, float y, float w, float h) {
			commands.add(String.format("rect(%1.0f, %1.0f, %1.0f, %1.0f);", x,
					y, w, h));
		}

		@Override
		public void ellipse(float x, float y, float w, float h) {
			commands.add(String.format("ellipse(%1.0f, %1.0f, %1.0f, %1.0f);",
					x, y, w, h));
		}
	}

	@Before
	public void setUp() throws Exception {
		testApp = new MockPApplet();
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(testApp, scheduler);
		pattern = new Pattern(loom);

		testApp.init();

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		testApp.dispose();
		testApp = null;
	}

	@Test
	public void line() {
		pattern.extend("0");
		pattern.asDrawCommand(Draw.line(0, 0, 100, 100));

		loom.draw();

		assertThat(testApp.commands, hasItem("line(0, 0, 100, 100);"));
	}

	@Test
	public void rect() {
		pattern.extend("0");
		pattern.asDrawCommand(Draw.rect(0, 0, 100, 100));

		loom.draw();

		assertThat(testApp.commands, hasItem("rect(0, 0, 100, 100);"));
	}

	@Test
	public void ellipse() {
		pattern.extend("0");
		pattern.asDrawCommand(Draw.ellipse(0, 0, 100, 100));

		loom.draw();

		assertThat(testApp.commands, hasItem("ellipse(0, 0, 100, 100);"));
	}

	@Test
	public void turtle() {
		pattern.extend("0123");
		pattern.asDrawCommand(Draw.forward(100),
				Draw.c(Draw.rotate(PConstants.HALF_PI), Draw.forward(100)),
				Draw.c(Draw.rotate(PConstants.HALF_PI), Draw.forward(100)),
				Draw.c(Draw.rotate(PConstants.HALF_PI), Draw.forward(100)));

		int[] sizes = new int[] { 4, 9, 14, 19 };
		for (int i = 0; i < sizes.length; i++) {
			scheduler.setElapsedMillis(i * 250);
			loom.draw();
			assertThat(testApp.commands, hasSize(sizes[i]));
		}
	}
}
