package net.sytes.jaraya.repo;

import net.sytes.jaraya.enums.Property;
import net.sytes.jaraya.exception.TelegramException;
import net.sytes.jaraya.exception.UtilException;
import net.sytes.jaraya.util.Properties;
import org.apache.commons.dbutils.DbUtils;

import java.io.File;
import java.sql.*;

public class Repository implements AutoCloseable {

    protected final Connection connect;

    protected Repository() throws TelegramException {
        connect = initConnection();
    }


    protected Connection initConnection() throws TelegramException {
        try {
            if (this.connect == null) {
                new File("db/").mkdirs();
                return DriverManager.getConnection("jdbc:sqlite:db/" + Properties.get(Property.NAME_BOT.name()) + ".db");
            }
            return connect;
        } catch (SQLException | UtilException e) {
            throw new TelegramException(e);
        }
    }

    protected boolean tableExist(String tableName) throws TelegramException {

        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?;";

        try (PreparedStatement statement = connect.prepareStatement(sql)) {
            statement.setString(1, tableName);
            String ls = null;
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ls = rs.getString("name");
                }
            }
            return ls != null && !ls.isEmpty();

        } catch (SQLException e) {
            throw new TelegramException(e);
        }

    }

    @Override
    public void close() throws Exception {
        try {
            DbUtils.close(connect);
        } catch (SQLException e) {
            throw new TelegramException(e);
        }
    }
}
