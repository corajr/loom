/**
 * Clap replicates a famous composition by a minimalist composer.
 *
 * There are two patterns: one stays the same and the other shifts.
 * The procedure for shifting the second pattern is described declaratively,
 * rather than written out.
 *
 * You will need The MidiBus <https://github.com/sparks/themidibus/> installed for this example.
 *
 * You will also need a MIDI synthesizer connected (real or virtual) to hear sound.
 * OS X users might try SimpleSynth <http://notahat.com/simplesynth/>, while Windows
 * users can simply use the sketch as-is.
 */
import themidibus.*;

import com.corajr.loom.*;
import com.corajr.loom.wrappers.*;
import com.corajr.loom.transforms.*;
import com.corajr.loom.continuous.*;
import com.corajr.loom.util.MidiTools.Percussion;

MidiBus myBus;
Loom loom;
Pattern pattern, pattern2;

void setup() {
  size(400, 400);

  loom = new Loom(this, 112);
  myBus = MidiBusImpl.getDefaultMidiBus(this);
  loom.setMidiBus(myBus);

  pattern = new Pattern(loom);

  // define the basic beat pattern
  pattern.extend("111011010110").loop();
  pattern.asColor(#000000, #FFFFFF);  
  pattern2 = pattern.clone();

  pattern.asMidiPercussion(Percussion.HAND_CLAP);
  pattern2.asMidiPercussion(Percussion.CLAVES);

  // This is the crucial piece: every 4 bars, shift the 12-beat pattern left by 1.
  pattern2.every(4, new Transforms.Shift(-1, 12));

  loom.play();
}

void draw() {
  background(0);

  fill(pattern.asColor());
  rect(0, 0, width / 2, height);

  fill(pattern2.asColor());
  rect(width / 2, 0, width / 2, height);
}