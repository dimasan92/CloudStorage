package ru.geekbrains.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnectionService extends Service {

    private ExecutorService executor;    // управляет потоками отправки и получения сообщений

    private ConnectBinder binder; // экземпляр для работы с сервисом из Activity
    private boolean connected;    // показывет, плдключен ли клиент к серверу

    //вызывается при запуске сервиса
    @Override
    public void onCreate() {
        super.onCreate();
        binder = new ConnectBinder();
        executor = Executors.newFixedThreadPool(2);
        connected = false;
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
