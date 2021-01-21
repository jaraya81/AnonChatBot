package net.sytes.jaraya.repo;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
import net.sytes.jaraya.enums.PremiumType;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.User;
import net.sytes.jaraya.state.State;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.sytes.jaraya.model.User.Columns.*;

@Slf4j
public class UserRepo extends Repository {

    private static final String TABLE = "user";
    private static final String QUERY_BASIC = "SELECT * FROM %s WHERE %s=?";

    public UserRepo() throws TelegramException {
        super();
        preparing();

    }

    private void preparing() throws TelegramException {
        if (!tableExist(TABLE)) {
            String sql = String.format(
                    "create table %s (%s INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT, %s DATETIME, %s DATETIME NOT NULL, %s DATETIME NOT NULL)",
                    TABLE,
                    ID.value(),
                    ID_USER.value(),
                    USERNAME.value(),
                    DESCRIPTION.value(),
                    STATE.value(),
                    LANG.value(),
                    PREMIUM.value(),
                    DATE_PREMIUM.value(),
                    CREATION.value(),
                    UPDATE.value());
            log.info(sql);
            try {
                new QueryRunner().update(connect, sql);
            } catch (SQLException e) {
                throw new TelegramException(e);
            }

        }
    }

    public User getByIdUser(Long idUser) throws TelegramException {
        if (Objects.isNull(idUser)) {
            return null;
        }
        List<User> users;
        try {
            String query = String.format(QUERY_BASIC, TABLE, ID_USER.value());
            users = new QueryRunner().query(connect,
                    query,
                    new BeanListHandler<>(User.class), idUser);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return !users.isEmpty() ? users.get(0) : null;
    }

    @SneakyThrows
    public User save(User user) {
        if (Objects.isNull(user) || Objects.isNull(user.getIdUser()) || Objects.isNull(user.getState())) {
            throw new TelegramException(String.format("%s", user != null ? user : "user is null"));
        }
        try {
            if (Objects.isNull(user.getId())) {
                String query = String.format(
                        "INSERT INTO %s (%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?)",
                        TABLE,
                        ID_USER.value(),
                        USERNAME.value(),
                        DESCRIPTION.value(),
                        STATE.value(),
                        LANG.value(),
                        PREMIUM.value(),
                        CREATION.value(),
                        UPDATE.value(),
                        DATE_PREMIUM.value()
                );
                new QueryRunner().update(connect, query,
                        user.getIdUser(),
                        user.getUsername(),
                        user.getDescription(),
                        user.getState(),
                        user.getLang() != null && !user.getLang().isEmpty() ? user.getLang() : MsgProcess.ES,
                        user.getPremiumType(),
                        new Date(),
                        new Date(),
                        new Date()
                );
            } else {
                String query = String.format("UPDATE %s SET %s=?,%s=?,%s=?,%s=?,%s=?,%s=?,%s=?,%s=? WHERE %s=?",
                        TABLE,
                        ID_USER.value(),
                        USERNAME.value(),
                        DESCRIPTION.value(),
                        STATE.value(),
                        LANG.value(),
                        PREMIUM.value(),
                        DATE_PREMIUM.value(),
                        UPDATE.value(),
                        ID.value());
                new QueryRunner().update(connect, query,
                        user.getIdUser(),
                        user.getUsername(),
                        user.getDescription(),
                        user.getState(),
                        user.getLang() != null && !user.getLang().isEmpty() ? user.getLang() : MsgProcess.ES,
                        user.getPremiumType() != null ? user.getPremiumType() : PremiumType.TEMPORAL.name(),
                        user.getPremiumType() != null ? user.getDatePremium() : new Date(),
                        new Date(),
                        user.getId()
                );
            }
            return getByIdUser(user.getIdUser());
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
    }

    @SneakyThrows
    public List<User> getByLang(String lang) {
        List<User> users;
        try {
            String query = String.format(QUERY_BASIC,
                    TABLE,
                    LANG);
            users = new QueryRunner().query(connect, query,
                    new BeanListHandler<>(User.class),
                    lang == null || lang.isEmpty() ? MsgProcess.ES : lang);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return users;
    }

    @SneakyThrows
    public List<User> getByInactiveMinutes(int minutes) {
        List<User> users;
        try {
            String query = String.format("SELECT * FROM %s",
                    TABLE);
            users = new QueryRunner().query(connect, query,
                    new BeanListHandler<>(User.class));
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return users.parallelStream()
                .filter(x -> x.getDateupdate().toLocalDateTime().plusMinutes(minutes).isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public List<User> getByState(State state) {
        List<User> users = null;
        String sql = String.format(QUERY_BASIC,
                TABLE, STATE);
        try {
            users = new QueryRunner().query(connect,
                    sql,
                    new BeanListHandler<>(User.class), state.name());
        } catch (SQLException e) {
            TelegramException.throwIt(e);
        }
        return users;
    }

    @SneakyThrows
    public List<User> getAll() {
        List<User> users = null;
        String sql = String.format("SELECT * FROM %s", TABLE);
        try {
            users = new QueryRunner().query(connect,
                    sql,
                    new BeanListHandler<>(User.class));
        } catch (SQLException e) {
            TelegramException.throwIt(e);
        }
        return users;
    }
}