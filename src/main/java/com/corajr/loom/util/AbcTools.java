package com.corajr.loom.util;

import java.util.Iterator;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.*;
import com.corajr.loom.time.Interval;

import abc.notation.*;
import abc.parser.TuneParser;

public class AbcTools {
	public static final TuneParser parser = new TuneParser();

	public static Pattern fromString(Loom loom, String tuneString) {
		EventCollection tuneEvents = eventsFromString(tuneString);
		Pattern pat = new Pattern(loom, tuneEvents);
		pat.setLoopInterval(tuneEvents.getTotalInterval());
		return pat.asMidiData1(0, 127);
	}

	public static EventCollection eventsFromString(String tuneString) {
		EventCollection events = new EventCollection();

		if (!tuneString.startsWith("X:")) {
			if (tuneString.startsWith("K:")) {
				tuneString = "X:1\nT:\n" + tuneString;
			} else {
				tuneString = "X:1\nT:\nK:C\n" + tuneString;
			}
		}

		Tune tune = null;
		try {
			tune = parser.parse(tuneString);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Tune could not be parsed!");
		}

		if (tune != null) {
			BigFraction start = BigFraction.ZERO;

			@SuppressWarnings("unchecked")
			Iterator<MusicElement> it = tune.getMusic().iterator();

			KeySignature sig = null;

			while (it.hasNext()) {
				MusicElement elem = it.next();
				if (elem instanceof KeySignature) {
					sig = (KeySignature) elem;
				} else if (elem instanceof Note) {
					Note note = (Note) elem;

					byte whitekey = note.getStrictHeight();
					short dur = note.getDuration();
					BigFraction duration = new BigFraction(dur, 96);

					if (whitekey != Note.REST) {
						// dealing with actual note

						byte accidental = AccidentalType.NONE;
						if (note.hasAccidental()) {
							accidental = note.getAccidental();
						} else {
							if (sig != null) {
								accidental = sig.getAccidentalFor(whitekey);
							}
						}
						byte octave = note.getOctaveTransposition();

						int octaveStart = 60 + (12 * octave);

						int midinote = octaveStart + whitekey;

						switch (accidental) {
						case AccidentalType.SHARP:
							midinote++;
							break;
						case AccidentalType.FLAT:
							midinote--;
							break;
						case AccidentalType.NONE:
						case AccidentalType.NATURAL:
						default:
							break;
						}
						Interval interval = new Interval(start,
								start.add(duration));
						events.add(new LEvent(interval, midinote / 127.0));
					}

					start = start.add(duration);

				}
			}
		}

		return events;
	}
}
