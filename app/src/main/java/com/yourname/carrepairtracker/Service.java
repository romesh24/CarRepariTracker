package com.yourname.carrepairtracker;

import java.io.Serializable;

public class Service implements Serializable {
    private int id;
    private String customerName;
    private String vehicleNumber;
    private String carModel;
    private String fuelType;  // Added field
    private String serviceType;
    private String serviceDate;
    private String nextServiceDate;
    private double price;

    public Service(int id, String customerName, String vehicleNumber, String carModel,
                   String fuelType, String serviceType, String serviceDate,
                   String nextServiceDate, double price) {
        this.id = id;
        this.customerName = customerName != null ? customerName : "";
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber : "";
        this.carModel = carModel != null ? carModel : "";
        this.fuelType = fuelType != null ? fuelType : "";
        this.serviceType = serviceType != null ? serviceType : "";
        this.serviceDate = serviceDate != null ? serviceDate : "";
        this.nextServiceDate = nextServiceDate != null ? nextServiceDate : "";
        this.price = price;
    }


    public String getFuelType() { return fuelType; }


    // Getters
    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getCarModel() { return carModel; }
    public String getServiceType() { return serviceType; }
    public String getServiceDate() { return serviceDate; }
    public String getNextServiceDate() { return nextServiceDate; }
    public double getPrice() { return price; }
}