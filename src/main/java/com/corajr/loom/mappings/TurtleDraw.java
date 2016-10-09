package com.corajr.loom.mappings;

import java.util.Collection;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Classes for various drawing commands with a "turtle" that remembers the
 * drawing state. When added to a pattern, these commands will be executed by
 * that pattern's {@link Turtle}.
 * 
 * @author corajr
 */

public class TurtleDraw {
	/**
	 * Combines multiple draw commands into one operation.
	 */
	public static class Compound extends TurtleDrawCommand {
		TurtleDrawCommand[] commands;

		/**
		 * Create a new Compound command.
		 * 
		 * @param commands
		 *            the commands to be combined
		 */
		public Compound(TurtleDrawCommand... commands) {
			this.commands = commands;
		}

		/**
		 * Create a new Compound command.
		 * 
		 * @param commands
		 *            the commands to be combined
		 */
		public Compound(Collection<TurtleDrawCommand> commands) {
			this(commands.toArray(new TurtleDrawCommand[] {}));
		}

		@Override
		public void setParent(PApplet parent) {
			super.setParent(parent);
			for (TurtleDrawCommand command : commands) {
				command.setParent(parent);
			}
		}

		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			for (TurtleDrawCommand command : commands) {
				state = command.draw(parent, state);
			}
			return state;
		}
	}

	/**
	 * Move the turtle by a certain amount without drawing anything.
	 */
	public static class Move extends TurtleDrawCommand {
		float drawLength;

		/**
		 * Create a new Move.
		 * 
		 * @param drawLength
		 *            the amount to move forward
		 */
		public Move(float drawLength) {
			this.drawLength = drawLength;
		}

		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.move(drawLength);
		}
	}

	/**
	 * Draw forward by a specified amount and move the turtle to the new
	 * location.
	 */
	public static class Forward extends Move {
		/**
		 * Creates a new draw forward command.
		 * 
		 * @param drawLength
		 *            the amount to draw forward
		 */
		public Forward(float drawLength) {
			super(drawLength);
		}

		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			PVector pos = state.getPosition();

			TurtleState nextState = state.move(drawLength);
			PVector newPos = nextState.getPosition();

			parent.line(pos.x, pos.y, newPos.x, newPos.y);

			return nextState;
		}
	}

	/**
	 * Rotate the turtle by a specified amount.
	 */
	public static class Turn extends TurtleDrawCommand {
		float angle;

		/**
		 * Creates a new Turn command.
		 * 
		 * @param angle
		 *            the amount to turn in radians
		 */
		public Turn(float angle) {
			this.angle = angle;
		}

		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.turn(angle);
		}
	}

	/**
	 * Push the current position and heading onto the turtle's stack.
	 */
	public static class Push extends TurtleDrawCommand {
		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.pushPositionHeading();
		}
	}

	/**
	 * Pop a stored position and heading off of the turtle's stack.
	 */
	public static class Pop extends TurtleDrawCommand {
		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.popPositionHeading();
		}
	}

	/**
	 * Creates a draw forward command.
	 * 
	 * @param drawLength
	 *            the amount to draw forward
	 * @return a new forward
	 */
	public static Forward forward(float drawLength) {
		return new Forward(drawLength);
	}

	/**
	 * Creates a rotate command.
	 * 
	 * @param theta
	 *            the amount to rotate in radians
	 * @return a new Turn
	 */
	public static Turn turn(float theta) {
		return new Turn(theta);
	}

	/**
	 * Creates a new Push.
	 * 
	 * @return a new Push
	 */
	public static Push push() {
		return new Push();
	}

	/**
	 * Creates a new Pop.
	 * 
	 * @return a new Pop
	 */
	public static Pop pop() {
		return new Pop();
	}

	/**
	 * Shorthand to combine commands.
	 * 
	 * @param commands
	 *            the commands to be combined
	 * @return a new compound command
	 */
	public static TurtleDrawCommand c(TurtleDrawCommand... commands) {
		return new Compound(commands);
	}

}
