package core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class UserHandler implements Runnable {

    private ServerCore server;
    private Socket socket;

    private DataInputStream inData;
    private DataOutputStream outData;

    UserHandler(ServerCore server, Socket socket) {
        this.server = server;
        this.socket = socket;
//        files = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            this.inData = new DataInputStream(socket.getInputStream());
            this.outData = new DataOutputStream(socket.getOutputStream());
//            try {
//                // цикл авторизации/регистрации пользователей
//                while (!socket.isClosed()) {
//                    String requestMsg = inData.readUTF();
//                    if (requestMsg.equals(Constants.END_SESSION)) {
//                        throw new SocketException("Окончание сессии");
//                    } else if (requestMsg.startsWith(Constants.REG_REQUEST)) {
//                        if (reg(requestMsg)) break;
//                    } else if (requestMsg.startsWith(Constants.AUTH_REQUEST)) {
//                        if (auth(requestMsg)) break;
//                    }
//                }
//                // цикл общения сервера с пользователем
//                while (!socket.isClosed()) {
//                    String msg = inData.readUTF();
//                    if (msg.startsWith(Constants.ADD_FILE)) addFile(msg);
//                    else if (msg.startsWith(Constants.DELETE_FILE)) deleteFile(msg);
////                    else if (msg.equals(Constants.REPLACE_FILE)) replaceFile();
////                    else if (msg.equals(Constants.GET_FILE)) getFile();
////                    else if (msg.equals(Constants.END_SESSION)) break;
//                }
//            } catch (SocketException | EOFException e) {
//                System.out.println("Клиент " + nickname + " IP:" + socket.getInetAddress() +
//                        " Порт:" + socket.getLocalPort() + " отсоединился");
//            } catch (IOException e) {
//                System.err.println("Ошибка при получении сообщений от клиента " + socket.getInetAddress() + " "
//                        + socket.getLocalPort() + ". Ошибка: " + e + " " + e.getMessage());
//                e.printStackTrace();
//            }
        } catch (IOException e) {
            System.err.println("Ошибка при получении данных сокета клиента " + socket.getInetAddress() + " "
                    + socket.getLocalPort() + ". Ошибка: " + e.getMessage());
//        } finally {
//            nickname = null;
//            server.unsubscribe(this);
//            if (!socket.isClosed()) {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    System.err.println("Ошибка при попытке закрытия сокета пользователя " + socket.getInetAddress() + " "
//                            + socket.getLocalPort() + ". Ошибка: " + e.getMessage());
//                }
//            }
        }
    }
}
