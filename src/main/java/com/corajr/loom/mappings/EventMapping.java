package com.corajr.loom.mappings;

import com.corajr.loom.LEvent;

/**
 * Allows the user to call a mapping using an event instead of a value. Useful
 * when the event that triggered this call has additional information (the
 * interval over which it applies, or a reference back to another event with a
 * more meaningful value).
 * 
 * @param <T>
 *            the type to return
 */
public interface EventMapping<T> {
	public T call(LEvent event);
}
