package net.sytes.jaraya.repo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.UserTag;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static net.sytes.jaraya.model.UserTag.Columns.*;
import static net.sytes.jaraya.util.Validation.check;

@Slf4j
public class TagUserRepo extends Repository {

    private static final String TABLE = "userTags";

    public TagUserRepo() throws TelegramException {
        super();
        preparing();
    }

    private void preparing() throws TelegramException {
        if (!tableExist(TABLE)) {
            String sql = String.format(
                    "create table %s (%s INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT NOT NULL, %s DATETIME NOT NULL)",
                    TABLE,
                    ID.value(),
                    ID_USER.value(),
                    TAG.value(),
                    CREATION.value()
            );
            log.info(sql);
            try {
                new QueryRunner().update(connect, sql);
            } catch (SQLException e) {
                throw new TelegramException(e);
            }

        }
    }

    @SneakyThrows
    public List<UserTag> getByUserId(long userId) {
        List<UserTag> tagUsers;
        try {
            String sql = String.format("SELECT * FROM %s WHERE %s=?",
                    TABLE,
                    UserTag.Columns.ID_USER.value()
            );
            tagUsers = new QueryRunner().query(connect, sql,
                    new BeanListHandler<>(UserTag.class), userId);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return tagUsers;
    }

    @SneakyThrows
    public int save(long userId, String tag) {
        UserTag tagUser = getByUserId(userId)
                .stream()
                .filter(x -> x.getTag().contentEquals(tag))
                .findFirst()
                .orElse(null);
        if (tagUser == null) {
            String insertQuery = String.format("INSERT INTO %s (%s,%s,%s) VALUES (?,?,?)",
                    TABLE,
                    ID_USER.value(),
                    TAG.value(),
                    CREATION.value()
            );
            try {
                return new QueryRunner().update(connect, insertQuery, userId, tag, new Date());
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        }
        return 0;
    }

    @SneakyThrows
    public int delete(UserTag tag) {
        check(tag != null, "TagUser null");
        check(tag.getIdUser() != null, "idUser is null");
        String insertQuery = String.format("DELETE FROM %s WHERE %s=?", TABLE, ID.value());
        try {
            return new QueryRunner().update(connect, insertQuery, tag.getId());
        } catch (SQLException e) {
            TelegramException.throwIt(e);
        }
        return 0;
    }
}