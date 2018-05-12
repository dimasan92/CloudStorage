package ru.geekbrains.main.storage;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.geekbrains.main.R;
import ru.geekbrains.services.FileStorageService;
import ru.geekbrains.services.ServerConnectionService;

public class StorageActivity extends AppCompatActivity {
    // сервисы
    private boolean boundFiles;
    private boolean boundServer;
        private ServerConnectionService serverConnectionService;
        private FileStorageService fileStorageService;
    private ServiceConnection serverServiceConnection;
    private ServiceConnection filesServiceConnection;
    // список
    private ArrayList<Map<String, Object>> data;
    private SimpleAdapter simpleAdapter;
    private static final String LIST_ATTRIBUTE_TEXT = "text";
    private static final String LIST_ATTRIBUTE_IMAGE = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        makeList();

        // привязываемся к сервисам
        boundServer = false;
        boundFiles = false;
        serverServiceConnection = serverServiceConnection();
        filesServiceConnection = storageServiceConnection();
        bindService(new Intent(this, ServerConnectionService.class), serverServiceConnection, 0);
        bindService(new Intent(this, FileStorageService.class), filesServiceConnection, 0);
    }

    // создание списка из storage_fileslist_item.xml элементов
    private void makeList() {
        data = new ArrayList<>();
        String[] from = {LIST_ATTRIBUTE_TEXT, LIST_ATTRIBUTE_IMAGE};
        int[] to = {R.id.tv_item_text, R.id.iv_item_image};
        simpleAdapter = new SimpleAdapter(this, data, R.layout.storage_fileslist_item, from, to);
        ListView lvFiles = findViewById(R.id.lv_list_of_files);
        lvFiles.setAdapter(simpleAdapter);
        // разрешение вызова контекстного меню для элементов списка
        registerForContextMenu(lvFiles);
        for (String file : fileList()) {
            addFileToList(file);
        }
    }

    // добавляет строку в список
    private void addFileToList(String filename) {
        Map<String, Object> item = new HashMap<>();
        item.put(LIST_ATTRIBUTE_TEXT, filename);
        item.put(LIST_ATTRIBUTE_IMAGE, getImageByExt(filename));
        data.add(item);
        simpleAdapter.notifyDataSetChanged();
    }

    // добавляет изображение по расширению файла
    private int getImageByExt(String filename) {
        String[] temp = filename.split("\\.");
        String ext = temp[temp.length - 1];
        switch (ext) {
            case "mp3":
                return R.drawable.mp3128;
            case "jpeg":
            case "jpg":
                return R.drawable.jpeg128;
            case "pdf":
                return R.drawable.pdf128;
            case "txt":
                return R.drawable.text128;
            case "zip":
            case "rar":
                return R.drawable.zip128;
            default:
                return R.drawable.default128;
        }
    }

    // экземпляр для подключения к сервису соединения с сервером
    private ServiceConnection serverServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serverConnectionService =
                        ((ServerConnectionService.ConnectBinder) service).getService();
                serverConnectionService.setHandler(storageHandler());
                boundServer = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                boundServer = false;
            }
        };
    }

    // экземпляр для подключения к сервису работы с файлами
    private ServiceConnection storageServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                fileStorageService =
                        ((FileStorageService.FileBinder) service).getService();
                fileStorageService.setHandler(storageHandler());
                boundFiles = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                boundFiles = false;
            }
        };
    }

        @SuppressLint("HandlerLeak")
    private Handler storageHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
//                    case R.string.copy_file_success:
//                        Toast.makeText(StorageActivity.this, R.string.copy_file_success,
//                                Toast.LENGTH_SHORT).show();
//                        String filename = (String) msg.obj;
//                        addFileToList(filename);
//                        serverConnectionService.sendMsg(Constants.ADD_FILE + " " + filename);
//                        break;
//                    case R.string.file_not_found:
//                        Toast.makeText(StorageActivity.this, R.string.file_not_found,
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    case R.string.copy_file_error:
//                        Toast.makeText(StorageActivity.this, R.string.copy_file_error,
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    case R.string.add_file_processing:
//                        Toast.makeText(StorageActivity.this, R.string.add_file_processing,
//                                Toast.LENGTH_SHORT).show();
//                        fileStorageService.sendFileToServer(serverConnectionService.getExecutor(),
//                                serverConnectionService.getOutData(), StorageActivity.this,
//                                (String) msg.obj);
//                        break;
//                    case R.string.add_file_already:
//                        Toast.makeText(StorageActivity.this, R.string.add_file_already,
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    case R.string.add_file_success:
//                        Toast.makeText(StorageActivity.this, R.string.add_file_success,
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case R.string.add_file_fail:
//                        Toast.makeText(StorageActivity.this, R.string.add_file_fail,
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    case R.string.delete_file_from_device_success:
//                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_device_success,
//                                Toast.LENGTH_SHORT).show();
//                        serverConnectionService.sendMsg(Constants.DELETE_FILE + " " + msg.obj);
//                        break;
//                    case R.string.delete_file_from_device_fail:
//                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_device_fail,
//                                Toast.LENGTH_LONG).show();
//                        addFileToList((String) msg.obj);
//                        break;
//                    case R.string.delete_file_from_server_success:
//                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_server_success,
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case R.string.delete_file_from_server_not_exist:
//                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_server_not_exist,
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    case R.string.delete_file_from_server_fail:
//                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_server_fail,
//                                Toast.LENGTH_LONG).show();
//                        break;
                }
            }
        };
    }
}
