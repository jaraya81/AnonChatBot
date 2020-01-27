package net.sytes.jaraya.enums;

public enum Msg {
    START_OK("WELCOME_START"),
    START_BANNED_USER("START_BANNED_USER"),
    START_AGAIN("START_AGAIN"),
    USER_PLAY("USER_PLAY"),
    USER_PAUSE("USER_PAUSE"),
    USER_REPORT("USER_REPORT"),
    USER_1_NEXT_OK("USER_1_NEXT_OK"),
    USER_2_NEXT_OK("USER_2_NEXT_OK"),
    USER_NEXT_WAITING("USER_NEXT_WAITING"),
    USER_NO_CHAT("USER_NO_CHAT"),

    SET_BIO_OK("SET_BIO_OK"),

    BIO("BIO"),

    CONFIG("CONFIG"),
    CHAT_TIMEOUT("CHAT_TIMEOUT"),

    USER_BLOCK("USER_BLOCK"),

    ABOUT("ABOUT"),

    NEXT_YOU("NEXT_YOU"),

    INACTIVITY_USER("INACTIVITY_USER"),

    SET_LANG_OK("SET_LANG_OK"),

    LANG("LANG"),

    USER_ACTIVE("USER_ACTIVE"),

    NEW_BIO("NEW_BIO");

    private String code;

    Msg(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
