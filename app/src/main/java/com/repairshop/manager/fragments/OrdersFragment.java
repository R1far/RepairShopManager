package com.repairshop.manager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.repairshop.manager.R;
import com.repairshop.manager.activities.orders.CreateOrderActivity;
import com.repairshop.manager.activities.orders.EditOrderActivity;
import com.repairshop.manager.adapters.OrdersAdapter;
import com.repairshop.manager.firebase.FirebaseOrderManager;
import com.repairshop.manager.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент списка заказов
 * ORD-001, ORD-003
 */
public class OrdersFragment extends Fragment implements OrdersAdapter.OnOrderClickListener {

    private RecyclerView recyclerViewOrders;
    private SearchView searchViewOrders;
    private TextView tvEmptyOrders;
    private FloatingActionButton fabCreateOrder;

    private OrdersAdapter adapter;
    private FirebaseOrderManager orderManager;
    private List<Order> allOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        orderManager = new FirebaseOrderManager();
        allOrders = new ArrayList<>();

        setupViews(view);
        setupRecyclerView();
        setupSearchView();
        loadOrders();

        return view;
    }

    private void setupViews(View view) {
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        searchViewOrders = view.findViewById(R.id.searchViewOrders);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);
        fabCreateOrder = view.findViewById(R.id.fabCreateOrder);

        fabCreateOrder.setOnClickListener(v -> openCreateOrderScreen());
    }

    private void setupRecyclerView() {
        adapter = new OrdersAdapter(this);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(adapter);
    }

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

    private void loadOrders() {
        orderManager.getAllActiveOrders().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allOrders = task.getResult();
                adapter.updateOrders(allOrders);
                updateEmptyView();
            } else {
                Toast.makeText(getContext(), "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
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

    private void openCreateOrderScreen() {
        Intent intent = new Intent(getContext(), CreateOrderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(getContext(), EditOrderActivity.class);
        intent.putExtra("ORDER_ID", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}
