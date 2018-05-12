package ru.geekbrains.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServerConnectionService extends Service {
    public ServerConnectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
