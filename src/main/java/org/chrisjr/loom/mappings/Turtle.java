package org.chrisjr.loom.mappings;

import java.util.concurrent.*;

import processing.core.PApplet;
import processing.core.PVector;

public class Turtle extends CopyOnWriteArrayList<DrawCommand> {
	public static final PositionHeading DEFAULT_POSITION = new PositionHeading(
			new PVector(0.0f, 0.0f), 0.0f);

	PApplet parent;
	PositionHeading positionHeading;
	BlockingDeque<PositionHeading> posStack = new LinkedBlockingDeque<PositionHeading>();

	public Turtle(PApplet parent) {
		this(parent, DEFAULT_POSITION);
	}

	public Turtle(PApplet parent, PositionHeading positionHeading) {
		this.parent = parent;
		this.positionHeading = positionHeading;
	}

	public void add(TurtleDrawCommand command) {
		command.setTurtle(this);
		super.add(command);
	}

	public void draw() {
		for (DrawCommand command : this) {
			command.draw(parent);
		}
	}

	public synchronized PositionHeading getPositionHeading() {
		return positionHeading;
	}

	public synchronized void setPositionHeading(PositionHeading positionHeading) {
		this.positionHeading = positionHeading;
	}

	public void pushPositionHeading() {
		posStack.push(getPositionHeading());
	}

	public void popPositionHeading() {
		setPositionHeading(posStack.pop());
	}

	@Override
	public void clear() {
		setPositionHeading(DEFAULT_POSITION);
		super.clear();
	}
}
