package org.chrisjr.loom.mappings;

import java.util.ArrayList;

import processing.core.PApplet;

public class Turtle extends ArrayList<DrawCommand> {
	PApplet parent;

	public Turtle(PApplet parent) {
		this.parent = parent;
	}

	public void draw() {
		for (DrawCommand command : this) {
			command.draw();
		}
	}
}
