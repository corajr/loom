/**
 * Fugue shows how patterns can be transformed and combined.
 *
 * Using Bach's Fugue no. 1 in C from the Well-Tempered Clavier,
 * we can write the subject and counter-subject out and play each
 * with transposition or delay.
 *
 * You will need The MidiBus <https://github.com/sparks/themidibus/> and a MIDI synthesizer
 * installed for this example. If you don't hear any sound, please consult the instructions at:
 * https://corajr.github.io/loom/reference/com/corajr/loom/wrappers/MidiBusImpl.html#midi
 */

import com.corajr.loom.*;
import com.corajr.loom.time.*;
import com.corajr.loom.util.*;
import themidibus.*;

Loom loom;
Pattern pattern, pattern2, pattern3;
Interval duration;

MidiBus myBus;

String header = "X:1\nT:Bach Fugue no. 1 in C Major\nL:1/8\nK:C\n";
String fugueSubject = header + "zCDEF3/2G/4F/4EA|DG3/2A/2G/2F/2E/2";
String counterSubject = header + "F/2E/2D/2C/2D/2C/2B,/2|A,^FG3^F/2E/2^FD|G";

void setup() {
  size(400, 300);

  loom = new Loom(this, 104);
  pattern = Pattern.fromABC(loom, fugueSubject);
  pattern.asColor(#000100, #00FF00);

  // transpose the subject up a 5th, delayed by 3 bars
  pattern2 = pattern.clone().transpose(7).delay(3);
  pattern2.asColor(#010000, #FF0000);

  pattern3 = Pattern.fromABC(loom, counterSubject);
  pattern3.delay(3.125);
  pattern3.asColor(#000001, #0000FF);
  
  pattern.asMidiMessage(pattern);
  pattern2.asMidiMessage(pattern2);
  pattern3.asMidiMessage(pattern3);
  
  duration = pattern2.getTotalInterval();

  // List valid MIDI devices.
  // MidiBus.list();

  // Initialize the MIDI bus and add it to the Loom.
  myBus = new MidiBus(this, -1, "Bus 1");
  loom.setMidiBus(myBus);

  loom.play();
}

void draw() {
  background(0);
  pattern.rect(0, 0, width, 100, duration);
  pattern2.rect(0, 100, width, 100, duration);
  pattern3.rect(0, 200, width, 100, duration);
}