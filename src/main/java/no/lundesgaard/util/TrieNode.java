package no.lundesgaard.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.Stream.concat;

public class TrieNode<K, V> {
	private final Map<Byte, TrieNode<K, V>> children = new HashMap<>();
	private Optional<Entry<K, V>> optionalEntry = Optional.empty();

	public Optional<Entry<K, V>> getOptionalEntry() {
		return optionalEntry;
	}

	public Optional<Entry<K, V>> setEntry(K key, V value) {
		return setEntry(new SimpleEntry<>(key, value));
	}

	private Optional<Entry<K, V>> setEntry(Entry<K, V> entry) {
		if (optionalEntry.isPresent()) {
			Optional<Entry<K, V>> oldOptionalEntry = optionalEntry;
			optionalEntry = Optional.ofNullable(entry);
			return oldOptionalEntry;
		}
		optionalEntry = Optional.ofNullable(entry);
		return Optional.empty();
	}

	public Optional<Entry<K, V>> removeEntry() {
		return setEntry(null);
	}

	public boolean hasEntry() {
		return optionalEntry.isPresent();
	}

	public int size() {
		return (int) nodeWithEntryStream().count();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public void clear() {
		children.forEach((key, node) -> node.clear());
		children.clear();
		optionalEntry = Optional.empty();
	}

	public Iterator<TrieNode<K, V>> nodeWithEntryIterator() {
		return nodeWithEntryStream().iterator();
	}

	private Stream<TrieNode<K, V>> nodeWithEntryStream() {
		return stream().filter(TrieNode::hasEntry);
	}

	@SuppressWarnings("Convert2MethodRef")
	private Stream<TrieNode<K, V>> stream() {
		Stream<TrieNode<K, V>> nodeStream = Stream.of(this);
		if (children.isEmpty()) {
			return nodeStream;
		}
		return concat(nodeStream, childrenStream().flatMap(node -> node.stream()));
	}

	private Stream<TrieNode<K, V>> childrenStream() {
		return children.values().stream();
	}

	public Optional<TrieNode<K, V>> findNode(byte[] partialKeys) {
		return findNode(partialKeys, false);
	}

	public Optional<TrieNode<K, V>> findOrCreateNodes(byte[] partialKeys) {
		return findNode(partialKeys, true);
	}

	private Optional<TrieNode<K, V>> findNode(byte[] partialKeys, boolean createNodes) {
		if (partialKeys == null || partialKeys.length == 0) {
			return Optional.of(this);
		}
		return findNode(first(partialKeys), rest(partialKeys), createNodes);
	}

	private byte first(byte[] partialKeys) {
		return partialKeys[0];
	}

	private byte[] rest(byte[] partialKeys) {
		if (partialKeys.length == 1) {
			return new byte[0];
		}
		return copyOfRange(partialKeys, 1, partialKeys.length);
	}

	private Optional<TrieNode<K, V>> findNode(byte firstPartialKey, byte[] restPartialKeys, boolean createNodes) {
		TrieNode<K, V> node = children.get(firstPartialKey);
		if (node == null && createNodes) {
			return createNodes(firstPartialKey, restPartialKeys);
		}
		if (node == null) {
			return Optional.empty();
		}
		if (createNodes) {
			return node.findOrCreateNodes(restPartialKeys);
		}
		return node.findNode(restPartialKeys);
	}

	private Optional<TrieNode<K, V>> createNodes(byte firstPartialKey, byte[] restPartialKeys) {
		TrieNode<K, V> node = new TrieNode<>();
		children.put(firstPartialKey, node);
		if (restPartialKeys.length == 0) {
			return Optional.of(node);
		}
		return node.createNodes(first(restPartialKeys), rest(restPartialKeys));
	}

	public boolean containsValue(Object value) {
		return optionalEntry
				.map(e -> e.getValue() == null && value == null || e.getValue() != null && e.getValue().equals(value))
				.filter(b -> b)
				.orElse(childrenStream().anyMatch(node -> node.containsValue(value)));
	}
}
