package net.sytes.jaraya.repo;

import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.model.Report;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ReportRepo extends Repository {

    private static final String TABLE = "report";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ID_USER = "user";
    private static final String COLUMN_CREATION = "datecreation";

    public ReportRepo() throws TelegramException {
        super();
        preparing();

    }

    private void preparing() throws TelegramException {
        if (!tableExist(TABLE)) {
            String sql = String.format(
                    "create table %s (%s INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s DATETIME NOT NULL)", TABLE,
                    COLUMN_ID, COLUMN_ID_USER, COLUMN_CREATION);
            log.info(sql);
            try {
                new QueryRunner().update(connect, sql);
            } catch (SQLException e) {
                throw new TelegramException(e);
            }

        }
    }

    public List<Report> getByIdUser(Long idUser) throws TelegramException {
        if (Objects.isNull(idUser)) {
            return new ArrayList<>();
        }
        List<Report> reports;
        try {
            reports = new QueryRunner().query(connect, "SELECT * FROM " + TABLE +
                            " WHERE " + COLUMN_ID_USER + "=?"
                    ,
                    new BeanListHandler<Report>(Report.class), idUser)
                    .stream()
                    .filter(x -> x.getDatecreation().after(new Timestamp(System.currentTimeMillis() - 86400000))).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
        return reports;
    }

    public Integer save(Report user) throws TelegramException {
        if (Objects.isNull(user) || Objects.isNull(user.getUser())) {
            return null;
        }
        if (Objects.isNull(user.getId())) {
            String insertQuery = "INSERT INTO " + TABLE + "(" + COLUMN_ID_USER + "," + COLUMN_CREATION + ") VALUES (?,?)";
            try {
                return new QueryRunner().update(connect, insertQuery, user.getUser(),
                        new java.util.Date());
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        } else {
            String updateQuery = "UPDATE " + TABLE + " SET " + COLUMN_ID_USER + "=?," + "=? WHERE " + COLUMN_ID + "=?";
            try {
                return new QueryRunner().update(connect, updateQuery, user.getUser(), user.getId());
            } catch (SQLException e) {
                throw new TelegramException(e);
            }
        }

    }
}
