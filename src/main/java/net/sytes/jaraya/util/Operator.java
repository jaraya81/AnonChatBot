package net.sytes.jaraya.util;

public class Operator {
    private Operator() {
    }

    public static <T> T elvis(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
