package com.repairshop.manager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Модель пользователя для RepairShopManager
 * AUTH-002, AUTH-004
 */
public class User {
    private String userId;
    private String fullName;
    private String email;
    private String role; // "master" или "admin"
    
    @ServerTimestamp
    private Timestamp createdAt;

    // Пустой конструктор для Firebase
    public User() {
    }

    // Конструктор для создания нового пользователя
    public User(String userId, String fullName, String email, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // Геттеры и сеттеры
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
