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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class TrieMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Serializable {
	private static final long serialVersionUID = 1L;
	private static final String EMPTY_STRING = "";
	
	private transient Entry<K, V> root = null;
	private transient int size;
	private transient int modCount;

	@Override
	public void clear() {
		root = null;
		size = 0;
		modCount = 0;
	}

	@Override
	public boolean containsKey(Object key) {
		Entry<K,V> entry = getEntry(root, objectToString(key), 0);
		return entry != null && entry.isEntry();
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
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}
	
	public V get(Object key) {
		Entry<K, V> entry = getEntry(key);
		if (entry != null) {
			return entry.getValue();
		}
		return null;
	}

	public Set<K> keySet() {
		return new KeySet();
	}
	
	public V put(K key, V value) {
		String keyString = objectToString(key);
		return put(keyString, key, value);
	}

	public V remove(Object key) {
		String keyString = objectToString(key);
		if (keyString.length() == 0) {
			if (root != null && root.keyPart == (char) 0) {
				return removeEntry(root);
			}
			return null;
		}
		return removeEntry(root, keyString, 0);
	}

	public int size() {
		return size;
	}

	public Collection<V> values() {
		return new Values();
	}
	
	private Entry<K, V> getEntry(Object key) {
		if (root == null) {
			return null;
		}
		String keyString = objectToString(key);
		if (keyString.length() == 0) {
			if (root.keyPart != (char) 0) {
				return null;
			}
			return root;
		}
		return getEntry(root, keyString, 0);
	}
	
	private Entry<K, V> getEntry(Entry<K, V> current, String keyString, int offset) {
		if (current == null || keyString.charAt(offset) < current.keyPart) {
			return null;
		}
		
		if (keyString.charAt(offset) > current.keyPart) {
			return getEntry(current.nextSibling, keyString, offset);
		}
		if (offset == keyString.length() - 1) {
			return current;
		}
		return getEntry(current.firstChild, keyString, offset + 1);
	}
	
	private V put(String keyString, K key, V value) {
		if (keyString.length() == 0) {
			if (root == null) {
				root = createEntry((char) 0, null, null, null, key, value);
			}
			if (root.keyPart == (char) 0) {
				root = createEntry((char) 0, root.nextSibling, root.firstChild, null, key, value);
			}
			else {
				root = createEntry((char) 0, root, null, null, key, value); 
			}
		}
		if (root == null) {
			root = createEntryPath(keyString, 0, key, value);
		}
		return put(root, keyString, 0, key, value);
	}
	
	private V put(Entry<K, V> current, String keyString, int offset, K key, V value) {
		char keyPart = keyString.charAt(offset);
		if (keyPart < current.keyPart || keyPart > current.keyPart) {
			if (keyPart < current.keyPart) {
				Entry<K, V> entry = createEntryPath(keyString, offset, key, value);
				entry.nextSibling = current;
				current.previous = entry;
			}
			else if (current.nextSibling == null) {
				Entry<K, V> entry = createEntryPath(keyString, offset, key, value);
				entry.previous = current;
				current.nextSibling = entry;
			}
			else {
				return put(current.nextSibling, keyString, offset, key, value);
			}
			return null;
		}

		if (offset == keyString.length() - 1) {
			V oldValue = current.getValue();
			createEntry(current, key, value);
			return oldValue;
		}
		if (current.firstChild == null) {
			Entry<K, V> entry = createEntryPath(keyString, offset + 1, key, value);
			current.firstChild = entry;
			entry.previous = current;
			return null;
		}
		return put(current.firstChild, keyString, offset + 1, key, value);
	}
	
	private Entry<K, V> createEntryPath(String keyString, int offset, K key, V value) {
		Entry<K, V> root = new Entry<K, V>(keyString.charAt(offset), null, null, null, null, null);
		Entry<K, V> current = root;
		for (int i = offset + 1; i < keyString.length() - 1; i++) {
			Entry<K, V> previous = current;
			current = new Entry<K, V>(keyString.charAt(i), null, null, previous, null, null);
			previous.firstChild = current;
		}
		Entry<K, V> previous = current;
		current = createEntry(keyString.charAt(keyString.length() - 1), null, null, previous, key, value);
		previous.firstChild = current;
		return root;
	}
	
	private Entry<K, V> createEntry(char keyPart, Entry<K, V> nextSibling, Entry<K, V> firstChild, Entry<K, V> previous, K key, V value) {
		Entry<K, V> entry = new Entry<K, V>(keyPart, nextSibling, firstChild, previous, key, value);
		if (nextSibling != null) {
			nextSibling.previous = entry;
		}
		if (firstChild != null) {
			firstChild.previous = entry;
		}
		if (key != null) {
			size++;
		}
		return entry;
	}
	
	private Entry<K, V> createEntry(Entry<K, V> oldEntry) {
		return createEntry(oldEntry, null, null);
	}
	
	private Entry<K, V> createEntry(Entry<K, V> oldEntry, K key, V value) {
		return new Entry<K, V>(oldEntry.keyPart, oldEntry.nextSibling, oldEntry.firstChild, oldEntry.previous, null, null);
	}
	
	private Entry<K, V> clearEntry(Entry<K, V> entry) {
		if (entry.firstChild != null || entry.nextSibling != null) { 
			return createEntry(entry);
		}
		else {
			return null;
		}
	}
	
	private V removeEntry(Entry<K, V> current, String keyString, int offset) {
		if (current == null) {
			return null;
		}
		
		if (keyString.charAt(offset) < current.keyPart) {
			return null;
		}
		
		if (keyString.charAt(offset) > current.keyPart) {
			return removeEntry(current.nextSibling, keyString, offset);
		}
		
		if (offset + 1 == keyString.length()) {
			if (current.isEntry()) {
				return removeEntry(current);
			}
			return null;
		}
		
		return removeEntry(current.firstChild, keyString, offset + 1);
	}
	
	private V removeEntry(Entry<K, V> entry) {
		Entry<K, V> previous = entry.previous;
		if (previous == null) {
			this.root = clearEntry(entry);
		}
		else if (previous.firstChild == entry) {
			previous.firstChild = createEntry(entry);
		}
		else {
			previous.nextSibling = createEntry(entry);
		}
		this.modCount++;
		this.size--;
		return entry.getValue();
	}
	
	private String objectToString(Object o) {
		if (o == null) {
			return EMPTY_STRING;
		}
		String s = o.toString();
		if (s.length() == 0) {
			return EMPTY_STRING;
		}
		return s;
	}
	
	private Entry<K, V> getFirstEntry(Deque<Entry<K, V>> nextSiblingStack) {
		return getNextEntry(root, nextSiblingStack, false);
	}
	
	private Entry<K, V> getNextEntry(Entry<K, V> current, Deque<Entry<K, V>> nextSiblingStack, boolean ignoreCurrent) {
		if (current == null) {
			return null;
		}
		if (current.isEntry() && ignoreCurrent == false) {
			return current;
		}
		if (current.firstChild != null) {
			if (current.nextSibling != null) {
				nextSiblingStack.push(current.nextSibling);
			}
			return getNextEntry(current.firstChild, nextSiblingStack, false);
		}
		if (current.nextSibling != null) {
			return getNextEntry(current.nextSibling, nextSiblingStack, false);
		}
		if (nextSiblingStack.isEmpty()) {
			return null;
		}
		return getNextEntry(nextSiblingStack.pop(), nextSiblingStack, false);
	}

	private static class Entry<K, V> extends AbstractMap.SimpleEntry<K, V> implements Map.Entry<K, V>, Serializable {
		private static final long serialVersionUID = 1L;
		
		char keyPart;
		Entry<K, V> nextSibling;
		Entry<K, V> firstChild;
		Entry<K, V> previous;
		
		public Entry(char keyPart, Entry<K, V> nextSibling, Entry<K, V> firstChild, Entry<K, V> previous, K key, V value) {
			super(key, value);
			this.keyPart = keyPart;
			this.nextSibling = nextSibling;
			this.firstChild = firstChild;
			this.previous = previous;
		}
		
		public boolean isEntry() {
			return getKey() != null;
		}
		
		public String toString() {
			return keyPart + ", " + super.toString(); 
		}
	}
	
	private abstract class AbstractEntryIterator<T> implements Iterator<T> {
		private Entry<K, V> next;
		private Entry<K, V> lastReturned;
		private int expectedModCount;
		private Deque<Entry<K, V>> nextSiblingStack = new ArrayDeque<Entry<K, V>>();
		
		public AbstractEntryIterator() {
			next = getFirstEntry(nextSiblingStack);
			lastReturned = null;
			expectedModCount = modCount;
		}
		
		public boolean hasNext() {
			return next != null;
		}
		
		protected Entry<K, V> nextEntry() {
			Entry<K, V> entry = lastReturned = next;
			if (entry == null) {
				throw new NoSuchElementException();
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			next = getNextEntry(entry, nextSiblingStack, true);
			return entry;
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
	
	private class EntryIterator extends AbstractEntryIterator<Map.Entry<K, V>> {
		public Map.Entry<K, V> next() {
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
	
	private class EntrySet extends AbstractSet<Map.Entry<K, V>>{
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}
		
		@Override
		public int size() {
			return size;
		}
		
		@Override
		public boolean contains(Object o) {
			if (o == null || o instanceof Map.Entry == false) {
				return false;
			}
			Map.Entry otherEntry = (Map.Entry) o;
			Entry entry = getEntry(otherEntry.getKey());
			if (entry == null) {
				return false;
			}
			return entry.equals(otherEntry);
		}
		
		@Override
		public boolean remove(Object o) {
			if (o == null || o instanceof Map.Entry == false) {
				return false;
			}
			Map.Entry otherEntry = (Map.Entry) o;
			Entry<K, V> entry = getEntry(otherEntry.getKey());
			if (entry == null) {
				return false;
			}
			removeEntry(entry);
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
			Entry<K, V> entry = getEntry(o);
			removeEntry(entry);
			return entry.isEntry();
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
        	Deque<Entry<K, V>> nextSiblingStack = new ArrayDeque<Entry<K,V>>();
            for (Entry<K,V> e = getFirstEntry(nextSiblingStack); e != null; e = getNextEntry(e, nextSiblingStack, true)) {
            	V value = e.getValue();
            	if (value == null && o == null || value != null && value.equals(o)) {
            		removeEntry(e);
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
