package com.example.firebasepw;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder> {

    private List<Service> services;
    private List<Service> filteredServices;
    private Consumer<Service> onEditClick;
    private Consumer<Service> onDeleteClick;

    public ServicesAdapter(List<Service> services, Consumer<Service> onEditClick,
                           Consumer<Service> onDeleteClick) {
        this.services = services;
        this.filteredServices = new ArrayList<>(services);
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServiceViewHolder holder, int position) {
        Service service = filteredServices.get(position);
        holder.nameTextView.setText(service.getName());
        holder.categoryTextView.setText(service.getCategory());
        holder.priceTextView.setText(String.format("%.2f", service.getPrice()));

        holder.editButton.setOnClickListener(v -> onEditClick.accept(service));
        holder.deleteButton.setOnClickListener(v -> onDeleteClick.accept(service));
    }

    @Override
    public int getItemCount() {
        return filteredServices.size();
    }

    public void updateList(List<Service> newList) {
        filteredServices.clear();
        filteredServices.addAll(newList);
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, categoryTextView, priceTextView;
        Button editButton, deleteButton;

        ServiceViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}