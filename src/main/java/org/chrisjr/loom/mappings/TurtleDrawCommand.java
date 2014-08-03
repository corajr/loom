package org.chrisjr.loom.mappings;

import java.util.concurrent.Callable;

import processing.core.PApplet;

public abstract class TurtleDrawCommand extends DrawCommand implements
		Callable<Void> {
	private Turtle turtle;

	public void setTurtle(Turtle turtle) {
		this.turtle = turtle;
	}

	@Override
	public void draw(PApplet parent) {
		// no-op; must be passed the current turtle state to function
	}

	public TurtleState draw(PApplet parent, TurtleState state) {
		return state;
	}

	@Override
	public Void call() {
		turtle.add(this);
		return null;
	}
}