package com.corajr.loom.util;

import java.io.File;
import java.util.*;

import javax.sound.midi.*;

public class MidiTools {
	public static void printMidi(MidiMessage message) {
		printMidiRaw(message.getMessage());
	}

	public static void printMidiRaw(byte[] bytes) {
		boolean first = true;
		for (byte b : bytes) {
			if (first)
				first = false;
			else
				System.out.print(" ");
			System.out.print("0x" + Integer.toHexString(b & 0xff));
		}
		System.out.println();
	}

	public static Track[] readTracksFrom(File midiFile) {
		Sequence sequence = null;
		try {
			sequence = MidiSystem.getSequence(midiFile);
		} catch (Exception e) {
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

	/**
	 * Get a hash code for a message that allows to test for equality.
	 * 
	 * @param message
	 *            the message to be hashed
	 * @return a hash code for the message
	 */
	public static int hash(ShortMessage message) {
		return Arrays.hashCode(message.getMessage());
	}

	/**
	 * The names of all General MIDI instruments.
	 * 
	 */
	public enum Instrument {
		ACOUSTIC_GRAND_PIANO, BRIGHT_ACOUSTIC_PIANO, ELECTRIC_GRAND_PIANO, HONKY_TONK_PIANO, ELECTRIC_PIANO_1, ELECTRIC_PIANO_2, HARPSICHORD, CLAVI,

		CELESTA, GLOCKENSPIEL, MUSIC_BOX, VIBRAPHONE, MARIMBA, XYLOPHONE, TUBULAR_BELLS, DULCIMER,

		DRAWBAR_ORGAN, PERCUSSIVE_ORGAN, ROCK_ORGAN, CHURCH_ORGAN, REED_ORGAN, ACCORDION, HARMONICA, TANGO_ACCORDION,

		ACOUSTIC_GUITAR_NYLON, ACOUSTIC_GUITAR_STEEL, ELECTRIC_GUITAR_JAZZ, ELECTRIC_GUITAR_CLEAN, ELECTRIC_GUITAR_MUTED, OVERDRIVEN_GUITAR, DISTORTION_GUITAR, GUITAR_HARMONICS,

		ACOUSTIC_BASS, ELECTRIC_BASS_FINGER, ELECTRIC_BASS_PICK, FRETLESS_BASS, SLAP_BASS_1, SLAP_BASS_2, SYNTH_BASS_1, SYNTH_BASS_2,

		VIOLIN, VIOLA, CELLO, CONTRABASS, TREMOLO_STRINGS, PIZZICATO_STRINGS, ORCHESTRAL_HARP, TIMPANI,

		STRING_ENSEMBLE_1, STRING_ENSEMBLE_2, SYNTHSTRINGS_1, SYNTHSTRINGS_2, CHOIR_AAHS, VOICE_OOHS, SYNTH_VOICE, ORCHESTRA_HIT,

		TRUMPET, TROMBONE, TUBA, MUTED_TRUMPET, FRENCH_HORN, BRASS_SECTION, SYNTHBRASS_1, SYNTHBRASS_2,

		SOPRANO_SAX, ALTO_SAX, TENOR_SAX, BARITONE_SAX, OBOE, ENGLISH_HORN, BASSOON, CLARINET,

		PICCOLO, FLUTE, RECORDER, PAN_FLUTE, BLOWN_BOTTLE, SHAKUHACHI, WHISTLE, OCARINA,

		LEAD_1_SQUARE, LEAD_2_SAWTOOTH, LEAD_3_CALLIOPE, LEAD_4_CHIFF, LEAD_5_CHARANG, LEAD_6_VOICE, LEAD_7_FIFTHS, LEAD_8_BASS_LEAD,

		PAD_1_NEW_AGE, PAD_2_WARM, PAD_3_POLYSYNTH, PAD_4_CHOIR, PAD_5_BOWED, PAD_6_METALLIC, PAD_7_HALO, PAD_8_SWEEP,

		FX_1_RAIN, FX_2_SOUNDTRACK, FX_3_CRYSTAL, FX_4_ATMOSPHERE, FX_5_BRIGHTNESS, FX_6_GOBLINS, FX_7_ECHOES, FX_8_SCI_FI,

		SITAR, BANJO, SHAMISEN, KOTO, KALIMBA, BAG_PIPE, FIDDLE, SHANAI,

		TINKLE_BELL, AGOGO, STEEL_DRUMS, WOODBLOCK, TAIKO_DRUM, MELODIC_TOM, SYNTH_DRUM, REVERSE_CYMBAL,

		GUITAR_FRET_NOISE, BREATH_NOISE, SEASHORE, BIRD_TWEET, TELEPHONE_RING, HELICOPTER, APPLAUSE, GUNSHOT
	}

	public enum Percussion {
		ACOUSTIC_BASS_DRUM, BASS_DRUM_1, SIDE_STICK, ACOUSTIC_SNARE, HAND_CLAP, ELECTRIC_SNARE, LOW_FLOOR_TOM, CLOSED_HI_HAT, HIGH_FLOOR_TOM, PEDAL_HI_HAT, LOW_TOM, OPEN_HI_HAT, LOW_MID_TOM, HI_MID_TOM, CRASH_CYMBAL_1, HIGH_TOM, RIDE_CYMBAL_1, CHINESE_CYMBAL, RIDE_BELL, TAMBOURINE, SPLASH_CYMBAL, COWBELL, CRASH_CYMBAL_2, VIBRASLAP, RIDE_CYMBAL_2, HI_BONGO, LOW_BONGO, MUTE_HI_CONGA, OPEN_HI_CONGA, LOW_CONGA, HIGH_TIMBALE, LOW_TIMBALE, HIGH_AGOGO, LOW_AGOGO, CABASA, MARACAS, SHORT_WHISTLE, LONG_WHISTLE, SHORT_GUIRO, LONG_GUIRO, CLAVES, HI_WOOD_BLOCK, LOW_WOOD_BLOCK, MUTE_CUICA, OPEN_CUICA, MUTE_TRIANGLE, OPEN_TRIANGLE;

		public int getNote() {
			return ordinal() + 35;
		}
	}

	public enum Note {
		CNeg1, CSharpNeg1, DNeg1, DSharpNeg1, ENeg1, FNeg1, FSharpNeg1, GNeg1, GSharpNeg1, ANeg1, ASharpNeg1, BNeg1, C0, CSharp0, D0, DSharp0, E0, F0, FSharp0, G0, GSharp0, A0, ASharp0, B0, C1, CSharp1, D1, DSharp1, E1, F1, FSharp1, G1, GSharp1, A1, ASharp1, B1, C2, CSharp2, D2, DSharp2, E2, F2, FSharp2, G2, GSharp2, A2, ASharp2, B2, C3, CSharp3, D3, DSharp3, E3, F3, FSharp3, G3, GSharp3, A3, ASharp3, B3, C4, CSharp4, D4, DSharp4, E4, F4, FSharp4, G4, GSharp4, A4, ASharp4, B4, C5, CSharp5, D5, DSharp5, E5, F5, FSharp5, G5, GSharp5, A5, ASharp5, B5, C6, CSharp6, D6, DSharp6, E6, F6, FSharp6, G6, GSharp6, A6, ASharp6, B6, C7, CSharp7, D7, DSharp7, E7, F7, FSharp7, G7, GSharp7, A7, ASharp7, B7, C8, CSharp8, D8, DSharp8, E8, F8, FSharp8, G8, GSharp8, A8, ASharp8, B8, C9, CSharp9, D9, DSharp9, E9, F9, FSharp9, G9
	}
}
