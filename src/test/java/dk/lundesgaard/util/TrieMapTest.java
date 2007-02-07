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
public class TrieMapTest 
    extends TestCase
{
	// test data
	private static final String KEY_01 = "foo";
	private static final String KEY_02 = "bar";
	private static final String KEY_03 = "zot";
	private static final String VALUE_01 = "FOO";
	private static final String VALUE_02 = "BAR";
	private static final String VALUE_03 = "ZOT";
	private static final String UNKNOWN_KEY = "trie";
	private static final String UNKNOWN_VALUE = "TRIE";
	
	// performance test data
	private static final String FILE_TESTDATA = "testdata.properties";
	private static final String FILE_TESTDATA_RANDOM = "testdata_random.properties";
	private static final String FILE_TESTDATA_DICT = "testdata_dict.properties";
	private static final int COUNT_ENTRIES = 100000;
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
    	trieMap.put(KEY_01, VALUE_01);
    	assertFalse("is empty", trieMap.isEmpty());
    	assertFalse("size == 0", trieMap.size() == 0);
    	trieMap.put(KEY_02, VALUE_02);
    	trieMap.put(KEY_03, VALUE_03);
    	assertEquals("size == 3", 3, trieMap.size());
    	assertEquals("3 entries", 3, trieMap.entrySet().size());
    	assertEquals("3 keys", 3, trieMap.keySet().size());
    	assertEquals("3 values", 3, trieMap.values().size());
    	
    	// test contains
    	assertTrue("contains key KEY_01", trieMap.containsKey(KEY_01));
    	assertTrue("contains key KEY_02", trieMap.containsKey(KEY_02));
    	assertTrue("contains key KEY_03", trieMap.containsKey(KEY_03));
    	assertFalse("does not contain key UNKNOWN_KEY", trieMap.containsKey(UNKNOWN_KEY));
    	assertTrue("contains value VALUE_01", trieMap.containsValue(VALUE_01));
    	assertTrue("contains value VALUE_02", trieMap.containsValue(VALUE_02));
    	assertTrue("contains value VALUE_03", trieMap.containsValue(VALUE_03));
    	assertFalse("does not contain value UNKNOWN_VALUE", trieMap.containsKey(UNKNOWN_VALUE));
    	
    	// test: entry set
    	Set entries = trieMap.entrySet();
    	assertNotNull("valid entry set", entries);
    	Iterator iter = entries.iterator();
		Map.Entry entry = (Map.Entry) iter.next();
		assertEquals("first entry's key equals KEY_01", KEY_01, entry.getKey());
		assertEquals("first entry's value equals VALUE_01", VALUE_01, entry.getValue());
		entry = (Map.Entry) iter.next();
		assertEquals("second entry's key equals KEY_02", KEY_02, entry.getKey());
		assertEquals("second entry's value equals VALUE_02", VALUE_02, entry.getValue());
		entry = (Map.Entry) iter.next();
		assertEquals("third entry's key equals KEY_03", KEY_03, entry.getKey());
		assertEquals("third entry's value equals VALUE_03", VALUE_03, entry.getValue());
		
		// test: key set
    	Set keys = trieMap.keySet();
    	assertNotNull("valid key set", keys);
    	iter = keys.iterator();
		Object key = iter.next();
		assertEquals("first key is KEY_01", KEY_01, key);
		key = iter.next();
		assertEquals("second key is KEY_02", KEY_02, key);
		key = iter.next();
		assertEquals("third key is KEY_03", KEY_03, key);
    	
    	// test: values
    	Collection<String> values = trieMap.values();
    	assertNotNull("valid values", values);
    	Iterator<String> valueIterator = values.iterator();
    	String value = valueIterator.next();
		assertEquals("first value is VALUE_01", VALUE_01, value);
    	value = valueIterator.next();
		assertEquals("second value is VALUE_02", VALUE_02, value);
    	value = valueIterator.next();
		assertEquals("third value is VALUE_03", VALUE_03, value);
		
    	// test: get
    	assertEquals("get value with key KEY_01 equals VALUE_01", VALUE_01, trieMap.get(KEY_01));
    	assertEquals("get value with key KEY_02 equals VALUE_02", VALUE_02, trieMap.get(KEY_02));
    	assertEquals("get value with key KEY_03 equals VALUE_03", VALUE_03, trieMap.get(KEY_03));
    	assertNull("get value with key UNKNOWN_KEY is null", trieMap.get(UNKNOWN_KEY));
    	
    	// test: remove
    	assertEquals("remove value with key KEY_01 equals VALUE_01", VALUE_01, trieMap.remove(KEY_01));
    	assertNull("remove value with key KEY_01 is null", trieMap.remove(KEY_01));
    	assertFalse("does not contains key KEY_01", trieMap.containsKey(KEY_01));
    	assertFalse("does not contains key VALUE_01", trieMap.containsValue(VALUE_01));
    	assertEquals("size == 2", 2, trieMap.size());
    	
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
    
    public void testPerformance() throws IOException {
    	System.out.println("\nPerformance test");
		System.out.println("reading test data...");
    	Properties testDataRandom = new Properties();
    	testDataRandom.load(getClass().getResourceAsStream(FILE_TESTDATA_RANDOM));
    	Properties testDataDict = new Properties();
    	testDataDict.load(getClass().getResourceAsStream(FILE_TESTDATA_DICT));
    	
    	System.out.println("running performance test...");
    	System.out.println("\nRandom data:");
    	runTests(testDataRandom, 1);
    	System.out.println("\nDictionary data:");
    	runTests(testDataDict, TEST_RUNS);
    }
    
    private void runTests(Properties testData, int runs) {
    	Map<String, String> hashMap = new HashMap<String, String>();
    	Map<String, String> treeMap = new TreeMap<String, String>();
    	Map<String, String> trieMap = new TrieMap<String, String>();
    	
    	Map<Long, String> testResults = new TreeMap<Long, String>();
    	runPutTest(testResults, hashMap, testData, runs);
    	runPutTest(testResults, treeMap, testData, runs);
    	runPutTest(testResults, trieMap, testData, runs);
    	System.out.println("put operation: ");
    	printResults(testResults);
    	
    	testResults.clear();
    	runGetTest(testResults, hashMap, testData, runs);
    	runGetTest(testResults, treeMap, testData, runs);
    	runGetTest(testResults, trieMap, testData, runs);
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
