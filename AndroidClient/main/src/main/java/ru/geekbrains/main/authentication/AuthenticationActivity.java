package ru.geekbrains.main.authentication;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.geekbrains.main.R;

public class AuthenticationActivity extends AppCompatActivity {

    // флаг для управления окнами регистрации/авторизации
    private boolean registration = false;

    // элементы пользовательского интерфейса
    private ConstraintLayout mainLayout;
    private LinearLayout authenticationLayout;
    private TextView tvTitle;
    private TextView tvNickname;
    private EditText etNickname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // находим все элементы интерфейса в xml
        setContentView(R.layout.activity_authentication);
        mainLayout = findViewById(R.id.main_layout);
        authenticationLayout = findViewById(R.id.authentication_layout);
        tvTitle = findViewById(R.id.tv_title);
        tvNickname = findViewById(R.id.tv_nickname);
        etNickname = findViewById(R.id.et_nickname);
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
}
