/**
 * Blink flips the sketch window between black and white.
 *
 * A minimal example of Loom's Pattern metaphor; it contains the
 * Loom (scheduler) and a single Pattern on that loom.
 */

import com.corajr.loom.*;

Loom loom;
Pattern pattern;

void setup() {
  size(400, 400);

  loom = new Loom(this);
  pattern = new Pattern(loom);

  // The pattern is initially empty, so we must `extend` it
  // with values. The string "0101" defines this pattern,
  // which is spread out over one second.
  // Try others such as "01", "1000", etc. to see the effect.
  pattern.extend("0101");

  // When rendering this pattern as a color,
  // we declare 0 is black and 1 is white.
  pattern.asColor(#000000, #FFFFFF);

  // Loop the pattern (otherwise, it stops after one cycle)
  pattern.loop();

  // Finally, start the scheduler.
  loom.play();
}

void draw() {
  // The pattern cycles independently of the draw loop;
  // we can get its value at a particular time.
  background(pattern.asColor());
}