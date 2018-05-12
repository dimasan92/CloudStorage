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

    // данные пользователей
    public static final String SERVER_MAIN_DIRECTORY = "UserData\\";

    // сообщения клиент/сервер
    public static final String ADD_FILE = "/addFile";
    public static final String ADD_FILE_RESPONSE = "/addFileResponse";
    public static final String ADD_FILE_SUCCESS = "/addFileSuccess";
    public static final String ADD_FILE_ALREADY = "/addFileAlready";
    public static final String ADD_FILE_FAIL = "/addFileFail";
    public static final String DELETE_FILE = "/deleteFile";
    public static final String DELETE_FILE_SUCCESS = "/deleteFileSuccess";
    public static final String DELETE_FILE_NOT_EXIST = "/deleteFileNotExist";
    public static final String DELETE_FILE_FAIL = "/deleteFileFail";
    public static final String REPLACE_FILE = "/replaceFile";
    public static final String REPLACE_FILE_SUCCESS = "/replaceFileSuccess";
    public static final String REPLACE_FILE_FAIL = "/replaceFileFail";
    public static final String GET_FILE = "/getFile";
    public static final String GET_FILE_SUCCESS = "/getFileSuccess";
    public static final String GET_FILE_FAIL = "/getFileFail";
    public static final String END_SESSION = "/end";
}
