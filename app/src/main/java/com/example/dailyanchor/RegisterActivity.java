package com.example.dailyanchor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private final Calendar selectedDate = Calendar.getInstance();
    private boolean chosenBirthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        EditText name = findViewById(R.id.register_name);
        TextView birthday = findViewById(R.id.register_birthday);
        EditText email = findViewById(R.id.register_email);
        EditText password = findViewById(R.id.register_password);
        birthday.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate.set(year, month, day);
                birthday.setText(new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(selectedDate.getTime()));
                chosenBirthday = true;
            }, 2004, 0, 1);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });
        findViewById(R.id.btn_create_account).setOnClickListener(v -> {
            String fullName = name.getText().toString().trim();
            String address = email.getText().toString().trim();
            String secret = password.getText().toString();
            if (fullName.isEmpty()) {
                name.setError("What's your name?");
                return;
            }
            if (!chosenBirthday) {
                birthday.setError("Choose your birthday");
                return;
            }
            if (address.isEmpty()) {
                email.setError("Pop in your email");
                return;
            }
            if (secret.isEmpty()) {
                password.setError("Create a password");
                return;
            }
            DemoSession.name = fullName;
            DemoSession.email = address;
            DemoSession.birthday = birthday.getText().toString();
            DemoSession.password = secret;
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.btn_back_login).setOnClickListener(v -> finish());
    }
}
