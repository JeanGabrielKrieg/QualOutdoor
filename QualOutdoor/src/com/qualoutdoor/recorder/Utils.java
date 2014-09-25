package com.qualoutdoor.recorder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

/**
 * A static class containing various utility methods
 * 
 * @author Gaborit Nicolas
 * 
 */
public final class Utils {

    /**
     * Capitalize a string by setting the first character to uppercase
     * 
     * @param s
     *            The string to capitalize
     * @return The capitalized string
     */
    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * Convert a Bundle into a JSONObject
     * 
     * @param bundle
     *            The bundle to convert.
     */
    public static JSONObject bundleToJSON(Bundle bundle) {
        // The resulting JSONObject
        JSONObject result = new JSONObject();

        // Find all the keys
        Set<String> keys = bundle.keySet();

        // Add each key value into the JSON
        for (String key : keys) {
            try {
                // The associated value
                Object value = bundle.get(key);
                // Wrap the object if necessary and add the key:value pair
                result.put(key, wrap(value));

            } catch (JSONException e) {
                // Ignore this key
            }
        }

        return result;
    }

    /**
     * Modified from [Google
     * Code](https://android.googlesource.com/platform/libcore
     * /+/master/json/src/main/java/org/json/JSONObject.java). Because this
     * method is not available before API 19. Wraps the given object if
     * necessary.
     * 
     * <p>
     * If the object is null or , returns {@link #NULL}.
     * 
     * If the object is a {@code JSONArray} or {@code JSONObject}, no wrapping
     * is necessary.
     * 
     * If the object is {@code NULL}, no wrapping is necessary.
     * 
     * If the object is an array or {@code Collection}, returns an equivalent
     * {@code JSONArray}.
     * 
     * If the object is a {@code Map}, returns an equivalent {@code JSONObject}.
     * 
     * ADDED: If the object is a {@code Bundle} returns an equivalent {@code JSONObject}
     * 
     * If the object is a primitive wrapper type or {@code String}, returns the
     * object.
     * 
     * Otherwise if the object is from a {@code java} package, returns the
     * result of {@code toString}. If wrapping fails, returns null.
     * </p>
     */
    public static Object wrap(Object o) {
        if (o == null) {
            return JSONObject.NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(JSONObject.NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            } else if (o.getClass().isArray()) {
                return new JSONArray(o);
            }
            if (o instanceof Map) {
                return new JSONObject((Map) o);
            }
            // Added to the original code for supporting Bundles
            if (o instanceof Bundle) {
                return bundleToJSON((Bundle) o);
            }
            if (o instanceof Boolean || o instanceof Byte
                    || o instanceof Character || o instanceof Double
                    || o instanceof Float || o instanceof Integer
                    || o instanceof Long || o instanceof Short
                    || o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
