import org.chrisjr.loom.*;
import org.chrisjr.loom.transforms.*;
import org.chrisjr.loom.mappings.*;

Loom loom;
Pattern pattern;

void setup() {
  size(800, 600);
  
  loom = new Loom(this);

  LsysRewriter lsys = new LsysRewriter("X->F-[[X]+X]+F[+FX]-X", "F->FF");
  EventCollection axiom = lsys.makeAxiom("X");

  lsys.generations = 5;
  lsys.setCommand("F", Draw.forward(10));
  lsys.setCommand("+", Draw.rotate(radians(35)));
  lsys.setCommand("-", Draw.rotate(radians(-35)));
  lsys.setCommand("[", Draw.push());
  lsys.setCommand("]", Draw.pop());

  EventCollection events = lsys.apply(axiom);
  pattern = new Pattern(loom, events);
  pattern.asTurtleDrawCommand(lsys.getDrawCommands());
  
  loom.play();
}

void draw() {
  translate(width/2, height/2);
  loom.draw();
}
