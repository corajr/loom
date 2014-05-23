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

import org.chrisjr.loom.time.*;

import processing.core.*;

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

	private Scheduler scheduler;

	public final static String VERSION = "##library.prettyVersion##";

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public Loom(PApplet theParent) {
		this(theParent, new RealTimeScheduler());
	}

	/**
	 * Constructor for a new Loom with a particular type of scheduling
	 * 
	 * @param theParent
	 *            parent Processing sketch
	 * @param theScheduler
	 *            choose real-time or non-real-time scheduling
	 */
	public Loom(PApplet theParent, Scheduler theScheduler) {
		myParent = theParent;
		scheduler = theScheduler;
		scheduler.setPatterns(patterns);
		welcome();
	}

	private void welcome() {
		System.out
				.println("##library.name## ##library.prettyVersion## by ##author##");
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
}