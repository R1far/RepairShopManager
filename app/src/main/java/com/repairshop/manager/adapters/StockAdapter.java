package com.repairshop.manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.repairshop.manager.R;
import com.repairshop.manager.models.StockItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка товаров на складе в RecyclerView
 * Связывает данные товаров с визуальными элементами
 */
public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    // Список товаров для отображения
    private List<StockItem> stockList;
    // Обработчик нажатий на элементы списка
    private OnStockItemClickListener clickListener;

    /**
     * Интерфейс для обработки нажатий на товары в списке
     */
    public interface OnStockItemClickListener {
        // Вызывается при нажатии на товар
        void onStockItemClick(StockItem item);
    }

    // Конструктор адаптера - инициализирован пустой список и обработчик нажатий
    public StockAdapter(OnStockItemClickListener listener) {
        this.stockList = new ArrayList<>();
        this.clickListener = listener;
    }

    /**
     * Обновление списка товаров новыми данными
     */
    public void updateStock(List<StockItem> newItems) {
        // Обновлен список товаров
        this.stockList = newItems;
        // Уведомлена система о необходимости перерисовки
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создан визуальный элемент из макета
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        // Возвращен новый ViewHolder с этим элементом
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        // Получен товар по позиции
        StockItem item = stockList.get(position);
        // Привязаны данные товара к визуальному элементу
        holder.bind(item, clickListener);
    }

    @Override
    public int getItemCount() {
        // Возвращено количество товаров в списке
        return stockList.size();
    }

    /**
     * Класс для хранения ссылок на визуальные элементы товара
     */
    static class StockViewHolder extends RecyclerView.ViewHolder {
        // Текстовое поле для артикула
        private TextView tvArticleNumber;
        // Текстовое поле для названия товара
        private TextView tvItemName;
        // Текстовое поле для количества
        private TextView tvQuantity;
        // Текстовое поле для цены
        private TextView tvPrice;

        // Конструктор - инициализированы ссылки на элементы
        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArticleNumber = itemView.findViewById(R.id.tvArticleNumber);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        /**
         * Заполнение визуальных элементов данными товара
         */
        public void bind(StockItem item, OnStockItemClickListener listener) {
            // Установлен артикул товара
            tvArticleNumber.setText(item.getArticleNumber());
            // Установлено название товара
            tvItemName.setText(item.getItemName());
            // Установлено количество товара
            tvQuantity.setText(item.getQuantity() + " шт");
            // Установлена цена товара
            tvPrice.setText(String.format("%.0f ₽", item.getPrice()));

            // Установлен обработчик нажатия на элемент
            itemView.setOnClickListener(v -> {
                // Проверено что обработчик существует
                if (listener != null) {
                    // Вызван обработчик нажатия с текущим товаром
                    listener.onStockItemClick(item);
                }
            });
        }
    }
}
