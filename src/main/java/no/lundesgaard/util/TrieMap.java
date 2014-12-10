package no.lundesgaard.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TrieMap<K, V> extends AbstractMap<K, V> {
	private TrieNode<K, V> rootNode = new TrieNode<>();

	public TrieMap() {
	}

	public TrieMap(Map<K, V> map) {
		putAll(map);
	}

	public boolean containsKey(Object key) {
		return findNode(key).map(TrieNode::hasEntry).orElse(false);
	}

	public boolean containsValue(Object value) {
		return rootNode.containsValue(value);
	}

	public V get(Object key) {
		return findNode(key)
				.flatMap(TrieNode::getOptionalEntry)
				.filter(e -> e.getKey() == null && key == null || e.getKey() != null && e.getKey().equals(key))
				.map(Entry::getValue)
				.orElse(null);
	}

	private Optional<TrieNode<K, V>> findNode(Object key) {
		return rootNode.findNode(partialKeysFromKey(key));
	}

	private byte[] partialKeysFromKey(Object key) {
		if (key == null) {
			return null;
		}
		if (key instanceof Trieable) {
			return ((Trieable) key).getBytes();
		}
		return key.toString().getBytes(UTF_8);
	}

	public V put(K key, V value) {
		TrieNode<K, V> node = findOrCreateNodes(key).get();
		return node.setEntry(key, value).map(Entry::getValue).orElse(null);
	}

	private Optional<TrieNode<K, V>> findOrCreateNodes(K key) {
		return rootNode.findOrCreateNodes(partialKeysFromKey(key));
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		map.forEach(this::put);
	}

	public V remove(Object key) {
		return findNode(key)
				.flatMap(TrieNode::removeEntry)
				.map(Entry::getValue)
				.orElse(null);
	}

	public int size() {
		return rootNode.size();
	}

	public boolean isEmpty() {
		return rootNode.isEmpty();
	}

	public void clear() {
		rootNode.clear();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new AbstractSet<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return new Iterator<Entry<K, V>>() {
					Iterator<TrieNode<K, V>> iterator = rootNode.nodeWithEntryIterator();
					TrieNode<K, V> currentNode;

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Entry<K, V> next() {
						currentNode = iterator.next();
						return currentNode.getOptionalEntry().get();
					}

					@Override
					public void remove() {
						if (currentNode != null) {
							currentNode.removeEntry();
							currentNode = null;
						} else {
							throw new IllegalStateException("no current entry");
						}
					}
				};
			}

			@Override
			public int size() {
				return rootNode.size();
			}
		};
	}
}
