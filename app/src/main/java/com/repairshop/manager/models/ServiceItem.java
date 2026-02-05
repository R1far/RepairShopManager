package com.repairshop.manager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Модель услуги
 * SRV-001, SRV-002, SRV-003, SRV-004, SRV-005
 */
public class ServiceItem {
    @DocumentId
    private String serviceId;
    private String serviceName;
    private double price;

    @ServerTimestamp
    private Timestamp createdAt;

    @ServerTimestamp
    private Timestamp updatedAt;

    // Пустой конструктор для Firebase
    public ServiceItem() {
    }

    // Конструктор для создания новой услуги
    public ServiceItem(String serviceName, double price) {
        this.serviceName = serviceName;
        this.price = price;
    }

    // Геттеры и сеттеры
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ServiceItem{" +
                "serviceId='" + serviceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", price=" + price +
                '}';
    }
}
