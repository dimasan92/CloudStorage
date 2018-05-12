package core;

import common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerCore {

    private ServerSocket serverSocket;              // сокет, определяемый при создании сервера

    private List<UserHandler> connectedUsers;       // список подключенных на текущий момент клиентов
    private ExecutorService usersExecutorService;   // управление нитями для пользователей

    public ServerCore() {
        connectedUsers = Collections.synchronizedList(new ArrayList<>());
//        Database dataBase = new CloudStorageDatabase();
//        dataBase.connect();
//        storageService = new SimpleFileStorage(dataBase);
//        authService = new SimpleAuthService(dataBase);
//        regService = new SimpleRegService(dataBase, storageService);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            new Thread(startServer()).start();
            startInputTerminal(br);
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера(порт " + Constants.SERVER_PORT + "): " + e.getMessage());
        } finally {
//            dataBase.disconnect();
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии сервера(порт " + Constants.SERVER_PORT + ") :" + e.getMessage());
            }
        }
    }

    // обработка команд управления сервером
    private void startInputTerminal(BufferedReader br) {
        while (true) {
            try {
                String s = br.readLine();
                switch (s) {
                    case Constants.HELP:
                        System.out.println("Возможные команды для сервера:");
                        System.out.println(Constants.RESTART + " - перезапуск сервера");
                        System.out.println(Constants.EXIT + " - завершение работы сервера");
                        System.out.println(Constants.USER_LIST + " - подключенные пользователи");
                        break;
                    case Constants.RESTART:
                        serverSocket.close();
                        System.out.println("Перезапуск сервера(порт " + Constants.SERVER_PORT + ")");
                        new Thread(startServer()).start();
                        break;
                    case Constants.EXIT:
                        System.out.println("Остановка работы сервера(порт " + Constants.SERVER_PORT + ")");
                        return;
                    case Constants.USER_LIST:
                        System.out.println("Подключеные пользователи:");
//                        List<String> users = getListOfUsers();
//                        for (String user : users)
//                            System.out.println(user);
                        break;
                }
            } catch (IOException e) {
                System.err.println("Ошибка при связи с терминалом сервера(порт " + Constants.SERVER_PORT + "): " + e.getMessage());
            }
        }
    }

    // запуск сервера(нить, ответственная за обработку подключений клиентов)
    private Runnable startServer() {
        return () -> {
            connectedUsers.clear();
            usersExecutorService = Executors.newCachedThreadPool();
            try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
                System.out.println("Сервер(порт " + Constants.SERVER_PORT + ") запущен...Ожидание подключения клиентов...");
                usersProcessing();
            } catch (SocketException e) {
                System.err.println("Сервер(порт " + Constants.SERVER_PORT + ") прекратил обработку" +
                        " клиентов по причине: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("При запуске сервера(порт " + Constants.SERVER_PORT + ") произошла ошибка: " + e.getMessage());
            }
        };
    }

    // работа с клиентами
    private void usersProcessing() throws IOException{
        // получение данных от подключившихся пользователей и создание для них отдельных нитей в пуле
        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            System.out.println("Клиент присоединился: IP: " + socket.getInetAddress() + " Порт: " + socket.getLocalPort());
            usersExecutorService.submit(new UserHandler(this, socket));
        }
    }
}
