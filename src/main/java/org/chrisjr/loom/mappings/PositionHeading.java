package org.chrisjr.loom.mappings;

import processing.core.PVector;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(angle);
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PositionHeading))
			return false;
		PositionHeading other = (PositionHeading) obj;
		if (Float.floatToIntBits(angle) != Float.floatToIntBits(other.angle))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}
}