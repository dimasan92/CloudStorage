package common;

public class Constants {
    // порт приложения "Сервер"
    public static final int SERVER_PORT = 8599;

    // команды для управления
    public static final String HELP = "/help";
    public static final String EXIT = "/exit";
    public static final String RESTART = "/restart";
    public static final String USER_LIST = "/user_list";

    // команды аутентификации
    public static final String REG_REQUEST = "/reg";
    public static final String REG_SUCCESS = "/regOk";
    public static final String REG_NICK_ALREADY_EXIST = "/regNickExist";
    public static final String REG_FAILURE = "/regFail";
    public static final String AUTH_REQUEST = "/auth";
    public static final String AUTH_SUCCESS = "/authOk";
    public static final String AUTH_NICK_IS_BUSY = "/authNickBusy";
    public static final String AUTH_NICK_NOT_EXIST = "/authNickNotExist";
    public static final String AUTH_FAILURE = "/authFail";

    // сообщения клиент/сервер
    public static final String END_SESSION = "/end";
}
