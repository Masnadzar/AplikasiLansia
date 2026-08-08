package com.alya.aplikasilansia.ui.bloodpressure;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.BloodPressure;

import java.util.List;

public class BloodPressureAdapter extends RecyclerView.Adapter<BloodPressureAdapter.ViewHolder> {
    private List<BloodPressure> bloodPressureList;
    public void updateList(List<BloodPressure> newItems) {
        bloodPressureList.clear(); // Clear the existing items
        bloodPressureList.addAll(newItems);
        notifyDataSetChanged();
    }
    public BloodPressureAdapter (List<BloodPressure> bloodPressureList) {
        this.bloodPressureList = bloodPressureList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bp_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodPressure bloodPressure = bloodPressureList.get(position);
        holder.bind(bloodPressure);
    }

    @Override
    public int getItemCount() {
        return bloodPressureList.size();
    }

    public void setBloodPressureList(List<BloodPressure> bloodPressureList) {
        this.bloodPressureList = bloodPressureList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView bloodPressureTV;
        private TextView pulseTV;
        private TextView dateTV;

        public ViewHolder (@NonNull View itemView) {
            super(itemView);
            bloodPressureTV = itemView.findViewById(R.id.tv_blood_pressure);
            pulseTV = itemView.findViewById(R.id.tv_pulse);
            dateTV = itemView.findViewById(R.id.tv_date_bp_history);
        }

        public void bind (BloodPressure bloodPressure) {
            bloodPressureTV.setText(bloodPressure.getBloodPressure());
            pulseTV.setText(bloodPressure.getPulse());
            dateTV.setText(bloodPressure.getBpDate());
        }
    }
}
