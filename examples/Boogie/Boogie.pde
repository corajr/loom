import java.util.Map;

import org.chrisjr.loom.*;
import org.chrisjr.loom.time.*;
import org.chrisjr.loom.wrappers.*;
import static org.chrisjr.loom.Event.*;

import themidibus.*;

MidiBus myBus;

int seed = 0;
boolean recording = false;

Integer[] verticalPitches = new Integer[] { 57, 60, 64};
Integer[] horizontalPitches = new Integer[] { 62, 67, 71};

Loom loom;
NonRealTimeScheduler scheduler;

int desiredFPS = 24;

float millis = 0;
float millisPerFrame = 1000.0 / desiredFPS;

HashMap<Integer, Pattern> vertical, horizontal;

int roadWidth = 10;
color yellow = #FFD300;
color gray = #DDDDDD;
color blue = #00499A;
color red = #CA0000;

float mu = 0.0;
float sd = 1.0;

org.chrisjr.loom.Event[] makeEvents() {
  int numEvents = 20;
  int[] durations = new int[numEvents];
  float totalDuration = 0.0;

  for (int i = 0; i < numEvents; i++) {
    int duration = int(exp(mu + (sd * randomGaussian())) + 1.0);
    durations[i] = duration;
    totalDuration += duration;
  }

  org.chrisjr.loom.Event[] events = new org.chrisjr.loom.Event[numEvents];

  for (int i = 0; i < numEvents; i++) {
    float duration = durations[i] / totalDuration;
    float value = duration > 0.1 ? 0.0 : round(random(1, 3)) / 3.0; 
    events[i] = evt(duration, value);
  }

  return seq(events);
}

void setup() {
  size(600, 600);
  noSmooth();

  if (recording) {
    scheduler = new NonRealTimeScheduler();
    loom = new Loom(this, scheduler);
  } else {
    loom = new Loom(this, 5);
  }
  vertical = new HashMap<Integer, Pattern>();
  horizontal = new HashMap<Integer, Pattern>();

  randomSeed(seed);

  for (int i = 0; i < 10; i++) {
    Pattern verticalPat = new Pattern(loom, makeEvents());
    verticalPat.asColor(yellow, gray, blue, red).loop();
    verticalPat.asMidiChannel(0).asMidi("ACOUSTIC_GRAND_PIANO");
    verticalPat.asMidiNote(verticalPitches).asMidiMessage(verticalPat);
    vertical.put(i * 60, verticalPat);

    Pattern horizontalPat = new Pattern(loom, makeEvents());
    horizontalPat.asColor(yellow, gray, blue, red).loop();
    horizontalPat.asMidiChannel(1).asMidi("ACOUSTIC_GRAND_PIANO");
    horizontalPat.asMidiNote(horizontalPitches).asMidiMessage(horizontalPat);
    horizontal.put(i * 60, horizontalPat);
  }

  if (recording) {
    loom.recordMidi("boogie.mid");
  } else {
    myBus = new MidiBus(this, "Bus 1", "Bus 1");
    loom.midiBusWrapper.set(new MidiBusImpl(myBus));
    loom.play();
  }
}

void draw() {
  background(255);

  for (Map.Entry me : horizontal.entrySet ()) {
    Pattern pat = (Pattern) me.getValue();
    pat.rect(0, ((Integer) me.getKey()).intValue(), width, roadWidth);
  }

  translate(width, 0);
  rotate(HALF_PI);

  for (Map.Entry me : vertical.entrySet ()) {
    Pattern pat = (Pattern) me.getValue();
    pat.rect(0, ((Integer) me.getKey()).intValue(), height, roadWidth);
  }


  if (recording) {
    saveFrame("####.png");

    millis += desiredFPS;
    scheduler.setElapsedMillis((long) millis);

    if (millis > 4000)
      exit();
  }
}

void exit() {
  loom.dispose(); // needed to save MIDI file
  super.exit();
}

