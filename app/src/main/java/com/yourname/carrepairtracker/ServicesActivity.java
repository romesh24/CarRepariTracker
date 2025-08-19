package com.yourname.carrepairtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class ServicesActivity extends Activity {
    private static final int ADD_SERVICE_REQUEST = 1;
    private static final int EDIT_SERVICE_REQUEST = 2;

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private DatabaseHelper databaseHelper;
    private int userId;
    private List<Service> allServices = new ArrayList<>();
    private String userEmail;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        databaseHelper = new DatabaseHelper(getApplicationContext());
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        if (userEmail == null) {
            Toast.makeText(this, "User session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = databaseHelper.getUserId(userEmail);
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        setupButtons();
        setupSearch();
        setupBottomNavigation();
        loadServices();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(new ArrayList<>(), databaseHelper);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ServiceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Service service) {
                openServiceDetails(service);
            }

            @Override
            public void onEditClick(Service service) {
                editService(service);
            }

            @Override
            public void onDeleteClick(Service service) {
                showDeleteConfirmation(service);
            }
        });
    }

    private void setupButtons() {
        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ServicesActivity.this, PostServiceActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivityForResult(intent, ADD_SERVICE_REQUEST);
        });
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterServices(newText);
                return true;
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_post) {
                    Intent postIntent = new Intent(ServicesActivity.this, PostServiceActivity.class);
                    postIntent.putExtra("USER_EMAIL", userEmail);
                    startActivity(postIntent);
                    return true;
                } else if (itemId == R.id.nav_services) {
                    // Already in services activity
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    Intent logoutIntent = new Intent(ServicesActivity.this, LoginActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logoutIntent);
                    finish();
                    return true;
                }

                return false; // Default return if no item matches
            }
        });
        // Set the services item as selected by default
        bottomNavigationView.setSelectedItemId(R.id.nav_services);
    }

    private void openServiceDetails(Service service) {
        try {
            Intent intent = new Intent(this, ServiceDetailActivity.class);
            intent.putExtra("SERVICE", service);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening details", Toast.LENGTH_SHORT).show();
            Log.e("ServicesActivity", "Error starting ServiceDetailActivity", e);
        }
    }

    private void editService(Service service) {
        try {
            Intent intent = new Intent(this, EditServiceActivity.class);
            intent.putExtra("SERVICE", service);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivityForResult(intent, EDIT_SERVICE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Error editing service", Toast.LENGTH_SHORT).show();
            Log.e("ServicesActivity", "Error starting EditServiceActivity", e);
        }
    }

    private void showDeleteConfirmation(Service service) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Delete service record for " + service.getVehicleNumber() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteService(service))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteService(Service service) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return databaseHelper.deleteService(service.getId());
                } catch (Exception e) {
                    Log.e("ServicesActivity", "Error deleting service", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(ServicesActivity.this, "Service deleted", Toast.LENGTH_SHORT).show();
                    loadServices();
                } else {
                    Toast.makeText(ServicesActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void loadServices() {
        new AsyncTask<Void, Void, List<Service>>() {
            @Override
            protected List<Service> doInBackground(Void... voids) {
                List<Service> services = new ArrayList<>();
                Cursor cursor = null;
                try {
                    cursor = databaseHelper.getAllServicesWithDetails(userId);
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            Service service = parseServiceFromCursor(cursor);
                            if (service != null) {
                                services.add(service);
                            }
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e("ServicesActivity", "Error loading services", e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return services;
            }

            @Override
            protected void onPostExecute(List<Service> services) {
                allServices.clear();
                allServices.addAll(services);
                adapter.updateServices(allServices);
            }
        }.execute();
    }

    private Service parseServiceFromCursor(Cursor cursor) {
        try {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_ID);
            int vehicleNumberIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VEHICLE_NUMBER);
            int serviceDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_DATE);
            int customerNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CUSTOMER_NAME);
            int serviceTypeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_TYPE);
            int nextServiceDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NEXT_SERVICE_DATE);
            int priceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRICE);
            int modelIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MODEL);
            int fuelTypeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FUEL_TYPE);

            if (idIndex == -1 || vehicleNumberIndex == -1 || serviceDateIndex == -1 ||
                    customerNameIndex == -1 || serviceTypeIndex == -1 ||
                    nextServiceDateIndex == -1 || priceIndex == -1 ||
                    modelIndex == -1 || fuelTypeIndex == -1) {
                throw new IllegalStateException("Missing required columns in cursor");
            }

            int id = cursor.getInt(idIndex);
            String vehicleNumber = cursor.getString(vehicleNumberIndex);
            String serviceDate = cursor.getString(serviceDateIndex);
            String customerName = cursor.getString(customerNameIndex);
            String serviceType = cursor.getString(serviceTypeIndex);
            String nextServiceDate = cursor.getString(nextServiceDateIndex);
            double price = cursor.getDouble(priceIndex);
            String carModel = cursor.getString(modelIndex);
            String fuelType = cursor.getString(fuelTypeIndex);

            return new Service(id, customerName, vehicleNumber, carModel,
                    fuelType, serviceType, serviceDate, nextServiceDate, price);
        } catch (Exception e) {
            Log.e("ServicesActivity", "Error parsing service from cursor", e);
            return null;
        }
    }

    private void filterServices(String query) {
        List<Service> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(allServices);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Service service : allServices) {
                if (service.getVehicleNumber().toLowerCase().contains(lowerCaseQuery) ||
                        service.getServiceDate().toLowerCase().contains(lowerCaseQuery) ||
                        service.getCustomerName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(service);
                }
            }
        }
        adapter.updateServices(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_SERVICE_REQUEST || requestCode == EDIT_SERVICE_REQUEST)
                && resultCode == RESULT_OK) {
            loadServices();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServices();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}