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

import com.repairshop.manager.R;
import com.repairshop.manager.activities.orders.EditOrderActivity;
import com.repairshop.manager.adapters.OrdersAdapter;
import com.repairshop.manager.firebase.FirebaseOrderManager;
import com.repairshop.manager.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * HIS-001: Фрагмент архива заказов
 * HIS-002: Поиск в архиве
 */
public class HistoryFragment extends Fragment implements OrdersAdapter.OnOrderClickListener {

    private RecyclerView recyclerViewHistory;
    private SearchView searchViewHistory;
    private TextView tvEmptyHistory;

    private OrdersAdapter adapter;
    private FirebaseOrderManager orderManager;
    private List<Order> allOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        orderManager = new FirebaseOrderManager();
        allOrders = new ArrayList<>();

        setupViews(view);
        setupRecyclerView();
        setupSearchView();
        loadOrders();

        return view;
    }

    private void setupViews(View view) {
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        searchViewHistory = view.findViewById(R.id.searchViewHistory);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
    }

    private void setupRecyclerView() {
        adapter = new OrdersAdapter(this);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchViewHistory.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        // Используем тот же метод поиска, так как критерии (телефон, имя) совпадают
        List<Order> filteredOrders = orderManager.searchOrders(allOrders, query);
        adapter.updateOrders(filteredOrders);
        updateEmptyView();
    }

    private void loadOrders() {
        orderManager.getArchiveOrders().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allOrders = task.getResult();
                adapter.updateOrders(allOrders);
                updateEmptyView();
            } else {
                Toast.makeText(getContext(), "Ошибка загрузки архива", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        // Открываем тот же экран редактирования/просмотра
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
