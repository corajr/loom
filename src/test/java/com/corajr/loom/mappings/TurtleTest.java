package com.corajr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.corajr.loom.mappings.PositionHeading;
import com.corajr.loom.mappings.Turtle;
import com.corajr.loom.mappings.TurtleDraw;
import com.corajr.loom.mappings.TurtleState;

import processing.core.PConstants;
import processing.core.PVector;

public class TurtleTest {
	static final double EPSILON = 1E-4;
	static final PVector START = TurtleState.DEFAULT_POSITION.getPosition();

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
	public void turtleStatePushPop() {
		// trace out a 3:4:5 triangle, with turns
		TurtleState state = TurtleState.defaultState();

		assertThat((double) state.getPosition().dist(START),
				is(closeTo(0.0, EPSILON)));

		state = state.move(100).turn(PConstants.PI);
		assertThat((double) state.getPosition().dist(START),
				is(closeTo(100.0, EPSILON)));

		state = state.pushPositionHeading();

		state = state.turn(PConstants.PI).move(200);
		assertThat((double) state.getPosition().dist(START),
				is(closeTo(300.0, EPSILON)));

		state = state.pushPositionHeading();

		state = state.turn(PConstants.HALF_PI).move(400);
		assertThat((double) state.getPosition().dist(START),
				is(closeTo(500.0, EPSILON)));

		state = state.popPositionHeading();

		assertThat((double) state.getPosition().dist(START),
				is(closeTo(300.0, EPSILON)));

		state = state.popPositionHeading();
		assertThat((double) state.getPosition().dist(START),
				is(closeTo(100.0, EPSILON)));

		state = state.move(100);
		assertThat((double) state.getPosition().dist(START),
				is(closeTo(0.0, EPSILON)));
	}

	@Test
	public void addCommands() {
		TestMockPApplet testApp = new TestMockPApplet();
		testApp.init();

		Turtle turtle = new Turtle(testApp);

		turtle.add(TurtleDraw.forward(100));
		turtle.add(TurtleDraw.c(TurtleDraw.turn(PConstants.HALF_PI),
				TurtleDraw.forward(100)));
		turtle.draw();

		assertThat(testApp.commands, hasSize(2));
		assertThat(testApp.commands.get(0), is(equalTo("line(0, 0, 0, -100);")));
		assertThat(testApp.commands.get(1),
				is(equalTo("line(0, -100, 100, -100);")));

		testApp.dispose();
	}
}
