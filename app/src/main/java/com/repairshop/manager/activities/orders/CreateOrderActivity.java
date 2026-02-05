package com.repairshop.manager.activities.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.repairshop.manager.R;
import com.repairshop.manager.firebase.FirebaseHelper;
import com.repairshop.manager.firebase.FirebaseOrderManager;
import com.repairshop.manager.models.Order;
import com.repairshop.manager.models.ServiceItem;
import com.repairshop.manager.models.StockItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.repairshop.manager.firebase.FirebaseStockManager;

/**
 * Экран создания нового заказа
 */
public class CreateOrderActivity extends AppCompatActivity {

    private TextInputEditText etObjectName;
    private TextInputEditText etClientName;
    private TextInputEditText etClientPhone;
    private TextInputEditText etProblemDescription;
    private TextView tvMasterName;
    
    // Новые UI элементы
    private LinearLayout llServicesList;
    private LinearLayout llPartsList;
    private TextView tvTotalPrice;
    private MaterialButton btnAddService;
    private MaterialButton btnAddPart;
    private MaterialButton btnSaveOrder;

    private FirebaseOrderManager orderManager;
    private FirebaseStockManager stockManager;
    private String currentMasterName;
    private String currentMasterId;
    
    // Кэш доступных элементов
    private List<ServiceItem> availableServices;
    private List<StockItem> availableParts;
    
