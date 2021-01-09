package net.sytes.jaraya.enums;

public enum Tag {
    GENERAL("General \uD83D\uDE0E");

    Tag(String value) {
        this.value = value;
    }

    private final String value;

    public String value() {
        return value;
    }
}
