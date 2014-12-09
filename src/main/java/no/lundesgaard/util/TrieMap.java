package no.lundesgaard.util;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.Stream.concat;

public class TrieMap<V> extends AbstractMap<String, V> {
    private Node<V> root = new Node<>();

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
    public V put(String key, V value) {
        return root.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return root.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
            root.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        root.clear();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return new AbstractSet<Entry<String, V>>() {
            @Override
            public Iterator<Entry<String, V>> iterator() {
                return new Iterator<Entry<String, V>>() {
                    Iterator<Node<V>> iterator = root.stream()
                            .filter(Node::hasValue)
                            .iterator();
                    Node<V> currentNode;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Entry<String, V> next() {
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

    private class Node<U> implements Map.Entry<String, U> {
        private final Node<U> parent;
        private final Character character;
        private Map<Character, Node<U>> nodes = new HashMap<>();
        private Optional<U> optionalValue;

        Node() {
            this.parent = null;
            this.character = null;
        }

        Node(Node<U> parent, char character) {
            this.parent = parent;
            this.character = character;
        }

        int size() {
            return (int) stream().filter(Node::hasValue).count();
        }

        boolean isEmpty() {
            return size() == 0;
        }

        boolean containsKey(Object key) {
            if (key != null && !(key instanceof String)) {
                return false;
            }
            Optional<Node<U>> optionalNode = findNode((String) key);
            return optionalNode.isPresent() && optionalNode.get().hasValue();
        }

        boolean hasValue() {
            return optionalValue != null;
        }

        U get(Object key) {
            if (key != null && !(key instanceof String)) {
                return null;
            }
            Optional<Node<U>> optionalNode = findNode((String) key);
            return optionalNode.map(node -> node.getValue()).orElse(null);
        }

        U remove(Object key) {
            if (key != null && !(key instanceof String)) {
                return null;
            }
            Optional<Node<U>> optionalNode = findNode((String) key);
            if (optionalNode.isPresent()) {
                return optionalNode.get().removeValue();
            }
            return null;
        }

        void clear() {
            for (Node<U> node : nodes.values()) {
                node.clear();
            }
            nodes.clear();
            optionalValue = null;
        }

        Stream<Node<U>> stream() {
            Stream<Node<U>> nodeStream = Stream.of(this);
            if (nodes.isEmpty()) {
                return nodeStream;
            }
            return concat(nodeStream, nodes.values().stream().flatMap(node -> node.stream()));
        }

        @Override
        public String getKey() {
            if (parent == null) {
                return null;
            }
            StringBuilder keyBuilder = new StringBuilder();
            Node current = this;
            while (current != root) {
                keyBuilder.append(current.character);
                current = current.parent;
            }
            return keyBuilder.reverse().toString();
        }

        @Override
        public U getValue() {
            if (optionalValue == null) {
                return null;
            }
            return optionalValue.orElse(null);
        }

        @Override
        public U setValue(U value) {
            if (optionalValue == null) {
                optionalValue = Optional.ofNullable(value);
                return null;
            }
            U oldValue = optionalValue.orElse(null);
            optionalValue = Optional.ofNullable(value);
            return oldValue;
        }

        U removeValue() {
            if (optionalValue == null) {
                return null;
            }
            U oldValue = optionalValue.orElse(null);
            optionalValue = null;
            return oldValue;
        }

        U put(String key, U value) {
            Node<U> node = findOrCreateNodes(key).get();
            return node.setValue(value);
        }

        Optional<Node<U>> findOrCreateNodes(String key) {
            return findNode(key, true);
        }

        Optional<Node<U>> findNode(String key) {
            return findNode(key, false);
        }

        Optional<Node<U>> findNode(String key, boolean createNodes) {
            if (key == null && parent == null) {
                return Optional.of(this);
            }
            if (key == null) {
                throw new IllegalArgumentException("null key on non-root node");
            }
            return findNode(key.toCharArray(), createNodes);
        }

        Optional<Node<U>> findOrCreateNodes(char... keyChars) {
            return findNode(keyChars, true);
        }

        Optional<Node<U>> findNode(char... keyChars) {
            return findNode(keyChars, false);
        }

        Optional<Node<U>> findNode(char[] keyChars, boolean createNodes) {
            if (keyChars.length == 0) {
                return Optional.of(this);
            }
            return findNode(first(keyChars), rest(keyChars), createNodes);
        }

        char first(char[] chars) {
            return chars[0];
        }

        char[] rest(char[] chars) {
            if (chars.length == 1) {
                return new char[0];
            }
            return copyOfRange(chars, 1, chars.length);
        }

        Optional<Node<U>> findNode(char firstKeyChar, char[] restKeyChars, boolean createNodes) {
            Node<U> node = nodes.get(firstKeyChar);
            if (node == null && createNodes) {
                return createNodes(firstKeyChar, restKeyChars);
            }
            if (node == null) {
                return Optional.empty();
            }
            if (createNodes) {
                return node.findOrCreateNodes(restKeyChars);
            }
            return node.findNode(restKeyChars);
        }

        Optional<Node<U>> createNodes(char firstKeyChar, char[] restKeyChars) {
            Node<U> node = new Node<>(this, firstKeyChar);
            nodes.put(firstKeyChar, node);
            if (restKeyChars.length == 0) {
                return Optional.of(node);
            }
            return node.createNodes(first(restKeyChars), rest(restKeyChars));
        }

        boolean containsValue(Object value) {
            boolean containsValue = optionalValue != null
                    && (value == null && !optionalValue.isPresent()
                    || optionalValue.filter(v -> v.equals(value)).isPresent());
            return containsValue || nodes.values().stream().anyMatch(node -> node.containsValue(value));
        }

        public String toString() { return getKey() + "=" + getValue(); }

        public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(getKey(), e.getKey()) &&
                        Objects.equals(getValue(), e.getValue()))
                    return true;
            }
            return false;
        }
    }
}
