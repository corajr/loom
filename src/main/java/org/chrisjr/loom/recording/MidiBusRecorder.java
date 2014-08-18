package org.chrisjr.loom.recording;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.*;
import org.chrisjr.loom.wrappers.*;
import org.chrisjr.loom.time.Interval;

import themidibus.MidiBus;

public class MidiBusRecorder implements IMidiBus {
	Loom loom;
	Sequence sequence;
	Track track;
	File midiFile;

	public MidiBusRecorder(Loom loom, File midiFile) {
		this.loom = loom;
		this.midiFile = midiFile;

		try {
			sequence = new Sequence(Sequence.PPQ, 250);
		} catch (InvalidMidiDataException e) {
			// we define the division type above, so this should never arise
			e.printStackTrace();
		}

		track = sequence.createTrack();
	}

	@Override
	public void sendMessage(MidiMessage message) {
		Interval currentInterval = loom.getCurrentInterval();
		BigFraction now = currentInterval.getStart().add(
				currentInterval.getSize().divide(2));
		long ticks = now.multiply(1000).longValue();

		track.add(new MidiEvent(message, ticks));
	}

	@Override
	public void dispose() {
		try {
			MidiSystem.write(sequence, 0, midiFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
