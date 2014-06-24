package org.chrisjr.loom.util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.sound.midi.*;

public class MidiTools {
	public static void printMidi(MidiMessage message) {
		printMidiRaw(message.getMessage());
	}

	public static void printMidiRaw(byte[] bytes) {
		for (byte b : bytes) {
			System.out.print("0x" + Integer.toHexString(b & 0xff));
			System.out.print(" ");
		}
		System.out.println();
	}

	public static Track[] readTracksFrom(File midiFile) {
		Sequence sequence = null;
		try {
			sequence = MidiSystem.getSequence(midiFile);
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}

		if (sequence == null)
			throw new IllegalArgumentException("MIDI file "
					+ midiFile.getPath() + " could not be loaded.");

		return sequence.getTracks();
	}

	/**
	 * @param midiFile
	 *            the MIDI file to be loaded
	 * @return a list of MIDI events (with all tracks collapsed into one)
	 */
	public static List<MidiEvent> readFile(File midiFile) {
		List<MidiEvent> events = new ArrayList<MidiEvent>();

		for (Track track : readTracksFrom(midiFile)) {
			for (int i = 0; i < track.size(); i++) {
				events.add(track.get(i));
			}
		}

		return events;
	}
}
