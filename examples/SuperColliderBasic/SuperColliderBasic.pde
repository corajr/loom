/**
 * SuperColliderBasic shows how to interface with SuperCollider.
 *
 * Patterns in Loom can be used to drive an SC synthesizer.
 * To run this example, first run the code from SynthDef.pde
 * in SC to create the synth.
 */

import com.corajr.loom.*;
import supercollider.*;
import oscP5.*;

Loom loom;
Pattern pattern;
Synth synth;

void setup() {
  size(400, 400);

  loom = new Loom(this);
  pattern = new Pattern(loom);

  // set-up the initial synth
  synth = new Synth("sine");

  synth.set("amp", 0.5);
  synth.set("freq", 220);

  pattern.extend("0101");
  pattern.asColor(#000000, #FFFFFF);

  // interpolate the parameter between 220 and 440 Hz
  pattern.asSynthParam(synth, "freq", 220, 440);

  // send the synth creation command to the server
  synth.create();
  pattern.loop();

  loom.play();
}

void draw() {
  background(pattern.asColor());
}

void exit() {
  synth.free(); // be sure to free up the synth!
  super.exit();
}