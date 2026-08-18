package com.alya.aplikasilansia.ui.reminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.ReminderHistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// BARU: adapter untuk menampilkan daftar riwayat reminder yang sudah pernah trigger.
public class ReminderHistoryAdapter extends RecyclerView.Adapter<ReminderHistoryAdapter.HistoryViewHolder> {

    private final List<ReminderHistory> items = new ArrayList<>();

    public void updateList(List<ReminderHistory> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvTriggeredAt;
        private final ImageView imgIcon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvDesc = itemView.findViewById(R.id.tv_history_desc);
            tvTriggeredAt = itemView.findViewById(R.id.tv_history_time);
            imgIcon = itemView.findViewById(R.id.img_history_ic);
        }

        public void bind(ReminderHistory history) {
            tvTitle.setText(history.getTitle());
            tvDesc.setText(history.getDesc());
            tvTriggeredAt.setText(formatDate(history.getTriggeredAt()));
            // Kalau icon 0/tidak valid (misal data lama sebelum fitur ini ada), pakai ikon default
            imgIcon.setImageResource(history.getIcon() != 0 ? history.getIcon() : R.drawable.ic_remind_pumpkin);
        }

        private String formatDate(String timestamp) {
            if (timestamp == null) return "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, d MMMM yyyy 'pukul' HH:mm", new Locale("id", "ID"));
            try {
                Date date = sdf.parse(timestamp);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return timestamp;
            }
        }
    }
}