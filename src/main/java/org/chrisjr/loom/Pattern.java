package org.chrisjr.loom;

import java.awt.Color;
import java.util.*;

/**
 * @author chrisjr
 * 
 *         The base class for patterns in Loom. Patterns may be discrete or
 *         continuous.
 * 
 */
public class Pattern {
	Loom myLoom;

	private String patternString = "";
	private Map<String, Object> outputMappings;

	/**
	 * Constructor for an empty Pattern.
	 * 
	 * @param loom
	 *            the loom that holds this pattern (can be null)
	 */
	public Pattern(Loom loom) {
		this(loom, "");
	}

	/**
	 * Constructor for a Pattern initialized by a string.
	 * 
	 * @param loom
	 *            the loom that holds this pattern (can be null)
	 * @param string
	 *            a string declaring the pattern
	 * 
	 */
	public Pattern(Loom loom, String string) {
		myLoom = loom;
		if (myLoom != null)
			addTo(myLoom);
		patternString = string;
		outputMappings = new HashMap<String, Object>();
	}

	protected void addTo(Loom loom) {
		loom.patterns.add(this);
	}

	/**
	 * @param string
	 *            a string such as "10010010" describing a pattern
	 * @return the updated pattern
	 */
	public Pattern extend(String string) {
		setPatternString(getPatternString() + string);
		return this;
	}

	/**
	 * Set a mapping from the pattern's events to sounds
	 * 
	 * @param instrument
	 *            the name of a MIDI instrument to trigger
	 * @return the updated pattern
	 */
	public Pattern asSound(String instrument) {
		outputMappings.put("sound", instrument);
		return this;
	}

	/**
	 * Set a mapping from the pattern's events to colors
	 * 
	 * @param colors
	 *            a list of colors to represent each state
	 * @return the updated pattern
	 */
	public Pattern asColor(Color... colors) {
		outputMappings.put("color", colors);
		return this;
	}

	public Color asColor() {
		return Color.black;
	}
	
	public Pattern asRunnable(Runnable runnable) {
		outputMappings.put("runnable", runnable);
		return this;
	}
	
	public Runnable asRunnable() {
		return (Runnable) outputMappings.get("runnable");
	}

	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String patternString) {
		this.patternString = patternString;
	}
}
