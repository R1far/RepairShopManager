package com.repairshop.manager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.repairshop.manager.R;
import com.repairshop.manager.adapters.EmployeeAdapter;
import com.repairshop.manager.firebase.AuthManager;
import com.repairshop.manager.firebase.FirebaseEmployeeManager;
import com.repairshop.manager.firebase.FirebaseSettingsManager;
import com.repairshop.manager.models.User;
import com.repairshop.manager.utils.ValidationUtils;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EmployeesFragment extends Fragment implements EmployeeAdapter.OnEmployeeClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchView;

    private EmployeeAdapter adapter;
    private FirebaseEmployeeManager employeeManager;
    private FirebaseSettingsManager settingsManager;
    private AuthManager authManager;

    // Настройки
    private View layoutSettings;
    private TextInputEditText etAccessCode;
    private View btnSaveCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employees, container, false);

        employeeManager = new FirebaseEmployeeManager();
        settingsManager = new FirebaseSettingsManager();
        authManager = new AuthManager();

        initViews(view);
        
        // Сначала получаем роль текущего пользователя, потом загружаем список
        loadCurrentUserAndData();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewEmployees);
        progressBar = view.findViewById(R.id.progressBarEmployees);
        tvEmpty = view.findViewById(R.id.tvEmptyEmployees);
        searchView = view.findViewById(R.id.searchViewEmployees);

        // Настройки
        layoutSettings = view.findViewById(R.id.layoutSettings);
        etAccessCode = view.findViewById(R.id.etAccessCode);
        btnSaveCode = view.findViewById(R.id.btnSaveCode);

        btnSaveCode.setOnClickListener(v -> saveAccessCode());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Настройка SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });
    }

    private void loadCurrentUserAndData() {
        authManager.getCurrentUserRole().addOnSuccessListener(role -> {
            // Настраиваем адаптер с полученной ролью
            adapter = new EmployeeAdapter(role, this);
            recyclerView.setAdapter(adapter);

            if ("admin".equals(role)) {
                showSettings();
            } else {
                hideSettings();
            }
            
            // Загружаем данные
            loadEmployees();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Ошибка получения прав доступа", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void loadEmployees() {
        progressBar.setVisibility(View.VISIBLE);
        employeeManager.getAllEmployees().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                List<User> users = task.getResult();
                if (users == null || users.isEmpty()) {
                    showEmptyView();
                } else {
                    showDataView(users);
                }
            } else {
                Toast.makeText(getContext(), "Ошибка загрузки сотрудников", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyView() {
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showDataView(List<User> users) {
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.setEmployees(users);
    }

    @Override
    public void onDeleteClick(User user) {
        // Подтверждение удаления
        new AlertDialog.Builder(getContext())
                .setTitle("Удаление сотрудника")
                .setMessage("Вы действительно хотите удалить сотрудника " + user.getFullName() + "?")
                .setPositiveButton("Да", (dialog, which) -> deleteEmployee(user))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteEmployee(User user) {
        // Нельзя удалить самого себя
        if (user.getUserId().equals(authManager.getCurrentUser().getUid())) {
            Toast.makeText(getContext(), "Нельзя удалить свою учетную запись", Toast.LENGTH_SHORT).show();
            return;
        }

        employeeManager.deleteEmployee(user.getUserId())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Сотрудник удален", Toast.LENGTH_SHORT).show();
                    loadEmployees(); // Обновляем список
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show();
                });
    }

    private void showSettings() {
        layoutSettings.setVisibility(View.VISIBLE);
        loadAccessCode();
    }

    private void hideSettings() {
        layoutSettings.setVisibility(View.GONE);
    }

    private void loadAccessCode() {
        settingsManager.getAccessCode(new FirebaseSettingsManager.SettingsCallback<String>() {
            @Override
            public void onSuccess(String code) {
                if (etAccessCode != null) {
                    etAccessCode.setText(code);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Ошибка загрузки кода: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAccessCode() {
        String newCode = etAccessCode.getText().toString().trim();
        if (ValidationUtils.isFieldEmpty(newCode)) {
            etAccessCode.setError("Введите код");
            return;
        }

        settingsManager.updateAccessCode(newCode, new FirebaseSettingsManager.SettingsCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(getContext(), "Код обновлен", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Ошибка сохранения: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
