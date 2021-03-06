package ru.geekbrains.main.authentication;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import common.Constants;
import ru.geekbrains.main.R;
import ru.geekbrains.main.storage.StorageActivity;
import ru.geekbrains.services.FileStorageService;
import ru.geekbrains.services.ServerConnectionService;

public class AuthenticationActivity extends AppCompatActivity {

    // сервис соединения с сервером
    private ServerConnectionService serverConnectionService;
    private ServiceConnection serviceConnection;
    private Intent intentConnection;
    private boolean bound;

    // сервис работы с файлами
    private Intent intentFiles;

    // флаг для управления окнами регистрации/авторизации
    private boolean registration = false;

    // элементы пользовательского интерфейса
    private ConstraintLayout mainLayout;
    private LinearLayout authenticationLayout;
    private TextView tvTitle;
    private TextView tvNickname;
    private EditText etLogin;
    private EditText etNickname;
    private EditText etPass;
    private Button btnReg;
    private Button btnAuth;
    private Button btnSend;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getBooleanExtra("finish", false)) finish();

        super.onCreate(savedInstanceState);
        // находим все элементы интерфейса в xml
        setContentView(R.layout.activity_authentication);
        mainLayout = findViewById(R.id.main_layout);
        authenticationLayout = findViewById(R.id.authentication_layout);
        tvTitle = findViewById(R.id.tv_title);
        tvNickname = findViewById(R.id.tv_nickname);
        etLogin = findViewById(R.id.et_login);
        etNickname = findViewById(R.id.et_nickname);
        etPass = findViewById(R.id.et_password);
        btnReg = findViewById(R.id.btn_reg);
        btnAuth = findViewById(R.id.btn_auth);
        btnSend = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar);

        // запускаем сервисы
        intentConnection = new Intent(this, ServerConnectionService.class);
        intentFiles = new Intent(this, FileStorageService.class);
        startService(intentConnection);
        startService(intentFiles);

        // привязываемся к сервису, управляющему соединением с сервером
        bound = false;
        serviceConnection = serverServiceConnection();
        bindService(intentConnection, serviceConnection, 0);
    }

    // обработчк нажатий кнопоки "Регистрация"
    public void onClickReg(View view) {
        registration = true;
        authFormVisible();
    }

    // обработчк нажатий кнопоки "Авторизация"
    public void onClickAuth(View view) {
        registration = false;
        authFormVisible();
    }

    // обработчк нажатий кнопоки "Отправить"
    public void onClickSend(View view) {
        authentication();
    }

    // показывать поля для регистрации или авторизации, в соответствии с выбором пользователя
    private void authFormVisible() {
        ConstraintSet set = new ConstraintSet();
        set.clone(mainLayout);
        set.clear(R.id.buttons_layout, ConstraintSet.BOTTOM);
        TransitionManager.beginDelayedTransition(mainLayout);
        set.applyTo(mainLayout);

        if (registration) {
            tvTitle.setText(R.string.registration);
            tvNickname.setVisibility(View.VISIBLE);
            etNickname.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText(R.string.authorization);
            tvNickname.setVisibility(View.GONE);
            etNickname.setVisibility(View.GONE);
        }
        authenticationLayout.setVisibility(View.VISIBLE);
    }

    // делать доступными/недоступными поля при попытке отправки данных на сервер
    public void connectionViewEnabled(boolean enabled) {
        etLogin.setEnabled(enabled);
        etPass.setEnabled(enabled);
        etNickname.setEnabled(enabled);
        btnReg.setEnabled(enabled);
        btnAuth.setEnabled(enabled);
        btnSend.setEnabled(enabled);
        if (enabled)
            progressBar.setVisibility(View.GONE);
        else progressBar.setVisibility(View.VISIBLE);
    }

    // отправка сообщения авторизации/регистрации
    private void authentication() {
        if (!hasConnection()) {
            Toast.makeText(this, R.string.not_net, Toast.LENGTH_LONG).show();
            return;
        }
        String nickname = null;
        if (registration) nickname = String.valueOf(etNickname.getText());
        String login = String.valueOf(etLogin.getText());
        String password = String.valueOf(etPass.getText());
        if ("".equals(login) || "".equals(password) || (registration && "".equals(nickname))) {
            Toast.makeText(this, R.string.fields_not_filled, Toast.LENGTH_SHORT).show();
            return;
        }
        connectionViewEnabled(false);
        if (!serverConnectionService.isConnected()) serverConnectionService.connect();
        if (registration) {
            serverConnectionService.sendMsg(Constants.REG_REQUEST + " " + nickname + " " + login + " " + password);
        } else {
            serverConnectionService.sendMsg(Constants.AUTH_REQUEST + " " + login + " " + password);
        }
    }

    // проверка подключения к сети
    private boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (netInfo != null && netInfo.isConnected()) {
                return true;
            }
            netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (netInfo != null && netInfo.isConnected()) {
                return true;
            }
            netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
        return false;
    }

    // экземпляр для подключения к сервису соединения с сервером
    private ServiceConnection serverServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serverConnectionService =
                        ((ServerConnectionService.ConnectBinder) service).getService();
                serverConnectionService.setHandler(authHandler());
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
    }

    @SuppressLint("HandlerLeak")
    private Handler authHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case R.string.reg_success:
                        Toast.makeText(AuthenticationActivity.this, R.string.reg_success,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.string.reg_nick_already_exist:
                        Toast.makeText(AuthenticationActivity.this, R.string.reg_nick_already_exist,
                                Toast.LENGTH_LONG).show();
                        connectionViewEnabled(true);
                        break;
                    case R.string.reg_failure:
                        Toast.makeText(AuthenticationActivity.this, R.string.reg_failure,
                                Toast.LENGTH_LONG).show();
                        connectionViewEnabled(true);
                        break;
                    case R.string.auth_success:
                        Toast.makeText(AuthenticationActivity.this, R.string.auth_success,
                                Toast.LENGTH_LONG).show();
                        if (bound) unbindService(serviceConnection);
                        bound = false;
                        startActivity(new Intent(AuthenticationActivity.this, StorageActivity.class));
                        break;
                    case R.string.auth_nick_is_busy:
                        Toast.makeText(AuthenticationActivity.this, R.string.auth_nick_is_busy,
                                Toast.LENGTH_LONG).show();
                        connectionViewEnabled(true);
                        break;
                    case R.string.auth_nick_not_exist:
                        Toast.makeText(AuthenticationActivity.this, R.string.auth_nick_not_exist,
                                Toast.LENGTH_LONG).show();
                        connectionViewEnabled(true);
                        break;
                    case R.string.auth_failure:
                        Toast.makeText(AuthenticationActivity.this, R.string.auth_failure,
                                Toast.LENGTH_LONG).show();
                        connectionViewEnabled(true);
                        break;
                    case R.string.server_lost:
                        Toast.makeText(AuthenticationActivity.this, R.string.server_lost,
                                Toast.LENGTH_LONG).show();
                        connectionViewEnabled(true);
                        break;
                    case R.string.server_attempt_fail:
                        Toast.makeText(AuthenticationActivity.this, R.string.server_attempt_fail,
                                Toast.LENGTH_LONG).show();
                        connectionViewEnabled(true);
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
            finish();
            super.onBackPressed();
        });
        adb.setNegativeButton(R.string.no, (dialog, which) -> {
        });
        adb.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) unbindService(serviceConnection);
        stopService(intentConnection);
        stopService(intentFiles);
    }
}
