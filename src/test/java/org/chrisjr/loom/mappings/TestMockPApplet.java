package org.chrisjr.loom.mappings;

import java.util.ArrayList;

import processing.core.PApplet;

public class TestMockPApplet extends PApplet {
	private static final long serialVersionUID = -7174699997244200887L;
	ArrayList<String> commands = new ArrayList<String>();

	@Override
	public void setup() {
		size(400, 400);

		// prevent thread from starving everything else
		noLoop();
	}

	@Override
	public void stroke(int rgb) {
		commands.add(String.format("stroke(%s);", Integer.toHexString(rgb)));
	}

	@Override
	public void fill(int rgb) {
		commands.add(String.format("fill(%s);", Integer.toHexString(rgb)));
	}

	@Override
	public void translate(float x, float y) {
		commands.add(String.format("translate(%1.0f, %1.0f);", x, y));
	}

	@Override
	public void rotate(float theta) {
		commands.add(String.format("rotate(%1.3f);", theta));
	}

	@Override
	public void pushMatrix() {
		commands.add("pushMatrix();");
	}

	@Override
	public void popMatrix() {
		commands.add("popMatrix();");
	}

	@Override
	public void line(float x1, float y1, float x2, float y2) {
		commands.add(String.format("line(%1.0f, %1.0f, %1.0f, %1.0f);", x1, y1,
				x2, y2));
	}

	@Override
	public void rect(float x, float y, float w, float h) {
		commands.add(String.format("rect(%1.0f, %1.0f, %1.0f, %1.0f);", x, y,
				w, h));
	}

	@Override
	public void ellipse(float x, float y, float w, float h) {
		commands.add(String.format("ellipse(%1.0f, %1.0f, %1.0f, %1.0f);", x,
				y, w, h));
	}
}