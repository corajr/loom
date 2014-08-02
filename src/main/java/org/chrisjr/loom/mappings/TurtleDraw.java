package org.chrisjr.loom.mappings;

import java.util.Collection;

import org.chrisjr.loom.mappings.PositionHeading;

import processing.core.PApplet;
import processing.core.PVector;

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
		public void setTurtle(Turtle turtle) {
			super.setTurtle(turtle);
			for (TurtleDrawCommand command : commands) {
				command.setTurtle(turtle);
			}
		}

		@Override
		public void draw(PApplet parent) {
			for (TurtleDrawCommand command : commands) {
				command.draw(parent);
			}
		}
	}

	public static class Move extends TurtleDrawCommand {
		float drawLength;

		public Move(float drawLength) {
			this.drawLength = drawLength;
		}

		@Override
		public PositionHeading updatedPositionHeading(PositionHeading current) {
			return current.move(drawLength);
		}
	}

	public static class Forward extends Move {
		public Forward(float drawLength) {
			super(drawLength);
		}

		@Override
		public void draw(PApplet parent) {
			PositionHeading posHead = turtle.getPositionHeading();

			PVector pos = posHead.getPosition();

			PositionHeading newPosHead = updatedPositionHeading(posHead);
			PVector newPos = newPosHead.getPosition();

			parent.line(pos.x, pos.y, newPos.x, newPos.y);

			turtle.setPositionHeading(newPosHead);
		}
	}

	public static class Turn extends TurtleDrawCommand {
		float angle;

		public Turn(float angle) {
			this.angle = angle;
		}

		@Override
		public PositionHeading updatedPositionHeading(PositionHeading current) {
			return current.turn(angle);
		}
	}

	public static class Push extends TurtleDrawCommand {
		@Override
		public void draw(PApplet parent) {
			turtle.pushPositionHeading();
		}
	}

	public static class Pop extends TurtleDrawCommand {
		@Override
		public void draw(PApplet parent) {
			turtle.popPositionHeading();
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
