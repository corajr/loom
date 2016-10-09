/**
 * SimpleTurtle draws a square using a "turtle," as in LOGO.
 *
 * Turtle drawing is needed for L-systems.
 */
import com.corajr.loom.*;
import com.corajr.loom.mappings.*;

Loom loom;
Pattern pattern;

void setup() {
  size(400, 400);

  loom = new Loom(this);
  pattern = new Pattern(loom);

  pattern.extend("0123");
  TurtleDrawCommand forwardAndTurn = 
    TurtleDraw.c(TurtleDraw.forward(100), TurtleDraw.turn(HALF_PI));

  pattern.asTurtleDrawCommand(forwardAndTurn);
  pattern.loop();

  loom.play();
}

void draw() {
  background(255);
  translate(width/2, height/2);

  loom.draw();
}