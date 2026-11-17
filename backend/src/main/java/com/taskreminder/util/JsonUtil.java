package com.taskreminder.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal JSON builder and parser for plain Java.
 * Avoids any external library.
 */
public final class JsonUtil {

    private JsonUtil() {}

    // ─── Builder ───────────────────────────────────────────────────

    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            appendValue(sb, e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    public static String success(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("message", message);
        return toJson(m);
    }

    public static String error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("error", message);
        return toJson(m);
    }

    private static void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Boolean || value instanceof Number) {
            sb.append(value);
        } else if (value instanceof String s) {
            sb.append("\"").append(escape(s)).append("\"");
        } else if (value instanceof java.util.List<?> list) {
            sb.append("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                appendValue(sb, item);
            }
            sb.append("]");
        } else if (value instanceof Map<?, ?> map) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(e.getKey().toString())).append("\":");
                appendValue(sb, e.getValue());
            }
            sb.append("}");
        } else {
            sb.append("\"").append(escape(value.toString())).append("\"");
        }
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ─── Parser ────────────────────────────────────────────────────

    /**
     * Parse a flat JSON object {"key":"value",...} into a Map.
     * Handles string, number, boolean and null values.
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) return result;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))  json = json.substring(0, json.length() - 1);
        json = json.trim();
        if (json.isEmpty()) return result;

        int i = 0;
        while (i < json.length()) {
            // skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;

            // expect "key"
            if (json.charAt(i) != '"') { i++; continue; }
            int keyStart = ++i;
            while (i < json.length() && json.charAt(i) != '"') {
                if (json.charAt(i) == '\\') i++; // skip escaped char
                i++;
            }
            String key = json.substring(keyStart, i);
            i++; // closing quote

            // skip whitespace and colon
            while (i < json.length() && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ':')) i++;

            // read value
            String value;
            if (i >= json.length()) break;
            if (json.charAt(i) == '"') {
                // string value
                int valStart = ++i;
                StringBuilder val = new StringBuilder();
                while (i < json.length() && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                        char next = json.charAt(i + 1);
                        switch (next) {
                            case '"'  -> val.append('"');
                            case '\\' -> val.append('\\');
                            case 'n'  -> val.append('\n');
                            case 'r'  -> val.append('\r');
                            case 't'  -> val.append('\t');
                            default   -> val.append(next);
                        }
                        i += 2;
                    } else {
                        val.append(json.charAt(i++));
                    }
                }
                value = val.toString();
                i++; // closing quote
            } else {
                // non-string value (number, boolean, null)
                int valStart = i;
                while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                value = json.substring(valStart, i).trim();
            }

            result.put(key, value);

            // skip comma
            while (i < json.length() && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) i++;
        }
        return result;
    }
}
