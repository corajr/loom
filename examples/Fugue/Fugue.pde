import org.chrisjr.loom.*;
import org.chrisjr.loom.time.*;
import org.chrisjr.loom.wrappers.*;
import org.chrisjr.loom.util.*;
import themidibus.*;

Loom loom;
Pattern pattern, pattern2, pattern3;
Interval duration;

MidiBus myBus;

String header = "X:1\nT:Bach Fugue no. 1 in C Major\nL:1/8\nK:C\n";
String fugueSubject = header + "zCDEF3/2G/4F/4EA|DG3/2A/2G/2F/2E/2";
String counterSubject = header + "F/2E/2D/2C/2D/2C/2B,/2|A,^FG3^F/2E/2^FD|G";

void setup() {
  size(400, 300);

  loom = new Loom(this, 104);
  pattern = Pattern.fromABC(loom, fugueSubject);
  pattern.asColor(#000100, #00FF00);

  pattern2 = pattern.clone().transpose(7).delay(3);
  pattern2.asColor(#010000, #FF0000);

  pattern3 = Pattern.fromABC(loom, counterSubject);
  pattern3.delay(3.125);
  pattern3.asColor(#000001, #0000FF);
  
  pattern.asMidiMessage(pattern);
  pattern2.asMidiMessage(pattern2);
  pattern3.asMidiMessage(pattern3);
  
  duration = pattern2.getTotalInterval();

  myBus = new MidiBus(this, "Bus 1", "Bus 1");

  loom.midiBusWrapper.set(new MidiBusImpl(myBus));

  loom.play();
}

void draw() {
  background(0);
  pattern.rect(0, 0, width, 100, duration);
  pattern2.rect(0, 100, width, 100, duration);
  pattern3.rect(0, 200, width, 100, duration);
}

