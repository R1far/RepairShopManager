package com.repairshop.manager.activities.orders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.repairshop.manager.R;
import com.repairshop.manager.adapters.OrdersAdapter;
import com.repairshop.manager.firebase.FirebaseOrderManager;
import com.repairshop.manager.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Экран списка заказов
 */
public class OrdersActivity extends AppCompatActivity implements OrdersAdapter.OnOrderClickListener {

    private RecyclerView recyclerViewOrders;
    private SearchView searchViewOrders;
    private TextView tvEmptyOrders;
    private FloatingActionButton fabCreateOrder;

    private OrdersAdapter adapter;
    private FirebaseOrderManager orderManager;
    private List<Order> allOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        orderManager = new FirebaseOrderManager();
        allOrders = new ArrayList<>();

        setupViews();
        setupRecyclerView();
        setupSearchView();
        loadOrders();
    }

    private void setupViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        searchViewOrders = findViewById(R.id.searchViewOrders);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        fabCreateOrder = findViewById(R.id.fabCreateOrder);

        fabCreateOrder.setOnClickListener(v -> openCreateOrderScreen());
    }

    private void setupRecyclerView() {
        adapter = new OrdersAdapter(this);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(adapter);
    }

    /**
     * Поиск заказа
     */
    private void setupSearchView() {
        searchViewOrders.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.updateOrders(allOrders);
            updateEmptyView();
            return;
        }

        List<Order> filteredOrders = orderManager.searchOrders(allOrders, query);
        adapter.updateOrders(filteredOrders);
        updateEmptyView();
    }

    /**
     * Загрузка списка заказов
     */
    private void loadOrders() {
        orderManager.getAllActiveOrders().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allOrders = task.getResult();
                adapter.updateOrders(allOrders);
                updateEmptyView();
            } else {
                Toast.makeText(this, "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Переход к экрану создания заказа
     */
    private void openCreateOrderScreen() {
        Intent intent = new Intent(this, CreateOrderActivity.class);
        startActivity(intent);
    }

    /**
     * Переход к экрану редактирования заказа
     */
    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, EditOrderActivity.class);
        intent.putExtra("ORDER_ID", order.getOrderId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}
