package net.sytes.jaraya.enums;

public enum Tag {
    GENERAL("GENERAL"),
    AMISTAD("AMISTAD"),
    MENS("WOMEN"),
    WOMEN("MENS"),
    H_H("H_H"),
    M_M("M_M"),
    NO_BINARY("NO_BINARY"),
    SPORT("SPORT"),
    VIDEO_GAMES("VIDEO_GAMES"),
    ANIME("ANIME"),
    ARGENTINA("ARGENTINA"),
    BRASIL("BRASIL"),
    CANADA("CANADA"),
    CHILE("CHILE"),
    COLOMBIA("COLOMBIA"),
    CUBA("CUBA"),
    ESPANA("ESPANA"),
    GUATEMALA("GUATEMALA"),
    MEXICO("MEXICO"),
    PANAMA("PANAMA"),
    PERU("PERU"),
    PORTUGAL("PORTUGAL"),
    REP_DOMINICANA("REP_DOMINICANA"),
    USA("USA"),
    VENEZUELA("VENEZUELA"),
    ;

    Tag(String reverse) {
        this.reverse = reverse;
    }

    private final String reverse;

    public String reverse() {
        return reverse;
    }
}
