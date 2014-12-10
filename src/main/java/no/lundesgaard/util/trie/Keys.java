package no.lundesgaard.util.trie;

import java.util.List;

public class Keys {
	public static <K> Object[] partialKeysFromKey(K key) {
		// TODO
		if (key instanceof String) {
			char[] chars = ((String) key).toCharArray();
			Character[] characters = new Character[chars.length];
			for (int i = 0; i < chars.length; i++) {
				characters[i] = chars[i];
			}
			return characters;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <K> K asKey(Object[] partialKeys) {
		if (partialKeys == null || partialKeys.length == 0) {
			return null;
		}
		// TODO
		Class<?> partialKeyType = partialKeys[0].getClass();
		if (partialKeyType == Character.class) {
			char[] chars = new char[partialKeys.length];
			for (int i = 0; i < partialKeys.length; i++) {
				chars[i] = (Character) partialKeys[i];
			}
			return (K) String.valueOf(chars);
		}
		return null;
	}

	private Keys() {
	}
}
