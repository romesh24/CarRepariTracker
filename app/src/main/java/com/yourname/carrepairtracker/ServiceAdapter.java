package com.yourname.carrepairtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<Service> serviceList;
    private OnItemClickListener listener;
    private DatabaseHelper databaseHelper;

    public interface OnItemClickListener {
        void onItemClick(Service service);
        void onEditClick(Service service);
        void onDeleteClick(Service service);
    }

    public ServiceAdapter(List<Service> serviceList, DatabaseHelper databaseHelper) {
        this.serviceList = serviceList;
        this.databaseHelper = databaseHelper;
    }

    public void updateServices(List<Service> services) {
        this.serviceList = services;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.vehicleNumber.setText(service.getVehicleNumber());
        holder.serviceDate.setText(service.getServiceDate());

        holder.viewButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(service);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(service);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView vehicleNumber, serviceDate;
        ImageButton viewButton, editButton, deleteButton;

        public ServiceViewHolder(View itemView) {
            super(itemView);
            vehicleNumber = itemView.findViewById(R.id.vehicleNumber);
            serviceDate = itemView.findViewById(R.id.serviceDate);
            viewButton = itemView.findViewById(R.id.viewButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}