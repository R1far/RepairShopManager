package com.repairshop.manager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.repairshop.manager.R;
import com.repairshop.manager.activities.auth.LoginActivity;
import com.repairshop.manager.firebase.AuthManager;
import com.repairshop.manager.fragments.OrdersFragment;
import com.repairshop.manager.fragments.StockFragment;
import com.repairshop.manager.fragments.HistoryFragment;

/**
 * Главный экран приложения
 * Временная заглушка для модуля AUTH
 */
public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager();
        fragmentManager = getSupportFragmentManager();
        
        // Проверка авторизации
        checkAuthentication();
        
        setupViews();
        
        // Загрузить OrdersFragment по умолчанию
        if (savedInstanceState == null) {
            loadFragment(new OrdersFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_orders);
        }
    }

    private void checkAuthentication() {
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            // Пользователь не авторизован, вернуться на экран входа
            navigateToLogin();
        }
    }

    private void setupViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_orders) {
                loadFragment(new OrdersFragment());
                return true;
            }
            
            if (itemId == R.id.nav_stock) {
                loadFragment(new StockFragment());
                return true;
            }
            
            if (itemId == R.id.nav_services) {
                loadFragment(new com.repairshop.manager.fragments.ServiceFragment());
                return true;
            }

            if (itemId == R.id.nav_history) {
                loadFragment(new HistoryFragment());
                return true;
            }
            
            if (itemId == R.id.nav_employees) {
                loadFragment(new com.repairshop.manager.fragments.EmployeesFragment());
                return true;
            }
            
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_logout) {
            performLogout();
            return true;
        } else if (itemId == R.id.action_settings) {
            Toast.makeText(this, "Модуль Настройки в разработке", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * Выход из системы
     */
    private void performLogout() {
        authManager.logout();
        Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
