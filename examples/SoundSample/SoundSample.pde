import processing.sound.*;

/**
 * SoundSample uses the Sound library to play a sample according to a pattern.
 *
 * Be sure you have installed the Sound library from the Processing
 * Contribution Manager (Sketch -> Import Library -> Add Library).
 */

import com.corajr.loom.*;
import processing.sound.*;
SoundFile snare;

Loom loom;
Pattern pattern;

void setup() {
  size(400,400);
  
  loom = new Loom(this);
  pattern = new Pattern(loom);

  SoundFile snare = new SoundFile(this, "snare.aif");
  pattern.extend("0101");
  pattern.asColor(#000000, #FFFFFF);
  pattern.asSoundFile(snare);

  pattern.loop();
  
  loom.play();
}

void draw() {
  background(pattern.asColor());
}