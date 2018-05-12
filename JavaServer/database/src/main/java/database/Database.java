package database;

import java.sql.Connection;

public interface Database {
    Object LOCK = new Object(); //используется для синхронизации обращений к БД

    Connection getConnection();
    void connect();
    void disconnect();
}
