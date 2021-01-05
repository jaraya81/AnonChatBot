package net.sytes.jaraya.security;

import java.nio.charset.StandardCharsets;

public class Base64 {

    public static byte[] encode(byte[] src) {
        return java.util.Base64.getEncoder().encode(src);
    }

    public static byte[] decode(byte[] src) {
        return java.util.Base64.getDecoder().decode(src);
    }

    public static String encode(String src) {
        return java.util.Base64.getEncoder().encodeToString(src.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String src) {
        return new String(java.util.Base64.getDecoder().decode(src), StandardCharsets.UTF_8);
    }

    public static byte[] encodeUrl(byte[] src) {
        return java.util.Base64.getUrlEncoder().encode(src);
    }

    public static byte[] decodeUrl(byte[] src) {
        return java.util.Base64.getUrlDecoder().decode(src);
    }

    public static String encodeUrl(String src) {
        return java.util.Base64.getUrlEncoder().encodeToString(src.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeUrl(String src) {
        return new String(java.util.Base64.getUrlDecoder().decode(src), StandardCharsets.UTF_8);
    }
}
