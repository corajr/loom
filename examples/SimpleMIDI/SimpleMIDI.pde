/**
 * SimpleMIDI demonstrates the basic use of MIDI to play notes.
 *
 * You will need The MidiBus <https://github.com/sparks/themidibus/> installed for this example.
 *
 * You will also need a MIDI synthesizer connected (real or virtual) to hear sound.
 * OS X users might try SimpleSynth <http://notahat.com/simplesynth/>, while Windows
 * users can simply use the sketch as-is.
 */
import com.corajr.loom.*;
import com.corajr.loom.wrappers.*;
import themidibus.*;

Loom loom;
Pattern pattern;
MidiBus myBus;

void setup() {
  size(400,400);
  
  loom = new Loom(this, 120);
  pattern = new Pattern(loom);
  
  // Initialize the MIDI bus and add it to the Loom.
  myBus = MidiBusImpl.getDefaultMidiBus(this);
  loom.setMidiBus(myBus);

  pattern.extend("0123");
  pattern.asColor(#000000, #FFFFFF);

  // 1 maps to 60 (middle C), 2 to 64 (E), 3 to 67 (G)
  // (0 maps to -127, meaning a rest.)
  pattern.asMidiNote(-127, 60, 64, 67);
  
  // This is needed to generate note-on/-off messages
  // as well as setting the default channel and instrument.
  pattern.asMidiMessage(pattern);

  pattern.loop();
  
  loom.play();
}

void draw() {
  background(pattern.asColor());
}