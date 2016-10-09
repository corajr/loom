package com.corajr.loom.mappings;

import processing.core.PApplet;

/**
 * Describes a drawing operation but does not execute it right away.
 * 
 * @author corajr
 */
public abstract class DrawCommand {
	private PApplet parent;

	/**
	 * Does the actual drawing of the shape/line/etc. Color is handled
	 * separately, so this will simply do something like call `parent.rect(0, 0,
	 * 100, 100);`.
	 * 
	 * @param parent
	 *            the PApplet to draw to
	 */
	public abstract void draw(PApplet parent);

	public void draw() {
		draw(parent);
	}

	public void setParent(PApplet parent) {
		this.parent = parent;
	}
}
