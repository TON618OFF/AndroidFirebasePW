package com.example.firebasepw;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users;
    private Consumer<User> onEditClick;
    private Consumer<User> onDeleteClick;

    public UsersAdapter(List<User> users, Consumer<User> onEditClick, Consumer<User> onDeleteClick) {
        this.users = users;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.emailTextView.setText(user.getEmail());
        holder.nameTextView.setText(user.getName());
        holder.roleTextView.setText(user.getRole());

        holder.editButton.setOnClickListener(v -> onEditClick.accept(user));
        holder.deleteButton.setOnClickListener(v -> onDeleteClick.accept(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView, nameTextView, roleTextView;
        Button editButton, deleteButton;

        UserViewHolder(View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}