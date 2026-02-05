package com.repairshop.manager.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.repairshop.manager.R;
import com.repairshop.manager.activities.MainActivity;
import com.repairshop.manager.firebase.AuthManager;
import com.repairshop.manager.models.User;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText codeInput;
    
    private Button registerButton;
    private TextView loginLink;
    
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.etFullName);
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        codeInput = findViewById(R.id.etAccessCode);
        
        registerButton = findViewById(R.id.btnRegister);
        loginLink = findViewById(R.id.tvAlreadyHaveAccount);
        
        authManager = new AuthManager();
        
        setupButtons();
    }

    private void setupButtons() {
        registerButton.setOnClickListener(v -> doRegister());
        loginLink.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String code = codeInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);

        authManager.register(name, email, password, code, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                goToMain();
            }

            @Override
            public void onFailure(String error) {
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
