package com.yourname.carrepairtracker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ServiceDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        Service service = (Service) getIntent().getSerializableExtra("SERVICE");

        TextView vehicleNumber = findViewById(R.id.vehicleNumber);
        TextView customerName = findViewById(R.id.customerName);
        TextView carModel = findViewById(R.id.carModel);
        TextView fuelType = findViewById(R.id.fuelType); // Added
        TextView serviceType = findViewById(R.id.serviceType);
        TextView serviceDate = findViewById(R.id.serviceDate);
        TextView nextServiceDate = findViewById(R.id.nextServiceDate);
        TextView price = findViewById(R.id.price);

        vehicleNumber.setText(service.getVehicleNumber());
        customerName.setText(service.getCustomerName());
        carModel.setText(service.getCarModel());
        fuelType.setText(service.getFuelType()); // Added
        serviceType.setText(service.getServiceType());
        serviceDate.setText(service.getServiceDate());
        nextServiceDate.setText(service.getNextServiceDate());
        price.setText(String.format("$%.2f", service.getPrice()));
    }
}