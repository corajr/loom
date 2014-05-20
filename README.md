# Loom

## An Audiovisual Pattern Language for Processing

Loom is a Processing library that allows the user to specified patterns for generative audiovisual works. Such patterns may unfold along a linear timeline or recur cyclically, and can be triggered by external events (e.g. sensor input or user interaction). This frees the coder to focus on the rhythm and dynamics of the experience, rather than fussing over synchronization issues and frame rates.

### Features

* patterns can map to any kind of output
* patterns can be combined/transformed
* "batteries included" (immediately get interesting results)
	* but, still easy to integrate with external programs such as Max/MSP or SuperCollider
* scheduler to support both real-time and non-real-time synthesis, optionally recording a "score" for later playback

### Getting Started

Here is a code sample (subject to change) of how it might work:

	Loom loom;
	Pattern pat, pat2, pat3, pat4;
	
	void setup() {
	    // […]
	
	    // create the overall pattern controller for sketch
	    // with optional BPM argument
	    loom = new Loom(this, 160);
	
	    // make an empty pattern
	    // (attaches itself to loom automatically)
	    pat = new Pattern(this);
	
	    // the rhythm from Steve Reich's "Clapping Music"
	    // represented as string for conciseness
	    // (numerical args also possible)
	    pat.extend("111011010110");
	
	    // sound rendering handled automatically e.g.
	    // through Java MIDI (or Minim, SuperCollider, ...)
	    pat.asSound("clap");
	
	    // switch between blue to white in tandem with sound
	    pat.asColor("blue", "white");
	
	    pat2 = pat.clone();
	
	    // chained method calls + transformation
	    pat2.asSound("clave").every(12, "shiftLeft");
	
	    pat2.asColor("red", "white");
	
	    // set to loop
	    pat.loop();
	    pat2.loop();
	
	    // continuous beat-linked pattern
	    pat3 = new Pattern(this, "sine");
	    pat3.period(6); // complete cycle in 6 beats
	    pat3.asInt(0, 100); // maps [-1,1] to [0,100]
	
	    // every 12 bars of main cycle:
	    //     run the sine pattern four times then stop
	    pat.every(12, pat3.repeat(4));
	
	    // chord triggered by external event
	    pat4 = new Pattern(this);
	    pat4.simultaneous(0, 4, 7).asSound("piano");
	    pat4.listen("mouseClicked");
	
	    // patterns will start on first frame (t == 0.0 s)
	}
	
	void draw() {
	    background(255);
	
	    // patterns execute independently of draw loop
	    // current state can be retrieved in desired formats
	
	    fill(pat.asColor());
	    rect(0, 0, width/2, height);
	
	    fill(pat2.asColor());
	    rect(width/2, 0, width/2, height);
	
	    fill(255);
	    ellipse(width/2, height/2, pat3.asInt(), pat3.asInt());
	}
	
	void keyTyped() {
	    if (key == '+' || key == '=') {
	        loom.speed(1.25); // increase overall speed
	    } else if (key == '-' || key == '_')
	        loom.speed(0.8); // decrease
	    } else if (key == ' ') {
	        loom.pause(); // pause pattern execution
	    }
	}