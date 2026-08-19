package com.alya.aplikasilansia.ui.berita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class BeritaAdapter extends RecyclerView.Adapter<BeritaAdapter.ViewHolder> {

    private List<BeritaItem> items;
    private OnItemClickListener listener;

    public BeritaAdapter(List<BeritaItem> items) {
        this.items = items;
    }

    public void updateList(List<BeritaItem> newItems) {
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(BeritaItem item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_berita, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BeritaItem item = items.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(items.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvJudul, tvTanggal, tvKategori, tvBadge;
        private final ImageView imgBerita;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tvJudulBerita);
            tvTanggal = itemView.findViewById(R.id.tvTanggalBerita);
            tvKategori = itemView.findViewById(R.id.tvKategoriBerita);
            tvBadge = itemView.findViewById(R.id.tvBadgeType);
            imgBerita = itemView.findViewById(R.id.imgBerita);
        }

        public void bind(BeritaItem item) {
            tvJudul.setText(item.getTitle());
            tvTanggal.setText(item.getDate());
            tvKategori.setText(item.getCategory());

            if (item.isTips()) {
                tvBadge.setText("Tips");
                tvBadge.setBackgroundResource(R.drawable.bg_badge_tips);
            } else {
                tvBadge.setText("Berita");
                tvBadge.setBackgroundResource(R.drawable.bg_badge_berita);
            }

            Glide.with(itemView)
                    .load(item.getImage())
                    .placeholder(R.drawable.img_news)
                    .error(R.drawable.img_news)
                    .centerCrop()
                    .into(imgBerita);
        }
    }
}