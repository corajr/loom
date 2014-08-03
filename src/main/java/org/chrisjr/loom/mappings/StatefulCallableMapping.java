package org.chrisjr.loom.mappings;

import org.chrisjr.loom.util.StatefulCallable;

public class StatefulCallableMapping extends ObjectMapping<StatefulCallable> {

	public StatefulCallableMapping(StatefulCallable... objects) {
		this.objects = objects;
	}

	public StatefulCallable getNoop() {
		return objects[0];
	}

	@Override
	public StatefulCallable call(double value) {
		if (value < 1.0)
			value += 1.0 / objects.length;
		return super.call(value);
	}

}
