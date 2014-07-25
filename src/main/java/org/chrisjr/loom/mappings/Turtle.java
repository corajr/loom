package org.chrisjr.loom.mappings;

import java.util.concurrent.*;

import processing.core.PApplet;

public class Turtle extends CopyOnWriteArrayList<DrawCommand> {
	PApplet parent;

	public Turtle(PApplet parent) {
		this.parent = parent;
	}

	public void draw() {
		for (DrawCommand command : this) {
			command.draw(parent);
		}
	}
}
