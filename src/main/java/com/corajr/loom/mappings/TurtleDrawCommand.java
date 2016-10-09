package com.corajr.loom.mappings;

import java.util.concurrent.Callable;

import processing.core.PApplet;

/**
 * Defines the basis for a {@link Turtle}'s drawing commands. The
 * <code>call</code> method serves to add this command to the turtle's list, and
 * allows these commands to be called by the scheduler through a common
 * interface.
 * 
 * @author corajr
 */
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

	/**
	 * Draws the current command and returns an updated turtle state.
	 * 
	 * @param parent
	 *            the PApplet to draw to
	 * @param state
	 *            the current state of the turtle (position/heading and stack)
	 * @return the updated state
	 */
	public TurtleState draw(PApplet parent, TurtleState state) {
		return state;
	}

	@Override
	public Void call() {
		turtle.add(this);
		return null;
	}
}