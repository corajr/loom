/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package org.chrisjr.loom;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.MidiMessage;

import org.chrisjr.loom.recording.*;
import org.chrisjr.loom.time.*;

import processing.core.*;
import oscP5.*;
import themidibus.*;

/**
 * This is a template class and can be used to start a new processing library or
 * tool. Make sure you rename this class as well as the name of the example
 * package 'template' to your own library or tool naming convention.
 * 
 * @example Hello
 * 
 *          (the tag @example followed by the name of an example included in
 *          folder 'examples' will automatically include the example in the
 *          javadoc.)
 * 
 */

public class Loom {
	PApplet myParent;

	public PatternCollection patterns = new PatternCollection();

	private final Scheduler scheduler;

	private OscP5 oscP5 = null;
	private MidiBus myBus = null;

	public final static String VERSION = "##library.prettyVersion##";

	public final static String WELCOME_MESSAGE = "##library.name## ##library.prettyVersion## by ##author##";

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public Loom(PApplet theParent) {
		this(theParent, new RealTimeScheduler(), 1000L);
	}

	public Loom(PApplet theParent, Scheduler scheduler) {
		this(theParent, scheduler, 1000L);
	}

	public Loom(PApplet theParent, int bpm) {
		this(theParent, new RealTimeScheduler(), bpmToPeriod(bpm));
	}

	/**
	 * Constructor for a new Loom with a particular type of scheduling
	 * 
	 * @param theParent
	 *            parent Processing sketch
	 * @param theScheduler
	 *            real-time or non-real-time scheduler
	 * @param periodMillis
	 *            the cycle period in milliseconds
	 */
	public Loom(PApplet theParent, Scheduler theScheduler, long periodMillis) {
		myParent = theParent;
		scheduler = theScheduler;
		scheduler.setPatterns(patterns);
		scheduler.setPeriod(periodMillis);
		welcome();
	}

	private void welcome() {
		if (!WELCOME_MESSAGE.startsWith("##")) // don't print while testing
			System.out.println(WELCOME_MESSAGE);
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	public Interval getCurrentInterval() {
		return scheduler.getCurrentInterval();
	}

	public void play() {
		scheduler.play();
	}

	public void pause() {
		scheduler.pause();
	}

	public void stop() {
		scheduler.stop();
	}

	public void record() throws IOException {
		record("osc", "midi");
	}

	public void record(String oscFilePrefix, String midiFilePrefix)
			throws IOException {
		String timestamp = Long.toString(System.currentTimeMillis());

		File oscFile = null;
		File midiFile = null;

		if (oscFilePrefix != null)
			oscFile = myParent.dataFile(oscFilePrefix + timestamp + ".osc");
		if (midiFilePrefix != null)
			midiFile = myParent.dataFile(midiFilePrefix + timestamp + ".mid");

		record(oscFile, midiFile);
	}

	public void record(File oscFile, File midiFile) throws IOException {
		if (oscFile != null)
			setOscP5(new OscP5Recorder(this, oscFile));
		if (midiFile != null)
			setMidiBus(new MidiBusRecorder(this, midiFile));

		play();
	}

	private static int periodToBpm(long period) {
		return (int) (240000.0 / period);
	}

	private static long bpmToPeriod(int bpm) {
		return (long) (240000.0 / bpm);
	}

	public int getBPM() {
		return periodToBpm(getPeriod());
	}

	public void setBPM(int bpm) {
		setPeriod(bpmToPeriod(bpm));
	}

	public long getPeriod() {
		return scheduler.getPeriod();
	}

	public void setPeriod(long millis) {
		scheduler.setPeriod(millis);
	}

	public OscP5 getOscP5() {
		if (oscP5 == null)
			throw new IllegalStateException("OscP5 not set!");
		return oscP5;
	}

	public void setOscP5(OscP5 oscP5) {
		this.oscP5 = oscP5;
	}

	public MidiBus getMidiBus() {
		// TODO can a MidiBus be created inside here? Is it even necessary?
		if (myBus == null)
			throw new IllegalStateException("MidiBus not set!");
		return myBus;
	}

	public void setMidiBus(MidiBus midiBus) {
		this.myBus = midiBus;
	}

	public void oscEvent(OscMessage theOscMessage) {
		// TODO handle incoming messages
		System.out.println(theOscMessage.addrPattern());
	}

	public void midiMessage(MidiMessage theMidiMessage) {
		// MidiTools.printMidi(theMidiMessage);
	}

	public void rawMidi(byte[] raw) {
		// MidiTools.printMidiRaw(raw);
	}

	public PApplet getParent() {
		return myParent;
	}

	public void dispose() {
		if (oscP5 != null)
			oscP5.dispose();
		if (myBus != null)
			myBus.dispose();
	}
}