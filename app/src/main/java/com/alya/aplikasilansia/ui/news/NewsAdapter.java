package com.alya.aplikasilansia.ui.news;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<News> items;
    private OnItemClickListener listener;

    public NewsAdapter(List<News> items) {
        this.items = items;
    }

    public void updateList(List<News> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(News newsItem);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);

        return new NewsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(clickedPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView newsNameTV;
        private TextView newsDate;
        private TextView newsCategory;
        private ImageView newsImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            newsNameTV = itemView.findViewById(R.id.tv_newsTitle);
            newsCategory = itemView.findViewById(R.id.headNewsCat);
            newsDate = itemView.findViewById(R.id.newsDateList);
            newsImage = itemView.findViewById(R.id.newsImg);
        }

        public void bind(News news1) {
            newsNameTV.setText(news1.getName());
            newsCategory.setText(news1.getCategory());
            newsDate.setText(news1.getDate());
            Glide.with(itemView)
                    .load(news1.getImage())
                    .placeholder(R.drawable.img_news)
                    .error(R.drawable.img_news)
                    .centerCrop()

                    .into(newsImage);
        }
    }
}
