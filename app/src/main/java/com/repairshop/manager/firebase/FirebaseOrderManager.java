package com.repairshop.manager.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.repairshop.manager.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с заказами в базе данных Firebase
 * Реализованы функции: создание, обновление, просмотр и поиск заказов
 */
public class FirebaseOrderManager {
    
    // Ссылка на базу данных Firebase
    private FirebaseFirestore firestore;
    // Ссылка на коллекцию "orders" (заказы) в базе данных
    private CollectionReference ordersCollection;
    
    // Конструктор класса - инициализировано подключение к базе данных
    public FirebaseOrderManager() {
        this.firestore = FirebaseHelper.getInstance().getFirestore();
        this.ordersCollection = firestore.collection("orders");
    }
    
    /**
     * Создание нового заказа в базе данных
     */
    public Task<String> createOrder(Order order) {
        // Добавлен новый заказ в коллекцию
        return ordersCollection.add(order)
                .continueWith(task -> {
                    // Проверен успех операции
                    if (task.isSuccessful()) {
                        // Получен ID созданного заказа
                        return task.getResult().getId();
                    }
                    // Выброшена ошибка при неудаче
                    throw task.getException();
                });
    }
    
    /**
     * Обновление информации о заказе
     */
    public Task<Void> updateOrder(Order order) {
        // Обновлены все поля заказа одним запросом к базе данных
        return ordersCollection.document(order.getOrderId()).update(
                "objectName", order.getObjectName(),
                "clientName", order.getClientName(),
                "clientPhone", order.getClientPhone(),
                "problemDescription", order.getProblemDescription(),
                "status", order.getStatus(),
                "masterName", order.getMasterName(),
                "masterId", order.getMasterId(),
                "selectedServices", order.getSelectedServices(),
                "selectedParts", order.getSelectedParts(),
                "totalPrice", order.getTotalPrice(),
                "updatedAt", FieldValue.serverTimestamp()
        );
    }
    
    /**
     * Получение информации о заказе по его ID
     */
    public Task<Order> getOrderById(String orderId) {
        // Выполнен запрос к базе данных
        return ordersCollection.document(orderId).get()
                .continueWith(task -> {
                    // Проверен успех операции
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        // Проверено существование документа
                        if (document.exists()) {
                            // Преобразован документ в объект Order
                            return document.toObject(Order.class);
                        }
                    }
                    return null;
                });
    }
    
    /**
     * Получение списка всех активных заказов
     * Активные заказы - все кроме "Выдан" и "Отменен"
     */
    public Task<List<Order>> getAllActiveOrders() {
        // Выполнен запрос с сортировкой по дате создания
        return ordersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    // Создан список для хранения активных заказов
                    List<Order> orders = new ArrayList<>();
                    
                    // Проверен успех операции
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        
                        // Перебраны все документы из результата
                        int totalCount = querySnapshot.getDocuments().size();
                        for (int i = 0; i < totalCount; i++) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(i);
                            
                            // Преобразован документ в объект Order
                            Order order = document.toObject(Order.class);
                            
                            if (order != null) {
                                // Получен статус заказа
                                String status = order.getStatus();
                                
                                // Проверено что заказ не завершен и не отменен
                                boolean isNotCompleted = !status.equals("Выдан");
                                boolean isNotCancelled = !status.equals("Отменен");
                                
                                // Добавлен заказ в список если он активный
                                if (isNotCompleted && isNotCancelled) {
                                    orders.add(order);
                                }
                            }
                        }
                    }
                    return orders;
                });
    }
    
    /**
     * Поиск заказов по телефону клиента, фамилии или модели объекта
     * Поиск выполняется без учета регистра
     */
    public List<Order> searchOrders(List<Order> orders, String query) {
        // Создан список для результатов поиска
        List<Order> results = new ArrayList<>();
        
        // Преобразован поисковый запрос в нижний регистр
        String searchQuery = query.toLowerCase();
        
        // Перебраны все заказы из списка
        int totalOrders = orders.size();
        for (int i = 0; i < totalOrders; i++) {
            Order order = orders.get(i);
            
            // Получены поля для поиска в нижнем регистре
            String phoneText = order.getClientPhone().toLowerCase();
            String nameText = order.getClientName().toLowerCase();
            String objectText = order.getObjectName().toLowerCase();
            
            // Проверено совпадение с телефоном
            boolean foundInPhone = phoneText.contains(searchQuery);
            
            // Проверено совпадение с именем клиента
            boolean foundInName = nameText.contains(searchQuery);
            
            // Проверено совпадение с названием объекта
            boolean foundInObject = objectText.contains(searchQuery);
            
            // Добавлен заказ в результаты если найдено совпадение
            if (foundInPhone || foundInName || foundInObject) {
                results.add(order);
            }
        }
        
        return results;
    }
    
    /**
     * Изменение статуса заказа
     */
    public Task<Void> updateOrderStatus(String orderId, String newStatus) {
        // Обновлен статус и время изменения одним запросом
        return ordersCollection.document(orderId).update(
                "status", newStatus,
                "updatedAt", FieldValue.serverTimestamp()
        );
    }
    
    /**
     * Изменение назначенного мастера для заказа
     */
    public Task<Void> updateOrderMaster(String orderId, String masterId, String masterName) {
        // Обновлены данные мастера и время изменения одним запросом
        return ordersCollection.document(orderId).update(
                "masterId", masterId,
                "masterName", masterName,
                "updatedAt", FieldValue.serverTimestamp()
        );
    }

    /**
     * Получение архива заказов (завершенные и отмененные)
     * Архивный заказ - со статусом "Выдан" или "Отменен"
     */
    public Task<List<Order>> getArchiveOrders() {
        // Выполнен запрос с сортировкой по дате создания
        return ordersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    // Создан список для хранения архивных заказов
                    List<Order> orders = new ArrayList<>();
                    
                    // Проверен успех операции
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        
                        // Перебраны все документы из результата
                        int totalCount = querySnapshot.getDocuments().size();
                        for (int i = 0; i < totalCount; i++) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(i);
                            
                            // Преобразован документ в объект Order
                            Order order = document.toObject(Order.class);
                            
                            if (order != null) {
                                // Получен статус заказа
                                String status = order.getStatus();
                                
                                // Проверено что заказ завершен или отменен
                                boolean isCompleted = status.equals("Выдан");
                                boolean isCancelled = status.equals("Отменен");
                                
                                // Добавлен заказ в список если он архивный
                                if (isCompleted || isCancelled) {
                                    orders.add(order);
                                }
                            }
                        }
                    }
                    return orders;
                });
    }
}
