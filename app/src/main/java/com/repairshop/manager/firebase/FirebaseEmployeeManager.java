package com.repairshop.manager.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.repairshop.manager.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с сотрудниками в базе данных Firebase
 * Реализованы функции: просмотр списка сотрудников, удаление
 */
public class FirebaseEmployeeManager {

    // Ссылка на базу данных Firebase
    private FirebaseFirestore db;

    // Конструктор класса - инициализировано подключение к базе данных
    public FirebaseEmployeeManager() {
        this.db = FirebaseHelper.getInstance().getFirestore();
    }

    /**
     * Получение списка всех сотрудников
     * Сотрудники отсортированы по имени
     */
    public Task<List<User>> getAllEmployees() {
        // Выполнен запрос с сортировкой по полному имени
        return db.collection("users")
                .orderBy("fullName", Query.Direction.ASCENDING)
                .get()
                .continueWith(task -> {
                    // Создан список для хранения сотрудников
                    List<User> users = new ArrayList<>();
                    
                    // Проверен успех операции
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Преобразованы все документы в список объектов User
                        users = task.getResult().toObjects(User.class);
                    }
                    
                    return users;
                });
    }

    /**
     * Удаление сотрудника из базы данных по его ID
     */
    public Task<Void> deleteEmployee(String userId) {
        // Удален документ сотрудника из базы данных
        return db.collection("users")
                .document(userId)
                .delete();
    }
}
