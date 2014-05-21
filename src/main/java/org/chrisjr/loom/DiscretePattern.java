package org.chrisjr.loom;

public class DiscretePattern extends Pattern {
	public EventCollection events = new EventCollection();
	
	public DiscretePattern(Loom loom) {
		super(loom);
	}
	
	/**
	 * @param string
	 *            a string such as "10010010" describing a pattern
	 * @return the updated pattern
	 */
	public Pattern extend(String string) {
		return this;
	}	
	public double getValue() {
		return 0.0;
	}

}
