import themidibus.*;

import org.chrisjr.loom.*;
import org.chrisjr.loom.wrappers.*;
import org.chrisjr.loom.transforms.*;
import org.chrisjr.loom.continuous.*;
import org.chrisjr.loom.util.MidiTools.Percussion;

MidiBus myBus;
Loom loom;
Pattern[] patterns;

void setup() {
  size(400, 400);

  loom = new Loom(this, 120);
  myBus = new MidiBus(this, "Bus 1", "Bus 1");
  loom.setMidiBus(myBus);

  patterns = new Pattern[2];

  for (int i = 0; i < 2; i++) {
    patterns[i] = new Pattern(loom);
    patterns[i].extend("111011010110")
      .asColor(#000000, #FFFFFF)
        .asMidi(i == 0 ? Percussion.HAND_CLAP : Percussion.CLAVES)
          .loop();
  }

  patterns[1].every(4, new Transforms.Shift(-1, 12));

  loom.play();
}

void draw() {
  background(0);

  fill(patterns[0].asColor());
  rect(0, 0, width / 2, height);

  fill(patterns[1].asColor());
  rect(width / 2, 0, width / 2, height);
}

