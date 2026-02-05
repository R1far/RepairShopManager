package com.repairshop.manager.activities.stock;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.repairshop.manager.R;
import com.repairshop.manager.firebase.FirebaseStockManager;
import com.repairshop.manager.models.StockItem;

/**
 * Экран создания нового товара
 */
public class CreateStockActivity extends AppCompatActivity {

    private TextInputEditText etArticleNumber;
    private TextInputEditText etItemName;
    private TextInputEditText etQuantity;
    private TextInputEditText etPrice;
    private MaterialButton btnSaveStock;

    private FirebaseStockManager stockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stock);

        stockManager = new FirebaseStockManager();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etArticleNumber = findViewById(R.id.etArticleNumber);
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        btnSaveStock = findViewById(R.id.btnSaveStock);
    }

    private void setupListeners() {
        btnSaveStock.setOnClickListener(v -> saveStockItem());
    }

    private void saveStockItem() {
        String article = etArticleNumber.getText().toString().trim();
        String name = etItemName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (validateInput(article, name, quantityStr, priceStr)) {
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);

            StockItem newItem = new StockItem(article, name, quantity, price);
            
            btnSaveStock.setEnabled(false);
            btnSaveStock.setText(R.string.please_wait);

            stockManager.createStockItem(newItem)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Товар успешно создан", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Ошибка при создании товара", Toast.LENGTH_SHORT).show();
                            btnSaveStock.setEnabled(true);
                            btnSaveStock.setText(R.string.btn_save_stock);
                        }
                    });
        }
    }

    private boolean validateInput(String article, String name, String quantityStr, String priceStr) {
        if (article.isEmpty()) {
            etArticleNumber.setError(getString(R.string.error_empty_field));
            return false;
        }
        if (name.isEmpty()) {
            etItemName.setError(getString(R.string.error_empty_field));
            return false;
        }
        if (quantityStr.isEmpty()) {
            etQuantity.setError(getString(R.string.error_empty_field));
            return false;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError(getString(R.string.error_empty_field));
            return false;
        }
        return true;
    }
}
