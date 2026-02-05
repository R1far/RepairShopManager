package com.repairshop.manager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.repairshop.manager.R;
import com.repairshop.manager.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка заказов в RecyclerView
 * Связывает данные заказов с визуальными элементами
 */
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    // Список заказов для отображения
    private List<Order> orderList;
    // Обработчик нажатий на заказы
    private OnOrderClickListener clickListener;

    /**
     * Интерфейс для обработки нажатий на заказы в списке
     */
    public interface OnOrderClickListener {
        // Вызывается при нажатии на заказ
        void onOrderClick(Order order);
    }

    // Конструктор адаптера - инициализирован пустой список
    public OrdersAdapter(OnOrderClickListener listener) {
        this.orderList = new ArrayList<>();
        this.clickListener = listener;
    }

    /**
     * Обновление списка заказов новыми данными
     */
    public void updateOrders(List<Order> newOrders) {
        // Обновлен список заказов
        this.orderList = newOrders;
        // Уведомлена система о необходимости перерисовки
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создан визуальный элемент из макета
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent, false);
        // Возвращен новый ViewHolder
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        // Получен заказ по позиции
        Order order = orderList.get(position);
        // Привязаны данные к элементу
        holder.bind(order, clickListener);
    }

    @Override
    public int getItemCount() {
        // Возвращено количество заказов
        return orderList.size();
    }

    /**
     * Класс для хранения ссылок на визуальные элементы заказа
     */
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        // Текстовые поля для отображения данных заказа
        private TextView tvOrderId;
        private TextView tvOrderStatus;
        private TextView tvObjectName;
        private TextView tvClientName;
        private TextView tvClientPhone;
        private TextView tvOrderTotal;
        private TextView tvOrderItems;

        // Конструктор - инициализированы ссылки на элементы
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvObjectName = itemView.findViewById(R.id.tvObjectName);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvClientPhone = itemView.findViewById(R.id.tvClientPhone);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
        }

        /**
         * Заполнение визуальных элементов данными заказа
         */
        public void bind(Order order, OnOrderClickListener listener) {
            // Получен ID заказа
            String orderId = order.getOrderId();
            
            // Установлен короткий ID заказа (первые 6 символов)
            if (orderId != null && orderId.length() > 6) {
                tvOrderId.setText("#" + orderId.substring(0, 6));
            } else {
                tvOrderId.setText("#" + orderId);
            }
            
            // Установлены данные заказа
            tvOrderStatus.setText(order.getStatus());
            tvObjectName.setText(order.getObjectName());
            tvClientName.setText(order.getClientName());
            tvClientPhone.setText(order.getClientPhone());

            // Установлен цвет в зависимости от статуса заказа
            String status = order.getStatus();
            
            // Проверен статус "Новый"
            if (status.equals("Новый")) {
                tvOrderStatus.setBackgroundColor(Color.parseColor("#2196F3"));
            }
            
            // Проверен статус "В работе"
            if (status.equals("В работе")) {
                tvOrderStatus.setBackgroundColor(Color.parseColor("#FF9800"));
            }
            
            // Проверен статус "Готов"
            if (status.equals("Готов")) {
                tvOrderStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            
            // Проверен status "Выдан"
            if (status.equals("Выдан")) {
                tvOrderStatus.setBackgroundColor(Color.parseColor("#9E9E9E"));
            }

            // Установлена общая стоимость заказа
            if (tvOrderTotal != null) {
                tvOrderTotal.setText(order.getTotalPrice() + " ₽");
            }
            
            // Рассчитано и установлено количество услуг и запчастей
            if (tvOrderItems != null) {
                // Получено количество услуг
                int servicesCount = 0;
                if (order.getSelectedServices() != null) {
                    servicesCount = order.getSelectedServices().size();
                }
                
                // Получено количество запчастей
                int partsCount = 0;
                if (order.getSelectedParts() != null) {
                    partsCount = order.getSelectedParts().size();
                }
                
                // Установлен текст с количеством
                tvOrderItems.setText("Усл: " + servicesCount + " | Зап: " + partsCount);
            }

            // Установлен обработчик нажатия
            itemView.setOnClickListener(v -> {
                // Проверено что обработчик существует
                if (listener != null) {
                    // Вызван обработчик с текущим заказом
                    listener.onOrderClick(order);
                }
            });
        }
    }
}
