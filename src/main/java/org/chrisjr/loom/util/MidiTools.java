package org.chrisjr.loom.util;

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
}
