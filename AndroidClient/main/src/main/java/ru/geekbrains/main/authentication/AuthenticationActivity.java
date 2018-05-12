package ru.geekbrains.main.authentication;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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

public class AuthenticationActivity extends AppCompatActivity {

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
}
