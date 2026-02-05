package com.repairshop.manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.repairshop.manager.R;
import com.repairshop.manager.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка услуг в RecyclerView
 * Связывает данные услуг с визуальными элементами
 */
public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    // Список услуг для отображения
    private List<ServiceItem> serviceList;
    // Обработчик нажатий на услуги
    private OnServiceItemClickListener clickListener;

    /**
     * Интерфейс для обработки нажатий на услуги
     */
    public interface OnServiceItemClickListener {
        // Вызывается при нажатии на услугу
        void onServiceItemClick(ServiceItem item);
    }

    // Конструктор адаптера - инициализирован пустой список
    public ServiceAdapter(OnServiceItemClickListener listener) {
        this.serviceList = new ArrayList<>();
        this.clickListener = listener;
    }

    /**
     * Обновление списка услуг новыми данными
     */
    public void updateServices(List<ServiceItem> newItems) {
        // Обновлен список услуг
        this.serviceList = newItems;
        // Уведомлена система о необходимости перерисовки
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создан визуальный элемент из макета
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        // Возвращен новый ViewHolder
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        // Получена услуга по позиции
        ServiceItem item = serviceList.get(position);
        // Привязаны данные к элементу
        holder.bind(item, clickListener);
    }

    @Override
    public int getItemCount() {
        // Возвращено количество услуг
        return serviceList.size();
    }

    /**
     * Класс для хранения ссылок на визуальные элементы услуги
     */
    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        // Текстовое поле для названия услуги
        private TextView tvServiceName;
        // Текстовое поле для цены
        private TextView tvPrice;

        // Конструктор - инициализированы ссылки на элементы
        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        /**
         * Заполнение визуальных элементов данными услуги
         */
        public void bind(ServiceItem item, OnServiceItemClickListener listener) {
            // Установлено название услуги
            tvServiceName.setText(item.getServiceName());
            // Установлена цена услуги
            tvPrice.setText(String.format("%.0f ₽", item.getPrice()));

            // Установлен обработчик нажатия
            itemView.setOnClickListener(v -> {
                // Проверено что обработчик существует
                if (listener != null) {
                    // Вызван обработчик с текущей услугой
                    listener.onServiceItemClick(item);
                }
            });
        }
    }
}
