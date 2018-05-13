package common;

public interface MessageHandler {

    enum TYPE {ERROR, NOTIFY}

    void message(String message, TYPE type);
}
