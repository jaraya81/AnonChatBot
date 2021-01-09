package net.sytes.jaraya.repo;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Chat;
import net.sytes.jaraya.state.ChatState;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ChatRepo extends Repository {

    private static final String TABLE = "chats";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ID_USER1 = "user1";
    private static final String COLUMN_ID_USER2 = "user2";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_CREATION = "datecreation";
    private static final String COLUMN_UPDATE = "dateupdate";


    public ChatRepo() throws TelegramException {
        super();
        preparing();
    }


    private void preparing() throws TelegramException {
        if (!tableExist(TABLE)) {
            String sql = String.format(
                    "create table %s (%s INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT, %s TEXT NOT NULL, %s DATETIME NOT NULL, %s DATETIME NOT NULL)", TABLE,
                    COLUMN_ID, COLUMN_ID_USER1, COLUMN_ID_USER2, COLUMN_STATE, COLUMN_CREATION, COLUMN_UPDATE);
            log.info(sql);
            try {
                new QueryRunner().update(connect, sql);
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        }
    }

    @SneakyThrows
    public List<Chat> getByIdUserAndState(Long idUser, ChatState state) {
        if (Objects.isNull(idUser)) {
            return new ArrayList<>();
        }
        List<Chat> chats;
        try {
            String sql = String.format("SELECT * FROM %s WHERE %s=? AND (%s=? OR %s=?)",
                    TABLE,
                    COLUMN_STATE,
                    COLUMN_ID_USER1,
                    COLUMN_ID_USER2);
            chats = new QueryRunner().query(connect, sql,
                    new BeanListHandler<>(Chat.class), state.name(), idUser, idUser);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return chats;
    }

    @SneakyThrows
    public List<Chat> getByIdUser(long idUser) {
        List<Chat> chats;
        try {
            chats = new QueryRunner().query(connect, "SELECT * FROM " + TABLE +
                            " WHERE " + COLUMN_ID_USER1 + "=?"
                            + " OR " + COLUMN_ID_USER2 + "=?",
                    new BeanListHandler<>(Chat.class), idUser, idUser);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return chats;
    }

    @SneakyThrows
    public Integer save(Chat user) {
        if (Objects.isNull(user) || Objects.isNull(user.getUser1()) || Objects.isNull(user.getUser2()) || Objects.isNull(user.getState())) {
            return null;
        }
        if (Objects.isNull(user.getId())) {
            String insertQuery = "INSERT INTO " + TABLE + "(" + COLUMN_ID_USER1 + "," + COLUMN_ID_USER2 + "," + COLUMN_STATE + "," + COLUMN_CREATION + "," + COLUMN_UPDATE + ") VALUES (?,?,?,?,?)";
            try {
                return new QueryRunner().update(connect, insertQuery, user.getUser1(), user.getUser2(), user.getState(),
                        new Date(), new Date());
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        } else {
            String updateQuery = "UPDATE " + TABLE + " SET " + COLUMN_ID_USER1 + "=?," + COLUMN_ID_USER2 + "=?," + COLUMN_STATE + "=?," + COLUMN_UPDATE + "=? WHERE " + COLUMN_ID + "=?";
            try {
                return new QueryRunner().update(connect, updateQuery, user.getUser1(), user.getUser2(), user.getState(), new Date(), user.getId());
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        }

    }

    public List<Chat> getByStatusAndMinusMinute(ChatState chatState, int minutes) throws TelegramException {
        List<Chat> chats;
        try {
            chats = new QueryRunner().query(connect, "SELECT * FROM " + TABLE + " WHERE " + COLUMN_STATE + "=?",
                    new BeanListHandler<>(Chat.class), chatState.name());
        } catch (SQLException e) {
            throw new TelegramException(e);
        }

        return chats.stream()
                .filter(x -> x.getDateupdate().toLocalDateTime().plusMinutes(minutes).isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

    }

    @SneakyThrows
    public void delete(List<Chat> chats) {
        chats.parallelStream().forEach(this::delete);
    }

    @SneakyThrows
    public int delete(Chat chat) {
        String insertQuery = String.format("DELETE FROM %s WHERE %s in (?)", TABLE, COLUMN_ID);
        try {
            return new QueryRunner().update(connect, insertQuery, chat.getId());
        } catch (SQLException e) {
            TelegramException.throwIt(e);
        }
        return 0;
    }
}