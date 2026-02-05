package com.repairshop.manager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Модель товара на складе
 */
public class StockItem {
    // @DocumentId удален, чтобы разрешить сериализацию в списке заказов
    private String itemId;
    private String articleNumber;
    private String itemName;
    private int quantity;
    private double price;
    
    @ServerTimestamp
    private Timestamp createdAt;
    
    @ServerTimestamp
    private Timestamp updatedAt;

    // Пустой конструктор для Firebase
    public StockItem() {
    }

    // Конструктор для создания нового товара
    public StockItem(String articleNumber, String itemName, int quantity, double price) {
        this.articleNumber = articleNumber;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    // Геттеры и сеттеры
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        return "StockItem{" +
                "itemId='" + itemId + '\'' +
                ", articleNumber='" + articleNumber + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
