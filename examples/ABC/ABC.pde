import org.chrisjr.loom.*;
import org.chrisjr.loom.wrappers.*;
import org.chrisjr.loom.util.*;
import themidibus.*;

Loom loom;
Pattern pattern;
MidiBus myBus;

void setup() {
  size(400, 400);

  loom = new Loom(this, 120);
  pattern = Pattern.fromABC(loom, "zCDEF3/2G/4F/4EA|DG3/2A/2G/2F/2E/2||");

  myBus = new MidiBus(this, "Bus 1", "Bus 1");
  loom.setMidiBus(myBus);

  pattern.asColor(#000000, #FFFFFF).asMidiMessage(pattern);
  pattern.once();

  loom.play();
}

void draw() {
  background(pattern.asColor());
}

