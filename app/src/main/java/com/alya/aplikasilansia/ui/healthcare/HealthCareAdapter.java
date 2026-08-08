package com.alya.aplikasilansia.ui.healthcare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;

import java.util.List;

public class HealthCareAdapter extends RecyclerView.Adapter<HealthCareAdapter.ViewHolder> {
    private List<HealthCare> items;

    private Context context;

    public HealthCareAdapter(List<HealthCare> items, Context context) {
        this.items = items;
        this.context = context;
    }    public void updateList(List<HealthCare> newItems) {
        items.clear(); // Clear the existing items
        items.addAll(newItems);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_care, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthCareAdapter.ViewHolder holder, int position) {
        HealthCare healthcare = items.get(position);
        holder.bind(healthcare); // Call bind() method to bind data to views
        holder.itemView.setOnClickListener(v -> {
            String url = healthcare.getUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView addressTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_healthcare_name);
            addressTextView = itemView.findViewById(R.id.tv_healthcare_address);
        }

        public void bind(HealthCare healthcare) {
            nameTextView.setText(healthcare.getName());
            addressTextView.setText(healthcare.getAddress());
        }
    }
}
