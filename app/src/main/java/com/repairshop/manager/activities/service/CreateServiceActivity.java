package com.repairshop.manager.activities.service;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.repairshop.manager.R;
import com.repairshop.manager.firebase.FirebaseServiceManager;
import com.repairshop.manager.models.ServiceItem;

/**
 * Экран создания новой услуги
 */
public class CreateServiceActivity extends AppCompatActivity {

    private EditText etServiceName;
    private EditText etServicePrice;
    private MaterialButton btnSaveService;

    private FirebaseServiceManager serviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_service);

        serviceManager = new FirebaseServiceManager();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServicePrice = findViewById(R.id.etServicePrice);
        btnSaveService = findViewById(R.id.btnSaveService);
    }

    private void setupListeners() {
        btnSaveService.setOnClickListener(v -> saveService());
    }

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String priceStr = etServicePrice.getText().toString().trim();

        if (validateInput(name, priceStr)) {
            double price = Double.parseDouble(priceStr);

            ServiceItem newItem = new ServiceItem(name, price);
            
            btnSaveService.setEnabled(false);
            btnSaveService.setText(R.string.please_wait);

            serviceManager.createService(newItem)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Услуга успешно создана", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Ошибка при создании услуги", Toast.LENGTH_SHORT).show();
                            btnSaveService.setEnabled(true);
                            btnSaveService.setText(R.string.btn_create_service);
                        }
                    });
        }
    }

    private boolean validateInput(String name, String priceStr) {
        if (name.isEmpty()) {
            etServiceName.setError(getString(R.string.error_empty_field));
            return false;
        }
        if (priceStr.isEmpty()) {
            etServicePrice.setError(getString(R.string.error_empty_field));
            return false;
        }
        return true;
    }
}
