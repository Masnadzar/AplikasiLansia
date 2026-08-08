package com.alya.aplikasilansia.ui.news;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class NewsFragment extends Fragment implements NewsAdapter.OnItemClickListener {
    NewsViewModel newsViewModel;
    NewsAdapter adapter;

    private CardView headNews;
    private RecyclerView newsRV;
    private ImageView newsImage;
    private TextView tvNewsTitle, tvCategory, tvDate;

    public NewsFragment() {
        // Required empty public constructor
    }


    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsViewModel = new ViewModelProvider(this).get(NewsViewModel.class);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        headNews = view.findViewById(R.id.headNews);
        newsImage = view.findViewById(R.id.headNewsImg);
        tvNewsTitle = view.findViewById(R.id.headNewsTitle);
        tvCategory = view.findViewById(R.id.headNewsCat);
        tvDate = view.findViewById(R.id.headNewsDate);

        newsRV = view.findViewById(R.id.rv_news_list);

        headNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for head news item
                News headNewsItem = (News) headNews.getTag();
                if (headNewsItem != null) {
                    Intent intent = new Intent(getActivity(), NewsContentActivity.class);
                    intent.putExtra("news_name", headNewsItem.getName());
                    intent.putExtra("news_date", headNewsItem.getDate());
                    intent.putExtra("news_category", headNewsItem.getCategory());
                    intent.putExtra("news_source", headNewsItem.getSource());
                    intent.putExtra("news_image", headNewsItem.getImage().toString()); // Assuming Uri to String conversion
                    intent.putExtra("news_content", headNewsItem.getNewsContent()); // Assuming Uri to String conversion
                    startActivity(intent);
                }
            }
        });

        newsRecyclerViewList(); // Initialize with empty list

        newsViewModel.getNewsLiveData().observe(getViewLifecycleOwner(), newsList -> {
            if (newsList != null && !newsList.isEmpty()) {
                // Set the first news item to the headNews view
                setHeadNews(newsList.get(0));

                // Pass the rest of the news items to the adapter
                adapter.updateList(newsList.subList(1, newsList.size()));
            }
        });

        return view;
    }

    private void setHeadNews(News newsItem) {
        if (newsItem != null) {
            tvNewsTitle.setText(newsItem.getName());
            tvCategory.setText(newsItem.getCategory());
            tvDate.setText(newsItem.getDate());
            // Use Picasso or Glide to load the image
            Glide.with(this)
                    .load(newsItem.getImage().toString())
                    .into(newsImage);
            // Store the news item in the headNews tag for later use
            headNews.setTag(newsItem);
        }
    }

    private void newsRecyclerViewList() {
        newsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new NewsAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this); // Set click listener
        newsRV.setAdapter(adapter);
    }

    @Override
    public void onItemClick(News newsItem) {
        // Handle item click here, navigate to NewsContentActivity with news details
        Intent intent = new Intent(getActivity(), NewsContentActivity.class);
        // Pass necessary data to NewsContentActivity using intent extras
        intent.putExtra("news_name", newsItem.getName());
        intent.putExtra("news_date", newsItem.getDate());
        intent.putExtra("news_category", newsItem.getCategory());
        intent.putExtra("news_source", newsItem.getSource());
        intent.putExtra("news_image", newsItem.getImage().toString()); // Assuming Uri to String conversion
        intent.putExtra("news_content", newsItem.getNewsContent()); // Assuming Uri to String conversion
        startActivity(intent);
    }
}