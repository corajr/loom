package org.chrisjr.loom;

import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import netP5.NetAddress;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import oscP5.OscMessage;
import oscP5.OscP5;

public class AsOscMessageTest {
	private OscP5 oscP5;
	private NetAddress myRemoteLocation = new NetAddress("127.0.0.1", 12001);

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	private AtomicInteger eventsCounter = new AtomicInteger();

	@Before
	public void setUp() throws Exception {
		oscP5 = new OscP5(this, 12001);

		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);
		
		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
		oscP5.dispose();
	}

	void oscEvent(OscMessage theOscMessage) {
		eventsCounter.incrementAndGet();
		System.out.print(theOscMessage.addrPattern());
		System.out.print(" ");
		System.out.println(theOscMessage.get(0).intValue());
	}
	
	void waitForEvents(int expected, int timeout) {
		int millisPassed = 0;
		while (eventsCounter.get() < expected && millisPassed < timeout) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			millisPassed++;
		}
		if (eventsCounter.get() < expected) fail("Timed out waiting for events");
	}

	@Ignore
	public void localReceiver() {
		OscMessage myMessage = new OscMessage("/test");
		myMessage.add(123);
		oscP5.send(myMessage, myRemoteLocation);
		
		waitForEvents(1, 100);
	}
	
	@Test
	public void receive() {
		pattern.extend("1101");

		Pattern messagePat = new Pattern(loom);
		messagePat.asOscMessage("/test", 123);

		pattern.addChild(messagePat);
		pattern.asOscBundle(myRemoteLocation);
		
		scheduler.setElapsedMillis(1001);
		waitForEvents(3, 200);
	}
}
