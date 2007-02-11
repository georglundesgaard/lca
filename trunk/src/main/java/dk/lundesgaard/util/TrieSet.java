package dk.lundesgaard.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class TrieSet<T> extends AbstractSet<T> implements Set<T>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private TrieMap<T, Object> map;
	
	@Override
	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean add(T e) {
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