    // Выбранные элементы
    private List<ServiceItem> selectedServices;
    private List<StockItem> selectedParts;
    private double totalPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);



        orderManager = new FirebaseOrderManager();
        stockManager = new FirebaseStockManager();
        selectedServices = new ArrayList<>();
        selectedParts = new ArrayList<>();

        setupViews();
        loadCurrentMaster();
        loadAvailableServices();
        loadAvailableParts();
    }

    private void setupViews() {
        etObjectName = findViewById(R.id.etObjectName);
        etClientName = findViewById(R.id.etClientName);
        etClientPhone = findViewById(R.id.etClientPhone);
        etProblemDescription = findViewById(R.id.etProblemDescription);
        tvMasterName = findViewById(R.id.tvMasterName);
        
        llServicesList = findViewById(R.id.llServicesList);
        llPartsList = findViewById(R.id.llPartsList);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnAddService = findViewById(R.id.btnAddService);
        btnAddPart = findViewById(R.id.btnAddPart);
        btnSaveOrder = findViewById(R.id.btnSaveOrder);

        btnAddService.setOnClickListener(v -> showAddServiceDialog());
        btnAddPart.setOnClickListener(v -> showAddPartDialog());
        btnSaveOrder.setOnClickListener(v -> saveOrder());
    }

    /**
     * Автозаполнение поля "Мастер" текущим пользователем
     */
    private void loadCurrentMaster() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentMasterId = currentUser.getUid();
            
            FirebaseHelper.getInstance().getFirestore()
                    .collection("users")
                    .document(currentMasterId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                currentMasterName = document.getString("fullName");
                                tvMasterName.setText(currentMasterName);
                            }
                        }
                    });
        }
    }

    // Новые методы для выбора

    private void loadAvailableServices() {
        availableServices = new ArrayList<>();
        FirebaseHelper.getInstance().getFirestore()
                .collection("services")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            ServiceItem item = doc.toObject(ServiceItem.class);
                            if (item != null) availableServices.add(item);
                        }
                    }
                });
    }

    private void loadAvailableParts() {
        availableParts = new ArrayList<>();
        FirebaseHelper.getInstance().getFirestore()
                .collection("stock")
                .whereGreaterThan("quantity", 0)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            StockItem item = doc.toObject(StockItem.class);
                            if (item != null) {
                                item.setItemId(doc.getId());
                                availableParts.add(item);
                            }
                        }
                    }
                });
    }

    private void showAddServiceDialog() {
        if (availableServices == null || availableServices.isEmpty()) {
            loadAvailableServices();
            Toast.makeText(this, "Нет доступных услуг или список загружается", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[availableServices.size()];
        for (int i = 0; i < availableServices.size(); i++) {
            ServiceItem item = availableServices.get(i);
            names[i] = item.getServiceName() + " (" + item.getPrice() + " ₽)";
        }

        new AlertDialog.Builder(this)
                .setTitle("Выберите услугу")
                .setItems(names, (dialog, which) -> {
                    addServiceToOrder(availableServices.get(which));
                })
                .show();
    }

    private void showAddPartDialog() {
        if (availableParts == null || availableParts.isEmpty()) {
            loadAvailableParts();
            Toast.makeText(this, "Нет доступных запчастей или список загружается", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[availableParts.size()];
        for (int i = 0; i < availableParts.size(); i++) {
            StockItem item = availableParts.get(i);
            items[i] = item.getItemName() + " (" + item.getPrice() + " ₽)";
        }

        new AlertDialog.Builder(this)
                .setTitle("Выберите запчасть")
                .setItems(items, (dialog, which) -> {
                    addPartToOrder(availableParts.get(which));
                })
                .show();
    }

    private void addServiceToOrder(ServiceItem service) {
        selectedServices.add(service);
        renderServices();
        updateTotal();
    }

    private void addPartToOrder(StockItem part) {
        selectedParts.add(part);
        renderParts();
        updateTotal();
    }

    private void renderServices() {
        llServicesList.removeAllViews();
        for (ServiceItem service : selectedServices) {
            TextView tv = new TextView(this);
            tv.setText(service.getServiceName() + " - " + service.getPrice() + " ₽");
            tv.setTextSize(16);
            tv.setPadding(0, 8, 0, 8);
            llServicesList.addView(tv);
        }
    }

    private void renderParts() {
        llPartsList.removeAllViews();
        for (StockItem part : selectedParts) {
            TextView tv = new TextView(this);
            tv.setText(part.getItemName() + " - " + part.getPrice() + " ₽");
            tv.setTextSize(16);
            tv.setPadding(0, 8, 0, 8);
            llPartsList.addView(tv);
        }
    }

    private void updateTotal() {
        double total = 0.0;
        for (ServiceItem service : selectedServices) total += service.getPrice();
        for (StockItem part : selectedParts) total += part.getPrice();
        totalPrice = total;
        tvTotalPrice.setText(totalPrice + " ₽");
    }

    /**
     * Сохранение нового заказа
     */
    private void saveOrder() {
        String objectName = etObjectName.getText().toString().trim();
        String clientName = etClientName.getText().toString().trim();
        String clientPhone = etClientPhone.getText().toString().trim();
        String problemDescription = etProblemDescription.getText().toString().trim();

        // Валидация
        if (objectName.isEmpty()) {
            Toast.makeText(this, "Введите объект ремонта", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clientName.isEmpty()) {
            Toast.makeText(this, "Введите ФИО клиента", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clientPhone.isEmpty()) {
            Toast.makeText(this, "Введите номер телефона", Toast.LENGTH_SHORT).show();
            return;
        }

        if (problemDescription.isEmpty()) {
            Toast.makeText(this, "Введите описание проблемы", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создание заказа со статусом "Новый"
        Order newOrder = new Order(
                objectName,
                clientName,
                clientPhone,
                problemDescription,
                currentMasterName,
                currentMasterId,
                "Новый"
        );
        
        // Добавление выбранных элементов
        newOrder.setSelectedServices(selectedServices);
        newOrder.setSelectedParts(selectedParts);
        newOrder.setTotalPrice(totalPrice);

        // Списание запчастей со склада для нового заказа
        saveOrderWithStockCheck(newOrder);
    }

    private void saveOrderWithStockCheck(Order newOrder) {
        if (selectedParts.isEmpty()) {
            saveOrderToFirebase(newOrder);
            return;
        }

        // Агрегирование количества для избежания конфликтов
        Map<String, Integer> counts = new HashMap<>();
        for (StockItem part : selectedParts) {
            String id = part.getItemId();
            counts.put(id, counts.getOrDefault(id, 0) + 1);
        }

        List<Task<Void>> tasks = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            tasks.add(stockManager.decreaseQuantity(entry.getKey(), entry.getValue()));
        }

        Tasks.whenAll(tasks)
                .addOnSuccessListener(aVoid -> saveOrderToFirebase(newOrder))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка склада: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveOrderToFirebase(Order newOrder) {
        // Сохранение в Firebase
        orderManager.createOrder(newOrder).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Заказ создан", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка создания заказа", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
