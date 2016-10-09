package com.corajr.loom.mappings;

import java.util.concurrent.*;

import processing.core.PApplet;

/**
 * Holds a list of {@link TurtleDrawCommand}s and draws them on command.
 * 
 * @author corajr
 */
public class Turtle extends ConcurrentLinkedQueue<TurtleDrawCommand> {
	private static final long serialVersionUID = -6712479056720391577L;
	PApplet parent;

	/**
	 * Create a new Turtle with a given PApplet.
	 * 
	 * @param parent
	 *            the PApplet that will be used for drawing
	 */
	public Turtle(PApplet parent) {
		this.parent = parent;
	}

	/**
	 * Creates a default {@link TurtleState} and threads it through each
	 * {@link TurtleDrawCommand}, applying the requisite transformations.
	 */
	public void draw() {
		TurtleState state = TurtleState.defaultState();
		for (TurtleDrawCommand command : this) {
			state = command.draw(parent, state);
		}
	}
}
