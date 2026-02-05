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
import com.repairshop.manager.activities.service.CreateServiceActivity;
import com.repairshop.manager.activities.service.EditServiceActivity;
import com.repairshop.manager.adapters.ServiceAdapter;
import com.repairshop.manager.firebase.FirebaseServiceManager;
import com.repairshop.manager.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент списка услуг
 * SRV-001, SRV-002
 */
public class ServiceFragment extends Fragment implements ServiceAdapter.OnServiceItemClickListener {

    private RecyclerView recyclerViewService;
    private SearchView searchViewService;
    private TextView tvEmptyService;
    private FloatingActionButton fabCreateService;

    private ServiceAdapter adapter;
    private FirebaseServiceManager serviceManager;
    private com.repairshop.manager.firebase.AuthManager authManager;
    private List<ServiceItem> allServiceItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_list, container, false);

        serviceManager = new FirebaseServiceManager();
        authManager = new com.repairshop.manager.firebase.AuthManager();
        allServiceItems = new ArrayList<>();

        setupViews(view);
        setupRecyclerView();
        setupSearchView();
        checkUserRole();
        loadServiceItems();

        return view;
    }

    private void setupViews(View view) {
        recyclerViewService = view.findViewById(R.id.recyclerViewService);
        searchViewService = view.findViewById(R.id.searchViewService);
        tvEmptyService = view.findViewById(R.id.tvEmptyService);
        fabCreateService = view.findViewById(R.id.fabCreateService);

        fabCreateService.setOnClickListener(v -> openCreateServiceScreen());
    }

    private void checkUserRole() {
        authManager.getCurrentUserRole().addOnSuccessListener(role -> {
            if ("admin".equals(role)) {
                fabCreateService.setVisibility(View.VISIBLE);
            } else {
                fabCreateService.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ServiceAdapter(this);
        recyclerViewService.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewService.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchViewService.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            adapter.updateServices(allServiceItems);
            updateEmptyView();
            return;
        }

        List<ServiceItem> filteredItems = serviceManager.searchServices(allServiceItems, query);
        adapter.updateServices(filteredItems);
        updateEmptyView();
    }

    private void loadServiceItems() {
        serviceManager.getAllServices().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allServiceItems = task.getResult();
                adapter.updateServices(allServiceItems);
                updateEmptyView();
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Ошибка загрузки услуг", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            tvEmptyService.setVisibility(View.VISIBLE);
            recyclerViewService.setVisibility(View.GONE);
        } else {
            tvEmptyService.setVisibility(View.GONE);
            recyclerViewService.setVisibility(View.VISIBLE);
        }
    }

    private void openCreateServiceScreen() {
        Intent intent = new Intent(getContext(), CreateServiceActivity.class);
        startActivity(intent);
    }

    @Override
    public void onServiceItemClick(ServiceItem item) {
        Intent intent = new Intent(getContext(), EditServiceActivity.class);
        intent.putExtra("SERVICE_ID", item.getServiceId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadServiceItems();
    }
}
