package net.sytes.jaraya.util;

public class StringUtil {

    private StringUtil() {
    }

    public static String clean(String text) {
        if (text == null) return "";
        return text.replace(")", "\\)")
                .replace("(", "\\(")
                .replace(".", "\\.")
                .replace("-", "\\-")
                .replace("+", "\\+")
                .replace("!", "\\!")
                .replace("#", "\\#")
                .replace("-", "\\-")
                .replace("=", "\\=");
    }
}
