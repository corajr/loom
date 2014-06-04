package org.chrisjr.loom.mappings;

public class ObjectMapping<T> implements Mapping<T> {
	final T[] objects;

	@SuppressWarnings("unchecked")
	public ObjectMapping(T... objects) {
		this.objects = objects;
	}

	public T call(double value) {
		int i = (int) (value * (objects.length - 1));
		return objects[i];
	}
}
