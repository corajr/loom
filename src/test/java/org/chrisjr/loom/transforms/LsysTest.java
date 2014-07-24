package org.chrisjr.loom.transforms;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.*;
import org.chrisjr.loom.time.*;
import org.junit.Test;

public class LsysTest {

	@Test
	public void ruleStringToDoubles() {
		String ruleString = "A->AB";
		String alphabet = LsysRewriter.getAlphabetFrom(ruleString);

		assertThat(alphabet, is(equalTo("AB")));

		LsysRewriter.LsysRule rule = LsysRewriter
				.ruleFrom(alphabet, ruleString);
		assertThat(rule.matchOn, is(equalTo(0.0)));
		assertThat(rule.replaceWith[0], is(equalTo(0.0)));
		assertThat(rule.replaceWith[1], is(equalTo(1.0)));
	}

	@Test
	public void algae() {
		EventCollection events = new EventCollection();
		events.add(new Event(new Interval(0, 1), 0.0));

		LsysRewriter rewriter = new LsysRewriter("A->AB", "B->A");
		EventCollection oneGen = rewriter.apply(events);
		assertThat(oneGen.size(), is(equalTo(2)));

		int[] sizes = new int[] { 1, 2, 3, 5, 8, 13, 21, 34 };
		for (int i = 0; i < 7; i++) {
			rewriter.generations = i;
			EventCollection gens = rewriter.apply(events);
			assertThat(gens.size(), is(equalTo(sizes[i])));
		}
	}

	@Test
	public void fromDouble() {
		LsysRewriter rewriter = new LsysRewriter("A->AB", "B->A");
		assertThat(rewriter.alphabet, is(equalTo("AB")));
		assertThat(rewriter.fromDouble(0.0), is(equalTo("A")));
		assertThat(rewriter.fromDouble(1.0), is(equalTo("B")));
	}
}
