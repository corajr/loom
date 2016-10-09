package com.corajr.loom.mappings;

import processing.core.PVector;

/**
 * Holds the current position and direction information from a Turtle, and
 * allows it to be manipulated in various ways.
 * 
 * @author corajr
 * @see Turtle
 */
public final class PositionHeading {
	final private PVector position;
	final private float angle;

	public PositionHeading() {
		this(new PVector(0, 0), 0.0f);
	}

	public PositionHeading(PVector position, float angle) {
		this.position = position;
		this.angle = angle;
	}

	public PVector getPosition() {
		return position.get();
	}

	public float getAngle() {
		return angle;
	}

	public PositionHeading move(float dist) {
		PVector vec = new PVector(0, -dist);
		vec.rotate(angle);
		return add(vec);
	}

	public PositionHeading add(PVector vec) {
		return new PositionHeading(PVector.add(position, vec), angle);
	}

	public PositionHeading turn(float angle) {
		return new PositionHeading(position, this.angle + angle);
	}

	@Override
	public String toString() {
		return String.format("(%s, %s) rad=%s", position.x, position.y, angle);
	}
}