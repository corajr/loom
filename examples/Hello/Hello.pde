import org.chrisjr.loom.*;

Loom loom;
Pattern pattern;

void setup() {
  size(400,400);
  
  loom = new Loom(this);
  pattern = new Pattern(loom);

  pattern.extend("0101");
  pattern.asColor(#000000, #FFFFFF);

  pattern.loop();
  
  loom.play();
}

void draw() {
  background(pattern.asColor());
}