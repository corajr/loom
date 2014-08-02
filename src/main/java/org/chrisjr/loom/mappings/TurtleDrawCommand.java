package org.chrisjr.loom.mappings;

import processing.core.PApplet;

public abstract class TurtleDrawCommand extends DrawCommand {
	protected Turtle turtle;

	public void setTurtle(Turtle turtle) {
		this.turtle = turtle;
	}

	public PositionHeading updatedPositionHeading() {
		return updatedPositionHeading(turtle.getPositionHeading());
	}

	public PositionHeading updatedPositionHeading(PositionHeading current) {
		return current;
	}

	@Override
	public void draw(PApplet parent) {
	}
}