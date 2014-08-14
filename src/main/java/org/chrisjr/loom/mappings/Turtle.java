package org.chrisjr.loom.mappings;

import java.util.concurrent.*;

import processing.core.PApplet;

public class Turtle extends ConcurrentLinkedQueue<TurtleDrawCommand> {
	private static final long serialVersionUID = -6712479056720391577L;
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
