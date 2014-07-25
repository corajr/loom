package org.chrisjr.loom.mappings;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import processing.core.PApplet;

public abstract class DrawCommand implements Callable<Void> {
	private PApplet parent;
	Turtle turtle = null;

	/**
	 * Does the actual drawing of the shape/line/etc. Color is handled
	 * separately, so this will simply do something like call `parent.rect(0, 0,
	 * 100, 100);`.
	 * 
	 */
	public abstract void draw(PApplet parent);

	public void draw() {
		draw(parent);
	}

	@Override
	public Void call() {
		if (turtle != null)
			turtle.add(this);
		else
			draw();
		return null;
	}

	public void setParent(PApplet parent) {
		this.parent = parent;
	}

	public void setTurtle(Turtle turtle) {
		this.turtle = turtle;
	}
}
