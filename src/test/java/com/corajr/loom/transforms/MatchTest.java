package com.corajr.loom.transforms;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.corajr.loom.*;
import com.corajr.loom.transforms.EventRewriter;
import com.corajr.loom.transforms.MatchRewriter;

public class MatchTest {

	@Test
	public void matchRewrite() {
		EventCollection events = EventCollection.fromString("01234");
		EventRewriter rewriter = new MatchRewriter(0.5);
		EventCollection transformed = rewriter.apply(events);
		assertThat(transformed.size(), is(equalTo(1)));
		LEvent e = transformed.values().iterator().next();
		assertThat(e.getInterval().getStart().doubleValue(), is(equalTo(0.4)));
		assertThat(e.getValue(), is(equalTo(0.5)));
	}
}
