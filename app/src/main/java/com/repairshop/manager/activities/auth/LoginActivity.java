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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private TextView createAccountLink;
    
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        createAccountLink = findViewById(R.id.tvCreateAccount);
        
        authManager = new AuthManager();
        
        checkIfLoggedIn();
        setupButtons();
    }

    private void checkIfLoggedIn() {
        if (authManager.getCurrentUser() != null) {
            goToMain();
        }
    }

    private void setupButtons() {
        loginButton.setOnClickListener(v -> doLogin());
        createAccountLink.setOnClickListener(v -> goToRegister());
    }

    private void doLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                loginButton.setEnabled(true);
                goToMain();
            }

            @Override
            public void onFailure(String error) {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
