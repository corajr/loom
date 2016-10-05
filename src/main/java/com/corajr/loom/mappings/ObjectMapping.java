package com.corajr.loom.mappings;

/**
 * Maps from numerical values to objects of a specified type. The object will be
 * selected by finding the floor of (value * (length - 1)) and using the result
 * as an index into the input array.
 * 
 * @param <T>
 *            the type of object to return
 */
public class ObjectMapping<T> implements Mapping<T> {
	T[] objects;

	@SuppressWarnings("unchecked")
	public ObjectMapping(T... objects) {
		this.objects = objects;
	}

	@Override
	public T call(double value) {
		int i = (int) (value * (objects.length - 1));
		return objects[i];
	}
}
