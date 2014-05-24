package org.chrisjr.loom;

import org.chrisjr.loom.continuous.*;

public class ContinuousPattern extends Pattern {
	
	ContinuousFunction function = null;

	public ContinuousPattern(Loom loom) {
		super(loom);
	}

	public ContinuousPattern(Loom loom, double _defaultValue) {
		super(loom, _defaultValue);
	}
	
	public ContinuousPattern(Loom loom, ContinuousFunction _function) {
		super(loom);
		function = _function;		
	}
	
	public double getValue() {
		double value = defaultValue;
		if (function != null) {
			try {
				// midpoint of function within current interval
				value = function.call(myLoom.getCurrentInterval().getStart());
				value += function.call(myLoom.getCurrentInterval().getStart());
				value /= 2.0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}
}
