package org.chrisjr.loom;

import org.chrisjr.loom.continuous.*;
import org.chrisjr.loom.time.Interval;

public class ContinuousPattern extends Pattern implements Cloneable {
	
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
				Interval now = getCurrentInterval();
				value = function.call(now.getStart());
				value += function.call(now.getEnd());
				value /= 2.0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	@Override
	public ContinuousPattern clone() throws CloneNotSupportedException {
		return (ContinuousPattern) super.clone();
	}
}
