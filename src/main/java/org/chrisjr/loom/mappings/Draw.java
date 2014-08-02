package org.chrisjr.loom.mappings;

import java.util.*;

import processing.core.*;

/**
 * Classes for various drawing commands. When added to a pattern, these commands
 * will be attached to the current PApplet and will execute in its graphics
 * context.
 * 
 * @author chrisjr
 */
public class Draw {

	/**
	 * NOOP is useful for instructions that are to be ignored, i.e. constants in
	 * an L-system that do not draw anything.
	 */
	public static final Noop NOOP = new Noop();

	public static class Noop extends TurtleDrawCommand {
		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state;
		}
	}

	public static class Compound extends DrawCommand {
		DrawCommand[] commands;

		public Compound(DrawCommand... commands) {
			this.commands = commands;
		}

		public Compound(Collection<DrawCommand> commands) {
			this(commands.toArray(new DrawCommand[] {}));
		}

		@Override
		public void setParent(PApplet parent) {
			super.setParent(parent);
			for (DrawCommand command : commands) {
				command.setParent(parent);
			}
		}

		@Override
		public void draw(PApplet parent) {
			for (DrawCommand command : commands) {
				command.draw(parent);
			}
		}
	}

	public static class Line extends DrawCommand {
		float x1, y1, x2, y2;

		public Line(float x1, float y1, float x2, float y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public void draw(PApplet parent) {
			parent.line(x1, y1, x2, y2);
		}
	}

	public static class Rect extends DrawCommand {
		float x, y, w, h;

		public Rect(float x, float y, float w, float h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		@Override
		public void draw(PApplet parent) {
			parent.rect(x, y, w, h);
		}
	}

	public static class Ellipse extends DrawCommand {
		float x, y, w, h;

		public Ellipse(float x, float y, float w, float h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		@Override
		public void draw(PApplet parent) {
			parent.ellipse(x, y, w, h);
		}
	}

	public static class Translate extends DrawCommand {
		float x, y;

		public Translate(float x, float y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void draw(PApplet parent) {
			parent.translate(x, y);
		}
	}

	public static class Forward extends DrawCommand {
		float drawLength;

		public Forward(float drawLength) {
			this.drawLength = drawLength;
		}

		@Override
		public void draw(PApplet parent) {
			parent.line(0, 0, 0, -drawLength);
			parent.translate(0, -drawLength);
		}
	}

	public static class Rotate extends DrawCommand {
		float theta;

		public Rotate(float theta) {
			this.theta = theta;
		}

		@Override
		public void draw(PApplet parent) {
			parent.rotate(theta);
		}
	}

	public static class Push extends DrawCommand {
		@Override
		public void draw(PApplet parent) {
			parent.pushMatrix();
		}
	}

	public static class Pop extends DrawCommand {
		@Override
		public void draw(PApplet parent) {
			parent.popMatrix();
		}
	}

	public static Line line(float x1, float y1, float x2, float y2) {
		return new Line(x1, y1, x2, y2);
	}

	public static Rect rect(float x, float y, float w, float h) {
		return new Rect(x, y, w, h);
	}

	public static Ellipse ellipse(float x, float y, float w, float h) {
		return new Ellipse(x, y, w, h);
	}

	public static Translate translate(float x, float y) {
		return new Translate(x, y);
	}

	public static Forward forward(float drawLength) {
		return new Forward(drawLength);
	}

	public static Rotate rotate(float theta) {
		return new Rotate(theta);
	}

	public static Push push() {
		return new Push();
	}

	public static Pop pop() {
		return new Pop();
	}

	/**
	 * Shorthand to combine multiple DrawCommands into a single command.
	 * 
	 * @param commands
	 *            the DrawCommands to be combined
	 * @return a compound command that calls `draw()` on all members when
	 *         called.
	 */
	public static Compound c(DrawCommand... commands) {
		return new Compound(commands);
	}
}
