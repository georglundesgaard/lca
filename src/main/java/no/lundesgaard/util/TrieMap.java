package no.lundesgaard.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import no.lundesgaard.util.trie.Node;
import no.lundesgaard.util.trie.Root;

public class TrieMap<K, P, V> extends AbstractMap<K, V> {
	private Root<K, P, V> root = new Root<>();

	@Override
	public int size() {
		return root.size();
	}

	@Override
	public boolean isEmpty() {
		return root.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return root.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return root.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return root.get(key);
	}

	@Override
	public V put(K key, V value) {
		return root.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return root.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.forEach(root::put);
	}

	@Override
	public void clear() {
		root.clear();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new AbstractSet<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return new Iterator<Entry<K, V>>() {
					Iterator<Node<K, V>> iterator = root.stream()
							.filter(Node::hasValue)
							.iterator();
					Node<K, V> currentNode;

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Entry<K, V> next() {
						currentNode = iterator.next();
						return currentNode;
					}

					@Override
					public void remove() {
						if (currentNode == null) {
							throw new IllegalStateException("no current entry");
						}
						currentNode.removeValue();
						currentNode = null;
					}
				};
			}

			@Override
			public int size() {
				return root.size();
			}
		};
	}

}
