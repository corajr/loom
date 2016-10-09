package com.corajr.loom.mappings;

import com.corajr.loom.LEvent;
import com.corajr.loom.util.StatefulCallable;

/**
 * Returns the appropriate callable when an extra StatefulNoop has been added at
 * the start. This ensures that patterns that provide the same value twice upon
 * two separate onsets will have their callable called twice. Used primarily
 * with {@link TurtleDrawCommand} where repeated instructions may have a
 * different outcome.
 * 
 * 
 * @see com.corajr.loom.Pattern#asTurtleDrawCommand(TurtleDrawCommand...)
 * @author corajr
 */
public class StatefulCallableMapping extends ObjectMapping<StatefulCallable>
		implements EventMapping<StatefulCallable> {

	public StatefulCallableMapping(StatefulCallable... objects) {
		this.objects = objects;
	}

	public StatefulCallable getNoop() {
		return objects[0];
	}

	@Override
	public StatefulCallable call(LEvent event) {
		LEvent parentEvent = event.getParentEvent();
		if (parentEvent == null)
			return null;

		return event.getValue() == 1.0 ? call(parentEvent.getValue())
				: getNoop();
	}

	@Override
	public StatefulCallable call(double value) {
		value *= (objects.length - 2);
		value += 1.0;
		value /= (objects.length - 1);

		return super.call(value);
	}

}
