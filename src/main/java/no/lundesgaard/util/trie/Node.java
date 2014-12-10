package no.lundesgaard.util.trie;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.Stream.concat;
import static no.lundesgaard.util.trie.Keys.asKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Node<K, V> implements Map.Entry<K, V> {
	private final Node<K, V> parent;
	private final Map<Object, Node<K, V>> children;
	private final Object partialKey;
	private Holder<V> valueHolder;
//	private final Class<K> keyType;
//	private final Class<P> partialKeyType;

	@SuppressWarnings("unchecked")
	protected Node(Node<K, V> parent, Object partialKey) {
		this.parent = parent;
		this.children = new HashMap<>();
		this.partialKey = partialKey;
//		this.keyType = (Class<K>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//		this.partialKeyType = (Class<P>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
	}

	protected Node() {
		this(null, null);
	}

	protected Object getPartialKey() {
		return partialKey;
	}

//	protected Class<P> getPartialKeyType() {
//		return partialKeyType;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public K getKey() {
		if (parent == null) {
			return null;
		}
		Object[] partialKeys = reverseStream().map(Node::getPartialKey).filter(o -> o != null).toArray();
		return asKey(partialKeys);
	}

//	protected Class<K> getKeyType() {
//		return keyType;
//	}

	@Override
	public V getValue() {
		if (valueHolder == null) {
			return null;
		}
		return valueHolder.value;
	}

	@Override
	public V setValue(V value) {
		if (valueHolder == null) {
			valueHolder = new Holder<>();
		}
		V oldValue = valueHolder.value;
		valueHolder.value = value;
		return oldValue;
	}

	public V removeValue() {
		if (valueHolder == null) {
			return null;
		}
		V oldValue = valueHolder.value;
		valueHolder = null;
		return oldValue;
	}

	public boolean hasValue() {
		return valueHolder != null;
	}

	protected int size() {
		return (int) stream().filter(Node::hasValue).count();
	}

	protected boolean isEmpty() {
		return size() == 0;
	}

	protected void clear() {
		children.forEach((key, node) -> node.clear());
		children.clear();
		valueHolder = null;
	}

	@SuppressWarnings("Convert2MethodRef")
	public Stream<Node<K, V>> stream() {
		Stream<Node<K, V>> nodeStream = Stream.of(this);
		if (children.isEmpty()) {
			return nodeStream;
		}
		return concat(nodeStream, childrenStream().flatMap(node -> node.stream()));
	}

	private Stream<Node<K, V>> childrenStream() {
		return children.values().stream();
	}

	public Stream<Node<K, V>> reverseStream() {
		Stream<Node<K, V>> nodeStream = Stream.of(this);
		if (parent == null) {
			return nodeStream;
		}
		return concat(parent.reverseStream(), nodeStream);
	}

	protected Optional<Node<K, V>> findNode(Object[] partialKeys) {
		return findNode(partialKeys, false);
	}

	protected Optional<Node<K, V>> findOrCreateNodes(Object[] partialKeys) {
		return findNode(partialKeys, true);
	}

	private Optional<Node<K, V>> findNode(Object[] partialKeys, boolean createNodes) {
		if (partialKeys == null || partialKeys.length == 0) {
			return Optional.of(this);
		}
		return findNode(first(partialKeys), rest(partialKeys), createNodes);
	}

	private Object first(Object[] partialKeys) {
		return partialKeys[0];
	}

	private Object[] rest(Object[] partialKeys) {
		if (partialKeys.length == 1) {
			return new Object[0];
		}
		return copyOfRange(partialKeys, 1, partialKeys.length);
	}

	private Optional<Node<K, V>> findNode(Object firstPartialKey, Object[] restPartialKeys, boolean createNodes) {
		Node<K, V> node = children.get(firstPartialKey);
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

	private Optional<Node<K, V>> createNodes(Object firstPartialKey, Object[] restPartialKeys) {
		Node<K, V> node = new Node<>(this, firstPartialKey);
		children.put(firstPartialKey, node);
		if (restPartialKeys.length == 0) {
			return Optional.of(node);
		}
		return node.createNodes(first(restPartialKeys), rest(restPartialKeys));
	}

	public boolean containsValue(Object value) {
		return valueHolder != null && (valueHolder.value == null && value == null || valueHolder.value != null && valueHolder.value.equals(value))
				|| childrenStream().anyMatch(node -> node.containsValue(value));
	}

	public int hashCode() {
		return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof Map.Entry) {
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			if (Objects.equals(getKey(), e.getKey()) &&
					Objects.equals(getValue(), e.getValue()))
				return true;
		}
		return false;
	}

	private class Holder<T> {
		T value;
	}
}
