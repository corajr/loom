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
		public void rect(float x, float y, float w, float h) {
			commands.add(String.format("rect(%1.0f, %1.0f, %1.0f, %1.0f);", x,
					y, w, h));
		}

		@Override
		public void line(float x1, float y1, float x2, float y2) {
			commands.add(String.format("line(%1.0f, %1.0f, %1.0f, %1.0f);", x1,
					y1, x2, y2));
		}
	}

	@Before
	public void setUp() throws Exception {
		testApp = new MockPApplet();
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(testApp, scheduler);

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
		pattern = new Pattern(loom);
		pattern.extend("0");
		pattern.asDrawCommand(new DrawCommand() {
			@Override
			public void draw() {
				parent.line(0, 0, 100, 100);
			}
		});

		loom.draw();

		assertThat(testApp.commands, contains("line(0, 0, 100, 100);"));
	}
}
