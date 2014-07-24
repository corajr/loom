import org.chrisjr.loom.*;
import org.chrisjr.loom.wrappers.*;
import org.chrisjr.loom.util.*;
import themidibus.*;

Loom loom;
Pattern pattern;
MidiBus myBus;

void setup() {
  size(400,400);
  
  loom = new Loom(this, 120);
  pattern = Pattern.fromABC(loom, "C|^FG2C|(3^FGA(3FGA|GA2||");
  myBus = new MidiBus(this, "Bus 1", "Bus 1");
  
  loom.midiBusWrapper.set(new MidiBusImpl(myBus));

  pattern.asColor(#000000, #FFFFFF);
  pattern.asMidiMessage(pattern);

  pattern.loop();
  
  loom.play();
}

void draw() {
  background(pattern.asColor());
}
