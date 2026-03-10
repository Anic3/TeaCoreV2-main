package me.tuanvo0022.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SmallCapsUtil {

    private static final Map<Character, Character> SMALL_CAPS_MAP = new ConcurrentHashMap<>();

    static {
        SMALL_CAPS_MAP.put('a', 'ᴀ');
        SMALL_CAPS_MAP.put('b', 'ʙ');
        SMALL_CAPS_MAP.put('c', 'ᴄ');
        SMALL_CAPS_MAP.put('d', 'ᴅ');
        SMALL_CAPS_MAP.put('e', 'ᴇ');
        SMALL_CAPS_MAP.put('f', 'ғ');
        SMALL_CAPS_MAP.put('g', 'ɢ');
        SMALL_CAPS_MAP.put('h', 'ʜ');
        SMALL_CAPS_MAP.put('i', 'ɪ');
        SMALL_CAPS_MAP.put('j', 'ᴊ');
        SMALL_CAPS_MAP.put('k', 'ᴋ');
        SMALL_CAPS_MAP.put('l', 'ʟ');
        SMALL_CAPS_MAP.put('m', 'ᴍ');
        SMALL_CAPS_MAP.put('n', 'ɴ');
        SMALL_CAPS_MAP.put('o', 'ᴏ');
        SMALL_CAPS_MAP.put('p', 'ᴘ');
        SMALL_CAPS_MAP.put('q', 'ǫ');
        SMALL_CAPS_MAP.put('r', 'ʀ');
        SMALL_CAPS_MAP.put('s', 'ꜱ');
        SMALL_CAPS_MAP.put('t', 'ᴛ');
        SMALL_CAPS_MAP.put('u', 'ᴜ');
        SMALL_CAPS_MAP.put('v', 'ᴠ');
        SMALL_CAPS_MAP.put('w', 'ᴡ');
        SMALL_CAPS_MAP.put('x', 'x');
        SMALL_CAPS_MAP.put('y', 'ʏ');
        SMALL_CAPS_MAP.put('z', 'ᴢ');
    }

    /**
     * Utility class for converting normal text to small caps.
     * <p>
     * This class should not be instantiated.
     */
    private SmallCapsUtil() {
        throw new UnsupportedOperationException("Utility class, not meant to be instantiated");
    }

    public static String convert(String input) {
        if (input == null) return null;
        StringBuilder result = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            result.append(SMALL_CAPS_MAP.getOrDefault(c, c));
        }
        return result.toString();
    }
}