package core;

import common.Constants;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class UserHandler implements Runnable {

    private ServerCore server;
    private Socket socket;

    private DataInputStream inData;
    private DataOutputStream outData;

    private String nickname;

    private File folder;
    private List<String> files;

    String getNickname() {
        return nickname;
    }

    UserHandler(ServerCore server, Socket socket) {
        this.server = server;
        this.socket = socket;
        nickname = "Никнейм не назначен";
        files = new ArrayList<>();
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
                // цикл общения сервера с пользователем
                while (!socket.isClosed()) {
                    String msg = inData.readUTF();
                    if (msg.startsWith(Constants.ADD_FILE)) addFile(msg);
                    else if (msg.startsWith(Constants.DELETE_FILE)) deleteFile(msg);
                    else if (msg.startsWith(Constants.LIST_OF_FILES_GET)) sendListOfFiles();
////                    else if (msg.equals(Constants.REPLACE_FILE)) replaceFile();
////                    else if (msg.equals(Constants.GET_FILE)) getFile();
////                    else if (msg.equals(Constants.END_SESSION)) break;
                }
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
        if (responseMsg.equals(Constants.REG_SUCCESS)) {
            // разбираем на массив {"/reg", "nickname login password"}
            // при разборе в auth - nickname отбрасывается
            String subMsg = requestMsg.split("\\s", 2)[1];
            return auth(subMsg);
        }
        return false;
    }

    // запрос на авторизацию
    private boolean auth(String requestMsg) {
        String[] auth = server.getAuthService().getAuthorizedUser(requestMsg, server.getListOfUsers());
        nickname = auth[0];
        sendMsg(auth[1]);
        if (nickname != null) {
            sendMsg(nickname);
            server.subscribe(this);
            folder = server.getStorageService().getUserFolder(nickname);
            if (folder.list() != null) {
                Collections.addAll(files, folder.list());
            }
            return true;
        }
        return false;
    }

    // добавоение файла на сервер
    private void addFile(String msg) {
        String filename = msg.split("\\s")[1];
        System.out.println("От пользователя " + nickname + " IP:" + socket.getInetAddress() +
                " Порт:" + socket.getLocalPort() + " поступил запрос на добавление файла: " + filename);
        if (!files.contains(filename)) {
            sendMsg(Constants.ADD_FILE_RESPONSE + " " + filename);
            if (server.getStorageService().writeFileFromUser(inData, folder + "\\" + filename)) {
                files.add(filename);
                sendMsg(Constants.ADD_FILE_SUCCESS);
                System.out.println("Файл " + filename + " добавлен");
            } else {
                sendMsg(Constants.ADD_FILE_FAIL);
            }
        } else {
            sendMsg(Constants.ADD_FILE_ALREADY);
            System.out.println("Файл уже добавлен: " + filename);
        }
    }

    private void deleteFile(String msg) {
        String filename = msg.split("\\s")[1];
        System.out.println("От пользователя " + nickname + " IP:" + socket.getInetAddress() +
                " Порт:" + socket.getLocalPort() + " поступил запрос на удаление файла: " + filename);
        if (files.contains(filename)) {
            if (server.getStorageService().removeFile(folder + "\\" + filename)) {
                files.remove(filename);
                sendMsg(Constants.DELETE_FILE_SUCCESS);
                System.out.println("Файл " + filename + " удален");
            } else {
                sendMsg(Constants.DELETE_FILE_FAIL);
                System.out.println("Не удалось удалить файл " + filename);
            }
        } else {
            sendMsg(Constants.DELETE_FILE_NOT_EXIST);
            System.out.println("Файл не существует на сервере " + filename);
        }
    }

    private void sendListOfFiles() {
        StringBuilder responseString = new StringBuilder();
        responseString.append(Constants.LIST_OF_FILES_SEND).append(" ");
        for (String file : files) {
            responseString.append(file).append(" ");
        }
        sendMsg(responseString.toString());
    }

//    private void replaceFile() {
//        try {
//            String filename = inData.readUTF();
//            System.out.println("От пользователя " + nickname + " IP:" + socket.getInetAddress() +
//                    " Порт:" + socket.getLocalPort() + " поступил запрос на замену файла: " + filename);
//            if (files.contains(filename) &&
//                    server.getStorageService().replaceFile(inData, folder + filename)) {
//                sendMsg(Constants.REPLACE_FILE_SUCCESS);
//                System.out.println("Файл " + filename + " заменен");
//            } else {
//                sendMsg(Constants.REPLACE_FILE_FAIL);
//                System.out.println("Не удалось заменить файл: " + filename);
//            }
//        } catch (IOException e) {
//            System.out.println("Ошибка при получении файла от пользователя " + socket.getInetAddress() + " "
//                    + socket.getLocalPort() + ". Ошибка: " + e.getMessage());
//        }
//    }

//    private void getFile() {
//        try {
//            String filename = inData.readUTF();
//            System.out.println("От пользователя " + nickname + " IP:" + socket.getInetAddress() +
//                    " Порт:" + socket.getLocalPort() + " поступил запрос на получение файла: " + filename);
//            if (files.contains(filename)) {
//                server.getStorageService().sendFileToUser(outData, folder.getPath(), filename);
//                sendMsg(Constants.GET_FILE_SUCCESS);
//                System.out.println("Файл " + filename + " отправлен");
//            } else {
//                sendMsg(Constants.GET_FILE_FAIL);
//                System.out.println("Не удалось отправить файл " + filename);
//            }
//        } catch (IOException e) {
//            System.out.println("Ошибка при передаче файла пользователю " + socket.getInetAddress() + " "
//                    + socket.getLocalPort() + ". Ошибка: " + e.getMessage());
//        }
//    }

    // отправка сообщений клиенту
    private void sendMsg(String msg) {
        try {
            outData.writeUTF(msg);
        } catch (IOException e) {
            System.err.println("Не удалось отправить сообщение клиенту " + e.getMessage());
        }
    }
}
