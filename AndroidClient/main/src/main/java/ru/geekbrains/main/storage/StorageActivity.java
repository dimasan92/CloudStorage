package ru.geekbrains.main.storage;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.Constants;
import ru.geekbrains.main.R;
import ru.geekbrains.main.authentication.AuthenticationActivity;
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

    // id для меню
    private static final int MENU_DELETE = 0;
    private static final int MENU_ADD = 1;
    private static final int MENU_SYNCHRONIZE = 2;

    // код возвращаемых сообщений
    private static final int FILE_SELECT_CODE = 0;

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

    // удаляет строку из списка
    private void deleteFileFromList(int position) {
        data.remove(position);
        simpleAdapter.notifyDataSetChanged();
    }

    // работа с главным и контекстным меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD, 0, R.string.add_file);
        menu.add(0, MENU_SYNCHRONIZE, 0, R.string.synchronize_files);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                showFileChooser();
                break;
            case MENU_SYNCHRONIZE:
                serverConnectionService.sendMsg(Constants.LIST_OF_FILES_GET);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(0, MENU_DELETE, 0, R.string.delete_file);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_DELETE) {
            AdapterContextMenuInfo adapter = (AdapterContextMenuInfo) item.getMenuInfo();
            String filename = (String) data.get(adapter.position).get(LIST_ATTRIBUTE_TEXT);
            fileStorageService.deleteFileFromDevice(filename, this);
            deleteFileFromList(adapter.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    // открыть менеджер файлов
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, getResources().getString(R.string.choose_file)), FILE_SELECT_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.not_filechooser, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // получение результата после вызова менеджера фалов
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    fileStorageService.addNewFile(this, data.getData());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                    case R.string.copy_file_success:
                        Toast.makeText(StorageActivity.this, R.string.copy_file_success,
                                Toast.LENGTH_SHORT).show();
                        String filename = (String) msg.obj;
                        addFileToList(filename);
                        serverConnectionService.sendMsg(Constants.ADD_FILE + " " + filename);
                        break;
                    case R.string.file_not_found:
                        Toast.makeText(StorageActivity.this, R.string.file_not_found,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.copy_file_error:
                        Toast.makeText(StorageActivity.this, R.string.copy_file_error,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.add_file_processing:
                        Toast.makeText(StorageActivity.this, R.string.add_file_processing,
                                Toast.LENGTH_SHORT).show();
                        fileStorageService.sendFileToServer(serverConnectionService.getExecutor(),
                                serverConnectionService.getOutData(), StorageActivity.this,
                                (String) msg.obj);
                        break;
                    case R.string.add_file_already:
                        Toast.makeText(StorageActivity.this, R.string.add_file_already,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.add_file_success:
                        Toast.makeText(StorageActivity.this, R.string.add_file_success,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.string.add_file_fail:
                        Toast.makeText(StorageActivity.this, R.string.add_file_fail,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.delete_file_from_device_success:
                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_device_success,
                                Toast.LENGTH_SHORT).show();
                        serverConnectionService.sendMsg(Constants.DELETE_FILE + " " + msg.obj);
                        break;
                    case R.string.delete_file_from_device_fail:
                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_device_fail,
                                Toast.LENGTH_LONG).show();
                        addFileToList((String) msg.obj);
                        break;
                    case R.string.delete_file_from_server_success:
                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_server_success,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.string.delete_file_from_server_not_exist:
                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_server_not_exist,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.delete_file_from_server_fail:
                        Toast.makeText(StorageActivity.this, R.string.delete_file_from_server_fail,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.server_lost:
                        Toast.makeText(StorageActivity.this, R.string.server_lost,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.server_attempt_fail:
                        Toast.makeText(StorageActivity.this, R.string.server_attempt_fail,
                                Toast.LENGTH_LONG).show();
                        break;
                    case R.string.list_of_server_files:
                        String[] tmp = ((String) msg.obj).split("\\s");
                        String[] listOfFilesFromServer = new String[tmp.length - 1];
                        System.arraycopy(tmp, 1, listOfFilesFromServer, 0, listOfFilesFromServer.length);
                        fileStorageService.synchronizeFiles(listOfFilesFromServer, fileList(),
                                serverConnectionService.getExecutor(), serverConnectionService.getOutData(),
                                StorageActivity.this);
                        break;
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.exit_app);

        adb.setPositiveButton(R.string.yes, (dialog, which) -> {
            if (boundServer) unbindService(serverServiceConnection);
            if (boundFiles) unbindService(filesServiceConnection);
            Intent intent = new Intent(this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("finish", true);
            startActivity(intent);
        });
        adb.setNegativeButton(R.string.no, (dialog, which) -> {
        });
        adb.show();
    }
}
