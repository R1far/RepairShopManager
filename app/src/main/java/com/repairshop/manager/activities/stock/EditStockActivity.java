package com.repairshop.manager.activities.stock;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.repairshop.manager.R;
import com.repairshop.manager.firebase.AuthManager;
import com.repairshop.manager.firebase.FirebaseStockManager;
import com.repairshop.manager.models.StockItem;

/**
 * Экран редактирования товара
 */
public class EditStockActivity extends AppCompatActivity {

    private TextInputEditText etArticleNumber;
    private TextInputEditText etItemName;
    private TextInputEditText etQuantity;
    private TextInputEditText etPrice;
    private MaterialButton btnUpdateStock;
    private MaterialButton btnReceiveStock;
    private MaterialButton btnDeleteStock;

    private FirebaseStockManager stockManager;
    private AuthManager authManager;
    
    private String itemId;
    private StockItem currentItem;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stock);

        stockManager = new FirebaseStockManager();
        authManager = new AuthManager();

        itemId = getIntent().getStringExtra("ITEM_ID");
        
        initViews();
        checkUserRole();
        loadStockItem();
        setupListeners();
    }

    private void initViews() {
        etArticleNumber = findViewById(R.id.etArticleNumber);
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        btnUpdateStock = findViewById(R.id.btnUpdateStock);
        btnReceiveStock = findViewById(R.id.btnReceiveStock);
        btnDeleteStock = findViewById(R.id.btnDeleteStock);
    }
    
    private void checkUserRole() {
        authManager.getCurrentUserRole().addOnSuccessListener(role -> {
            isAdmin = "admin".equals(role);
            updateUIForRole();
        });
    }
    
    private void updateUIForRole() {
        if (isAdmin) {
            btnDeleteStock.setVisibility(View.VISIBLE);
            etArticleNumber.setEnabled(true);
            etItemName.setEnabled(true);
            etPrice.setEnabled(true);
            btnUpdateStock.setVisibility(View.VISIBLE);
        } else {
            btnDeleteStock.setVisibility(View.GONE);
            // Обычный пользователь не может менять поля (кроме оформления прихода)
            etArticleNumber.setEnabled(false);
            etItemName.setEnabled(false);
            etPrice.setEnabled(false);
            // Прячем кнопку обновить если не админ, так как нечего обновлять
            btnUpdateStock.setVisibility(View.GONE);
        }
    }

    private void loadStockItem() {
        if (itemId == null) return;

        stockManager.getStockItemById(itemId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentItem = task.getResult();
                currentItem.setItemId(itemId);
                fillData(currentItem);
            } else {
                Toast.makeText(this, "Ошибка загрузки товара", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fillData(StockItem item) {
        etArticleNumber.setText(item.getArticleNumber());
        etItemName.setText(item.getItemName());
        etQuantity.setText(String.valueOf(item.getQuantity()));
        etPrice.setText(String.valueOf(item.getPrice()));
    }

    private void setupListeners() {
        btnUpdateStock.setOnClickListener(v -> updateStockItem());
        btnReceiveStock.setOnClickListener(v -> showReceiveDialog());
        btnDeleteStock.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void updateStockItem() {
        if (!isAdmin) return;
        
        String article = etArticleNumber.getText().toString().trim();
        String name = etItemName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        // Количество не обновляем здесь, только через приход/расход

        if (validateInput(article, name, priceStr)) {
            currentItem.setArticleNumber(article);
            currentItem.setItemName(name);
            currentItem.setPrice(Double.parseDouble(priceStr));
            
            stockManager.updateStockItem(currentItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Товар обновлен", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show()
                    );
        }
    }
    
    // Оформление прихода
    private void showReceiveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Оформление прихода");
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Количество");
        builder.setView(input);

        builder.setPositiveButton("Принять", (dialog, which) -> {
            String quantityStr = input.getText().toString();
            if (!quantityStr.isEmpty()) {
                int quantityToAdd = Integer.parseInt(quantityStr);
                performReceiveStock(quantityToAdd);
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    
    private void performReceiveStock(int quantityToAdd) {
        stockManager.increaseQuantity(itemId, quantityToAdd)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Приход оформлен", Toast.LENGTH_SHORT).show();
                    // Обновляем данные на экране
                    loadStockItem();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Ошибка оформления прихода", Toast.LENGTH_SHORT).show()
                );
    }
    
    // Удаление товара
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление товара")
                .setMessage("Вы уверены, что хотите удалить этот товар?")
                .setPositiveButton("Да", (dialog, which) -> deleteItem())
                .setNegativeButton("Нет", null)
                .show();
    }
    
    private void deleteItem() {
        stockManager.deleteStockItem(itemId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Товар удален", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                );
    }

    private boolean validateInput(String article, String name, String priceStr) {
        if (article.isEmpty()) {
            etArticleNumber.setError(getString(R.string.error_empty_field));
            return false;
        }
        if (name.isEmpty()) {
            etItemName.setError(getString(R.string.error_empty_field));
            return false;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError(getString(R.string.error_empty_field));
            return false;
        }
        return true;
    }
}
