package org.chrisjr.loom.mappings;

import java.util.concurrent.*;

import processing.core.PApplet;
import processing.core.PVector;

public class Turtle extends CopyOnWriteArrayList<TurtleDrawCommand> {
	PApplet parent;

	public Turtle(PApplet parent) {
		this.parent = parent;
	}

	public void draw() {
		TurtleState state = TurtleState.defaultState();
		for (TurtleDrawCommand command : this) {
			state = command.draw(parent, state);
		}
	}
}
