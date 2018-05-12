package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CloudStorageDatabase implements Database {

    private Connection connection;  // подключение к БД

    public Connection getConnection() {
        return connection;
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            if ((connection = DriverManager.getConnection("jdbc:sqlite:cloudstorage.db")) != null)
                System.out.println("Успешное подключение к БД");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Не удалось подключиться к БД - " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при попытке закрытия соединения с БД - " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Соединение c БД нельзя закрыть, так как оно не было открыто: " + e.getMessage());
        }
    }
}
