package ru.geekbrains.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileStorageService extends Service {

    private FileBinder binder; // экземпляр для работы с сервисом из Activity
    private ExecutorService executor;

    private Handler handler;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new FileBinder();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    public class FileBinder extends Binder {
        public FileStorageService getService() {
            return FileStorageService.this;
        }
    }
}
