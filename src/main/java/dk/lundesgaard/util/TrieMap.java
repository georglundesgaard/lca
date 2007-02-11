/*
 * Copyright 2007 Georg Lundesgaard (georg@lundesgaard.dk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package dk.lundesgaard.util;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class TrieMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient Node root = new Node((char) 0, null, null, null, null);
	private transient int size;
	private transient int modCount;
	private transient Set<Entry<K, V>> entrySet;
	private transient Set<K> keySet;
	private transient Collection<V> values;
	
	@Override
	public void clear() {
		root.clear();
		size = 0;
		modCount = 0;
	}

	@Override
	public boolean containsKey(Object key) {
		Node node = getNode(key);
		return node != null && node.hasEntry();
	}

	@Override
	public boolean containsValue(Object value) {
		ValueIterator valueIterator = new ValueIterator();
		while (valueIterator.hasNext()) {
			Object currentValue = valueIterator.next();
			if (currentValue == null && value == null) {
				return true;
			}
			if (value != null && value.equals(currentValue)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> es = entrySet;
		return es != null ? es : (entrySet = new EntrySet());
	}
	
	public V get(Object key) {
		Node node = getNode(key);
		if (node != null && node.hasEntry()) {
			return node.entry.getValue();
		}
		return null;
	}

	public Set<K> keySet() {
		Set<K> ks = keySet;
		return ks != null ? ks : (keySet = new KeySet());
	}
	
	public V put(K key, V value) {
		if (key == null) {
			return createEntry(root, key, value);
		}
		String keyString = objectToString(key);
		if (keyString == null) {
			return createEntry(root, key, value);
		}
		if (root.firstChild == null) {
			Node node = createNodePath(root, keyString, 0);
			createEntry(node, key, value);
			return null;
		}
		return put(root.firstChild, keyString, 0, key, value);
	}

	public V remove(Object key) {
		if (key == null) {
			return remove(root);
		}
		String keyString = objectToString(key);
		if (keyString == null) {
			return remove(root);
		}
		Node node = getNode(root.firstChild, keyString, 0);
		if (node != null) {
			return remove(node);
		}
		return null;
	}
	
	private V remove(Node node) {
		if (node.hasEntry()) {
			return removeEntry(node);
		}
		return null;
	}

	public int size() {
		return size;
	}

	public Collection<V> values() {
		Collection<V> v = values;
		return v != null ? v : (values = new Values());
	}
	
	private Node getNode(Object key) {
		if (key == null) {
			return root;
		}
		String keyString = objectToString(key);
		if (keyString == null) {
			return root;
		}
		return getNode(root.firstChild, keyString, 0);
	}
	
	private Node getNode(Node current, String keyString, int position) {
		Node node = current;
		for (int i = position; i < keyString.length();) {
			if (node == null) {
				node = null;
				break;
			}
			char keyPart = keyString.charAt(i);
			if (keyPart < node.keyPart) {
				node = null;
				break;
			}
			if (keyPart > node.keyPart) {
				node = node.nextSibling;
			}
			else {
				if (i + 1 < keyString.length()) {
					node = node.firstChild;
				}
				i++;
			}
		}
		return node;
	}
	
	private V put(Node current, String keyString, int position, K key, V value) {
		char keyPart = keyString.charAt(position);
		if (keyPart < current.keyPart || keyPart > current.keyPart) {
			if (keyPart < current.keyPart) {
				Node previous = current.previous;
				Node node = new Node(keyPart, current, null, previous, null);
				if (previous.firstChild == current) {
					previous.firstChild = node;
				}
				else {
					previous.nextSibling = node;
				}
				current.previous = node;
				if (position + 1 < keyString.length()) {
					node = createNodePath(node,  keyString, position + 1);
				}
				return createEntry(node, key, value);
			}
			else if (current.nextSibling == null) {
				Node node = new Node(keyPart, null, null, current, null);
				current.nextSibling = node;
				if (position + 1 < keyString.length()) {
					node = createNodePath(node, keyString, position + 1);
				}
				return createEntry(node, key, value);
			}
			else {
				return put(current.nextSibling, keyString, position, key, value);
			}
		}

		if (position + 1 == keyString.length()) {
			return createEntry(current, key, value);
		}
		if (current.firstChild == null) {
			Node node = createNodePath(current, keyString, position + 1);
			return createEntry(node, key, value);
		}
		return put(current.firstChild, keyString, position + 1, key, value);
	}
	
	private Node createNodePath(Node previous, String keyString, int position) {
		Node node = new Node(keyString.charAt(position), null, null, previous, null);
		previous.firstChild = node;
		for (int i = position + 1; i < keyString.length(); i++) {
			previous = node;
			node = new Node(keyString.charAt(i), null, null, previous, null);
			previous.firstChild = node;
		}
		return node;
	}
	
	private V createEntry(Node current, K key, V value) {
		Entry<K, V> oldEntry = current.entry;
		current.entry = new SimpleEntry<K, V>(key, value);
		modCount++;
		if (oldEntry != null) {
			return oldEntry.getValue();
		}
		size++;
		return null;
	}
	
	private V removeEntry(Node current) {
		Entry<K, V> oldEntry = current.entry;
		current.entry = null;
		removeNode(current);
		size--;
		modCount++;
		if (oldEntry != null) {
			return oldEntry.getValue();
		}
		return null;
	}
	
	private void removeNode(Node current) {
		if (current.hasChildren() == false) {
			Node previous = current.previous;
			if (previous == null) {
				// root node is not deleted
				return;
			}

			// move references
			if (previous.firstChild == current) {
				previous.firstChild = current.nextSibling;
			}
			else {
				previous.nextSibling = current.nextSibling;
			}
			
			if (current.hasSiblings()) {
				// current node is removed for sibling list
				current.nextSibling.previous = previous;
				return;
			}
			
			if (previous.hasEntry() == false) {
				// continue node removal when previous node has no entry
				removeNode(previous);
			}
		}
	}
	
	private String objectToString(Object o) {
		String s = o.toString();
		if (s.length() == 0) {
			return null;
		}
		return s;
	}
	
	private Node getFirstEntryNode() {
		if (root.hasEntry()) {
			return root;
		}
		return getNextEntryNode(root);
	}
	
	private Node getNextEntryNode(Node current) {
		if (current.hasChildren()) {
			if (current.firstChild.hasEntry()) {
				return current.firstChild;
			}
			return getNextEntryNode(current.firstChild);
		}
		if (current.hasSiblings()) {
			if (current.nextSibling.hasEntry()) {
				return current.nextSibling;
			}
			return getNextEntryNode(current.nextSibling);
		}
		Node parent = getAncestorWithSiblings(current);
		if (parent != null && parent.hasSiblings()) {
			if (parent.nextSibling.hasEntry()) {
				return parent.nextSibling;
			}
			return getNextEntryNode(parent.nextSibling);
		}
		return null;
	}
	
	private Node getAncestorWithSiblings(Node node) {
		Node previous = node.previous;
		if (previous == null) {
			return null;
		}
		if (previous.firstChild == node && previous.hasSiblings()) {
			return previous;
		}
		return getAncestorWithSiblings(previous);
	}
	
	private class Node implements Serializable {
		private static final long serialVersionUID = 1L;
		
		char keyPart;
		Node nextSibling;
		Node firstChild;
		Node previous;
		Entry<K, V> entry;
		
		Node(char keyPart, Node nextSibling, Node firstChild, Node previous, Entry<K, V> entry) {
			this.keyPart = keyPart;
			this.nextSibling = nextSibling;
			this.firstChild = firstChild;
			this.previous = previous;
			this.entry = entry;
		}
		
		boolean hasEntry() {
			return entry != null;
		}
		
		boolean hasChildren() {
			return firstChild != null;
		}
		
		boolean hasSiblings() {
			return nextSibling != null;
		}
		
		void clear() {
			// TODO: recursive clear?
			nextSibling = null;
			firstChild = null;
			previous = null;
			entry = null;
		}
		
		public String toString() {
			return keyPart + ", " + entry;
		}
	}
	
	private abstract class AbstractEntryIterator<T> implements Iterator<T> {
		private Node next;
		private Node lastReturned;
		private int expectedModCount;
		
		public AbstractEntryIterator() {
			next = getFirstEntryNode();
			lastReturned = null;
			expectedModCount = modCount;
		}
		
		public boolean hasNext() {
			return next != null;
		}
		
		protected Entry<K, V> nextEntry() {
			Node current = lastReturned = next;
			if (current == null) {
				throw new NoSuchElementException();
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			next = getNextEntryNode(current);
			return current.entry;
		}
		
		public void remove() {
			if (lastReturned == null) {
				throw new IllegalStateException();
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			removeEntry(lastReturned);
			expectedModCount = modCount;
            lastReturned = null;
		}
	}
	
	private class EntryIterator extends AbstractEntryIterator<Entry<K, V>> {
		public Entry<K, V> next() {
			return nextEntry();
		}
	}
	
	private class KeyIterator extends AbstractEntryIterator<K> {
		public K next() {
			return nextEntry().getKey();
		}
	}
	
	private class ValueIterator extends AbstractEntryIterator<V> {
		public V next() {
			return nextEntry().getValue();
		}
	}
	
	private class EntrySet extends AbstractSet<Entry<K, V>>{
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new EntryIterator();
		}
		
		@Override
		public int size() {
			return size;
		}
		
		@Override
		public boolean contains(Object o) {
			if (o == null || o instanceof Entry == false) {
				return false;
			}
			Entry otherEntry = (Entry) o;
			Node node = getNode(otherEntry.getKey());
			if (node == null || node.hasEntry() == false) {
				return false;
			}
			return node.entry.equals(otherEntry);
		}
		
		@Override
		public boolean remove(Object o) {
			if (o == null || o instanceof Entry == false) {
				return false;
			}
			Entry otherEntry = (Entry) o;
			Node node = getNode(otherEntry.getKey());
			if (node == null || node.hasEntry() == false) {
				return false;
			}
			removeEntry(node);
			return true;
		}
		
		@Override
		public void clear() {
			TrieMap.this.clear();
		}
	}
	
	private class KeySet extends AbstractSet<K>{
		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}
		
		@Override
		public int size() {
			return size;
		}
		
		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}
		
		@Override
		public boolean remove(Object o) {
			Node node = getNode(o);
			if (node == null || node.hasEntry() == false) {
				return false;
			}
			removeEntry(node);
			return true;
		}
		
		@Override
		public void clear() {
			TrieMap.this.clear();
		}
	}
	
    private class Values extends AbstractCollection<V> {
    	public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return size;
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public boolean remove(Object o) {
        	for (Node node = getFirstEntryNode(); node != null; node = getNextEntryNode(node)) {
        		Entry<K, V> entry = node.entry;
            	V value = entry.getValue();
            	if (value == null && o == null || value != null && value.equals(o)) {
            		removeEntry(node);
            		return true;
            	}
            }
            return false;
        }

        public void clear() {
            TrieMap.this.clear();
        }
    }
}
