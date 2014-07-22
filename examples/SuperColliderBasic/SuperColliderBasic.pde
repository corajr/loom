import org.chrisjr.loom.*;
import supercollider.*;
import oscP5.*;

Loom loom;
Pattern pattern;
Synth synth;

void setup() {
  size(400, 400);

  loom = new Loom(this);
  pattern = new Pattern(loom);

  synth = new Synth("sine");

  synth.set("amp", 0.5);
  synth.set("freq", 220);

  pattern.extend("0101");
  pattern.asColor(#000000, #FFFFFF);

  pattern.asSynthParam(synth, "freq", 220, 440);

  synth.create();
  pattern.loop();

  loom.play();
}

void draw() {
  background(pattern.asColor());
}

void exit() {
  synth.free();
  super.exit();
}
