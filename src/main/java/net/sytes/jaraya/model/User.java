package net.sytes.jaraya.model;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Tolerate;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.state.State;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Builder
@ToString
public class User implements Serializable {

    private Long id;
    private Long idUser;
    private String username;
    private String description;
    private String state;
    private String lang;
    private Timestamp datecreation;
    private Timestamp dateupdate;

    @Tolerate
    public User() {
        super();
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
}
