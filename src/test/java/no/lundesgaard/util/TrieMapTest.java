package no.lundesgaard.util;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TrieMapTest {
    // test data
    private static final String KEY_01 = "bar";
    private static final String KEY_02 = "foo";
    private static final String KEY_03 = "zot";
    private static final String KEY_UNKNOWN = "trie";
    private static final String KEY_NULL = null;
    private static final String KEY_BLANK = "";
    private static final String VALUE_01 = "BAR";
    private static final String VALUE_02 = "FOO";
    private static final String VALUE_03 = "ZOT";
    private static final String VALUE_UNKNOWN = "TRIE";
    private static final String VALUE_NULL = "NULL";
    private static final String VALUE_BLANK = "BLANK";

    private static final String[] KEYS = {KEY_NULL, KEY_01, KEY_02, KEY_03};
    private static final String[] VALUES = {VALUE_NULL, VALUE_01, VALUE_02, VALUE_03};

    private Random random;

    @Test
    public void testMap() {
        System.out.println("\nMap integrity tests");

        TrieMap<String> trieMap = new TrieMap<>();
        emptyMapTests(trieMap);

        // put values and test emptiness, size, entry set size, key set size and values size
        putTests(trieMap, KEYS, VALUES);

        // test contains
        containsTests(trieMap, KEYS, VALUES);
        assertFalse("does not contain key: " + KEY_UNKNOWN, trieMap.containsKey(KEY_UNKNOWN));
        assertFalse("does not contain value: " + VALUE_UNKNOWN, trieMap.containsValue(VALUE_UNKNOWN));

        // test: entry set
        entrySetTest(trieMap, KEYS, VALUES);
        assertEquals(KEYS.length + " entries", KEYS.length, trieMap.entrySet().size());

        // test: key set
        keySetTest(trieMap, KEYS);
        assertEquals(KEYS.length + " keys", KEYS.length, trieMap.keySet().size());

        // test: values
        valuesTest(trieMap, VALUES);
        assertEquals(KEYS.length + " values", KEYS.length, trieMap.values().size());

        // test: get
        getTests(trieMap, KEYS, VALUES);
        assertNull("get value with unknown key is null", trieMap.get(KEY_UNKNOWN));

        // test: modify
        assertEquals("remove value with key \"" + KEY_NULL + "\" equals \"" + VALUE_NULL + "\"", VALUE_NULL, trieMap.remove(KEY_NULL));
        assertEquals("size is 3", 3, trieMap.size());
        assertNull("remove value with key \"" + KEY_NULL + "\" is null", trieMap.remove(KEY_NULL));
        assertEquals("size is 3", 3, trieMap.size());
        trieMap.put(KEY_NULL, VALUE_NULL);
        assertEquals("size is 4", 4, trieMap.size());
        assertEquals("put new value with key \"" + KEY_BLANK + "\". Old value equals \"" + VALUE_NULL + "\"", VALUE_NULL, trieMap.put(KEY_BLANK, VALUE_BLANK));
        assertEquals("size is 4", 4, trieMap.size());
        assertEquals("put new value with key \"" + KEY_NULL + "\". Old value equals \"" + VALUE_BLANK + "\"", VALUE_BLANK, trieMap.put(KEY_NULL, VALUE_NULL));
        assertEquals("size is 4", 4, trieMap.size());

        // test: random remove
        long seed = System.currentTimeMillis();
        random = new Random(seed);
        int index = random.nextInt(KEYS.length);
        assertEquals("remove value with key \"" + KEYS[index] + "\" equals \"" + VALUES[index] + "\"", VALUES[index], trieMap.remove(KEYS[index]));
        assertNull("remove value with key \"" + KEYS[index] + "\" is null", trieMap.remove(KEYS[index]));
        assertFalse("does not contains key \"" + KEYS[index] + "\"", trieMap.containsKey(KEYS[index]));
        assertFalse("does not contains key \"" + VALUES[index] + "\"", trieMap.containsValue(VALUES[index]));
        assertEquals("size == " + (KEYS.length - 1), KEYS.length - 1, trieMap.size());
        trieMap.put(KEYS[index], VALUES[index]);

        // test: iterator remove
        iteratorRemoveTest(trieMap, trieMap.entrySet().iterator(), KEYS, VALUES);
        iteratorRemoveTest(trieMap, trieMap.keySet().iterator(), KEYS, VALUES);
        iteratorRemoveTest(trieMap, trieMap.values().iterator(), KEYS, VALUES);

        // test: hashCode when full
        assertEquals("hashCode when full is 2583079", 2583079, trieMap.hashCode());

        // test: equals another map
        Map<String, String> anotherMap = new HashMap<>();
        for (int i = 0; i < KEYS.length; i++) {
            String key = KEYS[i];
            String value = VALUES[i];
            anotherMap.put(key, value);
        }
        assertEquals("another map equals trie map", anotherMap, trieMap);
        assertEquals("trie map equals another map", trieMap, anotherMap);
        assertEquals("another map entry set equals trie map entry set", anotherMap.entrySet(), trieMap.entrySet());
        assertEquals("trie map entry set equals another map entry set", trieMap.entrySet(), anotherMap.entrySet());

        // test: clear
        trieMap.clear();
        emptyMapTests(trieMap);

        // test: hashCode when empty
        assertEquals("hashCode when empty is 0", 0, trieMap.hashCode());
    }

    private void emptyMapTests(TrieMap trieMap) {
        assertTrue("is empty", trieMap.isEmpty());
        assertEquals("size == 0", 0, trieMap.size());
        assertNotNull("valid entry set", trieMap.entrySet());
        assertEquals("no entries", 0, trieMap.entrySet().size());
        assertNotNull("valid key set", trieMap.keySet());
        assertEquals("no keys", 0, trieMap.keySet().size());
        assertNotNull("valid values", trieMap.values());
        assertEquals("no values", 0, trieMap.values().size());
        assertFalse("does not contain key KEY_01", trieMap.containsKey(KEY_01));
        assertFalse("does not contain value VALUE_01", trieMap.containsValue(VALUE_01));
        assertNull("get with key KEY_01 is null", trieMap.get(KEY_01));
        assertNull("remove with key KEY_01 is null", trieMap.remove(KEY_01));
    }

    private void putTests(TrieMap<String> trieMap, String[] keys, String[] values) {
        for (int i = 0; i < keys.length; i++) {
            trieMap.put(keys[i], values[i]);
            if (i == 0) {
                assertFalse("is empty", trieMap.isEmpty());
            }
            assertEquals("size == " + (i + 1), i + 1, trieMap.size());
        }
    }

    private void containsTests(TrieMap<String> trieMap, String[] keys, String[] values) {
        for (int i = 0; i < keys.length; i++) {
            assertTrue("contains key: " + keys[i], trieMap.containsKey(keys[i]));
            assertTrue("contains value: " + values[i], trieMap.containsValue(values[i]));
        }
    }

    private void entrySetTest(TrieMap<String> trieMap, String[] keys, String[] values) {
        Set<Map.Entry<String, String>> entrySet = trieMap.entrySet();
        assertNotNull("valid entry set", entrySet);
        Iterator<Map.Entry<String, String>> entryIterator = entrySet.iterator();
        for (int i = 0; i < keys.length; i++) {
            Map.Entry entry = entryIterator.next();
            assertEquals((i + 1) + ". entry's key equals \"" + keys[i] + "\"", keys[i], entry.getKey());
            assertEquals((i + 1) + ". entry's value equals \"" + values[i] + "\"", values[i], entry.getValue());
        }
    }

    private void keySetTest(TrieMap<String> trieMap, String[] keys) {
        Set<String> keySet = trieMap.keySet();
        assertNotNull("valid key set", keySet);
        Iterator<String> keyIterator = keySet.iterator();
        for (int i = 0; i < keys.length; i++) {
            String key = keyIterator.next();
            assertEquals((i + 1) + ". key equals \"" + keys[i] + "\"", keys[i], key);
        }
    }

    private void valuesTest(TrieMap<String> trieMap, String[] values) {
        Collection<String> valueCollection = trieMap.values();
        assertNotNull("valid value collection", values);
        Iterator<String> valueIterator = valueCollection.iterator();
        for (int i = 0; i < values.length; i++) {
            String value = valueIterator.next();
            assertEquals((i + 1) + ". value equals \"" + values[i] + "\"", values[i], value);
        }
    }

    private void getTests(TrieMap<String> trieMap, String[] keys, String[] values) {
        for (int i = 0; i < keys.length; i++) {
            assertEquals("get value with key \"" + keys[i] + "\" equals \"" + values[i] + "\"", values[i], trieMap.get(keys[i]));
        }
    }

    private void iteratorRemoveTest(TrieMap<String> trieMap, Iterator iterator, String[] keys, String[] values) {
        int index = random.nextInt(keys.length);
        for (int i = 0; iterator.hasNext(); i++) {
            iterator.next();
            if (i == index) {
                iterator.remove();
            }
        }
        assertEquals("size == " + (keys.length - 1), (keys.length - 1), trieMap.size());
        assertFalse("does not contain key \"" + keys[index] + "\"", trieMap.containsKey(keys[index]));
        trieMap.put(keys[index], values[index]);
    }
}