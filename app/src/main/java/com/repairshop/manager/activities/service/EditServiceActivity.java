package com.repairshop.manager.activities.service;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.repairshop.manager.R;
import com.repairshop.manager.firebase.AuthManager;
import com.repairshop.manager.firebase.FirebaseServiceManager;
import com.repairshop.manager.models.ServiceItem;

/**
 * Экран редактирования услуги
 */
public class EditServiceActivity extends AppCompatActivity {

    private EditText etServiceName;
    private EditText etServicePrice;
    private MaterialButton btnUpdateService;
    private MaterialButton btnDeleteService;

    private FirebaseServiceManager serviceManager;
    private AuthManager authManager;
    
    private String serviceId;
    private ServiceItem currentItem;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        serviceManager = new FirebaseServiceManager();
        authManager = new AuthManager();

        serviceId = getIntent().getStringExtra("SERVICE_ID");
        
        initViews();
        checkUserRole();
        loadServiceItem();
        setupListeners();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etServicePrice = findViewById(R.id.etServicePrice);
        btnUpdateService = findViewById(R.id.btnUpdateService);
        btnDeleteService = findViewById(R.id.btnDeleteService);
    }
    
    private void checkUserRole() {
        authManager.getCurrentUserRole().addOnSuccessListener(role -> {
            isAdmin = "admin".equals(role);
            updateUIForRole();
        });
    }
    
    private void updateUIForRole() {
        if (isAdmin) {
            btnDeleteService.setVisibility(View.VISIBLE);
            btnUpdateService.setVisibility(View.VISIBLE);
            etServiceName.setEnabled(true);
            etServicePrice.setEnabled(true);
        } else {
            btnDeleteService.setVisibility(View.GONE);
            btnUpdateService.setVisibility(View.GONE);
            etServiceName.setEnabled(false);
            etServicePrice.setEnabled(false);
        }
    }

    private void loadServiceItem() {
        if (serviceId == null) return;

        serviceManager.getServiceById(serviceId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentItem = task.getResult();
                currentItem.setServiceId(serviceId);
                fillData(currentItem);
            } else {
                Toast.makeText(this, "Ошибка загрузки услуги", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fillData(ServiceItem item) {
        etServiceName.setText(item.getServiceName());
        etServicePrice.setText(String.valueOf(item.getPrice()));
    }

    private void setupListeners() {
        btnUpdateService.setOnClickListener(v -> updateService());
        btnDeleteService.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void updateService() {
        if (!isAdmin) return;
        
        String name = etServiceName.getText().toString().trim();
        String priceStr = etServicePrice.getText().toString().trim();

        if (validateInput(name, priceStr)) {
            currentItem.setServiceName(name);
            currentItem.setPrice(Double.parseDouble(priceStr));
            
            serviceManager.updateService(currentItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Услуга обновлена", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show()
                    );
        }
    }
    
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление услуги")
                .setMessage("Вы уверены, что хотите удалить эту услугу?")
                .setPositiveButton("Да", (dialog, which) -> deleteItem())
                .setNegativeButton("Нет", null)
                .show();
    }
    
    private void deleteItem() {
        serviceManager.deleteService(serviceId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Услуга удалена", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                );
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
