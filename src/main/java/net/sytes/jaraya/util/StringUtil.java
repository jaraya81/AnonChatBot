package net.sytes.jaraya.util;

public class StringUtil {
    public static Object clean(String text) {
        if (text == null) return "";
        return text.replace(")", "\\)")
                .replace(".", "\\.")
                .replace("-", "\\-")
                .replace("!", "\\!")
                .replace("=", "\\=");
    }
}
