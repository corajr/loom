package org.chrisjr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import processing.core.PConstants;
import processing.core.PVector;

public class TurtleTest {
	static final double EPSILON = 1E-5;

	@Test
	public void positionHeadingTurn() {
		PositionHeading posHead = new PositionHeading();
		PositionHeading posHeadTurned = posHead.turn(PConstants.HALF_PI);

		assertThat(posHead.getAngle(), is(equalTo(0.0f)));
		assertThat(posHeadTurned.getAngle(), is(equalTo(PConstants.HALF_PI)));
	}

	@Test
	public void positionHeadingMove() {
		PositionHeading posHead = new PositionHeading();
		PositionHeading posHeadTurned = posHead.turn(PConstants.HALF_PI);
		PositionHeading posHeadMoved = posHeadTurned.move(100.0f);

		PVector dest = new PVector(100.0f, 0.0f);
		assertThat((double) posHeadMoved.getPosition().dist(dest),
				is(closeTo(0.0, EPSILON)));
		assertThat(posHeadMoved.getAngle(), is(equalTo(PConstants.HALF_PI)));
	}

	@Test
	public void addCommands() {
		MockPApplet testApp = new MockPApplet();
		testApp.init();

		Turtle turtle = new Turtle(testApp);

		turtle.add(TurtleDraw.forward(100));
		turtle.add(TurtleDraw.c(TurtleDraw.turn(PConstants.HALF_PI),
				TurtleDraw.forward(100)));
		turtle.draw();

		System.out.println(testApp.commands);

		testApp.dispose();
	}
}
