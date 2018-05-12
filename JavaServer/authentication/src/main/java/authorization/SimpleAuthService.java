package authorization;

import common.Constants;
import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SimpleAuthService implements AuthService {
    private Connection connection;

    private PreparedStatement psGetUser;

    public SimpleAuthService(Database database) {
        this.connection = database.getConnection();
        preparedStatements();
        System.out.println("Успешный запуск сервиса авторизации");
    }

    @Override
    public String[] getAuthorizedUser(String msg, List<String> listOfConnectedUsers) {
        String[] result = {null, null};
        // строка вида data[0] = /auth data[1] = login data[2] = password
        String[] data = msg.split("\\s");
        if (data.length == 3) {
            synchronized (Database.LOCK) {
                try {
                    String nickname = getUserByLoginAndPass(data[1], data[2]);
                    // проверка, чтобы ник существовал в БД
                    if (nickname != null) {
                        // проверка, чтобы ник не был занят
                        if (!listOfConnectedUsers.contains(nickname)) {
                            result[0] = nickname;
                            result[1] = Constants.AUTH_SUCCESS;
                        } else {
                            result[1] = Constants.AUTH_NICK_IS_BUSY;
                        }
                    } else {
                        result[1] = Constants.AUTH_NICK_NOT_EXIST;
                    }
                } catch (SQLException e) {
                    System.out.println("Сервис авторизации: ошибка доступа к БД при получении имени пользователя: " + e.getMessage());
                }
            }
        } else {
            result[1] = Constants.AUTH_FAILURE;
        }
        return result;
    }

    // подготовленные запросы
    private void preparedStatements() {
        try {
            psGetUser = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
        } catch (SQLException e) {
            System.out.println("Сервис авторизации: ошибка доступа к БД :" + e.getMessage());
        }
    }

    private String getUserByLoginAndPass(String login, String pass) throws SQLException {
        psGetUser.setString(1, login);
        psGetUser.setString(2, pass);
        try (ResultSet resultSet = psGetUser.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }
        }
        return null;
    }
}
