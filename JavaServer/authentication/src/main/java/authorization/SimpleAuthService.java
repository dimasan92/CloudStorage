package authorization;

import common.Constants;
import common.MessageHandler;
import common.MessageHandler.TYPE;
import database.Database;
import security.PasswordStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private Connection connection;
    private PreparedStatement psGetUserHash;
    private MessageHandler handler;

    public SimpleAuthService(Database database, MessageHandler handler) {
        this.connection = database.getConnection();
        preparedStatements();
        this.handler = handler;
        handler.message("Успешный запуск сервиса авторизации", TYPE.NOTIFY);
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
                    handler.message("Сервис авторизации: ошибка доступа к БД при получении имени пользователя: "
                            + e.getMessage(), TYPE.ERROR);
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
            psGetUserHash = connection.prepareStatement("SELECT * FROM users WHERE login = ?;");
        } catch (SQLException e) {
            handler.message("Сервис авторизации: ошибка доступа к БД :" + e.getMessage(), TYPE.ERROR);
        }
    }

    private String getUserByLoginAndPass(String login, String password) throws SQLException {
        psGetUserHash.setString(1, login);
        try (ResultSet resultSet = psGetUserHash.executeQuery()) {
            if (resultSet.next()) {
                if (!PasswordStorage.verifyPassword(password, resultSet.getString("pass")))
                    return null;
                return resultSet.getString("nickname");
            }
        } catch (PasswordStorage.CannotPerformOperationException | PasswordStorage.InvalidHashException e) {
            handler.message(e.getMessage(), TYPE.ERROR);
        }
        return null;
    }
}
