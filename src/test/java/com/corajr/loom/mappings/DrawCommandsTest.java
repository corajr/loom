package com.corajr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.EventCollection;
import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.mappings.Draw;
import com.corajr.loom.mappings.TurtleDraw;
import com.corajr.loom.mappings.TurtleDrawCommand;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.time.RealTimeScheduler;
import com.corajr.loom.transforms.LsysRewriter;

import processing.core.PApplet;
import processing.core.PConstants;

public class DrawCommandsTest {
	private TestMockPApplet testApp;
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Before
	public void setUp() throws Exception {
		testApp = new TestMockPApplet();
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
	public void translate() {
		pattern.extend("0");
		pattern.asDrawCommand(Draw.translate(50, 50));
		loom.draw();
		assertThat(testApp.commands, hasItem("translate(50, 50);"));
	}

	@Test
	public void turtle() {
		pattern.extend("1234");
		pattern.loop();
		pattern.asTurtleDrawCommand(
				TurtleDraw.forward(100),
				TurtleDraw.c(TurtleDraw.turn(PConstants.HALF_PI),
						TurtleDraw.forward(100)),
				TurtleDraw.c(TurtleDraw.turn(PConstants.HALF_PI),
						TurtleDraw.forward(100)),
				TurtleDraw.c(TurtleDraw.turn(PConstants.HALF_PI),
						TurtleDraw.forward(100)));

		String[][] expectedCommands = new String[][] {
				new String[] { "line(0, 0, 0, -100);" },
				new String[] { "line(0, 0, 0, -100);",
						"line(0, -100, 100, -100);" },
				new String[] { "line(0, 0, 0, -100);",
						"line(0, -100, 100, -100);", "line(100, -100, 100, 0);" },
				new String[] { "line(0, 0, 0, -100);",
						"line(0, -100, 100, -100);",
						"line(100, -100, 100, 0);", "line(100, 0, -0, 0);" } };

		for (int i = 0; i < expectedCommands.length * 2; i++) {
			testApp.commands.clear();

			scheduler.setElapsedMillis(i * 250);
			loom.draw();

			assertThat(testApp.commands, contains(expectedCommands[i
					% expectedCommands.length]));
		}
	}

	@Test
	public void turtleWithDragon() {
		LsysRewriter lsys = new LsysRewriter("X->X+YF", "Y->FX-Y");
		EventCollection axiom = lsys.makeAxiom("FX");

		lsys.generations = 4;
		lsys.setCommand("F", TurtleDraw.forward(10));
		lsys.setCommand("+", TurtleDraw.turn(PConstants.HALF_PI));
		lsys.setCommand("-", TurtleDraw.turn(-PConstants.HALF_PI));

		TurtleDrawCommand[] commands = lsys.getTurtleDrawCommands();

		EventCollection events = lsys.apply(axiom);

		pattern = new Pattern(loom, events);
		pattern.asTurtleDrawCommand(commands);
		pattern.loop();

		pattern.addAllTurtleDrawCommands();
		pattern.draw();

		String[] result = new String[testApp.commands.size()];
		result = testApp.commands.toArray(result);

		assertThat(testApp.commands, contains(result));

		pattern.turtle.clear();

		for (int i = 1; i < 3; i++) {
			testApp.commands.clear();

			scheduler.setElapsedMillis(1000 * i - 2);
			loom.draw();

			assertThat(testApp.commands, contains(result));
		}

	}

	private String[] makeLsys() {
		LsysRewriter lsys = new LsysRewriter("X->F-[[X]+X]+F[+FX]-X", "F->FF");
		EventCollection axiom = lsys.makeAxiom("X");

		lsys.generations = 4;
		lsys.setCommand("F", TurtleDraw.forward(100));
		lsys.setCommand("+", TurtleDraw.turn(PApplet.radians(35)));
		lsys.setCommand("-", TurtleDraw.turn(PApplet.radians(-35)));
		lsys.setCommand("[", TurtleDraw.push());
		lsys.setCommand("]", TurtleDraw.pop());

		TurtleDrawCommand[] commands = lsys.getTurtleDrawCommands();

		EventCollection events = lsys.apply(axiom);

		pattern = new Pattern(loom, events);
		pattern.asTurtleDrawCommand(false, commands);

		pattern.addAllTurtleDrawCommands();
		pattern.draw();

		String[] result = new String[testApp.commands.size()];
		result = testApp.commands.toArray(result);

		pattern.turtle.clear();
		return result;
	}

	@Test
	public void turtleWithLsys() {
		String[] result = makeLsys();

		for (int i = 0; i < 10; i++) {
			testApp.commands.clear();
			scheduler.setElapsedMillis(i * 100);

			loom.draw();
			assertThat(testApp.commands.size(), is(greaterThan(0)));
		}
		testApp.commands.clear();

		scheduler.setElapsedMillis(1000);
		loom.draw();

		assertThat(testApp.commands.size(), is(result.length));
		assertThat(testApp.commands, contains(result));
	}

	@Test
	public void turtleWithLsysRealtime() throws InterruptedException {
		loom = new Loom(testApp, new RealTimeScheduler());
		String[] result = makeLsys();

		testApp.commands.clear();
		loom.play();
		Thread.sleep(1010);
		loom.stop();

		loom.draw();

		assertThat(testApp.commands.size(), is(result.length));
		assertThat(testApp.commands, contains(result));
	}
}
