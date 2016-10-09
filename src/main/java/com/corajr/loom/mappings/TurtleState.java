package com.corajr.loom.mappings;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import processing.core.PVector;

/**
 * Stores both the current {@link PositionHeading} and a stack of previous
 * PositionHeadings for a {@link Turtle}.
 * 
 * @author corajr
 */
public class TurtleState {
	public static final PositionHeading DEFAULT_POSITION = new PositionHeading(
			new PVector(0.0f, 0.0f), 0.0f);

	final private PositionHeading positionHeading;
	final private BlockingDeque<PositionHeading> posStack;

	/**
	 * Creates a new TurtleState (immutable).
	 * 
	 * @param positionHeading
	 *            the position and heading to start from
	 * @param posStack
	 *            a stack of positions/headings
	 */
	public TurtleState(PositionHeading positionHeading,
			BlockingDeque<PositionHeading> posStack) {
		this.positionHeading = positionHeading;
		this.posStack = posStack;
	}

	/**
	 * Get the current position of the turtle as a PVector.
	 * 
	 * @return the position
	 */
	public PVector getPosition() {
		return positionHeading.getPosition();
	}

	/**
	 * Get the current angle of the turtle in radians.
	 * 
	 * @return the angle in radians
	 */
	public float getAngle() {
		return positionHeading.getAngle();
	}

	/**
	 * Moves the turtle along the current heading by a specified distance.
	 * 
	 * @param dist
	 *            the distance to move
	 * @return the updated state
	 */
	public TurtleState move(float dist) {
		return new TurtleState(positionHeading.move(dist), posStack);
	}

	/**
	 * Turns the turtle by the specified angle in radians.
	 * 
	 * @param angle
	 *            the angle to turn by
	 * @return the updated state
	 */
	public TurtleState turn(float angle) {
		return new TurtleState(positionHeading.turn(angle), posStack);
	}

	/**
	 * Push the current PositionHeading onto the stack.
	 * 
	 * @return the updated state
	 */
	public TurtleState pushPositionHeading() {
		posStack.push(positionHeading);
		return this;
	}

	/**
	 * Pop a PositionHeading off the top of the stack.
	 * 
	 * @return the updated state
	 */
	public TurtleState popPositionHeading() {
		PositionHeading posHead = posStack.poll();
		if (posHead == null) {
			throw new IllegalStateException(
					"Tried to pop position from an empty stack.");
		}
		return new TurtleState(posHead, posStack);
	}

	/**
	 * Returns a TurtleState which starts at (0,0), pointing to 0.0 radians, and
	 * has an empty PositionHeading stack.
	 * 
	 * @return a default turtle state
	 */
	public static TurtleState defaultState() {
		return new TurtleState(DEFAULT_POSITION,
				new LinkedBlockingDeque<PositionHeading>());
	}
}