package ru.geekbrains.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import common.Constants;

public class ServerConnectionService extends Service {

    private static final String IP_SERVER = "192.168.1.52";

    private Socket socket;
    private DataInputStream inData;
    private DataOutputStream outData;

    private Handler handler;            // сообщения от сервера в активити
    private ExecutorService executor;   // управляет потоками отправки и получения сообщений

    private ConnectBinder binder; // экземпляр для работы с сервисом из Activity
    private boolean connected;    // показывет, плдключен ли клиент к серверу

    private String nickname;


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    //вызывается при запуске сервиса
    @Override
    public void onCreate() {
        super.onCreate();
        binder = new ConnectBinder();
        executor = Executors.newFixedThreadPool(2);
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    // подключение к серверу
    public void connect() {
        executor.submit(() -> {
            try {
                socket = new Socket(IP_SERVER, Constants.SERVER_PORT);
                inData = new DataInputStream(socket.getInputStream());
                outData = new DataOutputStream(socket.getOutputStream());
                connected = true;
                listeningToServer();
            } catch (IOException e) {
                handler.sendEmptyMessage(R.string.server_attempt_fail);
            }
        });
    }

    public void sendMsg(String msg) {
        executor.submit(() -> {
            try {
                long beginTime = System.currentTimeMillis();
                while (!connected) {
                    long time = System.currentTimeMillis() - beginTime;
                    if (time >= 5000) {
                        throw new IOException();
                    }
                }
                outData.writeUTF(msg);
                outData.flush();
            } catch (IOException e) {
                handler.sendEmptyMessage(R.string.server_lost);
            }
        });
    }

        private void listeningToServer() {
        try {
            // регистрация/авторизация
            socket.setSoTimeout(30000);
            label:
            while (!socket.isClosed()) {
                String msg = inData.readUTF();
                switch (msg) {
                    case Constants.REG_SUCCESS:
                        handler.sendEmptyMessage(R.string.reg_success);
                        break;
                    case Constants.REG_NICK_ALREADY_EXIST:
                        handler.sendEmptyMessage(R.string.reg_nick_already_exist);
                        break;
                    case Constants.REG_FAILURE:
                        handler.sendEmptyMessage(R.string.reg_failure);
                        break;
                    case Constants.AUTH_SUCCESS:
                        nickname = inData.readUTF();
                        handler.sendEmptyMessage(R.string.auth_success);
                        break label;
                    case Constants.AUTH_NICK_IS_BUSY:
                        handler.sendEmptyMessage(R.string.auth_nick_is_busy);
                        break;
                    case Constants.AUTH_NICK_NOT_EXIST:
                        handler.sendEmptyMessage(R.string.auth_nick_not_exist);
                        break;
                    case Constants.AUTH_FAILURE:
                        handler.sendEmptyMessage(R.string.auth_failure);
                        break;
                }
            }
//            // общение с сервером
//            socket.setSoTimeout(0);
//            while (!socket.isClosed()) {
//                String msg = inData.readUTF();
//                if (msg.startsWith("/")) {
//                    if (msg.startsWith(Constants.ADD_FILE_RESPONSE)) {
//                        Message message = handler.obtainMessage();
//                        message.what = R.string.add_file_processing;
//                        message.obj = msg.split("\\s")[1];
//                        handler.sendMessage(message);
//                    } else if (msg.equals(Constants.ADD_FILE_ALREADY)) {
//                        handler.sendEmptyMessage(R.string.add_file_already);
//                    } else if (msg.equals(Constants.ADD_FILE_SUCCESS)) {
//                        handler.sendEmptyMessage(R.string.add_file_success);
//                    } else if (msg.equals(Constants.ADD_FILE_FAIL)) {
//                        handler.sendEmptyMessage(R.string.add_file_fail);
//                    } else if (msg.equals(Constants.DELETE_FILE_SUCCESS)) {
//                        handler.sendEmptyMessage(R.string.delete_file_from_server_success);
//                    } else if (msg.equals(Constants.DELETE_FILE_NOT_EXIST)) {
//                        handler.sendEmptyMessage(R.string.delete_file_from_server_not_exist);
//                    } else if (msg.equals(Constants.DELETE_FILE_FAIL)) {
//                        handler.sendEmptyMessage(R.string.delete_file_from_server_fail);
//                    }
//                }
//            }
        } catch (SocketException e) {
        } catch (IOException e) {
            handler.sendEmptyMessage(R.string.server_lost);
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        connected = false;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        executor.shutdown();
    }

    // возвращает Binder в методе onServiceConnected()
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ConnectBinder extends Binder {
        public ServerConnectionService getService() {
            return ServerConnectionService.this;
        }
    }
}
