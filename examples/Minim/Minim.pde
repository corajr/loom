import org.chrisjr.loom.*;
import ddf.minim.*;

Loom loom;
Pattern pattern;
Minim minim;

void setup() {
  size(400,400);
  
  loom = new Loom(this);
  pattern = new Pattern(loom);
  minim = new Minim(this);

  snare = minim.loadSample("snare.aif");
  pattern.extend("0101");
  pattern.asSample(snare);

  pattern.loop();
  
  loom.play();
}

void draw() {
  background(pattern.asColor());
}