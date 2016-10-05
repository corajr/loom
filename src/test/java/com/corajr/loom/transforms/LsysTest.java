package com.corajr.loom.transforms;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.junit.Test;

import com.corajr.loom.*;
import com.corajr.loom.mappings.TurtleDraw;
import com.corajr.loom.mappings.TurtleDrawCommand;
import com.corajr.loom.transforms.LsysRewriter;

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
		EventCollection events = EventCollection.fromString("0");

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

	public String eventsToString(LsysRewriter rewriter, Collection<LEvent> events) {
		StringBuilder sb = new StringBuilder();
		for (LEvent e : events) {
			sb.append(rewriter.fromDouble(e.getValue()));
		}

		return sb.toString();
	}

	@Test
	public void getAxiom() {
		LsysRewriter rewriter = new LsysRewriter("X->F-[[X]+X]+F[+FX]-X",
				"F->FF");
		EventCollection axiom = rewriter.makeAxiom("X");

		assertThat(axiom.size(), is(equalTo(1)));
		assertThat(eventsToString(rewriter, axiom.values()), is(equalTo("X")));
	}

	@Test
	public void moreComplex() {
		LsysRewriter rewriter = new LsysRewriter("X->X+YF", "Y->FX-Y");

		EventCollection gen0 = rewriter.makeAxiom("FX");

		String gen0s = eventsToString(rewriter, gen0.values());

		assertThat(gen0s.length(), is(equalTo(2)));
		EventCollection gen1 = rewriter.apply(gen0);

		String gen1s = eventsToString(rewriter, gen1.values());
		assertThat(gen1s.length(), is(equalTo(5)));
	}

	@Test
	public void manyGenerations() {
		LsysRewriter lsys = new LsysRewriter("X->F-[[X]+X]+F[+FX]-X", "F->FF");
		EventCollection axiom = lsys.makeAxiom("X");

		lsys.generations = 5;
		lsys.apply(axiom);
	}

	@Test
	public void drawCommandAdd() {
		LsysRewriter lsys = new LsysRewriter("X->X+YF", "Y->FX-Y");
		lsys.setCommand("F", TurtleDraw.forward(10));
		lsys.setCommand("+", TurtleDraw.turn((float) Math.PI / 2));
		lsys.setCommand("-", TurtleDraw.turn((float) -Math.PI / 2));

		TurtleDrawCommand[] commands = lsys.getTurtleDrawCommands();
		assertThat(commands.length, is(equalTo(5)));
	}
}
