package net.sytes.jaraya.util;

import net.sytes.jaraya.exception.CoreException;

public class Validation {
    public static void check(boolean condition, String msg) throws CoreException {
        if (!condition) {
            throw new CoreException(msg);
        }
    }
}
