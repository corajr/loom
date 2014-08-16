package org.chrisjr.loom.mappings;

import java.util.Collection;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Classes for various drawing commands with a "turtle" that remembers the
 * drawing state. When added to a pattern, these commands will be executed by
 * that pattern's {@link Turtle}.
 * 
 * @author chrisjr
 */

public class TurtleDraw {
	public static class Compound extends TurtleDrawCommand {
		TurtleDrawCommand[] commands;

		public Compound(TurtleDrawCommand... commands) {
			this.commands = commands;
		}

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

	public static class Move extends TurtleDrawCommand {
		float drawLength;

		public Move(float drawLength) {
			this.drawLength = drawLength;
		}

		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.move(drawLength);
		}
	}

	public static class Forward extends Move {
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

	public static class Turn extends TurtleDrawCommand {
		float angle;

		public Turn(float angle) {
			this.angle = angle;
		}

		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.turn(angle);
		}
	}

	public static class Push extends TurtleDrawCommand {
		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.pushPositionHeading();
		}
	}

	public static class Pop extends TurtleDrawCommand {
		@Override
		public TurtleState draw(PApplet parent, TurtleState state) {
			return state.popPositionHeading();
		}
	}

	public static Forward forward(float drawLength) {
		return new Forward(drawLength);
	}

	public static Turn turn(float theta) {
		return new Turn(theta);
	}

	public static Push push() {
		return new Push();
	}

	public static Pop pop() {
		return new Pop();
	}

	public static TurtleDrawCommand c(TurtleDrawCommand... commands) {
		return new Compound(commands);
	}

}
