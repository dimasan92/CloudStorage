package ru.geekbrains.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public void addNewFile(Context context, Uri uri) {
        executor.execute(() -> {
            String pathFrom = getPathByUri(context, uri);
            String[] parse = pathFrom.split("/");
            String filename = parse[parse.length - 1];
            if (copyFileToApp(context, pathFrom, filename)) {
                Message responseMsg = handler.obtainMessage();
                responseMsg.what = R.string.copy_file_success;
                responseMsg.obj = filename;
                handler.sendMessage(responseMsg);
            }
        });
    }

    public void sendFileToServer(ExecutorService executor, DataOutputStream outData,
                                 Context context, String path) {
        executor.execute(() -> {
            try (FileInputStream inFile = context.openFileInput(path)) {
                outData.writeLong(context.getFileStreamPath(path).length());
                System.out.println(context.getFileStreamPath(path).length());
                byte[] buffer = new byte[8 * 1024];
                int count;
                while ((count = inFile.read(buffer)) != -1) {
                    outData.write(buffer, 0, count);
                }
                outData.flush();
            } catch (FileNotFoundException e) {
                handler.sendEmptyMessage(R.string.file_not_found);
            } catch (IOException e) {
                handler.sendEmptyMessage(R.string.add_file_fail);
                e.printStackTrace();
            }
        });
    }

    public void deleteFileFromDevice(String filename, Context context) {
        executor.execute(() -> {
            Message message = handler.obtainMessage();
            message.obj = filename;
            if (context.deleteFile(filename)) {
                message.what = R.string.delete_file_from_device_success;
            } else {
                message.what = R.string.delete_file_from_device_fail;
            }
            handler.sendMessage(message);
        });
    }

    private String getPathByUri(Context context, Uri uri) {
        String path = null;
        String[] projection = {"_data"};
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("_data");
                if (cursor.moveToFirst()) {
                    path = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }
        return path;
    }

    private boolean copyFileToApp(Context context, String pathFrom, String pathTo) {
        File from = new File(pathFrom);
        try (FileInputStream inFile = new FileInputStream(from);
             FileOutputStream outFile = context.openFileOutput(pathTo, Context.MODE_PRIVATE)) {
            byte[] buffer = new byte[8 * 1024];
            int count;
            while ((count = inFile.read(buffer)) != -1) {
                outFile.write(buffer, 0, count);
            }
            outFile.flush();
            return true;
        } catch (FileNotFoundException e) {
            handler.sendEmptyMessage(R.string.file_not_found);
        } catch (IOException e) {
            handler.sendEmptyMessage(R.string.copy_file_error);
        }
        return false;
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
