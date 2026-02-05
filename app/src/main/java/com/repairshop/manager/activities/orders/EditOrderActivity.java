package com.repairshop.manager.activities.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.repairshop.manager.R;
import com.repairshop.manager.firebase.FirebaseHelper;
import com.repairshop.manager.firebase.FirebaseOrderManager;
import com.repairshop.manager.models.Order;
import com.repairshop.manager.models.ServiceItem;
import com.repairshop.manager.models.StockItem;
import com.repairshop.manager.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.repairshop.manager.firebase.FirebaseStockManager;

/**
 * Экран редактирования заказа
 */
public class EditOrderActivity extends AppCompatActivity {

    private TextInputEditText etObjectName;
    private TextInputEditText etClientName;
    private TextInputEditText etClientPhone;
    private TextInputEditText etProblemDescription;
    private Spinner spinnerStatus;
    private Spinner spinnerMaster;
    
    // Новые UI элементы
    private LinearLayout llServicesList;
    private LinearLayout llPartsList;
    private TextView tvTotalPrice;
    private MaterialButton btnAddService;
    private MaterialButton btnAddPart;
    private MaterialButton btnUpdateOrder;

    private FirebaseOrderManager orderManager;
    private Order currentOrder;
    private String orderId;

    private List<String> statusList;
    private List<User> mastersList;
    private List<String> mastersNamesList;
    
    // Кэш доступных элементов
    private List<ServiceItem> availableServices;
    private List<StockItem> availableParts;
    
