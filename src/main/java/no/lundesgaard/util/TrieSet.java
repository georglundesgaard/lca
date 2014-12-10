package no.lundesgaard.util;

import java.util.AbstractSet;
import java.util.Iterator;

public class TrieSet<E> extends AbstractSet<E> {
	private TrieMap<E, Object> map = new TrieMap<>();

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean add(E e) {
		if (map.containsKey(e)) {
			return false;
		}
		map.put(e, null);
		return true;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean remove(Object o) {
		if (map.containsKey(o)) {
			map.remove(o);
			return true;
		}
		return false;
	}
}
