package com.repairshop.manager.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Класс для работы с настройками приложения в базе данных Firebase
 * Управление кодом доступа для регистрации новых пользователей
 */
public class FirebaseSettingsManager {

    // Ссылка на базу данных Firebase
    private FirebaseFirestore db;

    // Конструктор класса - инициализировано подключение к базе данных
    public FirebaseSettingsManager() {
        this.db = FirebaseHelper.getInstance().getFirestore();
    }

    /**
     * Получение текущего кода доступа из базы данных
     */
    public void getAccessCode(SettingsCallback<String> callback) {
        // Выполнен запрос к коллекции настроек
        db.collection("settings")
                .document("access_code")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Проверено существование документа
                    if (documentSnapshot.exists()) {
                        // Получен код доступа из документа
                        String code = documentSnapshot.getString("code");
                        // Возвращен результат через callback
                        callback.onSuccess(code);
                    } else {
                        // Возвращена ошибка если код не найден
                        callback.onFailure("Код не найден");
                    }
                })
                .addOnFailureListener(e -> {
                    // Возвращена ошибка при неудачном запросе
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Обновление кода доступа в базе данных
     */
    public void updateAccessCode(String newCode, SettingsCallback<Boolean> callback) {
        // Обновлен код доступа одним запросом
        db.collection("settings")
                .document("access_code")
                .update("code", newCode)
                .addOnSuccessListener(aVoid -> {
                    // Возвращен успешный результат
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    // Возвращена ошибка при неудачном обновлении
                    callback.onFailure(e.getMessage());
                });
    }



    /**
     * Интерфейс для обработки результатов операций с настройками
     */
    public interface SettingsCallback<T> {
        // Вызывается при успешном выполнении операции
        void onSuccess(T result);
        // Вызывается при ошибке
        void onFailure(String error);
    }
}
