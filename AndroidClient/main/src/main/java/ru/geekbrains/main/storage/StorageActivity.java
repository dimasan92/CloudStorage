package ru.geekbrains.main.storage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.geekbrains.main.R;

public class StorageActivity extends AppCompatActivity {

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

//        // привязываемся к сервисам
//        boundServer = false;
//        boundFiles = false;
//        serverServiceConnection = serverServiceConnection();
//        filesServiceConnection = storageServiceConnection();
//        bindService(new Intent(this, ServerConnectionService.class), serverServiceConnection, 0);
//        bindSer
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
}
