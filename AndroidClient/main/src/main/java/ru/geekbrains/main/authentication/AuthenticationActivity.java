package ru.geekbrains.main.authentication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ru.geekbrains.main.R;

public class AuthenticationActivity extends AppCompatActivity {

    // флаг для управления окнами регистрации/авторизации
    private boolean registration = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
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

}
