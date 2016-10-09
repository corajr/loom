/**
 * ColorBars renders a pattern spread out in space.
 *
 * Rather than showing just the present value, future pattern values
 * scroll across the screen.
 */

import com.corajr.loom.*;

Loom loom;
Pattern pattern;

void setup() {
  size(400, 400);

  loom = new Loom(this);
  pattern = new Pattern(loom)
    .extend("0123456")
    .loop();

  pattern.asColor(#EBEBEB, 
    #EBEB10, 
    #10EBEB, 
    #10EB10, 
    #EB10EB, 
    #EB1010, 
    #1010EB);

  loom.play();
}

void draw() {
  background(0);

  // Render the full pattern as a rectangle (args in x, y, w, h order)
  pattern.rect(0, 0, width, height);
}