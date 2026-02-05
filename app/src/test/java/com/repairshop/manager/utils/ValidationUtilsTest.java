package com.repairshop.manager.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUtilsTest {

    // Таблица 1. Тестирование валидации ФИО
    @Test
    public void isValidFullName_correct() {
        assertTrue(ValidationUtils.isValidFullName("Иван Иванов"));
    }

    @Test
    public void isValidFullName_withDigits() {
        assertFalse("Имя не должно содержать цифр", ValidationUtils.isValidFullName("Иван 1"));
    }

    @Test
    public void isValidFullName_short() {
        // ID 3: Проверка слишком короткого имени -> False
        assertFalse(ValidationUtils.isValidFullName("Я"));
    }

    // Таблица 2. Тестирование валидации Email
    @Test
    public void isValidEmail_correct() {
        // ID 1: Проверка корректного email -> True
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
    }

    @Test
    public void isValidEmail_noAtSymbol() {
        // ID 2: Проверка email без символа @ -> False
        assertFalse(ValidationUtils.isValidEmail("userexample.com"));
    }

    @Test
    public void isValidEmail_empty() {
        // ID 3: Проверка пустого поля -> False
        assertFalse(ValidationUtils.isValidEmail(""));
    }

    // Таблица 3. Тестирование валидации пароля
    @Test
    public void isValidPassword_correctLength() {
        // ID 1: Проверка пароля нормальной длины -> True
        assertTrue(ValidationUtils.isValidPassword("password123"));
    }

    @Test
    public void isValidPassword_short() {
        // ID 2: Проверка короткого пароля -> False
        assertFalse(ValidationUtils.isValidPassword("12345"));
    }

    @Test
    public void isValidPassword_null() {
        // ID 3: Проверка null значения -> False
        assertFalse(ValidationUtils.isValidPassword(null));
    }
}
