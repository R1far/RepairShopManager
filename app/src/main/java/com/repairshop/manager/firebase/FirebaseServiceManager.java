package com.repairshop.manager.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.repairshop.manager.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с услугами в базе данных Firebase
 * Реализованы функции: создание, обновление, просмотр и поиск услуг
 */
public class FirebaseServiceManager {

    // Ссылка на базу данных Firebase
    private FirebaseFirestore firestore;
    // Ссылка на коллекцию "services" (услуги) в базе данных
    private CollectionReference serviceCollection;

    // Конструктор класса - инициализировано подключение к базе данных
    public FirebaseServiceManager() {
        this.firestore = FirebaseHelper.getInstance().getFirestore();
        this.serviceCollection = firestore.collection("services");
    }

    /**
     * Создание новой услуги в базе данных
     */
    public Task<String> createService(ServiceItem item) {
        // Добавлена новая услуга в коллекцию
        return serviceCollection.add(item)
                .continueWith(task -> {
                    // Проверен успех операции
                    if (task.isSuccessful()) {
                        // Получен ID созданной услуги
                        return task.getResult().getId();
                    }
                    // Выброшена ошибка при неудаче
                    throw task.getException();
                });
    }

    /**
     * Обновление информации об услуге (изменение названия или цены)
     */
    public Task<Void> updateService(ServiceItem item) {
        // Обновлены поля услуги и время изменения одним запросом
        return serviceCollection.document(item.getServiceId()).update(
                "serviceName", item.getServiceName(),
                "price", item.getPrice(),
                "updatedAt", FieldValue.serverTimestamp()
        );
    }

    /**
     * Получение информации об услуге по её ID
     */
    public Task<ServiceItem> getServiceById(String serviceId) {
        // Выполнен запрос к базе данных
        return serviceCollection.document(serviceId).get()
                .continueWith(task -> {
                    // Проверен успех операции
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        // Проверено существование документа
                        if (document.exists()) {
                            // Преобразован документ в объект ServiceItem
                            return document.toObject(ServiceItem.class);
                        }
                    }
                    return null;
                });
    }

    /**
     * Получение списка всех услуг (прайс-лист)
     * Услуги отсортированы по названию
     */
    public Task<List<ServiceItem>> getAllServices() {
        // Выполнен запрос с сортировкой по названию
        return serviceCollection
                .orderBy("serviceName", Query.Direction.ASCENDING)
                .get()
                .continueWith(task -> {
                    // Создан список для хранения услуг
                    List<ServiceItem> items = new ArrayList<>();
                    
                    // Проверен успех операции
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        
                        // Перебраны все документы из результата
                        int totalCount = querySnapshot.getDocuments().size();
                        for (int i = 0; i < totalCount; i++) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(i);
                            
                            // Преобразован документ в объект ServiceItem
                            ServiceItem item = document.toObject(ServiceItem.class);
                            
                            // Добавлена услуга в список
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }
                    return items;
                });
    }

    /**
     * Поиск услуги по названию
     * Поиск выполняется без учета регистра
     */
    public List<ServiceItem> searchServices(List<ServiceItem> items, String query) {
        // Создан список для результатов поиска
        List<ServiceItem> results = new ArrayList<>();
        
        // Преобразован поисковый запрос в нижний регистр
        String searchQuery = query.toLowerCase();

        // Перебраны все услуги из списка
        int totalItems = items.size();
        for (int i = 0; i < totalItems; i++) {
            ServiceItem item = items.get(i);
            
            // Получено название услуги в нижнем регистре
            String serviceName = item.getServiceName().toLowerCase();
            
            // Проверено совпадение с названием
            boolean foundInName = serviceName.contains(searchQuery);

            // Добавлена услуга в результаты если найдено совпадение
            if (foundInName) {
                results.add(item);
            }
        }

        return results;
    }

    /**
     * Удаление услуги из базы данных
     */
    public Task<Void> deleteService(String serviceId) {
        // Удален документ услуги из базы данных
        return serviceCollection.document(serviceId).delete();
    }
}
