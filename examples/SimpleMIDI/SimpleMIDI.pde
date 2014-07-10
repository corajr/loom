import org.chrisjr.loom.*;
import org.chrisjr.loom.wrappers.*;
import themidibus.*;

Loom loom;
Pattern pattern;
MidiBus myBus;

void setup() {
  size(400,400);
  
  loom = new Loom(this, 120);
  pattern = new Pattern(loom);
  myBus = new MidiBus(this, "Bus 1", "Bus 1");
  
  loom.midiBusWrapper.set(new MidiBusImpl(myBus));

  pattern.extend("0242");
  pattern.asColor(#000000, #FFFFFF);

  pattern.asMidiNote(60, 64, 67);
  pattern.asMidiMessage(pattern);

  pattern.loop();
  
  loom.play();
}

void draw() {
  background(pattern.asColor());
}
