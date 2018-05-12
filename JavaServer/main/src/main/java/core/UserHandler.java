package core;

import common.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

class UserHandler implements Runnable {

    private ServerCore server;
    private Socket socket;

    private DataInputStream inData;
    private DataOutputStream outData;

    private String nickname;

    UserHandler(ServerCore server, Socket socket) {
        this.server = server;
        this.socket = socket;
        nickname = "Никнейм не назначен";
//        files = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            this.inData = new DataInputStream(socket.getInputStream());
            this.outData = new DataOutputStream(socket.getOutputStream());
            try {
                // цикл авторизации/регистрации пользователей
                while (!socket.isClosed()) {
                    String requestMsg = inData.readUTF();
                    if (requestMsg.equals(Constants.END_SESSION)) {
                        throw new SocketException("Окончание сессии");
                    } else if (requestMsg.startsWith(Constants.REG_REQUEST)) {
                        if (reg(requestMsg)) break;
                    } else if (requestMsg.startsWith(Constants.AUTH_REQUEST)) {
                        if (auth(requestMsg)) break;
                    }
                }
//                // цикл общения сервера с пользователем
//                while (!socket.isClosed()) {
//                    String msg = inData.readUTF();
//                    if (msg.startsWith(Constants.ADD_FILE)) addFile(msg);
//                    else if (msg.startsWith(Constants.DELETE_FILE)) deleteFile(msg);
////                    else if (msg.equals(Constants.REPLACE_FILE)) replaceFile();
////                    else if (msg.equals(Constants.GET_FILE)) getFile();
////                    else if (msg.equals(Constants.END_SESSION)) break;
//                }
            } catch (SocketException | EOFException e) {
                System.out.println("Клиент " + nickname + " IP:" + socket.getInetAddress() +
                        " Порт:" + socket.getLocalPort() + " отсоединился");
            } catch (IOException e) {
                System.err.println("Ошибка при получении сообщений от клиента " + socket.getInetAddress() + " "
                        + socket.getLocalPort() + ". Ошибка: " + e + " " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при получении данных сокета клиента " + socket.getInetAddress() + " "
                    + socket.getLocalPort() + ". Ошибка: " + e.getMessage());
        } finally {
            nickname = null;
//            server.unsubscribe(this);
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при попытке закрытия сокета пользователя " + socket.getInetAddress() + " "
                            + socket.getLocalPort() + ". Ошибка: " + e.getMessage());
                }
            }
        }
    }

    // запрос на регистрацию
    private boolean reg(String requestMsg) {
        String responseMsg = server.getRegService().registrationOfUser(requestMsg);
        sendMsg(responseMsg);
//        if (responseMsg.equals(Constants.REG_SUCCESS)) {
//            // разбираем на массив {"/reg", "nickname login password"}
//            // при разборе в auth - nickname отбрасывается
//            String subMsg = requestMsg.split("\\s", 2)[1];
//            return auth(subMsg);
//        }
        return false;
    }

    // запрос на авторизацию
    private boolean auth(String requestMsg) {
//        String[] auth = server.getAuthService().getAuthorizedUser(requestMsg, server.getListOfUsers());
//        nickname = auth[0];
//        sendMsg(auth[1]);
//        if (nickname != null) {
//            sendMsg(nickname);
//            server.subscribe(this);
//            folder = server.getStorageService().getUserFolder(nickname);
//            if (folder.list() != null) {
//                Collections.addAll(files, folder.list());
//            }
//            return true;
//        }
        return false;
    }

    // отправка сообщений клиенту
    private void sendMsg(String msg) {
        try {
            outData.writeUTF(msg);
        } catch (IOException e) {
            System.err.println("Не удалось отправить сообщение клиенту " + e.getMessage());
        }
    }
}