    // Отслеживание изменений
    private FirebaseStockManager stockManager;
    private List<StockItem> originalParts;
    private String originalStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);



        orderManager = new FirebaseOrderManager();
        stockManager = new FirebaseStockManager();

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            Toast.makeText(this, "Ошибка: ID заказа не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupStatusSpinner();
        loadMasters();
        loadAvailableServices();
        loadAvailableParts();
        loadOrderData();
    }

    private void setupViews() {
        etObjectName = findViewById(R.id.etObjectName);
        etClientName = findViewById(R.id.etClientName);
        etClientPhone = findViewById(R.id.etClientPhone);
        etProblemDescription = findViewById(R.id.etProblemDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerMaster = findViewById(R.id.spinnerMaster);
        
        llServicesList = findViewById(R.id.llServicesList);
        llPartsList = findViewById(R.id.llPartsList);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnAddService = findViewById(R.id.btnAddService);
        btnAddPart = findViewById(R.id.btnAddPart);
        btnUpdateOrder = findViewById(R.id.btnUpdateOrder);

        btnAddService.setOnClickListener(v -> showAddServiceDialog());
        btnAddPart.setOnClickListener(v -> showAddPartDialog());
        btnUpdateOrder.setOnClickListener(v -> updateOrder());
    }

    private void setupStatusSpinner() {
        statusList = new ArrayList<>();
        statusList.add("Новый");
        statusList.add("В работе");
        statusList.add("Готов");
        statusList.add("Выдан");
        statusList.add("Отменен");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusList
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    /**
     * Загрузка списка мастеров
     */
    private void loadMasters() {
        mastersList = new ArrayList<>();
        mastersNamesList = new ArrayList<>();

        FirebaseHelper.getInstance().getFirestore()
                .collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                mastersList.add(user);
                                mastersNamesList.add(user.getFullName());
                            }
                        }

                        ArrayAdapter<String> masterAdapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                mastersNamesList
                        );
                        masterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMaster.setAdapter(masterAdapter);

                        // Установка текущего мастера после загрузки
                        if (currentOrder != null) {
                            setCurrentMasterInSpinner();
                        }
                    }
                });
    }

    /**
     * Загрузка данных заказа
     */
    private void loadOrderData() {
        orderManager.getOrderById(orderId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentOrder = task.getResult();
                if (currentOrder != null) {
                    fillOrderData();
                } else {
                    Toast.makeText(this, "Заказ не найден", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Ошибка загрузки заказа", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fillOrderData() {
        etObjectName.setText(currentOrder.getObjectName());
        etClientName.setText(currentOrder.getClientName());
        etClientPhone.setText(currentOrder.getClientPhone());
        etProblemDescription.setText(currentOrder.getProblemDescription());

        // Установка текущего статуса
        String currentStatus = currentOrder.getStatus();
        int statusPosition = statusList.indexOf(currentStatus);
        if (statusPosition >= 0) {
            spinnerStatus.setSelection(statusPosition);
        }

        // Установка текущего мастера (если список уже загружен)
        if (!mastersList.isEmpty()) {
            setCurrentMasterInSpinner();
        }

        // Инициализация списков, если null (для совместимости миграции)
        if (currentOrder.getSelectedServices() == null) {
            currentOrder.setSelectedServices(new ArrayList<>());
        }
        if (currentOrder.getSelectedParts() == null) {
            currentOrder.setSelectedParts(new ArrayList<>());
        }

        renderServices();
        renderParts();
        updateTotal();

        originalParts = new ArrayList<>(currentOrder.getSelectedParts());
        originalStatus = currentOrder.getStatus();
    }

    private void setCurrentMasterInSpinner() {
        String currentMasterName = currentOrder.getMasterName();
        int masterPosition = mastersNamesList.indexOf(currentMasterName);
        if (masterPosition >= 0) {
            spinnerMaster.setSelection(masterPosition);
        }
    }

    private void updateOrder() {
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

        // Обновление данных заказа
        currentOrder.setObjectName(objectName);
        currentOrder.setClientName(clientName);
        currentOrder.setClientPhone(clientPhone);
        currentOrder.setProblemDescription(problemDescription);

        // Обновление статуса
        String selectedStatus = spinnerStatus.getSelectedItem().toString();
        currentOrder.setStatus(selectedStatus);

        // Обновление мастера
        int selectedMasterPosition = spinnerMaster.getSelectedItemPosition();
        if (selectedMasterPosition >= 0 && selectedMasterPosition < mastersList.size()) {
            User selectedMaster = mastersList.get(selectedMasterPosition);
            currentOrder.setMasterId(selectedMaster.getUserId());
            currentOrder.setMasterName(selectedMaster.getFullName());
        }

        updateOrderWithStockCheck();
    }

    private void updateOrderWithStockCheck() {
        Map<String, Integer> stockChanges = calculateStockChanges();

        if (stockChanges.isEmpty()) {
            saveOrderToFirebase();
            return;
        }

        List<Map.Entry<String, Integer>> changesList = new ArrayList<>(stockChanges.entrySet());
        processNextStockChange(changesList, 0);
    }

    private void processNextStockChange(List<Map.Entry<String, Integer>> changes, int index) {
        if (index >= changes.size()) {
            saveOrderToFirebase();
            return;
        }

        Map.Entry<String, Integer> entry = changes.get(index);
        String itemId = entry.getKey();
        int delta = entry.getValue();

        Task<Void> task;
        if (delta < 0) {
            task = stockManager.decreaseQuantity(itemId, -delta);
        } else {
            task = stockManager.increaseQuantity(itemId, delta);
        }

        task.addOnSuccessListener(aVoid -> {
            processNextStockChange(changes, index + 1);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Ошибка склада (товар " + (index + 1) + "): " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private Map<String, Integer> calculateStockChanges() {
        Map<String, Integer> changes = new HashMap<>();
        
        List<StockItem> effectiveOldParts = "Отменен".equals(originalStatus) ? new ArrayList<>() : originalParts;
        List<StockItem> effectiveNewParts = "Отменен".equals(currentOrder.getStatus()) ? new ArrayList<>() : currentOrder.getSelectedParts();

        if (effectiveOldParts != null) {
            for (StockItem item : effectiveOldParts) {
                if (item != null && item.getItemId() != null) {
                    String id = item.getItemId();
                    changes.put(id, changes.getOrDefault(id, 0) + 1);
                }
            }
        }

        if (effectiveNewParts != null) {
            for (StockItem item : effectiveNewParts) {
                if (item != null && item.getItemId() != null) {
                    String id = item.getItemId();
                    changes.put(id, changes.getOrDefault(id, 0) - 1);
                }
            }
        }
        
        Map<String, Integer> finalChanges = new HashMap<>();
        for (Map.Entry<String, Integer> entry : changes.entrySet()) {
            if (entry.getValue() != 0) {
                finalChanges.put(entry.getKey(), entry.getValue());
            }
        }
        return finalChanges;
    }

    private void saveOrderToFirebase() {
        // Сохранение в Firebase
        orderManager.updateOrder(currentOrder).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Заказ обновлен", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка обновления заказа", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        currentOrder.getSelectedServices().add(service);
        renderServices();
        updateTotal();
    }

    private void addPartToOrder(StockItem part) {
        currentOrder.getSelectedParts().add(part);
        renderParts();
        updateTotal();
    }

    private void renderServices() {
        llServicesList.removeAllViews();
        if (currentOrder.getSelectedServices() != null) {
            for (ServiceItem service : currentOrder.getSelectedServices()) {
                TextView tv = new TextView(this);
                tv.setText(service.getServiceName() + " - " + service.getPrice() + " ₽");
                tv.setTextSize(16);
                tv.setPadding(0, 8, 0, 8);
                llServicesList.addView(tv);
            }
        }
    }

    private void renderParts() {
        llPartsList.removeAllViews();
        if (currentOrder.getSelectedParts() != null) {
            for (StockItem part : currentOrder.getSelectedParts()) {
                TextView tv = new TextView(this);
                tv.setText(part.getItemName() + " - " + part.getPrice() + " ₽");
                tv.setTextSize(16);
                tv.setPadding(0, 8, 0, 8);
                llPartsList.addView(tv);
            }
        }
    }

    private void updateTotal() {
        currentOrder.calculateTotal();
        tvTotalPrice.setText(currentOrder.getTotalPrice() + " ₽");
    }
}
