package com.yourname.carrepairtracker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EditServiceActivity extends Activity {
    private EditText etCustomerName, etVehicleNumber, etService;
    private EditText etDate, etNextDate, etPrice;
    private RadioGroup rgFuelType;
    private Button btnSave;
    private DatabaseHelper databaseHelper;
    private Service currentService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_service);

        databaseHelper = new DatabaseHelper(this);
        currentService = (Service) getIntent().getSerializableExtra("SERVICE");
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        userId = databaseHelper.getUserId(userEmail);

        initializeViews();
        populateFields();

        btnSave.setOnClickListener(v -> updateService());
    }

    private void initializeViews() {
        etCustomerName = findViewById(R.id.etCustomerName);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etService = findViewById(R.id.etService);
        etDate = findViewById(R.id.etDate);
        etNextDate = findViewById(R.id.etNextDate);
        etPrice = findViewById(R.id.etPrice);
        rgFuelType = findViewById(R.id.rgFuelType);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setText("Update Service");
    }

    private void populateFields() {
        etCustomerName.setText(currentService.getCustomerName());
        etVehicleNumber.setText(currentService.getVehicleNumber());
        etService.setText(currentService.getServiceType());
        etDate.setText(currentService.getServiceDate());
        etNextDate.setText(currentService.getNextServiceDate());
        etPrice.setText(String.valueOf(currentService.getPrice()));

        // Set fuel type radio button
        String fuelType = currentService.getFuelType();
        int radioId = -1;
        switch (fuelType.toLowerCase()) {
            case "petrol":
                radioId = R.id.rbPetrol;
                break;
            case "diesel":
                radioId = R.id.rbDiesel;
                break;

            case "electric":
                radioId = R.id.rbElectric;
                break;
        }
        if (radioId != -1) {
            rgFuelType.check(radioId);
        }
    }

    private void updateService() {
        // Get updated values
        String customerName = etCustomerName.getText().toString().trim();
        String vehicleNumber = etVehicleNumber.getText().toString().trim();
        String serviceType = etService.getText().toString().trim();
        String serviceDate = etDate.getText().toString().trim();
        String nextServiceDate = etNextDate.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());

        // Get selected fuel type
        int selectedId = rgFuelType.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        String fuelType = radioButton != null ? radioButton.getText().toString().toLowerCase() : "";

        // Update customer
        int customerId = databaseHelper.getCustomerIdByName(customerName, userId);
        if (customerId == -1) {
            customerId = (int) databaseHelper.addCustomer(customerName, "", "", userId);
        }

        // Update vehicle
        int vehicleId = databaseHelper.getVehicleIdByNumber(vehicleNumber);
        if (vehicleId == -1) {
            vehicleId = (int) databaseHelper.addVehicle(vehicleNumber, fuelType, "", 0, customerId);
        } else {
            // Update existing vehicle if needed
            ContentValues vehicleValues = new ContentValues();
            vehicleValues.put(DatabaseHelper.COLUMN_FUEL_TYPE, fuelType);
            databaseHelper.getWritableDatabase().update(
                    DatabaseHelper.TABLE_VEHICLES,
                    vehicleValues,
                    DatabaseHelper.COLUMN_VEHICLE_ID + "=?",
                    new String[]{String.valueOf(vehicleId)}
            );
        }

        // Update service
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SERVICE_TYPE, serviceType);
        values.put(DatabaseHelper.COLUMN_SERVICE_DATE, serviceDate);
        values.put(DatabaseHelper.COLUMN_NEXT_SERVICE_DATE, nextServiceDate);
        values.put(DatabaseHelper.COLUMN_PRICE, price);
       // values.put(DatabaseHelper.COLUMN_VEHICLE_ID_FK, vehicleId);

        int rowsAffected = databaseHelper.getWritableDatabase().update(
                DatabaseHelper.TABLE_SERVICES,
                values,
                DatabaseHelper.COLUMN_SERVICE_ID + "=?",
                new String[]{String.valueOf(currentService.getId())}
        );

        if (rowsAffected > 0) {
            Toast.makeText(this, "Service updated", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }
}