package com.corajr.loom.recording;

import oscP5.OscBundle;

public class TaggedOscBundle extends OscBundle {
	public TaggedOscBundle(OscBundle other) {
		for (int i = 0; i < other.size(); i++) {
			add(other.getMessage(i));
		}
	}

	@Override
	public void setTimetag(long timetag) {
		this.timetag = timetag;
	}
}
