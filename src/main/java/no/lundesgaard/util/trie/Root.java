package no.lundesgaard.util.trie;

import static no.lundesgaard.util.trie.Keys.partialKeysFromKey;

import java.util.Optional;

public final class Root<K, P, V> extends Node<K, V> {
	protected boolean isRoot() {
		return true;
	}

	public boolean containsKey(Object key) {
		Optional<Node<K, V>> optionalNode = findNode(key);
		return optionalNode.isPresent() && optionalNode.get().hasValue();
	}

	@SuppressWarnings("Convert2MethodRef")
	public V get(Object key) {
		Optional<Node<K, V>> optionalNode = findNode(key);
		return optionalNode.map(node -> node.getValue()).orElse(null);
	}

	public V put(K key, V value) {
		Node<K, V> node = findOrCreateNodes(key).get();
		return node.setValue(value);
	}

	public V remove(Object key) {
		Optional<Node<K, V>> optionalNode = findNode(key);
		if (optionalNode.isPresent()) {
			return optionalNode.get().removeValue();
		}
		return null;
	}
	
	public int size() {
		return super.size();
	}
	
	public boolean isEmpty() {
		return super.isEmpty();
	}
	
	public void clear() {
		super.clear();
	}
	
	private Optional<Node<K, V>> findNode(Object key) {
		return findNode(partialKeysFromKey(key));
	}

	private Optional<Node<K, V>> findOrCreateNodes(K key) {
		return findOrCreateNodes(partialKeysFromKey(key));
	}
}
