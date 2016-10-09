import java.util.Map;

import com.corajr.loom.*;
import com.corajr.loom.time.*;
import com.corajr.loom.transforms.*;
import static com.corajr.loom.LEvent.*;

import themidibus.*;

MidiBus myBus;

int seed = 2;
boolean recording = false;

Loom loom;
NonRealTimeScheduler scheduler;

int desiredFPS = 30;

float millis = 0;
float millisPerFrame = 1000.0 / desiredFPS;

Interval singleInterval = new Interval(0, 1);
HashMap<Integer, Pattern> vertical, horizontal;

int repeats = 2;

int roadWidth = 12;

float mu = 0.0;
float sd = 1.2;

LEvent[] makeEvents() {
  int numEvents = 8;
  int[] durations = new int[numEvents];
  float totalDuration = 0.0;

  for (int i = 0; i < numEvents; i++) {
    int duration = int(exp(mu + (sd * randomGaussian())) + 1.0);
    durations[i] = duration;
    totalDuration += duration;
  }

  LEvent[] events = new LEvent[numEvents];

  for (int i = 0; i < numEvents; i++) {
    float duration = durations[i] / totalDuration;
    float value = duration > 0.2 ? 0.0 : round(random(1, 3)) / 4.0; 
    events[i] = evt(duration, value);
  }

  return seq(events);
}

void setup() {
  size(720, 720);
  noSmooth();

  scheduler = new NonRealTimeScheduler();

  if (recording) {
    loom = new Loom(this, scheduler);
    loom.recordMidi("boogie.mid");
  } else {
    loom = new Loom(this);
  }

  loom.setPeriod(7500);

  vertical = new HashMap<Integer, Pattern>();
  horizontal = new HashMap<Integer, Pattern>();

  randomSeed(seed);

  Pattern proto = new Pattern(loom, new EventCollection());
  proto.asColor(#FFD300, #DDDDDD, #00499A, #CA0000, #005C35).asMidiData2(0, 80);

  for (int i = 0; i < 10; i++) {
    int octave = ((i % 5) + 3) * 12;

    Pattern verticalPat = proto.clone();
    verticalPat.extend(makeEvents()).repeat(repeats);

    if (i != 0 && i != 8)
      verticalPat.after(repeats, seq(rest(0.3), evt(0.3, 1.0)));

    verticalPat.asMidiChannel(0).asMidiInstrument("ORCHESTRAL_HARP");
    verticalPat.asMidiNote(-127, 0, 4, 7, 0).transpose(octave).asMidiMessage(verticalPat);

    vertical.put(i * (width/10), verticalPat);

    Pattern horizontalPat = proto.clone();
    horizontalPat.extend(makeEvents()).repeat(repeats);

    if (i == 1)
      horizontalPat.after(repeats, seq(rest(0.2), rest(0.3), evt(0.1, 1.0), rest(0.1), evt(0.1, 1.0), rest(0.1), evt(0.2, 1.0)));
    else if (i == 4)
      horizontalPat.after(repeats, seq(rest(0.2), rest(0.1), evt(0.1, 1.0), rest(0.1), evt(0.1, 1.0), rest(0.1), evt(0.1, 1.0)));

    horizontalPat.asColor(#FFD300, #DDDDDD, #00499A, #CA0000, #005C35);
    horizontalPat.asMidiChannel(1).asMidiInstrument("ORCHESTRAL_HARP");
    horizontalPat.asMidiNote(-127, 2, 9, 11, 9).transpose(octave).asMidiMessage(horizontalPat);

    horizontal.put(i * (height/10), horizontalPat);
  }

  if (!recording) {
    myBus = new MidiBus(this, "Bus 1", "Bus 1");
    loom.setMidiBus(myBus);
    loom.play();
  }
}

void draw() {
  background(255);

  for (Map.Entry me : horizontal.entrySet ()) {
    Pattern pat = (Pattern) me.getValue();
    pat.rect(0, ((Integer) me.getKey()).intValue(), width, roadWidth, singleInterval);
  }

  rotate(HALF_PI);
  translate(0, -width);

  for (Map.Entry me : vertical.entrySet ()) {
    Pattern pat = (Pattern) me.getValue();
    pat.rect(0, ((Integer) me.getKey()).intValue(), height, roadWidth, singleInterval);
  }

  if (recording) {
    millis += millisPerFrame;
    scheduler.setElapsedMillis((long) millis);

    saveFrame("####.png");

    if (millis > loom.getPeriod() * (repeats + 1.3))
      exit();
  }
}

void exit() {
  loom.dispose(); // needed to save MIDI file
  super.exit();
}

