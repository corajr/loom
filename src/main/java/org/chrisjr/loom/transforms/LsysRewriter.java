package org.chrisjr.loom.transforms;

import java.util.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Event;
import org.chrisjr.loom.EventCollection;
import org.chrisjr.loom.time.Interval;

public class LsysRewriter extends EventRewriter {
	public int generations;
	public String alphabet;

	public static class LsysRule extends Rule {
		double matchOn;
		double[] replaceWith;

		public LsysRule(double matchOn, double[] replaceWith) {
			this.matchOn = matchOn;
			this.replaceWith = replaceWith;
		}

		@Override
		public boolean canApply(int index, Event event) {
			return event.getValue() == matchOn;
		}

		@Override
		public Collection<Event> apply(int index, Event event) {
			ArrayList<Event> newEvents = new ArrayList<Event>();
			Interval oldInterval = event.getInterval();
			BigFraction start = oldInterval.getStart();
			BigFraction newSize = oldInterval.getSize().divide(
					replaceWith.length);

			Interval interval = new Interval(start, start.add(newSize));

			for (double value : replaceWith) {
				newEvents.add(new Event(interval, value));
				interval = interval.add(newSize);
			}

			return newEvents;
		}
	}

	public LsysRewriter(String... ruleStrings) {
		this(1, ruleStrings);
	}

	public LsysRewriter(int generations, String... ruleStrings) {
		this(generations, getAlphabetFrom(ruleStrings), makeRules(ruleStrings));
	}

	public LsysRewriter(int generations, String alphabet, ArrayList<Rule> rules) {
		super(rules);
		this.alphabet = alphabet;
		this.generations = generations;
	}

	public static LsysRewriter.LsysRule ruleFrom(String alphabet, String rule) {
		String[] in_out = rule.split("->");
		String in = in_out[0];
		String out = in_out[1];
		return new LsysRewriter.LsysRule(toDoubles(alphabet, in)[0], toDoubles(
				alphabet, out));
	}

	public static double[] toDoubles(String alphabet, String s) {
		double[] values = new double[s.length()];
		for (int i = 0; i < values.length; i++) {
			values[i] = (double) alphabet.indexOf(s.charAt(i))
					/ (alphabet.length() - 1);
		}
		return values;
	}

	public String fromDouble(double value) {
		int i = (int) (value * (alphabet.length() - 1));
		return alphabet.substring(i, i + 1);
	}

	public static String getAlphabetFrom(String... ruleStrings) {
		Set<Character> symbols = new TreeSet<Character>();
		for (String ruleString : ruleStrings) {
			for (String s : ruleString.split("->")) {
				for (char c : s.toCharArray()) {
					symbols.add(c);
				}
			}
		}

		String alphabet = "";
		for (Character c : symbols) {
			alphabet += c;
		}
		return alphabet;
	}

	public static ArrayList<Rule> makeRules(String[] strings) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		String alphabet = getAlphabetFrom(strings);
		for (String rule : strings) {
			rules.add(ruleFrom(alphabet, rule));
		}
		return rules;
	}

	@Override
	public EventCollection apply(EventCollection original) {
		EventCollection events = original;
		for (int i = 0; i < generations; i++) {
			events = super.apply(events);
		}
		return events;
	}
}
