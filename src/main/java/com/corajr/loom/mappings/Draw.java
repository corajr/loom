package com.corajr.loom.mappings;

import java.util.*;

import processing.core.*;

/**
 * Classes for various drawing commands. When added to a pattern, these commands
 * will be attached to the current PApplet and will execute in its graphics
 * context.
 * 
 * @author corajr
 */
public class Draw {

	/**
	 * NOOP is useful for instructions that are to be ignored, i.e. constants in
	 * an L-system that do not draw anything.
	 */
	public static final Noop NOOP = new Noop();

	/**
	 * Do nothing. Additionally leaves the state of a Turtle unchanged.
	 * 
	 * @author corajr
	 */
	public static class Noop extends TurtleDrawCommand {
		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state;
		}
	}

	/**
	 * Combines several different DrawCommands into a single command.
	 * 
	 * @author corajr
	 */
	public static class Compound extends DrawCommand {
		DrawCommand[] commands;

		/**
		 * Create a new Compound command.
		 * 
		 * @param commands
		 *            the commands to be combined
		 */
		public Compound(DrawCommand... commands) {
			this.commands = commands;
		}

		/**
		 * Create a new Compound command.
		 * 
		 * @param commands
		 *            the commands to be combined
		 */
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

	/**
	 * Draws a line from (x1, y1) to (x2, y2).
	 * 
	 * @author corajr
	 */
	public static class Line extends DrawCommand {
		float x1, y1, x2, y2;

		/**
		 * Creates the Line command.
		 * 
		 * @param x1
		 *            x-coordinate of first point
		 * @param y1
		 *            y-coordinate of first point
		 * @param x2
		 *            x-coordinate of second point
		 * @param y2
		 *            y-coordinate of first point
		 */
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

	/**
	 * Draws a rectangle.
	 * 
	 * @author corajr
	 */
	public static class Rect extends DrawCommand {
		float x, y, w, h;

		/**
		 * Creates the rectangle command.
		 * 
		 * @param x
		 *            the x-coordinate of upper-left corner
		 * @param y
		 *            the y-coordinate of upper-left corner
		 * @param w
		 *            the width of the rectangle
		 * @param h
		 *            the height of the rectangle
		 */
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

	/**
	 * Draws an ellipse.
	 */
	public static class Ellipse extends DrawCommand {
		float x, y, w, h;

		/**
		 * Create the ellipse command.
		 * 
		 * @param x
		 *            the x-coordinate of the center
		 * @param y
		 *            the y-coordinate of the center
		 * @param w
		 *            the length of the horizontal axis
		 * @param h
		 *            the length of the vertical axis
		 */
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

	/**
	 * Translates the drawing context.
	 */
	public static class Translate extends DrawCommand {
		float x, y;

		/**
		 * Create the translate command.
		 * 
		 * @param x
		 *            the amount to translate horizontally in pixels
		 * @param y
		 *            the amount to translate vertically in pixels
		 */
		public Translate(float x, float y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void draw(PApplet parent) {
			parent.translate(x, y);
		}
	}

	/**
	 * Draw a line by a specified amount and translate to the end of that line.
	 * 
	 * @see TurtleDrawCommand#Forward
	 */
	public static class Forward extends DrawCommand {
		float drawLength;

		/**
		 * Create a new draw forward command.
		 * 
		 * @param drawLength
		 *            the amount to draw/move forward
		 */
		public Forward(float drawLength) {
			this.drawLength = drawLength;
		}

		@Override
		public void draw(PApplet parent) {
			parent.line(0, 0, 0, -drawLength);
			parent.translate(0, -drawLength);
		}
	}

	/**
	 * Rotate the drawing context by the specified amount.
	 */
	public static class Rotate extends DrawCommand {
		float theta;

		/**
		 * Create the rotate command.
		 * 
		 * @param theta
		 *            the amount to turn by in radians.
		 */
		public Rotate(float theta) {
			this.theta = theta;
		}

		@Override
		public void draw(PApplet parent) {
			parent.rotate(theta);
		}
	}

	/**
	 * Push the current transformation matrix of the draw context to the stack.
	 */
	public static class Push extends DrawCommand {
		@Override
		public void draw(PApplet parent) {
			parent.pushMatrix();
		}
	}

	/**
	 * Pop a transformation matrix of the draw context from the stack.
	 */
	public static class Pop extends DrawCommand {
		@Override
		public void draw(PApplet parent) {
			parent.popMatrix();
		}
	}

	/**
	 * Make a new Line.
	 * 
	 * @param x1
	 *            x-coordinate of first point
	 * @param y1
	 *            y-coordinate of first point
	 * @param x2
	 *            x-coordinate of second point
	 * @param y2
	 *            y-coordinate of first point
	 * @return a new Line
	 */
	public static Line line(float x1, float y1, float x2, float y2) {
		return new Line(x1, y1, x2, y2);
	}

	/**
	 * Make a new Rect.
	 * 
	 * @param x
	 *            the x-coordinate of upper-left corner
	 * @param y
	 *            the y-coordinate of upper-left corner
	 * @param w
	 *            the width of the rectangle
	 * @param h
	 *            the height of the rectangle
	 * @return a new Rect
	 */
	public static Rect rect(float x, float y, float w, float h) {
		return new Rect(x, y, w, h);
	}

	/**
	 * Make a new Ellipse.
	 * 
	 * @param x
	 *            the x-coordinate of the center
	 * @param y
	 *            the y-coordinate of the center
	 * @param w
	 *            the length of the horizontal axis
	 * @param h
	 *            the length of the vertical axis
	 * @return a new Ellipse
	 */
	public static Ellipse ellipse(float x, float y, float w, float h) {
		return new Ellipse(x, y, w, h);
	}

	/**
	 * Make a new Translate.
	 * 
	 * @param x
	 *            the amount to translate horizontally in pixels
	 * @param y
	 *            the amount to translate vertically in pixels
	 * @return a new Translate
	 */
	public static Translate translate(float x, float y) {
		return new Translate(x, y);
	}

	/**
	 * Make a new Forward.
	 * 
	 * @param drawLength
	 *            the amount to draw/move forward
	 * @return a new Forward
	 */
	public static Forward forward(float drawLength) {
		return new Forward(drawLength);
	}

	/**
	 * Make a new Rotate.
	 * 
	 * @param theta
	 *            the amount to rotate by in radians
	 * @return a new Rotate
	 */
	public static Rotate rotate(float theta) {
		return new Rotate(theta);
	}

	/**
	 * Make a new Push command.
	 * 
	 * @return a new Push
	 */
	public static Push push() {
		return new Push();
	}

	/**
	 * Make a new Pop command.
	 * 
	 * @return a new Pop
	 */
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
