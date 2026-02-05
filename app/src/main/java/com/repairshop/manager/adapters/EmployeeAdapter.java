package com.repairshop.manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.repairshop.manager.R;
import com.repairshop.manager.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка сотрудников в RecyclerView
 * Связывает данные сотрудников с визуальными элементами
 */
public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    // Список сотрудников для отображения
    private List<User> employees = new ArrayList<>();
    // Полный список сотрудников (для фильтрации)
    private List<User> employeesFull = new ArrayList<>();
    // Роль текущего пользователя (для проверки прав доступа)
    private String currentUserRole;
    // Обработчик действий с сотрудниками
    private OnEmployeeClickListener listener;

    /**
     * Интерфейс для обработки действий с сотрудниками
     */
    public interface OnEmployeeClickListener {
        // Вызывается при нажатии кнопки удаления
        void onDeleteClick(User user);
    }

    // Конструктор адаптера - инициализированы роль и обработчик
    public EmployeeAdapter(String currentUserRole, OnEmployeeClickListener listener) {
        this.currentUserRole = currentUserRole;
        this.listener = listener;
    }

    /**
     * Обновление списка сотрудников новыми данными
     */
    public void setEmployees(List<User> employees) {
        // Обновлен список сотрудников
        this.employees = employees;
        // Сохранена полная копия для фильтрации
        this.employeesFull = new ArrayList<>(employees);
        // Уведомлена система о необходимости перерисовки
        notifyDataSetChanged();
    }

    /**
     * Фильтрация списка сотрудников по запросу
     */
    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Показан полный список
            employees = new ArrayList<>(employeesFull);
        } else {
            // Преобразован запрос в нижний регистр для поиска без учета регистра
            String lowerQuery = query.toLowerCase().trim();
            List<User> filtered = new ArrayList<>();
            
            // Отфильтрованы сотрудники по имени и email
            for (User user : employeesFull) {
                if (user.getFullName().toLowerCase().contains(lowerQuery) ||
                    user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filtered.add(user);
                }
            }
            employees = filtered;
        }
        // Уведомлена система о необходимости перерисовки
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создан визуальный элемент из макета
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        // Возвращен новый ViewHolder
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        // Получен сотрудник по позиции
        User user = employees.get(position);
        // Привязаны данные к элементу
        holder.bind(user, currentUserRole, listener);
    }

    @Override
    public int getItemCount() {
        // Возвращено количество сотрудников
        return employees.size();
    }

    /**
     * Класс для хранения ссылок на визуальные элементы сотрудника
     */
    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        // Текстовые поля для данных сотрудника
        TextView tvName, tvEmail, tvRole;
        // Кнопка удаления сотрудника
        ImageButton btnDelete;

        // Конструктор - инициализированы ссылки на элементы
        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmail = itemView.findViewById(R.id.tvEmployeeEmail);
            tvRole = itemView.findViewById(R.id.tvEmployeeRole);
            btnDelete = itemView.findViewById(R.id.btnDeleteEmployee);
        }

        /**
         * Заполнение визуальных элементов данными сотрудника
         */
        public void bind(User user, String role, OnEmployeeClickListener listener) {
            // Установлено имя сотрудника
            tvName.setText(user.getFullName());
            // Установлен email сотрудника
            tvEmail.setText(user.getEmail());
            
            // Получена роль сотрудника
            String userRole = user.getRole();
            
            // Установлен текст роли на русском языке
            if ("admin".equalsIgnoreCase(userRole)) {
                tvRole.setText("Администратор");
            } else {
                tvRole.setText("Мастер");
            }

            // Проверена роль текущего пользователя для отображения кнопки удаления
            // Удаление доступно только администратору
            if ("admin".equals(role)) {
                // Показана кнопка удаления
                btnDelete.setVisibility(View.VISIBLE);
                // Установлен обработчик нажатия на кнопку удаления
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(user));
            } else {
                // Скрыта кнопка удаления для обычных пользователей
                btnDelete.setVisibility(View.GONE);
            }
        }
    }
}
