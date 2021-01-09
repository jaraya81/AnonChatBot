package net.sytes.jaraya.repo;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.component.MsgProcess;
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

@Slf4j
public class UserRepo extends Repository {

    private static final String TABLE = "user";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ID_USER = "iduser";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_LANG = "lang";
    private static final String COLUMN_CREATION = "datecreation";
    private static final String COLUMN_UPDATE = "dateupdate";
    private static final String SELECT_FROM = "SELECT * FROM ";

    public UserRepo() throws TelegramException {
        super();
        preparing();

    }

    private void preparing() throws TelegramException {
        if (!tableExist(TABLE)) {
            String sql = String.format(
                    "create table %s (%s INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s TEXT NOT NULL, %s TEXT NOT NULL, %s DATETIME NOT NULL, %s DATETIME NOT NULL)",
                    TABLE, COLUMN_ID, COLUMN_ID_USER, COLUMN_USERNAME, COLUMN_DESCRIPTION, COLUMN_STATE, COLUMN_LANG, COLUMN_CREATION, COLUMN_UPDATE);
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
            users = new QueryRunner().query(connect, SELECT_FROM + TABLE + " WHERE " + COLUMN_ID_USER + "=?",
                    new BeanListHandler<>(User.class), idUser);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return !users.isEmpty() ? users.get(0) : null;
    }

    public Integer save(User user) throws TelegramException {
        if (Objects.isNull(user) || Objects.isNull(user.getIdUser()) || Objects.isNull(user.getState())) {
            return null;
        }
        if (Objects.isNull(user.getId())) {
            String insertQuery = "INSERT INTO " + TABLE + "(" + COLUMN_ID_USER + "," + COLUMN_USERNAME + "," + COLUMN_DESCRIPTION + "," + COLUMN_STATE + "," + COLUMN_LANG + "," + COLUMN_CREATION + "," + COLUMN_UPDATE + ") VALUES (?,?,?,?,?,?,?)";
            try {
                return new QueryRunner().update(connect, insertQuery, user.getIdUser(), user.getUsername(), user.getDescription(), user.getState(), user.getLang() != null && !user.getLang().isEmpty() ? user.getLang() : MsgProcess.ES,
                        new Date(), new Date());
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        } else {
            String updateQuery = "UPDATE " + TABLE + " SET " + COLUMN_ID_USER + "=?," + COLUMN_USERNAME + "=?," + COLUMN_DESCRIPTION + "=?," + COLUMN_STATE + "=?," + COLUMN_LANG + "=?," + COLUMN_UPDATE + "=? WHERE " + COLUMN_ID + "=?";
            try {
                return new QueryRunner().update(connect, updateQuery, user.getIdUser(), user.getUsername(), user.getDescription(), user.getState(), user.getLang() != null && !user.getLang().isEmpty() ? user.getLang() : MsgProcess.ES, new Date(), user.getId());
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        }

    }

    public List<User> getByLang(String lang) throws TelegramException {
        List<User> users;
        try {
            users = new QueryRunner().query(connect, SELECT_FROM + TABLE + " WHERE " + COLUMN_LANG + "=?",
                    new BeanListHandler<>(User.class), lang == null || lang.isEmpty() ? MsgProcess.ES : lang);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return users;
    }

    public List<User> getByInactiveMinutes(int minutes) throws TelegramException {
        List<User> users;
        try {
            users = new QueryRunner().query(connect, SELECT_FROM + TABLE,
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
        String sql = String.format("SELECT * FROM %s WHERE %s=?", TABLE, COLUMN_STATE);
        try {
            users = new QueryRunner().query(connect,
                    sql,
                    new BeanListHandler<>(User.class), state.name());
        } catch (SQLException e) {
            TelegramException.throwIt(e);
        }
        return users;
    }

}