import org.chrisjr.loom.*;
import org.chrisjr.loom.transforms.*;
import org.chrisjr.loom.mappings.*;

Loom loom;
Pattern pattern;

void setup() {
  size(800, 600);

  LsysRewriter lsys = new LsysRewriter("X->F-[[X]+X]+F[+FX]-X", "F->FF");
  EventCollection axiom = lsys.makeAxiom("X");

  lsys.generations = 10;
  lsys.setCommand("F", Draw.forward(10));
  lsys.setCommand("+", Draw.rotate(radians(35)));
  lsys.setCommand("-", Draw.rotate(radians(-35)));
  lsys.setCommand("[", Draw.push());
  lsys.setCommand("]", Draw.pop());

  pattern = new Pattern(loom, lsys.apply(axiom));
  pattern.asDrawCommand(lsys.getDrawCommands());
}

void draw() {
  loom.draw();
}

