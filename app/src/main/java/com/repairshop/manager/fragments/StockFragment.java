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
import com.repairshop.manager.activities.stock.CreateStockActivity;
import com.repairshop.manager.activities.stock.EditStockActivity;
import com.repairshop.manager.adapters.StockAdapter;
import com.repairshop.manager.firebase.FirebaseStockManager;
import com.repairshop.manager.models.StockItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент списка товаров на складе
 * STK-001, STK-002
 */
public class StockFragment extends Fragment implements StockAdapter.OnStockItemClickListener {

    private RecyclerView recyclerViewStock;
    private SearchView searchViewStock;
    private TextView tvEmptyStock;
    private FloatingActionButton fabCreateStock;

    private StockAdapter adapter;
    private FirebaseStockManager stockManager;
    private List<StockItem> allStockItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock, container, false);

        stockManager = new FirebaseStockManager();
        allStockItems = new ArrayList<>();

        setupViews(view);
        setupRecyclerView();
        setupSearchView();
        loadStockItems();

        return view;
    }

    private void setupViews(View view) {
        recyclerViewStock = view.findViewById(R.id.recyclerViewStock);
        searchViewStock = view.findViewById(R.id.searchViewStock);
        tvEmptyStock = view.findViewById(R.id.tvEmptyStock);
        fabCreateStock = view.findViewById(R.id.fabCreateStock);

        fabCreateStock.setOnClickListener(v -> openCreateStockScreen());
    }

    private void setupRecyclerView() {
        adapter = new StockAdapter(this);
        recyclerViewStock.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewStock.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchViewStock.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            adapter.updateStock(allStockItems);
            updateEmptyView();
            return;
        }

        List<StockItem> filteredItems = stockManager.searchStockItems(allStockItems, query);
        adapter.updateStock(filteredItems);
        updateEmptyView();
    }

    private void loadStockItems() {
        stockManager.getAllStockItems().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allStockItems = task.getResult();
                adapter.updateStock(allStockItems);
                updateEmptyView();
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Ошибка загрузки склада", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            tvEmptyStock.setVisibility(View.VISIBLE);
            recyclerViewStock.setVisibility(View.GONE);
        } else {
            tvEmptyStock.setVisibility(View.GONE);
            recyclerViewStock.setVisibility(View.VISIBLE);
        }
    }

    private void openCreateStockScreen() {
        Intent intent = new Intent(getContext(), CreateStockActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStockItemClick(StockItem item) {
        Intent intent = new Intent(getContext(), EditStockActivity.class);
        intent.putExtra("ITEM_ID", item.getItemId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStockItems();
    }
}
