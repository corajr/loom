package org.chrisjr.loom.mappings;

import processing.core.PApplet;

public abstract class TurtleDrawCommand extends DrawCommand {
	@Override
	public void draw(PApplet parent) {
		// no-op; must be passed the current turtle state to function
	}

	public TurtleState draw(PApplet parent, TurtleState state) {
		return state;
	}
}