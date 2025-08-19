package com.yourname.carrepairtracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PostServiceActivity extends Activity {
    private EditText etCustomerName, etVehicleNumber, etCarModel, etService, etDate, etNextDate, etPrice;
    private RadioGroup rgFuelType;
    private DatabaseHelper databaseHelper;
    private int userId;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_service);

        databaseHelper = new DatabaseHelper(this);
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        userId = databaseHelper.getUserId(userEmail);

        initializeViews();
        setupDatePickers();
    }

    private void initializeViews() {
        etCustomerName = findViewById(R.id.etCustomerName);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etCarModel = findViewById(R.id.etCarModel);
        etService = findViewById(R.id.etService);
        etDate = findViewById(R.id.etDate);
        etNextDate = findViewById(R.id.etNextDate);
        etPrice = findViewById(R.id.etPrice);
        rgFuelType = findViewById(R.id.rgFuelType);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveService());
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            calendar.set(year, month, day);
            updateDateLabel(etDate);
        };

        DatePickerDialog.OnDateSetListener nextDateListener = (view, year, month, day) -> {
            calendar.set(year, month, day);
            updateDateLabel(etNextDate);
        };

        etDate.setOnClickListener(v -> showDatePicker(dateListener));
        etNextDate.setOnClickListener(v -> showDatePicker(nextDateListener));
    }

    private void saveService() {
        if (!validateForm()) {
            return;
        }

        try {
            String customerName = etCustomerName.getText().toString().trim();
            String vehicleNumber = etVehicleNumber.getText().toString().trim();
            String carModel = etCarModel.getText().toString().trim();
            String serviceType = etService.getText().toString().trim();
            String serviceDate = etDate.getText().toString().trim();
            String nextServiceDate = etNextDate.getText().toString().trim();
            double price = Double.parseDouble(etPrice.getText().toString().trim());

            int selectedId = rgFuelType.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            String fuelType = radioButton != null ? radioButton.getText().toString().toLowerCase() : "";

            // Save customer
            int customerId = databaseHelper.getCustomerIdByName(customerName, userId);
            if (customerId == -1) {
                customerId = (int) databaseHelper.addCustomer(customerName, "", "", userId);
                if (customerId == -1) {
                    showError("Failed to save customer");
                    return;
                }
            }

            // Save vehicle
            int vehicleId = databaseHelper.getVehicleIdByNumber(vehicleNumber);
            if (vehicleId == -1) {
                vehicleId = (int) databaseHelper.addVehicle(vehicleNumber, fuelType, carModel, 0, customerId);
                if (vehicleId == -1) {
                    showError("Failed to save vehicle");
                    return;
                }
            }

            // Save service
            long serviceId = databaseHelper.addService(serviceType, serviceDate, nextServiceDate, price, "", vehicleId);
            if (serviceId != -1) {
                showSuccess("Service saved successfully");
                setResult(RESULT_OK);
                finish();
            } else {
                showError("Failed to save service");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (etCustomerName.getText().toString().trim().isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilCustomerName)).setError("Customer name required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilCustomerName)).setError(null);
        }

        if (etVehicleNumber.getText().toString().trim().isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilVehicleNumber)).setError("Vehicle number required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilVehicleNumber)).setError(null);
        }

        if (etService.getText().toString().trim().isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilService)).setError("Service type required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilService)).setError(null);
        }

        if (etDate.getText().toString().trim().isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilDate)).setError("Service date required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilDate)).setError(null);
        }

        if (etPrice.getText().toString().trim().isEmpty()) {
            ((TextInputLayout) findViewById(R.id.tilPrice)).setError("Price required");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.tilPrice)).setError(null);
        }

        if (rgFuelType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select fuel type", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateDateLabel(EditText editText) {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void showDatePicker(DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}