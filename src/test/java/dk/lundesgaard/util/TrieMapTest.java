/*
 * 
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
 * 
 */
package dk.lundesgaard.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for trie map.
 */
public class TrieMapTest extends TestCase
{
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
	
	private static final String[] KEYS = { KEY_NULL, KEY_01, KEY_02, KEY_03 };
	private static final String[] VALUES = { VALUE_NULL, VALUE_01, VALUE_02, VALUE_03 };
	
	// performance test data
	private static final String FILE_TESTDATA = "testdata.properties";
	private static final String FILE_TESTDATA_RANDOM = "testdata_random.properties";
	private static final String FILE_TESTDATA_DICT = "testdata_dict.properties";
	private static final int COUNT_ENTRIES = 100;
	private static final int TEST_RUNS = 1000;
	private static long seed;
	private static Random random;
	
    public static void main(String[] args) throws Exception {
    	if (args.length == 0 || args[0].equals("pt")) {
    		new TrieMapTest("foo").testPerformance();
    	}
    	else if (args[0].equals("rd")) {
    		seed = System.currentTimeMillis();
	    	random = new Random(seed);
	
	    	Properties properties = new Properties();
	    	for (int i = 0; i < COUNT_ENTRIES;) {
				String keyValue = toHexString(generateRandomBytes());
				if (properties.containsKey(keyValue)) {
					System.out.println("duplicate key");
					continue;
				}
				properties.setProperty(keyValue, keyValue);
	    		if ((i + 1 % 1000) == 0) {
	    			System.out.println(i + 1);
	    		}
				i++;
			}
	    	FileOutputStream out = new FileOutputStream(FILE_TESTDATA);
	    	properties.store(out, "Test data for TrieMap.");
	    	out.close();
    	}
    	else if (args[0].equals("sampledata")) {
	    	Properties properties = new Properties();
	    	BufferedReader in = new BufferedReader(new FileReader("sample.html"));
	    	for (String line = in.readLine(); line != null; line = in.readLine()) {
	    		StringTokenizer st = new StringTokenizer(line, " =-+?!@%&#<>'\"/\\,.:;[](){}1234567890*");
	    		while (st.hasMoreTokens()) {
	    			String token = st.nextToken();
	    			if (properties.containsKey(token) == false) {
	    				properties.put(token, token);
	    			}
	    		}
	    	}
	    	in.close();
	    	FileOutputStream out = new FileOutputStream(FILE_TESTDATA);
	    	properties.store(out, "Test data for TrieMap.");
	    	out.close();
    	}
    }
    
    private static byte[] generateRandomBytes() {
    	byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		int offset = random.nextInt(25);
		seed = seed ^ bytesToLong(bytes, offset);
		random.setSeed(seed);
		return bytes;
    }
    
    private static long bytesToLong(byte[] bytes, int offset)
	{
		long a = 0xFF00000000000000L & ((bytes[offset] & 0xFFL) << 56);
		long b = 0xFF000000000000L & ((bytes[offset + 1] & 0xFFL) << 48);
		long c = 0xFF0000000000L & ((bytes[offset + 2] & 0xFFL) << 40);
		long d = 0xFF00000000L & ((bytes[offset + 3] & 0xFFL) << 32);
		long e = 0xFF000000L & ((bytes[offset + 4] & 0xFFL) << 24);
		long f = 0xFF0000L & ((bytes[offset + 5] & 0xFFL) << 16);
		long g = 0xFF00L & ((bytes[offset + 6] & 0xFFL) << 8);
		long h = (bytes[offset + 7] & 0xFFL);
		return a + b + c + d + e + f + g + h;
	}
    
