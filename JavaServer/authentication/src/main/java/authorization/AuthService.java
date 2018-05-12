package authorization;

import java.util.List;

public interface AuthService {
    // возвращает массив вида: [никнейм, сообщение}
    String[] getAuthorizedUser(String msg, List<String> listOfConnectedUsers);
}
