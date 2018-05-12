package registration;

import common.Constants;
import database.Database;
import storage.FileStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleRegService implements RegService {

    private Connection connection;
    private FileStorage storage;

    private PreparedStatement psAddNewUser;
    private PreparedStatement psIsUserExists;

    public SimpleRegService(Database database, FileStorage storage) {
        connection = database.getConnection();
        this.storage = storage;
        preparedStatements();
        System.out.println("Успешный запуск сервиса регистрации");
    }

    @Override
    public String registrationOfUser(String msg) {
        String[] data = msg.split("\\s");
        // data[0] = /reg; data[1] = nickname; data[2] = login; data[3] = password
        synchronized (Database.LOCK) {
            try {
                if (isUserExist(data[1], data[2]))
                    return Constants.REG_NICK_ALREADY_EXIST;
                if (data.length == 4 && addNewUser(data[1], data[2], data[3]))
                    return Constants.REG_SUCCESS;
            } catch (SQLException e) {
                System.err.println("Сервис регистрации: ошибка доступа к БД при добавлении нового пользователя:" + e.getMessage());
            }
        }
        return Constants.REG_FAILURE;
    }

    // подготовленные запросы
    private void preparedStatements() {
        try {
            psAddNewUser = connection.prepareStatement("INSERT INTO users (nickname, login, password) VALUES (?, ?, ?);");
            psIsUserExists = connection.prepareStatement("SELECT* FROM users WHERE nickname = ? OR login = ?;");
        } catch (SQLException e) {
            System.err.println("Сервис регистрации: ошибка доступа к БД :" + e.getMessage());
        }
    }

    // добавляет нового пользователя в БД
    private boolean addNewUser(String nickname, String login, String pass) throws SQLException {
        psAddNewUser.setString(1, nickname);
        psAddNewUser.setString(2, login);
        psAddNewUser.setString(3, pass);
        int update = psAddNewUser.executeUpdate();
        if (!storage.assignFolderToUser(nickname)) return false;
        return update > 0;
    }

    // проверяет, существует ли пользователь с таким ником или логином
    private boolean isUserExist(String nickname, String login) throws SQLException {
        psIsUserExists.setString(1, nickname);
        psIsUserExists.setString(2, login);
        try (ResultSet rs = psIsUserExists.executeQuery()) {
            return rs.next();
        }
    }
}
