package com.repairshop.manager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Модель заказа на ремонт
 */
public class Order {
    @DocumentId
    private String orderId;
    private String objectName;
    private String clientName;
    private String clientPhone;
    private String problemDescription;
    private String masterName;
    private String masterId;
    private String status;
    
    @ServerTimestamp
    private Timestamp createdAt;
    
    @ServerTimestamp
    private Timestamp updatedAt;

    // Новые поля
    private List<ServiceItem> selectedServices;
    private List<StockItem> selectedParts;
    private double totalPrice;

    // Пустой конструктор для Firebase
    public Order() {
    }

    // Конструктор для создания нового заказа
    public Order(String objectName, String clientName, String clientPhone, 
                 String problemDescription, String masterName, String masterId, String status) {
        this.objectName = objectName;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.problemDescription = problemDescription;
        this.masterName = masterName;
        this.masterId = masterId;
        this.status = status;
        this.selectedServices = new ArrayList<>();
        this.selectedParts = new ArrayList<>();
        this.totalPrice = 0.0;
    }

    // Геттеры и сеттеры
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<ServiceItem> getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(List<ServiceItem> selectedServices) {
        this.selectedServices = selectedServices;
    }

    public List<StockItem> getSelectedParts() {
        return selectedParts;
    }

    public void setSelectedParts(List<StockItem> selectedParts) {
        this.selectedParts = selectedParts;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Расчет общей стоимости заказа
    public void calculateTotal() {
        // Обнулена общая сумма
        double total = 0.0;
        
        // Добавлена стоимость всех услуг
        if (selectedServices != null) {
            int servicesCount = selectedServices.size();
            for (int i = 0; i < servicesCount; i++) {
                ServiceItem service = selectedServices.get(i);
                total = total + service.getPrice();
            }
        }
        
        // Добавлена стоимость всех запчастей
        if (selectedParts != null) {
            int partsCount = selectedParts.size();
            for (int i = 0; i < partsCount; i++) {
                StockItem part = selectedParts.get(i);
                total = total + part.getPrice();
            }
        }
        
        // Сохранена рассчитанная сумма
        this.totalPrice = total;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", objectName='" + objectName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", clientPhone='" + clientPhone + '\'' +
                ", masterName='" + masterName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
