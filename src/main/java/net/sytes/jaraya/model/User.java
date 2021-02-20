package net.sytes.jaraya.model;

import lombok.*;
import lombok.experimental.Tolerate;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.enums.PremiumType;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.state.State;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Data
@Builder
@ToString
@Slf4j
public class User implements Serializable {

    private static final String START_PREMIUM = "[⭐️Pʀᴇᴍɪᴜᴍ]";

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
    private Long idReference;

    @Tolerate
    public User() {
        super();
    }

    public void setDescription(String idPhoto, String description) {
        String text = description != null
                ? description.replace(START_PREMIUM, "").trim()
                : "";
        this.description = text + (idPhoto != null ? "|" + idPhoto : "");
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
        return LocalDateTime.now(ZoneId.systemDefault()).isBefore(datePremium.toLocalDateTime());
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
        return String.format("<b>%s</b> %s", START_PREMIUM, getDescriptionText());
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
        DATE_PREMIUM("datePremium"),
        ID_REFERENCE("idReference");

        String value;

        Columns(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public static Timestamp calcDatePremium(@NonNull PremiumType premiumType, @NonNull Date date) {
        LocalDateTime ldt = Timestamp.from(date.toInstant()).toLocalDateTime();
        ldt = ldt.isBefore(LocalDateTime.now()) ? LocalDateTime.now() : ldt;
        if (premiumType == PremiumType.TEMPORAL) {
            return Timestamp.valueOf(ldt.plusDays(7));
        }
        if (premiumType == PremiumType.MONTHLY) {
            return Timestamp.valueOf(ldt.plusMonths(1));
        }
        if (premiumType == PremiumType.ANNUAL) {
            return Timestamp.valueOf(ldt.plusYears(1));
        }
        if (premiumType == PremiumType.DAY) {
            return Timestamp.valueOf(ldt.plusDays(2));
        }
        if (premiumType == PremiumType.NO) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        if (premiumType == PremiumType.PERMANENT) {
            return Timestamp.valueOf(ldt.plusYears(100));
        }
        return Timestamp.valueOf(ldt);
    }

}
