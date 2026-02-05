package com.repairshop.manager.models;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class OrderTest {

    // Таблица 4. Тестирование расчета стоимости
    @Test
    public void calculateTotal_servicesAndParts() {
        // ID 1: Услуги: 1000, Запчасти: 300 -> Total: 1300
        Order order = new Order();
        
        List<ServiceItem> services = new ArrayList<>();
        services.add(new ServiceItem("Service 1", 1000.0));
        order.setSelectedServices(services);
        
        List<StockItem> parts = new ArrayList<>();
        parts.add(new StockItem("ART-1", "Part 1", 1, 300.0));
        order.setSelectedParts(parts);
        
        order.calculateTotal();
        
        assertEquals(1300.0, order.getTotalPrice(), 0.001);
    }

    @Test
    public void calculateTotal_servicesOnly() {
        // ID 2: Услуги: 2000, Запчасти: 0 -> Total: 2000
        Order order = new Order();
        
        List<ServiceItem> services = new ArrayList<>();
        services.add(new ServiceItem("Service 1", 2000.0));
        order.setSelectedServices(services);
        

        order.setSelectedParts(new ArrayList<>());
        
        order.calculateTotal();
        
        assertEquals(2000.0, order.getTotalPrice(), 0.001);
    }

    @Test
    public void calculateTotal_empty() {
        // ID 3: Услуги: 0, Запчасти: 0 -> Total: 0
        Order order = new Order();
        
        order.calculateTotal();
        
        assertEquals(0.0, order.getTotalPrice(), 0.001);
    }
}
