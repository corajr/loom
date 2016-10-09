/**
 * Click shows how user input can influence a pattern.
 *
 * Clicking causes the TriggerFunction to fire,
 * momentarily raising the pattern's value to 1.
 */

import com.corajr.loom.*;
import com.corajr.loom.continuous.*;

Loom loom;
Pattern pattern;
TriggerFunction trigger;

void setup() {
  size(400,400);
  
  loom = new Loom(this);
  trigger = new TriggerFunction();
  pattern = new Pattern(loom, trigger);

  pattern.asColor(#000000, #FFFFFF);

  pattern.loop();
  
  loom.play();
}

void draw() {
  background(pattern.asColor());
}

void mouseClicked() {
  trigger.fire();
}