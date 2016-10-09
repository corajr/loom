/**
 * Lsystem illustrates how a pattern can be recursively rewritten.
 *
 * A simple pattern can be rewritten using a handful of transformation rules,
 * then played out over time.
 */

import com.corajr.loom.*;
import com.corajr.loom.transforms.*;
import com.corajr.loom.mappings.*;

Loom loom;
Pattern pattern;

void setup() {
  size(400, 400);
  noSmooth();

  loom = new Loom(this);

  LsysRewriter lsys = new LsysRewriter("X->F-[[X]+X]+F[+FX]-X", "F->FF");
  EventCollection axiom = lsys.makeAxiom("X");

  lsys.generations = 4;
  lsys.setCommand("F", TurtleDraw.forward(5));
  lsys.setCommand("+", TurtleDraw.turn(radians(25)));
  lsys.setCommand("-", TurtleDraw.turn(radians(-25)));
  lsys.setCommand("[", TurtleDraw.push());
  lsys.setCommand("]", TurtleDraw.pop());

  EventCollection events = lsys.apply(axiom);
  pattern = new Pattern(loom, events);
  pattern.speed(0.2).loop();
  pattern.asTurtleDrawCommand(lsys.getTurtleDrawCommands());

  loom.play();
}

void draw() {
  background(255);
  translate(width/2, height);
  loom.draw();
}