package net.sytes.jaraya.model;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Tolerate;
import net.sytes.jaraya.enums.PremiumType;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.state.State;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Builder
@ToString
public class User implements Serializable {

    private static final String START_PREMIUM = "\uD83C\uDF1F\uD83C\uDF1F\uD83C\uDF1F";

    private Long id;
    private Long idUser;
    private String username;
    private String description;
    private String state;
    private String lang;
    private Timestamp datecreation;
    private Timestamp dateupdate;
    private String premiumType;
    private Timestamp datePremium;

    @Tolerate
    public User() {
        super();
    }

    public void setDescription(String idPhoto, String description) {
        String text = description != null
                ? description.replace("\uD83C\uDF1F", "").trim()
                : "";
        this.description = text.replace("|", " ") + (idPhoto != null ? "|" + idPhoto : "");
    }

    public void setDescription(String description) {
        setDescription(null, description);
    }

    public String getDescriptionText() {
        return this.description.split("\\|")[0];
    }

    public String getDescriptionPhoto() {
        String[] arr = this.description.split("\\|");
        return arr.length > 1 ? arr[1] : null;
    }

    public void setPremium(String premium) {
        this.premiumType = premium;
    }

    public boolean isPremium() {
        return premiumType != null && (
                premiumType.contentEquals(PremiumType.ANNUAL.name())
                        || premiumType.contentEquals(PremiumType.PERMANENT.name())
                        || premiumType.contentEquals(PremiumType.TEMPORAL.name())
        );
    }

    public static boolean exist(User user) {
        return Objects.nonNull(user);
    }

    @SneakyThrows
    public static boolean is(User user, State state) {
        if (!User.exist(user) || Objects.isNull(state)) {
            throw new TelegramException("");
        }
        return user.getState().contentEquals(state.name());
    }

    public static boolean isBanned(User user) {
        return is(user, State.BANNED);
    }

    public static boolean isPausedOrStop(User user) {
        return is(user, State.PAUSE) || is(user, State.STOP);
    }

    public static boolean isPaused(User user) {
        return is(user, State.PAUSE);
    }

    public static boolean isPlayed(User user) {
        return is(user, State.PLAY);
    }

    public static boolean isEmptyBio(User user) {
        return is(user, State.EMPTY_BIO);
    }

    public String bioPremium() {
        return String.format("%s %s", START_PREMIUM, getDescription());
    }


    public enum Columns {
        ID("id"),
        ID_USER("iduser"),
        USERNAME("username"),
        DESCRIPTION("description"),
        STATE("state"),
        LANG("lang"),
        PREMIUM("premiumType"),
        CREATION("datecreation"),
        UPDATE("dateupdate"),
        DATE_PREMIUM("datePremium");

        String value;

        Columns(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