    private static String toHexString(byte[] bytes) {
    	StringBuffer result = new StringBuffer(bytes.length * 2);
    	for (int i = 0; i < bytes.length; i++) {
			result.append(Integer.toHexString(bytes[i] & 0xFF));
		}
    	return result.toString();
    }
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TrieMapTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TrieMapTest.class );
    }
    
    /**
     * Full test of trie map.
     */
    public void testMap()
    {
    	System.out.println("\nMap integrity tests");
    	
    	TrieMap<String, String> trieMap = new TrieMap<String, String>();
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
    	assertNull("remove value with key \"" + KEY_NULL + "\" is null", trieMap.remove(KEY_NULL));
    	trieMap.put(KEY_NULL, VALUE_NULL);
    	assertEquals("put new value with key \"" + KEY_BLANK + "\". Old value equals \"" + VALUE_NULL + "\"", VALUE_NULL, trieMap.put(KEY_BLANK, VALUE_BLANK));
    	assertEquals("put new value with key \"" + KEY_NULL + "\". Old value equals \"" + VALUE_BLANK + "\"", VALUE_BLANK, trieMap.put(KEY_NULL, VALUE_NULL));

    	// test: random remove
    	seed = System.currentTimeMillis();
    	random = new Random(seed);
    	int index = random.nextInt(KEYS.length);
    	assertEquals("remove value with key \"" + KEYS[index]+ "\" equals \"" + VALUES[index] + "\"", VALUES[index], trieMap.remove(KEYS[index]));
    	assertNull("remove value with key \"" + KEYS[index]+ "\" is null", trieMap.remove(KEYS[index]));
    	assertFalse("does not contains key \"" + KEYS[index]+ "\"", trieMap.containsKey(KEYS[index]));
    	assertFalse("does not contains key \"" + VALUES[index] + "\"", trieMap.containsValue(VALUES[index]));
    	assertEquals("size == " + (KEYS.length - 1), KEYS.length - 1, trieMap.size());
    	trieMap.put(KEYS[index], VALUES[index]);

    	// test: iterator remove
    	iteratorRemoveTest(trieMap, trieMap.entrySet().iterator(), KEYS, VALUES);
    	iteratorRemoveTest(trieMap, trieMap.keySet().iterator(), KEYS, VALUES);
    	iteratorRemoveTest(trieMap, trieMap.values().iterator(), KEYS, VALUES);
    	
    	// test: clear
    	trieMap.clear();
    	emptyMapTests(trieMap);
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
    
    private void putTests(TrieMap<String, String> trieMap, String[] keys, String[] values) {
    	for (int i = 0; i < keys.length; i++) {
        	trieMap.put(keys[i], values[i]);
        	if (i == 0) {
        		assertFalse("is empty", trieMap.isEmpty());
        	}
        	assertEquals("size == " + (i + 1), i + 1, trieMap.size());
		}
    }
    
    private void containsTests(TrieMap<String, String> trieMap, String[] keys, String[] values) {
    	for (int i = 0; i < keys.length; i++) {
        	assertTrue("contains key: " + keys[i], trieMap.containsKey(keys[i]));
        	assertTrue("contains value: " + values[i], trieMap.containsValue(values[i]));
    	}
    }
    
    private void entrySetTest(TrieMap<String, String> trieMap, String[] keys, String[] values) {
    	Set<Map.Entry<String, String>> entrySet = trieMap.entrySet();
    	assertNotNull("valid entry set", entrySet);
    	Iterator<Map.Entry<String, String>> entryIterator = entrySet.iterator();
		for (int i = 0; i < keys.length; i++) {
    		Map.Entry entry = entryIterator.next();
    		assertEquals((i + 1) + ". entry's key equals \"" + keys[i] + "\"", keys[i], entry.getKey());
    		assertEquals((i + 1) + ". entry's value equals \"" + values[i] + "\"", values[i], entry.getValue());
		}
    }
    
    private void keySetTest(TrieMap<String, String> trieMap, String[] keys) {
    	Set<String> keySet = trieMap.keySet();
    	assertNotNull("valid key set", keySet);
    	Iterator<String> keyIterator = keySet.iterator();
		for (int i = 0; i < keys.length; i++) {
    		String key = keyIterator.next();
    		assertEquals((i + 1) + ". key equals \"" + keys[i] + "\"", keys[i], key);
		}
    }
    
    private void valuesTest(TrieMap<String, String> trieMap, String[] values) {
    	Collection<String> valueCollection = trieMap.values();
    	assertNotNull("valid value collection", values);
    	Iterator<String> valueIterator = valueCollection.iterator();
		for (int i = 0; i < values.length; i++) {
    		String value = valueIterator.next();
    		assertEquals((i + 1) + ". value equals \"" + values[i] + "\"", values[i], value);
		}
    }
    
    private void getTests(TrieMap<String, String> trieMap, String[] keys, String[] values) {
    	for (int i = 0; i < keys.length; i++) {
        	assertEquals("get value with key \"" + keys[i] + "\" equals \"" + values[i] + "\"", values[i], trieMap.get(keys[i]));
		}
    }
    
    private void iteratorRemoveTest(TrieMap<String, String> trieMap, Iterator iterator, String[] keys, String[] values) {
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
    
    public void testPerformance() throws IOException {
    	System.out.println("\nPerformance test");
    	System.out.println("running performance test...");
    	System.out.println("\nDictionary data:");
    	Properties testData = new Properties();
    	testData.load(getClass().getResourceAsStream(FILE_TESTDATA_DICT));
    	runTests(testData, TEST_RUNS);
    	testData.clear();
    	System.out.println("\nRandom data:");
    	testData.load(getClass().getResourceAsStream(FILE_TESTDATA_RANDOM));
    	runTests(testData, TEST_RUNS);
    }
    
    private void runTests(Properties testData, int runs) {
    	Map<String, String> map1 = new TrieMap<String, String>();
    	Map<String, String> map2 = new HashMap<String, String>();
    	Map<String, String> map3 = new TreeMap<String, String>();
    	
    	Map<Long, String> testResults = new TreeMap<Long, String>();
    	runPutTest(testResults, map1, testData, runs);
    	runPutTest(testResults, map2, testData, runs);
    	runPutTest(testResults, map3, testData, runs);
    	System.out.println("put operation: ");
    	printResults(testResults);
    	
    	testResults.clear();
    	runGetTest(testResults, map1, testData, runs);
    	runGetTest(testResults, map2, testData, runs);
    	runGetTest(testResults, map3, testData, runs);
    	System.out.println("get operation: ");
    	printResults(testResults);
    }
    
    private void printResults(Map<Long, String> testResults) {
    	int i = 0;
    	for (Iterator<Map.Entry<Long, String>> iter = testResults.entrySet().iterator(); iter.hasNext(); i++) {
			Map.Entry<Long, String> entry = iter.next();
			System.out.println("\t" + (i + 1) + ": " + entry.getKey() + " ms, " + entry.getValue());
		}
    }
    
    private void runPutTest(Map<Long, String> testResults, Map<String, String> map, Properties testData, int runs) {
    	Set<String> propertyNames = testData.stringPropertyNames();
    	Timer timer = new Timer();
    	for (int i = 0; i < runs; i++) {
    		map.clear();
        	timer.start();
	    	for (Iterator<String> iter = propertyNames.iterator(); iter.hasNext(); timer.incrementCount()) {
	    		String key = iter.next();
				String value = testData.getProperty(key);
				map.put(key, value);
			}
	    	timer.stop();	
		}
    	if (testResults.containsKey(timer.time)) {
    		testResults.put(timer.time, testResults.get(timer.time) + ", " + map.getClass().toString());
    	}
    	testResults.put(timer.time, map.getClass().toString());
    }
    
    private void runGetTest(Map<Long, String> testResults, Map<String, String> map, Properties testData, int runs) {
    	Set<String> propertyNames = testData.stringPropertyNames();
    	Timer timer = new Timer();
    	timer.start();
    	for (int i = 0; i < runs; i++) {
	    	for (Iterator<String> iter = propertyNames.iterator(); iter.hasNext(); timer.incrementCount()) {
				String key = iter.next();
				map.get(key);
			}
    	}
    	timer.stop();
    	if (testResults.containsKey(timer.time)) {
    		testResults.put(timer.time, testResults.get(timer.time) + ", " + map.getClass().toString());
    	}
    	testResults.put(timer.time, map.getClass().toString());
    }
    
    private static class Timer {
    	static final String OPERATION_GET = "get";
    	static final String OPERATION_PUT = "put";
    	
    	static final long SECOND = 1000;
    	static final long MINUTE = 60 * SECOND;
    	static final long HOUR = 60 * MINUTE;
    	
    	long start;
    	long time;
    	int count;
    	
    	public void start() {
    		start = System.currentTimeMillis();
    	}
    	
    	public void stop() {
    		time += System.currentTimeMillis() - start;
    	}
    	
    	private double timePerEntry() {
    		return (time) * 1.0d / count;
    	}
    	
    	private String timeString() {
    		long ms = time;
    		long minutes = ms / MINUTE;
    		ms = ms % MINUTE;
    		long seconds = ms / SECOND;
    		ms = ms % SECOND;
    		return minutes + "m " + seconds + "s " + ms + "ms";
    	}
    	
    	public int getCount() {
    		return count;
    	}
    	
    	public void incrementCount() {
    		count++;
    	}

    	public void print(Class clazz, String operation) {
    		System.out.println("\n" + clazz + ": " + operation);
        	System.out.println(count + " " + operation + " operations in " + timeString());
        	System.out.println(timePerEntry() + " ms per operation");    		
    	}
    }
}
