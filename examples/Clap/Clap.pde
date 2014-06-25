import themidibus.*;

import org.chrisjr.loom.*;
import org.chrisjr.loom.transforms.*;
import org.chrisjr.loom.continuous.*;

Loom loom;
Pattern pat, pat2;

void setup() {
  size(400, 400);

  loom = new Loom(this, 160);
  pat = new Pattern(loom);

  pat.extend("111011010110");
  pat.asColor(#000000, #FFFFFF);

  pat2 = new Pattern(loom, new FollowerFunction(pat));

  pat2.asColor(#000000, #FFFFFF);
  pat2.every(12, new Transforms.Shift(-1, 12));

  pat.loop();
  pat2.loop();

  loom.play();
}

void draw() {
  background(0);

  fill(pat.asColor());
  rect(0, 0, width / 2, height);

  fill(pat2.asColor());
  rect(width / 2, 0, width / 2, height);
}
