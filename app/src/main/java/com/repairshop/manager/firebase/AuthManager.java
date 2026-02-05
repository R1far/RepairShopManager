package com.repairshop.manager.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.repairshop.manager.models.User;

/**
 * Класс для управления аутентификацией пользователей
 * Реализованы функции: вход, регистрация, проверка кода доступа
 */
public class AuthManager {
    
    // Ссылка на систему аутентификации Firebase
    private FirebaseAuth auth;
    // Ссылка на базу данных Firebase
    private FirebaseFirestore db;

    // Конструктор класса - инициализирована аутентификация и база данных
    public AuthManager() {
        FirebaseHelper helper = FirebaseHelper.getInstance();
        this.auth = helper.getAuth();
        this.db = helper.getFirestore();
    }

    /**
     * Вход пользователя в систему
     */
    public void login(String email, String password, AuthCallback callback) {
        // Выполнена попытка входа с email и паролем
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    // Получен пользователь из результата
                    FirebaseUser firebaseUser = result.getUser();
                    
                    // Проверено что пользователь существует
                    if (firebaseUser != null) {
                        // Загружены данные пользователя из базы данных
                        loadUser(firebaseUser.getUid(), callback);
                    } else {
                        // Возвращена ошибка
                        callback.onFailure("Ошибка входа");
                    }
                })
                .addOnFailureListener(e -> {
                    // Преобразовано сообщение об ошибке в понятный формат
                    callback.onFailure(getErrorMessage(e.getMessage()));
                });
    }

    /**
     * Регистрация нового пользователя в системе
     * Требуется проверка кода доступа
     */
    public void register(String name, String email, String password, String code, AuthCallback callback) {
        // Проверен код доступа перед созданием пользователя
        checkAccessCode(code, new VerifyCallback() {
            @Override
            public void onSuccess() {
                // Создан новый пользователь после успешной проверки
                createUser(name, email, password, callback);
            }

            @Override
            public void onFailure(String error) {
                // Возвращена ошибка проверки кода
                callback.onFailure(error);
            }
        });
    }

    /**
     * Проверка правильности введенного кода доступа
     */
    private void checkAccessCode(String code, VerifyCallback callback) {
        // Получен документ с кодом доступа из базы данных
        db.collection("settings")
                .document("access_code")
                .get()
                .addOnSuccessListener(doc -> {
                    // Проверено существование настройки
                    if (doc.exists()) {
                        // Получен правильный код из базы
                        String correctCode = doc.getString("code");
                        
                        // Сравнен введенный код с правильным
                        if (code.equals(correctCode)) {
                            // Код верный - продолжение регистрации
                            callback.onSuccess();
                        } else {
                            // Код неверный - ошибка
                            callback.onFailure("Неверный код доступа");
                        }
                    } else {
                        // Код доступа не настроен в системе
                        callback.onFailure("Код доступа не настроен");
                    }
                })
                .addOnFailureListener(e -> {
                    // Ошибка при чтении из базы данных
                    callback.onFailure("Ошибка проверки кода");
                });
    }

    /**
     * Создание нового пользователя в системе аутентификации
     */
    private void createUser(String name, String email, String password, AuthCallback callback) {
        // Создан пользователь с email и паролем
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    // Получен созданный пользователь
                    FirebaseUser firebaseUser = result.getUser();
                    
                    // Проверено что пользователь создан
                    if (firebaseUser != null) {
                        // Сохранены данные пользователя в базе
                        saveUserToDb(firebaseUser.getUid(), name, email, callback);
                    } else {
                        // Возвращена ошибка создания
                        callback.onFailure("Ошибка создания пользователя");
                    }
                })
                .addOnFailureListener(e -> {
                    // Преобразовано сообщение об ошибке
                    callback.onFailure(getErrorMessage(e.getMessage()));
                });
    }

    /**
     * Сохранение данных пользователя в базу данных
     */
    private void saveUserToDb(String userId, String name, String email, AuthCallback callback) {
        // Создан новый документ пользователя в базе данных
        // Используется set() для создания нового документа с данными
        User user = new User(userId, name, email, "master");
        
        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Возвращен успешный результат
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    // Возвращена ошибка сохранения
                    callback.onFailure("Ошибка сохранения данных");
                });
    }

    /**
     * Загрузка данных пользователя из базы данных
     */
    private void loadUser(String userId, AuthCallback callback) {
        // Получен документ пользователя из базы
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    // Проверено существование пользователя
                    if (doc.exists()) {
                        // Преобразован документ в объект User
                        User user = doc.toObject(User.class);
                        // Возвращены данные пользователя
                        callback.onSuccess(user);
                    } else {
                        // Пользователь не найден в базе
                        callback.onFailure("Пользователь не найден");
                    }
                })
                .addOnFailureListener(e -> {
                    // Ошибка при загрузке данных
                    callback.onFailure("Ошибка загрузки данных");
                });
    }

    /**
     * Выход пользователя из системы
     */
    public void logout() {
        // Выполнен выход из аккаунта
        auth.signOut();
    }

    /**
     * Получение текущего авторизованного пользователя
     */
    public FirebaseUser getCurrentUser() {
        // Возвращен текущий пользователь
        return auth.getCurrentUser();
    }

    /**
     * Получение роли текущего пользователя (admin или master)
     */
    public com.google.android.gms.tasks.Task<String> getCurrentUserRole() {
        // Получен текущий пользователь
        FirebaseUser user = auth.getCurrentUser();
        
        // Проверено что пользователь авторизован
        if (user == null) {
            // Возвращен null если пользователь не авторизован
            return com.google.android.gms.tasks.Tasks.forResult(null);
        }
        
        // Получены данные пользователя из базы
        return db.collection("users").document(user.getUid()).get()
                .continueWith(task -> {
                    // Проверен успех операции
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Возвращена роль пользователя
                        return task.getResult().getString("role");
                    }
                    return null;
                });
    }

    /**
     * Преобразование технического сообщения об ошибке в понятное для пользователя
     */
    private String getErrorMessage(String error) {
        // Проверено что сообщение не пустое
        if (error == null) {
            return "Неизвестная ошибка";
        }
        
        // Проверены различные типы ошибок
        
        // Ошибка: пользователь не найден
        if (error.contains("no user") || error.contains("not found")) {
            return "Пользователь не найден";
        }
        
        // Ошибка: неверный пароль
        if (error.contains("password") || error.contains("wrong-password")) {
            return "Неверный пароль";
        }
        
        // Ошибка: неверный формат email
        if (error.contains("email") && error.contains("badly")) {
            return "Неверный формат email";
        }
        
        // Ошибка: email уже занят
        if (error.contains("already in use")) {
            return "Email уже используется";
        }
        
        // Ошибка: слабый пароль
        if (error.contains("weak-password") || error.contains("at least")) {
            return "Пароль слишком короткий";
        }
        
        // Ошибка: нет интернета
        if (error.contains("network")) {
            return "Нет подключения к интернету";
        }
        
        // Другая ошибка - возвращено оригинальное сообщение
        return "Ошибка: " + error;
    }

    /**
     * Интерфейс для обработки результатов аутентификации
     */
    public interface AuthCallback {
        // Вызывается при успешной аутентификации
        void onSuccess(User user);
        // Вызывается при ошибке
        void onFailure(String error);
    }

    /**
     * Интерфейс для обработки результатов проверки кода доступа
     */
    public interface VerifyCallback {
        // Вызывается при успешной проверке
        void onSuccess();
        // Вызывается при ошибке
        void onFailure(String error);
    }
}
