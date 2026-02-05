package com.repairshop.manager.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Класс для централизованного доступа к сервисам Firebase
 * Реализован паттерн Singleton
 */
public class FirebaseHelper {
    // Единственный экземпляр класса
    private static FirebaseHelper instance;
    // Объект для работы с аутентификацией
    private FirebaseAuth auth;
    // Объект для работы с базой данных
    private FirebaseFirestore firestore;

    // Приватный конструктор - инициализированы сервисы Firebase
    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Получение единственного экземпляра класса
    public static synchronized FirebaseHelper getInstance() {
        // Создан экземпляр если его еще нет
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        // Возвращен существующий экземпляр
        return instance;
    }

    // Получение объекта аутентификации
    public FirebaseAuth getAuth() {
        return auth;
    }

    // Получение объекта базы данных
    public FirebaseFirestore getFirestore() {
        return firestore;
    }
}
