package org.chrisjr.loom.mappings;

import java.util.concurrent.Callable;

import processing.core.PApplet;

public abstract class DrawCommand {
	PApplet parent;

	/**
	 * Does the actual drawing of the shape/line/etc. Color is handled
	 * separately, so this will simply do something like call `parent.rect(0, 0,
	 * 100, 100);`.
	 * 
	 */
	public abstract void draw();

	public void setParent(PApplet parent) {
		this.parent = parent;
	}
}
