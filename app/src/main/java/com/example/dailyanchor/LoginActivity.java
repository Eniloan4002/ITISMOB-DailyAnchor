package com.example.dailyanchor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        EditText email = findViewById(R.id.login_email);
        EditText password = findViewById(R.id.login_password);
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            String typedEmail = email.getText().toString().trim();
            if (typedEmail.isEmpty()) {
                email.setError("Pop in your email first");
                return;
            }
            if (password.getText().toString().isEmpty()) {
                password.setError("Add your password to continue");
                return;
            }
            DemoSession.email = typedEmail;
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.btn_register_link).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
