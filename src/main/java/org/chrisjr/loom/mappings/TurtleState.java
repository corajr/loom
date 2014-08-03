package org.chrisjr.loom.mappings;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import processing.core.PVector;

public class TurtleState {
	public static final PositionHeading DEFAULT_POSITION = new PositionHeading(
			new PVector(0.0f, 0.0f), 0.0f);

	final private PositionHeading positionHeading;
	final private BlockingDeque<PositionHeading> posStack;

	public TurtleState(PositionHeading positionHeading,
			BlockingDeque<PositionHeading> posStack) {
		this.positionHeading = positionHeading;
		this.posStack = posStack;
	}

	public PVector getPosition() {
		return positionHeading.getPosition();
	}

	public float getAngle() {
		return positionHeading.getAngle();
	}

	public TurtleState move(float dist) {
		return new TurtleState(positionHeading.move(dist), posStack);
	}

	public TurtleState turn(float angle) {
		return new TurtleState(positionHeading.turn(angle), posStack);
	}

	public TurtleState pushPositionHeading() {
		posStack.push(positionHeading);
		return this;
	}

	public TurtleState popPositionHeading() {
		PositionHeading posHead = posStack.poll();
		if (posHead == null) {
			throw new IllegalStateException(
					"Tried to pop position from an empty stack.");
		}
		return new TurtleState(posHead, posStack);
	}

	public static TurtleState defaultState() {
		return new TurtleState(DEFAULT_POSITION,
				new LinkedBlockingDeque<PositionHeading>());
	}
}