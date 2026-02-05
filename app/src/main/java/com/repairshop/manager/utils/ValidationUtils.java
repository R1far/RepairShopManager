package com.repairshop.manager.utils;



/**
 * Класс с утилитами для проверки корректности введенных данных
 * Содержит методы валидации email, паролей и обязательных полей
 */
public class ValidationUtils {

    // Стандартный паттерн для email, чтобы не зависеть от android.util.Patterns в unit-тестах
    private static final String EMAIL_PATTERN = 
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+";

    /**
     * Проверка корректности формата email адреса
     */
    public static boolean isValidEmail(String email) {
        // Проверено что email не пустой и соответствует стандартному формату
        if (email == null || email.isEmpty()) {
            return false;
        }
        return java.util.regex.Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    /**
     * Проверка корректности пароля (минимум 6 символов)
     */
    public static boolean isValidPassword(String password) {
        // Проверено что пароль не пустой и содержит минимум 6 символов
        return password != null && password.length() >= 6;
    }

    /**
     * Проверка что поле не пустое
     */
    public static boolean isFieldEmpty(String field) {
        // Проверено что поле пустое или содержит только пробелы
        return field == null || field.trim().isEmpty();
    }

    /**
     * Проверка корректности полного имени (минимум 2 символа, без цифр)
     */
    public static boolean isValidFullName(String fullName) {
        // Проверено что имя не пустое и содержит минимум 2 символа
        if (fullName == null || fullName.trim().length() < 2) {
            return false;
        }
        
        // Проверка на отсутствие цифр
        for (char c : fullName.toCharArray()) {
            if (Character.isDigit(c)) {
                return false;
            }
        }
        
        return true;
    }
}
