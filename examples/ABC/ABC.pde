/**
 * ABC shows the use of the text-based ABC format for entering musical notation.
 *
 * You will need The MidiBus <https://github.com/sparks/themidibus/> and a MIDI synthesizer
 * installed for this example. If you don't hear any sound, please consult the instructions at:
 * https://corajr.github.io/loom/reference/com/corajr/loom/wrappers/MidiBusImpl.html#midi
 *
 * Note that the key defaults to C major; if you want to specify a different key,
 * you can either provide just the key in the input string (e.g. "K:D\n ..."
 * or a complete ABC header. See http://abcnotation.com/learn for a tutorial
 * on ABC notation.
 */

import com.corajr.loom.*;
import com.corajr.loom.wrappers.*;
import com.corajr.loom.util.*;
import themidibus.*;

Loom loom;
Pattern pattern;
MidiBus myBus;

void setup() {
  size(400, 400);

  // Initialize the Loom with a tempo of 120 BPM.
  loom = new Loom(this, 120);

  // Creates the pattern from a string containing ABC notation.
  // If using C major, the header can be omitted.
  pattern = Pattern.fromABC(loom, "zCDEF3/2G/4F/4EA|DG3/2A/2G/2F/2E/2||");

  // List valid MIDI devices.
  // MidiBus.list();

  // Initialize the MIDI bus and add it to the Loom.
  myBus = new MidiBus(this, -1, "Bus 1");
  loom.setMidiBus(myBus);

  // Rendering as a color, interpolate between black and white;
  // rendering as MIDI, get the notes and scale from the pattern.
  pattern.asColor(#000000, #FFFFFF).asMidiMessage(pattern);

  // Don't repeat.
  pattern.once();

  loom.play();
}

void draw() {
  background(pattern.asColor());
}